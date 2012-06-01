package de.uka.iti.ivil.jbc.util;

import java.io.ByteArrayInputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.gjt.jclasslib.structures.ClassFile;

import sun.reflect.generics.parser.SignatureParser;
import sun.reflect.generics.tree.ArrayTypeSignature;
import sun.reflect.generics.tree.BooleanSignature;
import sun.reflect.generics.tree.BottomSignature;
import sun.reflect.generics.tree.ByteSignature;
import sun.reflect.generics.tree.CharSignature;
import sun.reflect.generics.tree.ClassTypeSignature;
import sun.reflect.generics.tree.DoubleSignature;
import sun.reflect.generics.tree.FieldTypeSignature;
import sun.reflect.generics.tree.FloatSignature;
import sun.reflect.generics.tree.FormalTypeParameter;
import sun.reflect.generics.tree.IntSignature;
import sun.reflect.generics.tree.LongSignature;
import sun.reflect.generics.tree.ShortSignature;
import sun.reflect.generics.tree.SimpleClassTypeSignature;
import sun.reflect.generics.tree.TypeSignature;
import sun.reflect.generics.tree.TypeVariableSignature;
import sun.reflect.generics.tree.VoidDescriptor;
import sun.reflect.generics.tree.Wildcard;
import sun.reflect.generics.visitor.TypeTreeVisitor;
import de.uka.iti.ivil.jbc.environment.BytecodeCompilerError;
import de.uka.iti.ivil.jbc.util.parsers.JavaTypeToDescriptorParser;
import de.uka.iti.ivil.jbc.util.parsers.ParseException;
import de.uka.iti.ivil.jbc.util.parsers.ast.type.ClassOrInterfaceType;
import de.uka.iti.ivil.jbc.util.parsers.ast.type.PrimitiveType;
import de.uka.iti.ivil.jbc.util.parsers.ast.type.ReferenceType;
import de.uka.iti.ivil.jbc.util.parsers.ast.type.TypeVisitor;
import de.uka.iti.ivil.jbc.util.parsers.ast.type.VoidType;
import de.uka.iti.ivil.jbc.util.parsers.ast.type.WildcardType;

/**
 * This class represents types of objects and provides means of converting
 * between object type representations.
 *
 * @author timm.felden@felden.com
 *
 */
public class ObjectType {
    private static final Map<String, String> EMPTY_STRING_MAP = new HashMap<String, String>();
    // the descriptor is always present
    final private String jvmType;
    // true iff the type is generic
    final private boolean isGeneric;
    // nonnull iff isGeneric; containes the parsed TypeSignature
    final private TypeSignature signature;

    // the class name in Bytecode style
    private String bytecodeClassName;

    // the type name in java style
    private String javaTypeName;

    // the ivil base type of this type
    private String baseType;

    // the name of the type function symbol
    private String ivilFunctionName;
    // the complete term of this type MUST be null if there are type arguments,
    // because in that case, the term may vary depending on context
    private String ivilTypeTerm;
    // the set of free type variables of this type
    private Set<String> freeTypeVaribales;

    // this will cause TypeNames to be spread across threads and load cycles
    static private Map<String, ObjectType> typeMap = Collections.synchronizedMap(new HashMap<String, ObjectType>());
    static {
        // create default types
        typeMap.put("I", createTypeFromSingleTypeDescriptor("I"));
        typeMap.put("Z", createTypeFromSingleTypeDescriptor("Z"));
        typeMap.put("Ljava/lang/Object;", createTypeFromSingleTypeDescriptor("Ljava/lang/Object;"));

    }

