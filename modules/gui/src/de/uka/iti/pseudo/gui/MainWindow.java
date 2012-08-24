/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2012 Karlsruhe Institute of Technology
 *
 * The system is protected by the GNU General Public License.
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.gui;

import java.awt.BorderLayout;
import java.awt.event.WindowListener;
import java.io.IOException;
import java.net.URL;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.UIManager;
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
import com.javadocking.model.DockModel;
import com.javadocking.model.FloatDockModel;

import de.uka.iti.pseudo.auto.strategy.StrategyException;
import de.uka.iti.pseudo.gui.actions.BarAction;
import de.uka.iti.pseudo.gui.actions.BarManager;
import de.uka.iti.pseudo.gui.parameters.ParameterPanel;
import de.uka.iti.pseudo.gui.sequent.SequentComponent;
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

    /*
     * The split pane registers some key strokes which are to used elsewhere.
     * We remove the according infos from the look and feel. The keys are then
     * available again.
     */
    static {
        UIManager.getDefaults().remove("SplitPane.ancestorInputMap");
    }

    /**
     * indicator for property changes on mainwindow that
     * window is initialised now.
     */
    // public static final String INITIALISED = "pseudo.initialised";

    private final ProofCenter proofCenter;

    private SequentComponent sequentComponent;

    private GoalList goalList;

    private ProofComponent proofComponent;

    private RuleApplicationComponent ruleApplicationComponent;

    private BarManager barManager;

    private final int number;
    private static int counter = 0;

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
        this.number = ++counter;
    }

    void makeGUI() throws IOException, StrategyException {

        // setup the bar manager
        URL resource = getClass().getResource("actions/menu.xml");
        if(resource == null) {
            throw new IOException("resource actions/menu.properties not found");
        }
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
            // Give the dock model to the docking manager if not yet set.
            if(DockingManager.getDockModel() == null) {
                FloatDockModel dockModel = new FloatDockModel();
                DockingManager.setDockModel(dockModel);
            }

            // register this frame with the dock model
            DockModel dockModel = DockingManager.getDockModel();
            dockModel.addOwner("mainFrame" + number, this);

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
            psSplitDock.setDividerLocation(350);

            topDock.addDockingListener(new ResizeWeightAdaptation(0));
            topDock.addChildDock(leftTabDock, new Position(Position.LEFT));
            topDock.addChildDock(rbSplitDock, new Position(Position.CENTER));
            topDock.setDividerLocation(200);
            // topDock.addComponentListener(new TopDockResizeListener());

            // Add the root dock to the dock model.
            dockModel.addRootDock("splitDock" + number, topDock, this);
        }
        {
            sequentComponent = new SequentComponent(proofCenter);
            sequentComponent.setBorder(new EmptyBorder(5,5,5,5));
            JScrollPane scroll = new JScrollPane(sequentComponent);
            // make the background seamless
            scroll.getViewport().setBackground(sequentComponent.getBackground());
            Dockable dock = new DefaultDockable("sequentview" + number, scroll, "Sequent");
            rightTabDock.addDockable(dock, new Position(0));
            proofCenter.addPropertyChangeListener(ProofCenter.SELECTED_PROOFNODE, sequentComponent);
            proofCenter.addPropertyChangeListener(ProofCenter.SELECTED_RULEAPPLICATION, sequentComponent);
            proofCenter.addNotificationListener(ProofCenter.PROOFTREE_HAS_CHANGED, sequentComponent);
        }
        {
            goalList = new GoalList(proofCenter);
            goalList.addListSelectionListener(new ListSelectionListener() {
                @Override
                public void valueChanged(ListSelectionEvent e) {
                    if(!e.getValueIsAdjusting()) {
                        ProofNode proofNode = goalList.getSelectedProofNode();
                        if (proofNode != null && proofNode != proofCenter.getCurrentProofNode()) {
                            proofCenter.fireSelectedProofNode(proofNode);
                        }
                    }
                }});
            JScrollPane scroll = new JScrollPane(goalList);
            Dockable dock = new DefaultDockable("goallist" + number, scroll, "Goal list");
            leftTabDock.addDockable(dock, new Position(0));
        }
        {
            proofComponent = new ProofComponent(proofCenter);
            proofComponent.addTreeSelectionListener(new TreeSelectionListener() {
                @Override
                public void valueChanged(TreeSelectionEvent e) {
                    ProofNode selectedProofNode = proofComponent.getSelectedProofNode();
                    if (selectedProofNode != null && selectedProofNode != proofCenter.getCurrentProofNode()) {
                        proofCenter.fireSelectedProofNode(selectedProofNode);
                    }
                }
            });
            JScrollPane scroll = new JScrollPane(proofComponent);
            Dockable dock = new DefaultDockable("proof" + number, scroll, "Proof tree");
            leftTabDock.addDockable(dock, new Position(1));
        }
        {
            ruleApplicationComponent = new RuleApplicationComponent(proofCenter);
            proofCenter.addPropertyChangeListener(ProofCenter.SELECTED_PROOFNODE, ruleApplicationComponent);
            JScrollPane scroll = new JScrollPane(ruleApplicationComponent);
            Dockable dock = new DefaultDockable("ruleApp" + number, scroll, "Rule Application");
            leftTabDock.addDockable(dock, new Position(2));
        }
        {
            RuleBrowserComponent ruleBrowserComponent = new RuleBrowserComponent(proofCenter);
            Dockable dock = new DefaultDockable("ruleBrowser" + number, ruleBrowserComponent, "Rule Browser");
            leftTabDock.addDockable(dock, new Position(3));
        }
        {
            ParameterPanel settings = new ParameterPanel(proofCenter);
            Dockable dock = new DefaultDockable("settings" + number, settings, "Settings");
            leftTabDock.addDockable(dock, new Position(4));

            //show the proof tree
            leftTabDock.setSelectedDockable(leftTabDock.getDockable(1));
        }
        {
            ProgramPanel panel = new ProgramPanel(proofCenter);
            proofCenter.addPropertyChangeListener(ProofCenter.SELECTED_PROOFNODE, panel);
            Dockable dock = new DefaultDockable("program" + number, panel, "Program");
            programTabDock.addDockable(dock, new Position(0));
        }
        {
            SourcePanel panel = new SourcePanel(proofCenter);
            proofCenter.addPropertyChangeListener(ProofCenter.SELECTED_PROOFNODE, panel);
            Dockable dock = new DefaultDockable("source" + number, panel, "Sources");
            sourceTabDock.addDockable(dock, new Position(0));
        }
        {
            setJMenuBar(barManager.makeMenubar("prover.menubar"));
            getContentPane().add(barManager.makeToolbar("prover.toolbar"), BorderLayout.NORTH);
        }
        {
            // ExitAction is actually also a WindowListener. ...
            // we call the bar manager so that no unnecessary copy
            // is created if it already exists.
            addWindowListener((WindowListener) barManager.makeAction("general.close"));
            setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        }
        setSize(1000, 700);
    }

    @Override
    public void dispose() {
        DockingManager.getDockModel().removeOwner(this);
        super.dispose();
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
