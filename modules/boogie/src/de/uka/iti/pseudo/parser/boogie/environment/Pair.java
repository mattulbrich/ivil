package de.uka.iti.pseudo.parser.boogie.environment;

/**
 * Same as C++ std::pair
 * 
 * @author timm.felden@felden.com
 * 
 * @param <T1>
 * @param <T2>
 */
public final class Pair<T1, T2> {
    public final T1 first;
    public final T2 second;

    Pair(T1 first, T2 second) {
        this.first = first;
        this.second = second;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Pair) {
            @SuppressWarnings("unchecked")
            Pair<T1, T2> p = (Pair<T1, T2>) obj;
            return first.equals(p.first) && second.equals(p.second);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return first.hashCode() + second.hashCode();
    }
}
