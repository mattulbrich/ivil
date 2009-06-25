package de.uka.iti.pseudo.auto;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;

import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.proof.ProofException;
import de.uka.iti.pseudo.term.Sequent;
import de.uka.iti.pseudo.term.TermException;
import de.uka.iti.pseudo.util.Pair;

public class Z3 implements DecisionProcedure {
    
    public Z3() {
        
    }

    @Override 
    public Pair<Result, String> solve(Sequent sequent, Environment env) throws ProofException {
        
        try {
            Runtime rt = Runtime.getRuntime();
            Process process = rt.exec("z3");
            
            Writer w = new OutputStreamWriter(process.getOutputStream());
            
            SMTLibTranslator trans = new SMTLibTranslator(env);
            trans.export(sequent, w);
            trans.export(sequent, new OutputStreamWriter(System.out));

            w.close();
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
            
        } catch (IOException e) {
            throw new ProofException("Error while calling decision procedure Z3", e);
        } catch (TermException e) {
            throw new ProofException("Error while calling decision procedure Z3", e);
        }
    }

}
