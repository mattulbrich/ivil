/*
 * This file is part of PSEUDO
 * Copyright (C) 2009 Universitaet Karlsruhe, Germany
 *    written by Mattias Ulbrich
 * 
 * The system is protected by the GNU General Public License. 
 * See LICENSE.TXT for details.
 */
package de.uka.iti.pseudo.gui.bar;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Map;

import de.uka.iti.pseudo.environment.Function;
import de.uka.iti.pseudo.gui.ProofCenter;
import de.uka.iti.pseudo.gui.TermComponent;
import de.uka.iti.pseudo.gui.bar.BarManager.InitialisingAction;
import de.uka.iti.pseudo.parser.ASTLocatedElement;
import de.uka.iti.pseudo.prettyprint.TermTag;
import de.uka.iti.pseudo.proof.ProofNode;
import de.uka.iti.pseudo.proof.RuleApplication;
import de.uka.iti.pseudo.rule.meta.SkolemMetaFunction;
import de.uka.iti.pseudo.term.Application;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.util.GUIUtil;

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
        if(targetProofNode != null)
            getProofCenter().fireSelectedProofNode(targetProofNode);
    }

    // initialise myself as listener to the proof center
    @Override
    public void initialised() {
        getProofCenter().addPropertyChangeListener(TermComponent.TERM_COMPONENT_SELECTED_TAG, this);
    }

    @Override public void propertyChange(PropertyChangeEvent evt) {
        assert TermComponent.TERM_COMPONENT_SELECTED_TAG.equals(evt.getPropertyName());

        targetProofNode = null;
        setEnabled(false);
        
        TermTag selectedTermTag = (TermTag) evt.getNewValue();
        boolean inAuto = Boolean.TRUE.equals(
                getProofCenter().getProperty(ProofCenter.ONGOING_PROOF));
        
        String skolemName = extractSkolemName(selectedTermTag);
        if(skolemName == null) {
            System.err.println("Not a skolem symbol");
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
        
        System.err.println("The introduction of the skolem constant is not a parent of this node");
    }

    /*
     * check whether a rule application is the introduction of a skolem
     * constant. This can be checked because the skolem meta function stores its
     * introduced name in a property.
     */
    private boolean matches(RuleApplication ra, String skolemName) {
        Map<String, String> p = ra.getProperties();
        for (Map.Entry<String, String> entry : p.entrySet()) {
            
            if(!entry.getKey().startsWith(SkolemMetaFunction.SKOLEM_NAME_PROPERTY + "("))
                continue;
            
            if(!entry.getValue().equals(skolemName))
                continue;
            
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
    private String extractSkolemName(TermTag termTag) {
        Term t = termTag.getTerm();
        if (t instanceof Application) {
            Application app = (Application) t;
            Function f = app.getFunction();
            ASTLocatedElement declLoc = f.getDeclaration();
            if(declLoc == SkolemMetaFunction.SKOLEM)
                return f.getName();
        }
        return null;
    }
}