package de.uka.iti.ivil.jbc.environment.cfg;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Stack;

import org.gjt.jclasslib.bytecode.AbstractInstruction;
import org.gjt.jclasslib.bytecode.ImmediateByteInstruction;
import org.gjt.jclasslib.bytecode.ImmediateShortInstruction;
import org.gjt.jclasslib.bytecode.MultianewarrayInstruction;
import org.gjt.jclasslib.structures.CPInfo;
import org.gjt.jclasslib.structures.ClassFile;
import org.gjt.jclasslib.structures.InvalidByteCodeException;
import org.gjt.jclasslib.structures.attributes.CodeAttribute;
import org.gjt.jclasslib.structures.attributes.ExceptionTableEntry;
import org.gjt.jclasslib.structures.attributes.LocalVariableCommonEntry;
import org.gjt.jclasslib.structures.attributes.LocalVariableTableAttribute;
import org.gjt.jclasslib.structures.attributes.LocalVariableTypeTableAttribute;
import org.gjt.jclasslib.structures.constants.ConstantClassInfo;
import org.gjt.jclasslib.structures.constants.ConstantFieldrefInfo;
import org.gjt.jclasslib.structures.constants.ConstantMethodrefInfo;
import org.gjt.jclasslib.structures.constants.ConstantReference;
import org.gjt.jclasslib.structures.constants.ConstantUtf8Info;

import de.uka.iti.ivil.jbc.environment.BytecodeCompilerError;
import de.uka.iti.ivil.jbc.environment.BytecodeCompilerException;
import de.uka.iti.ivil.jbc.environment.NameResolver;
import de.uka.iti.ivil.jbc.util.ClassType;
import de.uka.iti.ivil.jbc.util.MethodName;
import de.uka.iti.ivil.jbc.util.MethodType;
import de.uka.iti.ivil.jbc.util.ObjectType;

/**
 * adds type constraints for cfg under construction
 *
 * @author timm.felden@felden.com
 *
 */
final class CFGTypeConstraintFactory {

    public static final class Constraint {
        // source is modifiable, while target is only modifiable if isSameType
        // is true; it will be modified as well, if there is now way through the
        // control flow graph that allows for different types
        public Type source, target;
        // the same type flag is used, if the types have to be the same, after
        // solution; this is required to allow for creation of types in one pass
        // instead of two?(check this)
        // if true, the target will be copied to source after merge
        public boolean isSameType;

        public Constraint(Type source, Type target, boolean same) {
            this.source = source;
            this.target = target;
            this.isSameType = same;
        }
    }

    // translation state
    private final ControlFlowGraph cfg;
    private final LinkedList<Constraint> typeConstraints;
    private final TypeFrame[] frames;

    // state of the current instruction
    private TypeFrame currentFrame;
    private AbstractInstruction inst;
    private int index;

    private CFGTypeConstraintFactory(ControlFlowGraph cfg, LinkedList<Constraint> typeConstraints, TypeFrame[] frames)
            throws BytecodeCompilerException {
        this.cfg = cfg;
        this.typeConstraints = typeConstraints;
        this.frames = frames;

        addLocalVariableTypes();
        addOpcodeTypeConstraints();
    }

    static public LinkedList<Constraint> build(ControlFlowGraph cfg, TypeFrame[] frames)
            throws BytecodeCompilerException {
        // Attention: This constructor already does the work!
        CFGTypeConstraintFactory state = new CFGTypeConstraintFactory(cfg,
                new LinkedList<CFGTypeConstraintFactory.Constraint>(), frames);
        return state.typeConstraints;
    }

    /**
     * gathers type information from the local variable and the local variable
     * type table to make type inference on the stack easier.
     */
    private void addLocalVariableTypes() {
        CodeAttribute code = (CodeAttribute) cfg.method.findAttribute(CodeAttribute.class);
        AbstractInstruction[] instructions = cfg.instructions;
        CPInfo[] cp = code.getClassFile().getConstantPool();

        // create frames
        for (int i = frames.length - 1; i >= 0; i--) {
            frames[i] = new TypeFrame(code.getMaxLocals());
        }

        // if there is generic type information present, evaluate these types
        // first
        {
            LocalVariableTypeTableAttribute lvars = (LocalVariableTypeTableAttribute) code
                    .findAttribute(LocalVariableTypeTableAttribute.class);
            if (null != lvars) {
                for (LocalVariableCommonEntry entry : lvars.getLocalVariableEntries()) {
                    for (int i = 0; i < frames.length; i++) {
                        if (entry.getStartPc() - 1 <= instructions[i].getOffset()
                                && instructions[i].getOffset() < (entry.getStartPc() + entry.getLength())) {
                            frames[i].localVariables[entry.getIndex()] = Type.createExactType(ObjectType
                                    .createTypeFromSingleTypeSignature(((ConstantUtf8Info) cp[entry
                                            .getDescriptorOrSignatureIndex()]).getString()));
                        }
                    }
                }
            }
        }

        // set types, that have not yet been written
        {
            LocalVariableTableAttribute lvars = (LocalVariableTableAttribute) code
                    .findAttribute(LocalVariableTableAttribute.class);
            if (null == lvars) {
                throw new BytecodeCompilerError("you have to compile your code with debug information!");
            }

            for (LocalVariableCommonEntry entry : lvars.getLocalVariableEntries()) {
                for (int i = 0; i < frames.length; i++) {
                    if (null == frames[i].localVariables[entry.getIndex()]
                            && entry.getStartPc() - 1 <= instructions[i].getOffset()
                            && instructions[i].getOffset() < (entry.getStartPc() + entry.getLength())) {
                        frames[i].localVariables[entry.getIndex()] = Type.createExactType(ObjectType
                                .createTypeFromSingleTypeDescriptor(((ConstantUtf8Info) cp[entry
                                        .getDescriptorOrSignatureIndex()]).getString()));
                    }
                }
            }
        }
    }

