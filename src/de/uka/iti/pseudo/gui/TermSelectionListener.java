package de.uka.iti.pseudo.gui;

import java.util.EventListener;

import de.uka.iti.pseudo.proof.TermSelector;
import de.uka.iti.pseudo.term.Sequent;

public interface TermSelectionListener extends EventListener {

    void termSelected(Sequent sequent, TermSelector termSelector);

}
