package de.uka.iti.pseudo.gui;

import javax.swing.JFrame;
import javax.swing.WindowConstants;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import de.uka.iti.pseudo.proof.Proof;

public class ShowProofComponent {

    public static void main(String[] args) {
        JFrame f = new JFrame();
        
        ProofComponent pc = new ProofComponent(new Proof());
        
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("Hello World");
        root.add(new DefaultMutableTreeNode("Child 1"));
        root.add(new DefaultMutableTreeNode("Child 2"));
        ((DefaultMutableTreeNode)root.getChildAt(0)).add(new DefaultMutableTreeNode("Child Child"));
        
        DefaultTreeModel m = new DefaultTreeModel(root);
        pc.setModel(m);
        f.getContentPane().add(pc);
        f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        f.setSize(400,400);
        f.setVisible(true);
    }

}
