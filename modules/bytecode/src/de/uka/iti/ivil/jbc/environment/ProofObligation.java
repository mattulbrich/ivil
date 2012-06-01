package de.uka.iti.ivil.jbc.environment;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is used to represent and load a single ProofObligation. It has a
 * rough correspondence to ivils Environment. Note that ProofObligation holds an
 * environment with a proof if it was successfully loaded.
 * 
 * @note it is not assumed that more then one thread updates the state of
 *       ProofObligations!
 * 
 * @note ProofObligations form a tree.
 * 
 * @note ProofObligation is commonly abbreviated as PO.
 * 
 * @author timm.felden@felden.com
 * 
 */
abstract public class ProofObligation {

    /**
     * Proof obligations can be in different states.
     * 
     * @author timm.felden@felden.com
     * 
     */
    public static enum State {
        loadFailed(1), load(2), waitingForProof(3), proofing(4), proofFailed(5), proofFinished(6);

        private final int val;

        private State(int val) {
            this.val = val;
        }

        public int val() {
            return val;
        }

        public static Object get(int i) {
            switch (i) {
            case 1:
                return loadFailed;
            case 2:
                return load;
            case 3:
                return waitingForProof;
            case 4:
                return proofing;
            case 5:
                return proofFailed;
            case 6:
                return proofFinished;
            default:
                throw new IllegalArgumentException("no state has value " + i);
            }
        }
    }

    private State state = State.proofFinished;

    // double linked PO tree
    public final ProofObligation parent;
    public final List<ProofObligation> children = new ArrayList<ProofObligation>();
    
    /**
     * Sets parent as parent and adds this to the children of parent.
     * 
     * @param parent
     */
    protected ProofObligation(ProofObligation parent) {
        this.parent = parent;
        if (null != parent)
            parent.children.add(this);
    }

    /**
     * @return the state of this PO. Might be calculated lazy.
     */
    public State getState() {
        return state;
    }

    /**
     * recalculate state. this is needed if a child changes its state.
     */
    abstract protected void updateState();

    /**
     * Sets a new state. Use with stare, as writing states can cause severe
     * threading related bugs.
     */
    public void setState(State s) {
        if (state != s) {
            state = s;
            if (parent != null)
                parent.updateState();
        }
    }

    public void addChild(ProofObligation child) {
        children.add(child);
    }

    /**
     * @return the name of this PO.
     */
    abstract public String getName();

    @Override
    public String toString() {
        return getState() + " - " + getName();
    }
}
