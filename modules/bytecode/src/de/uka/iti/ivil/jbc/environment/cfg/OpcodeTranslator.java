package de.uka.iti.ivil.jbc.environment.cfg;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.gjt.jclasslib.bytecode.AbstractBranchInstruction;
import org.gjt.jclasslib.bytecode.AbstractInstruction;
import org.gjt.jclasslib.bytecode.ImmediateByteInstruction;
import org.gjt.jclasslib.bytecode.ImmediateShortInstruction;
import org.gjt.jclasslib.bytecode.IncrementInstruction;
import org.gjt.jclasslib.bytecode.LookupSwitchInstruction;
import org.gjt.jclasslib.bytecode.MatchOffsetPair;
import org.gjt.jclasslib.bytecode.MultianewarrayInstruction;
import org.gjt.jclasslib.bytecode.OpcodesUtil;
import org.gjt.jclasslib.bytecode.TableSwitchInstruction;
import org.gjt.jclasslib.structures.CPInfo;
import org.gjt.jclasslib.structures.ClassFile;
import org.gjt.jclasslib.structures.InvalidByteCodeException;
import org.gjt.jclasslib.structures.attributes.CodeAttribute;
import org.gjt.jclasslib.structures.attributes.ExceptionTableEntry;
import org.gjt.jclasslib.structures.constants.ConstantClassInfo;
import org.gjt.jclasslib.structures.constants.ConstantDoubleInfo;
import org.gjt.jclasslib.structures.constants.ConstantFieldrefInfo;
import org.gjt.jclasslib.structures.constants.ConstantFloatInfo;
import org.gjt.jclasslib.structures.constants.ConstantIntegerInfo;
import org.gjt.jclasslib.structures.constants.ConstantLongInfo;
import org.gjt.jclasslib.structures.constants.ConstantReference;
import org.gjt.jclasslib.structures.constants.ConstantStringInfo;

import de.uka.iti.ivil.jbc.environment.BytecodeCompilerException;
import de.uka.iti.ivil.jbc.environment.NameResolver;
import de.uka.iti.ivil.jbc.util.ClassType;
import de.uka.iti.ivil.jbc.util.EscapeName;
import de.uka.iti.ivil.jbc.util.MethodName;
import de.uka.iti.ivil.jbc.util.MethodType;
import de.uka.iti.ivil.jbc.util.ObjectType;

/**
 * The opcode translator provides a function to add translations of arbitrary
 * opcodes to your statementlist. This file contains all opcode translations in
 * order to make (translation) refactoring more efficient and less error prone.
 * 
 * @author timm.felden@felden.com
 * 
 */
final class OpcodeTranslator {

    static public final String IMPLICIT_NPE = "; \"no implicit NullPointerException\"";
    static public final String IMPLICIT_NASE = "; \"no implicit NegativeArraySizeException\"";
    static public final String IMPLICIT_CCE = "; \"no implicit ClassCastException\"";
    static public final String IMPLICIT_AOOBE = "; \"no implicit ArrayOutOfBoundsException\"";
    static public final String IMPLICIT_AE = "; \"no implicit ArithmeticException\"";

    static void translate(ControlFlowGraph cfg) throws BytecodeCompilerException {
        // do nothing if no code is present
        if (0 == cfg.instructions.length)
            return;

        final NameResolver resolver = cfg.getPO().resolver;
        do {
            if (cfg.currentInstractionIsSpecial()) {
                // skip the ldc part
                if (cfg.getCurrentInstruction().getOpcode() == AbstractInstruction.OPCODE_LDC)
                    continue;

                final String specialCode = cfg.getSpecialFunctionArgument();

                int targetIndex = cfg.instructionIndex;
                boolean isLoopInvariant = cfg.getPO().getContractResolver().endsWithLoopInvariantStatement(specialCode);
                if (isLoopInvariant) {
                    // calculate the target index so the names will be correctly
                    // resolved
                    while (targetIndex < cfg.instructions.length) {
                        if (cfg.blocks.getBlock(targetIndex).parents.size() > 1)
                            break;
                        else
                            targetIndex++;
                    }
                }
                cfg.writeBeginStatement();

                String[] transNames = new String[((CodeAttribute) cfg.method.findAttribute(CodeAttribute.class))
                        .getMaxLocals()];
                String[] names = new String[transNames.length];
                for (int i = 0; i < names.length; i++) {
                    transNames[i] = cfg.registers.get(i, targetIndex);
                    if (null != transNames[i]) {
                        names[i] = EscapeName.revert(transNames[i].split("_")[1]);
                    }
                }

                String[] contract = cfg
                        .getPO()
                        .getContractResolver()
                        .getSpecialContract(cfg.getPO(), specialCode, transNames, names,
                                cfg.getCurrentInstruction().getOffset());

                // ensure there is at least one statement
                if (contract.length > 0) {
                    for (int i = 0; i < contract.length - 1; i++)
                        cfg.statements.end(contract[i]);

                    if (isLoopInvariant)
                        cfg.statements.endMoveable(contract[contract.length - 1]);
                    else
                        cfg.statements.end(contract[contract.length - 1]);
                } else
                    cfg.statements.end("skip");

            } else {
                cfg.writeBeginStatement();
                cfg.statements.add("# ").add(cfg.getCurrentInstruction().getOpcodeVerbose()).add("\n  ");
                addCurrentStatement(cfg, resolver);
            }
        } while (cfg.finishInstruction());
    }

