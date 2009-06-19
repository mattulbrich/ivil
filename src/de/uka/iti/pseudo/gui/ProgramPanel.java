package de.uka.iti.pseudo.gui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.Date;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JPanel;

import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.environment.Program;
import de.uka.iti.pseudo.proof.ProofNode;
import de.uka.iti.pseudo.proof.RuleApplication;

public class ProgramPanel extends JPanel implements ProofNodeSelectionListener {

    private Environment env;
    private ProgramComponent programComponent;
    private int numberOfKnownPrograms = 0;
    private JComboBox selectionBox;
    
    public ProgramPanel(Environment environment) {
        env = environment;
        init();
    }

    private void init() {
        setLayout(new BorderLayout());
        {
            programComponent = new ProgramComponent();
            add(programComponent, BorderLayout.CENTER);
        }
        {
            selectionBox = new JComboBox();
            selectionBox.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    selectProgram();
                }
            });
            add(selectionBox, BorderLayout.NORTH);
            updatePrograms();
        }
    }

    private void updatePrograms() {
        Collection<Program> programs = env.getPrograms();
        
        if(programs.size() == numberOfKnownPrograms)
            return;
        
        DefaultComboBoxModel model = new DefaultComboBoxModel(programs.toArray());
        selectionBox.setModel(model);
        selectProgram();
        
        numberOfKnownPrograms = programs.size();
    }
    
    private void selectProgram() {
        Program p = (Program) selectionBox.getSelectedItem();
        programComponent.setProgram(p);
        programComponent.repaint();
    }

    public void proofNodeSelected(ProofNode node) {
        updatePrograms();
        programComponent.setProofNode(node);
    }

    public void ruleApplicationSelected(RuleApplication ruleApplication) {
        // do nothing
    }

}
