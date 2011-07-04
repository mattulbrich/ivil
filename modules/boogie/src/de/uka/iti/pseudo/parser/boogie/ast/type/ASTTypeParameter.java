package de.uka.iti.pseudo.parser.boogie.ast.type;

import de.uka.iti.pseudo.parser.boogie.ASTVisitException;
import de.uka.iti.pseudo.parser.boogie.ASTVisitor;
import de.uka.iti.pseudo.parser.boogie.Token;
import de.uka.iti.pseudo.parser.boogie.ast.NamedASTElement;
import de.uka.iti.pseudo.parser.boogie.util.ASTConversions;

public class ASTTypeParameter extends ASTType implements NamedASTElement {

    /**
     * The escaped name of this parameter.
     */
    private final String name;
    private final Token first;

    public ASTTypeParameter(Token first) {
        this.first = first;
        // note: naming conventions are important, as it guarantees,that no type
        // parameters with names _%i, where %i is an arbitrary integer, can
        // exist
        this.name = ASTConversions.getEscapedName(first);
    }

    @Override
    public String getPrettyName() {
        // type variables are prefixed to match the ivil TypeVariable naming
        // convention
        return "'" + name;
    }

    @Override
    public Token getLocationToken() {
        return first;
    }

    @Override
    public void visit(ASTVisitor v) throws ASTVisitException {
        v.visit(this);
    }

    @Override
    public String getName() {
        return name;
    }

}
