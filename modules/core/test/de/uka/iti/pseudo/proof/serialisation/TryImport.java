package de.uka.iti.pseudo.proof.serialisation;

import java.io.File;
import java.io.FileInputStream;

import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.environment.EnvironmentMaker;
import de.uka.iti.pseudo.parser.Parser;
import de.uka.iti.pseudo.proof.Proof;

public class TryImport {

    public static void main(String[] args) throws Exception {
        ProofImport pxml = new ProofXML();
        
        EnvironmentMaker em = new EnvironmentMaker(new Parser(), new File("examples/simple/fakultaet.p"));
        Environment env = em.getEnvironment();
        Proof proof = new Proof(em.getProblemTerm());
        
        pxml.importProof(new FileInputStream("examples/simple/fakultaet.pxml"), proof, env);
        
        System.out.println(pxml);
    }

}
