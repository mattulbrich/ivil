package de.uka.iti.pseudo.parser.proof;

import java.util.ArrayList;
import java.util.List;

import de.uka.iti.pseudo.parser.ASTElement;
import de.uka.iti.pseudo.parser.ASTVisitException;
import de.uka.iti.pseudo.parser.ASTVisitor;
import de.uka.iti.pseudo.parser.Token;
import de.uka.iti.pseudo.util.Triple;

public class ASTProofNode extends ASTElement {

    private final Token first;

    private final List<Triple<Token,Token,String>> arguments =
            new ArrayList<Triple<Token,Token,String>>();

    public ASTProofNode(Token first) {
        this.first = first;
    }

    @Override
    public void visit(ASTVisitor v) throws ASTVisitException {
        // TODO Auto-generated method stub
    }

    @Override
    public Token getLocationToken() {
        return first;
    }

    public void put(Token key, Token valueToken, String value) {
        arguments.add(Triple.make(key, valueToken, value));
    }

    public void putUnnamed(Token valueToken, String value) {
        put(null, valueToken, value);
    }

    @Override
    public String toString() {
        return super.toString() + "[" + first + "]";
    }

}
