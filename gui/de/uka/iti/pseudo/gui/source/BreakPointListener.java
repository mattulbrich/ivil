package de.uka.iti.pseudo.gui.source;

import java.awt.event.ActionListener;

public interface BreakPointListener {

    void breakPointRemoved(Object source, int line);

    void breakPointAdded(Object source, int line);

}
