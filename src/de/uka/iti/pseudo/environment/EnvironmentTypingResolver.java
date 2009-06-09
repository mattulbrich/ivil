package de.uka.iti.pseudo.environment;

import de.uka.iti.pseudo.parser.ASTDefaultVisitor;
import de.uka.iti.pseudo.parser.ASTElement;
import de.uka.iti.pseudo.parser.ASTVisitException;
import de.uka.iti.pseudo.parser.file.ASTLocatedTerm;
import de.uka.iti.pseudo.parser.file.ASTRule;
import de.uka.iti.pseudo.parser.file.ASTRuleFind;
import de.uka.iti.pseudo.parser.file.ASTRuleReplace;
import de.uka.iti.pseudo.parser.file.MatchingLocation;
import de.uka.iti.pseudo.parser.term.ASTTerm;
import de.uka.iti.pseudo.term.Type;
import de.uka.iti.pseudo.term.UnificationException;
import de.uka.iti.pseudo.term.creation.TypingContext;

/**
 * Resolve types in terms that appear in rules.
 * 
 * Within a rule definition, several terms can appear. They cannot be typed separately since
 * schema variables may hinder this.
 * 
 */
public class EnvironmentTypingResolver extends ASTDefaultVisitor {

    private Environment env;
    private TypingContext typingContext = new TypingContext();
    private Type currentFindRawType;

    public EnvironmentTypingResolver(Environment env) {
        this.env = env;
    }

    protected void visitDefault(ASTElement arg)
            throws ASTVisitException {
        for (ASTElement child : arg.getChildren()) {
            child.visit(this);
        }
    }

    public void visit(ASTRule arg) throws ASTVisitException {
        super.visit(arg);
        
        // XXX make find and replace have same type
        
        // reset context for next rule / problem
        typingContext = new TypingContext(); 
        currentFindRawType = null;
    }
    
    public void visit(ASTRuleFind arg) throws ASTVisitException {
        super.visit(arg);
        currentFindRawType = arg.getLocatedTerm().getTerm().getTyping().getRawType();
    }
    
    public void visit(ASTRuleReplace arg) throws ASTVisitException {
        super.visit(arg);
        
        // there must be a find clause if there is a replace clause
        assert currentFindRawType != null;
        Type rawType = arg.getTerm().getTyping().getRawType();
        try {
            typingContext.solveConstraint(currentFindRawType, rawType);
        } catch (UnificationException e) {
            throw new ASTVisitException("Replace terms must have same type as find term", arg, e);
        }
    }
    
    /*
     * ensure that located term which are not "both" are of boolean type
     */
    public void visit(ASTLocatedTerm arg) throws ASTVisitException {
        ASTTerm term = arg.getTerm();
        term.visit(this);
        if(arg.getMatchingLocation() != MatchingLocation.BOTH) {
            try {
                typingContext.solveConstraint(Environment.getBoolType(), 
                        term.getTyping().getRawType());
            } catch (UnificationException e) {
                throw new ASTVisitException("Located term must have boolean type", arg, e);
            }
        }
    }
    
}
