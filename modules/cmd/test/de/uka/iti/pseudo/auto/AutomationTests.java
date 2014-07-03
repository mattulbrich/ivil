package de.uka.iti.pseudo.auto;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import de.uka.iti.pseudo.cmd.AutomaticProblemProver;
import de.uka.iti.pseudo.cmd.FileProblemProverBuilder;
import de.uka.iti.pseudo.cmd.Result;
import de.uka.iti.pseudo.cmd.FileProblemProverBuilder.ProofObligationOption;

public class AutomationTests extends TestCase {

    private static final int TIMEOUT = 20;

    private static class AutoCase extends TestCase {
        private final String fileName;

        AutoCase(String fileName) {
            super("Test for " + fileName);
            this.fileName = fileName;
        }

        @Override
        protected void runTest() throws Throwable {
            FileProblemProverBuilder fileBuilder =
                    new FileProblemProverBuilder(new File(fileName));

            fileBuilder.setTimeout(TIMEOUT);
            fileBuilder.setRelayToSource(true);
            fileBuilder.setProofObligations(ProofObligationOption.ALL, new String[0]);

            for (AutomaticProblemProver app : fileBuilder.createProblemProvers()) {
                Result result = app.call();

                if(!result.getSuccess()) {
                    result.print(System.err);
                }

                assertTrue(result.getSuccess());
            }
        }
    }

    public static Test suite() throws IOException {
        TestSuite suite = new TestSuite("Automation tests");

        InputStream stream = AutomationTests.class.getResourceAsStream("testcases.txt");
        if(stream == null) {
            throw new IOException("cannot read testcases.txt");
        }

        List<String> cases = readLines(stream);

        for (String string : cases) {
            suite.addTest(new AutoCase(string));
        }

        return suite;
    }

    private static List<String> readLines(InputStream stream) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(stream));
        List<String> result = new ArrayList<String>();
        String line = br.readLine();
        while(line != null) {
            if(!line.startsWith("#") && line.length() > 0) {
                result.add(line);
            }
            line = br.readLine();
        }
        return result;
    }

}
