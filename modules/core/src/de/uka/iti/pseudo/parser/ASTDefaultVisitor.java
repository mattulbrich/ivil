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
package de.uka.iti.pseudo.parser;

import de.uka.iti.pseudo.parser.file.ASTAxiomDeclaration;
import de.uka.iti.pseudo.parser.file.ASTBinderDeclaration;
import de.uka.iti.pseudo.parser.file.ASTBinderDeclarationBlock;
import de.uka.iti.pseudo.parser.file.ASTFile;
import de.uka.iti.pseudo.parser.file.ASTFunctionDeclaration;
import de.uka.iti.pseudo.parser.file.ASTFunctionDeclarationBlock;
import de.uka.iti.pseudo.parser.file.ASTGoalAction;
import de.uka.iti.pseudo.parser.file.ASTIncludeDeclarationBlock;
import de.uka.iti.pseudo.parser.file.ASTLocatedTerm;
import de.uka.iti.pseudo.parser.file.ASTPluginDeclaration;
import de.uka.iti.pseudo.parser.file.ASTPlugins;
import de.uka.iti.pseudo.parser.file.ASTProgramDeclaration;
import de.uka.iti.pseudo.parser.file.ASTProperties;
import de.uka.iti.pseudo.parser.file.ASTPropertiesDeclaration;
import de.uka.iti.pseudo.parser.file.ASTRule;
import de.uka.iti.pseudo.parser.file.ASTRuleAdd;
import de.uka.iti.pseudo.parser.file.ASTRuleAssume;
import de.uka.iti.pseudo.parser.file.ASTRuleFind;
import de.uka.iti.pseudo.parser.file.ASTRuleRemove;
import de.uka.iti.pseudo.parser.file.ASTRuleReplace;
import de.uka.iti.pseudo.parser.file.ASTSortDeclaration;
import de.uka.iti.pseudo.parser.file.ASTSortDeclarationBlock;
import de.uka.iti.pseudo.parser.file.ASTWhereClause;
import de.uka.iti.pseudo.parser.program.ASTAssertStatement;
import de.uka.iti.pseudo.parser.program.ASTAssignment;
import de.uka.iti.pseudo.parser.program.ASTAssignmentStatement;
import de.uka.iti.pseudo.parser.program.ASTAssumeStatement;
import de.uka.iti.pseudo.parser.program.ASTEndStatement;
import de.uka.iti.pseudo.parser.program.ASTGotoStatement;
import de.uka.iti.pseudo.parser.program.ASTHavocStatement;
import de.uka.iti.pseudo.parser.program.ASTLabelStatement;
import de.uka.iti.pseudo.parser.program.ASTSchematicAssignmentStatement;
import de.uka.iti.pseudo.parser.program.ASTSkipStatement;
import de.uka.iti.pseudo.parser.program.ASTSourceLineStatement;
import de.uka.iti.pseudo.parser.program.ASTStatement;
import de.uka.iti.pseudo.parser.term.ASTApplicationTerm;
import de.uka.iti.pseudo.parser.term.ASTAsType;
import de.uka.iti.pseudo.parser.term.ASTBinderTerm;
import de.uka.iti.pseudo.parser.term.ASTFixTerm;
import de.uka.iti.pseudo.parser.term.ASTIdentifierTerm;
import de.uka.iti.pseudo.parser.term.ASTListTerm;
import de.uka.iti.pseudo.parser.term.ASTNumberLiteralTerm;
import de.uka.iti.pseudo.parser.term.ASTOperatorIdentifierTerm;
import de.uka.iti.pseudo.parser.term.ASTProgramTerm;
import de.uka.iti.pseudo.parser.term.ASTSchemaType;
import de.uka.iti.pseudo.parser.term.ASTSchemaUpdateTerm;
import de.uka.iti.pseudo.parser.term.ASTSchemaVariableTerm;
import de.uka.iti.pseudo.parser.term.ASTTerm;
import de.uka.iti.pseudo.parser.term.ASTType;
import de.uka.iti.pseudo.parser.term.ASTTypeApplication;
import de.uka.iti.pseudo.parser.term.ASTTypeVar;
import de.uka.iti.pseudo.parser.term.ASTTypevarBinderTerm;
import de.uka.iti.pseudo.parser.term.ASTUpdateTerm;