    /**
     * non generic constructor
     */
    private ObjectType(final String jvmType) {
        this.jvmType = jvmType;
        this.isGeneric = false;
        this.signature = null;

        // sanity checks
        assert null != jvmType;

        // TODO remove sanity check
        int depth = jvmType.lastIndexOf('[');
        if ('L' == jvmType.charAt(depth + 1)) {
            if (!jvmType.endsWith(";")) {
                throw new BytecodeCompilerError(jvmType + " is an illformed descriptor");
            }
        } else {
            if (depth + 2 != jvmType.length()) {
                throw new BytecodeCompilerError(jvmType + " is an illformed descriptor");
            }
        }
        if (-1 != depth) {
            for (int i = 0; i <= depth; i++) {
                if ('[' != jvmType.charAt(i)) {
                    throw new BytecodeCompilerError(jvmType + " is an illformed descriptor");
                }
            }
        }
    }

    /**
     * generic constructor
     */
    private ObjectType(final String jvmType, final TypeSignature typeSignature) {
        this.jvmType = jvmType;
        this.isGeneric = true;
        this.signature = typeSignature;

        // sanity checks
        assert null != jvmType;
        assert null != typeSignature;
    }

    /**
     * factory that allows creation of type names from class files
     *
     * @return a type name equivalent to the one requested
     */
    public static ObjectType createTypeFromBytecodeClass(ClassFile cls) {
        return ClassType.createTypeFromBytecodeClass(cls).toObjectType();
    }

    /**
     * creates a type name from a bytecode descriptor; the descriptor musst not
     * contain multiple type informations. If it does, use a TypeSignature!
     *
     * @param descriptor
     *            as defined in JVMSpec second edition §4.3
     */
    public static ObjectType createTypeFromSingleTypeDescriptor(String descriptor) {
        ObjectType rval = typeMap.get(descriptor);
        if (null == rval) {
            rval = new ObjectType(descriptor);
            typeMap.put(rval.getJVMType(), rval);
        }
        return rval;
    }

    public static ObjectType createTypeFromSingleTypeSignature(String signature) {
        // FIXME this is of course not a valid solution
        return createTypeFromSingleTypeDescriptor(signature);
        // ObjectType rval = typeMap.get(signature);
        // if (null == rval) {
        // rval = new ObjectType(signature,
        // SignatureParser.make().parseTypeSig(signature));
        // typeMap.put(rval.getJVMType(), rval);
        // }
        // return rval;
    }

    /**
     * proxy boolean for the type visitor
     *
     * @author timm.felden@felden.com
     *
     */
    private static final class BoolProxy {
        public boolean val;
    }

