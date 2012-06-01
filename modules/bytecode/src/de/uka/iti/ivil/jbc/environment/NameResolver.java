package de.uka.iti.ivil.jbc.environment;

import java.io.File;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import org.gjt.jclasslib.io.ClassFileReader;
import org.gjt.jclasslib.structures.ClassFile;
import org.gjt.jclasslib.structures.FieldInfo;
import org.gjt.jclasslib.structures.InvalidByteCodeException;
import org.gjt.jclasslib.structures.MethodInfo;
import org.gjt.jclasslib.structures.attributes.SignatureAttribute;
import org.gjt.jclasslib.structures.constants.ConstantClassInfo;

import de.uka.iti.ivil.jbc.environment.cfg.MethodTranslator;
import de.uka.iti.ivil.jbc.util.ClassType;
import de.uka.iti.ivil.jbc.util.EscapeName;
import de.uka.iti.ivil.jbc.util.MethodName;
import de.uka.iti.ivil.jbc.util.ObjectType;

/**
 * This class is used to encapsulate name resolution, as defined in VMSPEC
 * §5.4.3. The resolution is bound to ConcreteProofObligations, as resolution
 * might trigger loading of other class files. The resolver tracks state of the
 * content string and its definitions to avoid duplicate translations.
 * 
 * @author timm.felden@felden.com
 * 
 */
public class NameResolver {

    private final static String RT_JAR_PATH = Environment.SYS_DIR + "/rt.jar";

    private final ClassHierarchy hierarchy = new ClassHierarchy();
    /**
     * Maps "pack/className" to class info, if present.
     */
    final Map<String, ClassFile> knownClasses = new HashMap<String, ClassFile>();
    /**
     * Maps class info and "name__signature" to method info, if present.
     */
    private final Map<ClassFile, Map<MethodName, MethodInfo>> knownMethods = new HashMap<ClassFile, Map<MethodName, MethodInfo>>();
    /**
     * Maps class info and "name" to the string representation of the term of
     * type <name('a)>, if present.
     */
    private final Map<ClassFile, Map<String, String>> knownFields = new HashMap<ClassFile, Map<String, String>>();
    private final ConcreteProofObligation<?> po;

    public NameResolver(ConcreteProofObligation<?> concreteProofObligation) {
        this.po = concreteProofObligation;
    }

    /**
     * ensures presence of field name and returns it as used in heap access
     * expressions
     * 
     * @param className
     *            name of the class with prefixed packages
     * @param fieldName
     *            name of the field
     * @return a String that can be directly used as second argument to heap
     *         expressions
     */
    public String resolveFieldName(String className, String fieldName) throws BytecodeCompilerException {
        ClassFile cls = requestClass(className);
        Map<String, String> tmp = knownFields.get(cls);
        return null == tmp ? null : tmp.get(fieldName);
    }

    /**
     * ensures presence of field name and returns it as used in heap access
     * expressions
     * 
     * @param cls
     *            class file containing the field definition
     * 
     * @param fieldName
     *            name of the field
     * 
     * @return a String that can be directly used as second argument to heap
     *         expressions
     */
    public String resolveFieldName(ClassFile cls, String fieldName) throws BytecodeCompilerException {
        if (!knownClasses.containsKey(cls))
            requestClass(cls.getThisClassName());

        Map<String, String> tmp = knownFields.get(cls);
        return null == tmp ? null : tmp.get(fieldName);
    }

