package de.uka.iti.pseudo.util;

import java.applet.Applet;
import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.AWTEventListener;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JPopupMenu;
import javax.swing.Popup;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class PopupDisappearListener implements AWTEventListener, PropertyChangeListener {

    private Popup popup;
    private Component component;

    public PopupDisappearListener(Popup popup, Component component) {
        super();
        this.popup = popup;
        this.component = component;

        install();
    }

    private void install() {
        Toolkit tk = Toolkit.getDefaultToolkit();
        tk.addAWTEventListener(this,
                AWTEvent.MOUSE_EVENT_MASK // |
//                AWTEvent.MOUSE_MOTION_EVENT_MASK |
//                AWTEvent.MOUSE_WHEEL_EVENT_MASK |
//                AWTEvent.WINDOW_EVENT_MASK | sun.awt.SunToolkit.GRAB_EVENT_MASK
                );
        component.addPropertyChangeListener("finished", this);
    }

    private void uninstall() {
        Toolkit tk = Toolkit.getDefaultToolkit();
        tk.removeAWTEventListener(this);
        //component.removeComponentListener(this);
    }

    private void cancelPopup() {
        if(component.isVisible()) {
            popup.hide();
        }
        uninstall();
    }
    
    boolean isInPopup(Component src) {
        for (Component c=src; c!=null; c=c.getParent()) {
            System.out.println(c);
            if(c == component)
                return true;
        }
        return false;
    }


    public void eventDispatched(AWTEvent event) {
        Component src = (Component)event.getSource();
        
        if (event.getID() != MouseEvent.MOUSE_CLICKED)
            return;
        
        if (isInPopup(src))
            return;
        
        cancelPopup();
        boolean consumeEvent =
            UIManager.getBoolean("PopupMenu.consumeEventOnClose");
            
        // Consume the event so that normal processing stops.
        if(consumeEvent) {
            ((MouseEvent)event).consume();
        }
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if(evt.getPropertyName().equals("finished") && evt.getNewValue().equals(true))
            cancelPopup();
    }

}
