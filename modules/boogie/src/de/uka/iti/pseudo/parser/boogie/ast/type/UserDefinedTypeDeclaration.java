package de.uka.iti.pseudo.parser.boogie.ast.type;

import java.util.List;

import de.uka.iti.pseudo.parser.boogie.ASTVisitException;
import de.uka.iti.pseudo.parser.boogie.ASTVisitor;
import de.uka.iti.pseudo.parser.boogie.Token;
import de.uka.iti.pseudo.parser.boogie.ast.Attribute;
import de.uka.iti.pseudo.parser.boogie.ast.DeclarationBlock;

public class UserDefinedTypeDeclaration extends DeclarationBlock {

    public UserDefinedTypeDeclaration(Token firstToken, List<Attribute> attributes, List<UserTypeDefinition> definitions) {
        super(firstToken, attributes);

        addChildren(attributes);
        addChildren(definitions);
    }

    @Override
    public void visit(ASTVisitor v) throws ASTVisitException {
        v.visit(this);
    }

}
