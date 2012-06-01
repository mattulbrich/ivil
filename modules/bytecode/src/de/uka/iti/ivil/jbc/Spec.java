package de.uka.iti.ivil.jbc;

/**
 * This class serves as dummy to allow translation of code with embedded proof
 * obligation data. For example in the JML* POGenerator, this class is used to
 * carry JML code throug the translation process to embed loop invariants and
 * jml assertion in the right place.
 * 
 * @author timm.felden@felden.com
 * 
 */
public final class Spec {

    /**
     * This method has an empty body and therefore no measurable side effect. It
     * can be counted as not observable. (which corresponds to helper in JML)
     * 
     * @param code
     *            the code carried to special function translator of the
     *            POGenerator. This MUSST BE a String constant. For more
     *            information look into the documentation of the POGenerator you
     *            use.
     */
    public static void special(String code) {
    }
}
