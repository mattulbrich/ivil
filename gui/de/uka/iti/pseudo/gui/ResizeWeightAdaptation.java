/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2010 Universitaet Karlsruhe, Germany
 *    written by Mattias Ulbrich
 * 
 * The system is protected by the GNU General Public License. 
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.gui;

import java.awt.Component;

import javax.swing.JPanel;
import javax.swing.JSplitPane;

import com.javadocking.dock.Dock;
import com.javadocking.event.DockingEvent;
import com.javadocking.event.DockingListener;

/**
 * ResizeWeightAdaptation is used to adapt the resize weight of the
 * {@link JSplitPane} within a SplitDock.
 * 
 * Unfortunately, this weight is hard-coded in SplitDock:
 * 
 * <pre>
 * splitPane = DockingManager.getComponentFactory().createJSplitPane();
 * // ...
 * 
 * splitPane.setResizeWeight(0.5);
 * add(splitPane, BorderLayout.CENTER);
 * 
 * // Inform the listeners about the removal.
 * dockingEventSupport.fireDockingChanged(new ChildDockEvent(this, null, this, leftDock));
 * dockingEventSupport.fireDockingChanged(new ChildDockEvent(this, null, this, rightDock));
 * </pre>
 * 
 * If this listener is registered with a SplitDock, then the
 * {@link #dockingChanged(DockingEvent)} method is called (even twice) after the
 * creation of the SplitPane. We can use this opportunity to set the desired
 * resize weight.
 * 
 * @author mattias ulbrich
 */
class ResizeWeightAdaptation implements DockingListener {

    private double resizeWeight;

    public ResizeWeightAdaptation(double value) {
        this.resizeWeight = value;
    }

    @Override
    public void dockingChanged(DockingEvent dockingEvent) {
        System.err.println(dockingEvent);
        Dock dock = dockingEvent.getDestinationDock();
        if (dock instanceof JPanel) {
            JPanel panel = (JPanel) dock;
            Component component = panel.getComponent(0);
            if (component instanceof JSplitPane) {
                JSplitPane splitpane = (JSplitPane) component;
                System.err.println("setting new resize weight " + resizeWeight + ", was: "
                        + splitpane.getResizeWeight());
                splitpane.setResizeWeight(resizeWeight);
            }
        }
    }

    @Override
    public void dockingWillChange(DockingEvent dockingEvent) {
    }

}