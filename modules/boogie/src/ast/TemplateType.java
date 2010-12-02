package de.uka.iti.pseudo.parser.boogie.ast;

import java.util.List;

import de.uka.iti.pseudo.parser.boogie.ASTVisitException;
import de.uka.iti.pseudo.parser.boogie.ASTVisitor;
import de.uka.iti.pseudo.parser.boogie.Token;

public final class TemplateType extends NamedType {

    private final List<Type> arguments;

    public TemplateType(Token name, List<Type> types) {
        super(name, types.size());
        arguments = types;

        addChildren(types);
    }

    @Override
    public String getPrettyName() {
        if (arguments.size() == 0)
            return name.image;

        StringBuffer b = new StringBuffer(name.image);
        b.append("<");
        for (Type t : arguments) {
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

    public List<Type> getArguments() {
        return arguments;
    }

    @Override
    public String toString(){
        return "TemplateType [" + getPrettyName() + "]";
    }
}
