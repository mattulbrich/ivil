package de.uka.iti.pseudo.parser.boogie.environment;

import java.util.List;

import de.uka.iti.pseudo.parser.boogie.ast.BuiltInType;

/**
 * This type class is used to represent used types in the boogie file. It is
 * designed to allow for easy translation into the ivil type system.
 * 
 * @author timm.felden@felden.com
 * 
 */
public class UniversalType {

    // global instance for bool s
    private static final UniversalType BOOL_T;
    // global instance for int s
    private static final UniversalType INT_T;

    static {
        // massive improvement, as it prevents a lot of bools and ints to be
        // created

        UniversalType[] tmp = new UniversalType[0];
        BOOL_T = new UniversalType(false, "bool", null, tmp, tmp, tmp, null, 0);
        INT_T = new UniversalType(false, "int", null, tmp, tmp, tmp, null, 0);
    }

    /**
     * this is true iff this *type* is a placeholder for a polymorphic type
     * variable
     */
    final boolean isTypeVariable;

    /**
     * null iff this type represents a map, the typename else
     */
    final String name;

    /**
     * this field will be nonnull iff the type equals a type synonym
     */
    final String aliasname;

    /**
     * linkage of types is done using pointers
     */
    final UniversalType[] parameters;

    /**
     * if the type was created by a typeconstructor, there can be template
     * arguments
     */
    final UniversalType[] templateArguments;

    /**
     * contains domaintypes
     */
    final UniversalType[] domain;

    /**
     * contains the range type
     */
    final UniversalType range;

    /**
     * as procedures can return multiple results of type range, we need a
     * rangeCount to express that fact
     */
    final int rangeCount;

    /**
     * As instances of this class can be created from a lot of objects,
     * factories have been created to ensure intuitive usage and extension
     * 
     * @param isTypeVariable
     *            true iff the type represents a variable used as type parameter
     *            somewhere
     * 
     * @param name
     *            a readable name of this type; in general the name should be
     *            parsable to the defining type; exception to this rule are
     *            procedure types, as they use rangeCounts which have no boogie
     *            equivalent
     * 
     * @param aliasname
     *            used by type synonyms to allow for pretty printing
     * 
     * @param parameters
     *            list of type parameters declared at this level, all parameters
     *            need to be type variables
     * 
     * @param templTypes
     *            used for arguments to so called type constructors
     * 
     * @param domain
     *            the domain of a map type
     * 
     * @param range
     *            the range of a map type or function or procedure
     * 
     * @param rangeCount
     *            number of results of procedures; for simplicity reasons, this
     *            is also used for the other types with ranges
     */
    private UniversalType(boolean isTypeVariable, String name, String aliasname, UniversalType[] parameters,
            UniversalType[] templTypes, UniversalType[] domain, UniversalType range, int rangeCount) {
        this.isTypeVariable = isTypeVariable;
        this.name = name;
        this.aliasname = aliasname;
        this.parameters = parameters;
        this.templateArguments = templTypes;
        this.domain = domain;
        this.range = range;
        this.rangeCount = rangeCount;
    }

    /**
     * Create typeparameter from String.
     * 
     * @param s
     *            name of the typeparameter
     */
    static UniversalType newTypeParameter(String s) {
        UniversalType[] tmp = new UniversalType[0];
        return new UniversalType(true, s, null, tmp, tmp, tmp, null, 0);
    }

    /**
     * Create universal equivalent of built in types.
     * 
     * @param t
     */
    static UniversalType newBasicType(BuiltInType t) {
        if (t.isBool())
            return BOOL_T;
        else if (t.isInt())
            return INT_T;

        UniversalType[] tmp = new UniversalType[0];
        return new UniversalType(false, t.getPrettyName(), null, tmp, tmp, tmp, null, 0);
    }

    /**
     * Creates a new bool.
     */
    static UniversalType newBool() {
        return BOOL_T;
    }

    /**
     * Creates a new int.
     */
    static UniversalType newInt() {
        return INT_T;
    }

    /**
     * Create a map out of parameter, domain and range.
     * 
     * @param param
     * @param domain
     * @param range
     * @param rangeCount
     */
    static UniversalType newMap(List<UniversalType> parameters, List<UniversalType> domain, UniversalType range,
            int rangeCount) {

        // construct a nice name a la <...>[...]...
        StringBuffer buf = new StringBuffer();

        if (0 != parameters.size()) {

            buf.append("< ");
            for (int i = 0; i < parameters.size() - 1; i++) {
                buf.append(parameters.get(i).name);
                buf.append(", ");
            }
            buf.append(parameters.get(parameters.size() - 1).name);
            buf.append(" >");
        }

        buf.append("[");
        if (0 != domain.size()) {
            buf.append(" ");
            for (int i = 0; i < domain.size() - 1; i++) {
                buf.append(domain.get(i).name);
                buf.append(", ");
            }
            buf.append(domain.get(domain.size() - 1).name);
            buf.append(" ");
        }
        buf.append("]");
        if (1 == rangeCount)
            buf.append(range.name);
        else {
            buf.append("returns (");
            for (int i = 0; i < rangeCount; i++)
                buf.append(range.name);
            buf.append(")");
        }

        return new UniversalType(false, buf.toString(), null, parameters.toArray(new UniversalType[parameters.size()]),
                null, domain.toArray(new UniversalType[domain.size()]), range, rangeCount);

    }

    /**
     * This can be used e.g. to check a := [5]5. this statement is valid, if
     * [int]int is a subtype of a, thus a can be declared as "[int]int" or as
     * "<a>[a]int" or as "<a>[a]a"
     * 
     * @param t
     * @return true, iff one could completely replace t with this
     */
    public boolean compatible(UniversalType t) {
        /*
         * For example, the types [int]bool and α [α]bool are not compatible.
         * !!!
         */

        // TODO implement
        return false;
    }

    /**
     * One can assign into or from polymorphic types, as Objects of type <a>
     * might be ints or <b>s
     * 
     * @param t
     * @return true, if one could assign t to a variable of type this
     */
    public boolean assignable(UniversalType t) {

        return false;
    }

    @Override
    public String toString() {
        return name;
    }
}
