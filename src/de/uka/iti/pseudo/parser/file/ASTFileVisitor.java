package de.uka.iti.pseudo.parser.file;

public interface ASTFileVisitor {

	public void visit(ASTFile file);

	public void visit(ASTIncludeDeclarationBlock includeDeclarationBlock);

	public void visit(ASTSortDeclarationBlock sortDeclarationBlock);

	public void visit(ASTSortDeclaration sortDeclaration);

	public void visit(ASTFunctionDeclaration functionDeclaration);

	public void visit(ASTFunctionDeclarationBlock functionDeclarationBlock);

	public void visit(ASTRawTerm rawTerm);

	public void visit(ASTTypeRef typeRef);

	public void visit(ASTTypeVar typeVarRef);

	public void visit(ASTRule rule);

	public void visit(ASTRuleFind ruleFind);

	public void visit(ASTRuleAssume ruleAssume);

	public void visit(ASTRuleReplace ruleReplace);

	public void visit(ASTRuleAdd ruleAdd);

}
