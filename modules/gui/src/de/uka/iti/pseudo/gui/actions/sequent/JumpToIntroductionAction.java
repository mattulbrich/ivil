/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2012 Karlsruhe Institute of Technology
 *
 * The system is protected by the GNU General Public License.
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.gui.actions.sequent;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Map;

import de.uka.iti.pseudo.environment.Function;
import de.uka.iti.pseudo.gui.ProofCenter;
import de.uka.iti.pseudo.gui.actions.BarAction;
import de.uka.iti.pseudo.gui.actions.BarManager.InitialisingAction;
import de.uka.iti.pseudo.gui.sequent.TermComponent;
import de.uka.iti.pseudo.parser.ASTLocatedElement;
import de.uka.iti.pseudo.proof.ProofException;
import de.uka.iti.pseudo.proof.ProofNode;
import de.uka.iti.pseudo.proof.RuleApplication;
import de.uka.iti.pseudo.proof.SubtermSelector;
import de.uka.iti.pseudo.rule.meta.SkolemMetaFunction;
import de.uka.iti.pseudo.term.Application;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.util.GUIUtil;
import de.uka.iti.pseudo.util.Log;
import de.uka.iti.pseudo.util.NotificationEvent;
import de.uka.iti.pseudo.util.NotificationListener;

/**
 * GUI Action to jump to the node which introduces a symbol.
 *
 * <p>
 * This action is part of the popup menu in a term component. Before showing the
 * popup the property {@value TermComponent#TERM_COMPONENT_SELECTED_TAG} is set
 * to the currently mouse-selected term tag.
 *
 * This action then decides whether there is a definition for the selected term.
 * It is stored in {@link #targetProofNode}. The action is activated only if a
 * target has been identified.
 */

@SuppressWarnings("serial")
public class JumpToIntroductionAction
    extends BarAction
    implements InitialisingAction, PropertyChangeListener {

    private ProofNode targetProofNode;

    public JumpToIntroductionAction() {
        putValue(NAME, "Jump to introduction");
        putValue(SMALL_ICON, GUIUtil.makeIcon(JumpToIntroductionAction.class.getResource("img/arrow_up.png")));
        putValue(SHORT_DESCRIPTION, "Jump to the introcution of this symbol.");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // select the target proof node if it is non-null
        if(targetProofNode != null) {
            getProofCenter().fireSelectedProofNode(targetProofNode);
        }
    }

    // initialise myself as listener to the proof center
    @Override
    public void initialised() {
        getProofCenter().addPropertyChangeListener(TermComponent.TERM_COMPONENT_SELECTED_TAG, this);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        assert TermComponent.TERM_COMPONENT_SELECTED_TAG.equals(evt.getPropertyName());

        targetProofNode = null;
        setEnabled(false);

        TermComponent termComp = (TermComponent) evt.getNewValue();
        Term term = termComp.getTerm();
        SubtermSelector selector = termComp.getMouseSelection();
        if (selector == null) {
            return;
        }

        boolean inAuto = Boolean.TRUE.equals(
                getProofCenter().getProperty(ProofCenter.ONGOING_PROOF));

        String skolemName;
        try {
            skolemName = extractSkolemName(selector.selectSubterm(term));
        } catch (ProofException e) {
            return;
        }

        if(skolemName == null) {
            Log.log(Log.WARNING, "Not a skolem symbol");
            return;
        }

        if(!inAuto) {
            ProofNode pn = getProofCenter().getCurrentProofNode();
            while(pn != null) {

                RuleApplication ra = pn.getAppliedRuleApp();
                if(ra != null && matches(ra, skolemName)) {
                    targetProofNode = pn;
                    setEnabled(true);
                    return;
                }
                pn = pn.getParent();
            }
        }

        Log.log(Log.WARNING, "The introduction of the skolem constant is not a parent of this node");
    }

    /*
     * check whether a rule application is the introduction of a skolem
     * constant. This can be checked because the skolem meta function stores its
     * introduced name in a property.
     */
    private boolean matches(RuleApplication ra, String skolemName) {
        Map<String, String> p = ra.getProperties();
        for (Map.Entry<String, String> entry : p.entrySet()) {

            if(!entry.getKey().startsWith(SkolemMetaFunction.SKOLEM_NAME_PROPERTY + "(")) {
                continue;
            }

            if(!entry.getValue().equals(skolemName)) {
                continue;
            }

            return true;
        }
        return false;
    }

    /*
     * extract the skolem symbol name from a term tag.
     * Only if it IS a skolem symbol.
     *
     * Skolem symbols have an indicating location tag.
     */
    private String extractSkolemName(Term term) {
        if (term instanceof Application) {
            Application app = (Application) term;
            Function f = app.getFunction();
            ASTLocatedElement declLoc = f.getDeclaration();
            if(declLoc == SkolemMetaFunction.SKOLEM) {
                return f.getName();
            }
        }
        return null;
    }

}