/**
 * Default implementation of the {@link ASTVisitor} interface which delegates
 * all calls to an abstract method {@link #visitDefault(ASTElement)}.
 * 
 * The visit methods for all subtypes of ASTTerm, however, delegate their call
 * to {@link #visitDefaultTerm(ASTTerm)}. It calls by default
 * {@link #visitDefault(ASTElement)} but may be overridden in extending
 * subclasses.
 * 
 * Same applies to types ({@link #visitDefaultType(ASTType)})
 * and statements ({@link #visitDefaultStatement(ASTStatement)}.
 */
public abstract class ASTDefaultVisitor implements ASTVisitor {

    /**
     * Extending classes need to implement a default behaviour.
     * 
     * <p>
     * This method will be called unless a visit method is overridden.
     * 
     * @param arg
     *            the file element to apply to
     * 
     * @throws ASTVisitException
     *             may be thrown by any implementation
     */
    protected abstract void visitDefault(ASTElement arg)
            throws ASTVisitException;
    
    protected void visitDefaultTerm(ASTTerm arg) throws ASTVisitException {
        visitDefault(arg);
    }
    
    protected void visitDefaultType(ASTType arg) throws ASTVisitException {
        visitDefault(arg);
    }
    
    protected void visitDefaultStatement(ASTStatement arg) throws ASTVisitException {
        visitDefault(arg);
    }

    //
    // visited terms call visitDefaultTerm
    //
    public void visit(ASTApplicationTerm arg) throws ASTVisitException {
        visitDefaultTerm(arg);
    }

    public void visit(ASTBinderTerm arg) throws ASTVisitException {
        visitDefaultTerm(arg);
    }
    
    public void visit(ASTTypevarBinderTerm arg) throws ASTVisitException {
        visitDefaultTerm(arg);
    }

    public void visit(ASTIdentifierTerm arg) throws ASTVisitException {
        visitDefaultTerm(arg);
    }
    
    public void visit(ASTExplicitVariableTerm arg) throws ASTVisitException {
        visitDefaultTerm(arg);
    }

    public void visit(ASTListTerm arg) throws ASTVisitException {
        visitDefaultTerm(arg);
    }

    public void visit(ASTNumberLiteralTerm arg) throws ASTVisitException {
        visitDefaultTerm(arg);
    }

    public void visit(ASTOperatorIdentifierTerm arg) throws ASTVisitException {
        visitDefaultTerm(arg);
    }

    public void visit(ASTFixTerm arg) throws ASTVisitException {
        visitDefaultTerm(arg);
    }

    public void visit(ASTAsType arg) throws ASTVisitException {
        visitDefaultTerm(arg);
    }

    public void visit(ASTSchemaVariableTerm arg) throws ASTVisitException {
        visitDefaultTerm(arg);
    }

    public void visit(ASTProgramTerm arg) throws ASTVisitException {
        visitDefaultTerm(arg);
    }
    
    public void visit(ASTUpdateTerm arg) throws ASTVisitException {
        visitDefaultTerm(arg);
    }
    
    public void visit(ASTSchemaUpdateTerm schemaUpdateTerm)
            throws ASTVisitException {
        visitDefaultTerm(schemaUpdateTerm);
    }

    //
    // types call visitDefaultType
    //

    public void visit(ASTTypeApplication arg) throws ASTVisitException {
        visitDefaultType(arg);
    }

    public void visit(ASTTypeVar arg) throws ASTVisitException {
        visitDefaultType(arg);
    }
    
    @Override
    public void visit(ASTSchemaType arg) throws ASTVisitException {
        visitDefaultType(arg);
    }
    
    public void visit(ASTAssignment arg)  throws ASTVisitException {
        visitDefault(arg);
    }


