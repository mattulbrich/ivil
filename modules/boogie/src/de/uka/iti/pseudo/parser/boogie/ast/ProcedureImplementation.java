package de.uka.iti.pseudo.parser.boogie.ast;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import de.uka.iti.pseudo.parser.boogie.ASTVisitException;
import de.uka.iti.pseudo.parser.boogie.ASTVisitor;
import de.uka.iti.pseudo.parser.boogie.Token;
import de.uka.iti.pseudo.parser.boogie.util.ASTConversions;

public final class ProcedureImplementation extends DeclarationBlock implements NamedASTElement {

    private final String name;
    private final List<String> typeParameters;
    private final List<Variable> inParam, outParam;
    private final ProcedureBody body;

    /**
     * needed to create unique names
     */
    private final int implementationID = implementationCounter.getAndIncrement();
    private static final AtomicInteger implementationCounter = new AtomicInteger(0);

    public ProcedureImplementation(Token first, List<Attribute> attr, Token name, List<Token> typeParameters,
            List<Variable> inParam, List<Variable> outParam, ProcedureBody body) {
        super(first, attr);

        this.name = name.image;
        this.inParam = inParam;
        this.outParam = outParam;
        this.body = body;

        this.typeParameters = ASTConversions.toStringList(typeParameters);

        addChildren(inParam);
        addChildren(outParam);
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

    public List<Variable> getOutParameters() {
        return Collections.unmodifiableList(outParam);
    }

    public List<Variable> getInParameters() {
        return Collections.unmodifiableList(inParam);
    }

    public ProcedureBody getBody() {
        return body;
    }

    public int getImplementationID() {
        return implementationID;
    }
}
