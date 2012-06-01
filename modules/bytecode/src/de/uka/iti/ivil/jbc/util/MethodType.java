package de.uka.iti.ivil.jbc.util;

import java.util.ArrayList;
import java.util.List;

import sun.reflect.generics.parser.SignatureParser;
import sun.reflect.generics.tree.MethodTypeSignature;

/**
 * This class provides means of representing types of methods.
 * 
 * @author timm.felden@felden.com
 * 
 */
public class MethodType {

    // the descriptor or signature describing the method, because descriptors
    // are a subset of signatures
    private final String jvmType;
    // null for nongeneric types/methods
    private final MethodTypeSignature signature;

    // unescaped names of bound variables
    private List<String> boundVariables;

    private ObjectType resultType;
    private List<ObjectType> argumentTypes;

    private MethodType(final String jvmType, MethodTypeSignature signature) {
        this.jvmType = jvmType;
        this.signature = signature;
    }

    public static MethodType createFromDescriptor(String descriptor) {
        return new MethodType(descriptor, null);
    }

    public static MethodType createFromSignature(String signature) {
        return new MethodType(signature, SignatureParser.make().parseMethodSig(signature));
    }

    /**
     * @param boundVariablesSignaturePrefix
     *            the prefix of a method specifying the generic bound variables
     *            of this method. It is empty, if the method does not bind
     *            variables. Note that this is required because there is no
     *            nicer way to allow for restricted bound variables.
     * @param arguments
     *            the list of argument types
     * @param resultType
     *            the result type
     */
    public static MethodType createFromObjectTypes(String boundVariablesSignaturePrefix, List<ObjectType> arguments,
            ObjectType resultType) {

        boolean isGeneric = !"".equals(boundVariablesSignaturePrefix);
        StringBuilder sb = new StringBuilder(boundVariablesSignaturePrefix);
        sb.append("(");
        for (ObjectType t : arguments) {
            sb.append(t.getJVMType());
            if (t.isGeneric())
                isGeneric = true;
        }
        sb.append(")");
        sb.append(resultType.getJVMType());
        if (resultType.isGeneric())
            isGeneric = true;

        MethodType result = new MethodType(sb.toString(), isGeneric ? SignatureParser.make().parseMethodSig(
                sb.toString()) : null);

        result.argumentTypes = arguments;
        result.resultType = resultType;

        return result;
    }

    // public static MethodType createTypeSignatureFromDescriptor(String
    // descriptor) {
    // // this is a very very simple parser that splits the descriptor into its
    // // components and passes them down to the type name parser, which in
    // // return constructs a type name
    //
    // ArrayList<ObjectType> argumentTypes = new ArrayList<ObjectType>();
    // ObjectType returnType;
    // int position = 0;
    // // "("
    // assert descriptor.charAt(position) == '(';
    // position++;
    //
    // // argumentType*
    // while (')' != descriptor.charAt(position)) {
    // int index = position;
    // // "["*
    // while ('[' == descriptor.charAt(index))
    // index++;
    //
    // // for refs consume the class name until the ;
    // if ('L' == descriptor.charAt(index)) {
    // // skip until ";"
    // while (';' != descriptor.charAt(index))
    // index++;
    // }
    //
    // // index points now to the last character of the current descriptor,
    // // while position points to the first character
    // index++;
    // argumentTypes.add(ObjectType.createTypeFromSingleTypeDescriptor(descriptor.substring(position,
    // index)));
    //
    // position = index;
    // }
    //
    // // ")"
    // assert descriptor.charAt(position) == ')';
    // position++;
    //
    // // resultType
    // {
    // int index = position;
    // // "["*
    // while ('[' == descriptor.charAt(index))
    // index++;
    //
    // // for refs consume the class name until the ;
    // if ('L' == descriptor.charAt(index)) {
    // // skip until ";"
    // while (';' != descriptor.charAt(index))
    // index++;
    // }
    //
    // // index points now to the last character of the current descriptor,
    // // while position points to the first character
    // index++;
    // returnType =
    // ObjectType.createTypeFromSingleTypeDescriptor(descriptor.substring(position,
    // index));
    //
    // // check that we did not miss anything
    // assert index == descriptor.length();
    // }
    //
    // return new MethodType(returnType, argumentTypes);
    // }

    // public static MethodType createTypeSignature(ObjectType resultType,
    // List<ObjectType> argumentTypes) {
    // return new MethodType(resultType, argumentTypes);
    // }

    /**
     * @return the type of the result
     */
    public ObjectType getResultType() {
        if (null == resultType) {
            String desc = jvmType.substring(jvmType.lastIndexOf(")") + 1);
            if (null == signature)
                resultType = ObjectType.createTypeFromSingleTypeDescriptor(desc);
            else
                resultType = ObjectType.createTypeFromSingleTypeSignature(desc);
        }
        return resultType;
    }

    /**
     * @return a list of the types of the methods arguments
     */
    public List<ObjectType> getArgumentTypes() {
        if (null == argumentTypes) {
            argumentTypes = new ArrayList<ObjectType>();
            // this is rather sad, but we have to walk through the jvmType,
            // count argument depth and split the type in that way into pieces
            int lastIndex, index, depth = 0;
            index = jvmType.indexOf('(') + 1;
            lastIndex = index - 1;
            boolean workToDo = true, canBeBasic = true;
            while (workToDo) {
                switch (jvmType.charAt(index)) {
                case 'B':
                case 'C':
                case 'D':
                case 'F':
                case 'I':
                case 'J':
                case 'S':
                case 'Z':
                    if (canBeBasic) {
                        String desc = jvmType.substring(lastIndex + 1, index + 1);
                        lastIndex = index;
                        if (null != signature)
                            argumentTypes.add(ObjectType.createTypeFromSingleTypeSignature(desc));
                        else
                            argumentTypes.add(ObjectType.createTypeFromSingleTypeDescriptor(desc));
                        canBeBasic = true;
                    }
                    break;
                case 'L':
                case 'T':
                    canBeBasic = false;
                    break;

                case ';':
                    if (0 == depth) {
                        String desc = jvmType.substring(lastIndex + 1, index + 1);
                        lastIndex = index;
                        if (null != signature)
                            argumentTypes.add(ObjectType.createTypeFromSingleTypeSignature(desc));
                        else
                            argumentTypes.add(ObjectType.createTypeFromSingleTypeDescriptor(desc));
                        canBeBasic = true;
                    }
                    break;
                case '<':
                    depth++;
                    break;
                case '>':
                    depth--;
                    break;
                case ')':
                    workToDo = false;
                    break;
                }
                index++;
            }
        }
        return argumentTypes;
    }

    public List<String> boundVariables() {
        return boundVariables;
    }

    public boolean isGeneric() {
        return null != signature;
    }

    /**
     * @return a string that contains the descriptor or signature
     */
    public String getJVMType() {
        return jvmType;
    }
}
