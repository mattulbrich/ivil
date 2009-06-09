package de.uka.iti.pseudo.gui;

import java.awt.BorderLayout;
import java.io.IOException;
import java.net.URL;

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

import de.uka.iti.pseudo.gui.bar.BarManager;
import de.uka.iti.pseudo.proof.ProofNode;


// the center of this all
// TODO DOC

public class MainWindow extends JFrame {

    private ProofCenter proofCenter;

    private SequentComponent sequentComponent;

    private GoalList goalList;

    private ProofComponent proofComponent;

    private RuleApplicationComponent ruleApplicationComponent;

    private BarManager barManager;
    
    
    public MainWindow(ProofCenter proofCenter) throws IOException {
        super("Pseudo");
        this.proofCenter = proofCenter;
        makeGUI();
    }


    private void makeGUI() throws IOException {
        
        final JSplitPane content = new JSplitPane();
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(content, BorderLayout.CENTER);
        
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
            sequentComponent.addTermSelectionListener(proofCenter);
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
                public void valueChanged(TreeSelectionEvent e) {
                    ProofNode selectedProofNode = proofComponent.getSelectedProofNode();
                    if(selectedProofNode != null)
                        proofCenter.fireSelectedProofNode(selectedProofNode);
                }
            });
            JScrollPane scroll = new JScrollPane(proofComponent);
            Dockable dock = new DefaultDockable("proof", scroll, "Proof tree");
            tabDock.addDockable(dock, new Position(1));
        }
        {
            ruleApplicationComponent = new RuleApplicationComponent(proofCenter);
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
        {
            barManager = new BarManager(proofCenter, null);
            URL resource = getClass().getResource("bar/menu.properties");
            if(resource == null)
                throw new IOException("resource bar/menu.properties not found");
            setJMenuBar(barManager.makeMenubar(resource));
            getContentPane().add(barManager.makeToolbar(resource), BorderLayout.NORTH);
        }

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
