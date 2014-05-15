package de.uka.iti.pseudo.proof;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.rule.LocatedTerm;
import de.uka.iti.pseudo.rule.Rule;
import de.uka.iti.pseudo.rule.RuleException;
import de.uka.iti.pseudo.rule.WhereClause;
import de.uka.iti.pseudo.term.Sequent;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.TermException;
import de.uka.iti.pseudo.term.Type;
import de.uka.iti.pseudo.term.Update;
import de.uka.iti.pseudo.term.creation.ProgramComparingTermInstantiator;
import de.uka.iti.pseudo.term.creation.TermInstantiator;
import de.uka.iti.pseudo.util.Log;
import de.uka.iti.pseudo.util.Util;

/**
 * A RuleApplicationCertificate is a rule application that carries with it the
 * certificate that it can be applied to its proof node.
 *
 * Once the applicability has been certified, it needs not be checked a second
 * time. Thus, the wellformedness check is externalised to this class and it
 * needs be made only once.
 *
 * The application is immutable. BUT it has mutable properties in which additional
 * values can be stored. ProofNode.apply checks that only new values are added,
 * never overwritten.
 */
public final class RuleApplicationCertificate extends ImmutableRuleApplication {

    private final Environment env;
    private final Map<String, String> properties;

    private boolean verificationTried;
    private boolean verificationSuccessful;
    private Exception problemException;

    /**
     * Instantiates a new rule application certificate.
     *
     * @param ruleApp
     *            the rule application to certify
     * @param env
     *            the environment in which that happens
     */
    public RuleApplicationCertificate(RuleApplication ruleApp, Environment env) {
        super(ruleApp);

        this.properties = new HashMap<String, String>(ruleApp.getProperties());
        this.verificationTried = false;
        this.env = env;
    }

    @Override
    public Map<String, String> getProperties() {
        return properties;
    }

    @Override
    public boolean hasMutableProperties() {
        return true;
    }

    public Environment getEnvironment() {
        return env;
    }

    /**
     * Checks whether verification has been run already.
     *
     * Calling {@link #verify()} or {@link #ensureVerified()} does not
     * need to do new computations.
     *
     * @return true, if verification has already been run.
     */
    public boolean hasVerificationBeenTried() {
        return verificationTried;
    }

    /**
     * Checks if a previous verification was successful.
     *
     * If verification has not yet been performed, <code>false</code>
     * is returned as a conservative underapproximation.
     *
     * @return true, iff a previous verification was successful
     */
    public boolean wasVerificationSuccessful() {
        return verificationSuccessful;
    }

    /**
     * Verify this certificate.
     *
     * If the process has not been launched, yet, launch it.
     *
     * @return true, if the rule application can be verified.
     */
    public boolean verify() {
        doVerification();
        return wasVerificationSuccessful();
    }

    /**
     * Checks for monotone properties.
     *
     * The values in the original rule application must also be present in this
     * object.
     *
     * @return true, if properties are monotone.
     */
    public boolean hasMonotoneProperties() {
        Map<String, String> superProps = super.getProperties();
        for (String key : superProps.keySet()) {
            if(!Util.equalOrNull(superProps.get(key), properties.get(key))) {
                return false;
            }
        }
        return true;
    }


    /**
     * Verify this certificate.
     *
     * If the process has not been launched, yet, launch it.
     *
     * @throws ProofException
     *             if the verification cannot be done, the exception which
     *             indicates the problem is thrown.
     */
    public void ensureVerified() throws ProofException {
        doVerification();
        if(!wasVerificationSuccessful()) {
            if (problemException instanceof ProofException) {
                throw (ProofException) problemException;
            } else {
                throw new ProofException(problemException);
            }
        }
    }

