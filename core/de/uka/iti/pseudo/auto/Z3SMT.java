/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2010 Universitaet Karlsruhe, Germany
 *    written by Mattias Ulbrich
 * 
 * The system is protected by the GNU General Public License. 
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.auto;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;

import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.proof.ProofException;
import de.uka.iti.pseudo.term.Sequent;
import de.uka.iti.pseudo.term.TermException;
import de.uka.iti.pseudo.util.Pair;

public class Z3SMT implements DecisionProcedure {
    
    public Z3SMT() {
    }

    public Pair<Result, String> solve(final Sequent sequent, final Environment env, long timeout) throws ProofException, IOException {

        // System.out.println("Z3 for " + sequent);
        
        StringBuilder builder = new StringBuilder();
        SMTLibTranslator trans = new SMTLibTranslator(env);
        try {
            trans.export(sequent, builder);
        } catch (TermException e) {
            throw new ProofException("Error while preparing Z3 proof obligation");
        }
        
        final String challenge = builder.toString();
        // System.err.println(challenge);

        try {
            Runtime rt = Runtime.getRuntime();

            Process process = rt.exec("z3 -in -smt");
            Writer w = new OutputStreamWriter(process.getOutputStream());
            w.write(challenge);
            w.close();

            // System.err.println("Wait for " + process);

            TimeoutThread timeoutThread = new TimeoutThread(timeout, process);
            timeoutThread.start();

            int errorVal = process.waitFor();
            // System.err.println("Finished waiting: " + errorVal);

            if(timeoutThread.hasKilled) {
                // System.err.println("Timed out");
                return Pair.make(Result.UNKNOWN, "Z3 timed out");
            }

            BufferedReader r = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String answerLine = r.readLine();

            r = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            StringBuilder msg = new StringBuilder();
            String line = r.readLine();
            while(line != null) {
                msg.append(line).append("\n");
                line = r.readLine();
            }

            Pair<Result, String> result;
            if("unsat".equals(answerLine)) {
                result = Pair.make(Result.VALID, msg.toString());
            } else if("sat".equals(answerLine)) {
                result = Pair.make(Result.NOT_VALID, msg.toString());
            } else if("unknown".equals(answerLine)){
                result =  Pair.make(Result.UNKNOWN, msg.toString());
            } else
                throw new ProofException("Z3 returned an error message: " + msg);
            
            // System.err.println("Result: " + result);
            return result;

        } catch(Exception ex) {
            dumpTmp(challenge);
            // may get lost!
            ex.printStackTrace();
            throw new ProofException("Error while calling decision procedure Z3", ex);
        }
    }

    private void dumpTmp(String challenge) {
        Writer w = null;
        try {
            File tmp = File.createTempFile("ivil", ".smt");
            w  = new FileWriter(tmp);
            w.write(challenge);
            w.close();
            System.err.println("Challenge dumped to file " + tmp);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(w != null)
                try {
                    w.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
    }
    
    private static class TimeoutThread extends Thread {
        
        private Process process;
        private long timeout;
        
        private boolean hasKilled = false; 

        public TimeoutThread(long timeout, Process process) {
            this.timeout = timeout;
            this.process = process;
        }

        @Override 
        public void run() {
            try {
                Thread.sleep(timeout);
                try {
                    process.exitValue();
                } catch(IllegalThreadStateException ex) {
                    // was still running.
                    process.destroy();
                    hasKilled = true;
                }
                
            } catch(InterruptedException ex) {
            }
        }
    }

}
