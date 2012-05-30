/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2012 Karlsruhe Institute of Technology
 *
 * The system is protected by the GNU General Public License.
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.gui.actions.editor;

import java.awt.Container;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;

import org.fife.ui.rtextarea.SearchEngine;

import de.uka.iti.pseudo.gui.actions.BarAction;

@SuppressWarnings("serial")
public class OpenSearchDialogAction extends BarAction {

    private SearchDialog dialog;

    @Override
    public void actionPerformed(ActionEvent e) {
        if(dialog == null) {
            dialog = new SearchDialog(getParentFrame(), getEditor().getEditPane());
        }
        
        dialog.setLocationRelativeTo(getParentFrame());
        dialog.setVisible(true);
    }

}

@SuppressWarnings("serial")
class SearchDialog extends JDialog implements ActionListener {
    
    private static final int HISTORY_LENGTH = 30;
    private JTextArea textArea;
    private JComboBox searchField;
    private JCheckBox regexCB;
    private JCheckBox matchCaseCB;

    public SearchDialog(Frame parent, JTextArea textArea) {
        super(parent, "Search");
        this.textArea = textArea;
        initGUI();
    }

    private void initGUI() {
        Container cp = getContentPane();
        cp.setLayout(new GridBagLayout());
        {
            searchField = new JComboBox();
            searchField.setEditable(true);
            cp.add(searchField, new GridBagConstraints(0, 0, 4, 1, 1.0, 1.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(5, 5, 5, 5), 0, 0));
        }
        {
            JButton b = new JButton("Find Next");
            b.setActionCommand("FindNext");
            b.addActionListener(this);
            cp.add(b, new GridBagConstraints(0, 1, 1, 1, 0, 0,
                    GridBagConstraints.WEST, GridBagConstraints.BOTH,
                    new Insets(5, 5, 5, 5), 0, 0));
            getRootPane().setDefaultButton(b);
        }
        {
            JButton b = new JButton("Find Previous");
            b.setActionCommand("FindPrev");
            b.addActionListener(this);
            cp.add(b, new GridBagConstraints(1, 1, 1, 1, 0, 0,
                    GridBagConstraints.WEST, GridBagConstraints.BOTH,
                    new Insets(5, 5, 5, 5), 0, 0));
        }
        {
            regexCB = new JCheckBox("Regular Expression");
            cp.add(regexCB, new GridBagConstraints(2, 1, 1, 1, 0, 0,
                    GridBagConstraints.WEST, GridBagConstraints.BOTH,
                    new Insets(5, 5, 5, 5), 0, 0));
        }
        {
            matchCaseCB = new JCheckBox("Match Case");
            cp.add(matchCaseCB, new GridBagConstraints(3, 1, 1, 1, 0, 0,
                    GridBagConstraints.WEST, GridBagConstraints.BOTH,
                    new Insets(5, 5, 5, 5), 0, 0));
        }
        setResizable(false);
        pack();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        
        String command = e.getActionCommand();

        if("FindNext".equals(command)) {
           String text = (String) searchField.getSelectedItem();
           if (text.length() == 0) {
              return;
           }
           boolean forward = true;
           boolean matchCase = matchCaseCB.isSelected();
           boolean wholeWord = false;
           boolean regex = regexCB.isSelected();
           boolean found = SearchEngine.find(textArea, text, forward,
                 matchCase, wholeWord, regex);
           addToHistory(text);
           if (!found) {
              JOptionPane.showMessageDialog(this, "Text not found");
           }
        }

        else if ("FindPrev".equals(command)) {
           String text = (String) searchField.getSelectedItem();
           if (text.length() == 0) {
              return;
           }
           boolean forward = false;
           boolean matchCase = matchCaseCB.isSelected();
           boolean wholeWord = false;
           boolean regex = regexCB.isSelected();
           boolean found = SearchEngine.find(textArea, text, forward,
                 matchCase, wholeWord, regex);
           addToHistory(text);
           if (!found) {
              JOptionPane.showMessageDialog(this, "Text not found");
           }
        }
    }

    private void addToHistory(String text) {
        DefaultComboBoxModel model = (DefaultComboBoxModel) searchField.getModel();
        model.removeElement(text);
        model.insertElementAt(text, 0);
        if(model.getSize() > HISTORY_LENGTH) {
            model.removeElementAt(HISTORY_LENGTH);
        }
        
        model.setSelectedItem(text);
    }
    
}
