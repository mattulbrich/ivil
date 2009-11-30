package de.uka.iti.pseudo.justify;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import nonnull.NonNull;
import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.environment.EnvironmentException;
import de.uka.iti.pseudo.environment.Function;
import de.uka.iti.pseudo.environment.Sort;
import de.uka.iti.pseudo.environment.TypeVariableCollector;
import de.uka.iti.pseudo.parser.ASTLocatedElement;
import de.uka.iti.pseudo.parser.file.MatchingLocation;
import de.uka.iti.pseudo.rule.GoalAction;
import de.uka.iti.pseudo.rule.LocatedTerm;
import de.uka.iti.pseudo.rule.Rule;
import de.uka.iti.pseudo.rule.RuleException;
import de.uka.iti.pseudo.rule.GoalAction.Kind;
import de.uka.iti.pseudo.term.Application;
import de.uka.iti.pseudo.term.BindableIdentifier;
import de.uka.iti.pseudo.term.SchemaVariable;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.TermException;
import de.uka.iti.pseudo.term.Type;
import de.uka.iti.pseudo.term.TypeApplication;
import de.uka.iti.pseudo.term.TypeVariable;
import de.uka.iti.pseudo.term.Update;
import de.uka.iti.pseudo.term.Variable;
import de.uka.iti.pseudo.term.creation.DefaultTermVisitor;
import de.uka.iti.pseudo.term.creation.TermFactory;
import de.uka.iti.pseudo.term.creation.TermInstantiator;


// http://www.cs.chalmers.se/~philipp/publications/lfm.pdf
// p. 13

/*
 *  Steps:
 *  <ol>
 *  <li>construct context from <code>assume</code> clauses
 *  <li>Using the context create the meaning formula (extract problem)
 *  <li>Use a {@link SchemaVariableUseVisitor} to collect information on schema variables in that formula.
 *  <li>Skolemize all free type variables to new sorts.
 *  <li>turn bound schema variables to logical variables
 *  <li>turn unbound schema variables to skolem symbols with apropriate parameters
 *  </ol>
 */

public class RuleProblemExtractor {
    
    private static final Term FALSE = Environment.getFalse();
    private static final Term TRUE = Environment.getTrue();

    private Rule rule;
    
    private Term context;
    
    private TermFactory tf;

    private Environment env;

    private Map<String, Term> mapVars = new HashMap<String, Term>();

    private Map<String, Type> mapTypeVars = new HashMap<String, Type>();

    private TermInstantiator termInst = new TermInstantiator(mapVars, mapTypeVars, Collections.<String,Update>emptyMap());

    public RuleProblemExtractor(Rule rule, Environment env) {
        super();
        this.rule = rule;
        this.env = env;
        this.tf = new TermFactory(env);
    }
    
    public Term extractProblem() throws TermException, RuleException, EnvironmentException {
        
        makeContext();

        //
        // formulate rule as a term with schema vars.
        Term problem0;
        if(isRewrite())
            problem0 = extractRewriteProblem();
        else
            problem0 = extractLocatedProblem();
        
        //
        // collect info on schema variables
        SchemaVariableUseVisitor svuv = new SchemaVariableUseVisitor(); 
        problem0.visit(svuv);
        
        //
        // skolem types.
        mapTypeVars.clear();
        solemizeTypes(problem0);
        
        //
        // bound schema variables to variables.
        mapVars.clear();
        mapAllBoundSchemaVars(problem0, svuv.getBoundIdentifiers());
        
        //
        // then skolemize the remainder with according arity.
        mapRemainingSchemaVars(problem0, svuv.getSeenBindablesMap());
        
        //
        // instantiate
        Term problem = termInst.instantiate(problem0);
        
        return problem;
    }
    
    private void solemizeTypes(Term t) throws EnvironmentException, TermException {
        Set<TypeVariable> typeVars = TypeVariableCollector.collect(t);
        for (TypeVariable typeVar : typeVars) {
        	// Type variables become types beginning with skolem
            String name = env.createNewSortName("skolem");
            Sort sort = new Sort(name, 0, ASTLocatedElement.CREATED);
            env.addSort(sort);
            mapTypeVars.put(typeVar.getVariableName(), new TypeApplication(sort));
        }
        
    }
    
    private static class VariableCollector extends DefaultTermVisitor.DepthTermVisitor {
        private Set<String> variableNames = new HashSet<String>();
        public void visit(Variable variable) throws TermException {
            variableNames.add(variable.getName());
        }
        public String freshName(String prefix) {
            String newName = prefix;
            int counter = 1;
            while(variableNames.contains(newName)) {
                newName = prefix + counter;
                counter ++;
            }
            // mark the new name as used now also.
            variableNames.add(newName);
            return newName;
        }
    }
    
    private void mapAllBoundSchemaVars(Term term, Set<BindableIdentifier> boundIdentifiers) throws TermException {
        VariableCollector vc = new VariableCollector();
        term.visit(vc);
        for (BindableIdentifier boundvar : boundIdentifiers) {
            if (boundvar instanceof SchemaVariable) {
                SchemaVariable schemaVar = (SchemaVariable) boundvar;
                String schemaName = schemaVar.getName();
                // remove leading %
                String prefix = schemaName.substring(1);
                // new name is unique
                String newname = vc.freshName(prefix);
                Type instType = termInst.instantiate(schemaVar.getType());
                
                assert TypeVariableCollector.collect(instType).isEmpty() : "Ensure no free type var in instantiation";
                mapVars.put(schemaName, new Variable(newname, instType));
            }
        }
    }