    // from stack clone; guaranteed by the stack class
    @SuppressWarnings("unchecked")
    private void addOpcodeTypeConstraints() throws BytecodeCompilerException {
        // get handler PCs, because a handler has allways a single ref on the
        // stack and not what ever was on the stack on parent instructions
        HashSet<Integer> handlerPCs = new HashSet<Integer>();

        for (ExceptionTableEntry entry : ((CodeAttribute) cfg.method.findAttribute(CodeAttribute.class))
                .getExceptionTable()) {
            handlerPCs.add(entry.getHandlerPc());
        }

        // add typing, stack levels and register usage
        for (int i = 0; i < cfg.instructions.length; i++) {
            inst = cfg.instructions[i];
            index = i;

            // initialize the current frame
            if (i > 0 && cfg.blocks.getBlock(i).parents.size() > 0) {
                int index = cfg.blocks.getBlock(i).parents.get(0).index;
                currentFrame = frames[i];

                if (handlerPCs.contains(inst.getOffset())) {
                    currentFrame.stack = new Stack<Type>();
                    currentFrame.stack.push(Type.createCastableType(ObjectType
                            .createTypeFromSingleTypeDescriptor("Ljava/lang/Exception;")));
                    cfg.blocks.getBlock(i).isExceptionHandler = true;
                } else {
                    currentFrame.stack = (Stack<Type>) frames[index].stack.clone();
                }
            } else {
                // first frame
                currentFrame = frames[0];
                currentFrame.stack = new Stack<Type>();
            }
            switch (inst.getOpcode()) {
            case 0: // nop
                break;

            case 1: // aconst_null
                pushT(Type.createUnknown());
                break;

            case 3: // iconst_0
            case 4: // iconst_1
                pushT(Type.createBoolean());
                break;

            case 2: // iconst_m1
            case 5: // iconst_2
            case 6: // iconst_3
            case 7: // iconst_4
            case 8: // iconst_5
                pushT(Type.createExactType(ObjectType.createTypeFromSingleTypeDescriptor("I")));
                break;

            case 9: // lconst_0
            case 10: // lconst_1
                pushT(Type.createExactType(ObjectType.createTypeFromSingleTypeDescriptor("J")));
                break;

            case 11: // fconst_0
            case 12: // fconst_1
            case 13: // fconst_2
                pushT(Type.createExactType(ObjectType.createTypeFromSingleTypeDescriptor("F")));
                break;

            case 14: // dconst_0
            case 15: // dconst_1
                pushT(Type.createExactType(ObjectType.createTypeFromSingleTypeDescriptor("D")));
                break;

            case 16: // bipush
            case 17: // sipush
                pushT(Type.createExactType(ObjectType.createTypeFromSingleTypeDescriptor("I")));
                break;

            case 18: // ldc
            case 19: // ldc_w
            {
                // special functions don't change the stack
                if (cfg.blocks.getSpecial(i)) {
                    break;
                }

                int index;
                {
                    if (inst.getOpcode() == 18) {
                        index = ((ImmediateByteInstruction) inst).getImmediateByte();
                    } else {
                        index = ((ImmediateShortInstruction) inst).getImmediateShort();
                    }
                }
                try {
                    switch (cfg.method.getClassFile().getConstantPoolEntry(index, CPInfo.class).getTag()) {
                    case CPInfo.CONSTANT_INTEGER:
                        // note: this musst be an integer, because a bool can be
                        // created a lot faster with iconst0/1
                        pushT(Type.createExactType(ObjectType.createTypeFromSingleTypeDescriptor("I")));
                        break;
                    case CPInfo.CONSTANT_FLOAT:
                        pushT(Type.createExactType(ObjectType.createTypeFromSingleTypeDescriptor("F")));
                        break;
                    case CPInfo.CONSTANT_STRING:
                        pushT(Type
                                .createCastableType(ObjectType.createTypeFromSingleTypeDescriptor("Ljava/lang/String;")));
                        break;

                    default:
                        throw new BytecodeCompilerException("illegal ldc argument: "
                                + cfg.method.getClassFile().getConstantPoolEntry(index, CPInfo.class).getTagVerbose());
                    }
                } catch (InvalidByteCodeException e) {
                    throw new BytecodeCompilerException("constant pool access failed", e);
                }
            }
                break;
            case 20: // ldc2_w
            {
                int index = ((ImmediateShortInstruction) inst).getImmediateShort();
                try {
                    switch (cfg.method.getClassFile().getConstantPoolEntry(index, CPInfo.class).getTag()) {
                    case CPInfo.CONSTANT_LONG:
                        pushT(Type.createExactType(ObjectType.createTypeFromSingleTypeDescriptor("J")));
                        break;
                    case CPInfo.CONSTANT_DOUBLE:
                        pushT(Type.createExactType(ObjectType.createTypeFromSingleTypeDescriptor("D")));
                        break;

                    default:
                        throw new BytecodeCompilerException("illegal ldc argument: "
                                + cfg.method.getClassFile().getConstantPoolEntry(index, CPInfo.class).getTagVerbose());
                    }
                } catch (InvalidByteCodeException e) {
                    throw new BytecodeCompilerException("constant pool access failed", e);
                }
            }
                break;

            case 21: // iload
            case 26: // iload_0
            case 27: // iload_1
            case 28: // iload_2
            case 29: // iload_3
            {
                int target = inst.getOpcode() == 21 ? ((ImmediateByteInstruction) inst).getImmediateByte() : (inst
                        .getOpcode() - 26);
                pushT(loadT(target));
                break;
            }

            case 22: // lload
            case 30: // lload_0
            case 31: // lload_1
            case 32: // lload_2
            case 33: // lload_3
            {
                int target = inst.getOpcode() == 22 ? ((ImmediateByteInstruction) inst).getImmediateByte() : (inst
                        .getOpcode() - 30);
                pushT(loadT(target));
                break;
            }

            case 23: // fload
            case 34: // fload_0
            case 35: // fload_1
            case 36: // fload_2
            case 37: // fload_3
            {
                int target = inst.getOpcode() == 23 ? ((ImmediateByteInstruction) inst).getImmediateByte() : (inst
                        .getOpcode() - 34);
                pushT(loadT(target));
                break;
            }

            case 24: // dload
            case 38: // dload_0
            case 39: // dload_1
            case 40: // dload_2
            case 41: // dload_3
            {
                int target = inst.getOpcode() == 24 ? ((ImmediateByteInstruction) inst).getImmediateByte() : (inst
                        .getOpcode() - 38);
                pushT(loadT(target));
                break;
            }

            case 25: // aload
            case 42: // aload_0
            case 43: // aload_1
            case 44: // aload_2
            case 45: // aload_3
            {
                int target = inst.getOpcode() == 25 ? ((ImmediateByteInstruction) inst).getImmediateByte() : (inst
                        .getOpcode() - 42);
                pushT(loadT(target));
                break;
            }

            case 46: // iaload
                popT(Type.createExactType(ObjectType.createInt()));
                popT(Type.createCastableType(ObjectType.createObject()));
                pushT(Type.createBoolean());
                break;
            case 47: // laload
                popT(Type.createExactType(ObjectType.createInt()));
                popT(Type.createCastableType(ObjectType.createObject()));
                pushT(Type.createExactType(ObjectType.createTypeFromSingleTypeDescriptor("J")));
                break;

            case 48: // faload
                popT(Type.createExactType(ObjectType.createInt()));
                popT(Type.createCastableType(ObjectType.createObject()));
                pushT(Type.createExactType(ObjectType.createTypeFromSingleTypeDescriptor("F")));
                break;

            case 49: // daload
                popT(Type.createExactType(ObjectType.createInt()));
                popT(Type.createCastableType(ObjectType.createObject()));
                pushT(Type.createExactType(ObjectType.createTypeFromSingleTypeDescriptor("D")));
                break;

            case 50: // aaload
            {
                popT(Type.createExactType(ObjectType.createInt()));
                Type t = popT(Type.createCastableType(ObjectType.createObject()));
                pushT(Type.createCastableType(ObjectType.createTypeFromSingleTypeDescriptor(t.getTypeName()
                        .getJVMType().substring(1))));
            }
                break;

            case 51: // baload
                popT(Type.createExactType(ObjectType.createInt()));
                popT(Type.createCastableType(ObjectType.createObject()));
                pushT(Type.createBoolean());
                break;

            case 52: // caload
                popT(Type.createExactType(ObjectType.createInt()));
                popT(Type.createCastableType(ObjectType.createObject()));
                pushT(Type.createExactType(ObjectType.createInt()));
                break;

            case 53: // saload
                popT(Type.createExactType(ObjectType.createInt()));
                popT(Type.createCastableType(ObjectType.createObject()));
                pushT(Type.createExactType(ObjectType.createInt()));
                break;

            case 54: // istore
            case 59: // istore_0
            case 60: // istore_1
            case 61: // istore_2
            case 62: // istore_3
            {
                int target = inst.getOpcode() == 54 ? ((ImmediateByteInstruction) inst).getImmediateByte() : (inst
                        .getOpcode() - 59);
                popT(storeT(target));
                break;
            }

            case 55: // lstore
            case 63: // lstore_0
            case 64: // lstore_1
            case 65: // lstore_2
            case 66: // lstore_3
            {
                int target = inst.getOpcode() == 55 ? ((ImmediateByteInstruction) inst).getImmediateByte() : (inst
                        .getOpcode() - 63);
                popT(storeT(target));
                break;
            }

            case 56: // fstore
            case 67: // fstore_0
            case 68: // fstore_1
            case 69: // fstore_2
            case 70: // fstore_3
            {
                int target = inst.getOpcode() == 56 ? ((ImmediateByteInstruction) inst).getImmediateByte() : (inst
                        .getOpcode() - 67);
                popT(storeT(target));
                break;
            }

            case 57: // dstore
            case 71: // dstore_0
            case 72: // dstore_1
            case 73: // dstore_2
            case 74: // dstore_3
            {
                int target = inst.getOpcode() == 57 ? ((ImmediateByteInstruction) inst).getImmediateByte() : (inst
                        .getOpcode() - 71);
                popT(storeT(target));
                break;
            }

            case 58: // astore
            case 75: // astore_0
            case 76: // astore_1
            case 77: // astore_2
            case 78: // astore_3
            {
                int target = inst.getOpcode() == 58 ? ((ImmediateByteInstruction) inst).getImmediateByte() : (inst
                        .getOpcode() - 75);
                popT(storeT(target));
                break;
            }

            case 84: // bastore
            case 79: // iastore
                popT(Type.createBoolean());
                popT(Type.createExactType(ObjectType.createInt()));
                popT(Type.createCastableType(ObjectType.createObject()));
                break;

            case 80: // lastore
                popT(Type.createExactType(ObjectType.createTypeFromSingleTypeDescriptor("J")));
                popT(Type.createExactType(ObjectType.createInt()));
                popT(Type.createCastableType(ObjectType.createObject()));
                break;

            case 81: // fastore
                popT(Type.createExactType(ObjectType.createTypeFromSingleTypeDescriptor("F")));
                popT(Type.createExactType(ObjectType.createInt()));
                popT(Type.createCastableType(ObjectType.createObject()));
                break;

            case 82: // dastore
                popT(Type.createExactType(ObjectType.createTypeFromSingleTypeDescriptor("d")));
                popT(Type.createExactType(ObjectType.createInt()));
                popT(Type.createCastableType(ObjectType.createObject()));
                break;

            case 83: // aastore
                popT(Type.createCastableType(ObjectType.createObject()));
                popT(Type.createExactType(ObjectType.createInt()));
                popT(Type.createCastableType(ObjectType.createObject()));
                break;

            case 85: // castore
            case 86: // sastore
                popT(Type.createExactType(ObjectType.createInt()));
                popT(Type.createExactType(ObjectType.createInt()));
                popT(Type.createCastableType(ObjectType.createObject()));
                break;

            case 87: // pop
            {
                Type t = peekT();
                if (t.isCategory2()) {
                    // can not happen
                    throw new BytecodeCompilerException("illegal byte code");
                } else {
                    popT(t);
                }
                break;
            }
            case 88: // pop2
            {
                Type t = peekT();
                if (t.isCategory2()) {
                    popT(t);
                } else {
                    popT(t);
                    t = peekT();
                    popT(t);
                }
                break;
            }

            case 89: // dup
            {
                pushT(peekT());
                break;
            }

            case 90: // dup_x1
            {
                Type t = peekT();
                popT(t);
                Type s = peekT();
                popT(s);
                pushT(t);
                pushT(s);
                pushT(t);
            }
                break;
            case 91: // dup_x2
            {
                Type t = peekT();
                popT(t);
                Type s = peekT();
                popT(s);
                if (s.isCategory2()) {
                    pushT(t);
                    pushT(s);
                    pushT(t);
                } else {
                    Type q = peekT();
                    popT(q);
                    pushT(t);
                    pushT(q);
                    pushT(s);
                    pushT(t);
                }
            }
                break;
            case 92: // dup2
            {
                Type t = peekT();
                if (t.isCategory2()) {
                    pushT(t);
                } else {
                    popT(t);
                    Type s = peekT();
                    pushT(t);
                    pushT(s);
                    pushT(t);
                }
            }
                break;
            case 93: // dup2_x1
            {
                Type t = peekT();
                popT(t);
                Type s = peekT();
                popT(s);
                if (t.isCategory2()) {
                    pushT(t);
                    pushT(s);
                    pushT(t);
                } else {
                    Type q = peekT();
                    popT(q);
                    pushT(s);
                    pushT(t);
                    pushT(q);
                    pushT(s);
                    pushT(t);
                }
            }
                break;
            case 94: // dup2_x2
            {
                Type t = peekT();
                popT(t);
                Type s = peekT();
                popT(s);
                if (t.isCategory2()) {
                    if (s.isCategory2()) {
                        pushT(t);
                        pushT(s);
                        pushT(t);
                    } else {
                        Type q = peekT();
                        popT(q);
                        pushT(t);
                        pushT(q);
                        pushT(s);
                        pushT(t);
                    }
                } else {
                    Type q = peekT();
                    popT(q);
                    if (q.isCategory2()) {
                        pushT(s);
                        pushT(t);
                        pushT(q);
                        pushT(s);
                        pushT(t);
                    } else {
                        Type p = peekT();
                        popT(p);

                        pushT(s);
                        pushT(t);
                        pushT(p);
                        pushT(q);
                        pushT(s);
                        pushT(t);
                    }
                }
            }
                break;

            case 95: // swap
            {
                Type s, t;
                s = peekT();
                popT(s);
                t = peekT();
                popT(t);
                pushT(s);
                pushT(t);
            }
                break;

            case 96: // iadd
            case 100: // isub
            case 104: // imul
            case 108: // idiv
            case 112: // irem
            case 120: // ishl
            case 122: // ishr
            case 124: // iushr
                popT(Type.createExactType(ObjectType.createInt()));
                popT(Type.createExactType(ObjectType.createInt()));
                pushT(Type.createExactType(ObjectType.createInt()));
                break;

            case 126: // iand
            case 128: // ior
            case 130: // ixor
            {
                Type s = popT(Type.createBoolean());
                Type t = popT(Type.createBoolean());
                Type u = pushT(Type.createBoolean());
                addConstraint(s, addConstraint(t, u, true), true);
            }
                break;

            case 97: // ladd
            case 101: // lsub
            case 105: // lmul
            case 109: // ldiv
            case 113: // lrem
            case 121: // lshl
            case 123: // lshr
            case 125: // lushr
            case 127: // land
            case 129: // lor
            case 131: // lxor
                popT(Type.createExactType(ObjectType.createTypeFromSingleTypeDescriptor("J")));
                popT(Type.createExactType(ObjectType.createTypeFromSingleTypeDescriptor("J")));
                pushT(Type.createExactType(ObjectType.createTypeFromSingleTypeDescriptor("J")));
                break;

            case 98: // fadd
            case 102: // fsub
            case 106: // fmul
            case 110: // fdiv
            case 114: // frem
                popT(Type.createExactType(ObjectType.createTypeFromSingleTypeDescriptor("F")));
                popT(Type.createExactType(ObjectType.createTypeFromSingleTypeDescriptor("F")));
                pushT(Type.createExactType(ObjectType.createTypeFromSingleTypeDescriptor("F")));
                break;

            case 99: // dadd
            case 103: // dsub
            case 107: // dmul
            case 111: // ddiv
            case 115: // drem
                popT(Type.createExactType(ObjectType.createTypeFromSingleTypeDescriptor("D")));
                popT(Type.createExactType(ObjectType.createTypeFromSingleTypeDescriptor("D")));
                pushT(Type.createExactType(ObjectType.createTypeFromSingleTypeDescriptor("D")));
                break;

            case 116: // ineg
                pushT(popT(Type.createExactType(ObjectType.createInt())));
                break;

            case 117: // lneg
                pushT(popT(Type.createExactType(ObjectType.createTypeFromSingleTypeDescriptor("J"))));
                break;

            case 118: // fneg
                pushT(popT(Type.createExactType(ObjectType.createTypeFromSingleTypeDescriptor("F"))));
                break;

            case 119: // dneg
                pushT(popT(Type.createExactType(ObjectType.createTypeFromSingleTypeDescriptor("D"))));
                break;

            case 132: // iinc
                // has the right type anyway, if compiled with -g
                break;

            case 133: // i2l
                popT(Type.createCastableType(ObjectType.createInt()));
                pushT(Type.createCastableType(ObjectType.createTypeFromSingleTypeDescriptor("J")));
                break;
            case 134: // i2f
                popT(Type.createCastableType(ObjectType.createInt()));
                pushT(Type.createCastableType(ObjectType.createTypeFromSingleTypeDescriptor("F")));
                break;
            case 135: // i2d
                popT(Type.createCastableType(ObjectType.createInt()));
                pushT(Type.createCastableType(ObjectType.createTypeFromSingleTypeDescriptor("D")));
                break;

            case 136: // l2i
                popT(Type.createCastableType(ObjectType.createTypeFromSingleTypeDescriptor("J")));
                pushT(Type.createCastableType(ObjectType.createTypeFromSingleTypeDescriptor("I")));
                break;
            case 137: // l2f
                popT(Type.createCastableType(ObjectType.createTypeFromSingleTypeDescriptor("J")));
                pushT(Type.createCastableType(ObjectType.createTypeFromSingleTypeDescriptor("F")));
                break;
            case 138: // l2d
                popT(Type.createCastableType(ObjectType.createTypeFromSingleTypeDescriptor("J")));
                pushT(Type.createCastableType(ObjectType.createTypeFromSingleTypeDescriptor("D")));
                break;

            case 139: // f2i
                popT(Type.createCastableType(ObjectType.createTypeFromSingleTypeDescriptor("F")));
                pushT(Type.createCastableType(ObjectType.createTypeFromSingleTypeDescriptor("I")));
                break;
            case 140: // f2l
                popT(Type.createCastableType(ObjectType.createTypeFromSingleTypeDescriptor("F")));
                pushT(Type.createCastableType(ObjectType.createTypeFromSingleTypeDescriptor("J")));
                break;
            case 141: // f2d
                popT(Type.createCastableType(ObjectType.createTypeFromSingleTypeDescriptor("F")));
                pushT(Type.createCastableType(ObjectType.createTypeFromSingleTypeDescriptor("D")));
                break;

            case 142: // d2i
                popT(Type.createCastableType(ObjectType.createTypeFromSingleTypeDescriptor("D")));
                pushT(Type.createCastableType(ObjectType.createTypeFromSingleTypeDescriptor("I")));
                break;
            case 143: // d2l
                popT(Type.createCastableType(ObjectType.createTypeFromSingleTypeDescriptor("D")));
                pushT(Type.createCastableType(ObjectType.createTypeFromSingleTypeDescriptor("J")));
                break;
            case 144: // d2f
                popT(Type.createCastableType(ObjectType.createTypeFromSingleTypeDescriptor("D")));
                pushT(Type.createCastableType(ObjectType.createTypeFromSingleTypeDescriptor("F")));
                break;

            case 145: // i2b
            case 146: // i2c
            case 147: // i2s
                // no constraint needed
                break;

            case 148: // lcmp
                popT(Type.createExactType(ObjectType.createTypeFromSingleTypeDescriptor("J")));
                popT(Type.createExactType(ObjectType.createTypeFromSingleTypeDescriptor("J")));
                pushT(Type.createExactType(ObjectType.createInt()));
                break;

            case 149: // fcmpl
            case 150: // fcmpg
                popT(Type.createExactType(ObjectType.createTypeFromSingleTypeDescriptor("F")));
                popT(Type.createExactType(ObjectType.createTypeFromSingleTypeDescriptor("F")));
                pushT(Type.createExactType(ObjectType.createInt()));
                break;

            case 151: // dcmpl
            case 152: // dcmpg
                popT(Type.createExactType(ObjectType.createTypeFromSingleTypeDescriptor("D")));
                popT(Type.createExactType(ObjectType.createTypeFromSingleTypeDescriptor("D")));
                pushT(Type.createExactType(ObjectType.createInt()));
                break;

            case 153: // ifeq
            case 154: // ifne
                popT(Type.createBoolean());
                break;

            case 155: // iflt
            case 156: // ifge
            case 157: // ifgt
            case 158: // ifle
                popT(Type.createExactType(ObjectType.createInt()));
                break;

            case 159: // if_icmpeq
            case 160: // if_icmpne
            case 161: // if_icmplt
            case 162: // if_icmpge
            case 163: // if_icmpgt
            case 164: // if_icmple
                popT(Type.createExactType(ObjectType.createInt()));
                popT(Type.createExactType(ObjectType.createInt()));
                break;

            case 165: // if_acmpeq
            case 166: // if_acmpne
                popT(Type.createExactType(ObjectType.createObject()));
                popT(Type.createExactType(ObjectType.createObject()));
                break;

            case 167: // goto
                break;

            case 170: // tableswitch
            case 171: // lookupswitch
                popT(Type.createExactType(ObjectType.createInt()));
                break;

            case 172: // ireturn
            case 173: // lreturn
            case 174: // freturn
            case 175: // dreturn
                try {
                    final String desc = cfg.method.getDescriptor();
                    popT(Type.createExactType(ObjectType.createTypeFromSingleTypeDescriptor(desc.substring(desc
                            .lastIndexOf(')') + 1))));
                } catch (InvalidByteCodeException e) {
                    throw new BytecodeCompilerException("missing return type?", e);
                }
                break;

            case 176: // areturn

                try {
                    final String desc = cfg.method.getDescriptor();
                    popT(Type.createExactType(ObjectType.createTypeFromSingleTypeDescriptor(desc.substring(desc
                            .lastIndexOf(')') + 1))));
                } catch (InvalidByteCodeException e) {
                    throw new BytecodeCompilerException("missing return type?", e);
                }
                break;

            case 177: // return
                break;

            case 178: // getstatic
                pushT(getFieldType((ImmediateShortInstruction) inst));
                break;

            case 179: // putstatic
                popT(getFieldType((ImmediateShortInstruction) inst));
                break;

            case 180: // getfield
                popT(Type.createCastableType(ObjectType.createObject()));
                pushT(getFieldType((ImmediateShortInstruction) inst));
                break;

            case 181: // putfield
                popT(getFieldType((ImmediateShortInstruction) inst));
                popT(Type.createCastableType(ObjectType.createObject()));
                break;

            case 182: // invokevirtual
            case 183: // invokespecial
            case 185: // invokeinterface
            {
                ImmediateShortInstruction instruction = (ImmediateShortInstruction) inst;
                ConstantReference info = (ConstantReference) cfg.method.getClassFile().getConstantPool()[instruction
                        .getImmediateShort()];
                NameResolver resolver = cfg.proofObligation.resolver;

                try {
                    ClassFile targetClass = resolver.requestClass(info.getClassInfo().getName());
                    MethodName callee = resolver.resolveMethodName(targetClass, info.getNameAndTypeInfo().getName(),
                            info.getNameAndTypeInfo().getDescriptor());

                    MethodType signature = callee.getSignature(targetClass);
                    for (int j = signature.getArgumentTypes().size() - 1; j >= 0; j--) {
                        popT(Type.createExactType(signature.getArgumentTypes().get(j)));
                    }

                    popT(Type.createExactType(ObjectType.createTypeFromBytecodeClass(resolver.requestClass(callee
                            .getBytecodeClassName()))));

                    if (!"V".equals(signature.getResultType().getJVMType())) {
                        pushT(Type.createExactType(signature.getResultType()));
                    }

                } catch (InvalidByteCodeException e) {
                    throw new BytecodeCompilerException("call target not found", e);
                }
            }
                break;

            case 184: // invokestatic
                // special functions don't change the stack
                if (cfg.blocks.getSpecial(i)) {
                    break;
                }

                ImmediateShortInstruction instruction = (ImmediateShortInstruction) inst;
                ConstantMethodrefInfo info = (ConstantMethodrefInfo) cfg.method.getClassFile().getConstantPool()[instruction
                        .getImmediateShort()];
                NameResolver resolver = cfg.proofObligation.resolver;

                try {
                    ClassFile targetClass = resolver.requestClass(info.getClassInfo().getName());
                    MethodName callee = resolver.resolveMethodName(targetClass, info.getNameAndTypeInfo().getName(),
                            info.getNameAndTypeInfo().getDescriptor());

                    MethodType signature = callee.getSignature(targetClass);
                    for (int j = signature.getArgumentTypes().size() - 1; j >= 0; j--) {
                        popT(Type.createExactType(signature.getArgumentTypes().get(j)));
                    }

                    if (!"V".equals(signature.getResultType().getJVMType())) {
                        pushT(Type.createExactType(signature.getResultType()));
                    }

                } catch (InvalidByteCodeException e) {
                    throw new BytecodeCompilerException("call target not found", e);
                }
                break;
            case 186: // invokedynamic
                // invokedynamic is currently specified as unused opcode!
                break;

            case 187: // new
            {
                try {
                    pushT(Type
                            .createCastableType(ObjectType.createTypeFromBytecodeClass(cfg.proofObligation.resolver
                                    .requestClass(((ConstantClassInfo) cfg.method.getClassFile().getConstantPool()[((ImmediateShortInstruction) inst)
                                            .getImmediateShort()]).getName()))));
                    // we are unable to get the signature for this type; it is
                    // therefore important to infer it somewhere else
                    // anm.: is it? maybe we can get the signature direcly from
                    // the class
                } catch (InvalidByteCodeException e) {
                    throw new BytecodeCompilerException("failed to set descriptor", e);
                }
            }
                break;

            case 188: // newarray
            {
                popT(Type.createExactType(ObjectType.createInt()));
                String type;
                switch(((ImmediateByteInstruction) inst).getImmediateByte()){
                default:
                case 4:
                    type = "[Z";
                    break;
                case 5:
                    type = "[C";
                    break;
                case 6:
                    type = "[F";
                    break;
                case 7:
                    type = "[D";
                    break;
                case 8:
                    type = "[B";
                    break;
                case 9:
                    type = "[S";
                    break;
                case 10:
                    type = "[I";
                    break;
                case 11:
                    type = "[J";
                    break;
                }
                ClassType name = cfg.proofObligation.resolver.requestBaseTypeArray(type);
                pushT(Type.createCastableType(name.toObjectType()));
                break;
            }

            case 189: // anewarray
            {
                popT(Type.createExactType(ObjectType.createInt()));
                try {
                    // fix: the fucking bytecode format contains a descriptor,
                    // if the argument type is an array and a class name, if the
                    // the type is not an array
                    final String type = ((ConstantClassInfo) cfg.method.getClassFile().getConstantPool()[((ImmediateShortInstruction) inst)
                            .getImmediateShort()]).getName();
                    if ('[' == type.charAt(0)) {
                        pushT(Type.createCastableType(ObjectType.createTypeFromSingleTypeDescriptor("[" + type)));
                    }
                    else {
                        pushT(Type.createCastableType(ObjectType.createTypeFromSingleTypeDescriptor("[L" + type + ";")));
                    // ref arrays dont get a signature, they are not generic
                    }
                } catch (InvalidByteCodeException e) {
                    throw new BytecodeCompilerException("failed to set descriptor", e);
                }
            }
                break;

            case 190: // arraylength
                popT(Type.createCastableType(ObjectType.createObject()));
                pushT(Type.createExactType(ObjectType.createInt()));
                break;

            case 191: // athrow
                popT(Type.createExactType(ObjectType.createTypeFromBytecodeClass(cfg.proofObligation.resolver
                        .requestClass("java/lang/Throwable"))));
                break;

            case 192: // checkcast
                try {
                    // although checkcast does not actually pop an object from
                    // the stack, the type of the object is not connected to the
                    // type after the checkcast, because before a checkcast, the
                    // type is allowed to be arbitrary and after we KNOW that it
                    // is at least the type argument of the checkcast
                    // instruction
                    popT(Type.createCastableType(ObjectType.createObject()));

                    // push ClassName<?,?, ... ?>, because we can not check for
                    // generic types directly
                    ClassType type = ClassType.createTypeFromBytecodeClass(cfg.proofObligation.resolver
                            .requestClass(((ConstantClassInfo) cfg.method
                            .getClassFile().getConstantPoolEntry(
                                    ((ImmediateShortInstruction) inst).getImmediateShort(), ConstantClassInfo.class))
                                    .getName()));

                    // inst = {wildcard, wildcard...}
                    ArrayList<String> inst = new ArrayList<String>(type.getFreeTypeVariables().size());
                    for (int j = type.getFreeTypeVariables().size(); j > 0; j--) {
                        inst.add("*");
                    }

                    pushT(Type.createExactType(type.toObjectType(inst)));

                } catch (InvalidByteCodeException e1) {
                    e1.printStackTrace();
                }
                break;

            case 193: // instanceof
                popT(Type.createCastableType(ObjectType.createObject()));
                pushT(Type.createBoolean());
                break;

            case 194: // monitorenter
            case 195: // monitorexit
                popT(Type.createCastableType(ObjectType.createObject()));
                break;

            case 196: // wide
                break;

            case 197: // multianewarray
            {
                int dim = ((MultianewarrayInstruction) inst).getDimensions();
                String prefix = "";
                while (dim > 0) {
                    dim--;
                    popT(Type.createExactType(ObjectType.createInt()));
                    prefix = "[" + prefix;
                }
                try {
                    pushT(Type
                            .createExactType(ObjectType
                                    .createTypeFromSingleTypeDescriptor(prefix
                                            + ((ConstantClassInfo) cfg.method.getClassFile().getConstantPool()[((ImmediateShortInstruction) inst)
                                                    .getImmediateShort()]).getName())));
                } catch (InvalidByteCodeException e) {
                    throw new BytecodeCompilerException("could not find argument type in multianewarray instruction", e);
                }
            }
                break;

            case 198: // ifnull
            case 199: // ifnonnull
                popT(Type.createCastableType(ObjectType.createObject()));
                break;

            case 200: // goto_w
                break;
            }
        }
    }

