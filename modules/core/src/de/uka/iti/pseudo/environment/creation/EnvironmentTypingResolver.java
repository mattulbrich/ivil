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
package de.uka.iti.pseudo.environment.creation;

import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.parser.ASTDefaultVisitor;
import de.uka.iti.pseudo.parser.ASTElement;
import de.uka.iti.pseudo.parser.ASTVisitException;
import de.uka.iti.pseudo.parser.file.ASTAxiomDeclaration;
import de.uka.iti.pseudo.parser.file.ASTFile;
import de.uka.iti.pseudo.parser.file.ASTLocatedTerm;
import de.uka.iti.pseudo.parser.file.ASTProgramDeclaration;
import de.uka.iti.pseudo.parser.file.ASTProperties;
import de.uka.iti.pseudo.parser.file.ASTPropertiesDeclaration;
import de.uka.iti.pseudo.parser.file.ASTRule;
import de.uka.iti.pseudo.parser.file.ASTRuleFind;
import de.uka.iti.pseudo.parser.file.ASTRuleReplace;
import de.uka.iti.pseudo.parser.file.ASTProblemSequent;
import de.uka.iti.pseudo.parser.file.MatchingLocation;
import de.uka.iti.pseudo.parser.term.ASTTerm;
import de.uka.iti.pseudo.term.Type;
import de.uka.iti.pseudo.term.UnificationException;
import de.uka.iti.pseudo.term.creation.TypingContext;
import de.uka.iti.pseudo.term.creation.TypingResolver;
import de.uka.iti.pseudo.util.SelectList;

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
        typingResolver = new TypingResolver(env);
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
    
//    public void visit(ASTFile arg) throws ASTVisitException {
//        super.visit(arg);
//    }
    
    public void visit(ASTProblemSequent arg) throws ASTVisitException {
        // if there is a problem in the file, the current typing context is the one
        // of the problem term (because problems are last in a file)

        super.visit(arg);
        
        for (ASTTerm child : SelectList.select(ASTTerm.class, arg.getChildren())) {
            try {
                TypingContext typingContext = typingResolver.getTypingContext();
                typingContext.solveConstraint(child.getTyping().getRawType(), Environment.getBoolType());
            } catch (UnificationException e) {
                throw new ASTVisitException("Terms in the problem sequent must have type boolean.", arg, e);
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
        typingResolver = new TypingResolver(env); 
        currentFindRawType = null;
    }
    
    public void visit(ASTAxiomDeclaration arg) throws ASTVisitException {
        super.visit(arg);
        
        try {
            TypingContext typingContext = typingResolver.getTypingContext();
            typingContext.solveConstraint(arg.getTerm().getTyping().getRawType(), Environment.getBoolType());
        } catch (UnificationException e) {
            throw new ASTVisitException("Axioms must have type boolean.", arg, e);
        }
        
        // reset context for next rule / program / problem
        typingResolver = new TypingResolver(env); 
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

    @Override
    public void visit(ASTProperties plugins) throws ASTVisitException {
    }

    @Override
    public void visit(ASTPropertiesDeclaration plugin) throws ASTVisitException {
    }
    
}
