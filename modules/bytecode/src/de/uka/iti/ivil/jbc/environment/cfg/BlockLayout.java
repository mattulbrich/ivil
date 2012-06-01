package de.uka.iti.ivil.jbc.environment.cfg;

import java.util.ArrayList;

class BlockLayout {

    static class Block {
        public final int index, pc;
        public final ArrayList<Block> parents = new ArrayList<BlockLayout.Block>();
        public final ArrayList<Block> children = new ArrayList<BlockLayout.Block>();
        public boolean isExceptionHandler = false;

        // true iff the current instruction is part of a special function call;
        // this can only happen for ldc(_w) and invokestatic
        public boolean isSpecialFunctionCall = false;

        Block(int index, int pc) {
            this.index = index;
            this.pc = pc;
        }
    }

    private final Block blocks[];

    public BlockLayout(ControlFlowGraph cfg) {
        blocks = new Block[cfg.instructions.length];
        for (int i = 0; i < cfg.instructions.length; i++)
            blocks[i] = new Block(i, cfg.instructions[i].getOffset());
    }

    public void addBlock(int index, int... targets) {
        for(int i = 0; i < targets.length; i++){
            int target = targets[i];
            Block block = null;
            for (Block b : blocks) {
                if (b.pc == target) {
                    block = b;
                    break;
                }
            }
            assert null != block : "no such block";

            blocks[index].children.add(block);
            block.parents.add(blocks[index]);
        }
    }

	Block getBlock(int i) {
		return blocks[i];
	}

    void setSpecial(int i) {
        blocks[i].isSpecialFunctionCall = true;
    }

    public boolean getSpecial(int i) {
        return blocks[i].isSpecialFunctionCall;
    }
}