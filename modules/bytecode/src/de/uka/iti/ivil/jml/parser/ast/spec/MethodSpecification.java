package de.uka.iti.ivil.jml.parser.ast.spec;

import java.util.ArrayList;
import java.util.List;

import de.uka.iti.ivil.jml.parser.Token;
import de.uka.iti.ivil.jml.parser.ast.Node;
import de.uka.iti.ivil.jml.parser.ast.visitor.GenericVisitor;
import de.uka.iti.ivil.jml.parser.ast.visitor.VoidVisitor;

/**
 * Specification for a single method.
 * 
 * @author timm.felden@felden.com
 * 
 */
public class MethodSpecification extends Node {

    private final List<MethodContract> contracts;

    public MethodSpecification(Token begin, Token end, List<MethodContract> contracts) {
        super(begin.beginLine, begin.beginColumn, end.endLine, end.endColumn);
        this.contracts = contracts;
    }

    // creates a specification with one empty contract
    public MethodSpecification() {
        this.contracts = new ArrayList<MethodContract>(1);
        contracts.add(new MethodContract(new ArrayList<MethodContract.Line>()));
    }

    @Override
    public <R, A> R accept(GenericVisitor<R, A> v, A arg) {
        return v.visit(this, arg);
    }

    @Override
    public <A> void accept(VoidVisitor<A> v, A arg) {
        v.visit(this, arg);
    }

    public List<MethodContract> getContracts() {
        return contracts;
    }
}