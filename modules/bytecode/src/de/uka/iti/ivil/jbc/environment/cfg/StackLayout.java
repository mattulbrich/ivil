package de.uka.iti.ivil.jbc.environment.cfg;

import java.util.ArrayList;

import de.uka.iti.ivil.jbc.environment.cfg.BlockLayout.Block;
import de.uka.iti.ivil.jbc.util.ObjectType;

/**
 * The stack layout is used during type analysis and opcode translation to
 * represent the typeing of the stack before each individual operation.
 * 
 * @author timm.felden@felden.com
 */
class StackLayout {
    // ! @note: current implementation of layout is effective but not
    // efficient
    private final ObjectType[][] layout;

    // the current content of the stack
    private ObjectType[] current;

    // the fake level is used to access the right elements in pop/push
    // operations
    private int fakeLevel = 0;
    private final ControlFlowGraph cfg;

    /**
     * for efficiency, the stack part of the frames is destroyed
     */
    public StackLayout(ControlFlowGraph controlFlowGraph, TypeFrame[] frames) {
        this.cfg = controlFlowGraph;

        this.layout = new ObjectType[frames.length][];
        this.current = new ObjectType[0];

        // copy frame content destructively from the frames
        for(int i = 0; i < layout.length; i++){
            layout[i] = new ObjectType[frames[i].stack.size()];
            for (int j = layout[i].length - 1; j >= 0; j--) {
                layout[i][j] = frames[i].stack.pop().getTypeName();
                if (null == layout[i][j]) {
                    // TODO remove this warning, it can only be created by buggy
                    // type inference; maybe turn it into an Error
                    System.err.println("warning: found null on stack of " + cfg.methodName + "::" + i + ":" + j);
                    // Spec should never occur on the stack, so we use it to
                    // indicate nulls without breaking the other stuff
                    layout[i][j] = ObjectType.createTypeFromSingleTypeDescriptor("Lde/uka/iti/ivi/jbc/Spec;");
                }
            }
        }
    }

    /**
     * peek into the stack
     * 
     * @param i
     *            position from topstack. the last pushed type is at -1, that
     *            one before is -2 and so on
     * @return type of requested position
     */
    public ObjectType peek(int i) {
        return current[current.length + i];
    }

    /**
     * Initialize the stack layout with another layout. This has to be called if
     * a new block is to be translated, except for the first block, which is
     * created automatically.
     * 
     * @param index
     *            pc of the parent block
     */
    public void initializeWith(int index) {
        current = layout[index].clone();
    }

    /**
     * The current layout contains excactly one exception.
     */
    public void initializeWithException() {
        current = new ObjectType[] { ObjectType.createTypeFromSingleTypeDescriptor("Ljava/lang/Throwable;") };
    }

    /**
     * initializes fake modifikation of the stack
     * 
     * @note: the fake modification works only iff the stack is initialized for
     *        each operation, and no lower operations occur after raise
     *        operations
     */
    public void fakeInit() {
        if (cfg.blocks.getBlock(cfg.instructionIndex).isExceptionHandler) {
            initializeWithException();
            fakeLevel = 1;
            return;

        } else {
            ArrayList<Block> parents = cfg.blocks.getBlock(cfg.instructionIndex).parents;
            if (parents.size() > 0) {
                Block parent = parents.get(0);
                for (int i = 0; i < layout.length; i++) {
                    if (cfg.instructions[i].getOffset() == parent.pc) {
                        current = layout[i];
                        fakeLevel = current.length;
                        return;
                    }
                }
            } else {
                fakeLevel = 0;
                return;
            }
        }
        assert false : "unreachable";
    }

    public String lower() {
        ObjectType type = current[--fakeLevel];
        return "$stack_" + (1 + fakeLevel) + "_" + type.getBaseType();
    }

    public String raise() {
        current = layout[cfg.instructionIndex];
        ObjectType type = current[fakeLevel++];
        String var = "$stack_" + (fakeLevel) + "_" + type.getBaseType();

        cfg.proofObligation.requestFunction(var, type.getBaseType(), "assignable");

        return var;
    }

    public String peekTerm(int i) {
        return "$stack_" + (fakeLevel + 1 + i) + "_" + current[fakeLevel + i].getBaseType();
    }

    /**
     * Create code that pushs exception to the lowest position, but dont change
     * stack.
     */
    public String fakeException() {
        final String var = "$stack_1_ref";

        cfg.proofObligation.requestFunction(var, "ref", "assignable");

        return var;
    }

    public ObjectType getTop(int instructionIndex) {
        ObjectType[] t = layout[instructionIndex];
        return t[t.length - 1];
    }

    /**
     * return i'th layout; this is needed to create type constraints.
     */
    ObjectType[] getLayout(int index) {
        return layout[index];
    }

}