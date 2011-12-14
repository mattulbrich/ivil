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
import de.uka.iti.pseudo.cmd.AutomaticFileProver;
import de.uka.iti.pseudo.cmd.Result;

public class AutomationTests extends TestCase {
    
    private static final int TIMEOUT = 300;
    
    private static class AutoCase extends TestCase {
        private final String fileName;

        AutoCase(String fileName) {
            super("Test for " + fileName);
            this.fileName = fileName;
        }

        @Override
        protected void runTest() throws Throwable {
            AutomaticFileProver prover = new AutomaticFileProver(new File(fileName));

            if (!prover.hasProblem()) {
                fail(fileName + " does not contain a problem declaration");
            }

            prover.setTimeout(TIMEOUT);
            prover.setRelayToSource(true);
            Result result = prover.call();

            if(!result.getSuccess()) {
                result.print(System.err);
            }

            assertTrue(result.getSuccess());
        }
    }

    public static Test suite() throws IOException {
        TestSuite suite = new TestSuite("Test for de.uka.iti.pseudo - full automation");
        
        InputStream stream = AutomationTests.class.getResourceAsStream("testcases.txt");
        if(stream == null)
            throw new IOException("cannot read testcases.txt");
        
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
