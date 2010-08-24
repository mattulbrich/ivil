package de.uka.iti.pseudo.gui.actions;

import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.locks.Lock;

import javax.swing.AbstractListModel;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;

import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.gui.ProofCenter;
import de.uka.iti.pseudo.gui.actions.BarManager.InitialisingAction;
import de.uka.iti.pseudo.proof.Proof;
import de.uka.iti.pseudo.proof.ProofException;
import de.uka.iti.pseudo.proof.ProofNode;
import de.uka.iti.pseudo.util.ExceptionDialog;
import de.uka.iti.pseudo.util.GUIUtil;

// TODO DOC!

public class SnapshotManagerAction extends BarAction implements
        InitialisingAction, PropertyChangeListener {

    private SnapshotManager manager;

    public SnapshotManagerAction() {
        super("Snapshots", GUIUtil.makeIcon(SnapshotManager.class
                .getResource("img/camera.png")));
        putValue(SHORT_DESCRIPTION, "opens a dialog to save/restore intermediate proof states.");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (manager == null)
            manager = new SnapshotManager(getProofCenter());
        manager.display();
    }

    public void initialised() {
        ProofCenter proofCenter = getProofCenter();
        if (proofCenter != null)
            proofCenter.addPropertyChangeListener(ProofCenter.ONGOING_PROOF,
                    this);
    }

    public void propertyChange(PropertyChangeEvent evt) {
        setEnabled(!(Boolean) evt.getNewValue());
    }

    public static void main(String[] args) throws Exception {
        Proof p = new Proof(Environment.getTrue());
        ProofCenter pc = new ProofCenter(p, Environment.BUILT_IN_ENV);
        SnapshotManager m = new SnapshotManager(pc);
        m.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        m.display();
    }

}

class Snapshot {
    ProofNode[] openGoals;
    String description;
    String timestamp;

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(timestamp);
        if (description != null && description.length() > 0) {
            sb.append(": ").append(description);
        }
        return sb.toString();
    }
}

class SnapshotManager extends JDialog {

    private ProofCenter proofCenter;
    private ProofNode root;

    private List<Snapshot> snapshots = new ArrayList<Snapshot>();
    private JList choiceList;
    private JTextField descriptionField;
    private JButton restoreButton;

    public SnapshotManager(ProofCenter proofCenter) {
        super(proofCenter.getMainWindow(), "Proof Snapshot Manager");
        setModal(true);
        this.proofCenter = proofCenter;
        this.root = proofCenter.getProof().getRoot();
        init();
    }

    private void init() {
        Container cp = getContentPane();
        cp.setLayout(new GridBagLayout());
        {
            choiceList = new JList();
            choiceList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            choiceList.setBorder(BorderFactory
                    .createTitledBorder("Saved snapshots:"));
            cp.add(choiceList, new GridBagConstraints(0, 0, 2, 1, 1, 1,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(5, 5, 5, 5), 0, 0));
        }
        {
            JPanel butPanel = new JPanel(new FlowLayout());
            {
                restoreButton = new JButton("Rewind");
                restoreButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        restoreSelectedSnapshot();
                    }
                });
                butPanel.add(restoreButton);
            }
            {
                JButton closeButton = new JButton("Close");
                closeButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        setVisible(false);
                    }
                });
                butPanel.add(closeButton);
            }

            cp.add(butPanel, new GridBagConstraints(0, 1, 2, 1, 0, 0,
                    GridBagConstraints.EAST, GridBagConstraints.NONE,
                    new Insets(0, 0, 0, 0), 0, 0));
        }
        {
            JButton addButton = new JButton("Shoot: ");
            addButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    shootSnapshot();
                }
            });
            cp.add(addButton, new GridBagConstraints(0, 2, 1, 1, 0, 0,
                    GridBagConstraints.CENTER, GridBagConstraints.NONE,
                    new Insets(5, 5, 5, 5), 0, 0));
        }
        {
            descriptionField = new JTextField();
            descriptionField.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    shootSnapshot();
                }
            });
            cp.add(descriptionField, new GridBagConstraints(1, 2, 1, 1, 1, 0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(5, 0, 5, 5), 0, 0));
        }

        setSize(400, 400);
        setLocationRelativeTo(proofCenter.getMainWindow());
    }

    public void display() {
        checkSnapshots();
        final Object[] objects = snapshots.toArray();
        choiceList.setModel(new AbstractListModel() {
            public int getSize() {
                return objects.length;
            }

            public Object getElementAt(int i) {
                return objects[i];
            }
        });

        if (objects.length == 0) {
            restoreButton.setEnabled(false);
        } else {
            restoreButton.setEnabled(true);
            choiceList.setSelectedIndex(objects.length - 1);
        }
        
        setVisible(true);
    }

    private void checkSnapshots() {

        ListIterator<Snapshot> it = snapshots.listIterator();
        while (it.hasNext()) {
            Snapshot sn = it.next();
            if (!checkSnapshot(sn))
                it.remove();
        }

    }

    private boolean checkSnapshot(Snapshot sn) {
        for (ProofNode goal : sn.openGoals) {
            // find its root (the one w/o parent)
            while (goal.getParent() != null) {
                goal = goal.getParent();
            }

            if (goal != root)
                return false;
        }
        return true;

    }

    protected void shootSnapshot() {

        Snapshot sn = new Snapshot();

        sn.timestamp = SimpleDateFormat.getTimeInstance().format(new Date());
        sn.description = descriptionField.getText();
        List<ProofNode> opengoals = proofCenter.getProof().getOpenGoals();
        ProofNode[] array = new ProofNode[opengoals.size()];
        sn.openGoals = opengoals.toArray(array);

        snapshots.add(sn);
        
        setVisible(false);

    }

    private void restoreSelectedSnapshot() {
        Proof proof = proofCenter.getProof();
        Snapshot selected = (Snapshot) choiceList.getSelectedValue();
        if (selected == null)
            return;

        Lock lock = proofCenter.getProof().getLock();
        lock.lock();
        try {
            // if(checkSnapshot(selected))
            // return;

            for (ProofNode goal : selected.openGoals) {
                proof.prune(goal);
            }

        } catch (ProofException e) {
            ExceptionDialog.showExceptionDialog(getOwner(), e);
        } finally {
            lock.unlock();
        }

        setVisible(false);
    }

}