/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2010 Universitaet Karlsruhe, Germany
 *    written by Mattias Ulbrich
 * 
 * The system is protected by the GNU General Public License. 
 * See LICENSE.TXT (distributed with this file) for details.
 */
package test;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import de.uka.iti.pseudo.environment.FixOperator;
import de.uka.iti.pseudo.environment.Function;
import de.uka.iti.pseudo.prettyprint.PrettyPrintPlugin;
import de.uka.iti.pseudo.term.Application;
import de.uka.iti.pseudo.term.Binding;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.TermException;

public class UnicodePrettyPrinter extends PrettyPrintPlugin {
    
    Properties properties = new Properties();
    
    public UnicodePrettyPrinter() {
        InputStream stream = getClass().getResourceAsStream("UnicodePrettyPrinter.properties");
        if(stream == null) {
            System.err.println("Resource UnicodePrettyPrinter.properties is missing --> no pretty printing");
        } else {
            try {
                properties.load(stream);
            } catch (IOException e) {
                System.err.println("Error reading UnicodePrettyPrinter.properties is missing --> no pretty printing");
                e.printStackTrace();
                properties.clear();
            }
        }
    }
    
    /**
     * @see PrettyPrintVisitor#getPrecedence() 
     */
    private int getPrec(Term t) {
        if (t instanceof Application) {
            Application app = (Application) t;
            Function f = app.getFunction();
            String fctName = f.getName();
            FixOperator fixOperator = getEnvironment().getReverseFixOperator(fctName);
            if(fixOperator != null)
                return fixOperator.getPrecedence();
        }
        
        return Integer.MAX_VALUE;
    }
    
    /*
     * Visit a subterm and put it in parens possibly.
     * 
     * Parens are included if the subterm's precedence is less than
     * the precedence of the surrounding term
     * 
     * If typing is switched on, parentheses are included if the 
     * term has a non-maximal precedence, i.e. if it is a prefixed 
     * or infixed expression
     */
    private void visitMaybeParen(Term term, int subtermNo, int precedence)
            throws TermException {

        Term subterm = term.getSubterm(subtermNo);
        
        int innerPrecedence = getPrec(subterm);
        if ((isTyped() && innerPrecedence < Integer.MAX_VALUE)
                || (!isTyped() && innerPrecedence < precedence)) {
            append("(");
            printSubterm(term, subtermNo);
            append(")");
        } else {
            printSubterm(term, subtermNo);
        }

    }

    @Override public void prettyPrintTerm(Application term)
        throws TermException {
        
        // works only on fix terms if they are enabled
        if(!isPrintingFix())
            return;
        
        Function f = term.getFunction();
        String fctName = f.getName();
        FixOperator fixOperator = getEnvironment().getReverseFixOperator(fctName);
        
        // not a fix operator
        if(f.getArity() > 0 && fixOperator == null)
            return;
        
        String replacement = properties.getProperty(fctName);
        
        // no entry in properties
        if(replacement == null)
            return;
        
        switch(f.getArity()) {
        case 2:
            int myPrec = fixOperator.getPrecedence();
            visitMaybeParen(term, 0, myPrec);
            append(replacement);
            visitMaybeParen(term, 1, myPrec+1);
            break;
        case 1:
            myPrec = fixOperator.getPrecedence();
            append(replacement);
            visitMaybeParen(term, 0, myPrec);
            break;
        case 0:
            append(replacement);
            break;
        default:
            throw new Error("unreachable");
        }

    }

    @Override public void prettyPrintTerm(Binding term) throws TermException {
        String key = term.getBinder().getName();
        String replacement = properties.getProperty(key);
        
        if(replacement != null) {
            append("(");
            append(replacement);
            printBoundVariable(term);
            int i = 0;
            for (@SuppressWarnings("unused")
            Term t : term.getSubterms()) {
                append("; ");
                printSubterm(term, i);
                i++;
            }
            append(")");
        }
    }

    @Override
    public String getReplacementName(String name) {
		// no replacement for function names
        return null;
    }

}
