package de.uka.iti.pseudo.parser.boogie.ast;

import java.util.Collections;
import java.util.List;

import de.uka.iti.pseudo.parser.boogie.ASTVisitException;
import de.uka.iti.pseudo.parser.boogie.ASTVisitor;
import de.uka.iti.pseudo.parser.boogie.Token;
import de.uka.iti.pseudo.parser.boogie.util.ASTConversions;

public class ProcedureDeclaration extends DeclarationBlock implements NamedASTElement {

    private final String name;
    private final List<String> typeParameters;
    private final List<VariableDeclaration> inParam, outParam;
    private final List<Specification> specification;
    private final ProcedureBody body;

    public ProcedureDeclaration(Token first, List<Attribute> attributes, Token name, List<Token> typeParameters,
            List<VariableDeclaration> inParam, List<VariableDeclaration> outParam, List<Specification> specification,
 ProcedureBody body) {
        super(first, attributes);

        this.name = ASTConversions.getEscapedName(name);
        this.inParam = inParam;
        this.outParam = outParam;
        this.specification = specification;
        this.body = body;

        this.typeParameters = ASTConversions.toEscapedNameList(typeParameters);

        addChildren(inParam);
        addChildren(outParam);
        addChildren(specification);

        if (null != body)
            addChild(body);
    }

    @Override
    public void visit(ASTVisitor v) throws ASTVisitException {
        v.visit(this);
    }

    public String getName() {
        return name;
    }

    public List<String> getTypeParameters() {
        return Collections.unmodifiableList(typeParameters);
    }

    public List<VariableDeclaration> getOutParameters() {
        return Collections.unmodifiableList(outParam);
    }

    public List<VariableDeclaration> getInParameters() {
        return Collections.unmodifiableList(inParam);
    }

    public List<Specification> getSpecification() {
        return Collections.unmodifiableList(specification);
    }

    public ProcedureBody getBody() {
        assert isImplemented();
        return body;
    }

    public boolean isImplemented() {
        return null != body;
    }

    @Override
    public String toString(){
        return "ProcedureDeclaration [" + name + ", " + getLocation() + "]";
    }
}
