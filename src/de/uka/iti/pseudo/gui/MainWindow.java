package de.uka.iti.pseudo.gui;

import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;

import com.javadocking.DockingManager;
import com.javadocking.dock.Position;
import com.javadocking.dock.SplitDock;
import com.javadocking.dock.TabDock;
import com.javadocking.dockable.DefaultDockable;
import com.javadocking.dockable.Dockable;
import com.javadocking.model.FloatDockModel;


// the center of this all

public class MainWindow extends JFrame {

    private ProofCenter proofCenter;

    private SequentComponent sequentComponent;

    private GoalList goalList;

    private ProofComponent proofComponent;

    private RuleApplicationComponent ruleApplicationComponent;
    
    
    public MainWindow(ProofCenter proofCenter) {
        super("Pseudo");
        this.proofCenter = proofCenter;
        makeGUI();
    }


    private void makeGUI() {
        
        final JSplitPane content = new JSplitPane();
        getContentPane().add(content);
        
        // Create the dockings
        TabDock tabDock = new TabDock();
        {
            // Create the enclosing tock
            SplitDock topDock = new SplitDock();
            topDock.addChildDock(tabDock, new Position(Position.TOP));
            
            // Create the dock model for the docks.
            FloatDockModel dockModel = new FloatDockModel();
            dockModel.addOwner("mainFrame", this);
            
            // Give the dock model to the docking manager.
            DockingManager.setDockModel(dockModel);
            
            // add as content 
            content.add(topDock, JSplitPane.LEFT);
            
            // Add the root dock to the dock model.
            dockModel.addRootDock("splitDock", topDock, this);
        }
        {
            sequentComponent = new SequentComponent(proofCenter.getEnvironment());
            sequentComponent.setBorder(new EmptyBorder(5,5,5,5));
            content.add(sequentComponent, JSplitPane.RIGHT);
            proofCenter.addProofNodeSelectionListener(sequentComponent);
        }
        {
            goalList = new GoalList(proofCenter.getProof(), proofCenter.getEnvironment());
            proofCenter.addProofNodeSelectionListener(goalList);
            goalList.addListSelectionListener(new ListSelectionListener() {
                public void valueChanged(ListSelectionEvent e) {
                    if(!e.getValueIsAdjusting())
                        proofCenter.fireSelectedProofNode(goalList.getSelectedProofNode());
                }});
            JScrollPane scroll = new JScrollPane(goalList);
            Dockable dock = new DefaultDockable("goallist", scroll, "Goal list");
            tabDock.addDockable(dock, new Position(0));
        }
        {
            proofComponent = new ProofComponent(proofCenter.getProof());
            proofCenter.addProofNodeSelectionListener(proofComponent);
            proofComponent.addTreeSelectionListener(new TreeSelectionListener() {
                @Override public void valueChanged(TreeSelectionEvent e) {
                    proofCenter.fireSelectedProofNode(proofComponent.getSelectedProofNode());
                }
            });
            JScrollPane scroll = new JScrollPane(proofComponent);
            Dockable dock = new DefaultDockable("proof", scroll, "Proof tree");
            tabDock.addDockable(dock, new Position(1));
        }
        {
            ruleApplicationComponent = new RuleApplicationComponent(proofCenter.getEnvironment());
            proofCenter.addProofNodeSelectionListener(ruleApplicationComponent);
            JScrollPane scroll = new JScrollPane(ruleApplicationComponent);
            Dockable dock = new DefaultDockable("ruleApp", scroll, "Rule Application");
            tabDock.addDockable(dock, new Position(2));
        }
        {
            JPanel settings = new JPanel();
            settings.add(new JLabel("yet to come ..."));
            Dockable dock = new DefaultDockable("settings", settings, "Settings");
            tabDock.addDockable(dock, new Position(3));
        }
        
        new Timer().schedule(new TimerTask() {

            @Override public void run() {
                System.out.println(content.getMaximumDividerLocation());
                System.out.println(content.getMinimumDividerLocation());
            } }, 0, 1000);
    }


    public SequentComponent getSequentComponent() {
        return sequentComponent;
    }


    public GoalList getGoalList() {
        return goalList;
    }


    public ProofComponent getProofComponent() {
        return proofComponent;
    }


    public RuleApplicationComponent getRuleApplicationComponent() {
        return ruleApplicationComponent;
    }
   
}