    /**
     * Adds all necessary statements to the statement list which are needed to
     * model the effect of the current opcode.
     * 
     * @throws BytecodeCompilerException
     *             thrown in case of errors such as unsupported opcodes
     */
    private static void addCurrentStatement(ControlFlowGraph cfg, NameResolver resolver)
            throws BytecodeCompilerException {
        switch (cfg.getCurrentInstruction().getOpcode()) {

        // misc

        case AbstractInstruction.OPCODE_NOP:
        case AbstractInstruction.OPCODE_WIDE:
            cfg.statements.end("skip");
            return;

        case AbstractInstruction.OPCODE_NEW:
            _new(cfg, resolver);
            return;

        case AbstractInstruction.OPCODE_NEWARRAY:
            newArray(cfg);
            return;

        case AbstractInstruction.OPCODE_ANEWARRAY:
            aNewArray(cfg);
            return;

        case AbstractInstruction.OPCODE_MULTIANEWARRAY:
            multianewarray(cfg);
            return;

            // stack manipulations

        case AbstractInstruction.OPCODE_POP:
        case AbstractInstruction.OPCODE_POP2:
            pop(cfg);
            return;

        case AbstractInstruction.OPCODE_SWAP:
            swap(cfg);
            return;

        case AbstractInstruction.OPCODE_DUP:
        case AbstractInstruction.OPCODE_DUP_X1:
        case AbstractInstruction.OPCODE_DUP_X2:
            dup(cfg);
            return;

        case AbstractInstruction.OPCODE_DUP2:
        case AbstractInstruction.OPCODE_DUP2_X1:
        case AbstractInstruction.OPCODE_DUP2_X2:
            dup2(cfg);
            return;

            // constants

        case AbstractInstruction.OPCODE_ACONST_NULL:
            aconstNull(cfg);
            return;

        case AbstractInstruction.OPCODE_ICONST_M1:
        case AbstractInstruction.OPCODE_ICONST_0:
        case AbstractInstruction.OPCODE_ICONST_1:
        case AbstractInstruction.OPCODE_ICONST_2:
        case AbstractInstruction.OPCODE_ICONST_3:
        case AbstractInstruction.OPCODE_ICONST_4:
        case AbstractInstruction.OPCODE_ICONST_5:
            iconst(cfg);
            return;

        case AbstractInstruction.OPCODE_LCONST_0:
        case AbstractInstruction.OPCODE_LCONST_1:
            lconst(cfg);
            return;

        case AbstractInstruction.OPCODE_FCONST_0:
        case AbstractInstruction.OPCODE_DCONST_0:
            fconst(cfg, 0);
            return;
        case AbstractInstruction.OPCODE_FCONST_1:
        case AbstractInstruction.OPCODE_DCONST_1:
            fconst(cfg, 1);
            return;
        case AbstractInstruction.OPCODE_FCONST_2:
            fconst(cfg, 2);
            return;

        case AbstractInstruction.OPCODE_BIPUSH:
        case AbstractInstruction.OPCODE_SIPUSH:
            push(cfg);
            return;

        case AbstractInstruction.OPCODE_LDC:
        case AbstractInstruction.OPCODE_LDC_W:
        case AbstractInstruction.OPCODE_LDC2_W:
            if (!cfg.currentInstractionIsSpecial())
                ldc(cfg);
            return;

            // load/store

        case AbstractInstruction.OPCODE_ALOAD:
        case AbstractInstruction.OPCODE_ALOAD_0:
        case AbstractInstruction.OPCODE_ALOAD_1:
        case AbstractInstruction.OPCODE_ALOAD_2:
        case AbstractInstruction.OPCODE_ALOAD_3:
            Tload(cfg, AbstractInstruction.OPCODE_ALOAD, AbstractInstruction.OPCODE_ALOAD_0);
            return;

        case AbstractInstruction.OPCODE_ILOAD:
        case AbstractInstruction.OPCODE_ILOAD_0:
        case AbstractInstruction.OPCODE_ILOAD_1:
        case AbstractInstruction.OPCODE_ILOAD_2:
        case AbstractInstruction.OPCODE_ILOAD_3:
            Tload(cfg, AbstractInstruction.OPCODE_ILOAD, AbstractInstruction.OPCODE_ILOAD_0);
            return;

        case AbstractInstruction.OPCODE_LLOAD:
        case AbstractInstruction.OPCODE_LLOAD_0:
        case AbstractInstruction.OPCODE_LLOAD_1:
        case AbstractInstruction.OPCODE_LLOAD_2:
        case AbstractInstruction.OPCODE_LLOAD_3:
            Tload(cfg, AbstractInstruction.OPCODE_LLOAD, AbstractInstruction.OPCODE_LLOAD_0);
            return;

        case AbstractInstruction.OPCODE_FLOAD:
        case AbstractInstruction.OPCODE_FLOAD_0:
        case AbstractInstruction.OPCODE_FLOAD_1:
        case AbstractInstruction.OPCODE_FLOAD_2:
        case AbstractInstruction.OPCODE_FLOAD_3:
            Tload(cfg, AbstractInstruction.OPCODE_FLOAD, AbstractInstruction.OPCODE_FLOAD_0);
            return;

        case AbstractInstruction.OPCODE_DLOAD:
        case AbstractInstruction.OPCODE_DLOAD_0:
        case AbstractInstruction.OPCODE_DLOAD_1:
        case AbstractInstruction.OPCODE_DLOAD_2:
        case AbstractInstruction.OPCODE_DLOAD_3:
            Tload(cfg, AbstractInstruction.OPCODE_DLOAD, AbstractInstruction.OPCODE_DLOAD_0);
            return;

        case AbstractInstruction.OPCODE_ASTORE:
        case AbstractInstruction.OPCODE_ASTORE_0:
        case AbstractInstruction.OPCODE_ASTORE_1:
        case AbstractInstruction.OPCODE_ASTORE_2:
        case AbstractInstruction.OPCODE_ASTORE_3:
            Tstore(cfg, AbstractInstruction.OPCODE_ASTORE, AbstractInstruction.OPCODE_ASTORE_0);
            return;

        case AbstractInstruction.OPCODE_ISTORE:
        case AbstractInstruction.OPCODE_ISTORE_0:
        case AbstractInstruction.OPCODE_ISTORE_1:
        case AbstractInstruction.OPCODE_ISTORE_2:
        case AbstractInstruction.OPCODE_ISTORE_3:
            Tstore(cfg, AbstractInstruction.OPCODE_ISTORE, AbstractInstruction.OPCODE_ISTORE_0);
            return;

        case AbstractInstruction.OPCODE_LSTORE:
        case AbstractInstruction.OPCODE_LSTORE_0:
        case AbstractInstruction.OPCODE_LSTORE_1:
        case AbstractInstruction.OPCODE_LSTORE_2:
        case AbstractInstruction.OPCODE_LSTORE_3:
            Tstore(cfg, AbstractInstruction.OPCODE_LSTORE, AbstractInstruction.OPCODE_LSTORE_0);
            return;

        case AbstractInstruction.OPCODE_FSTORE:
        case AbstractInstruction.OPCODE_FSTORE_0:
        case AbstractInstruction.OPCODE_FSTORE_1:
        case AbstractInstruction.OPCODE_FSTORE_2:
        case AbstractInstruction.OPCODE_FSTORE_3:
            Tstore(cfg, AbstractInstruction.OPCODE_FSTORE, AbstractInstruction.OPCODE_FSTORE_0);
            return;

        case AbstractInstruction.OPCODE_DSTORE:
        case AbstractInstruction.OPCODE_DSTORE_0:
        case AbstractInstruction.OPCODE_DSTORE_1:
        case AbstractInstruction.OPCODE_DSTORE_2:
        case AbstractInstruction.OPCODE_DSTORE_3:
            Tstore(cfg, AbstractInstruction.OPCODE_DSTORE, AbstractInstruction.OPCODE_DSTORE_0);
            return;

            // array access

        case AbstractInstruction.OPCODE_BALOAD:
        case AbstractInstruction.OPCODE_SALOAD:
        case AbstractInstruction.OPCODE_CALOAD:
        case AbstractInstruction.OPCODE_IALOAD:
        case AbstractInstruction.OPCODE_LALOAD:
        case AbstractInstruction.OPCODE_FALOAD:
        case AbstractInstruction.OPCODE_DALOAD:
            Baload(cfg);
            return;

        case AbstractInstruction.OPCODE_AALOAD:
            aaload(cfg);
            return;

        case AbstractInstruction.OPCODE_BASTORE:
        case AbstractInstruction.OPCODE_SASTORE:
        case AbstractInstruction.OPCODE_CASTORE:
        case AbstractInstruction.OPCODE_IASTORE:
        case AbstractInstruction.OPCODE_LASTORE:
        case AbstractInstruction.OPCODE_FASTORE:
        case AbstractInstruction.OPCODE_DASTORE:
            Bastore(cfg);
            return;

        case AbstractInstruction.OPCODE_AASTORE:
            aastore(cfg);
            return;

        case AbstractInstruction.OPCODE_ARRAYLENGTH:
            arraylength(cfg);
            return;

            // object access

        case AbstractInstruction.OPCODE_GETFIELD:
            getfield(cfg, resolver);
            return;

        case AbstractInstruction.OPCODE_GETSTATIC:
            getstatic(cfg, resolver);
            return;

        case AbstractInstruction.OPCODE_PUTFIELD:
            putfield(cfg, resolver);
            return;

        case AbstractInstruction.OPCODE_PUTSTATIC:
            putstatic(cfg, resolver);
            return;

            // arithmetics, comparison and casts

        case AbstractInstruction.OPCODE_IADD:
        case AbstractInstruction.OPCODE_IAND:
        case AbstractInstruction.OPCODE_IDIV:
        case AbstractInstruction.OPCODE_IMUL:
        case AbstractInstruction.OPCODE_IOR:
        case AbstractInstruction.OPCODE_IREM:
        case AbstractInstruction.OPCODE_ISHL:
        case AbstractInstruction.OPCODE_ISHR:
        case AbstractInstruction.OPCODE_ISUB:
        case AbstractInstruction.OPCODE_IUSHR:
        case AbstractInstruction.OPCODE_IXOR:
        case AbstractInstruction.OPCODE_LADD:
        case AbstractInstruction.OPCODE_LAND:
        case AbstractInstruction.OPCODE_LDIV:
        case AbstractInstruction.OPCODE_LMUL:
        case AbstractInstruction.OPCODE_LOR:
        case AbstractInstruction.OPCODE_LREM:
        case AbstractInstruction.OPCODE_LSHL:
        case AbstractInstruction.OPCODE_LSHR:
        case AbstractInstruction.OPCODE_LSUB:
        case AbstractInstruction.OPCODE_LUSHR:
        case AbstractInstruction.OPCODE_LXOR:
        case AbstractInstruction.OPCODE_FADD:
        case AbstractInstruction.OPCODE_FDIV:
        case AbstractInstruction.OPCODE_FMUL:
        case AbstractInstruction.OPCODE_FREM:
        case AbstractInstruction.OPCODE_FSUB:
        case AbstractInstruction.OPCODE_DADD:
        case AbstractInstruction.OPCODE_DDIV:
        case AbstractInstruction.OPCODE_DMUL:
        case AbstractInstruction.OPCODE_DREM:
        case AbstractInstruction.OPCODE_DSUB:
        case AbstractInstruction.OPCODE_LCMP:
        case AbstractInstruction.OPCODE_FCMPL:
        case AbstractInstruction.OPCODE_FCMPG:
        case AbstractInstruction.OPCODE_DCMPL:
        case AbstractInstruction.OPCODE_DCMPG:
            binaryOperation(cfg);
            return;

        case AbstractInstruction.OPCODE_INEG:
        case AbstractInstruction.OPCODE_LNEG:
        case AbstractInstruction.OPCODE_FNEG:
        case AbstractInstruction.OPCODE_DNEG:
        case AbstractInstruction.OPCODE_I2B:
        case AbstractInstruction.OPCODE_I2F:
        case AbstractInstruction.OPCODE_I2L:
        case AbstractInstruction.OPCODE_I2D:
        case AbstractInstruction.OPCODE_I2S:
        case AbstractInstruction.OPCODE_L2I:
        case AbstractInstruction.OPCODE_L2D:
        case AbstractInstruction.OPCODE_L2F:
        case AbstractInstruction.OPCODE_F2I:
        case AbstractInstruction.OPCODE_F2L:
        case AbstractInstruction.OPCODE_F2D:
        case AbstractInstruction.OPCODE_D2I:
        case AbstractInstruction.OPCODE_D2F:
        case AbstractInstruction.OPCODE_D2L:
            unaryOperation(cfg);
            return;

        case AbstractInstruction.OPCODE_IINC:
            iinc(cfg);
            return;

        case AbstractInstruction.OPCODE_CHECKCAST:
            checkcast(cfg);
            return;

        case AbstractInstruction.OPCODE_INSTANCEOF:
            _instanceof(cfg);
            return;

            // control flow

        case AbstractInstruction.OPCODE_IF_ACMPEQ:
        case AbstractInstruction.OPCODE_IF_ACMPNE:
            if_acmpOP(cfg);
            return;

        case AbstractInstruction.OPCODE_IF_ICMPEQ:
        case AbstractInstruction.OPCODE_IF_ICMPGE:
        case AbstractInstruction.OPCODE_IF_ICMPGT:
        case AbstractInstruction.OPCODE_IF_ICMPLE:
        case AbstractInstruction.OPCODE_IF_ICMPLT:
        case AbstractInstruction.OPCODE_IF_ICMPNE:
            if_icmpOP(cfg);
            return;

        case AbstractInstruction.OPCODE_IFEQ:
        case AbstractInstruction.OPCODE_IFLE:
        case AbstractInstruction.OPCODE_IFLT:
        case AbstractInstruction.OPCODE_IFGE:
        case AbstractInstruction.OPCODE_IFGT:
        case AbstractInstruction.OPCODE_IFNE:
            ifOP(cfg);
            return;

        case AbstractInstruction.OPCODE_IFNULL:
        case AbstractInstruction.OPCODE_IFNONNULL:
            ifnull(cfg);
            return;

        case AbstractInstruction.OPCODE_GOTO:
        case AbstractInstruction.OPCODE_GOTO_W:
            _goto(cfg);
            return;

        case AbstractInstruction.OPCODE_LOOKUPSWITCH:
            lookupSwitch(cfg);
            return;

        case AbstractInstruction.OPCODE_TABLESWITCH:
            tableSwitch(cfg);
            return;

        case AbstractInstruction.OPCODE_ARETURN:
        case AbstractInstruction.OPCODE_DRETURN:
        case AbstractInstruction.OPCODE_FRETURN:
        case AbstractInstruction.OPCODE_IRETURN:
        case AbstractInstruction.OPCODE_LRETURN:
            Treturn(cfg);
            return;
        case AbstractInstruction.OPCODE_RETURN:
            cfg.statements.end("end");
            return;

        case AbstractInstruction.OPCODE_ATHROW:
            athrow(cfg, resolver);
            return;

            // invoke
        case AbstractInstruction.OPCODE_INVOKESTATIC:
            invoke(cfg, resolver, true);
            return;

        case AbstractInstruction.OPCODE_INVOKEVIRTUAL:
        case AbstractInstruction.OPCODE_INVOKESPECIAL:
        case AbstractInstruction.OPCODE_INVOKEINTERFACE:
            invoke(cfg, resolver, false);
            return;

        case AbstractInstruction.OPCODE_MONITORENTER:
            monitorUsage(cfg, true);
            return;
        case AbstractInstruction.OPCODE_MONITOREXIT:
            monitorUsage(cfg, false);
            return;

        case AbstractInstruction.OPCODE_INVOKEDYNAMIC:
            throw new BytecodeCompilerException("The JVM Spec clearly states, that invoke dynamic is currently unused!");

        default:
            throw new BytecodeCompilerException("Opcode " + cfg.getCurrentInstruction().getOpcodeVerbose()
                    + " is not yet supported; sorry.");
        }
    }

