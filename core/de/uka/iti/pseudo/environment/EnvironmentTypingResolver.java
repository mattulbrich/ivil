package de.uka.iti.pseudo.environment;

import de.uka.iti.pseudo.parser.ASTDefaultVisitor;
import de.uka.iti.pseudo.parser.ASTElement;
import de.uka.iti.pseudo.parser.ASTVisitException;
import de.uka.iti.pseudo.parser.file.ASTFile;
import de.uka.iti.pseudo.parser.file.ASTLocatedTerm;
import de.uka.iti.pseudo.parser.file.ASTProgramDeclaration;
import de.uka.iti.pseudo.parser.file.ASTRule;
import de.uka.iti.pseudo.parser.file.ASTRuleFind;
import de.uka.iti.pseudo.parser.file.ASTRuleReplace;
import de.uka.iti.pseudo.parser.file.MatchingLocation;
import de.uka.iti.pseudo.parser.program.ASTStatement;
import de.uka.iti.pseudo.parser.term.ASTTerm;
import de.uka.iti.pseudo.term.Type;
import de.uka.iti.pseudo.term.UnificationException;
import de.uka.iti.pseudo.term.creation.TypingContext;
import de.uka.iti.pseudo.term.creation.TypingResolver;

/**
 * Resolve types in terms that appear in rules.
 * 
 * Within a rule definition, several terms can appear. They cannot be typed separately since
 * schema variables may hinder this.
 * 
 */
public class EnvironmentTypingResolver extends ASTDefaultVisitor {

    private Environment env;
    private TypingResolver typingResolver;
    private Type currentFindRawType;

    public EnvironmentTypingResolver(Environment env) {
        this.env = env;
        typingResolver = new TypingResolver(env, new TypingContext());
    }

    /*
     * this is depth visiting
     */
    protected void visitDefault(ASTElement arg)
            throws ASTVisitException {
        for (ASTElement child : arg.getChildren()) {
            child.visit(this);
        }
    }
    
    protected void visitDefaultTerm(ASTTerm arg) throws ASTVisitException {
        arg.visit(typingResolver);
    }
    
    public void visit(ASTFile arg) throws ASTVisitException {
        super.visit(arg);
        
        // if there is a problem in the file, the current typing context is the one
        // of the problem term (because problems are last in a file)
        ASTTerm problemTerm = arg.getProblemTerm();
        if(problemTerm != null) {
            try {
                TypingContext typingContext = typingResolver.getTypingContext();
                typingContext.solveConstraint(problemTerm.getTyping().getRawType(), Environment.getBoolType());
            } catch (UnificationException e) {
                throw new ASTVisitException("Problem terms must habe type boolean.", arg, e);
            }
        }
    }
    
    public void visit(ASTProgramDeclaration arg)
            throws ASTVisitException {
        // program statements are typed by TermMaker
    }

    public void visit(ASTRule arg) throws ASTVisitException {
        super.visit(arg);
        
        // reset context for next rule / program / problem
        typingResolver = new TypingResolver(env, new TypingContext()); 
        currentFindRawType = null;
    }
    
//    public void visit(ASTProgramDeclaration arg) throws ASTVisitException {
//        // do not visit all children, the identifier of the declaration might be troublesome
//        for (ASTElement child : arg.getChildren()) {
//            child.visit(this);
//        }
//    }
    
    public void visit(ASTRuleFind arg) throws ASTVisitException {
        super.visit(arg);
        currentFindRawType = arg.getLocatedTerm().getTerm().getTyping().getRawType();
    }
    
    public void visit(ASTRuleReplace arg) throws ASTVisitException {
        super.visit(arg);
        
        // there must be a find clause if there is a replace clause
        if(currentFindRawType == null)
            throw new ASTVisitException("There must be a find clause if there is a replace clause", arg);
        
        Type rawType = arg.getTerm().getTyping().getRawType();
        try {
            TypingContext typingContext = typingResolver.getTypingContext();
            typingContext.solveConstraint(currentFindRawType, rawType);
        } catch (UnificationException e) {
            throw new ASTVisitException("Replace terms must have same type as find term", arg, e);
        }
    }
    
    /*
     * ensure that located term which are not "both" are of boolean type
     * arg.getTerm() may change since the typingResolver may (due to 
     * shunting yard) replace the term
     */
    public void visit(ASTLocatedTerm arg) throws ASTVisitException {
        arg.getTerm().visit(this);
        
        if(arg.getMatchingLocation() != MatchingLocation.BOTH) {
            try {
                TypingContext typingContext = typingResolver.getTypingContext();
                typingContext.solveConstraint(Environment.getBoolType(), 
                        arg.getTerm().getTyping().getRawType());
            } catch (UnificationException e) {
                throw new ASTVisitException("Located term must have boolean type", arg, e);
            }
        }
    }
    
}
