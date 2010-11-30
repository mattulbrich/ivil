package de.uka.iti.pseudo.parser.boogie.environment;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import de.uka.iti.pseudo.parser.boogie.ASTElement;
import de.uka.iti.pseudo.parser.boogie.ASTVisitException;
import de.uka.iti.pseudo.parser.boogie.ast.BuiltInType;
import de.uka.iti.pseudo.parser.boogie.ast.MapAccessExpression;

/**
 * This type class is used to represent used types in the boogie file. It is
 * designed to allow for easy translation into the ivil type system.
 * 
 * @author timm.felden@felden.com
 * 
 */
public class UniversalType {

    // global instance for bool s
    static final UniversalType BOOL_T;
    // global instance for int s
    static final UniversalType INT_T;

    // to allow for nonnull attributes, this is used a lot
    static final UniversalType[] voidMap = new UniversalType[0];

    static {
        // massive improvement, as it prevents a lot of bools and ints to be
        // created

        UniversalType[] tmp = voidMap;
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
     * paths that can be used to infer i'th parameter this uses heavily the
     * structure of generated ASTs: template and domain parameters will be the
     * i'th child; for range -1 will be used, as it is needed for checking but
     * not for inference.
     */
    private final List<InferencePath>[] paths;

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
    @SuppressWarnings("unchecked")
    private UniversalType(boolean isTypeVariable, String name, String aliasname, UniversalType[] parameters,
            UniversalType[] templateArguments, UniversalType[] domain, UniversalType range, int rangeCount) {
        this.isTypeVariable = isTypeVariable;
        this.name = name;
        this.aliasname = aliasname;
        this.parameters = parameters;
        this.templateArguments = null != templateArguments ? templateArguments : voidMap;
        this.domain = null != domain ? domain : voidMap;
        this.range = range;
        this.rangeCount = rangeCount;

        paths = new List[parameters.length];
        for (int i = 0; i < paths.length; i++) {
            paths[i] = InferencePath.getPaths(this, parameters[i]);
            if (paths[i].size() == 0)
                throw new IllegalArgumentException("Type variable " + parameters[i]
                        + " is not mentioned in the domain!");
        }
    }

    /**
     * Create typeparameter from String.
     * 
     * @param s
     *            name of the typeparameter
     */
    static UniversalType newTypeParameter(String s) {
        UniversalType[] tmp = voidMap;
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

        UniversalType[] tmp = voidMap;
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

        UniversalType[] tmp = voidMap;
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
        UniversalType[] tmp = voidMap, args = new UniversalType[argumentCount];
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

        // copy interisting arguments
        UniversalType[] args = definition.templateArguments.clone();
        int pos = 0;
        for (int i = 0; i < args.length; i++) {
            if (definition.templateArguments[i].isTypeVariable) {
                if (pos >= arguments.size())
                    throw new ASTVisitException("you have to supply more arguments");

                args[i] = arguments.get(pos);
                pos++;
            } else {
                args[i] = definition.templateArguments[i];
            }
        }

        UniversalType rval;
        if (null != definition.range)
            rval = new UniversalType(false, definition.name, definition.aliasname, definition.parameters, voidMap,
                    definition.domain, definition.range, definition.rangeCount);
        else
            rval = new UniversalType(false, definition.name, definition.aliasname, definition.parameters, args,
                    definition.domain, definition.range, definition.rangeCount);

        if (null != definition.aliasname) {
            // we have to replace occurances to pointers in
            // definitons.templateArguments by the supplied arguments

            rval = replaceInType(rval, definition.templateArguments, args);
        }

        return rval;
    }

    /**
     * Returns a new type synonym definition
     * 
     * @param alias
     * @param templateArguments
     * @param parent
     * @return
     */
    static UniversalType newTypeSynonym(String alias, List<UniversalType> templateArguments, UniversalType parent,
            Map<String, ASTElement> typeSpace) {

        List<UniversalType> args = new LinkedList<UniversalType>();

        int pos = 0;
        for (int i = 0; i < parent.templateArguments.length; i++) {
            if (!parent.templateArguments[i].isTypeVariable) {
                args.add(parent.templateArguments[i]);
            } else {
                if (pos >= templateArguments.size()) {
                    for (UniversalType t : parent.templateArguments)
                        System.out.println(t);

                    throw new IllegalArgumentException("you supplied not enaugh arguments for this synonym");
                }

                if (typeSpace.containsKey(templateArguments.get(pos).name))
                    args.add(templateArguments.get(pos));
                else
                    args.add(newTypeParameter(templateArguments.get(pos).name));
                pos++;
            }
        }
        for (; pos < templateArguments.size(); pos++)
            if (typeSpace.containsKey(templateArguments.get(pos).name))
                args.add(templateArguments.get(pos));
            else
                args.add(newTypeParameter(templateArguments.get(pos).name));

        return new UniversalType(false, parent.name, alias, parent.parameters, args.toArray(new UniversalType[args
                .size()]), parent.domain, parent.range, parent.rangeCount);
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

        return new UniversalType(false, "", null, parameters.toArray(new UniversalType[parameters.size()]), null,
                domain.toArray(new UniversalType[domain.size()]), range, rangeCount);

    }

    /**
     * Infers parameter types
     * 
     * @param map
     *            base type
     * @param node
     *            occurance of type usage
     * 
     * @param state
     *            state is needed to get typeinformation from nodes
     * 
     * @return type of the result of this access
     * 
     * @throws TypeSystemException
     *             thrown if type can not be infered because wrong arguments
     *             were supplied
     */
    // FIXME check all inference points to make sure no problems like <a>[a, a]a
    // -> [int, bool]??? occur
    public static UniversalType newInferedType(UniversalType map, MapAccessExpression node,
            final EnvironmentCreationState state) throws TypeSystemException {
        if (0 == map.parameters.length)
            return map;

        UniversalType[] is = new UniversalType[map.parameters.length];

        for (int i = 0; i < map.parameters.length; i++) {
            is[i] = map.paths[i].get(0).inferType(node, state);
        }

        UniversalType rval = new UniversalType(false, map.name, map.aliasname, voidMap, map.templateArguments,
                map.domain, map.range, map.rangeCount);

        rval = replaceInType(rval, map.parameters, is);
        if (rval.range.isTypeVariable && rval.range == map.range)
            throw new TypeSystemException("\n" + node.getLocation() + " could not infer type for variable "
                    + rval.range);

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
                if (was[j] == parameters[i] || was[j].name.equals(parameters[i].name)) {
                    parameters[i] = is[j];
                    touched = true;
                    break;
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
                if (was[j] == templateArguments[i] || was[j].name.equals(templateArguments[i].name)) {
                    templateArguments[i] = is[j];
                    touched = true;
                    break;
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
                if (was[j] == domain[i] || was[j].name.equals(domain[i].name)) {
                    domain[i] = is[j];
                    touched = true;
                    break;
                }
            }
            if (!touched)
                domain[i] = replaceInType(domain[i], was, is);
            if (domain[i] != old.domain[i])
                changes = true;
        }

        if (null != range) {
            touched = false;
            for (int j = 0; j < was.length; j++) {
                if (was[j] == range || was[j].name.equals(range.name)) {
                    range = is[j];
                    touched = true;
                    break;
                }
            }
            if (!touched)
                range = replaceInType(range, was, is);
            if (range != old.range)
                changes = true;
        }

        if (!changes)
            return old;
        else
            return new UniversalType(old.isTypeVariable, old.name, old.aliasname, parameters, templateArguments,
                    domain, range, old.rangeCount);
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

        // same types are compatible
        if (this == t)
            return true;

        // if one type is a typevariable, both are compatible, as the variable
        // can be instantiated with a matching type
        if (this.isTypeVariable || t.isTypeVariable)
            return true;

        // if we have a missmatching number of arguments, parameters, ..., we
        // can never be compatible
        if (!(name.equals(name) && parameters.length == t.parameters.length
                && templateArguments.length == t.templateArguments.length && domain.length == t.domain.length && rangeCount == t.rangeCount))
            return false;

        if (range == null)
            return name.equals(t.name);

        // we are compatible, iff we can match paths of typevariables and our
        // children are compatible after we replaced typevariables in the
        // matched way

        if (parameters.length != 0) {
            int[] rotation = new int[parameters.length];
            for (int i = 0; i < rotation.length; i++) {
                boolean match = false;

                int j;

                for (j = 0; !match && j < paths.length; j++) {
                    if (paths[i].size() != paths[j].size())
                        continue;

                    match = true;
                    for (int k = 0; match && k < paths[j].size(); k++) {
                        if (!paths[i].get(k).equals(t.paths[j].get(k)))
                            match = false;
                    }
                }

                if (!match) {
                    return false;
                }
                rotation[i] = j - 1; // -1 needed to compensate j++
            }
            UniversalType[] params = new UniversalType[rotation.length];
            for (int i = 0; i < rotation.length; i++)
                params[i] = parameters[rotation[i]];

            t = replaceInType(t, t.parameters, params);
        }

        for (int i = 0; i < templateArguments.length; i++)
            if (!templateArguments[i].compatible(t.templateArguments[i]))
                return false;

        for (int i = 0; i < domain.length; i++)
            if (!domain[i].compatible(t.domain[i]))
                return false;

        if (range != null || t.range != null)
            if (range == null || t.range == null || !range.compatible(t.range))
                return false;

        return true;
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

    /**
     * @return true iff there is a subtype, that has nonzero template arguments
     */
    public boolean isConstructor() {
        if (isTypeVariable)
            return false;

        if (null != domain)
            for (int i = 0; i < domain.length; i++)
                if (domain[i].isConstructor())
                    return true;

        if (null != range)
            return range.isConstructor();
        else
            return false;
    }

    public void toIvilType() {
        // to allow for compatibility with the ivil typesystem, type arguments
        // have to be sorted, such that all types, that are compatible, will
        // result in the same translation? maybe its easier to remove all
        // duplicates from the tree

        // TODO implement
    }

    public String toString() {
        StringBuffer buf = new StringBuffer();
        if (isTypeVariable)
            buf.append("'");

        if (null != aliasname) {
            buf.append(aliasname);
            buf.append(" = ");
        }

        buf.append(name);

        if (templateArguments.length != 0) {
            buf.append(" <<");
            for (int i = 0; i < templateArguments.length; i++) {
                if (i != 0)
                    buf.append(" ");
                buf.append(templateArguments[i]);
            }
            buf.append(">>");
        }

        // construct a nice name a la <...>[...]...
        if (!isTypeVariable && (0 != parameters.length || 0 != domain.length || range != null)) {
            if (0 != parameters.length) {

                buf.append("< ");
                for (int i = 0; i < parameters.length - 1; i++) {
                    buf.append(parameters[i].name);
                    buf.append(", ");
                }
                buf.append(parameters[parameters.length - 1].name);
                buf.append(" >");
            }

            buf.append("[");
            if (0 != domain.length) {
                buf.append(" ");
                for (int i = 0; i < domain.length - 1; i++) {
                    buf.append(domain[i].toString());
                    buf.append(", ");
                }
                buf.append(domain[domain.length - 1].toString());
                buf.append(" ");
            }
            buf.append("]");

            if (1 == rangeCount) {
                buf.append(range.toString());
            } else {
                buf.append("returns (");
                for (int i = 0; i < rangeCount; i++)
                    buf.append(range.name);
                buf.append(")");
            }
        }

        return buf.toString();
    }
}
