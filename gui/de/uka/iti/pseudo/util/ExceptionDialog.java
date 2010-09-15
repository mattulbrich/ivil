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

/*
 * this class has originally been written for "verbtrainer"
 */

package de.uka.iti.pseudo.util;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * This code was edited or generated using CloudGarden's Jigloo SWT/Swing GUI
 * Builder, which is free for non-commercial use. If Jigloo is being used
 * commercially (ie, by a corporation, company or business for any purpose
 * whatever) then you should purchase a license for each developer using Jigloo.
 * Please visit www.cloudgarden.com for details. Use of Jigloo implies
 * acceptance of these licensing terms. A COMMERCIAL LICENSE HAS NOT BEEN
 * PURCHASED FOR THIS MACHINE, SO JIGLOO OR THIS CODE CANNOT BE USED LEGALLY FOR
 * ANY CORPORATE OR COMMERCIAL PURPOSE.
 */
public class ExceptionDialog extends JDialog {
    private static final long serialVersionUID = -3300467843405170589L;

    private Icon errorIcon = UIManager.getIcon("OptionPane.errorIcon");
    private JLabel jIcon;
    private JTextArea jTextArea;
    private JScrollPane jScrollPane;
    private JToggleButton jDetails;
    private JButton jOK;
    private JPanel jPanel1;
    private JComponent jError;
    private Throwable exception;
    private String message;
    private Dimension firstSize;

    private static int MIN_WIDTH = 300;
    private static int LINE_LENGTH = 72;

    private ExceptionDialog(Window w, String message, Throwable throwable) {
        super(w, "Fehler", ModalityType.APPLICATION_MODAL);
        this.exception = throwable;
        this.message = message;
        initGUI();
        firstSize = getSize();
    }

//    private ExceptionDialog() {
//        this(new JFrame(), new Exception("test"));
//    }

    private void initGUI() {
        {
            GridBagLayout thisLayout = new GridBagLayout();
            thisLayout.columnWidths = new int[] { 0, MIN_WIDTH };
            getContentPane().setLayout(thisLayout);
            this.setSize(282, 195);
            {
                jIcon = new JLabel();
                getContentPane().add(
                        jIcon,
                        new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
                                GridBagConstraints.CENTER,
                                GridBagConstraints.NONE, new Insets(20, 20, 10,
                                        10), 0, 0));
                jIcon.setIcon(errorIcon);
            }
            {
                jError = mkErrorPanel();
                getContentPane().add(
                        jError,
                        new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0,
                                GridBagConstraints.CENTER,
                                GridBagConstraints.BOTH, new Insets(20, 10, 10,
                                        20), 0, 0));
                // jError.setHorizontalAlignment(SwingConstants.CENTER);
                // jError.setHorizontalTextPosition(SwingConstants.CENTER);
            }
            {
                jPanel1 = new JPanel();
                getContentPane().add(
                        jPanel1,
                        new GridBagConstraints(0, 1, 2, 1, 0.0, 0.0,
                                GridBagConstraints.EAST,
                                GridBagConstraints.NONE,
                                new Insets(0, 0, 0, 0), 0, 0));
                {
                    jDetails = new JToggleButton();
                    jPanel1.add(jDetails);
                    jDetails.setText("Details ...");
                    jDetails.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent evt) {
                            jDetailsActionPerformed(evt);
                        }
                    });
                }
                {
                    jOK = new JButton();
                    jPanel1.add(jOK);
                    jOK.setText("OK");
                    jOK.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent evt) {
                            setVisible(false);
                        }
                    });
                }
            }
            {
                jScrollPane = new JScrollPane();
                {
                    jTextArea = new JTextArea();
                    jTextArea.setTabSize(1);
                    jScrollPane.setViewportView(jTextArea);
                }
            }
        }
        pack();
    }

    private void jDetailsActionPerformed(ActionEvent evt) {
        if (jDetails.isSelected()) {
            getContentPane().add(
                    jScrollPane,
                    new GridBagConstraints(0, 2, 2, 1, 0.0, 1.0,
                            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                            new Insets(10, 10, 10, 10), 0, 0));
            StringWriter out = new StringWriter();
            exception.printStackTrace(new PrintWriter(out));
            jTextArea.setText(out.toString());
            // jTextArea.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            setSize(new Dimension(firstSize.width, firstSize.height * 2));
        } else {
            getContentPane().remove(jScrollPane);
            pack();
            setSize(firstSize);
        }
    }

    // from BasicOptionPaneUI.
    /**
     * Recursively creates new JLabel instances to represent <code>d</code>.
     * Each JLabel instance is added to <code>c</code>.
     */
    private JComponent mkErrorPanel() {
        // Primitive line wrapping

        if (message == null)
            return new JLabel("");

        if (message.length() <= LINE_LENGTH)
            return new JLabel(message);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        while (message.length() > LINE_LENGTH) {
            int p = message.lastIndexOf(' ', LINE_LENGTH);
            if (p <= 0) {
                p = message.indexOf(' ', LINE_LENGTH);
            }

            if (p > 0) {
                panel.add(new JLabel(message.substring(0, p)));
                message = message.substring(p + 1);
            } else {
                panel.add(new JLabel(message));
                message = "";
            }
        }

        if (!message.trim().isEmpty())
            panel.add(new JLabel(message));

        return panel;
    }

    public static void showExceptionDialog(Window parentComponent,
            Throwable throwable) {
        showExceptionDialog(parentComponent, throwable.getLocalizedMessage(),
                throwable);
    }

    public static void showExceptionDialog(Window owner, String message) {
        showExceptionDialog(owner, new StackTraceThrowable(message));
    }

    public static void showExceptionDialog(final Window parentComponent,
            final String message, final Throwable throwable) {

        // ensure excetption dialogs dont interrupt awt
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                ExceptionDialog dlg = new ExceptionDialog(parentComponent,
                        message, throwable);
                dlg.setLocationRelativeTo(parentComponent);
                dlg.setVisible(true);
                dlg.dispose();
            }
        });
    }

    public static void main(String[] args) {
        showExceptionDialog(new JFrame(), new NullPointerException(
                "Some more eleborate error message"));
        showExceptionDialog(new JFrame(), "some other error");
        showExceptionDialog(new JFrame(), "message and exception", new Exception("this should not appear"));
        showExceptionDialog(
                new JFrame(),
                "ugly looooooooooong error with lots of details, for instance http://java.sun.com/j2se/1.4.2/docs/api/java/awt/GridBagLayout.html#columnWidths");
        showExceptionDialog(
                new JFrame(),
                "1234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890");
        showExceptionDialog(
                new JFrame(),
                "1234567890 1234567890 1234567890 1234567890 1234567890 1234567890 1234567890 1234567890 1234567890 1234567890");
        System.exit(0);
    }

    private static class StackTraceThrowable extends Throwable {

        private static final long serialVersionUID = 4003181857244674111L;

        public StackTraceThrowable(String message) {
            super(message);
        }

        public void printStackTrace(PrintWriter s) {
            synchronized (s) {
                s.println("Stack Trace:");
                StackTraceElement[] trace = getStackTrace();
                for (int i = 1; i < trace.length; i++)
                    s.println("\tat " + trace[i]);
            }
        }
    }

}
