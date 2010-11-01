package de.uka.iti.pseudo.gui;

import java.io.File;

import javax.swing.Action;
import javax.swing.SwingUtilities;

import de.uka.iti.pseudo.TestCaseWithEnv;
import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.environment.EnvironmentMaker;
import de.uka.iti.pseudo.parser.Parser;
import de.uka.iti.pseudo.proof.Proof;

/**
 * This test will try to close all examples using the gui.
 * 
 * @author timm.felden@felden.com
 */
public class TestExamples extends TestCaseWithEnv {

    /**
     * try to find proofs for simple examples, they should close automatically
     * 
     * @throws Exception
     *             no exception shall be thrown
     */
    public void testSimpleExamples() throws Exception {
        String[] paths = { "examples/simple/simpleBlowup.p", "examples/simple/blowup.p", "examples/simple/cond.p",
                "examples/simple/fakultaet.p", "examples/simple/properties.p" };
        for (String path : paths) {

            ClassLoader.getSystemClassLoader().setDefaultAssertionStatus(true);
            Parser fp = new Parser();

            EnvironmentMaker em = new EnvironmentMaker(fp, new File(path));
            Environment env = em.getEnvironment();

            Proof proof = new Proof(em.getProblemTerm());

            ProofCenter proofCenter = new ProofCenter(proof, env);
            MainWindow main = proofCenter.getMainWindow();
            main.setVisible(true);

            {
                final Action auto = proofCenter.getBarManager().getAction("proof.auto");
                assertTrue("failed to load AutoProofAction", auto != null);

                SwingUtilities.invokeAndWait(new Runnable() {
                    public void run() {
                        auto.actionPerformed(null);
                    }
                });
                assertFalse("Proof could not be found automatically for " + path, proofCenter.getProof().hasOpenGoals());
            }
        }
    }
}
