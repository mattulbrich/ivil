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
package de.uka.iti.pseudo.gui.parameters;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;

import de.uka.iti.pseudo.auto.strategy.Strategy;
import de.uka.iti.pseudo.auto.strategy.StrategyManager;
import de.uka.iti.pseudo.gui.ProofCenter;
import de.uka.iti.pseudo.gui.VerticalLayout;

public class ParameterPanel extends JPanel {

    private static final long serialVersionUID = -8785480731800659193L;
    
    private ProofCenter proofCenter;
    private JComboBox activeStrategySelector;
    private JComboBox paramSelector;
    private ParameterSheet paramSheet;

    public ParameterPanel(ProofCenter proofCenter) {
        this.proofCenter = proofCenter;
        init();
    }

    @SuppressWarnings("serial")
    private void init() {
        final StrategyManager strategyManager = proofCenter.getStrategyManager();
        setLayout(new VerticalLayout());
        {
            add(new JLabel("Active strategy:"));
            activeStrategySelector = new JComboBox(strategyManager.getAllStrategies().toArray());
            System.out.println(strategyManager.getSelectedStrategy());
            activeStrategySelector.setSelectedItem(strategyManager.getSelectedStrategy());
            activeStrategySelector.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    Strategy selectedItem = (Strategy) activeStrategySelector.getSelectedItem();
                    strategyManager.setSelectedStrategy(selectedItem);
                }
            });
            add(activeStrategySelector);
        }
        {
            JComponent separator = new JComponent() { 
                protected void paintComponent(Graphics g) {
                    g.setColor(UIManager.getColor("TabbedPane.selected"));
                    int h = getHeight()/2;
                    g.fill3DRect(5, h-1, getWidth()-10, 2, false);
                }
            };
            separator.setPreferredSize(new Dimension(30,30));
            add(separator);
        }
        {
            add(new JLabel("Configure strategy:"));
            paramSelector = new JComboBox(strategyManager.getAllStrategies().toArray());
            paramSelector.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    makeParameterSheet(paramSelector.getSelectedItem());
                }
            });
            add(paramSelector);
            
            // display properties of default item
            makeParameterSheet(paramSelector.getSelectedItem());
        }
    }
    
    protected void makeParameterSheet(Object selectedItem) {
        if(paramSheet != null) {
            remove(paramSheet);
        }
        
        try {
            paramSheet = new ParameterSheet(selectedItem);
        } catch (Exception e) {
            throw new Error(e);
        }
        
        add(paramSheet);
        revalidate();
        repaint();
    }

    /*
     * Make a new titled panel using the BorderFactory
     */
    @SuppressWarnings("unused")
    private JPanel makeTitledPanel(String title) {
        JPanel result = new JPanel(new VerticalLayout());
        result.setBorder(BorderFactory.createTitledBorder(title));
        return result;
    }
    
}