    private void mapRemainingSchemaVars(Term term,
            Map<SchemaVariable, Set<BindableIdentifier>> seenBindablesMap)
            throws TermException, EnvironmentException {
        for (Map.Entry<SchemaVariable, Set<BindableIdentifier>> entry : seenBindablesMap.entrySet()) {
            SchemaVariable sv = entry.getKey();
            // not yet mapped
            if(!mapVars.containsKey(sv.getName())) {
                Set<BindableIdentifier> deps = entry.getValue();
                skolemizeSchemaVar(sv, deps);
            }
        }
    }

    

    private void skolemizeSchemaVar(SchemaVariable sv,
            Set<BindableIdentifier> dependencies) throws TermException, EnvironmentException {
        Type[] argTypes = new Type[dependencies.size()];
        Term[] argTerms = new Term[dependencies.size()];
        
        Type instType = termInst.instantiate(sv.getType());
        
        //
        // make argument array
        int i = 0;
        for (BindableIdentifier dep : dependencies) {
            argTypes[i] = dep.getType();
            if (dep instanceof SchemaVariable) {
                SchemaVariable schema = (SchemaVariable)dep;
                argTerms[i] = mapVars.get(schema.getName());
            } else {
                argTerms[i] = termInst.instantiate(dep);
            }
            i++;
        }
        
        //
        // make new symbol, stripping leading %
        String name = sv.getName().substring(1);
        name = env.createNewFunctionName(name);
        Function f = new Function(name, instType, argTypes, false, false, ASTLocatedElement.CREATED);
        env.addFunction(f);
        
        //
        // build skolemisation (application)
        Term sk = new Application(f, instType, argTerms);
        assert TypeVariableCollector.collect(sk).isEmpty() : "Ensure no free type variables in skolemisation";
        
        mapVars.put(sv.getName(), sk);
    }

    private Term extractLocatedProblem() throws RuleException, TermException {
        
        LocatedTerm findClause = rule.getFindClause();
        
        // having no find is not assuming anything --> empty sequence --> false
        if(findClause == null)
            findClause = new LocatedTerm(FALSE, MatchingLocation.SUCCEDENT);
        
        boolean findInAntecedent = findClause.getMatchingLocation() == MatchingLocation.ANTECEDENT;
        Term findTerm = findClause.getTerm();
        
        Term result = TRUE;
        List<GoalAction> actions = rule.getGoalActions();
        for (GoalAction action : actions) {
            
            if(action.getKind() != Kind.COPY)
                throw new RuleException("ProblemExtraction works only for copy goals at the moment");
            
            Term add = FALSE;
            for (Term t : action.getAddAntecedent()) {
                add = disj(add, tf.not(t));
            }
            for (Term t : action.getAddSuccedent()) {
                add = disj(add, t);
            
            }
            Term replace = action.getReplaceWith();
            
            // copy original term if not remove
            if(replace == null && !action.isRemoveOriginalTerm())
                replace = findTerm;

            if(replace != null) {
                if(findInAntecedent) {
                    add = disj(add, tf.not(replace));
                } else {
                    add = disj(add, replace);
                }
            }
            
            result = conj(result, add);
        }
        
        Term findAndContext = context;
        if(findInAntecedent) {
            findAndContext = disj(findAndContext, tf.not(findTerm));
        } else {
            findAndContext = disj(findAndContext, findTerm);
        }
        
        result = tf.impl(result, findAndContext);
        return result;
    }

    private Term extractRewriteProblem() throws TermException, RuleException {
        
        assert context != null;
        
        Term find = rule.getFindClause().getTerm();
        Term result = TRUE;
        
        List<GoalAction> actions = rule.getGoalActions();
        for (GoalAction action : actions) {
            
            if(action.getKind() != Kind.COPY)
                throw new RuleException("ProblemExtraction works only for copy goals at the moment");
            
            Term add = FALSE;
            for (Term t : action.getAddAntecedent()) {
                add = disj(add, tf.not(t));
            }
            for (Term t : action.getAddSuccedent()) {
                add = disj(add, t);
            }
            
            if(add == null)
                add = Environment.getFalse();
            
            Term replace = action.getReplaceWith();
            if(replace == null)
                replace = find;
                
            Term eq = tf.eq(replace, find);
            Term imp = tf.impl(eq, add);
            result = conj(result, imp);
        }
        
        result = tf.impl(result, context);
        
        return result;
    }

    

    private void makeContext() throws TermException, RuleException {
        context = FALSE;
        
        List<LocatedTerm> assumptions = rule.getAssumptions();
        for (LocatedTerm assume : assumptions) {
            switch (assume.getMatchingLocation()) {
            case ANTECEDENT:
                context = disj(context, tf.not(assume.getTerm()));
                break;
            case SUCCEDENT:
                context = disj(context, assume.getTerm());
                break;
            default:
                throw new RuleException("Error in assumption statement");
            }
        }
    }
    
    
    private Term conj(@NonNull Term t1, @NonNull Term t2) throws TermException {
        if(t1 == TRUE)
            return t2;
        else if(t2 == TRUE)
            return t1;
        else
            return tf.and(t1, t2);
    }
    
    private Term disj(@NonNull Term t1, @NonNull Term t2) throws TermException {
        if(t1 == FALSE)
            return t2;
        else if(t2 == FALSE)
            return t1;
        else
            return tf.or(t1, t2);
    }

    private boolean isRewrite() {
        LocatedTerm find = rule.getFindClause();
        return find != null && find.getMatchingLocation() == MatchingLocation.BOTH;
    }

}