    private Type getFieldType(ImmediateShortInstruction instruction) throws BytecodeCompilerException {
        ConstantFieldrefInfo info = (ConstantFieldrefInfo) cfg.method.getClassFile().getConstantPool()[instruction
                .getImmediateShort()];

        try {
            return Type.createExactType(ObjectType.createTypeFromSingleTypeDescriptor(info.getNameAndTypeInfo()
                    .getDescriptor()));
        } catch (InvalidByteCodeException e) {
            throw new BytecodeCompilerError(e);
        }
    }

    /**
     * indicates that a local variable is read. The returend type is the type of
     * that local variable.
     */
    private Type loadT(int target) throws BytecodeCompilerException {
        Type result = currentFrame.localVariables[target];

        return result;
    }

    /**
     * indicates that a local variable is written. The returend type is the type
     * of that local variable at the next instruction index, i.e. the minimal
     * type that might be assigned to this local variable.
     */
    private Type storeT(int target) throws BytecodeCompilerException {
        // use that store operations do not jump
        return frames[index + 1].localVariables[target];
    }

    /**
     * takes push and pop into account
     *
     * @return the type that is currently on top of the stack
     */
    private Type peekT() {
        return currentFrame.stack.peek();
    }

    /**
     * takes push and pop into account
     *
     * @return the top type from the stack removing it
     */
    private Type popT(Type type) throws BytecodeCompilerException {
        Type target = currentFrame.stack.pop();
        addConstraint(type, target, true);
        return target;
    }

    /**
     * takes push and pop into account
     *
     * @param type
     *            pushes that type on top of the stack
     * @return the same type that has been pushed; for further use
     */
    private Type pushT(Type type) {
        currentFrame.stack.push(type);
        return type;
    }

    /**
     * Add a constraint and return one argument to allow for direct adding of
     * other constraints.
     *
     * @return target
     */
    private Type addConstraint(Type source, Type target, boolean isSameType) {
        typeConstraints.add(new Constraint(source, target, isSameType));
        return target;
    }
}
