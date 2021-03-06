/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2012 Karlsruhe Institute of Technology
 *
 * The system is protected by the GNU General Public License.
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.parser.file;

import de.uka.iti.pseudo.parser.ASTVisitException;
import de.uka.iti.pseudo.parser.proof.ASTProofScript;
import de.uka.iti.pseudo.parser.proof.ASTProofScriptNode;
import de.uka.iti.pseudo.parser.proof.ASTProofSourceFile;

/**
 * This interface is part of the visitor pattern for {@link ASTFileElement}s.
 */

public interface ASTFileVisitor {

	public void visit(ASTFile file) throws ASTVisitException;

    public void visit(ASTPlugins plugins) throws ASTVisitException;

    public void visit(ASTPluginDeclaration plugin) throws ASTVisitException;

    public void visit(ASTProperties plugins) throws ASTVisitException;

    public void visit(ASTPropertiesDeclaration plugin) throws ASTVisitException;

	public void visit(ASTIncludeDeclarationBlock includeDeclarationBlock) throws ASTVisitException;

	public void visit(ASTSortDeclarationBlock sortDeclarationBlock) throws ASTVisitException;

	public void visit(ASTSortDeclaration sortDeclaration) throws ASTVisitException;

	public void visit(ASTFunctionDeclaration functionDeclaration) throws ASTVisitException;

	public void visit(ASTFunctionDeclarationBlock functionDeclarationBlock) throws ASTVisitException;

	public void visit(ASTLemmaDeclaration astAxiomDeclaration) throws ASTVisitException;

	public void visit(ASTRule rule) throws ASTVisitException;

	public void visit(ASTRuleFind ruleFind) throws ASTVisitException;

	public void visit(ASTRuleAssume ruleAssume) throws ASTVisitException;

	public void visit(ASTRuleReplace ruleReplace) throws ASTVisitException;

	public void visit(ASTRuleAdd ruleAdd) throws ASTVisitException;

	public void visit(ASTBinderDeclarationBlock binderDeclarationBlock) throws ASTVisitException;

	public void visit(ASTBinderDeclaration binderDeclaration) throws ASTVisitException;

    public void visit(ASTWhereClause whereClause) throws ASTVisitException;

    public void visit(ASTLocatedTerm locatedTerm) throws ASTVisitException;

    public void visit(ASTProblemSequent astSequent) throws ASTVisitException;

    public void visit(ASTGoalAction goalAction) throws ASTVisitException;

    public void visit(ASTRuleRemove ruleRemove)  throws ASTVisitException;

    public void visit(ASTProgramDeclaration statementList) throws ASTVisitException;

    public void visit(ASTProofSourceFile astProofSourceFile) throws ASTVisitException;

    public void visit(ASTProofScript astProofScript) throws ASTVisitException;

    public void visit(ASTProofScriptNode astProofScriptNode) throws ASTVisitException;


}