    private static void _instanceof(ControlFlowGraph cfg) throws BytecodeCompilerException {
        ImmediateShortInstruction inst = (ImmediateShortInstruction) cfg.getCurrentInstruction();

        // get type from the opcodes immediate
        String type;
        try {
            type = ObjectType.createTypeFromBytecodeClass(
                    cfg.proofObligation.resolver.requestClass(((ConstantClassInfo) cfg.method.getClassFile()
                            .getConstantPoolEntry(inst.getImmediateShort(), ConstantClassInfo.class)).getName()))
                    .getIvilTypeTerm();
        } catch (InvalidByteCodeException e) {
            throw new BytecodeCompilerException("failed to resolve typename", e);
        }

        final String r = cfg.pop();
        cfg.push("(!$null=" + r + ") & instanceof(" + r + ", " + type + ")");
    }

    private static void checkcast(ControlFlowGraph cfg) throws BytecodeCompilerException {
        final String target = cfg.peek(-1);

        ImmediateShortInstruction inst = (ImmediateShortInstruction) cfg.getCurrentInstruction();

        // get type from the opcodes immediate
        String type;
        try {
            type = ObjectType.createTypeFromBytecodeClass(
                    cfg.proofObligation.resolver.requestClass(((ConstantClassInfo) cfg.method.getClassFile()
                            .getConstantPoolEntry(inst.getImmediateShort(), ConstantClassInfo.class)).getName()))
                    .getIvilTypeTerm();
        } catch (InvalidByteCodeException e) {
            throw new BytecodeCompilerException("failed to resolve typename", e);
        }

        // assert instanceof(ref, type)
        cfg.statements.add("assert instanceof(").add(target).add(", ").add(type).add(")").end(IMPLICIT_CCE);
    }

    private static void swap(ControlFlowGraph cfg) throws BytecodeCompilerException {

        final String s = cfg.pop();
        final String t = cfg.pop();
        cfg.push(s);
        cfg.push(t);
    }

    @SuppressWarnings("unchecked")
    private static void lookupSwitch(ControlFlowGraph cfg) throws BytecodeCompilerException {
        LookupSwitchInstruction inst = (LookupSwitchInstruction) cfg.getCurrentInstruction();

        StringBuilder def = new StringBuilder();

        // pop(_i)
        // goto +1+(2*i)
        // assume case condition
        // goto target
        final String _i = cfg.pop();

        final String prefix = "PC" + inst.getOffset() + "_";

        String[] labels = cfg.statements.gotoLabels(prefix, inst.getMatchOffsetPairs().size() + 1);

        int index = 0;
        for (MatchOffsetPair pair : (java.util.List<MatchOffsetPair>) inst.getMatchOffsetPairs()) {
            if (def.length() != 0)
                def.append("&");
            def.append("(!").append(_i).append(" = ").append(pair.getMatch()).append(" )");

            cfg.statements.add(labels[index++]).add(":\n  ");
            cfg.statements.add("assume ").add(_i).add(" = ").end(pair.getMatch());
            cfg.statements.add("goto PC").end(inst.getOffset() + pair.getOffset());
        }

        cfg.statements.add(labels[index++]).add(":\n  ");
        cfg.statements.add("assume ").end(def.toString());
        cfg.statements.add("goto PC").end(inst.getOffset() + inst.getDefaultOffset());
    }

