package de.uka.iti.pseudo.gui;

import javax.swing.WindowConstants;

import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.parser.term.TestTermParser;
import de.uka.iti.pseudo.proof.MockingProof;
import de.uka.iti.pseudo.proof.Proof;

public class ShowMainWindow {

    public static void main(String[] args) throws Exception {
        
        Environment env = TestTermParser.loadEnv();
        Proof proof = new MockingProof();
        
        MainWindow main = new MainWindow(proof, env);
        main.setSize(600, 600);
        main.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        main.setVisible(true);
    }

}
