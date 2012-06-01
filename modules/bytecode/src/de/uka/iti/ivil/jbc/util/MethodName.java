package de.uka.iti.ivil.jbc.util;

import org.gjt.jclasslib.structures.ClassFile;
import org.gjt.jclasslib.structures.InvalidByteCodeException;
import org.gjt.jclasslib.structures.MethodInfo;
import org.gjt.jclasslib.structures.attributes.SignatureAttribute;

import de.uka.iti.ivil.jbc.environment.BytecodeCompilerException;
import de.uka.iti.ivil.jbc.environment.NameResolver;

/**
 * This class provides a universal representation of method names and provides
 * automated conversion to various forms and construction from various forms.
 * 
 * The byte code representation is used to compare names and is therefore always
 * present. The other representations will be built on demand.
 * 
 * Method names are immutable, and can therefore be freely shared. The creation
 * of nonexistent name representations is deterministic and therefore thread
 * safe, as overwriting a value by the same value is harmless.
 * 
 * @author timm.felden@felden.com
 * 
 */
public class MethodName {

    // byte code representation
    private final String bcClassName, bcMethodName, bcDescriptor;

    private MethodType signature;

    // escaped representation
    private String programName, className, methodName;
    private String[] escapedPackage;

    private MethodName(String bcClassName, String bcMethodName, String bcDescriptor) {
        this.bcClassName = bcClassName;
        this.bcMethodName = bcMethodName;
        this.bcDescriptor = bcDescriptor;
    }

    /**
     * Creates a method name from informations obtained from a java byte code
     * method ref field.
     */
    public static MethodName createFromByteCode(String completeClassName, String name, String descriptor) {
        return new MethodName(completeClassName, name, descriptor);
    }

    /**
     * Creates a method from bytecode style names and a TypeSignature
     */
    // public static MethodName createFromByteCode(String completeClassName,
    // String name, MethodType signature) {
    // MethodName rval = new MethodName(completeClassName, name,
    // signature.getDescriptor());
    // rval.signature = signature;
    // return rval;
    // }

    /**
     * Creates a method name from informations obtained from a java byte code
     * method ref field.
     */
    public static MethodName createFromClassFile(ClassFile cls, MethodInfo method) throws BytecodeCompilerException {
        try {
            return new MethodName(cls.getThisClassName(), method.getName(), method.getDescriptor());
        } catch (InvalidByteCodeException e) {
            throw new BytecodeCompilerException("failed to create method name", e);
        }
    }

    /*
     * Creates a method name from a fully escaped program name
     */
    // public static MethodName createFromProgramName(String programName) {
    // Info info = EscapeProgram.revert(programName);
    //
    // String bcClassName, bcMethodName, bcDescriptor;
    // {
    // StringBuilder sb = new StringBuilder();
    // for(String s : info.pack)
    // sb.append(EscapeName.revert(s)).append("/");
    //
    // sb.append(EscapeName.revert(info.className));
    // bcClassName = sb.toString();
    // }
    // bcMethodName = EscapeName.revert(info.methodName);
    // bcDescriptor = EscapeType.signatureToDescriptor(info.signature);
    //
    // MethodName result = new MethodName(bcClassName, bcMethodName,
    // bcDescriptor);
    //
    // result.escapedPackage = info.pack;
    // result.className = info.className;
    // result.methodName = info.methodName;
    // result.programName = programName;
    //
    // return result;
    // }

    public String getJavaName() {
        assert false;
        return null;
    }

    public String getBytecodeClassName() {
        return bcClassName;
    }

    public String getBytecodeMethodName() {
        return bcMethodName;
    }

    public String getDescriptor() {
        return bcDescriptor;
    }

