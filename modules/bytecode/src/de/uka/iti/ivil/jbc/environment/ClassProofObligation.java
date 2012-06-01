package de.uka.iti.ivil.jbc.environment;

import de.uka.iti.ivil.jbc.util.EscapeName;

public class ClassProofObligation extends ProofObligation {
    
    private final String name;

    /**
     * The package of this file. This is needed in order to find the class file.
     * The name is NOT escaped.
     */
    public final String pack;

    /**
     * Fully escaped package name.
     */
    public final String escapedPack;

    /**
     * Class path for this proof obligation. The class path must end with a '/'.
     */
    public final String classPath;

    public ClassProofObligation(final ProofObligation parent, final String name, String pack, String classPath) {
        super(parent);
        
        assert null != parent : "Class PO may not be root!";

        this.name = name;
        this.pack = pack;

        assert classPath.endsWith("/") : "class path is required to end with /";
        this.classPath = classPath;

        {
            String[] parts = pack.split("/");
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < parts.length - 1; i++) {
                if (i > 0)
                    sb.append("_");
                sb.append(EscapeName.build(parts[i]));
            }
            this.escapedPack = sb.toString();
        }
    }

    @Override
    protected void updateState() {
        // if load failed somewhere, this wont ever change
        if (getState().equals(State.loadFailed))
            return;

        // if PO has no children, it is valid
        if (children.size() == 0) {
            setState(State.proofFinished);
            return;
        }

        State min = State.proofFinished;
        for (ProofObligation c : children)
            if (c.getState().val() < min.val())
                min = c.getState();

        setState(min);

    }

    @Override
    public String getName() {
        return name;
    }

}
