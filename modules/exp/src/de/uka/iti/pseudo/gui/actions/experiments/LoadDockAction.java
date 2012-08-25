package de.uka.iti.pseudo.gui.actions.experiments;

import java.awt.event.ActionEvent;

import com.javadocking.DockingManager;
import com.javadocking.model.DockModel;
import com.javadocking.model.codec.DockModelDecoder;
import com.javadocking.model.codec.DockModelPropertiesDecoder;

import de.uka.iti.pseudo.gui.actions.BarAction;

public class LoadDockAction extends BarAction {

    @Override
    public void actionPerformed(ActionEvent e) {
        DockModel model = DockingManager.getDockModel();
        DockModelDecoder decoder = new DockModelPropertiesDecoder();
//        try {
//            decoder.decode(model, "/tmp/dockingModel.dck");
//        } catch (Exception ex) {
//            ExceptionDialog.showExceptionDialog(getParentFrame(), ex);
//        }
    }

}
