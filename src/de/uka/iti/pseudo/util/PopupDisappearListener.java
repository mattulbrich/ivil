package de.uka.iti.pseudo.util;

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.Container;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.UIManager;

public class PopupDisappearListener implements AWTEventListener, PropertyChangeListener {

    private Component component;
    private Container container;

    public PopupDisappearListener(Component component, Container container) {
        super();
        this.component = component;
        this.container = container;

        install();
    }

    private void install() {
        Toolkit tk = Toolkit.getDefaultToolkit();
        tk.addAWTEventListener(this,
                AWTEvent.MOUSE_EVENT_MASK 
                );
        component.addPropertyChangeListener("finished", this);
    }

    private void uninstall() {
        Toolkit tk = Toolkit.getDefaultToolkit();
        tk.removeAWTEventListener(this);
        //component.removeComponentListener(this);
    }

    private void cancelPopup() {
        if(container.isVisible()) {
            container.setVisible(false);
        }
        uninstall();
    }
    
    boolean isInPopup(Component src) {
        for (Component c=src; c!=null; c=c.getParent()) {
            if(c == container)
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
