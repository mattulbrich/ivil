package de.uka.iti.ivil.jbc.environment.cfg;

import java.util.Arrays;

import org.gjt.jclasslib.bytecode.AbstractBranchInstruction;
import org.gjt.jclasslib.bytecode.AbstractInstruction;
import org.gjt.jclasslib.bytecode.ImmediateByteInstruction;
import org.gjt.jclasslib.bytecode.ImmediateShortInstruction;
import org.gjt.jclasslib.bytecode.LookupSwitchInstruction;
import org.gjt.jclasslib.bytecode.MatchOffsetPair;
import org.gjt.jclasslib.bytecode.TableSwitchInstruction;
import org.gjt.jclasslib.structures.CPInfo;
import org.gjt.jclasslib.structures.InvalidByteCodeException;
import org.gjt.jclasslib.structures.attributes.CodeAttribute;
import org.gjt.jclasslib.structures.attributes.ExceptionTableEntry;
import org.gjt.jclasslib.structures.constants.ConstantMethodrefInfo;

import de.uka.iti.ivil.jbc.environment.BytecodeCompilerException;

/**
 * adds basic control flow graph for cfg under construction
 * 
 * @author timm.felden@felden.com
 * 
 */
final class CFGBlocksFactory {

    private final ControlFlowGraph cfg;

    /**
     * create block factory, which automatically adds block information to cfg.
     */
    public CFGBlocksFactory(ControlFlowGraph cfg) throws BytecodeCompilerException {
        this.cfg = cfg;

        addBlocks();
    }

