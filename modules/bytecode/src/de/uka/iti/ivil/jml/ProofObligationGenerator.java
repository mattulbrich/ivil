package de.uka.iti.ivil.jml;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.gjt.jclasslib.structures.AccessFlags;
import org.gjt.jclasslib.structures.ClassFile;
import org.gjt.jclasslib.structures.InvalidByteCodeException;
import org.gjt.jclasslib.structures.MethodInfo;
import org.gjt.jclasslib.structures.attributes.CodeAttribute;
import org.gjt.jclasslib.structures.attributes.LocalVariableCommonEntry;
import org.gjt.jclasslib.structures.attributes.LocalVariableTableAttribute;

import de.uka.iti.ivil.jbc.environment.BytecodeCompilerException;
import de.uka.iti.ivil.jbc.environment.ClassProofObligation;
import de.uka.iti.ivil.jbc.environment.ConcreteProofObligation;
import de.uka.iti.ivil.jbc.environment.ContractResolver;
import de.uka.iti.ivil.jbc.environment.Environment;
import de.uka.iti.ivil.jbc.environment.ProofObligation;
import de.uka.iti.ivil.jbc.environment.ProofObligation.State;
import de.uka.iti.ivil.jbc.util.ClassType;
import de.uka.iti.ivil.jbc.util.EscapeName;
import de.uka.iti.ivil.jbc.util.MethodName;
import de.uka.iti.ivil.jbc.util.MethodType;
import de.uka.iti.ivil.jbc.util.ObjectType;
import de.uka.iti.ivil.jml.expression.Translator;
import de.uka.iti.ivil.jml.parser.JMLParser;
import de.uka.iti.ivil.jml.parser.ast.CompilationUnit;
import de.uka.iti.ivil.jml.parser.ast.PackageDeclaration;
import de.uka.iti.ivil.jml.parser.ast.body.ClassOrInterfaceDeclaration;
import de.uka.iti.ivil.jml.parser.ast.body.ConstructorDeclaration;
import de.uka.iti.ivil.jml.parser.ast.body.InvariantDeclaration;
import de.uka.iti.ivil.jml.parser.ast.body.MethodDeclaration;
import de.uka.iti.ivil.jml.parser.ast.body.Parameter;
import de.uka.iti.ivil.jml.parser.ast.expr.NameExpr;
import de.uka.iti.ivil.jml.parser.ast.expr.QualifiedNameExpr;
import de.uka.iti.ivil.jml.parser.ast.spec.JMLStatement;
import de.uka.iti.ivil.jml.parser.ast.spec.MethodContract;
import de.uka.iti.ivil.jml.parser.ast.spec.MethodContract.Line;
import de.uka.iti.ivil.jml.parser.ast.spec.MethodContract.LineType;
import de.uka.iti.ivil.jml.parser.ast.spec.StoreRefExpression;
import de.uka.iti.ivil.jml.parser.ast.visitor.DumpVisitor;
import de.uka.iti.ivil.jml.parser.ast.visitor.VoidVisitorAdapter;

/**
 * Built in generator which handles .jml files. The generator object is used to
 * keep state of the load thread.
 *
 * @author timm.felden@felden.com
 */
