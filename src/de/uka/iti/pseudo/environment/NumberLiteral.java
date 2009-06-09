package de.uka.iti.pseudo.environment;

import java.math.BigInteger;

import de.uka.iti.pseudo.parser.ASTLocatedElement;
import de.uka.iti.pseudo.term.Type;

public class NumberLiteral extends Function {
    
    private static final Type[] NO_ARGS = new Type[0];

    private BigInteger value;
    
    public NumberLiteral(BigInteger value, Environment env)
            throws EnvironmentException {
        super(value.toString(), env.getIntType(), NO_ARGS, true, false,
                ASTLocatedElement.BUILTIN);
    }
    
    public NumberLiteral add(NumberLiteral otherLiteral, Environment env) {
        BigInteger result = value.add(otherLiteral.value);
        return env.getNumberLiteral(result);
    }
    
    // ...

}