    @SuppressWarnings("unchecked")
    private void addBlocks() throws BytecodeCompilerException {
        for (int i = 0; i < cfg.instructions.length; i++) {
            final AbstractInstruction inst = cfg.instructions[i];
            final int pc = inst.getOffset();
            switch (inst.getOpcode()) {
            case 0: // nop
            case 1: // aconst_null
            case 2: // iconst_m1
            case 3: // iconst_0
            case 4: // iconst_1
            case 5: // iconst_2
            case 6: // iconst_3
            case 7: // iconst_4
            case 8: // iconst_5
            case 9: // lconst_0
            case 10: // lconst_1
            case 11: // fconst_0
            case 12: // fconst_1
            case 13: // fconst_2
            case 14: // dconst_0
            case 15: // dconst_1
            case 16: // bipush
            case 17: // sipush
            case 20: // ldc2_w
            case 21: // iload
            case 22: // lload
            case 23: // fload
            case 24: // dload
            case 25: // aload
            case 26: // iload_0
            case 27: // iload_1
            case 28: // iload_2
            case 29: // iload_3
            case 30: // lload_0
            case 31: // lload_1
            case 32: // lload_2
            case 33: // lload_3
            case 34: // fload_0
            case 35: // fload_1
            case 36: // fload_2
            case 37: // fload_3
            case 38: // dload_0
            case 39: // dload_1
            case 40: // dload_2
            case 41: // dload_3
            case 42: // aload_0
            case 43: // aload_1
            case 44: // aload_2
            case 45: // aload_3
            case 46: // iaload
            case 47: // laload
            case 48: // faload
            case 49: // daload
            case 50: // aaload
            case 51: // baload
            case 52: // caload
            case 53: // saload
            case 54: // istore
            case 55: // lstore
            case 56: // fstore
            case 57: // dstore
            case 58: // astore
            case 59: // istore_0
            case 60: // istore_1
            case 61: // istore_2
            case 62: // istore_3
            case 63: // lstore_0
            case 64: // lstore_1
            case 65: // lstore_2
            case 66: // lstore_3
            case 67: // fstore_0
            case 68: // fstore_1
            case 69: // fstore_2
            case 70: // fstore_3
            case 71: // dstore_0
            case 72: // dstore_1
            case 73: // dstore_2
            case 74: // dstore_3
            case 75: // astore_0
            case 76: // astore_1
            case 77: // astore_2
            case 78: // astore_3
            case 79: // iastore
            case 80: // lastore
            case 81: // fastore
            case 82: // dastore
            case 83: // aastore
            case 84: // bastore
            case 85: // castore
            case 86: // sastore
            case 87: // pop
            case 88: // pop2
            case 89: // dup
            case 90: // dup_x1
            case 91: // dup_x2
            case 92: // dup2
            case 93: // dup2_x1
            case 94: // dup2_x2
            case 95: // swap
            case 96: // iadd
            case 97: // ladd
            case 98: // fadd
            case 99: // dadd
            case 100: // isub
            case 101: // lsub
            case 102: // fsub
            case 103: // dsub
            case 104: // imul
            case 105: // lmul
            case 106: // fmul
            case 107: // dmul
            case 108: // idiv
            case 109: // ldiv
            case 110: // fdiv
            case 111: // ddiv
            case 112: // irem
            case 113: // lrem
            case 114: // frem
            case 115: // drem
            case 116: // ineg
            case 117: // lneg
            case 118: // fneg
            case 119: // dneg
            case 120: // ishl
            case 121: // lshl
            case 122: // ishr
            case 123: // lshr
            case 124: // iushr
            case 125: // lushr
            case 126: // iand
            case 127: // land
            case 128: // ior
            case 129: // lor
            case 130: // ixor
            case 131: // lxor
            case 132: // iinc
            case 133: // i2l
            case 134: // i2f
            case 135: // i2d
            case 136: // l2i
            case 137: // l2f
            case 138: // l2d
            case 139: // f2i
            case 140: // f2l
            case 141: // f2d
            case 142: // d2i
            case 143: // d2l
            case 144: // d2f
            case 145: // i2b
            case 146: // i2c
            case 147: // i2s

            case 148: // lcmp
            case 149: // fcmpl
            case 150: // fcmpg
            case 151: // dcmpl
            case 152: // dcmpg
                cfg.blocks.addBlock(i, pc + inst.getSize());
                break;

            case 18: // ldc
            case 19: // ldc_w
            {
                // check for
                // "lcd string; ivokestatic de/uka/iti/ivil/jbc/Spec/special"

                int index;
                if (cfg.instructions[i].getOpcode() != AbstractInstruction.OPCODE_LDC)
                    index = ((ImmediateShortInstruction) cfg.instructions[i]).getImmediateShort();
                else
                    index = ((ImmediateByteInstruction) cfg.instructions[i]).getImmediateByte();

                CPInfo entry;
                try {
                    entry = cfg.method.getClassFile().getConstantPoolEntry(index, CPInfo.class);
                } catch (InvalidByteCodeException e) {
                    throw new BytecodeCompilerException("could not find constant", e);
                }
                if (entry.getTag() == CPInfo.CONSTANT_STRING
                        && cfg.instructions[i + 1].getOpcode() == AbstractInstruction.OPCODE_INVOKESTATIC) {
                    // control flow can not end at an ldc, thus there has to be
                    // a next instruction

                    ImmediateShortInstruction instruction = (ImmediateShortInstruction) cfg.instructions[i + 1];
                    ConstantMethodrefInfo info = (ConstantMethodrefInfo) cfg.method.getClassFile().getConstantPool()[instruction
                            .getImmediateShort()];
                    try {
                        if (info.getClassInfo().getName().equals("de/uka/iti/ivil/jbc/Spec")
                                && info.getNameAndTypeInfo().getName().equals("special")) {

                            cfg.blocks.addBlock(i, pc + inst.getSize());
                            cfg.blocks.setSpecial(i);
                            cfg.blocks.setSpecial(i + 1);
                            break;
                        }

                    } catch (InvalidByteCodeException e) {
                        throw new BytecodeCompilerException("failed to read method name", e);
                    }

                }
                // if that did not work out, we just load a constant
                cfg.blocks.addBlock(i, pc + inst.getSize());
            }
                break;

            case 153: // ifeq
            case 154: // ifne
            case 155: // iflt
            case 156: // ifge
            case 157: // ifgt
            case 158: // ifle
            case 159: // if_icmpeq
            case 160: // if_icmpne
            case 161: // if_icmplt
            case 162: // if_icmpge
            case 163: // if_icmpgt
            case 164: // if_icmple
            case 165: // if_acmpeq
            case 166: // if_acmpne
                cfg.blocks.addBlock(i, pc + inst.getSize(), pc + ((AbstractBranchInstruction) inst).getBranchOffset());
                break;

            case 167: // goto
                cfg.blocks.addBlock(i, pc + ((AbstractBranchInstruction) inst).getBranchOffset());
                break;

            case 168: // jsr
            case 169: // ret
                throw new BytecodeCompilerException("ruled out by jbc version 51");

            case 170: // tableswitch
            {
                TableSwitchInstruction instruction = (TableSwitchInstruction) inst;

                int[] targets = new int[1 + instruction.getJumpOffsets().length];

                int j = 0;
                for (; j < instruction.getJumpOffsets().length; j++) {
                    targets[j] = pc + instruction.getJumpOffsets()[j];
                }
                targets[j] = pc + instruction.getDefaultOffset();

                cfg.blocks.addBlock(i, targets);
                break;
            }

            case 171: // lookupswitch
            {
                LookupSwitchInstruction instruction = (LookupSwitchInstruction) inst;

                int[] targets = new int[1 + instruction.getMatchOffsetPairs().size()];

                int index = 0;
                targets[index++] = pc + instruction.getDefaultOffset();
                for (MatchOffsetPair pair : (java.util.List<MatchOffsetPair>) instruction.getMatchOffsetPairs()) {
                    targets[index++] = pc + pair.getOffset();
                }
                cfg.blocks.addBlock(i, targets);
                break;
            }

            case 172: // ireturn
            case 173: // lreturn
            case 174: // freturn
            case 175: // dreturn
            case 176: // areturn
            case 177: // return
                cfg.blocks.addBlock(i);
                break;
            case 178: // getstatic
            case 179: // putstatic
            case 180: // getfield
            case 181: // putfield
                cfg.blocks.addBlock(i, pc + inst.getSize());
                break;

            // in this context, all legal invoke operations behave in the same
            // way
            case 182: // invokevirtual
            case 183: // invokespecial
            case 184: // invokestatic
            case 185: // invokeinterface
                // special functions may jump to an exception handler, however
                // this behavior is discouraged

                int[] t = getExceptionHandlers(inst.getOffset());
                int[] targets = Arrays.copyOf(t, t.length + 1);
                targets[t.length] = pc + inst.getSize();
                cfg.blocks.addBlock(i, targets);
                break;

            case 186: // invokedynamic
                throw new BytecodeCompilerException("invokedynamic is not yet supported");

            case 187: // new
            case 188: // newarray
            case 189: // anewarray
            case 190: // arraylength
                cfg.blocks.addBlock(i, pc + inst.getSize());
                break;

            case 191: // athrow
                cfg.blocks.addBlock(i, getExceptionHandlers(inst.getOffset()));
                break;

            case 192: // checkcast
            case 193: // instanceof
                cfg.blocks.addBlock(i, pc + inst.getSize());
                break;

            case 194: // monitorenter
            case 195: // monitorexit
                cfg.blocks.addBlock(i, pc + inst.getSize());
                break;

            case 196: // wide
                cfg.blocks.addBlock(i, pc + inst.getSize());
                break;

            case 197: // multianewarray
                cfg.blocks.addBlock(i, pc + inst.getSize());
                break;

            case 198: // ifnull
            case 199: // ifnonnull
                cfg.blocks.addBlock(i, pc + inst.getSize(), pc + ((AbstractBranchInstruction) inst).getBranchOffset());
                break;

            case 200: // goto_w
                cfg.blocks.addBlock(i, pc + ((AbstractBranchInstruction) inst).getBranchOffset());
                break;

            case 201: // jsr_w
                throw new BytecodeCompilerException("ruled out by jbc version 51");

            default:
                throw new BytecodeCompilerException("illegal opcode: " + inst.getOpcode());
            }
        }
    }

    private int[] getExceptionHandlers(int pc) {
        CodeAttribute code = ((CodeAttribute) cfg.method.findAttribute(CodeAttribute.class));

        int length = 0;
        for (ExceptionTableEntry entry : code.getExceptionTable())
            if (entry.getStartPc() <= cfg.pc() && cfg.pc() < entry.getEndPc())
                length++;

        int[] result = new int[length];
        int next = 0;
        for (ExceptionTableEntry entry : code.getExceptionTable())
            if (entry.getStartPc() <= cfg.pc() && cfg.pc() < entry.getEndPc())
                result[next++] = entry.getHandlerPc();

        return result;
    }

}
