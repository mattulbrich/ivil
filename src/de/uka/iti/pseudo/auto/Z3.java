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
            Process process = rt.exec("z3");
            
            Writer w = new OutputStreamWriter(process.getOutputStream());
            
            SMTLibTranslator trans = new SMTLibTranslator(env);
            trans.export(sequent, w);
            trans.export(sequent, new OutputStreamWriter(System.out));
            w.close();
            
            process.waitFor();
            
            BufferedReader r = new BufferedReader(new InputStreamReader(process.getInputStream()));
            
            String firstLine = r.readLine();
            
            StringBuilder msg = new StringBuilder(firstLine);
            String line = r.readLine();
            while(line != null) {
                msg.append(line);
                line = r.readLine();
            }
            
            if("unsat".equals(firstLine)) {
                return Pair.make(Result.VALID, msg.toString());
            } else if("sat".equals(firstLine)) {
                return Pair.make(Result.NOT_VALID, msg.toString());
            } else if("unknown".equals(firstLine)){
                return Pair.make(Result.UNKNOWN, msg.toString());
            } else
                throw new ProofException("Z3 returned an error message: " + msg);
        }};
        
        
        FutureTask<Pair<Result, String>> ft = new FutureTask<Pair<Result, String>>(callable);
        
        try {
            return ft.get(timeout, TimeUnit.MILLISECONDS);
        } catch (TimeoutException ex) {
            //ex.printStackTrace();
            return Pair.make(Result.UNKNOWN, "Call to Z3 has timed out");
        } catch(Exception ex) {
            throw new ProofException("Error while calling decision procedure Z3", ex);
        }
    }

    public String call() throws Exception {
        return null;
    }

}