    /**
     * creates a type name from a
     *
     * TODO implement missing generic java type names
     *
     * @param javaName
     *            a class name of the form pack.age.className
     *
     * @throws BytecodeCompilerError
     *             can be thrown if javaName is illformed
     */
    public static ObjectType createTypeFromJavaTypeName(final String javaName) {
        String descriptor;
        final BoolProxy isBoolean = new BoolProxy();
        try {
            de.uka.iti.ivil.jbc.util.parsers.ast.type.Type type = new JavaTypeToDescriptorParser(
                    new ByteArrayInputStream(javaName.getBytes())).Type();
            StringBuilder sb = new StringBuilder();
            type.visit(new TypeVisitor<StringBuilder>() {

                // needed to ensure, that scopes do not print L...;
                int isScope = 0;

                @Override
                public void visit(ClassOrInterfaceType type, StringBuilder arg) {

                    if (0 == isScope) {
                        arg.append("L");
                    }

                    if (null != type.getScope()) {
                        isScope++;
                        type.getScope().visit(this, arg);
                        arg.append("/");
                        isScope--;
                    }
                    arg.append(type.getName());

                    if (null != type.getTypeArgs() && type.getTypeArgs().size() > 0) {
                        isBoolean.val = true;
                        arg.append("<");
                        List<de.uka.iti.ivil.jbc.util.parsers.ast.type.Type> args = type.getTypeArgs();
                        for (int i = 0; i < args.size(); i++) {
                            args.get(i).visit(this, arg);
                        }
                        arg.append(">");
                    }

                    if (0 == isScope) {
                        arg.append(";");
                    }
                }

                @Override
                public void visit(PrimitiveType type, StringBuilder sb) {
                    switch (type.getType()) {
                    case Boolean:
                        sb.append("Z");
                        return;
                    case Byte:
                        sb.append("B");
                        return;
                    case Char:
                        sb.append("C");
                        return;
                    case Double:
                        sb.append("D");
                        return;
                    case Float:
                        sb.append("F");
                        return;
                    case Int:
                        sb.append("I");
                        return;
                    case Long:
                        sb.append("J");
                        return;
                    case Short:
                        sb.append("S");
                        return;
                    }
                }

                @Override
                public void visit(ReferenceType type, StringBuilder arg) {
                    for (int i = type.getArrayCount(); i > 0; i--) {
                        arg.append("[");
                    }
                    type.getType().visit(this, arg);
                }

                @Override
                public void visit(VoidType type, StringBuilder arg) {
                    arg.append("V");
                }

                @Override
                public void visit(WildcardType type, StringBuilder arg) {
                    if (null != type.getExtends()) {
                        arg.append("+");
                        type.getExtends().visit(this, arg);
                    } else if (null != type.getSuper()) {
                        arg.append("-");
                        type.getSuper().visit(this, arg);
                    } else {
                        arg.append("*");
                    }
                }
            }, sb);
            descriptor = sb.toString();
        } catch (ParseException e) {
            throw new BytecodeCompilerError(e);
        }
        ObjectType rval = typeMap.get(descriptor);
        if (null == rval) {
            if (isBoolean.val) {
                rval = new ObjectType(descriptor, SignatureParser.make().parseTypeSig(descriptor));
            } else {
                rval = new ObjectType(descriptor);
            }
            rval.javaTypeName = javaName;
            typeMap.put(rval.getJVMType(), rval);
        }
        return rval;
    }

    public static ObjectType createInt() {
        return typeMap.get("I");
    }

    public static ObjectType createObject() {
        return typeMap.get("Ljava/lang/Object;");
    }

    public String getJVMType() {
        return jvmType;
    }

    public boolean isGeneric() {
        return isGeneric;
    }

    /**
     * if the type name is an array, the class name is the name of the base type
     * of the array.
     *
     * @return the class name in Bytecode format; if there is no class name,
     *         null will be returned
     */
    public String getBytecodeClassName() {
        if (null == bytecodeClassName) {
            if (isGeneric) {
                final StringBuilder sb = new StringBuilder();
                if (signature instanceof ClassTypeSignature) {
                    ClassTypeSignature param = (ClassTypeSignature) signature;

                    for (int i = 0; i < param.getPath().size(); i++) {
                        if (i != 0) {
                            sb.append("/");
                        }
                        sb.append(param.getPath().get(i).getName());
                    }
                    bytecodeClassName = sb.toString();
                } else {
                    throw new BytecodeCompilerError("missing implementation");
                }

            } else {
                // faster
                try {
                    int start = 2 + jvmType.lastIndexOf('[');
                    bytecodeClassName = jvmType.substring(start, jvmType.length() - 1);
                } catch (Exception e) {
                    // legal iff the type simply does not have a base class
                }
            }
        }
        return bytecodeClassName;
    }

    public String getJavaType() {
        if (null == javaTypeName) {
            // TODO FieldTypeSignature f =
            // SignatureParser.make().parseTypeSig(javaName);
            int depth = jvmType.lastIndexOf('[');
            String tmp = jvmType.substring(depth + 1);
            // TODO MU: I changed this to a char switch statement, was Java7 string switch
            switch (tmp.charAt(0)) {
            case 'Z':
                tmp = "boolean";
                break;
            case 'B':
                tmp = "byte";
                break;
            case 'C':
                tmp = "char";
                break;
            case 'I':
                tmp = "int";
                break;
            case 'S':
                tmp = "short";
                break;
            case 'J':
                tmp = "long";
                break;
            case 'D':
                tmp = "double";
                break;
            case 'F':
                tmp = "float";
                break;

            case 'V':
                tmp = "void";
                break;

            default:
                tmp = tmp.substring(1, tmp.length() - 1).replace('/', '.');
                break;
            }
            while (depth > 0) {
                tmp = tmp + "[]";
                depth--;
            }
            javaTypeName = tmp;
        }
        return javaTypeName;
    }

