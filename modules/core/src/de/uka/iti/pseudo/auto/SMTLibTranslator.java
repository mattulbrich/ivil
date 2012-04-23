package de.uka.iti.pseudo.auto;

import java.io.IOException;

import nonnull.NonNull;
import de.uka.iti.pseudo.term.Sequent;
import de.uka.iti.pseudo.term.TermException;

public interface SMTLibTranslator {

    /**
     * Export a sequent to an output stream.
     * 
     * <p>
     * It will translate the sequent, add all axioms from the environment and
     * write everything to the output stream. If new sorts and/or symbols are
     * created, they get declared, too.
     * 
     * @param sequent
     *            the sequent to export
     * 
     * @param builder
     *            the stream to output to
     * 
     * @throws TermException
     *             if the translation fails
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public abstract void export(@NonNull Sequent sequent,
            @NonNull Appendable builder)
            throws TermException, IOException;

}
