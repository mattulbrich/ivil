package de.uka.iti.ivil.jbc.environment.cfg;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;

import org.gjt.jclasslib.bytecode.AbstractInstruction;
import org.gjt.jclasslib.bytecode.ImmediateByteInstruction;
import org.gjt.jclasslib.bytecode.ImmediateShortInstruction;
import org.gjt.jclasslib.io.ByteCodeReader;
import org.gjt.jclasslib.structures.CPInfo;
import org.gjt.jclasslib.structures.InvalidByteCodeException;
import org.gjt.jclasslib.structures.MethodInfo;
import org.gjt.jclasslib.structures.attributes.CodeAttribute;
import org.gjt.jclasslib.structures.attributes.LineNumberTableAttribute;
import org.gjt.jclasslib.structures.attributes.LineNumberTableEntry;
import org.gjt.jclasslib.structures.constants.ConstantStringInfo;

import de.uka.iti.ivil.jbc.environment.BytecodeCompilerException;
import de.uka.iti.ivil.jbc.environment.ConcreteProofObligation;
import de.uka.iti.ivil.jbc.util.MethodName;
import de.uka.iti.ivil.jbc.util.ObjectType;

/**
 * This class models a control flow graph plus various informations that can be
 * obtained while building it. These include stack layout, register layout (and
 * renaming into local variable names) and types of values.
 * 
 * @note using the reg, push, pop and peek operations provided by this class is
 *       obligatory
 * 
 * @author timm.felden@felden.com
 * 
 */
final public class ControlFlowGraph {

    // written later by the factory
    RegisterLayout registers;

    // written later by the factory
    StackLayout stack;

    final BlockLayout blocks;

    /**
     * The method under translation
     */
    public final MethodInfo method;
    public final MethodName methodName;

    /**
     * the program counter of the currently translated operation
     */
    int currentPc = 0;

    private AbstractInstruction currentInstruction;
    int instructionIndex;
    final AbstractInstruction[] instructions;
    /**
     * It is important to keep track of already translated instructions, as
     * jumps can cause already translated blocks to be split into two new
     * blocks. This however does not affect the translation of the instructions.
     */
    private boolean[] translatedPCOffsets;

    final public StatementList statements;
    final ConcreteProofObligation<?> proofObligation;

    /**
     * Create a control flow graph for the method under translation, that can be
     * used to translate opcodes.
     * 
     * @param method
     *            the method to be translated
     */
    @SuppressWarnings("unchecked")
    ControlFlowGraph(MethodInfo method, ConcreteProofObligation<?> po) throws BytecodeCompilerException {
        this.method = method;
        this.methodName = MethodName.createFromClassFile(method.getClassFile(), method);
        this.proofObligation = po;
        try {
            ArrayList<AbstractInstruction> code = ByteCodeReader.readByteCode(((CodeAttribute) method
                    .findAttribute(CodeAttribute.class)).getCode());
            instructions = code.toArray(new AbstractInstruction[code.size()]);
        } catch (IOException e) {
            throw new BytecodeCompilerException("creating cfg for methods without code is illegal!", e);
        }
        instructionIndex = 0;
        currentInstruction = instructions[0];
        translatedPCOffsets = new boolean[((CodeAttribute) method.findAttribute(CodeAttribute.class)).getCode().length];
        for (int i = 0; i < translatedPCOffsets.length; i++)
            translatedPCOffsets[i] = false;

        this.statements = new StatementList();
        this.blocks = new BlockLayout(this);
    }

    /**
     * Writes all created statements to the corresponding proof obligation.
     */
    public void writeStatements() throws BytecodeCompilerException {
        proofObligation.content.append(statements.build());
    }

    /**
     * @return the current program counter
     */
    public int pc() {
        return currentPc;
    }

    /**
     * @return the current instruction or null, if no current instruction exists
     */
    public AbstractInstruction getCurrentInstruction() {
        return currentInstruction;
    }

    /**
     * call this, if the translation of the current instruction is finished.
     * 
     * @return true iff there are other statements to be translated
     */
    public boolean finishInstruction() throws BytecodeCompilerException {
        instructionIndex++;
        if (instructionIndex >= instructions.length)
            return false;

        currentInstruction = instructions[instructionIndex];
        currentPc = currentInstruction.getOffset();
        stack.fakeInit();
        return true;
    }

    /**
     * writes the correct begin statement for the translation of the next
     * instruction
     */
    void writeBeginStatement() {
        LineNumberTableEntry[] lines = ((LineNumberTableAttribute) method.findAttribute(CodeAttribute.class)
                .findAttribute(LineNumberTableAttribute.class)).getLineNumberTable();

        int lineNumber = 0;
        for (int i = 0; i < lines.length; i++) {
            if (lines[i].getStartPc() <= currentPc && (lines.length - 1 == i || lines[i + 1].getStartPc() > currentPc)) {
                lineNumber = lines[i].getLineNumber();
                break;
            }
        }
        statements.begin(lineNumber, currentPc, blocks.getBlock(instructionIndex).parents.size() > 1);
    }