    /**
     * @return the name of the corresponding program
     * 
     * @throws BytecodeCompilerException
     *             if the program name could not be built
     */
    public String getProgramName(ClassFile targetClass) throws BytecodeCompilerException {
        if (null == programName) {
            StringBuilder sb = new StringBuilder();
            for (String s : getPackage())
                sb.append(s).append("_");

            sb.append(getClassName()).append("_").append(getMethodName());

            // TODO this might be more complicated for generics!
            if (getSignature(targetClass).getResultType().getBaseType().equals("ref"))
                sb.append("__").append(signature.getResultType().getIvilSimplifiedFunctionName());
            else
                sb.append("__").append(signature.getResultType().getJavaType());
            for (ObjectType t : signature.getArgumentTypes())
                if (t.getBaseType().equals("ref"))
                    sb.append("__").append(t.getIvilSimplifiedFunctionName());
                else
                    sb.append("__").append(t.getJavaType());

            programName = sb.toString();
        }
        return programName;
    }

    public String[] getPackage() {
        if (null == escapedPackage) {
            String[] pack = bcClassName.split("/");
            escapedPackage = new String[pack.length - 1];
            for (int i = 0; i < escapedPackage.length; i++)
                escapedPackage[i] = EscapeName.build(pack[i]);
        }
        return escapedPackage;
    }

    /**
     * @return the escaped class name
     */
    public String getClassName() {
        if (null == className) {
            String[] pack = bcClassName.split("/");
            className = EscapeName.build(pack[pack.length - 1]);
        }
        return className;
    }

    /**
     * @return the escaped method name
     */
    public String getMethodName() {
        if (null == methodName)
            methodName = EscapeName.build(bcMethodName);
        return methodName;
    }

    /**
     * @param resolver
     *            required to resolve generic type of the method; this allows
     *            for creation of method names without knowing the actual
     *            implementation or generic type
     * 
     * @return signature of the method using escaped java types
     * 
     * @throws BytecodeCompilerException
     *             if the signature could not be built
     */
    public MethodType getSignature(NameResolver resolver) throws BytecodeCompilerException {
        if (null == signature) {
            ClassFile cls = resolver.requestClass(bcClassName);
            MethodInfo m;
            try {
                m = cls.getMethod(bcMethodName, bcDescriptor);

                if (null != m.findAttribute(SignatureAttribute.class))
                    signature = MethodType.createFromSignature(cls.getConstantPoolUtf8Entry(
                            ((SignatureAttribute) m.findAttribute(SignatureAttribute.class)).getSignatureIndex())
                            .getString());
                else
                    signature = MethodType.createFromDescriptor(bcDescriptor);
            } catch (InvalidByteCodeException e) {
                throw new BytecodeCompilerException(e);
            }
        }
        return signature;
    }

    /**
     * @param targetClass
     *            required to resolve generic type of the method; this allows
     *            for creation of method names without knowing the actual
     *            implementation or generic type
     * 
     * @return signature of the method using escaped java types
     * 
     * @throws BytecodeCompilerException
     *             if the signature could not be built
     */
    public MethodType getSignature(ClassFile targetClass) throws BytecodeCompilerException {
        if (null == signature) {
            ClassFile cls = targetClass;
            MethodInfo m;
            try {
                m = cls.getMethod(bcMethodName, bcDescriptor);

                if (null != m.findAttribute(SignatureAttribute.class))
                    signature = MethodType.createFromSignature(cls.getConstantPoolUtf8Entry(
                            ((SignatureAttribute) m.findAttribute(SignatureAttribute.class)).getSignatureIndex())
                            .getString());
                else
                    signature = MethodType.createFromDescriptor(bcDescriptor);
            } catch (InvalidByteCodeException e) {
                throw new BytecodeCompilerException(e);
            }
        }
        return signature;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof MethodName) {
            MethodName m = (MethodName) o;
            return m.bcMethodName.equals(bcMethodName) && m.bcDescriptor.equals(m.bcDescriptor)
                    && m.bcClassName.equals(bcClassName);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return bcMethodName.hashCode() ^ bcClassName.hashCode() ^ bcDescriptor.hashCode();
    }

    @Override
    public String toString() {
        return bcClassName + "/" + bcMethodName + bcDescriptor;
    }

}