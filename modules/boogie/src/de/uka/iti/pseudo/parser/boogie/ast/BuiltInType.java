package de.uka.iti.pseudo.parser.boogie.ast;

import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.environment.EnvironmentException;
import de.uka.iti.pseudo.parser.boogie.ASTVisitException;
import de.uka.iti.pseudo.parser.boogie.ASTVisitor;
import de.uka.iti.pseudo.parser.boogie.Token;
import de.uka.iti.pseudo.term.TermException;
import de.uka.iti.pseudo.term.Type;

final public class BuiltInType extends NamedType {

    private final boolean isInt;
    private final boolean isBool;
    private final int bvDimension;

    public BuiltInType(Token name) {
        super(name, 0);
        
        String n = name.image;

        if (n.equals("int")) {
            isInt = true;
            isBool = false;
            bvDimension = 0;
        } else if (n.equals("bool")) {
            isInt = false;
            isBool = true;
            bvDimension = 0;
        } else if (n.startsWith("bv")) {
            isInt = isBool = false;
            bvDimension = Integer.parseInt(n.substring(2));
        } else
            throw new IllegalArgumentException("The Type " + name.image + " is not built-in.");
    }

    @Override
    public String getPrettyName() {
        if (isInt)
            return "int";
        else if (isBool)
            return "bool";
        else
            return "bv" + bvDimension;
    }

    @Override
    public void visit(ASTVisitor v) throws ASTVisitException {
        v.visit(this);
    }

    public boolean isInt() {
        return isInt;
    }

    public boolean isBool() {
        return isBool;
    }

    public int getBvDimension() {
        return bvDimension;
    }

    public Type newBasicType(Environment env) {
        if (isInt)
            return Environment.getIntType();
        else if (isBool)
            return Environment.getBoolType();
        else
            try {
                return env.mkType("bitvector");
            } catch (EnvironmentException e) {
                e.printStackTrace();
                assert false : "did you change boogie.p?";
            } catch (TermException e) {
                e.printStackTrace();
                assert false : "did you change boogie.p?";
            }
        return null; // can only happen, if assertions are turned of and someone
                     // messed with the system libs
    }

}
