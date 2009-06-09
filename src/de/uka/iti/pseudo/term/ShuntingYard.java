package de.uka.iti.pseudo.term;

import java.util.Stack;

import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.environment.FixOperator;
import de.uka.iti.pseudo.parser.ASTVisitException;
import de.uka.iti.pseudo.parser.file.ASTFixTerm;
import de.uka.iti.pseudo.parser.term.ASTListTerm;
import de.uka.iti.pseudo.parser.term.ASTOperatorIdentifierTerm;
import de.uka.iti.pseudo.parser.term.ASTTerm;
import de.uka.iti.pseudo.parser.term.Token;
import de.uka.iti.pseudo.util.Pair;

public class ShuntingYard {
    
    // TODO
    
    private static void flushStack(int threshold, 
            Stack<Pair<Token, FixOperator>> opStack,
            Stack<ASTTerm> termStack,
            Environment env) {
        
        while(!opStack.empty()) {
            
            FixOperator fixOp = opStack.peek().snd();
            
            if(fixOp.getPrecedence() < threshold)
                return;
            
            Token opToken = opStack.pop().fst();
            
            ASTTerm result;
            if(fixOp.isUnary()) {
                ASTTerm t1 = termStack.pop();
                result = new ASTFixTerm(opToken, fixOp, t1);
            } else {
                ASTTerm t2 = termStack.pop();
                ASTTerm t1 = termStack.pop();
                result = new ASTFixTerm(opToken, fixOp, t1, t2);
            }
            termStack.push(result);
        }
        
    }
    
    public static ASTTerm shuntingYard(Environment env, ASTListTerm listTerm) throws ASTVisitException {
        Stack<Pair<Token, FixOperator>> opStack =
            new Stack<Pair<Token,FixOperator>>();
        Stack<ASTTerm> termStack =
            new Stack<ASTTerm>();
        
        boolean expectOperator = false;
        
        for (ASTTerm element : listTerm.getSubterms()) {

            if(expectOperator) {
                if(element instanceof ASTOperatorIdentifierTerm) {
                    ASTOperatorIdentifierTerm op = (ASTOperatorIdentifierTerm)element;
                    Token opSymb = op.getSymbol();
                    FixOperator infixOp = env.getInfixOperator(opSymb.image);
                    
                    if(infixOp == null)
                        throw new ASTVisitException("Unknown infix operator " + opSymb, element);
                    
                    flushStack(infixOp.getPrecedence(), opStack, termStack, env);
                    
                    opStack.push(new Pair<Token, FixOperator>(opSymb, infixOp));
                    
                    expectOperator = false;
                } else {
                    throw new ASTVisitException("We expected an operator but received " + element, element);
                }
              
            } else {
                
                if(element instanceof ASTOperatorIdentifierTerm) {
                    ASTOperatorIdentifierTerm op = (ASTOperatorIdentifierTerm)element;
                    Token opSymb = op.getSymbol();
                    FixOperator prefixOp = env.getPrefixOperator(opSymb.image);
                    
                    if(prefixOp == null)
                        throw new ASTVisitException("Unknown prefix operator " + opSymb, element);
                    
                    opStack.push(new Pair<Token, FixOperator>(opSymb, prefixOp));
                } else {
                    termStack.push(element);
                    expectOperator = true;
                }
                
            }
        }

        if(!expectOperator)
            throw new ASTVisitException("Unbalanced fix expression", listTerm);

        // flushStack(Integer.MAX_VALUE, opStack, termStack, env);
        flushStack(0, opStack, termStack, env);

        assert opStack.empty();
        assert termStack.size() == 1;
        
        ASTTerm term = termStack.pop();
        term.setFilename(listTerm.getFileName());
        
        return term;
    }

}
