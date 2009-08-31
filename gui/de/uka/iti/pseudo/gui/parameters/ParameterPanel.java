package de.uka.iti.pseudo.gui.parameters;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToolBar;

import de.uka.iti.pseudo.auto.strategy.Strategy;
import de.uka.iti.pseudo.auto.strategy.StrategyManager;
import de.uka.iti.pseudo.gui.ProofCenter;
import de.uka.iti.pseudo.gui.VerticalLayout;

public class ParameterPanel extends JPanel {

    private ProofCenter proofCenter;
    private JComboBox activeStrategySelector;
    private JComboBox paramSelector;
    private ParameterSheet paramSheet;

    public ParameterPanel(ProofCenter proofCenter) {
        this.proofCenter = proofCenter;
        init();
    }

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
        add(new JToolBar.Separator());
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
    private JPanel makeTitledPanel(String title) {
        JPanel result = new JPanel(new VerticalLayout());
        result.setBorder(BorderFactory.createTitledBorder(title));
        return result;
    }
    
}
