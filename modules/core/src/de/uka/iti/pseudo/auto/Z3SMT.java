/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2012 Karlsruhe Institute of Technology
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
import java.util.HashSet;
import java.util.Set;

import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.proof.ProofException;
import de.uka.iti.pseudo.term.Sequent;
import de.uka.iti.pseudo.term.TermException;
import de.uka.iti.pseudo.util.Log;
import de.uka.iti.pseudo.util.Pair;
import de.uka.iti.pseudo.util.settings.Settings;

/**
 * The Decision Procedure Z3 is called from this class. The sequent to be solved
 * is translated into SMT format using a {@link SMTLibTranslator}.
 */
public class Z3SMT implements DecisionProcedure {

    /**
     * The system settings to read from
     */
    private static Settings settings = Settings.getInstance();

    /**
     * If this flag is set, the challenge is kept after solving and saved to a
     * file.
     */
    private static boolean KEEP_CHALLENGES =
        settings.getBoolean("pseudo.z3.keepFile", false);

    /**
     * The the SMT Lib Version 1 format if <code>true</code>, otherwise use the
     * more flexible SMT Lib Version 2 format.
     */
    private static boolean USE_SMT1 =
        settings.getBoolean("pseudo.z3.useSMT1", false);

    private final Set<Sequent> cache = new HashSet<Sequent>();

    @Override
    public String getKey() {
        return "Z3";
    }

    /* (non-Javadoc)
     * @see de.uka.iti.pseudo.auto.DecisionProcedure#solve(de.uka.iti.pseudo.term.Sequent, de.uka.iti.pseudo.environment.Environment, int)
     */
    @Override
    public Pair<Result, String> solve(final Sequent sequent, final Environment env, int timeout)
            throws ProofException, IOException, InterruptedException {

        boolean cached = cache.contains(sequent);
        if(cached) {
            Log.log(Log.DEBUG, "Cache hit for %s", sequent);
            return Pair.make(DecisionProcedure.Result.VALID, "(cached)");
        }

        // System.out.println("Z3 for " + sequent);

        StringBuilder builder = new StringBuilder();
        SMTLibTranslator trans;

        if(USE_SMT1) {
            trans = new SMTLib1Translator(env);
        } else {
            trans = new SMTLib2Translator(env);
        }

        try {
            trans.export(sequent, builder);
        } catch (TermException e) {
            throw new ProofException("Error while preparing Z3 proof obligation");
        }

        final String challenge = builder.toString();
        // System.err.println(challenge);

        Process process = null;

        try {
            Runtime rt = Runtime.getRuntime();

            process = rt.exec("z3 SOFT_TIMEOUT=" + timeout + " -in -smt2");

            Writer w = new OutputStreamWriter(process.getOutputStream());
            w.write(challenge);
            w.write("(check-sat)\n");
            w.close();

            // System.err.println("Wait for " + process);

            if(Thread.interrupted()) {
                throw new InterruptedException();
            }

            process.waitFor();

            StringBuilder msg = new StringBuilder();
            BufferedReader r = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String answerLine = r.readLine();
            msg.append(answerLine).append("\n");

            r = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            String line = r.readLine();
            while(line != null) {
                msg.append(line).append("\n");
                line = r.readLine();
                // System.err.println(line);
            }

            Pair<Result, String> result;
            if("unsat".equals(answerLine)) {
                result = Pair.make(Result.VALID, msg.toString());
                cache.add(sequent);
            } else if("sat".equals(answerLine)) {
                result = Pair.make(Result.NOT_VALID, msg.toString());
            } else if("unknown".equals(answerLine)){
                result =  Pair.make(Result.UNKNOWN, msg.toString());
            } else {
                throw new ProofException("Z3 returned an error message: " + msg);
            }

            if(KEEP_CHALLENGES) {
                Log.log("Result: " + result);
                dumpTmp(challenge);
            }

            Log.log(Log.DEBUG, "Result for %s: %s", sequent, result);
            return result;
        } catch(InterruptedException ex) {
            Log.log(Log.DEBUG, "Result for %s: interrupted", sequent);
            throw ex;

        } catch(Exception ex) {
            dumpTmp(challenge);
            // may get lost!
            ex.printStackTrace();
            throw new ProofException("Error while calling decision procedure Z3", ex);

        } finally {
            if(process != null) {
                // ensure the process is killed
                process.destroy();
            }
        }
    }

    /**
     * Dump the challenge into a text file in the temporary directory.
     *
     * @param challenge
     *            the translated challenge to dump.
     */
    private void dumpTmp(String challenge) {
        Writer w = null;
        try {
            File tmp = File.createTempFile("ivil" + ((System.currentTimeMillis() / 1000) % 10000) + "_", ".smt");
            w  = new FileWriter(tmp);
            w.write(challenge);
            w.close();
            Log.log(Log.ERROR, "Challenge dumped to file " + tmp);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(w != null) {
                try {
                    w.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


}