    //
    // statements call visitDefaultStatement
    //
    
    public void visit(ASTAssignmentStatement arg) throws ASTVisitException {
        visitDefaultStatement(arg);
    }
    
    public void visit(ASTSchematicAssignmentStatement arg) throws ASTVisitException {
        visitDefaultStatement(arg);
    }
    
    public void visit(ASTAssertStatement arg)  throws ASTVisitException {
        visitDefaultStatement(arg);
    }

    public void visit(ASTAssumeStatement arg)  throws ASTVisitException {
        visitDefaultStatement(arg);
    }

    public void visit(ASTEndStatement arg)  throws ASTVisitException {
        visitDefaultStatement(arg);
    }

    public void visit(ASTGotoStatement arg)  throws ASTVisitException {
        visitDefaultStatement(arg);
    }

    public void visit(ASTHavocStatement arg)  throws ASTVisitException {
        visitDefaultStatement(arg);
    }

    public void visit(ASTLabelStatement arg)  throws ASTVisitException {
        visitDefaultStatement(arg);
    }

    public void visit(ASTSkipStatement arg)  throws ASTVisitException {
        visitDefaultStatement(arg);
    }
    
    public void visit(ASTSourceLineStatement arg)  throws ASTVisitException {
        visitDefaultStatement(arg);
    }

    //
    // other elements call visitDefault directly
    //
    
    public void visit(ASTFile arg) throws ASTVisitException {
        visitDefault(arg);
    }

    public void visit(ASTPlugins arg) throws ASTVisitException {
        visitDefault(arg);
    }
    
    public void visit(ASTPluginDeclaration arg) throws ASTVisitException {
        visitDefault(arg);
    }
    
    public void visit(ASTIncludeDeclarationBlock arg) throws ASTVisitException {
        visitDefault(arg);
    }

    public void visit(ASTSortDeclarationBlock arg) throws ASTVisitException {
        visitDefault(arg);
    }

    public void visit(ASTSortDeclaration arg) throws ASTVisitException {
        visitDefault(arg);
    }

    public void visit(ASTFunctionDeclaration arg) throws ASTVisitException {
        visitDefault(arg);
    }

    public void visit(ASTFunctionDeclarationBlock arg) throws ASTVisitException {
        visitDefault(arg);
    }
    
    public void visit(ASTAxiomDeclaration arg) throws ASTVisitException {
        visitDefault(arg);
    }

    public void visit(ASTRule arg) throws ASTVisitException {
        visitDefault(arg);
    }

    public void visit(ASTRuleFind arg) throws ASTVisitException {
        visitDefault(arg);
    }

    public void visit(ASTRuleAssume arg) throws ASTVisitException {
        visitDefault(arg);
    }

    public void visit(ASTRuleReplace arg) throws ASTVisitException {
        visitDefault(arg);
    }

    public void visit(ASTRuleAdd arg) throws ASTVisitException {
        visitDefault(arg);
    }

    public void visit(ASTBinderDeclarationBlock arg) throws ASTVisitException {
        visitDefault(arg);
    }

    public void visit(ASTBinderDeclaration arg) throws ASTVisitException {
        visitDefault(arg);
    }

    public void visit(ASTWhereClause arg) throws ASTVisitException {
        visitDefault(arg);
    }

    public void visit(ASTLocatedTerm arg) throws ASTVisitException {
        visitDefault(arg);
    }

    public void visit(ASTGoalAction arg) throws ASTVisitException {
        visitDefault(arg);
    }

    public void visit(ASTRuleRemove arg) throws ASTVisitException {
        visitDefault(arg);
    }
    
    public void visit(ASTProgramDeclaration arg) throws ASTVisitException {
        visitDefault(arg);
    }

    @Override
    public void visit(ASTProperties arg) throws ASTVisitException {
        visitDefault(arg);
    }

    @Override
    public void visit(ASTPropertiesDeclaration arg) throws ASTVisitException {
        visitDefault(arg);
    }
    
}
