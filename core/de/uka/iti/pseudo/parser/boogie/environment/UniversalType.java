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
     * Create typeparameter from String.
     * 
     * @param s
     *            name of the typeparameter
     */
    UniversalType(String s) {
        isTypeVariable = true;
        name = s;
        aliasname = null;
        domain = templateArguments = parameters = new UniversalType[0];
        range = null;
        rangeCount = 0;
    }

    /**
     * Create universal equivalent of built in types.
     * 
     * @param t
     */
    public UniversalType(BuiltInType t){
        isTypeVariable = false;
        name = t.getPrettyName();
        aliasname = null;
        domain = templateArguments = parameters = new UniversalType[0];
        range = null;
        rangeCount = 0;
    }

    /**
     * Creates a new bool. This constructor is needed, as some expressions
     * always return bool.
     * 
     * @param b
     *            only needed to change constructor signature
     */
    public UniversalType(final boolean b) {
        isTypeVariable = false;
        name = "bool";
        aliasname = null;
        domain = templateArguments = parameters = new UniversalType[0];
        range = null;
        rangeCount = 0;
    }

    /**
     * Creates a new int. This constructor is needed, as some expressions always
     * return int.
     * 
     * @param b
     *            only needed to change constructor signature
     */
    public UniversalType(final int b) {
        isTypeVariable = false;
        name = "int";
        aliasname = null;
        domain = templateArguments = parameters = new UniversalType[0];
        range = null;
        rangeCount = 0;
    }

    /**
     * Create a map out of parameter, domain and range.
     * 
     * @param param
     * @param domain
     * @param range
     * @param rangeCount
     */
    public UniversalType(List<UniversalType> param, List<UniversalType> domain, UniversalType range, int rangeCount) {
        isTypeVariable = false;
        templateArguments = null;

        this.parameters = param.toArray(new UniversalType[param.size()]);
        this.domain = domain.toArray(new UniversalType[domain.size()]);
        this.range = range;

        this.rangeCount = rangeCount;

        this.aliasname = null;

        // construct a nice name a la <...>[...]...
        StringBuffer buf = new StringBuffer();

        if (0 != this.parameters.length) {

            buf.append("< ");
            for (int i = 0; i < this.parameters.length - 1; i++) {
                buf.append(this.parameters[i].name);
                buf.append(", ");
            }
            buf.append(this.parameters[this.parameters.length - 1].name);
            buf.append(" >");
        }

        buf.append("[");
        if (0 != this.domain.length) {
            buf.append(" ");
            for (int i = 0; i < this.domain.length - 1; i++) {
                buf.append(this.domain[i].name);
                buf.append(", ");
            }
            buf.append(this.domain[this.domain.length - 1].name);
            buf.append(" ");
        }
        buf.append("]");
        if (1 == rangeCount)
            buf.append(this.range.name);
        else {
            buf.append("returns (");
            for (int i = 0; i < rangeCount; i++)
                buf.append(this.range.name);
            buf.append(")");
        }

        this.name = buf.toString();
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