    private static void tableSwitch(ControlFlowGraph cfg) throws BytecodeCompilerException {

        StatementList st = cfg.statements;
        TableSwitchInstruction inst = (TableSwitchInstruction) cfg.getCurrentInstruction();

        // pop(_i)
        // goto +1, +3
        // assume _i < min || _i > max
        // goto default
        // goto +1+(2*i)
        // assume _i = i + min
        // goto target[i]

        final String prefix = "PC" + inst.getOffset() + "_";

        final String _i = cfg.pop();
        st.add("goto ").add(prefix).add("default, ").add(prefix).end("switch");

        st.add(prefix).add("default: \n");
        st.add("assume ").add(_i).add(" < ").add(inst.getLowByte()).add(" | ").add(_i).add(" > ")
                .end(inst.getHighByte());
        st.add("goto PC").end(inst.getOffset() + inst.getDefaultOffset());

        st.add(prefix).add("switch: \n");
        String labels[] = cfg.statements.gotoLabels(prefix, inst.getJumpOffsets().length);
        for (int i = 0; i < labels.length; i++) {
            st.add(labels[i]).add(": ");
            st.add("assume ").add(_i).add(" = $iadd(").add(inst.getLowByte()).add(", ").add(i).end(")");
            st.add("goto PC").end(inst.getOffset() + inst.getJumpOffsets()[i]);
        }
    }

    private static void arraylength(ControlFlowGraph cfg) throws BytecodeCompilerException {
        final String _r = cfg.pop();
        cfg.statements.add("assert !$null = ").add(_r).end(IMPLICIT_NPE);
        cfg.push("$heap[" + _r + ", $array_length]");
    }

    private static void monitorUsage(ControlFlowGraph cfg, boolean isEnter) throws BytecodeCompilerException {
        final String _r = cfg.pop();
        cfg.statements.add("assert !$null = ").add(_r).end(IMPLICIT_NPE);
        String[] contract = cfg.getPO().getContractResolver()
                .getMonitorContract(_r, cfg.getPO(), cfg.getCurrentInstruction().getOffset(), isEnter);

        for (String s : contract)
            cfg.statements.end(s);
    }

    private static void invoke(ControlFlowGraph cfg, NameResolver resolver, boolean isStatic)
            throws BytecodeCompilerException {
        // get commonly used informations
        StatementList st = cfg.statements;
        ImmediateShortInstruction instruction = (ImmediateShortInstruction) cfg.getCurrentInstruction();

        MethodName callee;
        ClassFile targetClass;
        try {
            ConstantReference info = (ConstantReference) cfg.method.getClassFile().getConstantPool()[instruction
                    .getImmediateShort()];
            targetClass = resolver.requestClass(info.getClassInfo().getName());
            callee = resolver.resolveMethodName(targetClass, info.getNameAndTypeInfo().getName(), info
                    .getNameAndTypeInfo().getDescriptor());
        } catch (InvalidByteCodeException e) {
            throw new BytecodeCompilerException(e);
        }
        MethodType signature = callee.getSignature(targetClass);

        // create required label names
        String exceptionalTermination = "PC" + cfg.pc() + "exceptionalCase", normalTermination = "PC" + cfg.pc()
                + "normalCase";

        // create name maps required for contract resolution
        String[] argumentNames = new String[signature.getArgumentTypes().size()];

        String resultName = "CResult_" + signature.getResultType().getBaseType();
        cfg.proofObligation.requestFunction(resultName, signature.getResultType().getBaseType(), "assignable");

        // //// create statements //// //

        // havoc
        st.end("havoc $exception");

        // pop in reverse order
        for (int i = signature.getArgumentTypes().size() - 1; i >= 0; i--)
            argumentNames[i] = cfg.pop();

        // check for heap pollution
        if (signature.isGeneric() && signature.getArgumentTypes().size() > 0) {
            // what about polymorphic methods? the type arguments should have
            // been instantiated on this stage, but what is the instantiation?
            if (isStatic) {
                // access to static generic methods is different and should be
                // easier, because the type should be completely instantiated?
            } else {
                ClassType cls = ClassType.createTypeFromBytecodeClass(targetClass);
                // assert ∃ T. typeof(CA_this_ref, type'(T)) & typeof(arg,
                // type(T))
                // (it is actually more efficient to fuse all those assertions
                // into one single assertion, because the instantiation of T
                // will be the same anyway)
                for (String tv : cls.getIvilTypeVaribaleNames())
                    st.add("(\\exists ").add(tv).add("; ");

                st.add("instanceof(CA_this_ref, ").add(cls.getIvilQuantifiedTypeTerm()).add(")");

                for (int i = 0; i < argumentNames.length; i++)
                    st.add(" & ").add("instanceof(").add(argumentNames[i]).add(", ")
                            .add(signature.getArgumentTypes().get(i).getIvilTypeTerm(cls.getDefaultInstantiationMap()))
                            .add(")");

                for (int i = cls.getIvilTypeVaribaleNames().size(); i > 0; i--)
                    st.add(")");

                st.end("");
            }
        }

        // NPE on this
        if (!isStatic) {
            cfg.proofObligation.requestFunction("CA_this_ref", "ref", "assignable");
            cfg.popAssign("CA_this_ref");
            st.add("assert !CA_this_ref = $null").end(IMPLICIT_NPE);
            if (signature.isGeneric()) {
                // check for heap pollution on this
            }
        }

        // updated old
        st.end("$temporary_heap := $old_heap");
        st.end("$old_heap := $heap");

        // insert contract
        String[] contract;
        try {
            contract = cfg.getPO().getContractResolver()
                    .getCallContracts(cfg.getPO(), callee, argumentNames, resultName);
        } catch (BytecodeCompilerException e) {
            throw new BytecodeCompilerException("failed to retrieve contracts for " + callee, e);
        }
        if (contract.length == 0)
            throw new BytecodeCompilerException("The method " + callee
                    + " has no contract!\n(A method contract musst not be empty.)");
        else
            for (String line : contract)
                st.end(line);

        // restore old
        st.end("$old_heap := $temporary_heap");

        // check for termination of callee
        st.add("goto ").add(exceptionalTermination).add(",").end(normalTermination);

        // exceptional
        st.add(exceptionalTermination).add(":\n  ");
        st.end("assume !$null = $exception");

        // makeExceptionhandlers
        {
            List<ExceptionTableEntry> handlers = new ArrayList<ExceptionTableEntry>();
            CodeAttribute c = ((CodeAttribute) cfg.method.findAttribute(CodeAttribute.class));
            for (ExceptionTableEntry entry : c.getExceptionTable())
                if (entry.getStartPc() <= cfg.pc() && cfg.pc() < entry.getEndPc())
                    handlers.add(entry);

            makeExceptionHandler(cfg, resolver, st, c, handlers);
        }

        // normal
        st.add(normalTermination).add(":\n  ");
        st.end("assume $null = $exception");

        // push result, if there is one
        if (!callee.getDescriptor().endsWith(")V")) {
            if (signature.isGeneric() && "ref".equals(signature.getResultType().getBaseType())) {
                st.add("assume ");
                ClassType cls = ClassType.createTypeFromBytecodeClass(targetClass);
                // assert ∃ T. typeof(CA_this_ref, type'(T)) & typeof(arg,
                // type(T))
                // (it is actually more efficient to fuse all those assertions
                // into one single assertion, because the instantiation of T
                // will be the same anyway)
                for (String tv : cls.getIvilTypeVaribaleNames())
                    st.add("(\\exists ").add(tv).add("; ");

                st.add("instanceof(CA_this_ref, ").add(cls.getIvilQuantifiedTypeTerm()).add(")");

                st.add(" & ").add("instanceof(").add(resultName).add(", ")
                        .add(signature.getResultType().getIvilTypeTerm(cls.getDefaultInstantiationMap())).add(")");

                for (int i = cls.getIvilTypeVaribaleNames().size(); i > 0; i--)
                    st.add(")");

                st.end("");
            }
            cfg.push(resultName);
        } else
            st.end("skip");
    }

