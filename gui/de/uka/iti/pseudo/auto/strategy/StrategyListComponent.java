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
package de.uka.iti.pseudo.auto.strategy;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.border.Border;

import de.uka.iti.pseudo.gui.parameters.ParameterSheet;
import de.uka.iti.pseudo.util.GUIUtil;
import de.uka.iti.pseudo.util.Util;

@SuppressWarnings("serial") 
public class StrategyListComponent extends JPanel implements PropertyChangeListener {

    private static final Icon ADD = GUIUtil.makeIcon(StrategyListComponent.class.getResource("add.png"));
    private static final Icon ARROW_DOWN = GUIUtil.makeIcon(StrategyListComponent.class.getResource("arrow_down.png"));
    private static final Icon ARROW_UP = GUIUtil.makeIcon(StrategyListComponent.class.getResource("arrow_up.png"));
    private static final Icon DELETE = GUIUtil.makeIcon(StrategyListComponent.class.getResource("delete.png"));
    private CompoundStrategy compoundStrategy;
    
    public StrategyListComponent() {
        addPropertyChangeListener(ParameterSheet.PARAM_SETTER, this);
    }  
    
    public void propertyChange(PropertyChangeEvent evt) {
        compoundStrategy = (CompoundStrategy) getClientProperty(ParameterSheet.PARAM_OBJECT);
        init();
    }
    
    private void init() {
        setLayout(new BorderLayout());
        {
            String caption = (String) getClientProperty(ParameterSheet.PARAM_SHORTDESC);
            Border border = BorderFactory.createTitledBorder(caption);
            setBorder(border);
        }
        JToolBar bar = new JToolBar();
        {
            bar.setFloatable(false);
            add(bar, BorderLayout.NORTH);
            // switch of that gradient in the background of the toolbar
            bar.setBackground(new Color(getBackground().getRGB()));
        }
        final DefaultListModel model = new DefaultListModel();
        final JList list = new JList(model);
        {
            for (Strategy s : compoundStrategy.getStrategies()) {
                model.addElement(s);
            }
        }
        {
            JButton add = new JButton(ADD);
            add.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    Strategy s = (Strategy)JOptionPane.showInputDialog(null,
                            "Choose the strategy to add", "Add strategy",
                            JOptionPane.QUESTION_MESSAGE, null, compoundStrategy
                                    .getAllStrategies().toArray(), null);
                    Log.println(s);
                    if(s != null) {
                        model.addElement(s);
                        writeBack(model);
                    }
                }
            });
            bar.add(add);
        }
        {
            JButton delete = new JButton(DELETE);
            delete.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    int index = list.getSelectedIndex();
                    if(index >= 0) {
                        model.remove(index);
                        writeBack(model);
                    }
                }
            });
            bar.add(delete);
        }
        {
            JButton up = new JButton(ARROW_UP);
            up .addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    int index = list.getSelectedIndex();
                    if(index > 0) {
                        Object o = model.remove(index);
                        model.add(index-1, o);
                        list.setSelectedIndex(index-1);
                        writeBack(model);
                    }
                }
            });
            bar.add(up);
        }
        {
            JButton down = new JButton(ARROW_DOWN);
            down .addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    int index = list.getSelectedIndex();
                    if(index < model.size()-1) {
                        Object o = model.remove(index);
                        model.add(index+1, o);
                        list.setSelectedIndex(index+1);
                        writeBack(model);
                    }
                }
            });
            bar.add(down);
        }
        {
            JScrollPane scroll = new JScrollPane(list);
            Dimension d = new Dimension(50, 100);
            scroll.setMinimumSize(d);
            add(scroll, BorderLayout.CENTER);
        }
    }

    private void writeBack(DefaultListModel model) {
        Strategy[] array = new Strategy[model.size()]; 
        model.copyInto(array);
        compoundStrategy.setStrategies(Util.readOnlyArrayList(array));
    }
    
}
