package de.uka.iti.ivil.jbc.environment.cfg;

import org.gjt.jclasslib.bytecode.AbstractInstruction;
import org.gjt.jclasslib.structures.AbstractStructure;
import org.gjt.jclasslib.structures.CPInfo;
import org.gjt.jclasslib.structures.attributes.CodeAttribute;
import org.gjt.jclasslib.structures.attributes.LocalVariableCommonAttribute;
import org.gjt.jclasslib.structures.attributes.LocalVariableCommonEntry;
import org.gjt.jclasslib.structures.attributes.LocalVariableTableAttribute;
import org.gjt.jclasslib.structures.constants.ConstantUtf8Info;

import de.uka.iti.ivil.jbc.environment.BytecodeCompilerException;
import de.uka.iti.ivil.jbc.util.EscapeName;
import de.uka.iti.ivil.jbc.util.ObjectType;

/**
 * Provides instruction based type and name information for registers. It also
 * provides a mechanism to solve constraints.
 * 
 * @author timm.felden@felden.com
 */
class RegisterLayout {
    private final ControlFlowGraph cfg;
    // store variable names
    private final String[][] nameStore;
    // store base types
    private final ObjectType[][] typeStore;

    RegisterLayout(ControlFlowGraph controlFlowGraph, TypeFrame[] frames) throws BytecodeCompilerException {
        this.cfg = controlFlowGraph;
        CodeAttribute code = (CodeAttribute) controlFlowGraph.method.findAttribute(CodeAttribute.class);
        LocalVariableTableAttribute lvars = (LocalVariableTableAttribute) code
                .findAttribute(LocalVariableTableAttribute.class);

        typeStore = new ObjectType[cfg.instructions.length][code.getMaxLocals()];
        nameStore = new String[typeStore.length][code.getMaxLocals()];

        // copy types
        for (int i = 0; i < frames.length; i++)
            for (int j = 0; j < frames[i].localVariables.length; j++)
                if (null != frames[i].localVariables[j])
                    typeStore[i][j] = frames[i].localVariables[j].getTypeName();

        buildNames(code, lvars);

        // try {
        // TypeSignature signature =
        // TypeSignature.createTypeSignatureFromDescriptor(controlFlowGraph.method
        // .getDescriptor());
        // int index = 0;
        // if (0 == (controlFlowGraph.method.getAccessFlags() &
        // AccessFlags.ACC_STATIC)) {
        // set(0,
        // 0,
        // new
        // Type(TypeName.createTypeFromBytecodeClassName(cfg.method.getClassFile().getThisClassName())));
        // index++;
        // }
        // for (TypeName type : signature.getArgumentTypes()) {
        //
        // IntermediateType t;
        // switch (type.getJavaType()) {
        // case "boolean":
        // t = IntermediateType.bool;
        // break;
        //
        // case "char":
        // case "int":
        // case "short":
        // case "byte":
        // t = IntermediateType._int;
        // break;
        //
        // case "long":
        // t = IntermediateType._long;
        // break;
        //
        // case "double":
        // t = IntermediateType._double;
        // break;
        //
        // case "float":
        // t = IntermediateType._float;
        // break;
        //
        // case "void":
        // throw new
        // ByteCodeCompilerException("can not happen: you are fucked");
        //
        // default:
        // t = IntermediateType.ref;
        // set(0, index, new Type(type));
        // break;
        //
        // }
        // if (t != IntermediateType.ref)
        // set(0, index, new Type(t));
        // if (t == IntermediateType._long || t == IntermediateType._double)
        // index += 2;
        // else
        // index++;
        // }
        //
        // buildInitialNames();
        //
        // } catch (InvalidByteCodeException e) {
        // throw new
        // ByteCodeCompilerException("failed to read method descriptor", e);
        // }
    }

    public String get(int i) {
        return nameStore[cfg.instructionIndex][i];
    }

    public String get(int i, int targetIndex) {
        return nameStore[targetIndex][i];
    }

    public ObjectType type(int i) {
        return typeStore[cfg.instructionIndex][i];
    }

    ObjectType[] getTypesAtInstruction(int instructionIndex) {
        return typeStore[instructionIndex];
    }

