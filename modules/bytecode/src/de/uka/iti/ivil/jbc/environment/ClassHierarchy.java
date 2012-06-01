package de.uka.iti.ivil.jbc.environment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import org.gjt.jclasslib.structures.AccessFlags;
import org.gjt.jclasslib.structures.ClassFile;
import org.gjt.jclasslib.structures.InvalidByteCodeException;
import org.gjt.jclasslib.structures.constants.ConstantClassInfo;

import de.uka.iti.ivil.jbc.util.ClassType;

/**
 * This class stores the loaded class hierarchy for a single proof obligation.
 * Its task is to automatically create axioms about types.
 * 
 * @author timm.felden@felden.com
 * 
 */
final class ClassHierarchy {
    /**
     * This class is used to store subclassing information which is needed to
     * create typeof(...) axioms, which state inequality of types which can
     * never be equal.
     * 
     * @author timm.felden@felden.com
     * 
     */
    private static final class TypeHierarchyEntry {

        public final boolean isInterface;
        public final ClassType name;

        // ! null iff root
        public final TypeHierarchyEntry superClass;
        public final LinkedList<TypeHierarchyEntry> superInterfaces = new LinkedList<ClassHierarchy.TypeHierarchyEntry>();
        public final LinkedList<TypeHierarchyEntry> children = new LinkedList<TypeHierarchyEntry>();

        public TypeHierarchyEntry(ClassFile cls, TypeHierarchyEntry superClass) throws InvalidByteCodeException {
            this.isInterface = (cls.getAccessFlags() & AccessFlags.ACC_INTERFACE) != 0;
            this.name = ClassType.createTypeFromBytecodeClass(cls);

            // link with super class
            this.superClass = superClass;
            if (null != superClass)
                superClass.children.add(this);
        }

        /**
         * this constructor is only used to create base type array fake classes
         */
        public TypeHierarchyEntry(ClassType name, TypeHierarchyEntry superClass) {
            this.isInterface = false;
            this.name = name;
            this.superClass = superClass;
        }
    }

    /**
     * The type hierarchy is organised as a DAG of class files, because of
     * interfaces.
     */
    // root can not be final, because entries are final :-(
    private TypeHierarchyEntry root = null;
    private final HashMap<ClassType, TypeHierarchyEntry> nameMap = new HashMap<ClassType, ClassHierarchy.TypeHierarchyEntry>();

    public void addRoot(ClassFile cls, ConcreteProofObligation<?> po) throws BytecodeCompilerException {
        try {
            assert 0 != cls.getSuperClass() : "only java/lang/Object can be the root of the type hierarchy!";
            assert cls.getThisClassName().equals("java/lang/Object");
            root = new TypeHierarchyEntry(cls, null);
        } catch (InvalidByteCodeException e) {
            e.printStackTrace();
            // should not happen and it would be annoying to pass the error
            // anywhere
        }

        po.requestFunction(root.name.getIvilFunctionName(), "type", "unique");
        nameMap.put(root.name, root);

        // axioms for Objects are done in the preamble
    }

    public void addBaseTypeArray(ClassType name, ConcreteProofObligation<?> po) {
        TypeHierarchyEntry entry = new TypeHierarchyEntry(name, root);
        nameMap.put(name, entry);

        createSubclassingAxioms(po, entry, name);
    }

    /**
     * ensures, that super classes and interfaces have already been added to the
     * hierarchy. ensures, that the class is added to the hierarchy and missing
     * subclass axioms are created.
     * 
     * Requires, that cls has indeed a parent.
     * 
     * @throws BytecodeCompilerException
     *             if a super class or interface could not be found
     */
    public void addClass(ClassFile cls, int typeArgumentCount, ConcreteProofObligation<?> po)
            throws InvalidByteCodeException, BytecodeCompilerException {

        // create a suitable entry
        TypeHierarchyEntry entry;
        {
            ClassType name = ClassType.createTypeFromBytecodeClass(po.resolver.requestClass(cls.getSuperClassName()));
            TypeHierarchyEntry superClass = nameMap.get(name);
            entry = new TypeHierarchyEntry(cls, superClass);
        }
        // super interfaces
        for (int index : cls.getInterfaces()) {
            ClassType name = ClassType.createTypeFromBytecodeClass(po.resolver.requestClass(((ConstantClassInfo) cls
                    .getConstantPoolEntry(index, ConstantClassInfo.class)).getName()));
            TypeHierarchyEntry se = nameMap.get(name);
            se.children.add(entry);
            entry.superInterfaces.add(se);
        }

        ClassType name = entry.name;

        // store the entry
        po.requestFunction(name.getIvilFunctionName(), "type", "unique");
        nameMap.put(name, entry);

        createSubclassingAxioms(po, entry, name);
    }