    private static void dup(ControlFlowGraph cfg) throws BytecodeCompilerException {
        switch (cfg.getCurrentInstruction().getOpcode()) {
        case AbstractInstruction.OPCODE_DUP:
            cfg.push(cfg.peek(-1));
            break;

        case AbstractInstruction.OPCODE_DUP_X1: {
            String s = cfg.pop();
            String t = cfg.pop();
            cfg.push(s);
            cfg.push(t);
            cfg.push(s);
        }
            break;

        case AbstractInstruction.OPCODE_DUP_X2: {
            boolean mode1 = cfg.peekType(-2).isCategory2Type();

            String s = cfg.pop();
            String t = cfg.pop();

            if (mode1) {
                cfg.push(s);
                cfg.push(t);
                cfg.push(s);
            } else {
                String u = cfg.pop();

                cfg.push(s);
                cfg.push(u);
                cfg.push(t);
                cfg.push(s);
            }
        }
            break;
        }
    }

    private static void dup2(ControlFlowGraph cfg) throws BytecodeCompilerException {

        switch (cfg.getCurrentInstruction().getOpcode()) {
        case AbstractInstruction.OPCODE_DUP2: {
            ObjectType t = cfg.peekType(-1);
            if (t.isCategory2Type()) {
                cfg.push(cfg.peek(-1));
            } else {
                String u = cfg.peek(-1), v = cfg.peek(-2);
                cfg.push(v);
                cfg.push(u);
            }
        }
            break;
        case AbstractInstruction.OPCODE_DUP2_X1: {
            boolean mode1 = cfg.peekType(-1).isCategory2Type();

            String s = cfg.pop();
            String t = cfg.pop();

            if (mode1) {
                cfg.push(s);
                cfg.push(t);
                cfg.push(s);
            } else {
                String u = cfg.pop();

                cfg.push(t);
                cfg.push(s);
                cfg.push(u);
                cfg.push(t);
                cfg.push(s);
            }
        }
            break;
        case AbstractInstruction.OPCODE_DUP2_X2: {
            boolean mode1 = cfg.peekType(-1).isCategory2Type();
            boolean mode2 = cfg.peekType(-2).isCategory2Type();

            String s = cfg.pop();
            String t = cfg.pop();
            if (mode1) {
                if (mode2) {
                    cfg.push(s);
                    cfg.push(t);
                    cfg.push(s);
                } else {
                    String u = cfg.pop();

                    cfg.push(s);
                    cfg.push(u);
                    cfg.push(t);
                    cfg.push(s);
                }
            } else {
                if (cfg.peekType(-1).isCategory2Type()) {

                    String u = cfg.pop();

                    cfg.push(t);
                    cfg.push(s);
                    cfg.push(u);
                    cfg.push(t);
                    cfg.push(s);
                } else {
                    String u = cfg.pop();
                    String v = cfg.pop();

                    cfg.push(t);
                    cfg.push(s);
                    cfg.push(v);
                    cfg.push(u);
                    cfg.push(t);
                    cfg.push(s);
                }
            }
        }
            break;
        }
    }

    private static void _new(ControlFlowGraph cfg, NameResolver resolver) throws BytecodeCompilerException {
        final StatementList st = cfg.statements;

        ClassType type;
        try {
            type = ClassType
                    .createTypeFromBytecodeClass(cfg.proofObligation.resolver
                            .requestClass(((ConstantClassInfo) cfg.method.getClassFile().getConstantPool()[((ImmediateShortInstruction) cfg
                                    .getCurrentInstruction()).getImmediateShort()]).getName()));
        } catch (InvalidByteCodeException e) {
            throw new BytecodeCompilerException(e);
        }

        // create an arbitrary new object, that is not an array
        st.end("havoc $newObject");

        // assume that the new object does not yet exist
        st.end("assume !$heap[$newObject, $created]; \"find an unallocated object\"");

        // set the right type
        st.add("assume ");
        if (type.getFreeTypeVariables().size() > 0) {
            for (String name : type.getIvilTypeVaribaleNames())
                st.add("(\\exists ").add(name).add("; ");
        }
        st.add("exactTypeOf($newObject) = ").add(type.getIvilQuantifiedTypeTerm());
        for (int i = type.getFreeTypeVariables().size(); i > 0; i--)
            st.add(")");
        st.end("");

        // allocate the object
        st.end("$heap := $alloc($newObject, $heap); \"allocate that object on the heap\"");

        cfg.push("$newObject");
    }

    private static void newArray(ControlFlowGraph cfg) throws BytecodeCompilerException {
        final StatementList st = cfg.statements;

        final String _i0 = cfg.pop();
        st.add("assert ").add(_i0).add(" >= 0").end(IMPLICIT_NASE);
        st.end("havoc $newArray");
        st.end("assume !$heap[$newArray, $created]");

        // set the correct array type
        final int type = ((ImmediateByteInstruction) cfg.getCurrentInstruction()).getImmediateByte();
        st.add("assume exactTypeOf($newArray) = TF_").add(OpcodesUtil.getArrayTypeVerbose(type).toUpperCase())
                .end("_ARRAY");

        // array indices are just fields, thus they will have default values
        // as well
        st.end("$heap := $alloc($newArray, $heap)");

        // set the correct length
        st.end("$heap := $heap[$newArray, $array_length := " + _i0 + "]");

        cfg.push("$newArray");
    }

    private static void aNewArray(ControlFlowGraph cfg) throws BytecodeCompilerException {
        final StatementList st = cfg.statements;
        ImmediateShortInstruction inst = (ImmediateShortInstruction) cfg.getCurrentInstruction();

        // get type from the opcodes immediate
        String type;
        try {
            String imm = ((ConstantClassInfo) cfg.method.getClassFile().getConstantPoolEntry(inst.getImmediateShort(),
                    ConstantClassInfo.class)).getName();

            if ('[' == imm.charAt(0))
                type = ObjectType.createTypeFromSingleTypeDescriptor(imm).getIvilTypeTerm();
            else
                type = ObjectType.createTypeFromBytecodeClass(cfg.proofObligation.resolver.requestClass(imm))
                        .getIvilTypeTerm();
        } catch (InvalidByteCodeException e) {
            throw new BytecodeCompilerException("failed to resolve typename", e);
        }

        final String _i0 = cfg.pop();
        st.add("assert ").add(_i0).add(" >= 0").end(IMPLICIT_NASE);
        st.end("havoc $newArray");
        st.add("assume exactTypeOf($newArray) = TF_array(").add(type).end(")");
        st.end("assume !$heap[$newArray, $created]");
        st.end("$heap := $alloc($newArray, $heap)");
        st.end("$heap := $heap[$newArray, $array_length := " + _i0 + "]");

        cfg.push("$newArray");
    }

