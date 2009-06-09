package de.uka.iti.pseudo.environment;

import de.uka.iti.pseudo.parser.ASTVisitException;
import de.uka.iti.pseudo.parser.file.ASTFileDefaultVisitor;
import de.uka.iti.pseudo.parser.file.ASTFileElement;
import de.uka.iti.pseudo.parser.file.ASTLocatedTerm;
import de.uka.iti.pseudo.parser.file.ASTRawTerm;
import de.uka.iti.pseudo.parser.file.ASTRule;
import de.uka.iti.pseudo.parser.file.MatchingLocation;
import de.uka.iti.pseudo.parser.file.Token;
import de.uka.iti.pseudo.parser.term.ASTHeadElement;
import de.uka.iti.pseudo.parser.term.ASTTerm;
import de.uka.iti.pseudo.parser.term.ParseException;
import de.uka.iti.pseudo.parser.term.TermParser;
import de.uka.iti.pseudo.term.UnificationException;
import de.uka.iti.pseudo.term.creation.TypingContext;
import de.uka.iti.pseudo.term.creation.TypingResolver;
import de.uka.iti.pseudo.util.Util;

// TODO Documentation needed
public class EnvironmentTypingResolver extends ASTFileDefaultVisitor {

    private Environment env;
    private TypingContext typingContext = new TypingContext();

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
        
        typingContext = new TypingContext(); 
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
                        term.getTermAST().getTyping().getRawtType());
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
