package de.uka.iti.pseudo.environment;

import java.util.Collection;

public abstract class TypeReference {

    public abstract void collectTypeVariables(Collection<String> coll);

}