    private static void multianewarray(ControlFlowGraph cfg) throws BytecodeCompilerException {
        final StatementList st = cfg.statements;
        final int depth = ((MultianewarrayInstruction) cfg.getCurrentInstruction()).getDimensions();
        final String[] type = new String[depth];
        {
            String t = cfg.peekResultType().getIvilTypeTerm();
            for (int i = 0; i < depth; i++) {
                type[i] = t;
                t = t.substring("TF_array(".length(), t.length() - 1);
            }
        }

        // first we save back and check the values on the stack
        String dimensions[] = new String[depth];
        for (int i = depth - 1; i > -1; i--) {
            dimensions[i] = cfg.pop();
            st.add("assert ").add(dimensions[i]).add(" >= 0").end(IMPLICIT_NASE);
        }

        st.end("havoc $delta_heap");
        // assume that the required array structure exists already on $dh
        st.end("havoc $newArray");
        st.add("assume $newArray = (\\newObject v; exactTypeOf(v) = ").add(type[0])
                .add(" & $delta_heap[v,$array_length] = ").add(dimensions[0]).end("; $heap; $delta_heap)");

        // create inner arrays and make them have no children
        {
            String target = "$newArray";
            st.add("assume ");

            for (int i = 1; i < depth; i++) {
                target = "$delta_heap[" + target + ", $array_index(d" + i + ")]";
                st.add("(\\forall d").add(i).add("; (0<=d").add(i).add(" & d").add(i).add(" < ").add(dimensions[i - 1]);

                st.add(") -> (").add(target).add(" = (\\newObject v; exactTypeOf(v) = ").add(type[i])
                        .add(" & $delta_heap[v,$array_length] = ").add(dimensions[i]).add("; $heap; $delta_heap) & ");
            }
            // assume that the most inner targets do not have children
            st.add("(\\forall f; $delta_heap[").add(target).add(", f] = $null)");

            for (int i = 1; i < depth; i++)
                st.add("))");

            st.end("");
        }

        // assume that all arrays are distinct from each other
        // ∀r,f. $dh[r,f] = 0 | ∀r',f'. $dh[r,f] = $dh[r',f'] -> r=r' & f=f'
        st.end("assume (\\forall r; (\\forall f; $delta_heap[r,f] = $null | (!$delta_heap[r,f] = $newArray & (\\forall r_; (\\forall f_; (\\T_all 'a; $delta_heap[r,f] = $delta_heap[r_,f_] as 'a) -> (r=r_ & f=f_))))))");

        // assume that $dh itself is a wellformed heap
        st.end("assume $wellformed($delta_heap)");

        // merge $dh and $heap
        st.end("$heap := (\\merge_heap p; $heap[fst(p), $created]; $heap; $delta_heap)");

        cfg.push("$newArray");
    }

    private static void athrow(ControlFlowGraph cfg, NameResolver resolver) throws BytecodeCompilerException {

        final StatementList st = cfg.statements;

        CodeAttribute code = (CodeAttribute) cfg.method.findAttribute(CodeAttribute.class);

        // if the exception is catched by an exception handler, throws acts like
        // a jump to this handler
        List<ExceptionTableEntry> handlers = new ArrayList<ExceptionTableEntry>();
        for (ExceptionTableEntry entry : code.getExceptionTable())
            if (entry.getStartPc() <= cfg.pc() && cfg.pc() < entry.getEndPc())
                handlers.add(entry);

        cfg.popAssign("$exception");
        st.add("assert !$exception = $null").end(IMPLICIT_NPE);

        if (handlers.isEmpty()) {
            // we have no matching handler, thus we shorten the translation and
            // simply return the exception
            st.end("end");

        }

        // if the exception is not catched by any exception handler, throw will
        // end the excecution of this method
        makeExceptionHandler(cfg, resolver, st, code, handlers);

    }

    private static void pop(ControlFlowGraph cfg) throws BytecodeCompilerException {
        cfg.pop();
        if (cfg.getCurrentInstruction().getOpcode() == AbstractInstruction.OPCODE_POP2
                && !cfg.peekType(-1).isCategory2Type())
            cfg.pop();

    }

    private static void aconstNull(ControlFlowGraph cfg) throws BytecodeCompilerException {
        cfg.push("$null");

    }

    private static void iconst(ControlFlowGraph cfg) throws BytecodeCompilerException {

        if ("bool".equals(cfg.peekResultType().getBaseType())) {
            cfg.push("" + (cfg.getCurrentInstruction().getOpcode() == AbstractInstruction.OPCODE_ICONST_1));
        } else {
            cfg.push("" + (cfg.getCurrentInstruction().getOpcode() - AbstractInstruction.OPCODE_ICONST_0));
        }

    }

    private static void lconst(ControlFlowGraph cfg) throws BytecodeCompilerException {

        cfg.push("" + (cfg.getCurrentInstruction().getOpcode() - AbstractInstruction.OPCODE_LCONST_0));

    }

    private static void fconst(ControlFlowGraph cfg, float f) throws BytecodeCompilerException {

        cfg.push(cfg.getPO().getFloatLiteral(f));

    }

    private static void push(ControlFlowGraph cfg) throws BytecodeCompilerException {
        int val;
        if (cfg.getCurrentInstruction().getOpcode() == AbstractInstruction.OPCODE_SIPUSH)
            val = ((ImmediateShortInstruction) cfg.getCurrentInstruction()).getImmediateShort();
        else
            val = ((ImmediateByteInstruction) cfg.getCurrentInstruction()).getImmediateByte();

        cfg.push("" + val);

    }

    private static void ldc(ControlFlowGraph cfg) throws BytecodeCompilerException {
        int index;
        if (cfg.getCurrentInstruction().getOpcode() != AbstractInstruction.OPCODE_LDC)
            index = ((ImmediateShortInstruction) cfg.getCurrentInstruction()).getImmediateShort();
        else
            index = ((ImmediateByteInstruction) cfg.getCurrentInstruction()).getImmediateByte();

        String value;
        CPInfo entry;
        try {
            entry = cfg.method.getClassFile().getConstantPoolEntry(index, CPInfo.class);
            switch (entry.getTag()) {
            case CPInfo.CONSTANT_INTEGER:
                value = Integer.toString(((ConstantIntegerInfo) entry).getInt());
                break;
            case CPInfo.CONSTANT_LONG:
                value = Long.toString(((ConstantLongInfo) entry).getLong());
                break;
            case CPInfo.CONSTANT_FLOAT:
                value = cfg.getPO().getFloatLiteral(((ConstantFloatInfo) entry).getFloat());
                break;
            case CPInfo.CONSTANT_DOUBLE:
                value = cfg.getPO().getFloatLiteral(((ConstantDoubleInfo) entry).getDouble());
                break;
            case CPInfo.CONSTANT_STRING:
                // TODO actually, this is not very clever
                value = "\""
                        + cfg.method.getClassFile()
                                .getConstantPoolUtf8Entry(((ConstantStringInfo) entry).getStringIndex()).getString()
                        + "\"";
                break;

            default:
                throw new BytecodeCompilerException("illegal constant type: " + entry.getTagVerbose());
            }
        } catch (InvalidByteCodeException e) {
            throw new BytecodeCompilerException("", e);
        }

        cfg.push(value);

    }

    private static void Tload(ControlFlowGraph cfg, int immediate, int base) throws BytecodeCompilerException {
        AbstractInstruction inst = cfg.getCurrentInstruction();
        int pos;
        if (inst.getOpcode() == immediate) {
            pos = ((ImmediateByteInstruction) inst).getImmediateByte();
        } else {
            pos = inst.getOpcode() - base;
        }
        final String target = cfg.load(pos);

        cfg.push(target);
        if (base == AbstractInstruction.OPCODE_ALOAD) {
            cfg.statements.add("assume instanceof(").add(target).add(", ").add(cfg.peekResultType().getIvilTypeTerm())
                    .end(")");
        }

    }

    private static void Tstore(ControlFlowGraph cfg, int immediate, int base) throws BytecodeCompilerException {
        AbstractInstruction inst = cfg.getCurrentInstruction();
        int pos;
        if (inst.getOpcode() == immediate) {
            pos = ((ImmediateByteInstruction) inst).getImmediateByte();
        } else {
            pos = inst.getOpcode() - base;
        }
        final String target = cfg.store(pos);

        cfg.popAssign(target);
    }

    private static void Baload(ControlFlowGraph cfg) throws BytecodeCompilerException {

        final String _i = cfg.pop();
        final String _r = cfg.pop();
        cfg.statements.add("assert 0 <= ").add(_i).add(" & ").add(_i).add(" < $heap[").add(_r).add(", $array_length]")
                .end(IMPLICIT_AOOBE);
        cfg.statements.add("assert !$null=").add(_r).end(IMPLICIT_NPE);
        cfg.push("$heap[" + _r + ", $array_index(" + _i + ")]");

    }

    private static void aaload(ControlFlowGraph cfg) throws BytecodeCompilerException {

        final String _i = cfg.pop();
        final String _r = cfg.pop();
        cfg.statements.add("assert 0 <= ").add(_i).add(" & ").add(_i).add(" < $heap[").add(_r).add(", $array_length]")
                .end(IMPLICIT_AOOBE);
        cfg.statements.add("assert !$null=").add(_r).end(IMPLICIT_NPE);

        cfg.statements.add("assume (\\forall t; instanceof(").add(_r).add(", TF_array(t)) -> instanceof($heap[")
                .add(_r).add(", $array_index(").add(_i).end(")], t))");
        cfg.push("$heap[" + _r + ", $array_index(" + _i + ")]");
    }