    synchronized private void doVerification() {
        if(verificationTried) {
            return;
        }

        Sequent sequent = getProofNode().getSequent();
        Map<String, Term> schemaMap = getSchemaVariableMapping();
        Map<String, Type> typeMap = getTypeVariableMapping();
        Map<String, Update> updateMap = getSchemaUpdateMapping();
        TermInstantiator inst = new ProgramComparingTermInstantiator(
                schemaMap, typeMap, updateMap, env);

        Rule rule = getRule();

        assert rule != null : "Rule in RuleApplication must not be null";

        verificationSuccessful = false;
        try {
            matchFindClause(sequent, inst, rule);
            matchAssumeClauses(sequent, inst, rule);
            verifyWhereClauses(inst, rule);
            verificationSuccessful = true;
        } catch (ProofException e) {
            problemException = e;
        } catch (RuntimeException e) {
            problemException = e;
        } finally {
            verificationTried = true;
        }
    }

    /*
     * Match find clause against the sequent.
     *
     * If there is no find clause in this rule, just return.
     */
    private void matchFindClause(Sequent sequent, TermInstantiator inst,
            Rule rule) throws ProofException {

        LocatedTerm findClause = rule.getFindClause();
        if(findClause == null) {
            return;
        }

        TermSelector findSelector = getFindSelector();
        Term findSubTerm = findSelector.selectSubterm(sequent);

        if (!findClause.isFittingSelect(findSelector)) {
            throw new ProofException("Illegal selector for find");
        }

        Term instantiated;
        try {
            instantiated = inst.instantiate(findClause.getTerm());
        } catch (TermException e) {
            throw new ProofException("cannot instantiate find clause", e);
        }

        if(!findSubTerm.equals(instantiated)) {
            throw new ProofException("find clause does not match: \nfind: " +
                    findSubTerm + " \ninstantiated: " + instantiated);
        }

        if(!findClause.isFittingSelect(findSelector)) {
            throw new ProofException("find selector does match find clase: \n" + findClause +
                    "\n" + instantiated + " - " + findSelector);
        }

        OptionalUpdateInstantiationChecker.check(this);
    }

    /*
     * Match assume clauses against the sequent.
     *
     * FIXME start with rule's clauses not with app's clauses
     */
    private void matchAssumeClauses(Sequent sequent,
            TermInstantiator inst, Rule rule) throws ProofException {

        List<TermSelector> assumeSelectors = getAssumeSelectors();
        int length = assumeSelectors.size();

        for (int i = 0; i < length; i++) {
            TermSelector assSel = assumeSelectors.get(i);
            assert assSel.isToplevel();
            Term assumeTerm = assSel.selectTopterm(sequent);
            LocatedTerm assumption = rule.getAssumptions().get(i);
            if (!assumption.isFittingSelect(assSel)) {
                throw new ProofException("Illegal selector for assume (" + i + ")");
            }
            Term instantiated;
            try {
                instantiated = inst.instantiate(assumption.getTerm());
            } catch (TermException e) {
                throw new ProofException("cannot instantiate assume clause", e);
            }
            if(!assumeTerm.equals(instantiated)) {
                throw new ProofException("assumption clause does not match");
            }
        }
    }


    /*
     * Verify where clauses in a rule application using a terminstantiator.
     */
    private void verifyWhereClauses(TermInstantiator inst,
            Rule rule) throws ProofException {
        for (WhereClause whereClause : rule.getWhereClauses()) {
            try {
                if(!whereClause.applyTo(inst, this, env)) {
                    Log.log(Log.ERROR, "WhereClause failed: " + whereClause);
                    Log.log(Log.DEBUG, "Term inst: " + inst);
                    throw new ProofException("WhereClause failed: " + whereClause +
                            ", instantiation: " + inst);
                }
            } catch (RuleException e) {
                Log.log(Log.ERROR, "WhereClause failed: " + whereClause);
                Log.log(Log.DEBUG, "Term inst: " + inst);
                throw new ProofException("WhereClause not applicable: " + whereClause, e);
            }
        }
    }

}
