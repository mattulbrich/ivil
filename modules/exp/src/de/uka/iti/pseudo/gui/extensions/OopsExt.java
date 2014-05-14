/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2012 Karlsruhe Institute of Technology
 *
 * The system is protected by the GNU General Public License.
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.gui.extensions;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.gui.ProofCenter;
import de.uka.iti.pseudo.proof.ProofException;
import de.uka.iti.pseudo.proof.ProofNode;
import de.uka.iti.pseudo.proof.RuleApplicationMaker;
import de.uka.iti.pseudo.proof.TermSelector;
import de.uka.iti.pseudo.rule.Rule;
import de.uka.iti.pseudo.util.ExceptionDialog;
import de.uka.iti.pseudo.util.Log;

public class OopsExt implements ContextExtension {

    @Override public String getName() {
        return "OOOPS";
    }

    @Override public String getDescription() {
        return "tbd";
    }

    @Override public boolean shouldOffer(ProofCenter proofCenter) {
        return true;
    }

    @Override public void run(ProofCenter proofCenter) {

        Environment env = proofCenter.getEnvironment();
        Rule oops = env.getRule("oops");
        LinkedList<ProofNode> openGoals = new LinkedList<ProofNode>();
        collectOpenGoals(proofCenter.getCurrentProofNode(), openGoals);

        try {
            for (ProofNode goal : openGoals) {
                RuleApplicationMaker ram = new RuleApplicationMaker(env);
                ram.setRule(oops);
                ram.setProofNode(goal);
                ram.matchInstantiations();
                proofCenter.getProof().apply(ram, env);
            }
        } catch (ProofException e) {
            ExceptionDialog.showExceptionDialog(proofCenter.getMainWindow(), e);
        }
    }

    private void collectOpenGoals(ProofNode pn, Collection<ProofNode> list) {
        List<ProofNode> children = pn.getChildren();
        if(children == null) {
            list.add(pn);
        } else {
            for (ProofNode child : children) {
                collectOpenGoals(child, list);
            }
        }
    }

}
