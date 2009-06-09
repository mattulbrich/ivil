package de.uka.iti.pseudo.environment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.uka.iti.pseudo.parser.ASTVisitException;
import de.uka.iti.pseudo.parser.file.ASTFileDefaultVisitor;
import de.uka.iti.pseudo.parser.file.ASTFileElement;
import de.uka.iti.pseudo.parser.file.ASTGoalAction;
import de.uka.iti.pseudo.parser.file.ASTLocatedTerm;
import de.uka.iti.pseudo.parser.file.ASTRawTerm;
import de.uka.iti.pseudo.parser.file.ASTRule;
import de.uka.iti.pseudo.parser.file.ASTRuleAdd;
import de.uka.iti.pseudo.parser.file.ASTRuleAssume;
import de.uka.iti.pseudo.parser.file.ASTRuleFind;
import de.uka.iti.pseudo.parser.file.ASTRuleRemove;
import de.uka.iti.pseudo.parser.file.ASTRuleReplace;
import de.uka.iti.pseudo.parser.file.ASTType;
import de.uka.iti.pseudo.parser.file.ASTTypeRef;
import de.uka.iti.pseudo.parser.file.ASTTypeVar;
import de.uka.iti.pseudo.parser.file.ASTWhereClause;
import de.uka.iti.pseudo.parser.file.MatchingLocation;
import de.uka.iti.pseudo.parser.file.Token;
import de.uka.iti.pseudo.parser.term.ASTTerm;
import de.uka.iti.pseudo.rule.GoalAction;
import de.uka.iti.pseudo.rule.LocatedTerm;
import de.uka.iti.pseudo.rule.Rule;
import de.uka.iti.pseudo.rule.RuleException;
import de.uka.iti.pseudo.rule.WhereClause;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.Type;
import de.uka.iti.pseudo.term.TypeVariable;
import de.uka.iti.pseudo.term.creation.TermMaker;
import de.uka.iti.pseudo.util.Pair;
import de.uka.iti.pseudo.util.SelectList;
import de.uka.iti.pseudo.util.Util;

// TODO Documentation needed
public class EnvironmentRuleDefinitionVisitor extends ASTFileDefaultVisitor {

    /**
     * Results of various types are transferred during traversal using the
     * following fields:
     */
    private Type resultingTypeRef;
    private Term resultingTerm;
    private MatchingLocation resultingMatchingLocation;
    private WhereClause resultingWhereclause;
    private GoalAction resultingGoalAction;
    private Environment env;
    
    public EnvironmentRuleDefinitionVisitor(Environment env) {
        this.env = env;
    }

    /*
     * default behaviour
     * 
     * visit children
     */
    protected void visitDefault(ASTFileElement arg) throws ASTVisitException {
        for (ASTFileElement child : arg.getChildren()) {
            child.visit(this);
        }
    }
    
    /*
     * collect all information for a rule definition and define it in env.
     * 
     * The with parts are parsed first as they are local definitions. 
     */
    public void visit(ASTRule arg) throws ASTVisitException {

        try {

            String name = arg.getName().image;
            List<ASTFileElement> children = arg.getChildren();
            
            List<LocatedTerm> assumes = new ArrayList<LocatedTerm>();
            {
                List<ASTRuleAssume> assumeASTs = SelectList.select(
                        ASTRuleAssume.class, children);
                for (ASTRuleAssume assume : assumeASTs) {
                    assume.visit(this);
                    assumes.add(new LocatedTerm(resultingTerm,
                            resultingMatchingLocation));
                }
            }

            LocatedTerm find;
            {
                List<ASTRuleFind> findASTs = SelectList.select(
                        ASTRuleFind.class, children);
                if (findASTs.size() != 1) {
                    throw new ASTVisitException(
                            "There is not exactly one find clause.", arg);
                }
                ASTRuleFind astFind = findASTs.get(0);
                astFind.visit(this);
                find = new LocatedTerm(resultingTerm, resultingMatchingLocation);
            }

            List<WhereClause> wheres = new ArrayList<WhereClause>();
            {
                List<ASTWhereClause> whereASTs = SelectList.select(
                        ASTWhereClause.class, children);
                for (ASTWhereClause where : whereASTs) {
                    where.visit(this);
                    wheres.add(resultingWhereclause);
                }
            }

            List<GoalAction> actions = new ArrayList<GoalAction>();
            {
                List<ASTGoalAction> actionASTs = SelectList.select(
                        ASTGoalAction.class, children);
                for (ASTGoalAction action : actionASTs) {
                    action.visit(this);
                    actions.add(resultingGoalAction);
                }
            }
            
            Map<String, String> properties = new HashMap<String, String>();
            {
                for (Pair<Token, Token> prop : arg.getProperties()) {
                    Token token = prop.snd();
                    String value;
                    if(token != null)
                        value = token.image;
                    else
                        value = "";
                    
                    properties.put(prop.fst().image, Util.stripQuotes(value));
                }
                Token description = arg.getDescription();
                if(description != null)
                    properties.put("description", Util.stripQuotes(description.image));
                
            }

            try {
                Rule rule = new Rule(name, assumes, find, wheres, actions, properties, arg);
                env.addRule(rule);
            } catch (EnvironmentException e) {
                throw new ASTVisitException(e);
            }
            
        } catch (RuleException e) {
            throw new ASTVisitException(arg, e);
        }
    }
    
