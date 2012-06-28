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

import java.awt.Component;
import java.awt.Graphics;

import javax.swing.Icon;

/**
 * The A little helper class that paints two icons on top of each other.
 *
 * It does not position the icons by any means but paints them on top of each
 * other.
 *
 * The width/height of an overlay icon is the maximum width/height of the two
 * icons.
 */
public class OverlayIcon implements Icon {

    /**
     * The first icon (bottom).
     */
    private final Icon bottomIcon;

    /**
     * The second icon (top).
     */
    private final Icon topIcon;

    /**
     * Instantiates a new overlay icon.
     *
     * @param bottomIcon
     *            the bottom icon
     * @param topIcon
     *            the icon on top
     */
    public OverlayIcon(Icon bottomIcon, Icon topIcon) {
        this.bottomIcon = bottomIcon;
        this.topIcon = topIcon;
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        bottomIcon.paintIcon(c, g, x, y);
        topIcon.paintIcon(c, g, x, y);
    }

    @Override
    public int getIconWidth() {
        return Math.max(bottomIcon.getIconWidth(), topIcon.getIconWidth());
    }

    @Override
    public int getIconHeight() {
        return Math.max(bottomIcon.getIconHeight(), topIcon.getIconHeight());
    }

}
