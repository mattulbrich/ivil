package de.uka.iti.pseudo.environment;

import de.uka.iti.pseudo.parser.ASTVisitException;
import de.uka.iti.pseudo.parser.file.ASTFileDefaultVisitor;
import de.uka.iti.pseudo.parser.file.ASTFileElement;
import de.uka.iti.pseudo.parser.file.ASTLocatedTerm;
import de.uka.iti.pseudo.parser.file.ASTRawTerm;
import de.uka.iti.pseudo.parser.file.ASTRule;
import de.uka.iti.pseudo.parser.file.ASTRuleFind;
import de.uka.iti.pseudo.parser.file.ASTRuleReplace;
import de.uka.iti.pseudo.parser.file.MatchingLocation;
import de.uka.iti.pseudo.parser.file.Token;
import de.uka.iti.pseudo.parser.term.ASTHeadElement;
import de.uka.iti.pseudo.parser.term.ASTTerm;
import de.uka.iti.pseudo.parser.term.ParseException;
import de.uka.iti.pseudo.parser.term.TermParser;
import de.uka.iti.pseudo.term.Type;
import de.uka.iti.pseudo.term.UnificationException;
import de.uka.iti.pseudo.term.creation.TypingContext;
import de.uka.iti.pseudo.term.creation.TypingResolver;
import de.uka.iti.pseudo.util.Util;

// TODO Documentation needed
public class EnvironmentTypingResolver extends ASTFileDefaultVisitor {

    private Environment env;
    private TypingContext typingContext = new TypingContext();
    private Type currentFindRawType;

    public EnvironmentTypingResolver(Environment env) {
        this.env = env;
    }

    protected void visitDefault(ASTFileElement arg)
            throws ASTVisitException {
        for (ASTFileElement child : arg.getChildren()) {
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
        currentFindRawType = arg.getLocatedTerm().getTerm().getTermAST().
                                getTyping().getRawType();
    }
    
    public void visit(ASTRuleReplace arg) throws ASTVisitException {
        super.visit(arg);
        
        // there must be a find clause if there is a replace clause
        assert currentFindRawType != null;
        Type rawType = arg.getRawTerm().getTermAST().getTyping().getRawType();
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
        ASTRawTerm term = arg.getTerm();
        term.visit(this);
        if(arg.getMatchingLocation() != MatchingLocation.BOTH) {
            try {
                typingContext.solveConstraint(Environment.getBoolType(), 
                        term.getTermAST().getTyping().getRawType());
            } catch (UnificationException e) {
                throw new ASTVisitException("Located term must have boolean type", arg, e);
            }
        }
    }
    
    public void visit(ASTRawTerm arg) throws ASTVisitException {
        
        Token termToken = arg.getTermToken();
        String content = Util.stripQuotes(termToken.image);
        
        TermParser parser = new TermParser(content, arg.getFileName(),
                termToken.beginLine, termToken.beginColumn);
        ASTTerm ast;
        try {
            ast = parser.parseTerm();
        } catch (ParseException e) {
            throw new ASTVisitException("Cannot parse the term " + content , arg, e);
        }
        
        ASTHeadElement head = new ASTHeadElement(ast);
        TypingResolver typingResolver = new TypingResolver(env, typingContext);
        ast.visit(typingResolver);
        ast = (ASTTerm) head.getWrappedElement();
        
        arg.setTermAST(ast);
    }
    
}
