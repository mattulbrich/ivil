package de.uka.iti.pseudo.parser.file;

import de.uka.iti.pseudo.parser.ASTVisitException;

public interface ASTFileVisitor {

	public void visit(ASTFile file) throws ASTVisitException;

	public void visit(ASTIncludeDeclarationBlock includeDeclarationBlock) throws ASTVisitException;

	public void visit(ASTSortDeclarationBlock sortDeclarationBlock) throws ASTVisitException;

	public void visit(ASTSortDeclaration sortDeclaration) throws ASTVisitException;

	public void visit(ASTFunctionDeclaration functionDeclaration) throws ASTVisitException;

	public void visit(ASTFunctionDeclarationBlock functionDeclarationBlock) throws ASTVisitException;

	public void visit(ASTRawTerm rawTerm) throws ASTVisitException;

	public void visit(ASTTypeRef typeRef) throws ASTVisitException;

	public void visit(ASTTypeVar typeVarRef) throws ASTVisitException;

	public void visit(ASTRule rule) throws ASTVisitException;

	public void visit(ASTRuleFind ruleFind) throws ASTVisitException;

	public void visit(ASTRuleAssume ruleAssume) throws ASTVisitException;

	public void visit(ASTRuleReplace ruleReplace) throws ASTVisitException;

	public void visit(ASTRuleAdd ruleAdd) throws ASTVisitException;

	public void visit(ASTBinderDeclarationBlock binderDeclarationBlock) throws ASTVisitException;

	public void visit(ASTBinderDeclaration binderDeclaration) throws ASTVisitException;

}