    /**
     * Returns a string that allows access to the i'th local variable. Ensures
     * the presence of the name. You MUSST NOT assign a value to the returned
     * variable, use store instead.
     * 
     * @param i
     *            the index into the local variables array
     * @return a string which represents an assignable function name which has
     *         to be used to load the i'th local variable at the current point
     *         in the program
     */
    public String load(int i) {
        return registers.get(i);
    }

    /**
     * Returns a string that allows access to the i'th local variable. Ensures
     * the presence of the name. It is legal to assign values to this variable.
     * 
     * @param i
     *            the index into the local variables array
     * @return a string which represents an assignable function name which has
     *         to be used to load the i'th local variable at the current point
     *         in the program
     * 
     * @throws BytecodeCompilerException
     *             in case of name lookup errors
     */
    public String store(int i) throws BytecodeCompilerException {
        return registers.get(i);
    }

    // public Type regType(int pos) {
    // return registers.type(pos);
    // }

    /**
     * Pushes an integer onto the stack. The term has to be of type integer.
     * 
     * @param term
     *            the result of term is pushed
     */
    public void push(String term) {
        statements.end(stack.raise() + " := " + term);
    }

    /**
     * returns typed term that corresponds to topstack and pops that value from
     * the stack
     */
    public String pop() {
        return stack.lower();
    }

    /**
     * assigns the top stack value to target and pops it.
     */
    public void popAssign(String target) {
        statements.add(target).add(" := ").end(pop());
    }

    /**
     * pops all elements from the stack
     */
    public void pushException() {
        statements.add(stack.fakeException()).end(" := $exception");
    }

    /**
     * returns a term that represents the i'th element from top. indexing starts
     * at -1 and uses lower indices for lower values.
     */
    public String peek(int i) {
        return stack.peekTerm(i);
    }

    /**
     * get the type by negative indexing from topstack. the topmost element can
     * be addrest with -1, lower elements can be found with lower indices(-2,
     * -3, ...)
     * 
     * the existence of this value is the problem of the caller
     * 
     * @return the type of the element
     */
    public ObjectType peekType(int i) {
        return stack.peek(i);
    }

    /**
     * This function tells you, what type the result of this operation will
     * have.
     * 
     * It can be called at any moment during the translation of the current
     * opcode.
     */
    public ObjectType peekResultType() {
        return stack.getTop(instructionIndex);
    }

    public ConcreteProofObligation<?> getPO() {
        return proofObligation;
    }

    /**
     * @return true iff the current instruction is marked as part of a special
     *         function call
     */
    public boolean currentInstractionIsSpecial() {
        return blocks.getSpecial(instructionIndex);
    }

    /**
     * this method is part of the cfg, because some needed information is hidden
     * from the opcode translator
     * 
     * @return the special functions argument
     */
    public String getSpecialFunctionArgument() throws BytecodeCompilerException {
        int index;
        if (instructions[instructionIndex - 1].getOpcode() != AbstractInstruction.OPCODE_LDC)
            index = ((ImmediateShortInstruction) instructions[instructionIndex - 1]).getImmediateShort();
        else
            index = ((ImmediateByteInstruction) instructions[instructionIndex - 1]).getImmediateByte();

        Object entry;
        try {
            entry = method.getClassFile().getConstantPoolEntry(index, CPInfo.class);

            return method.getClassFile().getConstantPoolUtf8Entry(((ConstantStringInfo) entry).getStringIndex())
                    .getString();
        } catch (InvalidByteCodeException e) {
            throw new BytecodeCompilerException("failed to retrieve constant special function call argument", e);
        }
    }

    /**
     * debug; if java had friends, this would be in a util class
     * 
     * the purpose of this method is to dump control flow information to the
     * given PrintStream to allow users to inspect the infered information
     */
    public void dumpSymbolicState(final PrintStream out) {
        // synchronize on out to ensure, that no two outputs overlap if used in
        // parallel
        synchronized (out) {
            out.print("symbolic state of method ");
            out.println(methodName);
            {
                // print max stack/max locals
                CodeAttribute code = (CodeAttribute) method.findAttribute(CodeAttribute.class);
                out.append("lv: ").append(Integer.toString(code.getMaxLocals())).append("; st: ")
                        .println(code.getMaxStack());
            }

            for (int i = 0; i < instructions.length; i++) {
                out.print("\ninstruction #");
                out.println(i);

                out.print("offset: ");
                out.print(instructions[i].getOffset());
                out.print("\tmnemonic: ");
                out.println(instructions[i].getOpcodeVerbose());

                out.print("locals: [");
                ObjectType[] lv = registers.getTypesAtInstruction(i);
                for (int j = 0; j < lv.length; j++) {
                    if (j > 0)
                        out.print(", ");
                    out.print(null == lv[j] ? "<empty>" : lv[j]);
                    if (null != lv[j])
                        out.append("{").append(registers.get(j, i)).append("}");
                }
                out.println("]");

                out.print("stack: {");
                ObjectType[] ss = stack.getLayout(i);
                for (int j = 0; j < ss.length; j++) {
                    if (j > 0)
                        out.print(", ");
                    out.print(ss[j]);
                }
                out.println("}");
            }

            out.println();
        }
    }
}
