package de.uka.iti.pseudo.gui.bar;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import de.uka.iti.pseudo.auto.DecisionProcedure;
import de.uka.iti.pseudo.auto.Z3SMT;
import de.uka.iti.pseudo.auto.DecisionProcedure.Result;
import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.gui.MainWindow;
import de.uka.iti.pseudo.gui.bar.BarManager.InitialisingAction;
import de.uka.iti.pseudo.proof.Proof;
import de.uka.iti.pseudo.proof.ProofNode;
import de.uka.iti.pseudo.term.Sequent;
import de.uka.iti.pseudo.util.Pair;

public class SMTBackgroundAction extends BarAction implements
        InitialisingAction, PropertyChangeListener, Observer, Runnable {
    
    private DecisionProcedure solver = new Z3SMT();
    
    private Map<Sequent, Boolean> sequentStatus =
        Collections.synchronizedMap(new HashMap<Sequent, Boolean>());
    
    private BlockingQueue<ProofNode> jobs = new LinkedBlockingQueue<ProofNode>();
    
    private Object lock = new Object();
    private Proof proof;

    private Environment env;

    private SMTAnnunciatorAction peer;
    
    public SMTBackgroundAction() {
        Thread thread = new Thread(this, "SMT Background");
        thread.setPriority(Thread.MIN_PRIORITY);
        thread.start();
        setIcon(BarManager.makeIcon(getClass().getResource("img/smt.png")));
    }

    public void initialised() {
        getProofCenter().getMainWindow().addPropertyChangeListener(MainWindow.IN_PROOF, this);
        proof = getProofCenter().getProof();
        proof.addObserver(this);
        env = getProofCenter().getEnvironment();
        try {
            peer = (SMTAnnunciatorAction) getProofCenter().getMainWindow().
                    getBarManager().makeAction(SMTAnnunciatorAction.class.getName());
        } catch (IOException e) {
            throw new Error(e);
        }
    }

    public void actionPerformed(ActionEvent e) {
        if(isSelected()) {
            // tell the thread that we are in again
            jobs.clear();
            for(ProofNode pn : proof.getOpenGoals()) {
                jobs.add(pn);
            }
            synchronized (lock) {
                lock.notify();
            }
        }
    }
    
    public void propertyChange(PropertyChangeEvent evt) {
        setEnabled((Boolean) evt.getOldValue());
    }

    public void update(Observable o, Object arg) {
        jobs.clear();
        for(ProofNode pn : proof.getOpenGoals()) {
            jobs.add(pn);
        }
    }

    public void run() {
        try {
            while(!Thread.interrupted()) {
                synchronized (lock) {
                    while(!isSelected()) {
                        lock.wait();
                    }
                }
                
                ProofNode pn = jobs.take();
                Sequent sequent = pn.getSequent();
                
                Boolean cached = sequentStatus.get(sequent);
                if(cached != null) {
                    if(cached) {
                        peer.addProvable(pn);
                    }
                }
                
                try {
                    Pair<Result, String> result = solver.solve(sequent, env, 3000);
                    boolean proveable = result.fst() == Result.VALID;
                    sequentStatus.put(sequent, proveable);
                    if(proveable)
                        peer.addProvable(pn);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    
}
