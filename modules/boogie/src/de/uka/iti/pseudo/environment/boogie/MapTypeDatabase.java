package de.uka.iti.pseudo.environment.boogie;

import java.util.HashMap;
import java.util.Map;

import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.parser.boogie.ast.MapType;
import de.uka.iti.pseudo.term.TermException;
import de.uka.iti.pseudo.term.Type;
import de.uka.iti.pseudo.term.TypeVisitor;

/**
 * This class maps map types to ivil types. Map types are normalised to ensure
 * correct treatment of type equivalence.
 * 
 * @author timm.felden@felden.com
 */
public final class MapTypeDatabase {

    /**
     * This class is the unfolded representation of a map. It is used to compare
     * map types and to assign the
     * 
     * @author timm.felden@felden.com
     */
    private static class UnfoldedMap extends Type {
        @Override
        public <R, A> R accept(TypeVisitor<R, A> visitor, A parameter) throws TermException {
            assert false : "can not be visited";
            return null;
        }

        @Override
        public boolean equals(Object object) {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public String toString() {
            // TODO Auto-generated method stub
            return null;
        }

    }

    /**
     * maps
     */
    private final Map<UnfoldedMap, Type> map = new HashMap<UnfoldedMap, Type>();;

    private final Environment env;

    public MapTypeDatabase(Environment env) {
        this.env = env;
    }

    public Type getType(MapType node) {
        return null;
    }
}
