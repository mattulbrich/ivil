package de.uka.iti.pseudo.auto;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.proof.ProofException;
import de.uka.iti.pseudo.term.Sequent;
import de.uka.iti.pseudo.util.Pair;

public class Z3 implements DecisionProcedure, Callable<String> {
    
    public Z3() {
    }

    public Pair<Result, String> solve(final Sequent sequent, final Environment env, long timeout) throws ProofException {
        
        Callable<Pair<Result, String>> callable = new Callable<Pair<Result, String>>() {
        public Pair<Result, String> call() throws Exception {
            Runtime rt = Runtime.getRuntime();
            Process process = rt.exec("z3 -in -z3");
            
            Writer w = new OutputStreamWriter(process.getOutputStream());
            
            Z3Translator trans = new Z3Translator(env);
            trans.export(sequent, w);
//            trans.export(sequent, new OutputStreamWriter(System.out));
            w.close();
            
            process.waitFor();
            
            BufferedReader r = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String answerLine = r.readLine();
            
            r = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            StringBuilder msg = new StringBuilder();
            String line = r.readLine();
            while(line != null) {
                msg.append(line);
                line = r.readLine();
            }
            
//            System.err.println("Z3 answers: " + msg);
            if("unsat".equals(answerLine)) {
                return Pair.make(Result.VALID, msg.toString());
            } else if("sat".equals(answerLine)) {
                return Pair.make(Result.NOT_VALID, msg.toString());
            } else if("unknown".equals(answerLine)){
                return Pair.make(Result.UNKNOWN, msg.toString());
            } else
                throw new ProofException("Z3 returned an error message: " + msg);
            
        }};
        
        
        FutureTask<Pair<Result, String>> ft = new FutureTask<Pair<Result, String>>(callable);
        Thread t = new Thread(ft, "Z3");
        t.start();
        
        try {
            return ft.get(timeout, TimeUnit.MILLISECONDS);
        } catch (TimeoutException ex) {
            //ex.printStackTrace();
            return Pair.make(Result.UNKNOWN, "Call to Z3 has timed out");
        } catch(Exception ex) {
            throw new ProofException("Error while calling decision procedure Z3", ex);
        } finally {
            if(t != null)
                t.interrupt();
        }
    }

    public String call() throws Exception {
        return null;
    }

}
