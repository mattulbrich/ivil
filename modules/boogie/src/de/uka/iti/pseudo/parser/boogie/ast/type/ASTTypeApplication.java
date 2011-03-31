package de.uka.iti.pseudo.parser.boogie.ast.type;

import java.util.List;

import de.uka.iti.pseudo.parser.boogie.ASTVisitException;
import de.uka.iti.pseudo.parser.boogie.ASTVisitor;
import de.uka.iti.pseudo.parser.boogie.Token;

/**
 * This is a used user defined type and is similar to a TypeApplication in ivil.
 * 
 * @author timm.felden@felden.com
 * 
 */
public final class ASTTypeApplication extends NamedType {

    private final List<ASTType> arguments;

    public ASTTypeApplication(Token name, List<ASTType> types) {
        super(name, types.size());
        arguments = types;

        addChildren(types);
    }

    @Override
    public String getPrettyName() {
        if (arguments.size() == 0)
            return name;

        StringBuffer b = new StringBuffer(name);
        b.append("<");
        for (ASTType t : arguments) {
            b.append(t.getPrettyName());
            if (arguments.indexOf(t) != arguments.size() - 1)
                b.append(", ");
            else
                b.append(">");
        }
        return b.toString();
    }

    @Override
    public void visit(ASTVisitor v) throws ASTVisitException {
        v.visit(this);
    }

    public List<ASTType> getArguments() {
        return arguments;
    }

    @Override
    public String toString(){
        return "TypeApplication [" + getPrettyName() + "]";
    }
}
