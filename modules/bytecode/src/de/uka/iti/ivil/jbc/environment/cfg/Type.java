package de.uka.iti.ivil.jbc.environment.cfg;

import de.uka.iti.ivil.jbc.environment.BytecodeCompilerException;
import de.uka.iti.ivil.jbc.util.ObjectType;

/**
 * The class stores information needed for generic type inference for method
 * code attributes. It is also used as a Proxy for types, to allow to give same
 * objects the same TypeName objects, if the instruction is i.e. a load followed
 * by a store.
 * 
 * The type of null will be represented by setting the typeName to null. This
 * type is as well used, if objects are created but the generic type is not yet
 * known, because this is usually represented after the constructor call, thus
 * the type has to flow up the control flow from the statements below. (if there
 * were control flow splits in between, this would result in half created
 * objects and would therefore be rejected)
 */
public class Type {

    /** true iff the int type can be represented as a bool as well */
    private boolean canBeBool;
    /**
     * true iff the given type is exact; this happens for example when
     * extracting type information from the local variable (type) table
     * 
     * exact means, that casts towards objects are not allowed, while casts that
     * make the type more specific are ok
     * 
     * note that this flag is irrelevant in case of integer/float up- and
     * down-casts, because those are not handled by the type inference as they
     * have no influence on the generated functions and formulas
     */
    private boolean isExactType;

    /** used to store the actual type information */
    private ObjectType typeName;

    /**
     * types can only be created by factory methods
     */
    private Type() {
    }

    /**
     * creates a potentially boolean. if you want to create an exact boolean,
     * createn an exact "Z"
     * 
     * @return a new type that can be either int or bool
     */
    static public Type createBoolean() {
        Type rval = new Type();
        rval.canBeBool = true;
        rval.typeName = ObjectType.createTypeFromSingleTypeDescriptor("Z");
        return rval;
    }

    /**
     * @return a new type that has to be exactly the argument type. This is to
     *         be used if the class file tells you the exact type by meta
     *         information.
     */
    static public Type createExactType(ObjectType type) {
        Type rval = new Type();
        rval.isExactType = true;
        rval.canBeBool = type.getJVMType().equals("Z");
        rval.typeName = type;
        return rval;
    }

    /**
     * creates a new type that may be modified during type inference
     */
    static public Type createCastableType(ObjectType type) {
        Type rval = new Type();
        rval.canBeBool = type.getJVMType().equals("Z");
        rval.typeName = type;
        return rval;
    }

    /**
     * creates an unkown type, which has to be used e.g. for typeing aconstnull
     */
    static public Type createUnknown() {
        // how about a magic trick?:D
        return new Type();
    }

    /**
     * tries to merge two types:<br>
     * 
     * If one of the types is exact, the exact type is returned. If both types
     * are exact, they have to be equal.<br>
     * 
     * If one type is null, the other type is returned.(actually this has to be
     * enforced by the invoker)<br>
     * 
     * If both types may be bool, the result may as well be bool. Otherwise the
     * result will not be bool.<br>
     * 
     * Otherwise, the most specific common super type is returned.<br>
     * 
     * Note: if one type is generic, the other type has to be generic as well,
     * thus it may be reinterpreted as having wildcards on all formal arguments.
     * This can be considered an inverse process of type erasure, but is
     * compatible with the concept.<br>
     * 
     * Note: that exact boolean types can be used to forge bytecode that would
     * be legal but is to be rejected with a strange error like integer
     * operations on boolean values. If this turns out to be a problem, either
     * implicit int <-> bool conversions have to be inserted or the bool type
     * has to be dropped.<br>
     * 
     * Note: invocation of this method will change, only this object, thus, if
     * you want to propagate a type constraint, you have to chose the unchanged
     * type as argument and if you want to actually make two types equal, merge
     * them and reset one.<br>
     * 
     * @param type
     *            to be merged with
     * @return this
     */
    public Type mergeWith(Type type) throws BytecodeCompilerException {
        if (null == type)
            return this;

        // deal with exact types
        if (isExactType || type.isExactType) {
            // TODO eq check if both are exact
            if (isExactType) {
                return this;
            } else {
                copy(type);
                return this;
            }
        }

        // deal with non exact bool
        if (canBeBool || type.canBeBool) {
            this.canBeBool = canBeBool & type.canBeBool;
            return this;
        }

        // TODO find most specific common super type; for the moment we assume
        // that both types are equal

        return this;
    }

    /**
     * makes this a copy of the argument type. This method is required due to
     * the proxy nature of the class and because some merges make two types
     * equal and others do not.
     */
    public void copy(Type type) {
        this.isExactType = type.isExactType;
        this.canBeBool = type.canBeBool;
        this.typeName = type.typeName;
    }

    /**
     * TypeNames are immutable, thus they can be exported safely
     */
    public ObjectType getTypeName() {
        return typeName;
    }

    public boolean isCategory2() {
        return typeName.isCategory2Type();
    }

    @Override
    public String toString() {
        return typeName.getJavaType();
    }
}