    // public void set(int pc, int i, Type type) throws
    // ByteCodeCompilerException {
    // registertypes[i] = type;
    //
    // if (null != lvars) {
    // for (LocalVariableCommonEntry entry : lvars.getLocalVariableEntries()) {
    // if (i != entry.getIndex())
    // continue;
    //
    // if (entry.getStartPc() <= pc && pc < entry.getStartPc() +
    // entry.getLength()) {
    // try {
    // String desc = code.getClassFile().getConstantPoolEntryName(
    // entry.getDescriptorOrSignatureIndex());
    // switch (desc) {
    //
    // case "Z":
    // type.setType(IntermediateType.bool);
    // return;
    // case "B":
    // case "C":
    // case "I":
    // case "S":
    // type.setType(IntermediateType._int);
    // return;
    // case "J":
    // type.setType(IntermediateType._long);
    // return;
    //
    // case "D":
    // type.setType(IntermediateType._double);
    // return;
    // case "F":
    // type.setType(IntermediateType._float);
    // return;
    //
    // case "V":
    // return;
    //
    // default:
    // type.setType(IntermediateType.ref);
    // type.setDescriptor(desc);
    // // ensure presence of used class
    // String clsName = desc.replace("[", "");
    // if (clsName.startsWith("L")) {
    // clsName = clsName.substring(1, clsName.length() - 1);
    // cfg.proofObligation.resolver.requestClass(clsName);
    // }
    // return;
    // }
    // } catch (InvalidByteCodeException e) {
    // throw new
    // ByteCodeCompilerException("name lookup for local variable failed", e);
    // }
    // }
    // }
    // }
    // // TODO if we found a descriptor, try to find a signature as well
    // // if (null != ltvars && null != type.getDescriptor()) {
    // // for (LocalVariableCommonEntry entry :
    // // ltvars.getLocalVariableEntries()) {
    // // if (i != entry.getIndex())
    // // continue;
    // //
    // // if (entry.getStartPc() <= pc && pc < entry.getStartPc() +
    // // entry.getLength()) {
    // // String sig;
    // // try {
    // // sig =
    // //
    // ltvars.getClassFile().getConstantPoolUtf8Entry(entry.getDescriptorOrSignatureIndex())
    // // .getString();
    // // } catch (InvalidByteCodeException e) {
    // // throw new ByteCodeCompilerException("signature not found", e);
    // // }
    // //
    // // type.setSignature(SignatureParser.make().parseTypeSig(sig));
    // // return;
    // // }
    // // }
    // // }
    // }

    void initializeWith(int index) {
        // registertypes = typeStore[index].clone();
    }

    private void buildNames(AbstractStructure code, LocalVariableCommonAttribute lvars)
            throws BytecodeCompilerException {

        // create names from local variable table entries
        if (null != lvars) {
            AbstractInstruction[] instructions = cfg.instructions;
            CPInfo[] cp = code.getClassFile().getConstantPool();
            for (LocalVariableCommonEntry entry : lvars.getLocalVariableEntries()) {
                for (int i = 0; i < nameStore.length; i++) {
                    // skip indices which dont match the current handler
                    if (entry.getStartPc() - 1 <= instructions[i].getOffset()
                            && instructions[i].getOffset() < (entry.getStartPc() + entry.getLength()))
                        nameStore[i][entry.getIndex()] = "R_"
                                    + EscapeName.build(((ConstantUtf8Info) cp[entry.getNameIndex()]).getString())
                                + "_" + typeStore[i][entry.getIndex()].getBaseType();
                }
            }
        }

        // create names for yet unassigned names
        for (int i = 0; i < nameStore.length; i++) {
            for (int j = 0; j < nameStore[i].length; j++) {
                // skip names, which are never used anywhere
                if (null == typeStore[i][j])
                    continue;

                if (null == nameStore[i][j])
                    nameStore[i][j] = "R_" + j + "_" + typeStore[i][j].getBaseType();

                // create function names
                final String name = nameStore[i][j];

                cfg.proofObligation.requestFunction(name, typeStore[i][j].getBaseType(), "assignable");
            }
        }
    }

}