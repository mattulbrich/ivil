package de.uka.iti.pseudo.gui;

import java.awt.BorderLayout;
import java.awt.Container;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import com.javadocking.DockingManager;
import com.javadocking.dock.Position;
import com.javadocking.dock.TabDock;
import com.javadocking.dockable.DefaultDockable;
import com.javadocking.dockable.Dockable;
import com.javadocking.model.FloatDockModel;

import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.proof.Proof;


// the center of this all

public class MainWindow extends JFrame {

    private Proof proof;

    private Environment env;

    private SequentComponent sequentComponent;

    private GoalList goalList;

    private ProofComponent proofComponent;

    private RuleApplicationComponent ruleApplicationComponent;
    
    
    public MainWindow(Proof proof, Environment env) {
        super("Pseudo");
        this.proof = proof;
        this.env = env;
        makeGUI();
    }


    private void makeGUI() {
        Container content = getContentPane();
        
        // Create the dockings
        TabDock leftDock = new TabDock();
        {
            // Create the dock model for the docks.
            FloatDockModel dockModel = new FloatDockModel();
            dockModel.addOwner("mainFrame", this);
            // Give the dock model to the docking manager.
            DockingManager.setDockModel(dockModel);

            content.add(leftDock, BorderLayout.WEST);
        }
        {
            sequentComponent = new SequentComponent(env);
            content.add(sequentComponent, BorderLayout.CENTER);
        }
        {
            goalList = new GoalList(proof, env);
            JScrollPane scroll = new JScrollPane(goalList);
            Dockable dock = new DefaultDockable("goallist", scroll, "Goal list");
            leftDock.addDockable(dock, new Position(0));
        }
        {
            proofComponent = new ProofComponent(proof);
            JScrollPane scroll = new JScrollPane(proofComponent);
            Dockable dock = new DefaultDockable("proof", scroll, "Proof tree");
            leftDock.addDockable(dock, new Position(1));
        }
        {
            ruleApplicationComponent = new RuleApplicationComponent(env);
            JScrollPane scroll = new JScrollPane(ruleApplicationComponent);
            Dockable dock = new DefaultDockable("ruleApp", scroll, "Rule Application");
            leftDock.addDockable(dock, new Position(2));
        }
        {
            JPanel settings = new JPanel();
            settings.add(new JLabel("yet to come ..."));
            Dockable dock = new DefaultDockable("settings", settings, "Settings");
            leftDock.addDockable(dock, new Position(3));
        }
    }
   
}