    private static void Bastore(ControlFlowGraph cfg) throws BytecodeCompilerException {

        final String _t = cfg.pop();
        final String _i = cfg.pop();
        final String _r = cfg.pop();
        cfg.statements.add("assert 0 <= ").add(_i).add(" & ").add(_i).add(" < $heap[").add(_r).add(", $array_length]")
                .end(IMPLICIT_AOOBE);
        cfg.statements.add("assert !$null=").add(_r).end(IMPLICIT_NPE);

        cfg.statements.end("$heap := $heap[" + _r + ", $array_index(" + _i + ") := " + _t + "]");
    }

    private static void aastore(ControlFlowGraph cfg) throws BytecodeCompilerException {

        ObjectType type = cfg.peekType(-1);

        final String _t = cfg.pop();
        final String _i = cfg.pop();
        final String _r = cfg.pop();
        cfg.statements.add("assert 0 <= ").add(_i).add(" & ").add(_i).add(" < $heap[").add(_r).add(", $array_length]")
                .end(IMPLICIT_AOOBE);
        cfg.statements.add("assert !$null=").add(_r).end(IMPLICIT_NPE);

        // if the class name is null, the object to be pushed is a null
        // constant, which therefore has the correct type
        if (null != type)
            cfg.statements.end("assert (\\exists t; instanceof(" + _t + ", t) & TF_array(t) = exactTypeOf(" + _r
                    + ")); \"no implicit ArrayStoreException\"");

        cfg.statements.end("$heap := $heap[" + _r + ", $array_index(" + _i + ") := " + _t + "]");

    }

    private static void getfield(ControlFlowGraph cfg, NameResolver resolver) throws BytecodeCompilerException {

        String name;

        ImmediateShortInstruction instruction = (ImmediateShortInstruction) cfg.getCurrentInstruction();
        ConstantFieldrefInfo info = (ConstantFieldrefInfo) cfg.method.getClassFile().getConstantPool()[instruction
                .getImmediateShort()];
        final String targetClass;
        try {
            targetClass = info.getClassInfo().getName();
            String targetName = info.getNameAndTypeInfo().getName();
            name = resolver.resolveFieldName(targetClass, targetName);
        } catch (InvalidByteCodeException e) {
            throw new BytecodeCompilerException("illformed class file", e);
        }

        final String _r = cfg.pop();
        cfg.statements.add("assert !$null = ").add(_r).end(IMPLICIT_NPE);

        cfg.push("$heap[" + _r + ", " + name + "]");
    }

    private static void getstatic(ControlFlowGraph cfg, NameResolver resolver) throws BytecodeCompilerException {

        String name;

        ImmediateShortInstruction instruction = (ImmediateShortInstruction) cfg.getCurrentInstruction();
        ConstantFieldrefInfo info = (ConstantFieldrefInfo) cfg.method.getClassFile().getConstantPool()[instruction
                .getImmediateShort()];
        try {
            String targetClass = info.getClassInfo().getName();
            String targetName = info.getNameAndTypeInfo().getName();
            name = resolver.resolveFieldName(targetClass, targetName);
        } catch (InvalidByteCodeException e) {
            throw new BytecodeCompilerException("illformed class file", e);
        }

        cfg.push("$heap[$static, " + name + "]");

    }

    private static void putfield(ControlFlowGraph cfg, NameResolver resolver) throws BytecodeCompilerException {

        String name;

        ImmediateShortInstruction instruction = (ImmediateShortInstruction) cfg.getCurrentInstruction();
        ConstantFieldrefInfo info = (ConstantFieldrefInfo) cfg.method.getClassFile().getConstantPool()[instruction
                .getImmediateShort()];
        String targetClass;
        try {
            targetClass = info.getClassInfo().getName();
            String targetName = info.getNameAndTypeInfo().getName();
            name = resolver.resolveFieldName(targetClass, targetName);
        } catch (InvalidByteCodeException e) {
            throw new BytecodeCompilerException("illformed class file", e);
        }

        String value = cfg.pop();
        final String _r = cfg.pop();
        cfg.statements.add("assert !$null = ").add(_r).end(IMPLICIT_NPE);

        cfg.statements.add("$heap := $heap[").add(_r).add(", ").add(name).add(" := ").add(value).end("]");

    }

    private static void putstatic(ControlFlowGraph cfg, NameResolver resolver) throws BytecodeCompilerException {

        String name;

        ImmediateShortInstruction instruction = (ImmediateShortInstruction) cfg.getCurrentInstruction();
        ConstantFieldrefInfo info = (ConstantFieldrefInfo) cfg.method.getClassFile().getConstantPool()[instruction
                .getImmediateShort()];
        try {
            String targetClass = info.getClassInfo().getName();
            String targetName = info.getNameAndTypeInfo().getName();
            name = resolver.resolveFieldName(targetClass, targetName);
        } catch (InvalidByteCodeException e) {
            throw new BytecodeCompilerException("illformed class file", e);
        }

        String value = cfg.pop();
        cfg.statements.end("$heap := $heap[$static, " + name + " := " + value + "]");

    }

    private static void binaryOperation(ControlFlowGraph cfg) throws BytecodeCompilerException {
        final String _2 = cfg.pop();
        final String _1 = cfg.pop();

        if (cfg.getCurrentInstruction().getOpcode() == AbstractInstruction.OPCODE_IDIV
                || cfg.getCurrentInstruction().getOpcode() == AbstractInstruction.OPCODE_LDIV)
            cfg.statements.add("assert !0=").add(_2).end(IMPLICIT_AE);

        cfg.push("$" + cfg.getCurrentInstruction().getOpcodeVerbose() + "(" + _1 + ", " + _2 + ")");
    }

    private static void unaryOperation(ControlFlowGraph cfg) throws BytecodeCompilerException {

        cfg.push("$" + cfg.getCurrentInstruction().getOpcodeVerbose() + "(" + cfg.pop() + ")");
    }

    private static void iinc(ControlFlowGraph cfg) throws BytecodeCompilerException {

        IncrementInstruction i = (IncrementInstruction) cfg.getCurrentInstruction();
        int index = i.getImmediateByte(), val = i.getIncrementConst();

        // load before writing, just to be sure
        String tmp = cfg.load(index);
        cfg.statements.add(cfg.store(index)).add(" := $iadd(").add(tmp).add(", ").add(val).end(")");
    }

    private static void if_acmpOP(ControlFlowGraph cfg) throws BytecodeCompilerException {
        final int target = ((AbstractBranchInstruction) cfg.getCurrentInstruction()).getBranchOffset() + cfg.pc();
        final String branch = "PC" + cfg.pc() + "branch", cont = "PC" + cfg.pc() + "cont";

        StatementList st = cfg.statements;

        final String _r1 = cfg.pop();
        final String _r0 = cfg.pop();
        st.end("goto " + branch + ", " + cont);
        st.add(branch).add(": assume ");
        if (cfg.getCurrentInstruction().getOpcode() == AbstractInstruction.OPCODE_IF_ACMPNE)
            st.add("!");
        st.add("$eq(").add(_r0).add(", ").add(_r1).end(")");
        st.add("goto PC").end(target);
        st.add(cont).add(": assume ");
        if (cfg.getCurrentInstruction().getOpcode() == AbstractInstruction.OPCODE_IF_ACMPEQ)
            st.add("!");
        st.add("$eq(").add(_r0).add(", ").add(_r1).end(")");
        int next = cfg.pc() + cfg.getCurrentInstruction().getSize();
        st.add("goto PC").end(next);

    }

    private static String if_icmp$op(AbstractInstruction inst) {
        switch (inst.getOpcode()) {
        case AbstractInstruction.OPCODE_IF_ICMPEQ:
            return "$eq";
        case AbstractInstruction.OPCODE_IF_ICMPLE:
            return "$lte";
        case AbstractInstruction.OPCODE_IF_ICMPLT:
            return "$lt";
        case AbstractInstruction.OPCODE_IF_ICMPGE:
            return "$gte";
        case AbstractInstruction.OPCODE_IF_ICMPGT:
            return "$gt";
        case AbstractInstruction.OPCODE_IF_ICMPNE:
            return "$neq";

        default:
            throw new IllegalArgumentException("unsupported OPCODE: " + inst);
        }
    }

