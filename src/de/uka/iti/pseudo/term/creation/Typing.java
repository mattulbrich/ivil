package de.uka.iti.pseudo.term.creation;

import de.uka.iti.pseudo.term.Type;
import nonnull.NonNull;

@NonNull
public class Typing {

    private Type rawType;

    private TypingContext typingContext;

    public Typing(Type rawType, TypingContext instantiation) {
        super();
        this.rawType = rawType;
        this.typingContext = instantiation;
    }

    public Type getRawtType() {
        return rawType;
    }

    public Type getType() {
        return typingContext.instantiate(rawType);
    }
    
    @Override
    public String toString() {
        return rawType.toString();
    }
}