    /**
     * This method translates an unescaped name, a descriptor and a class file
     * to a properly escaped program info WITHOUT adding a translation of the
     * program to the proof obligation.
     * 
     * The difference between simply constructing a method name yourself is a
     * check for existence of the described method.
     * 
     * @param cls
     *            the class that contains the target method, which has to be
     *            known to the resolver
     * 
     * @param unescapedName
     *            the unescaped name of the method as found in a class file
     * 
     * @param descriptor
     *            the descriptor of the method is used to distinguish between
     *            overloaded methods
     * 
     * @return the corresponding method name
     */
    public MethodName resolveMethodName(ClassFile cls, String unescapedName, String descriptor)
            throws BytecodeCompilerException {
        assert knownClasses.containsValue(cls) : "cls is unknown; it should have been requested before!";
        try {
            if (null == cls.getMethod(unescapedName, descriptor))
                throw new BytecodeCompilerException("the requested method " + unescapedName + descriptor
                        + " does not exist.");
        } catch (InvalidByteCodeException e) {
            throw new BytecodeCompilerException("the requested method " + unescapedName + descriptor
                    + " does not exist.", e);
        }

        return MethodName.createFromByteCode(cls.getThisClassName(), unescapedName, descriptor);
    }

    /**
     * ensures presence of the translation of the requested method
     * 
     * @param cls
     *            the surrounding class file
     * @param name
     *            the name of the method
     * @return an applicable method info structure
     */
    public MethodInfo requestMethod(ClassFile cls, MethodName name) throws BytecodeCompilerException {
        Map<MethodName, MethodInfo> entry = knownMethods.get(cls);

        if (entry.containsKey(name))
            return entry.get(name);

        MethodInfo method;
        String className = "";
        try {
            className = cls.getThisClassName();
            method = cls.getMethod(name.getBytecodeMethodName(), name.getDescriptor());
            if (null == method)
                throw new BytecodeCompilerException("Method " + name + " not found.");
        } catch (InvalidByteCodeException e) {
            throw new BytecodeCompilerException("Method " + name + " not found.", e);
        }

        String path = "";
        try {
            path = new File(((ClassProofObligation) po.parent).classPath + className).toURI().toURL().toString();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        MethodTranslator.translateProgram(cls, method, po, path);

        entry.put(name, method);

        return method;
    }

    /**
     * This is a lot like request method, except that it is looked for a method
     * with a matching signature and not a matching descriptor. The method is
     * however registered with its descriptor. To honor this, the correct method
     * name is returned. If a method with a mathching descriptor is found, the
     * returned method name will be exactly the argument method name and the
     * method will have been loaded.
     * 
     * @param cls
     *            the class to search in
     * 
     * @param genericMethodName
     *            the name and signature of the method(it does not matter, that
     *            type variables are not explicitly marked as such)
     * 
     * @return a method name which can be used to request a method
     *         successfully(which will have already been translated)
     */
    public MethodName requestGenericMethod(ClassFile cls, MethodName genericMethodName)
            throws BytecodeCompilerException {
        try {
            // check if there is a non generic method that mathches the method
            // name
            if (null != cls.getMethod(genericMethodName.getBytecodeMethodName(), genericMethodName.getDescriptor())) {
                requestMethod(cls, genericMethodName);
                return genericMethodName;
            }

            for (MethodInfo m : cls.getMethods()) {
                if (!m.getName().equals(genericMethodName.getMethodName()))
                    continue;

                SignatureAttribute sigAttr = (SignatureAttribute) m.findAttribute(SignatureAttribute.class);
                if (null == sigAttr)
                    continue;

                final String signature = cls.getConstantPoolUtf8Entry(sigAttr.getSignatureIndex()).getString();
                final String descriptor = genericMethodName.getDescriptor();

                if (signature.length() != descriptor.length())
                    continue;

                boolean success = true;
                for (int index = 0; index < signature.length(); index++) {
                    final char s = signature.charAt(index), d = descriptor.charAt(index);
                    // evil hack to implement type shadowing
                    if (s != d && !(s == 'T' && d == 'L')) {
                        success = false;
                        break;
                    }
                }
                if (success) {
                    MethodName rval = MethodName.createFromClassFile(cls, m);
                    requestMethod(cls, rval);
                    return rval;
                }
            }
        } catch (InvalidByteCodeException e) {
            throw new BytecodeCompilerException("generic method name lookup failed", e);
        }
        throw new BytecodeCompilerException("no generic method " + genericMethodName + " could be found.");
    }

    /**
     * ensures presence of desired class file
     * 
     * @param className
     *            class name as specified for JVMSPEC ref types (without L...; )
     * 
     * @throws BytecodeCompilerException
     *             in case of errors
     */
    public ClassFile requestClass(final String className) throws BytecodeCompilerException {
        if (knownClasses.containsKey(className))
            return knownClasses.get(className);

        // first search the project for a class
        ClassFile cls = null;
        try {
            // locate the file relative to package
            final String path = ((ClassProofObligation) po.parent).classPath + className + ".class";
            final File file = new File(path);
            if (file.exists())
                cls = ClassFileReader.readFromFile(file);
        } catch (Exception e) {
            throw new BytecodeCompilerException("failed to load class file from classpath: \"" + className + "\"", e);
        }

        // then search the rt.jar for a class
        if (null == cls) {
            // open rt.jar
            try {
                JarFile rt = new JarFile(RT_JAR_PATH, false);
                ZipEntry entry = rt.getEntry(className + ".class");
                if (null != entry)
                    cls = ClassFileReader.readFromInputStream(rt.getInputStream(entry));
            } catch (Exception e) {
                throw new BytecodeCompilerException("failed to load class file from rt.jar: \"" + className + "\"", e);
            }
        }

        // load failed, we did not find a class
        if (null == cls)
            throw new BytecodeCompilerException("could not locate class: \"" + className + "\"");

        knownClasses.put(className, cls);
        knownMethods.put(cls, new HashMap<MethodName, MethodInfo>());

        try {
            if (0 != cls.getSuperClass())
                requestClass(cls.getSuperClassName());
            for (int i : cls.getInterfaces()) {
                requestClass(((ConstantClassInfo) cls.getConstantPoolEntry(i, ConstantClassInfo.class)).getName());
            }
        } catch (InvalidByteCodeException e) {
            e.printStackTrace();
        }

        addType(cls);
        addFieldDeclarations(cls);
        addInvariant(cls);
        return cls;
    }

    /**
     * allows to request presence of base type array declarations; these are
     * treated as fake class to make them fit efficiently into the type
     * hierarchy.
     * 
     * @param type
     *            the base type of the array as descriptor, exactly two
     *            characters
     * 
     * @return a TypeName that corresponds to the array type, i.e. it has the
     *         base type ref and it is represented and axiomatised in the type
     *         hierarchy
     */
    public ClassType requestBaseTypeArray(String type) {

        ClassType rval = ClassType.createFakeType(type);

        if (!knownClasses.containsKey(type)) {
            hierarchy.addBaseTypeArray(rval, po);
            knownClasses.put(type, null);
        }
        return rval;
    }

    /**
     * turns a signature into a single type. can be used for local variables or
     * fields. ensures presence of involved classes.
     */
    public String resolveTypeNameFromSignature(String signature) throws BytecodeCompilerException {
        throw new BytecodeCompilerException("not yet implemented, sorry!");
    }

    /**
     * retrieves the type name of cls and all type names of super classes and
     * interfaces from the type hierarchy.
     */
    public Set<ClassType> getAllTypeNamesFromHierarchy(ClassFile cls) {
        return hierarchy.getAllImplementedTypes(ClassType.createTypeFromBytecodeClass(cls));
    }

    /**
     * adds type constants and constraints for cls.
     */
    private void addType(ClassFile cls) throws BytecodeCompilerException {
        try {
            // update type hierarchy and add subtype axioms if needed
            if (0 == cls.getSuperClass()) {
                hierarchy.addRoot(cls, po);
            } else {
                hierarchy.addClass(cls, 0, po);
            }
        } catch (InvalidByteCodeException e) {
            throw new BytecodeCompilerException("class type declaration failed", e);
        }
    }

    /**
     * adds field declarations to class types.
     */
    private void addFieldDeclarations(ClassFile cls) throws BytecodeCompilerException {
        HashMap<String, String> entries = new HashMap<String, String>();
        knownFields.put(cls, entries);
        StringBuilder axioms = new StringBuilder();

        try {
            String prefix = "F_" + EscapeName.build(cls.getThisClassName()) + "_";
            for (FieldInfo f : cls.getFields()) {
                final String name = EscapeName.build(f.getName());
                final String field = prefix + name;
                final String type = ObjectType.createTypeFromSingleTypeDescriptor(f.getDescriptor()).getBaseType();
                entries.put(f.getName(), field);

                po.requestFunction(field, "field", "unique");

                // add static type constraint for a ref typed field
                if (type.equals("ref")) {
                    // TODO depends on bound types:)

                    // String desc = f.getDescriptor();
                    // TODO the axioms are a) for wellformed heaps and b)
                    // dependand on the type of r
                    // TODO reimplement
                    // if ('[' == desc.charAt(0)) {
                    // // ∀ h,r:: typeof(h[r,field],
                    // // [[refType]] )
                    // int dim = 0;
                    // while ('[' == desc.charAt(dim))
                    // dim++;
                    //
                    // desc = desc.substring(dim);
                    //
                    // if (desc.startsWith("L")) {
                    // axioms.append("axiom field_ref_type_for_").append(field)
                    // .append(" (\\forall h; (\\forall o; $wellformed(h) -> superType(h[o, ")
                    // .append(field).append("], ");
                    //
                    // for (int i = 0; i < dim; i++)
                    // axioms.append("TF_array(");
                    //
                    // axioms.append(po.resolver.getTypeNameFromHierarchy(desc.substring(1,
                    // desc.length() - 1)));
                    //
                    // for (int i = 0; i < dim; i++)
                    // axioms.append(")");
                    //
                    // axioms.append(")))\n");
                    // }
                    // // there is no else here; if its not a ref type array,
                    // // no axiom is needed
                    // } else {
                    // SignatureAttribute sig = (SignatureAttribute)
                    // f.findAttribute(SignatureAttribute.class);
                    // if (null != sig && sig.getSignatureIndex() == -1) {
                    // // ∀ h,o :: $well(h) -> typeof(h[o,F],T)
                    // axioms.append("axiom field_ref_type_for_").append(field)
                    // .append(" (\\forall h; (\\forall o; $wellformed(h) -> instanceof(h[o, ")
                    // .append(field).append("], ")
                    // .append(po.resolver.getTypeNameFromHierarchy(desc.substring(1,
                    // desc.length() - 1)))
                    // .append(")))\n");
                    // } else {
                    // // ∀ h,r, T:: $well(h) & typeof(r, C(T)) ->
                    // // typeof(h[r,field],
                    // // [[descType]] )
                    //
                    // //
                    // axioms.append("axiom field_ref_type_for_").append(field)
                    // //
                    // .append(" (\\forall h as heap; (\\forall r; typeof(h[r, ").append(field)
                    // // .append("], ")
                    // //
                    // .append(po.resolver.getTypeNameFromHierarchy(desc.substring(1,
                    // // desc.length() - 1)))
                    // // .append(")))\n");
                    //
                    // }
                    // }
                }
            }
        } catch (InvalidByteCodeException e) {
            throw new BytecodeCompilerException("failed to add fields", e);
        }
        po.content.append(axioms).append("\n");
    }

    /**
     * Adds the invariants of a class to the PO.
     */
    private void addInvariant(ClassFile cls) throws BytecodeCompilerException {

        final ClassType typename = ClassType.createTypeFromBytecodeClass(cls);
        final String invariant = po.getContractResolver().getInvariant(po, typename);

        // create generic arguments
        final ArrayList<String> inst = new ArrayList<String>(typename.getIvilTypeVaribaleNames().size());
        for (String tv : typename.getIvilTypeVaribaleNames())
            inst.add("%" + tv);

        po.content.append("rule type_invariant_").append(typename.getIvilSimplifiedFunctionName())
                .append("\n  find $invariant(%h, %o, ").append(typename.getIvilCustomTypeTerm(inst)).append(")\n");
        po.content.append("  replace ").append(invariant).append("\n");
        po.content.append("  tags rewrite \"fol simp\"\n");
    }
}
