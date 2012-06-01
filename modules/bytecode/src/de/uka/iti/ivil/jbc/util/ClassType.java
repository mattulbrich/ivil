package de.uka.iti.ivil.jbc.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.gjt.jclasslib.structures.ClassFile;
import org.gjt.jclasslib.structures.attributes.SignatureAttribute;
import org.gjt.jclasslib.structures.constants.ConstantUtf8Info;

import sun.reflect.generics.parser.SignatureParser;
import sun.reflect.generics.tree.ClassSignature;
import sun.reflect.generics.tree.FormalTypeParameter;
import de.uka.iti.ivil.jbc.environment.BytecodeCompilerError;

/**
 * This class provides means of representation and conversion of class types. It
 * is also able to handle fake classes for base type arrays to make the
 * implementation of the class hierarchy easier.
 * 
 * @author timm.felden@felden.com
 */
final public class ClassType {

    // in the fromat of a class name!
    private final String jvmName;
    // a parsed class signature attribute
    private final ClassSignature signature;
    // cached argument list in order of appearance, what is important in case of
    // ivil type terms
    private ArrayList<String> typeArguments;
    // cached transformed ivil type varibale names(\var as type)
    private ArrayList<String> typeVariableNames;

    // the function name as it can be used to define a new function symbol
    private String ivilFunctionName;
    private String simplifiedFunctionName;
    // the function name with inserted quantifiable type arguments
    private String ivilQuantifiedTypeTerm;
    private Map<String, String> defaultInstantiationMap;

    private ClassType(String jvmName, ClassSignature signature) {
        this.jvmName = jvmName;
        this.signature = signature;
    }

    public static ClassType createTypeFromBytecodeClass(ClassFile cls) {
        ClassSignature signature = null;
        if (null != cls.findAttribute(SignatureAttribute.class)) {
            String sig = ((ConstantUtf8Info) cls.getConstantPool()[((SignatureAttribute) cls
                    .findAttribute(SignatureAttribute.class)).getSignatureIndex()]).getString();
            signature = SignatureParser.make().parseClassSig(sig);
        }
        return new ClassType(cls.getThisClassName(), signature);
    }

    /**
     * creates a fake class, which is currently restricted to base type arrays;
     * fake types do not have signatures.
     */
    public static ClassType createFakeType(String type) {
        if ('[' != type.charAt(0) || 2 != type.length())
            throw new BytecodeCompilerError("illegal fake type: " + type);

        return new ClassType(type, null);
    }

    /**
     * calculates the simplified name as well
     * 
     * @return the ivil function name as used in the declaration of the function
     */
    public String getIvilFunctionName() {
        if (null == ivilFunctionName) {
            // fake types
            if ('[' == jvmName.charAt(0)) {
                switch (jvmName.charAt(1)) {
                case 'B':
                    ivilFunctionName = "TF_BYTE_ARRAY";
                    break;
                case 'C':
                    ivilFunctionName = "TF_CHAR_ARRAY";
                    break;
                case 'D':
                    ivilFunctionName = "TF_DOUBLE_ARRAY";
                    break;
                case 'F':
                    ivilFunctionName = "TF_FLOAT_ARRAY";
                    break;
                case 'I':
                    ivilFunctionName = "TF_INT_ARRAY";
                    break;
                case 'J':
                    ivilFunctionName = "TF_LONG_ARRAY";
                    break;
                case 'S':
                    ivilFunctionName = "TF_SHORT_ARRAY";
                    break;
                case 'Z':
                    ivilFunctionName = "TF_BOOLEAN_ARRAY";
                    break;
                }
            } else {
                // real types
                String[] pack = jvmName.split("/");
                StringBuilder sb = new StringBuilder("T");
                for (int i = 0; i < pack.length; i++) {
                    sb.append("_").append(EscapeName.build(pack[i]));
                }

                simplifiedFunctionName = sb.toString();

                // generic?
                if (null != signature && signature.getFormalTypeParameters().length > 0) {
                    sb.append("(");
                    for (int i = 0; i < signature.getFormalTypeParameters().length; i++)
                        sb.append(0 == i ? "type" : ", type");
                    sb.append(")");
                }

                ivilFunctionName = sb.toString();
            }
        }
        return ivilFunctionName;
    }

    /**
     * simplified function name, that does not contain arguments
     */
    public String getIvilSimplifiedFunctionName() {
        if (null == simplifiedFunctionName) {
            getIvilFunctionName();
        }
        return simplifiedFunctionName;
    }

