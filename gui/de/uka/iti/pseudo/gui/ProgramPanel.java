package de.uka.iti.pseudo.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JPanel;

import de.uka.iti.pseudo.auto.strategy.BreakpointManager;
import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.environment.Program;
import de.uka.iti.pseudo.gui.source.BreakpointPane;
import de.uka.iti.pseudo.proof.ProofNode;
import de.uka.iti.pseudo.proof.RuleApplication;
import de.uka.iti.pseudo.term.statement.Statement;

public class ProgramPanel extends JPanel implements ProofNodeSelectionListener {

    private static final Color PROGRAM_COLOR =
        Main.getColor("pseudo.program.boogiecolor");
    
    private Environment env;
    private BreakpointPane programComponent;
    private int numberOfKnownPrograms = 0;
    private JComboBox selectionBox;
    private ProofCenter proofCenter;
    private PrettyPrint prettyPrinter;

    private BreakpointManager breakpointManager;
    
    public ProgramPanel(ProofCenter proofCenter) throws IOException {
        this.env = proofCenter.getEnvironment();
        this.proofCenter = proofCenter;
        this.prettyPrinter = proofCenter.getPrettyPrinter();
        this.breakpointManager = proofCenter.getBreakpointManager();
        init();
    }

    private void init() throws IOException {
        setLayout(new BorderLayout());
        {
            programComponent = new BreakpointPane(breakpointManager, false);
            programComponent.setForeground(PROGRAM_COLOR);
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
        Collection<Program> programs = env.getAllPrograms();
        
        if(programs.size() == numberOfKnownPrograms)
            return;
        
        DefaultComboBoxModel model = new DefaultComboBoxModel(programs.toArray());
        selectionBox.setModel(model);
        selectProgram();
        
        numberOfKnownPrograms = programs.size();
    }
    
    private void selectProgram() {
        Program p = (Program) selectionBox.getSelectedItem();
        programComponent.setText(toString(p));
        programComponent.setBreakPointResource(p);
    }
    
    private String toString(Program p) {
        StringBuilder sb = new StringBuilder();
        List<Statement> statements = p.getStatements();
        for (int i = 0; i < statements.size(); i ++) {
            sb.append(String.format("%3d: %s\n", i, prettyPrinter.print(statements.get(i)).toString()));
        }
        return sb.toString();
    }

    public void proofNodeSelected(ProofNode node) {
        updatePrograms();
//        programComponent.setProofNode(node);
    }

    public void ruleApplicationSelected(RuleApplication ruleApplication) {
        // do nothing
    }

}
