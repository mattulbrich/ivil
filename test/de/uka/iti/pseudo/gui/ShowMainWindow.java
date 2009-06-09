package de.uka.iti.pseudo.gui;

import java.io.File;

import javax.swing.WindowConstants;

import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.environment.EnvironmentMaker;
import de.uka.iti.pseudo.parser.Parser;
import de.uka.iti.pseudo.proof.Proof;

public class ShowMainWindow {

    public static void main(String[] args) throws Exception {
        
        ClassLoader.getSystemClassLoader().setDefaultAssertionStatus(true);
        Parser fp = new Parser();
        
        String arg = args.length > 0 ? args[0] : "examples/first.p";
        
        EnvironmentMaker em = new EnvironmentMaker(fp, new File(arg));
        Environment env = em.getEnvironment();
        
        env.dump();
        
        Proof proof = new Proof(em.getProblemTerm());
        
        ProofCenter proofCenter = new ProofCenter(proof, env);
        MainWindow main = proofCenter.getMainWindow();
        main.setSize(600, 600);
        main.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        main.setVisible(true);
    }

}
