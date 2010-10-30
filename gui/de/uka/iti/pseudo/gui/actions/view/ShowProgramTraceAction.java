package de.uka.iti.pseudo.gui.actions.view;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import de.uka.iti.pseudo.gui.ProofCenter;
import de.uka.iti.pseudo.gui.actions.BarAction;
import de.uka.iti.pseudo.gui.actions.BarManager.InitialisingAction;
import de.uka.iti.pseudo.gui.source.BreakpointPane;
import de.uka.iti.pseudo.util.Log;

/**
 * A simple action to toggle displaying of program traces in CodePanels
 * 
 * @author timm.felden@felden.com
 * 
 */
public class ShowProgramTraceAction extends BarAction implements PropertyChangeListener, InitialisingAction {

    private static final long serialVersionUID = 1772850577541959862L;
    
    private final boolean DEFAULT_SHOW_TRACE = true;

    public ShowProgramTraceAction() {
        super("Highlight program trace");
    }

    public void actionPerformed(ActionEvent e) {
        ProofCenter pc = getProofCenter();
        Log.log(Log.TRACE, "show program trace activated, isSelected:" + isSelected());
        pc.firePropertyChange(ProofCenter.CODE_PANE_SHOW_TRACE, isSelected());
    }

    public void propertyChange(PropertyChangeEvent evt) {
        Log.enter(evt);
        setSelected((Boolean) evt.getNewValue());
    }

    public void initialised() {
        ProofCenter pc = getProofCenter();
        pc.addPropertyChangeListener(ProofCenter.CODE_PANE_SHOW_TRACE, this);
        if (null == pc.getProperty(ProofCenter.CODE_PANE_SHOW_TRACE))
            getProofCenter().firePropertyChange(ProofCenter.CODE_PANE_SHOW_TRACE, DEFAULT_SHOW_TRACE);
        else
            setSelected((Boolean) pc.getProperty(ProofCenter.CODE_PANE_SHOW_TRACE));
    }
}