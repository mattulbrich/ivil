package de.uka.iti.pseudo.auto;

import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.proof.ProofException;
import de.uka.iti.pseudo.term.Sequent;
import de.uka.iti.pseudo.util.Pair;

public interface DecisionProcedure {

    public Pair<Result, String> solve(Sequent sequent, Environment env)
       throws ProofException;
    
    enum Result { VALID, NOT_VALID, UNKNOWN };
    
}