    public boolean isCategory2Type() {
        return jvmType.equals("J") || jvmType.equals("D");
    }

    /**
     * @return the "dimension" of this type
     */
    public int arrayDepth() {
        return 1 + jvmType.lastIndexOf('[');
    }

    /**
     * You can use this safely only for types that can not be reinterpreted,
     * i.e. field types, and method signatures. In method bodies, int may
     * actually be used as bool, which is not reflected here.
     *
     * @return one of int, bool, ref or float
     */
    public String getBaseType() {
        if (null == baseType) {
            if (jvmType.contains(";") || jvmType.contains("[")) {
                baseType = "ref";
            } else if (jvmType.equals("Z")) {
                baseType = "bool";
            } else if ("F".equals(jvmType) || "D".equals(jvmType)) {
                baseType = "float";
            } else {
                baseType = "int";
            }
        }
        return baseType;
    }

    /**
     * do not call this if the object is not a class type
     *
     * @return the ivil function name of the toplevel ivil type as required for
     *         the definition of the function name, i.e. with all arguments
     *         being "type"
     */
    public String getIvilFunctionName() {
        if (null == ivilFunctionName) {
            ivilFunctionName = getIvilTypeTerm();

            if (isGeneric) {
                // we need to do some correction, if the type is generic
                if (signature instanceof ClassTypeSignature) {
                    ClassTypeSignature cls = (ClassTypeSignature) signature;
                    // ok this might actually not be correct; lack of
                    // documentation
                    int arity = cls.getPath().get(0).getTypeArguments().length;
                    StringBuilder sb = new StringBuilder(ivilFunctionName.substring(0,
                            ivilFunctionName.indexOf('(') + 1));
                    for (int i = 0; i < arity; i++) {
                        sb.append(i != 0 ? ", type" : "type");
                    }
                    sb.append(")");
                    ivilFunctionName = sb.toString();
                } else if (signature instanceof TypeVariableSignature) {
                    // complicated, because we have to get the sourrounding
                    // class and method to find out what the type variable name
                    // actually means
                    throw new BytecodeCompilerError("todo: TA_?_?");

                } else {
                    throw new BytecodeCompilerError(
                            "you tried to get the function name of a type, that has no function name: " + toString());
                }
            }
        }
        return ivilFunctionName;
    }

    /**
     * returns the function name but without any arguments or braces
     *
     * @see #getIvilFunctionName()
     */
    public String getIvilSimplifiedFunctionName() {
        int index;
        if (-1 == (index = getIvilFunctionName().indexOf('('))) {
            return ivilFunctionName;
        } else {
            return ivilFunctionName.substring(0, index);
        }
    }

    /**
     * Composes a term of type "type" which acts as the translation of the given
     * type. Note that this function will yield null, if the TypeName does not
     * belong to a reference type.
     *
     * This method assumes, that there are no unbound type variables in this
     * type. If there are, you will get an illformed type.
     *
     * @return a term that has equivalent semantics as the type name. If the top
     *         level function symbol has arity 0, the term does not contain
     *         braces.
     */
    public String getIvilTypeTerm() {
        if (null == ivilTypeTerm) {
            ivilTypeTerm = getIvilTypeTerm(EMPTY_STRING_MAP);
        }
        return ivilTypeTerm;
    }

