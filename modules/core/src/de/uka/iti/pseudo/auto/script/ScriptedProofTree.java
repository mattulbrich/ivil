package de.uka.iti.pseudo.auto.script;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import nonnull.Nullable;
import de.uka.iti.pseudo.auto.strategy.StrategyException;
import de.uka.iti.pseudo.proof.Proof;
import de.uka.iti.pseudo.proof.ProofException;
import de.uka.iti.pseudo.proof.ProofNode;
import de.uka.iti.pseudo.util.Log;
import de.uka.iti.pseudo.util.Util;

public class ScriptedProofTree {

    private final Proof proof;
    private Node root;

    private class Node {

        public Node(ProofScriptNode scriptNode, ProofNode proofNode) {
            this.scriptNode = scriptNode;
            this.proofNode = proofNode;
        }

        private final ProofScriptNode scriptNode;
        private final ProofNode proofNode;
        private List<Node> children;
        private Exception raisedException;

        public void execute() {
            assert raisedException == null;
            assert proofNode.getChildren() == null : "Can only be applied to a goal";


            List<ProofNode> newGoals = null;
            try {
                newGoals = scriptNode.execute(proofNode);
            } catch(Exception ex) {
                Log.log(Log.WARNING, "Exception while executing script");
                Log.stacktrace(Log.WARNING, ex);
                raisedException = ex;
                return;
            }

            makeChildren(newGoals);
        }

        private void makeChildren(List<ProofNode> newGoals) {
            assert children == null;

            List<ProofScriptNode> scriptChildren = scriptNode.getChildren();
            if(newGoals.size() != scriptChildren.size()) {
                raisedException = new StrategyException(newGoals.size() + " open goals, but " +
                        scriptChildren.size() + " branches in proof script");
                return;
            }

            children = new ArrayList<Node>();
            for (int i = 0; i < newGoals.size(); i++) {
                Node child = new Node(scriptChildren.get(i), newGoals.get(i));
                children.add(child);
                child.execute();
            }
        }

        public void executeRelativeTo(@Nullable Node existingNode) throws ProofException {
            if(existingNode == null || raisedException != null ||
                    !scriptNode.hasSameCommandAs(existingNode.scriptNode)) {

                proof.prune(proofNode);
                execute();

            } else {

                assert existingNode.proofNode == proofNode;

                // Same command on same node -> same direct children
                // there may be differences deeper in the tree
                ArrayList<ProofNode> newGoals = new ArrayList<ProofNode>();
                for (Node existingChild : existingNode.children) {
                    newGoals.add(existingChild.proofNode);
                }

                makeChildren(newGoals);
            }
        }

        public void dump(int level, PrintStream err) {
            err.print(Util.duplicate(" ", level));
            err.println(scriptNode.getCommand().getName() +
                    " - " + proofNode.getNumber() +
                    " - " + raisedException);
            if(children != null) {
                for (Node child : children) {
                    child.dump(level+1, err);
                }
            }
        }

    }

    public ScriptedProofTree(Proof proof) {
        this.proof = proof;
    }

    public void reset() throws ProofException {
        proof.prune(proof.getRoot());
        root = null;
    }

    public void execute(ProofScript proofScript) throws ProofException {
        Node newRoot = new Node(proofScript.getRoot(), proof.getRoot());
        newRoot.executeRelativeTo(root);
        root = newRoot;
    }

    public void dump(PrintStream err) {
        if(root != null) {
            root.dump(0, err);
        } else {
            err.println("Empty proof script tree");
        }
    }
}
