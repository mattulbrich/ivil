package de.uka.iti.pseudo.gui.actions.experiments;

import java.awt.event.ActionEvent;
import java.util.Iterator;

import com.javadocking.DockingManager;
import com.javadocking.dock.CompositeDock;
import com.javadocking.dock.Dock;
import com.javadocking.dock.LeafDock;
import com.javadocking.model.DockModel;
import com.javadocking.model.codec.DockModelEncoder;
import com.javadocking.model.codec.DockModelPropertiesEncoder;

import de.uka.iti.pseudo.gui.actions.BarAction;
import de.uka.iti.pseudo.util.ExceptionDialog;

public class SaveDockAction extends BarAction {

    @Override
    public void actionPerformed(ActionEvent e) {
        DockModel model = DockingManager.getDockModel();
        Iterator it = model.getRootKeys(getProofCenter().getMainWindow());
        while(it.hasNext()) {
            String rootId = (String) it.next();
            System.out.println(rootId);
            System.out.println(out(model, model.getRootDock(rootId)));
        }
        DockModelEncoder encoder = new DockModelPropertiesEncoder();
        try {
            encoder.export(model, "/tmp/dockingModel.dck");
        } catch (Exception ex) {
            ExceptionDialog.showExceptionDialog(getParentFrame(), ex);
        }
    }

    private String out(DockModel model, Dock dock) {
        String result = ("(" + dock.getClass().getSimpleName());
        if (dock instanceof LeafDock) {
            LeafDock leafDock = (LeafDock) dock;
            for(int i = 0; i < leafDock.getDockableCount(); i++) {
                result += (" " + leafDock.getDockable(i).getID());
            }
        } else if (dock instanceof CompositeDock) {
            CompositeDock compDock = (CompositeDock) dock;
            for(int i = 0; i  < compDock.getChildDockCount(); i++) {
                result += " " + out(model, compDock.getChildDock(i));
            }
        }
        return result + ")";
    }

}