    private static void if_icmpOP(ControlFlowGraph cfg) throws BytecodeCompilerException {
        final int target = ((AbstractBranchInstruction) cfg.getCurrentInstruction()).getBranchOffset() + cfg.pc();

        StatementList st = cfg.statements;

        final String _1 = cfg.pop();
        final String _0 = cfg.pop();

        String prefix = "PC" + cfg.pc();
        st.end("goto " + prefix + "branch, " + prefix + "cont");
        st.add(prefix + "branch: assume ").add(if_icmp$op(cfg.getCurrentInstruction())).add("(").add(_0).add(", ")
                .add(_1).end(")");
        st.add("goto PC").end(target);
        st.add(prefix + "cont: assume !").add(if_icmp$op(cfg.getCurrentInstruction())).add("(").add(_0).add(", ")
                .add(_1).end(")");
        int next = cfg.pc() + cfg.getCurrentInstruction().getSize();
        st.add("goto PC").end(next);

    }

    private static String if$op(AbstractInstruction inst) {
        switch (inst.getOpcode()) {
        case AbstractInstruction.OPCODE_IFEQ:
            return "$eq";
        case AbstractInstruction.OPCODE_IFLE:
            return "$lte";
        case AbstractInstruction.OPCODE_IFLT:
            return "$lt";
        case AbstractInstruction.OPCODE_IFGE:
            return "$gte";
        case AbstractInstruction.OPCODE_IFGT:
            return "$gt";
        case AbstractInstruction.OPCODE_IFNE:
            return "$neq";

        default:
            throw new IllegalArgumentException("unsupported OPCODE: " + inst);
        }
    }

    private static void ifOP(ControlFlowGraph cfg) throws BytecodeCompilerException {
        final int target = ((AbstractBranchInstruction) cfg.getCurrentInstruction()).getBranchOffset() + cfg.pc();

        StatementList st = cfg.statements;

        String val = cfg.pop();

        String prefix = "PC" + cfg.pc();
        st.end("goto " + prefix + "branch, " + prefix + "cont");
        if ("bool".equals(cfg.peekType(-1).getBaseType())) {
            if (cfg.getCurrentInstruction().getOpcode() == AbstractInstruction.OPCODE_IFEQ)
                st.add(prefix).add("branch: assume !").end(val);
            else
                st.add(prefix).add("branch: assume ").end(val);
            st.add("goto PC").end(target);

            if (cfg.getCurrentInstruction().getOpcode() == AbstractInstruction.OPCODE_IFEQ)
                st.add(prefix).add("cont: assume ").end(val);
            else
                st.add(prefix).add("cont: assume !").end(val);

            int next = cfg.pc() + cfg.getCurrentInstruction().getSize();
            st.add("goto PC").end(next);

        } else {
            st.add(prefix).add("branch: assume ").add(if$op(cfg.getCurrentInstruction())).add("(").add(val).end(", 0)");
            st.add("goto PC").end(target);
            st.add(prefix).add("cont: assume !").add(if$op(cfg.getCurrentInstruction())).add("(").add(val).end(", 0)");
            int next = cfg.pc() + cfg.getCurrentInstruction().getSize();
            st.add("goto PC").end(next);
        }
    }

    private static void ifnull(ControlFlowGraph cfg) throws BytecodeCompilerException {
        final int target = ((AbstractBranchInstruction) cfg.getCurrentInstruction()).getBranchOffset() + cfg.pc();

        StatementList st = cfg.statements;

        final String _r = cfg.pop();

        String prefix = "PC" + cfg.pc();
        st.end("goto " + prefix + "branch, " + prefix + "cont");
        if (cfg.getCurrentInstruction().getOpcode() == AbstractInstruction.OPCODE_IFNULL)
            st.add(prefix).add("branch: assume $null = ").end(_r);
        else
            st.add(prefix).add("branch: assume !$null = ").end(_r);
        st.add("goto PC").end(target);
        if (cfg.getCurrentInstruction().getOpcode() == AbstractInstruction.OPCODE_IFNULL)
            st.add(prefix).add("cont: assume !$null = ").end(_r);
        else
            st.add(prefix).add("cont: assume $null = ").end(_r);
        int next = cfg.pc() + cfg.getCurrentInstruction().getSize();
        st.add("goto PC").end(next);

    }

    private static void _goto(ControlFlowGraph cfg) throws BytecodeCompilerException {

        int target = ((AbstractBranchInstruction) cfg.getCurrentInstruction()).getBranchOffset() + cfg.pc();

        cfg.statements.add("goto PC").end(target);
    }

    private static void Treturn(ControlFlowGraph cfg) throws BytecodeCompilerException {
        // assert absence of heap pollution
        if (cfg.peekType(-1).getBaseType().equals("ref")) {
            MethodType sig = cfg.methodName.getSignature(cfg.proofObligation.resolver);
            ClassType ct = ClassType.createTypeFromBytecodeClass(cfg.method.getClassFile());

            // in the non generic case, this assertion is guaranteed to be true
            // by the bytecode verifier
            if (sig.isGeneric()) {
                cfg.statements.add("assert instanceof(").add(cfg.peek(-1)).add(", ");
                Map<String, String> variableInstantiations = new HashMap<String, String>();
                for (int i = 0; i < ct.getFreeTypeVariables().size(); i++)
                    variableInstantiations.put(ct.getFreeTypeVariables().get(i), ct.getIvilTypeVaribaleNames().get(i)
                            .replaceFirst("TV_", "TA_"));

                cfg.statements.add(sig.getResultType().getIvilTypeTerm(variableInstantiations)).end(")");
            }
        }
        cfg.statements.add("assume $result = ").end(cfg.pop());
        cfg.statements.end("end");
    }

    /**
     * Inserts all statements needed for propper exception handling.
     */
    private static void makeExceptionHandler(ControlFlowGraph cfg, NameResolver resolver, StatementList st,
            CodeAttribute code, List<ExceptionTableEntry> handlers) throws BytecodeCompilerException {

        resolver.requestClass("java/lang/Throwable");
        // due to jbc constraints, as described in athrow documentation in jvm
        // spec
        st.end("assume instanceof($exception, T_java_lang_Throwable)");
        cfg.pushException();

        // goto <handlers>,<end>
        String[] targets = st.gotoLabels("PC" + cfg.pc() + "_ex", handlers.size() + 1);

        String handledTypes = "";
        for (int i = 0; i < handlers.size(); i++) {
            st.add(targets[i]).add(": ");
            final ExceptionTableEntry entry = handlers.get(i);
            // <handler>
            // assume AND(!instanceof(handeledexceptions, $exception))
            // assume instanceof(<handler.exception>, $exception)
            // goto <handler.target>

            if (i > 0)
                st.add("assume ").end(handledTypes);

            String type;
            try {
                if (0 != entry.getCatchType()) {
                    type = ((ConstantClassInfo) code.getClassFile().getConstantPool()[entry.getCatchType()]).getName();
                    resolver.requestClass(type);
                    type = "instanceof($exception, "
                            + ObjectType.createTypeFromBytecodeClass(resolver.requestClass(type)).getIvilTypeTerm()
                            + ")";
                } else {
                    resolver.requestClass("java/lang/Throwable");
                    type = "instanceof($exception, T_java_lang_Throwable)";
                }
            } catch (InvalidByteCodeException e) {
                throw new BytecodeCompilerException("failed to retrieve the exception handlers exception class name", e);
            }
            st.add("assume ").end(type);

            if (0 == i)
                handledTypes = "!" + type;
            else
                handledTypes = handledTypes + " & !" + type;

            st.add("goto PC").end(entry.getHandlerPc());
        }

        // <end>
        // assume AND(!instanceof(handeledexceptions, $exception))
        // end

        st.add(targets[targets.length - 1]).add(": ");
        if (!handledTypes.equals(""))
            st.add("assume ").end(handledTypes);
        st.end("end");
    }
}
