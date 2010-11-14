package de.uka.iti.pseudo.parser.boogie.ast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.uka.iti.pseudo.parser.boogie.ASTVisitException;
import de.uka.iti.pseudo.parser.boogie.ASTVisitor;
import de.uka.iti.pseudo.parser.boogie.Token;

public final class ProcedureImplementation extends DeclarationBlock {

    private final String name;
    private final List<String> typeParameters;
    private final List<Variable> inParam, outParam;
    private final ProcedureBody body;

    public ProcedureImplementation(Token first, List<Attribute> attr, Token name, List<Token> typeParameters,
            List<Variable> inParam, List<Variable> outParam, ProcedureBody body) {
        super(first, attr);

        this.name = name.image;
        this.inParam = inParam;
        this.outParam = outParam;
        this.body = body;

        this.typeParameters = new ArrayList<String>(typeParameters.size());
        for (Token t : typeParameters)
            this.typeParameters.add(t.image);
    }

    @Override
    public void visit(ASTVisitor v) throws ASTVisitException {
        v.visit(this);
    }

    public String getName() {
        return name;
    }

    public List<String> getTypeParameterns() {
        return Collections.unmodifiableList(typeParameters);
    }

    public List<Variable> getOutParameters() {
        return Collections.unmodifiableList(outParam);
    }

    public List<Variable> getInParameters() {
        return Collections.unmodifiableList(inParam);
    }

    public ProcedureBody getBody() {
        return body;
    }
}