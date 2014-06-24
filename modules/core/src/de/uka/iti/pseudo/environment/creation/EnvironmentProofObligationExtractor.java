package de.uka.iti.pseudo.environment.creation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.environment.Lemma;
import de.uka.iti.pseudo.environment.Program;
import de.uka.iti.pseudo.environment.ProofObligation;
import de.uka.iti.pseudo.parser.ASTDefaultVisitor;
import de.uka.iti.pseudo.parser.ASTElement;
import de.uka.iti.pseudo.parser.ASTVisitException;
import de.uka.iti.pseudo.parser.file.ASTFile;
import de.uka.iti.pseudo.parser.file.ASTLemmaDeclaration;
import de.uka.iti.pseudo.parser.file.ASTProgramDeclaration;
import de.uka.iti.pseudo.parser.file.ASTRule;
import de.uka.iti.pseudo.rule.Rule;
import de.uka.iti.pseudo.rule.RuleTagConstants;
import de.uka.iti.pseudo.term.Modality;

class EnvironmentProofObligationExtractor extends ASTDefaultVisitor {

    private final Environment env;
    private final Map<String, ProofObligation> proofObligations;
    private final List<Object> availableLemmasAndRules = new ArrayList<Object>();;

    public EnvironmentProofObligationExtractor(Environment env, Map<String, ProofObligation> proofObligations) {
        this.env = env;
        this.proofObligations = proofObligations;
    }

    @Override
    protected void visitDefault(ASTElement arg) throws ASTVisitException {
        // do nothing
    }

    @Override
    public void visit(ASTFile arg) throws ASTVisitException {
        for (ASTElement child : arg.getChildren()) {
            child.visit(this);
        }
    }

    @Override
    public void visit(ASTProgramDeclaration arg) throws ASTVisitException {

        String name = arg.getName().image;
        Program program = env.getProgram(name);

        ProofObligation po = new ProofObligation.ProgramPO(env, program, Modality.BOX);
        proofObligations.put(po.getName(), po);
        po = new ProofObligation.ProgramPO(env, program, Modality.BOX_TERMINATION);
        proofObligations.put(po.getName(), po);

    }

    @Override
    public void visit(ASTLemmaDeclaration arg) throws ASTVisitException {

        String name = arg.getName().image;
        Lemma lemma = env.getLemma(name);
        assert lemma != null;

        if(!lemma.getDefinedProperties().contains("axiom")) {
            ProofObligation po = new ProofObligation.LemmaPO(env, lemma, availableLemmasAndRules);
            proofObligations.put(po.getName(), po);
        }

        availableLemmasAndRules.add(lemma);
    }

    @Override
    public void visit(ASTRule arg) {
        String name = arg.getName().image;

        Rule rule = env.getRule(name);
        assert rule != null;

        if(!rule.getDefinedProperties().contains("axiom")) {
            ProofObligation po = new ProofObligation.RulePO(env, rule, availableLemmasAndRules);
            proofObligations.put(po.getName(), po);
        }

        availableLemmasAndRules.add(rule);

        Lemma lemma = env.getLemma(name);
        if(lemma != null && lemma.getProperty(RuleTagConstants.KEY_GENERATED_AXIOM) != null) {
            availableLemmasAndRules.add(lemma);
        }
    }

}