public class ProofObligationGenerator extends de.uka.iti.ivil.jbc.environment.ProofObligationGenerator implements
        Runnable, ContractResolver {

    final private List<ConcreteProofObligation<MethodName>> poList = new LinkedList<ConcreteProofObligation<MethodName>>();
    private CompilationUnit ast;
    private final Map<ConcreteProofObligation<MethodName>, MethodContract> contractMap = new HashMap<ConcreteProofObligation<MethodName>, MethodContract>();
    private ConcreteProofObligation<MethodName> currentPO;

    private final class POCollector extends VoidVisitorAdapter<Void> {
        private String pack = "";
        private ProofObligation parent;

        private POCollector(ProofObligation parent) {
            this.parent = parent;
        }

        @Override
        public void visit(PackageDeclaration node, Void arg) {
            NameExpr name = node.getName();
            while (name instanceof QualifiedNameExpr) {
                pack = name.getName() + "/" + pack;
                name = ((QualifiedNameExpr) name).getQualifier();
            }
            pack = name.getName() + "/" + pack;
        }

        @Override
        public void visit(ClassOrInterfaceDeclaration node, Void arg) {

            // we don't create proof obligations for interfaces
            if (node.isInterface()) {
                return;
            }

            String classPath = parent.getName();

            File location = new File(classPath);
            if (!location.isDirectory()) {
                // we have a file parent, so we have to chop the file name of
                classPath = classPath.substring(0, classPath.lastIndexOf('/'));
            }
            if (!"".equals(pack)) {
                // we need to remove the final "/"
                String pack = this.pack;
                if (pack.endsWith("/")) {
                    pack = pack.substring(0, pack.length() - 1);
                }

                assert -1 != classPath.lastIndexOf(pack) : "the class's location does not correspond to its package";
                // we are inside a package so chop the package name of
                try {
                    classPath = classPath.substring(0, classPath.lastIndexOf(pack));
                } catch (StringIndexOutOfBoundsException e) {
                    throw new IllegalArgumentException("The current file is located outside of its package: path="
                            + classPath + ", package=" + pack);
                }
            }

            parent = new ClassProofObligation(parent, node.getName(), pack, classPath + "/");

            super.visit(node, arg);

            parent = parent.parent;
        }

        /**
         * Create proof obligations. The target method is encoded using the POs
         * data field.
         */
        @Override
        public void visit(ConstructorDeclaration node, Void arg) {

            // create PO name
            ConcreteProofObligation<MethodName> po;

            MethodType sig;
            {
                List<ObjectType> args = new ArrayList<ObjectType>(node.getParameters().size());
                for (Parameter p : node.getParameters()) {
                    args.add(ObjectType.createTypeFromJavaTypeName(p.getType().toString()));
                }

                // can constructors even bind types?
                sig = MethodType.createFromObjectTypes("", args, ObjectType.createTypeFromSingleTypeDescriptor("V"));
            }

            // create method name
            final String className = ((ClassProofObligation) parent).pack + parent.getName();
            final MethodName methodName = MethodName.createFromByteCode(className, "<init>", sig.getJVMType());

            // TODO create real contracts
            if (node.hasMethodSpec()) {
                int i = 0;
                for (MethodContract c : node.getMethodSpec().getContracts()) {
                    parent.addChild(po = new ConcreteProofObligation<MethodName>(parent, methodName
                            + " - ensureMethodContract#" + ++i, ProofObligationGenerator.this));
                    poList.add(po);
                    po.setData(methodName);
                    contractMap.put(po, c);

                    if (c.get(LineType.diverges).size() > 0) {
                        parent.addChild(po = new ConcreteProofObligation<MethodName>(parent, methodName
                                + " - ensureMethodContract#" + ++i + "partial", ProofObligationGenerator.this));
                        poList.add(po);
                        po.setData(methodName);
                        contractMap.put(po, c);
                    }
                }
            }
        }

        /**
         * Create proof obligations. The target method is encoded using the POs
         * data field.
         */
        @Override
        public void visit(MethodDeclaration node, Void arg) {

            // create PO name
            ConcreteProofObligation<MethodName> po;

            MethodType sig;
            {
                List<ObjectType> args = new ArrayList<ObjectType>(node.getParameters().size());
                for (Parameter p : node.getParameters()) {
                    args.add(ObjectType.createTypeFromJavaTypeName(p.getType().toString()));
                }

                // note: this is invalid for generic methods
                sig = MethodType.createFromObjectTypes("", args,
                        ObjectType.createTypeFromJavaTypeName(node.getType().toString()));
            }

            // create method name
            final String className = ((ClassProofObligation) parent).pack + parent.getName();
            // NOTE: only correct for non generic case
            final MethodName methodName = MethodName.createFromByteCode(className, node.getName(), sig.getJVMType());

            // TODO create real contracts
            if (node.hasMethodSpec()) {
                int i = 0;
                for (MethodContract c : node.getMethodSpec().getContracts()) {
                    parent.addChild(po = new ConcreteProofObligation<MethodName>(parent, methodName
                            + " - ensureMethodContract#" + ++i, ProofObligationGenerator.this));
                    poList.add(po);
                    po.setData(methodName);
                    contractMap.put(po, c);

                    if (c.get(LineType.diverges).size() > 0) {
                        parent.addChild(po = new ConcreteProofObligation<MethodName>(parent, methodName
                                + " - ensureMethodContract#" + ++i + "partial", ProofObligationGenerator.this));
                        poList.add(po);
                        po.setData(methodName);
                        contractMap.put(po, c);
                    }
                }
            }
        }
    }

    @Override
    public void load(Environment env, String path, ProofObligation parent) throws BytecodeCompilerException {

        // get ast
        try {
            ast = JMLParser.parse(new FileInputStream(path));
        } catch (Exception e) {
            parent.setState(State.loadFailed);
            throw new BytecodeCompilerException(path + " is not a valid jml file!", e);
        }

        // collect POs from the ast
        try {
            (new POCollector(parent)).visit(ast, null);
        } catch (Exception e) {
            parent.setState(State.loadFailed);
            e.printStackTrace();
            return;
        }

        // start the actual load process
        new Thread(this).run();
    }

    /**
     * The actual load process.
     */
    @Override
    public void run() {
        while (!poList.isEmpty()) {
            currentPO = poList.remove(0);
            try {
                final ClassProofObligation parent = (ClassProofObligation) currentPO.parent;

                ClassFile cls = currentPO.resolver.requestClass(parent.pack + parent.getName());

                // create problem

                MethodName name = currentPO.getData();
                name = currentPO.resolver.requestGenericMethod(cls, name);
                MethodInfo method = currentPO.resolver.requestMethod(cls, name);

                makeProblem(currentPO, name, cls, method, currentPO.getName().endsWith("partial"));

                // finished loading
                currentPO.setState(State.waitingForProof);

            } catch (Exception e) {
                // TODO remove stack trace if tool is stable
                e.printStackTrace();
                currentPO.setError(e);
                currentPO.setState(State.loadFailed);
                continue;
            }
        }
    }

    /**
     * This function is the source of all problems.
     *
     * @param p
     *            the proof obligation associated with the problem
     */
    private void makeProblem(ConcreteProofObligation<MethodName> po, MethodName methodName, ClassFile cls,
            MethodInfo method, boolean diverges) throws BytecodeCompilerException {
        StringBuilder sb = new StringBuilder();

        // request $SPEC_ functions
        po.requestFunction("$SPEC_diverges", "bool", "assignable");
        po.requestFunction("$SPEC_frame", "set(prod(ref, field))", null);

        StringBuilder lvarBackup = new StringBuilder();

        // collect local variable names and backups
        String[] localVariableTranslatedNames, localVariableNames;
        boolean isStatic = 0 != (method.getAccessFlags() & AccessFlags.ACC_STATIC);
        {
            CodeAttribute code = ((CodeAttribute) method.findAttribute(CodeAttribute.class));
            if (null == code) {
                throw new BytecodeCompilerException(
                        "the target method is abstract and can therefore not be proven to be correct");
            }

            localVariableTranslatedNames = new String[code.getMaxLocals()];
            localVariableNames = new String[localVariableTranslatedNames.length];
            LocalVariableTableAttribute lvars = (LocalVariableTableAttribute) code
                    .findAttribute(LocalVariableTableAttribute.class);

            // TODO take static into account
            String types[] = new String[localVariableTranslatedNames.length];
            {
                MethodType sig = methodName.getSignature(po.resolver);
                int index;
                if (!isStatic) {
                    types[0] = "ref";
                    index = 1;
                } else {
                    index = 0;
                }

                for (ObjectType t : sig.getArgumentTypes()) {
                    types[index] = t.getBaseType();
                    if (t.isCategory2Type()) {
                        index += 2;
                    } else {
                        index++;
                    }
                }
            }

            // create names from local variable table entries
            if (null != lvars) {
                for (LocalVariableCommonEntry entry : lvars.getLocalVariableEntries()) {
                    // skip indices which dont match the current handler
                    if (0 == entry.getStartPc()) {
                        try {
                            localVariableNames[entry.getIndex()] = cls.getConstantPoolEntryName(entry.getNameIndex());
                            localVariableTranslatedNames[entry.getIndex()] = "R_"
                                    + EscapeName.build(localVariableNames[entry.getIndex()]) + "_"
                                    + types[entry.getIndex()];
                        } catch (InvalidByteCodeException e) {
                            throw new BytecodeCompilerException("failed to create register name", e);
                        }
                    }
                }
            }

            // create names for yet unassigned names
            for (int i = 0; i < localVariableTranslatedNames.length; i++) {
                // skip names, which are never used anywhere
                if (null == types[i]) {
                    continue;
                }

                if (null == localVariableTranslatedNames[i]) {
                    localVariableTranslatedNames[i] = "R_" + i + "_" + types[i];
                }

                // create function names
                po.requestFunction(localVariableTranslatedNames[i], types[i], "assignable");
            }

            // create backups
            for (int i = 0; i < localVariableTranslatedNames.length; i++) {
                final String s = localVariableTranslatedNames[i];
                if (null == s) {
                    continue;
                }

                if (i > 0) {
                    lvarBackup.append(" || ");
                }

                // <name> := SPEC_pre_<name>
                lvarBackup.append(s).append(" := SPEC_pre_").append(s);
                // <name> = SPEC_pre_name &
                sb.append(s).append(" = SPEC_pre_").append(s).append(" & ");

                // rename and create translated names
                localVariableTranslatedNames[i] = "SPEC_pre_" + s;
                po.requestFunction(localVariableTranslatedNames[i], s.substring(s.lastIndexOf('_') + 1), "assignable");
            }
        }

        Translator trans = new Translator(cls, method, po, localVariableNames, localVariableTranslatedNames, "$result",
                "R_this_ref");

        // the basic contract is:
        // lvarbackup, $SPEC_frame = assignable, $inv, $SPEC_diverges =
        // diverges, requires -> {lvar assign}[0;fun](ensures & $inv & (∀o,f.
        // <o,f> :: $SPEC_frame | !$old_heap[o, $created] | $old_heap[o,f] =
        // $heap[o,f]))

        MethodContract c = contractMap.get(po);

        // if (c.get(LineType.assignable).size() > 0) {
        sb.append(FrameBuilder.makeFrame("$SPEC_frame", "R_this_ref", c, po.resolver, trans, cls)).append(" & ");
        // }

        // invariants
        final String defaultInvariantTerm;
        {
            Set<ClassType> types = po.resolver.getAllTypeNamesFromHierarchy(cls);

            StringBuilder term = new StringBuilder();
            for (ClassType t : types) {

                // TODO hier muss mit TA instantiiert werden und NICHT mit TV:-(
                term.append("$invariant($heap, ").append(isStatic ? "$static" : "R_this_ref").append(", ")
                        .append(t.toObjectType().getIvilTypeTerm()).append(") & ");
            }
            defaultInvariantTerm = term.toString();
        }

        // assume invariant, if the method is not a constructor
        if (!methodName.getBytecodeMethodName().equals("<init>")) {
            sb.append(defaultInvariantTerm);
        }

        if (c.get(LineType.diverges).size() > 0) {
            sb.append(" $SPEC_diverges = (");
            for (int j = 0; j < c.get(LineType.diverges).size(); j++) {
                Line line = c.get(LineType.diverges).get(j);
                if (0 != j) {
                    sb.append(" & ");
                }
                sb.append(trans.translate(line.expr));
            }
            sb.append(") & ");
        }

        final List<Line> requires = c.get(LineType.requires);
        if (0 == requires.size()) {
            sb.append("true");
        } else {
            for (int i = 0; i < requires.size(); i++) {
                if (i > 0) {
                    sb.append(" & ");
                }
                sb.append(trans.translate(requires.get(i).expr));
            }
        }

        if (diverges) {
            sb.append(" & $SPEC_diverges -> ");
        } else {
            sb.append(" -> $SPEC_diverges | ");
        }

        if (lvarBackup.length() > 0) {
            sb.append("{ ").append(lvarBackup).append("}");
        }

        if (diverges) {
            sb.append("[0;").append(methodName.getProgramName(cls)).append("]");
        } else {
            sb.append("[[0;").append(methodName.getProgramName(cls)).append("]]");
        }

        final List<Line> ensures = c.get(LineType.ensures);
        sb.append("(");
        if (0 != ensures.size()) {
            // ensures
            for (Line line : ensures) {
                sb.append(trans.translate(line.expr)).append(" & ");
            }
        }

        // prove invariant
        sb.append(defaultInvariantTerm);

        // frame condition
        sb.append("( \\forall o as ref; ( \\forall f as field; pair(o, f) :: $SPEC_frame | !$old_heap[o, $created] | ($heap[o,f] = $old_heap[o,f] as int & ($heap[o,f] = $old_heap[o,f] as bool) & $heap[o,f] = $old_heap[o,f] as ref & $heap[o,f] = $old_heap[o,f] as float))))");

        po.problem = sb.toString();
    }

    /**
     * retrieves a method contract by looking at "classpath/pack/class.jml"
     */
    @Override
    public String[] getCallContracts(ConcreteProofObligation<?> currentPO, final MethodName callee,
            final String[] argNames, final String resultName) throws BytecodeCompilerException {
        assert null != currentPO : "this method works only, if there is a proof obligation under translation by this generator";

        String jmlPath = ((ClassProofObligation) currentPO.parent).classPath + callee.getBytecodeClassName() + ".jml";
        if (!new File(jmlPath).exists()) {
            // if no contract was supplied by the problem, look at the default
            // contracts
            jmlPath = Environment.SYS_DIR + "/jmlContracts/" + callee.getBytecodeClassName() + ".jml";

            if (!new File(jmlPath).exists()) {
                throw new BytecodeCompilerException("there is no jml file for " + callee.getBytecodeClassName());
            }
        }

        CompilationUnit jml;
        try {
            jml = JMLParser.parse(new FileInputStream(jmlPath));
        } catch (Exception e) {
            throw new BytecodeCompilerException("failed to parse " + jmlPath, e);
        }

        final List<MethodContract> contracts = new ArrayList<MethodContract>();

        // we have to create local variable names from the AST to match the
        // definition in the specification file
        final String[] localVariableNames = argNames.clone();

        (new VoidVisitorAdapter<Void>() {
            @Override
            public void visit(ClassOrInterfaceDeclaration node, Void arg) {
                assert node.getName().equals(callee.getClassName()) : "the jml file contains illegal type definitions!";
                super.visit(node, arg);
            }

            @Override
            public void visit(ConstructorDeclaration node, Void arg) {
                if (!"Z003cinitZ003e".equals(callee.getMethodName())) {
                    return;
                }

                for(int i = 0; i < localVariableNames.length; i++) {
                    localVariableNames[i] = node.getParameters().get(i).getId().getName();
                }

                if (node.hasMethodSpec()) {
                    contracts.addAll(node.getMethodSpec().getContracts());
                }
            }

            @Override
            public void visit(MethodDeclaration node, Void arg) {
                if (!EscapeName.build(node.getName()).equals(callee.getMethodName())) {
                    return;
                }

                for (int i = 0; i < localVariableNames.length; i++) {
                    localVariableNames[i] = node.getParameters().get(i).getId().getName();
                }

                if (node.hasMethodSpec()) {
                    contracts.addAll(node.getMethodSpec().getContracts());
                }
            }
        }).visit(jml, null);

        Translator trans;
        {
            ClassFile cls = currentPO.resolver.requestClass(callee.getBytecodeClassName());
            MethodInfo method;
            try {
                method = cls.getMethod(callee.getBytecodeMethodName(), callee.getDescriptor());
            } catch (InvalidByteCodeException e) {
                throw new BytecodeCompilerException("method not found", e);
            }

            trans = new Translator(cls, method, currentPO, localVariableNames, argNames, resultName, "CA_this_ref");
        }

        // TODO remove:)
        if (contracts.size() != 1) {
            throw new BytecodeCompilerException("TODO implement method contracts with also; size: " + contracts.size());
        }

        String[][] result = new String[contracts.size()][];
        for (int i = 0; i < result.length; i++) {
            MethodContract c = contracts.get(i);
            ArrayList<String> contract = new ArrayList<String>();

            // assert diverges (in case of termination)
            if (c.get(LineType.diverges).size() > 0) {
                StringBuilder sb = new StringBuilder("assert !$SPEC_diverges -> !(");
                for (int j = 0; j < c.get(LineType.diverges).size(); j++) {
                    Line line = c.get(LineType.diverges).get(j);
                    if (0 != j) {
                        sb.append(" & ");
                    }
                    sb.append(trans.translate(line.expr));
                }
                sb.append(")");
                contract.add(sb.toString());
            }

            // assert requires
            if (c.get(LineType.requires).size() > 0) {
                StringBuilder sb = new StringBuilder("assert ");
                for (int j = 0; j < c.get(LineType.requires).size(); j++) {
                    Line line = c.get(LineType.requires).get(j);
                    if (0 != j) {
                        sb.append(" & ");
                    }
                    sb.append(trans.translate(line.expr));
                }

                sb.append("; \"pre condition of ").append(callee).append("\"");

                contract.add(sb.toString());
            }

            // assignable
            {
                // create $SPEC_call_frame
                // havoc $delta_heap
                // assume ∀o $heap[o,$created] -> $delta_heap[o,$created]
                // $heap := (\merge_heap p; p :: $SPEC_call_frame |
                // !$heap[fst(p), $created]; $delta_heap; $heap)
                currentPO.requestFunction("$SPEC_call_frame", "set(prod(ref, field))", "assignable");
                contract.add("havoc $SPEC_call_frame");
                contract.add("assume "
                        + FrameBuilder.makeFrame("$SPEC_call_frame", "CA_this_ref", c, currentPO.resolver, trans,
                                currentPO.resolver.requestClass(callee.getBytecodeClassName())).toString());

                contract.add("havoc $delta_heap");
                contract.add("assume (\\forall o; $heap[o,$created] -> $delta_heap[o,$created])");
                contract.add(" $heap := (\\merge_heap p; p :: $SPEC_call_frame | !$heap[fst(p), $created]; $delta_heap; $heap)");
            }

            contract.add("assume $wellformed($heap)");

            // assume cond($exception=$null, ensures, signals)
            {
                StringBuilder sb = new StringBuilder("assume cond($exception=$null, true");
                for (Line line : c.get(LineType.ensures)) {
                    sb.append(" & ").append(trans.translate(line.expr));
                }
                sb.append(", ");

                if (c.get(LineType.signals).size() > 0) {
                    // jml man §9.9.4 specifies exceptions to be at least of
                    // type exception
                    currentPO.resolver.requestClass("java/lang/Exception");
                    sb.append(" instanceof($exception, T_java_lang_Exception)");
                    for (Line line : c.get(LineType.signals)) {
                        DumpVisitor type = new DumpVisitor();
                        type.visit(line.refType, null);
                        String sigType = type.getSource();
                        try {
                            currentPO.resolver.requestClass(sigType);
                        } catch (BytecodeCompilerException e) {
                            // try to look into java.lang
                            sigType = "java/lang/" + sigType;
                            currentPO.resolver.requestClass(sigType);

                            // convert sigType to type name
                            sigType = "T_" + sigType.replace("/", "_");

                            sb.append(" & (instanceof($exception, ").append(sigType).append(") -> ");
                            sb.append(trans.translate(line.expr));
                            sb.append(")");
                        }
                        sb.append(")");
                    }
                } else {
                    sb.append("false)");
                }

                contract.add(sb.toString());
            }

            // TODO measured_by?

            result[i] = contract.toArray(new String[contract.size()]);
        }

        return result[0];
    }

    @Override
    public boolean endsWithLoopInvariantStatement(String code) {
        if (code.startsWith("/*@ loop_invariant")) {
            return true;
        }
        return false;
    }

    @Override
    public String[] getSpecialContract(ConcreteProofObligation<?> currentPO, String content, String[] locals,
            String[] translatedLocals, int currentPC) throws BytecodeCompilerException {

        final Translator trans;
        final ClassFile cls;
        {
            MethodName callee = this.currentPO.getData();
            cls = currentPO.resolver.requestClass(callee.getBytecodeClassName());
            MethodInfo method;
            try {
                method = cls.getMethod(callee.getBytecodeMethodName(), callee.getDescriptor());
            } catch (InvalidByteCodeException e) {
                throw new BytecodeCompilerException("method not found", e);
            }
            trans = new Translator(cls, method, currentPO, translatedLocals, locals, "$result", "R_this_ref");
        }

        /**
         * expected are one of
         * <ul>
         * <li>assert expr;
         * <li>assume expr;
         * <li>invariant expr;
         * <li>ghost assignments?
         * </ul>
         */

        JMLStatement statement;
        try {
            statement = JMLParser.parseJMLStatement(new ByteArrayInputStream(content.getBytes()));
        } catch (Exception e) {
            throw new BytecodeCompilerException(content + " is not a valid jml statement", e);
        }

        String result;

        // TODO MU Was a java7-string-switch
        String type = statement.getType();
        if(type.equals("assert") || type.equals("assume")) {
            result = type + " " + trans.translate(statement.getArgument());

        } else if(type.equals("loop_invariant")) {
            List<StoreRefExpression> storeRefs = (null == statement.getAssignable() ? new ArrayList<StoreRefExpression>(
                    0) : statement.getAssignable());

            String[] rval = new String[4];
            // havoc frame
            // $SPEC_loop_frame = assigneable
            // $SPEC_loop_heap := $heap
            // skip $heap = (\mergeheap p; p :: frame; $heap; loop_heap) &
            // created & inv, var
            String heap = currentPO.requestFreshFunction("$SPEC_loop_heap", "heap", "assignable");
            String frame = currentPO.requestFreshFunction("$SPEC_loop_frame", "set(prod(ref, field))", "assignable");
            rval[0] = "havoc " + frame;
            rval[1] = "assume "
                    + FrameBuilder.makeFrame(frame, "R_this_ref", storeRefs, currentPO.resolver, trans, cls).toString();
            rval[2] = heap + ":= $heap";
            rval[3] = "skip_loopinv $heap = (\\merge_heap p; p :: " + frame + " | !" + heap
                    + "[fst(p), $created]; $heap; " + heap + ") & (\\forall o; " + heap
                    + "[o,$created] -> $heap[o,$created]) & $wellformed($heap) & ";

            result = trans.translate(statement.getArgument());

            if (null != statement.getVariant()) {
                result = result + ", " + trans.translate(statement.getVariant());
            }
            rval[3] = rval[3] + result;

            return rval;
        } else {

            result = "☢ could not translate " + content + "☢";
        }

        return new String[] { result };
    }

    @Override
    public String getInvariant(ConcreteProofObligation<?> currentPO, ClassType typeName)
            throws BytecodeCompilerException {
        String jmlPath = ((ClassProofObligation) currentPO.parent).classPath + typeName.getBytecodeClassName() + ".jml";
        if (!new File(jmlPath).exists()) {
            // if no contract was supplied by the problem, look at the default
            // contracts
            jmlPath = Environment.SYS_DIR + "/jmlContracts/" + typeName.getBytecodeClassName() + ".jml";

            if (!new File(jmlPath).exists()) {
                throw new BytecodeCompilerException("there is no jml file for " + typeName);
            }
        }

        CompilationUnit jml;
        try {
            jml = JMLParser.parse(new FileInputStream(jmlPath));
        } catch (Exception e) {
            throw new BytecodeCompilerException("failed to parse " + jmlPath, e);
        }

        final List<InvariantDeclaration> contracts = new ArrayList<InvariantDeclaration>();

        (new VoidVisitorAdapter<Void>() {

            @Override
            public void visit(InvariantDeclaration node, Void arg) {
                contracts.add(node);
            }
        }).visit(jml, null);

        if (0 == contracts.size()) {
            return "true";
        }

        Translator trans = new Translator(currentPO.resolver.requestClass(typeName.getBytecodeClassName()), currentPO,
                "%o");

        StringBuilder result = new StringBuilder();
        for (int index = 0; index < contracts.size(); index++) {
            final InvariantDeclaration inv = contracts.get(index);
            if (0 != index) {
                result.append(" & ");
            }
            result.append(trans.translate(inv.getExpression()).replace("$heap", "%h").replace("$old_heap", "%h"));
        }

        return result.toString();
    }

    @Override
    public String[] getMonitorContract(final String target, ConcreteProofObligation<?> po, int pc, boolean isEnter)
            throws BytecodeCompilerException {
        // no real translation is given, because we dont support parallel
        // programs
        po.addWarning(new Exception("monitors are unsupported by this JML translation"));
        return new String[] { "assert false;\"unsupported operation: monitor" + (isEnter ? "enter\"" : "exit\"") };
    }
}
