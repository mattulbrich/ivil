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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.text.JTextComponent;

import de.uka.iti.pseudo.gui.MainWindow;
import de.uka.iti.pseudo.util.GUIUtil;

/**
 * The Class HistoryEditor is a special case of a combo box.
 *
 * It does NOT use the implementation of {@link JComboBox} since this is not
 * easily configured. Instead it implements the needed features "by hand". This
 * component is used to show an input area (not necessarily a {@link JTextField}
 * ) together with a "history" button.
 *
 * Pressing this button opens a popup dialog containing the recently entered
 * texts. These may be multiline!
 */
public class HistoryEditor extends JPanel {

    private static final long serialVersionUID = 1147455315102890971L;
    private static final String ICON_NAME = "book_open.png";
    private static final Icon ICON =
            GUIUtil.makeIcon(MainWindow.class.getResource("img/" + ICON_NAME));

    private final JTextComponent textComponent;
    private final List<String> choices;
    private final JButton button;

    public HistoryEditor(List<String> instantiations, JTextComponent textComponent) {
        super(new BorderLayout());
        this.textComponent = textComponent;
        this.button = makeHistoryButton();

        add(textComponent, BorderLayout.CENTER);
        add(button, BorderLayout.EAST);

        this.choices = instantiations;

        textComponent.addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {
                if(e.isControlDown() && e.getKeyCode() == KeyEvent.VK_DOWN) {
                    showPopup();
                    e.consume();
                }
            }
        });
    }

    private JButton makeHistoryButton() {
        JButton result = new JButton(ICON);
        result.setBorder(BorderFactory.createEtchedBorder());
        result.setIcon(ICON);
        result.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showPopup();
            }
        });
        return result;
    }

    protected void showPopup() {
        JPopupMenu popup = new JPopupMenu();
        JList list = new JList(choices.toArray());
        {
            list.setCellRenderer(new Renderer(list.getBackground()));
            list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            MouseHandler handler = new MouseHandler(list, popup);
            list.addMouseListener(handler);
            list.addMouseMotionListener(handler);
        }
        JScrollPane scroller = new JScrollPane(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        {
            scroller.setViewportView(list);
            scroller.setMaximumSize(new Dimension(300, 300));
            scroller.setMinimumSize(new Dimension(300, 300));
            scroller.setPreferredSize(new Dimension(300, 300));
            scroller.setBorder(null);
        }
        popup.add(scroller);
        popup.show(textComponent, textComponent.getX(), textComponent.getY()+textComponent.getHeight());
        list.requestFocus();
    }

    private static class TopSeparationBorder implements Border {
        private static final Insets INSETS = new Insets(6,0,0,0);
        private final Color background;

        public TopSeparationBorder(Color background) {
            this.background = background;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            g.setColor(background);
            g.fillRect(x, y, width, 6);
            g.setColor(Color.darkGray);
            g.drawLine(x + 5, y + 3, x + width - 10, y + 3);
        }

        @Override
        public boolean isBorderOpaque() {
            return false;
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return INSETS;
        }
    };

    private class MouseHandler extends MouseAdapter {

        private final JList list;
        private final JPopupMenu popup;

        public MouseHandler(JList list, JPopupMenu popup) {
            this.list = list;
            this.popup = popup;
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            if(SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 1) {
                textComponent.setText(list.getSelectedValue().toString());
                popup.setVisible(false);
            }
        }

        @Override
        public void mouseMoved(MouseEvent e) {
            Point p = e.getPoint();
            int index = list.locationToIndex(p);
            list.setSelectedIndex(index);
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            Point p = e.getPoint();
            int index = list.locationToIndex(p);
            list.setSelectedIndex(index);
        }

    }

    private static class Renderer extends JTextArea implements ListCellRenderer {

        private final Border border;

        public Renderer(Color background) {
            this.border = new TopSeparationBorder(background);
        }

        @Override
        public Component getListCellRendererComponent(
                JList list,
                Object value,
                int index,
                boolean isSelected,
                boolean cellHasFocus) {

            setText(value.toString());

            if(index == 0) {
                setBorder(null);
            } else {
                setBorder(border);
            }

            if (isSelected) {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());
            } else {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
            }


            return this;
        }

    }

}
