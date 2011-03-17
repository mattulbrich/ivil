/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2010 Universitaet Karlsruhe, Germany
 * 
 * The system is protected by the GNU General Public License. 
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.gui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import javax.swing.SwingUtilities;

import nonnull.Nullable;
import de.uka.iti.pseudo.proof.ProofNode;

/**
 * An asynchronous update process for receiving notifications about proof
 * changes as the proof changes.
 * 
 * The behaviour of this class depends on whether an automatic proof is on its
 * way or not:
 * <ol>
 * <li>If there is no automatic proof running, a corresponding
 * {@link PropertyChangeEvent} on the property
 * {@link ProofCenter#PROOFNODES_HAVE_CHANGED} is created to be treated on the
 * AWT event queue.</li>
 * <li>If there is a proof running, the change is enqueued. If later the
 * automation finishes, an aggregated event is generated.</li>
 * </ol>
 * 
 * The recognition of whether a proof is running or not, is done using the
 * property {@link ProofCenter#ONGOING_PROOF}.
 * 
 * @author Mattias Ulbrich
 */
public class ProofObserver implements Observer, PropertyChangeListener {

    /**
     * The proof center behind the scenes
     */
    private ProofCenter proofCenter;

    /**
     * The aggregated list od changed proof nodes.
     */
    private @Nullable
    List<ProofNode> changedProofNodes;

    /**
     * The flag whether a proof is ongoing. This property is taken from the
     * {@link #proofCenter} and updated on change.
     */
    private boolean proofIsOngoing;

    /**
     * Instantiate a new observer.
     * 
     * It registers as listener with the proof center and the proof.
     * 
     * @param proofCenter
     *            the proof center to work for/with.
     */
    public ProofObserver(ProofCenter proofCenter) {
        super();
        this.proofCenter = proofCenter;

        proofCenter.addPropertyChangeListener(ProofCenter.ONGOING_PROOF, this);
        proofCenter.getProof().addObserver(this);
        proofIsOngoing = Boolean.TRUE.equals(proofCenter
                .getProperty(ProofCenter.ONGOING_PROOF));
    }

    /**
     * React to changes on the proof.
     * 
     * If no proof is running, generate an event on the event queue. Otherwise,
     * enqueue it.
     */
    @Override
    public synchronized void update(Observable o, Object arg) {

        final ProofNode pn = (ProofNode) arg;

        if (proofIsOngoing) {
            if (changedProofNodes == null) {
                changedProofNodes = new LinkedList<ProofNode>();
            }
            changedProofNodes.add(pn);
        } else {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    proofCenter.fireNotification(
                            ProofCenter.PROOFNODES_HAVE_CHANGED,
                            Collections.singletonList(pn));
                }
            });
        }
    }

    /*
     * Change the local flag to match the center's value. If a proof starts, do
     * not do anything. If a proof stops and there are enqueued nodes, fire an
     * aggregated event.
     */
    @Override
    public synchronized void propertyChange(PropertyChangeEvent evt) {
        assert ProofCenter.ONGOING_PROOF.equals(evt.getPropertyName());
        boolean newVal = Boolean.TRUE.equals(evt.getNewValue());

        if (newVal == false && changedProofNodes != null) {
            proofCenter.fireNotification(ProofCenter.PROOFNODES_HAVE_CHANGED,
                    changedProofNodes);
            changedProofNodes = null;
        }

        assert changedProofNodes == null : "either set to null or null already";
        proofIsOngoing = newVal;
    }
}
