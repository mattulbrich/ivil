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
package de.uka.iti.pseudo.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
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

import de.uka.iti.pseudo.gui.actions.BarAction;
import de.uka.iti.pseudo.gui.actions.CloseAction;
import de.uka.iti.pseudo.gui.actions.BarManager;
import de.uka.iti.pseudo.gui.parameters.ParameterPanel;
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
    
    static private class TopDockResizeListener implements ComponentListener{

        @Override
        public void componentHidden(ComponentEvent e) {
        }

        @Override
        public void componentMoved(ComponentEvent e) {
        }

        @Override
        public void componentResized(ComponentEvent e) {
            SplitDock topDock = (SplitDock) e.getComponent();
            if(200!=topDock.getDividerLocation())
                topDock.setDividerLocation(200);
        }

        @Override
        public void componentShown(ComponentEvent e) {
        }

    }
	
    /**
     * indicator for property changes on mainwindow that 
     * window is initialised now.
     */
    // public static final String INITIALISED = "pseudo.initialised";
    
    private ProofCenter proofCenter;

    private SequentComponent sequentComponent;

    private GoalList goalList;
    
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
        super("ivil - " + resourceName);
        this.proofCenter = proofCenter;
    }

    void makeGUI() throws IOException {

        // setup the bar manager
        URL resource = getClass().getResource("actions/menu.properties");
        if(resource == null)
            throw new IOException("resource actions/menu.properties not found");
        barManager = new BarManager(null, resource);
        barManager.putProperty(BarAction.CENTER, proofCenter);
        barManager.putProperty(BarAction.PARENT_FRAME, this);
        
        // Create the split dock.
        SplitDock topDock = new SplitDock();
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(topDock, BorderLayout.CENTER);
        
        
        // Create the dockings
        TabDock leftTabDock = new TabDock();
        TabDock rightTabDock = new TabDock();
        TabDock sourceTabDock = new TabDock();
        TabDock programTabDock = new TabDock();
        {
            // Create the dock model for the docks.
            FloatDockModel dockModel = new FloatDockModel();
            dockModel.addOwner("mainFrame", this);
            
            // Give the dock model to the docking manager.
            DockingManager.setDockModel(dockModel);
            
            // create compound dock for right and bottom
            SplitDock rbSplitDock = new SplitDock();

            // create compound dock for source and program
            SplitDock psSplitDock = new SplitDock();
            
            // Add the child docks to the split dock at the left and right.
            rbSplitDock.addChildDock(rightTabDock, new Position(Position.CENTER));
            rbSplitDock.addChildDock(psSplitDock, new Position(Position.BOTTOM));
            rbSplitDock.setDividerLocation(300);
            
            
            psSplitDock.addChildDock(programTabDock, new Position(Position.LEFT));
            psSplitDock.addChildDock(sourceTabDock, new Position(Position.RIGHT));
            psSplitDock.setDividerLocation(300);

            topDock.addChildDock(leftTabDock, new Position(Position.LEFT));
            topDock.addChildDock(rbSplitDock, new Position(Position.CENTER));
            topDock.setDividerLocation(200);
            topDock.addDockingListener(new ResizeWeightAdaptation(0));
            // topDock.addComponentListener(new TopDockResizeListener());
            
            // Add the root dock to the dock model.
            dockModel.addRootDock("splitDock", topDock, this);
        }
        {
            sequentComponent = new SequentComponent(proofCenter);
            sequentComponent.setBorder(new EmptyBorder(5,5,5,5));
            JScrollPane scroll = new JScrollPane(sequentComponent);
            // make the background seamless
            scroll.getViewport().setBackground(sequentComponent.getBackground());
            Dockable dock = new DefaultDockable("sequentview", scroll, "Sequent");
            rightTabDock.addDockable(dock, new Position(0));
            proofCenter.addPropertyChangeListener(ProofCenter.SELECTED_PROOFNODE, sequentComponent);
            proofCenter.addPropertyChangeListener(ProofCenter.SELECTED_RULEAPPLICATION, sequentComponent);
        }
        {
            goalList = new GoalList(proofCenter.getProof(), proofCenter.getEnvironment());
            proofCenter.addPropertyChangeListener(ProofCenter.SELECTED_PROOFNODE, goalList);
            goalList.addListSelectionListener(new ListSelectionListener() {
                public void valueChanged(ListSelectionEvent e) {
                    if(!e.getValueIsAdjusting()) {
                        ProofNode proofNode = goalList.getSelectedProofNode();
                        if(proofNode != null) {
                            proofCenter.fireSelectedProofNode(proofNode);
                        }
                    }
                }});
            JScrollPane scroll = new JScrollPane(goalList);
            Dockable dock = new DefaultDockable("goallist", scroll, "Goal list");
            leftTabDock.addDockable(dock, new Position(0));
        }
        {
            proofComponent = new ProofComponent(proofCenter);
            proofCenter.addPropertyChangeListener(ProofCenter.SELECTED_PROOFNODE, proofComponent);
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
            proofCenter.addPropertyChangeListener(ProofCenter.SELECTED_PROOFNODE, ruleApplicationComponent);
            JScrollPane scroll = new JScrollPane(ruleApplicationComponent);
            Dockable dock = new DefaultDockable("ruleApp", scroll, "Rule Application");
            leftTabDock.addDockable(dock, new Position(2));
        }
        {
            ProgramPanel panel = new ProgramPanel(proofCenter);
            proofCenter.addPropertyChangeListener(ProofCenter.SELECTED_PROOFNODE, panel);
            Dockable dock = new DefaultDockable("program", panel, "Program");
            programTabDock.addDockable(dock, new Position(0));
        }
        {
            SourcePanel panel = new SourcePanel(proofCenter);
            proofCenter.addPropertyChangeListener(ProofCenter.SELECTED_PROOFNODE, panel);
            Dockable dock = new DefaultDockable("source", panel, "Sources");
            sourceTabDock.addDockable(dock, new Position(0));
        }
        {
            ParameterPanel settings = new ParameterPanel(proofCenter);
            Dockable dock = new DefaultDockable("settings", settings, "Settings");
            leftTabDock.addDockable(dock, new Position(3));
        }
        {
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

}
