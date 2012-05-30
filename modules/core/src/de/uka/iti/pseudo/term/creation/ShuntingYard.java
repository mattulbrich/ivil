/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2012 Karlsruhe Institute of Technology
 *
 * The system is protected by the GNU General Public License.
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.term.creation;

import java.util.Stack;

import nonnull.NonNull;
import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.environment.FixOperator;
import de.uka.iti.pseudo.parser.ASTVisitException;
import de.uka.iti.pseudo.parser.Token;
import de.uka.iti.pseudo.parser.term.ASTFixTerm;
import de.uka.iti.pseudo.parser.term.ASTListTerm;
import de.uka.iti.pseudo.parser.term.ASTOperatorIdentifierTerm;
import de.uka.iti.pseudo.parser.term.ASTTerm;
import de.uka.iti.pseudo.util.Pair;

/**
 * The Class ShuntingYard encapsulates a static method to perform the shunting
 * yard algorithm. It is described in
 * {@linkplain http://en.wikipedia.org/wiki/Shunting_yard_algorithm}. This
 * version here supports also prefix operators.
 * 
 * There are two stacks: One for operators, one for data.
 * 
 * When a new operator has been found, the stack is reduced by those operators
 * that have higher precedence. They are contracted to new Objects.
 * 
 * The algorithm operates on a {@link ASTListTerm} and creates a nested
 * {@link ASTFixTerm} as a result. The filename attribute is set in the result.
 * 
 * @author mattias ulbrich
 * 
 */
public class ShuntingYard {
    
    /**
	 * Reduce the operator stack by those operations that have higher (or equal)
	 * priority than a threshold.
	 * 
	 * <pre>
	 * while precedence(op_stack.peek() &gt;= threshold)
	 *    op_stack.pop()
	 *    if op is unary
	 *       pop value vom term stack
	 *       wrap it in a ASTFixTerm with op
	 *       push it on the term stack again
	 *    else (binary)
	 *       pop two values from term stack
	 *       wrap them into a ASTFixTerm with op
	 *       push it on the term stack again
	 *    end
	 * end
	 * </pre>
	 * 
	 * @param threshold
	 *            the threshold for the flushing
	 * @param opStack
	 *            the operator stack
	 * @param termStack
	 *            the term stack
	 * @param env
	 *            the environment to extract information about fix operators.
	 */
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
    
    /**
	 * The shunting yard algorithm to convert an infix term construction into a AST.
	 * 
	 * <pre>
	 * expect := operand
	 * foreach symbol
	 *   if expect operator
	 *     get operator op
	 *     flushStack(op.precedence)
	 *     push operator
	 *     expect := operand
	 *   end
	 *   if expect operand
	 *     if prefix operator
	 *       push operator
	 *     else
	 *       push value
	 *   end
	 * end
	 * 
	 * flushStack(0);
	 * return termStack.peek();
	 * </pre>
	 * 
	 * @param env
	 *            the environment to retrieve fix information, non-null 
	 * @param listTerm
	 *            the term that is to be converted, non-null
	 * 
	 * @return the translated term, non-null
	 * 
	 * @throws ASTVisitException
	 *             in case of a not-found operator or a syntactical error.
	 */
    @NonNull public static ASTTerm shuntingYard(@NonNull Environment env, @NonNull ASTListTerm listTerm) throws ASTVisitException {
    	
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
        
        @NonNull ASTTerm term = termStack.pop();
	assert term != null : "nullness";

        term.setFilename(listTerm.getFileName());
        
        return term;
    }

}