    /**
     * a quantified version of the {@link #getIvilFunctionName()} method. This
     * one has the "type"s replaced by the quantifiable variable names.
     * 
     * It is legitimate to cache this String because it will be reused
     * frequently during type hierarchy creation.
     */
    public String getIvilQuantifiedTypeTerm() {
        if (null == ivilQuantifiedTypeTerm) {
            String typeName = getIvilFunctionName();
            if (getIvilTypeVaribaleNames().size() > 0) {
                StringBuilder sb = new StringBuilder(typeName.substring(0, typeName.indexOf('(') + 1));
                for (int i = 0; i < typeVariableNames.size(); i++) {
                    if (0 != i)
                        sb.append(", ");
                    sb.append(typeVariableNames.get(i));
                }
                sb.append(")");
                ivilQuantifiedTypeTerm = sb.toString();
            } else
                ivilQuantifiedTypeTerm = ivilFunctionName;
        }
        return ivilQuantifiedTypeTerm;
    }

    /**
     * creates a quantified type term with custom instantiations for types. For
     * convenience, this works even if there are no type arguments.
     * 
     * @param instantiations
     *            terms that are inserted in order
     * 
     * @return the expected term as string
     */
    public String getIvilCustomTypeTerm(ArrayList<String> instantiations) {
        if (getIvilTypeVaribaleNames().size() > 0) {
            StringBuilder sb = new StringBuilder(getIvilSimplifiedFunctionName());
            sb.append("(");
            for (int i = 0; i < instantiations.size(); i++) {
                if (0 != i)
                    sb.append(", ");
                sb.append(instantiations.get(i));
            }
            sb.append(")");
            return sb.toString();
        } else
            return ivilFunctionName;
    }

    /**
     * @return a list of the type variables bound by this class
     */
    public ArrayList<String> getFreeTypeVariables() {
        if (null == typeArguments)
            if (null == signature) {
                typeArguments = new ArrayList<String>(0);
            } else {
                ArrayList<String> rval = new ArrayList<String>(signature.getFormalTypeParameters().length);
                for (FormalTypeParameter s : signature.getFormalTypeParameters())
                    rval.add(s.getName());

                typeArguments = rval;
            }
        return typeArguments;
    }

    /**
     * @return a list of renamed type variables that can be used as ivil
     *         variable names
     */
    public ArrayList<String> getIvilTypeVaribaleNames() {
        if (null == typeVariableNames) {
            typeVariableNames = new ArrayList<String>();
            if (getFreeTypeVariables().size() > 0) {
                String clsName_ = getIvilFunctionName();
                int end = clsName_.indexOf('(');
                if (-1 != end)
                    clsName_ = clsName_.substring(0, end);
                clsName_ += "_";

                for (int i = 0; i < typeArguments.size(); i++)
                    typeVariableNames.add(i, "TV_" + clsName_ + EscapeName.build(typeArguments.get(i)));
            }
        }
        return typeVariableNames;
    }

    /**
     * Creates an object type out of this class type. Returns null if the type
     * contains free type variables. If a method with instantiations is needed,
     * create such a method, that takes a list of instantiations, which would
     * work a lot like {@link #getIvilQuantifiedTypeTerm()}
     * 
     * If type arguments have to be filled, they will be fild with unrestricted
     * wildcards.
     */
    public ObjectType toObjectType() {
        if (0 == getFreeTypeVariables().size())
            return ObjectType.createTypeFromSingleTypeDescriptor(jvmName.charAt(0) != '[' ? "L" + jvmName + ";"
                    : jvmName);

        StringBuilder sb = new StringBuilder("L");
        sb.append(jvmName);
        sb.append("<");
        for (int i = getFreeTypeVariables().size(); i > 0; i--)
            sb.append("*");
        sb.append(">;");
        return ObjectType.createTypeFromSingleTypeSignature(sb.toString());
    }

    /**
     * Creates a new object type, by creating a type signature. The parameter is
     * used to instantiate the type arguments of the class.
     */
    public ObjectType toObjectType(ArrayList<String> signatureTypeArguments) {
        if (0 == signatureTypeArguments.size())
            return ObjectType.createTypeFromSingleTypeDescriptor("L" + jvmName + ";");

        StringBuilder sb = new StringBuilder("L");
        sb.append(jvmName);
        sb.append("<");
        for (String s : signatureTypeArguments)
            sb.append(s);
        sb.append(">;");
        return ObjectType.createTypeFromSingleTypeSignature(sb.toString());
    }

    public String getBytecodeClassName() {
        return jvmName;
    }

    @Override
    public int hashCode() {
        return jvmName.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;

        if(obj instanceof ClassType)
            return jvmName.equals(((ClassType) obj).jvmName);

        return false;
    }

    @Override
    public String toString() {
        return jvmName;
    }

    public Map<String, String> getDefaultInstantiationMap() {
        if (null == defaultInstantiationMap) {
            defaultInstantiationMap = new HashMap<String, String>();
            for (int i = 0; i < getFreeTypeVariables().size(); i++)
                defaultInstantiationMap.put(getFreeTypeVariables().get(i), getIvilTypeVaribaleNames().get(i));
        }
        return defaultInstantiationMap;
    }
}