    /*
     * make a where clause
     * report if where condition is unknown. 
     */
    public void visit(ASTWhereClause arg) throws ASTVisitException {

        String identifier = arg.getIdentifier().image;

        WhereCondition where = WhereCondition.getWhereCondition(identifier);
        if (where == null)
            throw new ASTVisitException("Unknown where condition: "
                    + identifier, arg);

        List<ASTRawTerm> raws = SelectList.select(ASTRawTerm.class, arg
                .getChildren());
        Term[] terms = new Term[raws.size()];
        for (int i = 0; i < terms.length; i++) {
            raws.get(i).visit(this);
            terms[i] = resultingTerm;
        }

        try {
            resultingWhereclause = new WhereClause(where, terms);
        } catch (RuleException e) {
            throw new ASTVisitException(arg, e);
        }

    }

    /*
     * collect children an make a new goal action
     */
    public void visit(ASTGoalAction arg) throws ASTVisitException {
        super.visit(arg);

        Token kindToken = arg.getGoalKindToken();
        String kind;
        if(kindToken != null) {
            kind = kindToken.image;
        } else {
            // if no action target is specified, samegoal is the default
            kind = "samegoal";
        }
        Token nameToken = arg.getName();
        String name = nameToken == null ? null : Util.stripQuotes(nameToken.image);
        
        List<Term> addAntecendent = new ArrayList<Term>();
        List<Term> addSuccendent = new ArrayList<Term>();
        Term replaceWith = null;

        for(ASTRuleAdd add : SelectList.select(ASTRuleAdd.class, arg.getChildren())) {
            add.visit(this);
            if(resultingMatchingLocation == MatchingLocation.ANTECEDENT)
                addAntecendent.add(resultingTerm);
            else
                addSuccendent.add(resultingTerm);
        }
        
        for(ASTRuleReplace replace : SelectList.select(ASTRuleReplace.class, arg.getChildren())) {
            assert replaceWith == null;
            replace.visit(this);
            replaceWith = resultingTerm;
        }
        
        boolean hasRemove = !SelectList.select(ASTRuleRemove.class, 
                arg.getChildren()).isEmpty();

        
        try {
            resultingGoalAction = new GoalAction(kind, name, hasRemove, replaceWith, addAntecendent, addSuccendent);
        } catch (RuleException e) {
            throw new ASTVisitException(arg, e);
        }
    }

    /*
     * Type application
     */
    public void visit(ASTTypeRef arg) throws ASTVisitException {

        String name = arg.getTypeToken().image;

        List<ASTType> argumentTypes = arg.getArgTypes();
        Type domTy[] = new Type[argumentTypes.size()];

        for (int i = 0; i < domTy.length; i++) {
            argumentTypes.get(i).visit(this);
            domTy[i] = resultingTypeRef;
        }

        try {
            resultingTypeRef = env.mkType(name, domTy);
        } catch (Exception e) {
            throw new ASTVisitException(arg, e);
        }
    }

    /*
     * type variable
     */
    public void visit(ASTTypeVar arg) throws ASTVisitException {
        resultingTypeRef = new TypeVariable(arg.getTypeVarToken().image
                .substring(1));
    }

    /*
     * transform a ASTTerm to a Term.
     */
    public void visit(ASTRawTerm arg) throws ASTVisitException {
        ASTTerm ast = arg.getTermAST();
        assert ast != null;
        resultingTerm = TermMaker.makeTerm(ast, env);
    }

    /*
     * store the location for a located term.
     */
    public void visit(ASTLocatedTerm arg) throws ASTVisitException {
        super.visit(arg);
        resultingMatchingLocation = arg.getMatchingLocation();
    }

}
