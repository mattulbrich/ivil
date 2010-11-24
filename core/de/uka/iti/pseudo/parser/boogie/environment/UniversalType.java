package de.uka.iti.pseudo.parser.boogie.environment;

import java.util.List;

import de.uka.iti.pseudo.parser.boogie.ASTVisitException;
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
     * arguments; if the template arguments contain typevariables, the type is
     * used in a definition
     */
    final UniversalType[] templateArguments;

    /**
     * contains domaintypes
     */
    final UniversalType[] domain;

    /**
     * contains the range type. if a typesynonym is declared
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
            UniversalType[] templateArguments, UniversalType[] domain, UniversalType range, int rangeCount) {
        this.isTypeVariable = isTypeVariable;
        this.name = name;
        this.aliasname = aliasname;
        this.parameters = parameters;
        this.templateArguments = templateArguments;
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
     * Creates a new bitvector type.
     * 
     * @param dimension
     *            the dimension the new type will have
     * @return a fresh universal type
     */
    static UniversalType newBitvector(int dimension) {
        assert dimension >= 0;

        UniversalType[] tmp = new UniversalType[0];
        return new UniversalType(false, "bv" + dimension, null, tmp, tmp, tmp, null, 0);
    }

    /**
     * Create a new anonymous template type definition.
     * 
     * @param name
     * @param argumentCount
     * @return
     */
    static UniversalType newTemplateType(String name, int argumentCount) {
        UniversalType[] tmp = new UniversalType[0], args = new UniversalType[argumentCount];
        UniversalType _ = newTypeParameter("_");
        for (int i = 0; i < args.length; i++)
            args[i] = _;

        return new UniversalType(false, name, null, tmp, args, tmp, null, 0);
    }

    /**
     * Returns an instantiated template type.
     * 
     * @param definition
     * @param arguments
     * @return
     * @throws ASTVisitException
     */
    static UniversalType newTemplateType(UniversalType definition, List<UniversalType> arguments)
            throws ASTVisitException {
        // ensure a valid number of arguments
        if (arguments.size() < definition.templateArguments.length)
            throw new ASTVisitException("you have to supply more arguments: expected "
                    + definition.templateArguments.length + " but got only " + arguments.size());


        // copy interisting arguments
        UniversalType[] args = definition.templateArguments.clone();
        for (int i = 0; i < args.length; i++)
            args[i] = arguments.get(i);

        UniversalType rval = new UniversalType(false, definition.name, definition.aliasname, definition.parameters,
                args, definition.domain, definition.range, definition.rangeCount);

        if (null != definition.aliasname) {
            // we have to replace occurances to pointers in
            // definitons.templateArguments by the supplied arguments

            rval = replaceInType(rval, definition.templateArguments, args);
        }

        return rval;
    }

    /**
     * Usufull recursive occurance replacement for types used by template type
     * instantiation.
     * 
     * @param old
     *            the typeconstructor
     * @param was
     *            values to replace
     * @param is
     *            values that will replace was
     * @return the new type where all was elements are replaced by their
     *         respective is elements
     */
    private static UniversalType replaceInType(UniversalType old, UniversalType[] was, UniversalType[] is) {

        if (null == old)
            return old;

        boolean changes = false, touched;

        UniversalType[] parameters = old.parameters.clone(), templateArguments = old.templateArguments.clone(), domain = old.domain
                .clone();
        UniversalType range = old.range;

        for (int i = 0; i < old.parameters.length; i++) {
            touched = false;
            for (int j = 0; j < was.length; j++) {
                if (was[j] == parameters[i]) {
                    parameters[i] = is[j];
                    touched = true;
                }
            }
            if (!touched)
                parameters[i] = replaceInType(parameters[i], was, is);
            if (parameters[i] != old.parameters[i])
                changes = true;
        }

        for (int i = 0; i < old.templateArguments.length; i++) {
            touched = false;
            for (int j = 0; j < was.length; j++) {
                if (was[j] == templateArguments[i]) {
                    templateArguments[i] = is[j];
                    touched = true;
                }
            }
            if (!touched)
                templateArguments[i] = replaceInType(templateArguments[i], was, is);
            if (templateArguments[i] != old.templateArguments[i])
                changes = true;
        }
        for (int i = 0; i < old.domain.length; i++) {
            touched = false;
            for (int j = 0; j < was.length; j++) {
                if (was[j] == domain[i]) {
                    domain[i] = is[j];
                    touched = true;
                }
            }
            if (!touched)
                domain[i] = replaceInType(domain[i], was, is);
            if (domain[i] != old.domain[i])
                changes = true;
        }

        touched = false;
        for (int j = 0; j < was.length; j++) {
            if (was[j] == range) {
                range = is[j];
                touched = true;
            }
        }
        if (!touched)
            range = replaceInType(range, was, is);
        if (range != old.range)
            changes = true;

        if (!changes)
            return old;
        else
            return new UniversalType(old.isTypeVariable, old.name, old.aliasname, parameters, templateArguments,
                    domain, range, old.rangeCount);
    }

    /**
     * Returns a new type synonym definition
     * 
     * @param alias
     * @param templateArguments
     * @param parent
     * @return
     */
    static UniversalType newTypeSynonym(String alias, List<UniversalType> templateArguments, UniversalType parent) {

        StringBuffer buf = new StringBuffer();
        buf.append(alias);
        for (UniversalType t : templateArguments) {
            buf.append(" ");
            buf.append(t.name);
        }
        buf.append(" = ");
        buf.append(parent.name);

        return new UniversalType(false, buf.toString(), alias, parent.parameters,
                templateArguments.toArray(new UniversalType[templateArguments.size()]), parent.domain, parent.range, 1);
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
        return (isTypeVariable ? "'" : "") + name;
    }

    /**
     * Gets the dimension out of an this, if this will evaluate to bitvector.
     * 
     * @return the dimension of the corresponding bitvector type
     * @throws IllegalArgumentException
     *             if this will not evaluate to bitvector
     */
    public int getBVDimension() throws IllegalArgumentException {
        String name;

        // FIXME if typevariables are used in concatenation statements, its hard
        // to determine the bitvectors dimension, maybe bitvector dimensions
        // should be determined at runtime
        if (isTypeVariable)
            return 0;

        if (null == this.range)
            name = this.name;
        else {
            if (null != this.range.range)
                throw new IllegalArgumentException();

            if (range.isTypeVariable)
                return 0;

            name = this.range.name;
        }

        if (!name.subSequence(0, 2).equals("bv"))
            throw new IllegalArgumentException();

        try {
            return Integer.parseInt(name.substring(2));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException();
        }
    }
}