    private void createSubclassingAxioms(ConcreteProofObligation<?> po, TypeHierarchyEntry entry, ClassType name) {

        final StringBuilder content = po.content;

        // get generic type name
        final ArrayList<String> typeVariableNames = name.getIvilTypeVaribaleNames();
        String typeName = name.getIvilQuantifiedTypeTerm();

        // if there are generic type arguments, create wildcard subtypeing
        // rules
        // fun fact: this is a slight variation of ackermanns lemma (∃phi)
        for (String tv : typeVariableNames) {
            boolean first;

            // ∀o,t. instanceof(o, G(\wildcard(phi))) & phi(t) ->
            // instanceof(o, G(t))
            content.append("rule type_").append(name.getIvilSimplifiedFunctionName())
                    .append("_concrete_wildcard_subtypeing\n");

            content.append("  find instanceof(%o, ").append(name.getIvilSimplifiedFunctionName()).append("(");
            first = true;
            for (String arg : typeVariableNames) {
                if (!first)
                    content.append(", ");
                else
                    first = false;

                if (tv == arg) {
                    content.append("(\\wildcard %wc; %phi)");
                } else {
                    content.append("%").append(arg);
                }
            }
            content.append(")) |-\n");

            content.append("  assume |- instanceof(%o, ").append(name.getIvilSimplifiedFunctionName()).append("(");
            first = true;
            for (String arg : typeVariableNames) {
                if (!first)
                    content.append(", ");
                else
                    first = false;

                content.append("%").append(arg);
            }
            content.append("))\n");

            content.append("  where not presentInSuccedent $$subst(%wc, %").append(tv).append(", %phi)\n");

            content.append("  add |- $$subst(%wc, %").append(tv).append(", %phi)\n");
            // priorities are required because the first rule would match in any
            // sequent that matches the second rule as well, but the first rule
            // would actually lead to a dead end
            content.append("  tags prio \"100\"\n\n");

            // ∀o,t. instanceof(o, G(\wildcard(phi))) & (phi -> psi) ->
            // instanceof(o, G(\wildcard(psi)))

            content.append("rule type_").append(name.getIvilSimplifiedFunctionName())
                    .append("_wildcard_wildcard_subtypeing\n");

            content.append("  find instanceof(%o, ").append(name.getIvilSimplifiedFunctionName()).append("(");
            first = true;
            for (String arg : typeVariableNames) {
                if (!first)
                    content.append(", ");
                else
                    first = false;

                if (tv == arg) {
                    content.append("(\\wildcard %wc; %phi)");
                } else {
                    content.append("%").append(arg);
                }
            }
            content.append(")) |-\n");

            content.append("  assume |- instanceof(%o, ").append(name.getIvilSimplifiedFunctionName()).append("(");
            first = true;
            for (String arg : typeVariableNames) {
                if (!first)
                    content.append(", ");
                else
                    first = false;

                if (tv == arg) {
                    content.append("(\\wildcard %wc2; %psi)");
                } else {
                    content.append("%").append(arg);
                }
            }
            content.append("))\n");
            
            content.append("  where not presentInSuccedent (\\forall %wc; %phi -> $$subst(%wc2, %wc, %psi))\n");
            content.append("  add |- (\\forall %wc; %phi -> $$subst(%wc2, %wc, %psi))\n");

            content.append("  tags prio \"101\"\n\n");
        }

        // type is a subtype of super class
        {
            final ClassType superName = entry.superClass.name;

            content.append("axiom type__").append(name.getIvilSimplifiedFunctionName()).append("__extends__")
                    .append(superName.getIvilSimplifiedFunctionName()).append(" ");

            // quantify type variables
            for (String s : typeVariableNames)
                content.append("(\\forall ").append(s).append("; ");
            for (String s : superName.getIvilTypeVaribaleNames())
                content.append("(\\forall ").append(s).append("; ");

            content.append("superType(").append(typeName).append(", ").append(superName.getIvilQuantifiedTypeTerm())
                    .append(")");

            for (int i = typeVariableNames.size() + superName.getIvilTypeVaribaleNames().size(); i > 0; i--)
                content.append(")");

            content.append("\n\n");
        }

        // type is a subtype of super interfaces
        for (TypeHierarchyEntry si : entry.superInterfaces) {
            final ClassType superName = si.name;

            content.append("axiom type__").append(name.getIvilSimplifiedFunctionName()).append("__implements__")
                    .append(superName.getIvilSimplifiedFunctionName()).append(" ");

            // quantify type variables
            for (String s : typeVariableNames)
                content.append("(\\forall ").append(s).append("; ");
            for (String s : superName.getIvilTypeVaribaleNames())
                content.append("(\\forall ").append(s).append("; ");

            content.append("superType(").append(typeName).append(", ").append(superName.getIvilQuantifiedTypeTerm())
                    .append(")");

            for (int i = typeVariableNames.size() + superName.getIvilTypeVaribaleNames().size(); i > 0; i--)
                content.append(")");

            content.append("\n\n");
        }

        // type is not equal to other subclasses of the superclass
        if (!entry.isInterface) {
            for (TypeHierarchyEntry other : entry.superClass.children) {
                if (entry == other || other.isInterface)
                    continue;

                final ClassType otherName = other.name;

                content.append("axiom type__").append(name.getIvilSimplifiedFunctionName()).append("__extends_not__")
                        .append(otherName.getIvilSimplifiedFunctionName()).append(" ");

                // quantify type variables
                for (String s : typeVariableNames)
                    content.append("(\\forall ").append(s).append("; ");
                for (String s : otherName.getIvilTypeVaribaleNames())
                    content.append("(\\forall ").append(s).append("; ");

                content.append("(\\forall o; o=$null | !(instanceof(o, ").append(typeName).append(") & instanceof(o, ")
                        .append(otherName.getIvilQuantifiedTypeTerm()).append(")))");

                for (int i = typeVariableNames.size() + otherName.getIvilTypeVaribaleNames().size(); i > 0; i--)
                    content.append(")");

                content.append("\n\n");
            }
        }
    }

    public final Set<ClassType> getAllImplementedTypes(ClassType type) {
        Set<ClassType> rval = new HashSet<ClassType>();

        rval.add(type);
        TypeHierarchyEntry entry = nameMap.get(type);
        if (null != entry.superClass) {
            rval.addAll(getAllImplementedTypes(entry.superClass.name));

            for (TypeHierarchyEntry e : entry.superInterfaces)
                rval.addAll(getAllImplementedTypes(e.name));
        }
        return rval;
    }
}
