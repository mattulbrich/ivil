/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2012 Karlsruhe Institute of Technology
 *
 * The system is protected by the GNU General Public License.
 * See LICENSE.TXT (distributed with this file) for details.
 */

package de.uka.iti.pseudo.gui.util;

import java.awt.Point;
import java.awt.Window;

import javax.swing.JOptionPane;

import com.javadocking.DockingExecutor;
import com.javadocking.dock.Dock;
import com.javadocking.dock.LeafDock;
import com.javadocking.dock.Position;
import com.javadocking.dockable.Dockable;
import com.javadocking.model.DockingPath;
import com.javadocking.util.DockingUtil;


/**
 * The Class SameFrameDockingExecutor is a replacement of the default
 * DockingExecutor of the javadocking system.
 *
 * It ensures that {@link Dockable}s can only be moved to another {@link Dock}
 * if the source and target dock belong to the same owner frame.
 *
 * Otherwise an error message window is shown and the transfer is aborted.
 */
public class SameFrameDockingExecutor extends DockingExecutor {

    /**
     * Retrieve the owner windows for the dockable and the target dock and
     * ensure that they are identical.
     *
     * Otherwise show an error message.
     *
     * @param dockable
     *            a dockable which must belong to a registered dock
     * @param destinationDock
     *            a registered dock
     * @return <code>true</code> iff dockable and destinationDock have the same
     *         owner
     */
    private boolean checkSameOwner(Dockable dockable, Dock destinationDock) {
        Dock dock1 = dockable.getDock();
        Dock rootDock1 = DockingUtil.getRootDock(dock1);
        String rootDockKey1 = DockingUtil.getRootDockKey(rootDock1);
        Window owner1 = DockingUtil.getWindowOwner(rootDockKey1);

        Dock rootDock2 = DockingUtil.getRootDock(destinationDock);
        String rootDockKey2 = DockingUtil.getRootDockKey(rootDock2);
        Window owner2 = DockingUtil.getWindowOwner(rootDockKey2);

        if(owner1 == owner2) {
            return true;
        } else {
            JOptionPane.showMessageDialog(owner2,
                    "This panel cannot does not belong to the target window.",
                    "Cannot move panel", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    @Override
    public boolean changeDocking(Dockable dockable, Dock destinationDock,
            Point relativeLocation, Point dockableOffset) {
        if(checkSameOwner(dockable, destinationDock)) {
            return super.changeDocking(dockable, destinationDock, relativeLocation, dockableOffset);
        } else {
            return false;
        }
    }

    @Override
    public boolean changeDocking(Dockable dockable, Dock rootDock) {
        if(checkSameOwner(dockable, rootDock)) {
            return super.changeDocking(dockable, rootDock);
        } else {
            return false;
        }
    }

    @Override
    public boolean changeDocking(Dockable dockable, DockingPath dockingPath) {
        if(checkSameOwner(dockable, dockingPath.getDock(0))) {
            return super.changeDocking(dockable, dockingPath);
        } else {
            return false;
        }
    }

    @Override
    public boolean changeDocking(Dockable dockable, LeafDock destinationDock,
            Position position) {
        if(checkSameOwner(dockable, destinationDock)) {
            return super.changeDocking(dockable, destinationDock, position);
        } else {
            return false;
        }
    }
}