package de.uka.iti.pseudo.term.creation;

import de.uka.iti.pseudo.term.Type;
import nonnull.NonNull;

@NonNull
public class Typing {

    private Type rawType;

    private TypingContext instantiation;

    public Typing(Type rawType, TypingContext instantiation) {
        super();
        this.rawType = rawType;
        this.instantiation = instantiation;
    }

    public Type getRawtType() {
        return rawType;
    }

    public Type getType() {
        return rawType.instantiate(instantiation);
    }
}
