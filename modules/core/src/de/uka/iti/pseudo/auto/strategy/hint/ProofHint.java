package de.uka.iti.pseudo.auto.strategy.hint;

import java.util.List;

import de.uka.iti.pseudo.auto.strategy.StrategyException;
import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.environment.Mappable;

public interface ProofHint extends Mappable {

    HintRuleAppFinder createRuleAppFinder(Environment env, List<String> arguments) throws StrategyException;
}
