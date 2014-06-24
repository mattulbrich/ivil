package de.uka.iti.pseudo.parser.proof;

import java.util.ArrayList;
import java.util.List;

import de.uka.iti.pseudo.parser.ASTElement;
import de.uka.iti.pseudo.parser.ASTVisitException;
import de.uka.iti.pseudo.parser.ASTVisitor;
import de.uka.iti.pseudo.parser.Token;
import de.uka.iti.pseudo.util.Triple;

public class ASTProofScriptNode extends ASTElement {

    private final Token command;
    private final Token first;

    private final List<Triple<Token,Token,String>> arguments =
            new ArrayList<Triple<Token,Token,String>>();


    public ASTProofScriptNode(Token first, Token command) {
        this.first = first;
        this.command = command;
    }

    public ASTProofScriptNode(Token first) {
        this.first = first;
        this.command = first;
    }

    @Override
    public void visit(ASTVisitor v) throws ASTVisitException {
        v.visit(this);
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
        if(command != null) {
            return super.toString() + "[" + command + "]";
        } else {
            return super.toString() + "(empty)";
        }
    }

    public Token getCommand() {
        return command;
    }

    /**
     * @return the arguments
     */
    public List<Triple<Token, Token, String>> getArguments() {
        return arguments;
    }

}
