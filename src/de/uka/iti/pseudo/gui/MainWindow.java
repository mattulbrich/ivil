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
import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
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

import de.uka.iti.pseudo.gui.bar.BarManager;
import de.uka.iti.pseudo.gui.bar.CloseAction;
import de.uka.iti.pseudo.gui.editor.PFileEditor;
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
//        {
//            JPanel settings = new JPanel();
//            settings.add(new JLabel("yet to come ..."));
//            Dockable dock = new DefaultDockable("settings", settings, "Settings");
//            tabDock.addDockable(dock, new Position(3));
//        }
        {
            File file = new File(proofCenter.getEnvironment().getResourceName());
            PFileEditor pfedit = new PFileEditor(file);
            Dockable dock = new DefaultDockable("pfedit", pfedit, "Environment");
            tabDock.addDockable(dock, new Position(3));
        }
        {
            barManager = new BarManager(null);
            barManager.putProperty(BarManager.CENTER, proofCenter);
            barManager.putProperty(BarManager.PARENT_FRAME, this);
            URL resource = getClass().getResource("bar/menu.properties");
            if(resource == null)
                throw new IOException("resource bar/menu.properties not found");
            setJMenuBar(barManager.makeMenubar(resource));
            getContentPane().add(barManager.makeToolbar(resource), BorderLayout.NORTH);
        }
        {
            // ExitAction is actually also a WindowListener. ...
            // we call the bar manager so that no unnecessary copy 
            // is created if it already exists.
            addWindowListener((WindowListener) barManager.makeAction(CloseAction.class.getName()));    
            setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        }
        setSize(1000, 600);

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
