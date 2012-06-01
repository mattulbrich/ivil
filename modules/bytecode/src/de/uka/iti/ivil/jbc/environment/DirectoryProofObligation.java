package de.uka.iti.ivil.jbc.environment;

/**
 * This class forms a meta obligation for all obligations contained in this
 * directory. Directories with no children are considered to be correct.
 * 
 * @author timm.felden@felden.com
 */
public class DirectoryProofObligation extends ProofObligation {

    private final String path;

    public DirectoryProofObligation(final String path) {
        super(null);

        this.path = path;
    }

    public DirectoryProofObligation(final String path, final ProofObligation parent) {
        super(parent);

        this.path = path;
    }

    @Override
    public String getName() {
        return path;
    }

    @Override
    protected void updateState() {
        // if load failed somewhere, this wont ever change
        if(getState().equals(State.loadFailed))
            return;
        
        // if PO has no children, it is valid
        if(children.size()==0){
            setState(State.proofFinished);
            return;
        }
        
        State min = State.proofFinished;
        for(ProofObligation c : children)
            if (c.getState().val() < min.val())
                min = c.getState();

        setState(min);
    }

}
