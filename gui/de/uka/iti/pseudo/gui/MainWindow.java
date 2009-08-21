/*
 * This file is part of PSEUDO
 * Copyright (C) 2009 Universitaet Karlsruhe, Germany
 *    written by Mattias Ulbrich
 * 
 * The system is protected by the GNU General Public License. 
 * See LICENSE.TXT for details.
 */
package de.uka.iti.pseudo.gui;

import java.awt.BorderLayout;
import java.awt.event.WindowListener;
import java.io.IOException;
import java.net.URL;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.WindowConstants;
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

import de.uka.iti.pseudo.gui.bar.BarAction;
import de.uka.iti.pseudo.gui.bar.BarManager;
import de.uka.iti.pseudo.gui.bar.CloseAction;
import de.uka.iti.pseudo.gui.parameters.ParameterPanel;
import de.uka.iti.pseudo.gui.parameters.ParameterSheet;
import de.uka.iti.pseudo.gui.parameters.ParameterTest;
import de.uka.iti.pseudo.gui.source.ProgramPanel;
import de.uka.iti.pseudo.gui.source.SourcePanel;
import de.uka.iti.pseudo.proof.ProofNode;


/**
 * The class MainWindow describes the proof window for one single proof.
 * 
 * The different views are layout using javadocking dockables allowing a very
 * flexible way to look at things.
 */
@SuppressWarnings("serial") 
public class MainWindow extends JFrame {
    
    /**
     * indicator for property changes on mainwindow that 
     * window is initialised now.
     */
    public static final String INITIALISED = "pseudo.initialised";
    
    public static final String IN_PROOF = "pseudo.ongoing_proof";

    private ProofCenter proofCenter;

    private SequentComponent sequentComponent;

    private GoalList goalList;
    
    private ProgramComponent programComponent;

    private ProofComponent proofComponent;

    private RuleApplicationComponent ruleApplicationComponent;

    private BarManager barManager;
    
    /**
     * Instantiates a new main window.
     * 
     * @param proofCenter the underlying proof center
     * @param resourceName the resource name to be used as title
     * @throws IOException if the barmanager fails to find needed resources
     */
    public MainWindow(ProofCenter proofCenter, String resourceName) throws IOException {
        super("Pseudo - " + resourceName);
        this.proofCenter = proofCenter;
    }

    void makeGUI() throws IOException {
        
        // Create the split dock.
        SplitDock topDock = new SplitDock();
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(topDock, BorderLayout.CENTER);
        
        // Create the dockings
        TabDock leftTabDock = new TabDock();
        TabDock rightTabDock = new TabDock();
        TabDock bottomTabDock = new TabDock();
        {
            // Create the dock model for the docks.
            FloatDockModel dockModel = new FloatDockModel();
            dockModel.addOwner("mainFrame", this);
            
            // Give the dock model to the docking manager.
            DockingManager.setDockModel(dockModel);
            
            // create compound dock for right and bottom
            SplitDock rbSplitDock = new SplitDock();
            
            // Add the child docks to the split dock at the left and right.
            rbSplitDock.addChildDock(rightTabDock, new Position(Position.CENTER));
            rbSplitDock.addChildDock(bottomTabDock, new Position(Position.BOTTOM));
            rbSplitDock.setDividerLocation(300);
            
            topDock.addChildDock(leftTabDock, new Position(Position.LEFT));
            topDock.addChildDock(rbSplitDock, new Position(Position.CENTER));
            topDock.setDividerLocation(200);
            
            // Add the root dock to the dock model.
            dockModel.addRootDock("splitDock", topDock, this);
        }
        {
            sequentComponent = new SequentComponent(proofCenter);
            sequentComponent.setBorder(new EmptyBorder(5,5,5,5));
            Dockable dock = new DefaultDockable("sequentview", sequentComponent, "Sequent");
            rightTabDock.addDockable(dock, new Position(0));
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
            leftTabDock.addDockable(dock, new Position(0));
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
            leftTabDock.addDockable(dock, new Position(1));
        }
        {
            ruleApplicationComponent = new RuleApplicationComponent(proofCenter);
            proofCenter.addProofNodeSelectionListener(ruleApplicationComponent);
            JScrollPane scroll = new JScrollPane(ruleApplicationComponent);
            Dockable dock = new DefaultDockable("ruleApp", scroll, "Rule Application");
            leftTabDock.addDockable(dock, new Position(2));
        }
        {
            ProgramPanel panel = new ProgramPanel(proofCenter);
            proofCenter.addProofNodeSelectionListener(panel);
            JScrollPane scroll = new JScrollPane(panel);
            Dockable dock = new DefaultDockable("program", scroll, "Program");
            bottomTabDock.addDockable(dock, new Position(0));
        }
        {
            SourcePanel panel = new SourcePanel(proofCenter);
            proofCenter.addProofNodeSelectionListener(panel);
            JScrollPane scroll = new JScrollPane(panel);
            Dockable dock = new DefaultDockable("source", scroll, "Sources");
            bottomTabDock.addDockable(dock, new Position(1));
        }
        {
            ParameterPanel settings = new ParameterPanel(proofCenter);
            Dockable dock = new DefaultDockable("settings", settings, "Settings");
            leftTabDock.addDockable(dock, new Position(3));
        }
        {
            URL resource = getClass().getResource("bar/menu.properties");
            if(resource == null)
                throw new IOException("resource bar/menu.properties not found");
            barManager = new BarManager(null, resource);
            barManager.putProperty(BarAction.CENTER, proofCenter);
            barManager.putProperty(BarAction.PARENT_FRAME, this);
            setJMenuBar(barManager.makeMenubar());
            getContentPane().add(barManager.makeToolbar(), BorderLayout.NORTH);
        }
        {
            // ExitAction is actually also a WindowListener. ...
            // we call the bar manager so that no unnecessary copy 
            // is created if it already exists.
            addWindowListener((WindowListener) barManager.makeAction(CloseAction.class.getName()));    
            setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        }
        setSize(1000, 700);

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


    public BarManager getBarManager() {
        return barManager;
    }

    public void firePropertyChange(String property, boolean value) {
        firePropertyChange(property, !value, value);
    }
    
}