    /**
     * note: type variables will shadow class names, thus the instantiations
     * should replace TA; and LA; alike throw new ByteCodeCompilerError();
     *
     * @param variableInstantiations
     */
    public String getIvilTypeTerm(final Map<String, String> variableInstantiations) {
        if (null == signature) {

            // remove array prefix
            int depth = jvmType.lastIndexOf('[');
            String tmp = jvmType.substring(depth + 1);

            switch (tmp.charAt(tmp.length() - 1)) {
            case 'B':
                depth--;
                tmp = "TF_BYTE_ARRAY";
                break;
            case 'C':
                depth--;
                tmp = "TF_CHAR_ARRAY";
                break;
            case 'D':
                depth--;
                tmp = "TF_DOUBLE_ARRAY";
                break;
            case 'F':
                depth--;
                tmp = "TF_FLOAT_ARRAY";
                break;
            case 'I':
                depth--;
                tmp = "TF_INT_ARRAY";
                break;
            case 'J':
                depth--;
                tmp = "TF_LONG_ARRAY";
                break;
            case 'S':
                depth--;
                tmp = "TF_SHORT_ARRAY";
                break;
            case 'Z':
                depth--;
                tmp = "TF_BOOLEAN_ARRAY";
                break;

            case 'V':
                // this musst not appear in the output, except for signatures in
                // method name escapes
                tmp = "void";
                break;

            default: {
                tmp = tmp.substring(1, tmp.length() - 1);
                String[] pack = tmp.split("/");
                tmp = "T";
                for (int i = 0; i < pack.length; i++) {
                    tmp = tmp + "_" + EscapeName.build(pack[i]);
                }
            }
                break;
            }
            while (depth > 0) {
                tmp = "TF_array(" + tmp + ")";
                depth--;
            }
            return tmp;
        } else {
            final StringBuilder sb = new StringBuilder();
            signature.accept(new TypeTreeVisitor<Void>() {

                @Override
                public Void getResult() {
                    return null;
                }

                @Override
                public void visitArrayTypeSignature(ArrayTypeSignature arg0) {
                    // TODO MU: was a Java7-string-switch

                    String canonicalName = arg0.getComponentType().getClass().getCanonicalName();
                    if(canonicalName.equals("sun.reflect.generics.tree.BooleanSignature")) {
                        sb.append("TF_BOOLEAN_ARRAY");
                    } else {
                        sb.append("TF_array(");
                        arg0.getComponentType().accept(this);
                        sb.append(")");
                    }
                }

                @Override
                public void visitBooleanSignature(BooleanSignature arg0) {
                    // TODO boxing
                }

                @Override
                public void visitBottomSignature(BottomSignature arg0) {
                    throw new BytecodeCompilerError("found unexpected type Bottom aka TypeOfNull");
                }

                @Override
                public void visitByteSignature(ByteSignature arg0) {
                    // TODO boxing

                }

                @Override
                public void visitCharSignature(CharSignature arg0) {
                    // TODO boxing

                }

                @Override
                public void visitClassTypeSignature(ClassTypeSignature arg0) {
                    sb.append("T");
                    for (SimpleClassTypeSignature s : arg0.getPath()) {
                        sb.append("_").append(s.getName());
                    }
                    SimpleClassTypeSignature cls = arg0.getPath().get(arg0.getPath().size() - 1);

                    if (null != cls.getTypeArguments() && cls.getTypeArguments().length > 0) {
                        sb.append("(");
                        for (int i = 0; i < cls.getTypeArguments().length; i++) {
                            if (0 != i) {
                                sb.append(", ");
                            }
                            cls.getTypeArguments()[i].accept(this);
                        }
                        sb.append(")");
                    }
                }

                @Override
                public void visitDoubleSignature(DoubleSignature arg0) {
                    // TODO boxing

                }

                @Override
                public void visitFloatSignature(FloatSignature arg0) {
                    // TODO boxing

                }

                @Override
                public void visitFormalTypeParameter(FormalTypeParameter arg0) {
                    throw new BytecodeCompilerError("found unexpected formal type parameter in ObjectType.");
                }

                @Override
                public void visitIntSignature(IntSignature arg0) {
                    // TODO boxing

                }

                @Override
                public void visitLongSignature(LongSignature arg0) {
                    // TODO boxing

                }

                @Override
                public void visitShortSignature(ShortSignature arg0) {
                    // TODO boxing
                }

                @Override
                public void visitSimpleClassTypeSignature(SimpleClassTypeSignature arg0) {
                    // for some unrecognized reason, the name may contain . to
                    // separate packages on a simple class; this seems to be an
                    // undocumented feature of the signature parser, which
                    // always creates an upper and a lower bound and if one of
                    // them is not present, this bound will be a simple class
                    // with the name "java.lang.Object"
                    if("java.lang.Object".equals(arg0.getName())){
                        sb.append("T_java_lang_Object");
                        return;
                    }

                    sb.append("T_").append(EscapeName.build(arg0.getName()));

                    if (null != arg0.getTypeArguments() && arg0.getTypeArguments().length > 0) {
                        sb.append("(");
                        for (int i = 0; i < arg0.getTypeArguments().length; i++) {
                            if (0 != i) {
                                sb.append(", ");
                            }
                            arg0.getTypeArguments()[i].accept(this);
                        }
                        sb.append(")");
                    }
                }

                @Override
                public void visitTypeVariableSignature(TypeVariableSignature arg0) {
                    String inst = variableInstantiations.get(arg0.getIdentifier());
                    if (null != inst) {
                        sb.append(inst);
                    } else {
                        // fail silently because sometimes we dont need the
                        // whole term but there is no easier way to create this
                        // (ugly code:-()
                        sb.append("☢found unbound type variable(" + arg0.getIdentifier() + ") in type " + getJVMType()
                                + "☢");
                    }
                }

                @Override
                public void visitVoidDescriptor(VoidDescriptor arg0) {
                    // TODO boxing
                }

                @Override
                public void visitWildcard(Wildcard arg0) {
                    sb.append("(\\wildcard wc; ");
                    if (null == arg0.getUpperBounds() && null == arg0.getLowerBounds()) {
                        sb.append(true);
                    } else {
                        // for some reason, the SignatureParser creates upper
                        // and lower bounds, although the signature grammar does
                        // not offer this feature
                        boolean first = true;
                        if (null != arg0.getLowerBounds()) {
                            for (FieldTypeSignature t : arg0.getLowerBounds()) {
                                sb.append(first ? "superType(" : " & superType(");
                                first = false;

                                t.accept(this);
                                sb.append(", wc)");
                            }
                        }
                        if (null != arg0.getUpperBounds()) {
                            for (FieldTypeSignature t : arg0.getUpperBounds()) {
                                sb.append(first ? "superType(wc, " : " & superType(wc, ");
                                first = false;

                                t.accept(this);
                                sb.append(")");
                            }
                        }
                    }
                    sb.append(")");
                }
            });
            return sb.toString();
        }
    }

    /**
     * returns the amount of formal parameters that have to be added to this
     * type to make it usable. This can only be nonnull in case of generic types
     * and is null in case of instantiated generic types, because the type
     * parameters are saved, so a Map<String,String> has 0 arity.
     */
    public Set<String> getFreeTypeVariables() {
        // TODO currently only types without arguments are implemented
        // note: this might actually never have the intended meaning; maybe the
        // getIvilTerm function with arguments is indeed a factory method that
        // creates a new typename
        return Collections.emptySet();
    }

    /**
     * reduce the equality to the equality of descriptors
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o instanceof ObjectType) {
            ObjectType t = (ObjectType) o;
            // TODO take generics into account
            return t.jvmType.equals(jvmType);
        }
        return false;
    }

    /**
     * hashCode compatible to equals
     */
    @Override
    public int hashCode() {
        return jvmType.hashCode();
    }

    @Override
    public String toString() {
        return jvmType;
    }
}
