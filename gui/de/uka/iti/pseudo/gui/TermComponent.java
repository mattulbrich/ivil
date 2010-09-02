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

import java.awt.Color;
import java.awt.Font;
import java.awt.Point;
import java.awt.datatransfer.Transferable;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JPopupMenu;
import javax.swing.JTextPane;
import javax.swing.border.Border;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.Highlighter.HighlightPainter;

import nonnull.NonNull;
import de.uka.iti.pseudo.prettyprint.PrettyPrint;
import de.uka.iti.pseudo.prettyprint.TermTag;
import de.uka.iti.pseudo.proof.ProofNode;
import de.uka.iti.pseudo.proof.RuleApplication;
import de.uka.iti.pseudo.proof.SubtermSelector;
import de.uka.iti.pseudo.proof.TermSelector;
import de.uka.iti.pseudo.proof.SequentHistory.Annotation;
import de.uka.iti.pseudo.rule.Rule;
import de.uka.iti.pseudo.rule.RuleTagConstants;
import de.uka.iti.pseudo.term.LiteralProgramTerm;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.Type;
import de.uka.iti.pseudo.term.Update;
import de.uka.iti.pseudo.term.creation.TermInstantiator;
import de.uka.iti.pseudo.util.AnnotatedStringWithStyles;
import de.uka.iti.pseudo.util.Log;
import de.uka.iti.pseudo.util.NotScrollingCaret;
import de.uka.iti.pseudo.util.TermSelectionTransfer;
import de.uka.iti.pseudo.util.TermSelectionTransferable;
import de.uka.iti.pseudo.util.TextInstantiator;
import de.uka.iti.pseudo.util.AnnotatedString.Element;
import de.uka.iti.pseudo.util.settings.Settings;

/**
 * The Class TermComponent is used to show terms, it allows highlighting.
 */
public class TermComponent extends JTextPane {

    public static final String TERM_COMPONENT_SELECTED_TAG =
        "termComponent.popup.selectedTermTag";

    private static Settings S = Settings.getInstance();
    
    private static final long serialVersionUID = -4415736579829917335L;

    /**
     * some UI constants
     */
    private static final Font FONT = S.getFont("pseudo.termcomponent.font", null);
    
    // the highlight color should be bright
    private static final Color HIGHLIGHT_COLOR = 
        S.getColor("pseudo.termcomponent.highlightcolor", Color.ORANGE);

    // the modality background should be rather unnoticed
    private static final Color MODALITY_BACKGROUND = 
        S.getColor("pseudo.termcomponent.modalitybackground", Color.CYAN.brighter());
    
    // border color needs to match background of sequent view
    private static final Color BORDER_COLOR =
        S.getColor("pseudo.termcomponent.bordercolor", Color.DARK_GRAY);
    
    // variables should be noticed
    private static final Color VARIABLE_FOREGROUND =
        S.getColor("pseudo.termcomponent.variableforeground", Color.MAGENTA);
    
    // marking for an assumption
    private static final Color LIGHT_MARKING_COLOR = 
        S.getColor("pseudo.termcomponent.assumptionforeground", Color.LIGHT_GRAY);

    // marking for a find clause
    private static final Color DARK_MARKING_COLOR = 
        S.getColor("pseudo.termcomponent.findforeground", Color.LIGHT_GRAY);

    // empty border
    private static final Border BORDER = BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR), BorderFactory
                    .createEmptyBorder(5, 5, 5, 5));

    // the property for the bar manager to describe my popup menu
    private static final String POPUP_PROPERTY = "termComponent.popup";


    // darker and a lighter color for marking
    private HighlightPainter[] MARKINGS = {
            new DefaultHighlighter.DefaultHighlightPainter(DARK_MARKING_COLOR),
            new DefaultHighlighter.DefaultHighlightPainter(LIGHT_MARKING_COLOR) };

    /**
     * The term displayed
     */
    private Term term;
    
    /**
     * the tag of the term which is currently highlighted by the mouse
     * can be null
     */
    private TermTag mouseSelection;
    
    /**
     * the position of term within its sequent
     */
    private TermSelector termSelector;

    /**
     * The proofCenter to which this term belongs
     */
    private ProofCenter proofCenter;

    /**
     * The annotated string containing the pretty printed term.
     */
    private AnnotatedStringWithStyles<TermTag> annotatedString;

    /**
     * The highlight object as returned by the highlighter.
     * Used to highlight the mouse-selected subterm.
     */
    private Object theHighlight;

    /**
     * the collection of highlighting objects. Used to mark find and assume
     * instances.
     */
    private List<Object> marks = new ArrayList<Object>();

    /**
     * true if the currently presented proof node to which the component belongs
     * is open. It is used for creating the styles. Closed nodes are italic.
     */
    private boolean open;

    /**
     * The history for the presented term
     */
    private @NonNull Annotation history;

    /**
     * pretty printing instance needed for tooltips
     */
    private PrettyPrint prettyPrinter;

    // not needed in the future
    private int verbosityLevel;
    
    /**
     * the popup menu to do things locally
     */
    private PopupMenuListener popupMenuListener = new PopupMenuListener() {

        @Override public void popupMenuCanceled(PopupMenuEvent e) {
        }

        @Override public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
        }

        @Override public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
            Log.enter(e);
            proofCenter.firePropertyChange(TERM_COMPONENT_SELECTED_TAG, 
                    mouseSelection);
            Log.log(Log.VERBOSE, mouseSelection);
        }
    };

    // TODO DOC
    /**
     * Instantiates a new term component.
     * 
     * @param t
     *                the term to display
     * @param history
     * @param open
     * @param env
     *                the environment to use for pretty printing
     * @param termSelector
     *                selector object describing the position of the displayed
     *                term in its sequent
     * @param prettyPrinter
     *                the pretty printer to print the term in this component
     */
    public TermComponent(@NonNull Term t, Annotation history, boolean open,
            @NonNull ProofCenter proofCenter, @NonNull TermSelector termSelector)  {
        this.term = t;
        this.history = history;
        this.termSelector = termSelector;
        this.proofCenter = proofCenter;
        this.prettyPrinter = proofCenter.getPrettyPrinter();
        this.verbosityLevel = (Integer)proofCenter.getProperty(ProofComponent.VERBOSITY_PROPERTY);
        this.annotatedString = prettyPrinter.print(t);
        this.open = open;

        assert history != null;
        // must be toplevel
        assert termSelector.getSubtermSelector().getDepth() == 0;
        
        //
        // Set display properties
        setEditable(false);
        setBorder(BORDER);
        setCaret(new NotScrollingCaret());
        annotatedString.appendToDocument(getDocument(), attributeFactory);
        setDragEnabled(true);
        setTransferHandler(new TermSelectionTransfer());
        DefaultHighlighter highlight = new DefaultHighlighter();
        setHighlighter(highlight);
        addMouseListener(new MouseAdapter() {
            public void mouseExited(MouseEvent e) {
                TermComponent.this.mouseExited(e);
            }
        });
        addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseMoved(MouseEvent e) {
                TermComponent.this.mouseMoved(e.getPoint());
            }
        });
        addPropertyChangeListener("dropLocation", new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                DropLocation loc = (DropLocation) evt.getNewValue();
                if(loc != null)
                    TermComponent.this.mouseMoved(loc.getDropPoint());
            }
        });

        try {
            theHighlight = highlight.addHighlight(0, 0,
                    new DefaultHighlighter.DefaultHighlightPainter(
                            HIGHLIGHT_COLOR));
        } catch (BadLocationException e) {
            // may not happen even if document is empty
            throw new Error(e);
        }
        
        //
        // add popup
        try {
            JPopupMenu popupMenu = proofCenter.getBarManager().makePopup(POPUP_PROPERTY);
            setComponentPopupMenu(popupMenu);
            popupMenu.addPopupMenuListener(popupMenuListener);
        } catch (IOException ex) {
            Log.println("Disabling popup menu in term component");
            Log.stacktrace(ex);
        }
    }

    /*
     * store created attribute sets in a cache.
     */
    private static Map<String, AttributeSet> attributeCache = 
        new HashMap<String, AttributeSet>();
    
    /*
     * This is used by AnnotatedStringWithStyles to construct styles. 
     */
    private AnnotatedStringWithStyles.AttributeSetFactory attributeFactory = 
        new AnnotatedStringWithStyles.AttributeSetFactory() {
        public AttributeSet makeStyle(String descr) {

            if (!open)
                descr += " closed";

            AttributeSet cached = attributeCache.get(descr);

            if (cached != null)
                return cached;

            MutableAttributeSet retval = new SimpleAttributeSet();
            if(FONT != null) {
                StyleConstants.setFontFamily(retval, FONT.getFamily());
                StyleConstants.setFontSize(retval, FONT.getSize());
            }
                

            if (descr.contains("closed"))
//                StyleConstants.setForeground(retval, BORDER_COLOR);
                StyleConstants.setItalic(retval, true);

            if (descr.contains("program"))
                StyleConstants.setBackground(retval, MODALITY_BACKGROUND);

            if (descr.contains("update"))
                StyleConstants.setBackground(retval, MODALITY_BACKGROUND);

            if (descr.contains("keyword"))
                StyleConstants.setBold(retval, true);

            if (descr.contains("variable"))
                StyleConstants.setForeground(retval, VARIABLE_FOREGROUND);
            // StyleConstants.setItalic(retval, true);

            attributeCache.put(descr, retval);

            return retval;
        }

    };

    /*
     * Mouse exited the component: remove highlighting
     */
    private void mouseExited(MouseEvent e) {
        try {
            getHighlighter().changeHighlight(theHighlight, 0, 0);
            mouseSelection = null;
        } catch (BadLocationException ex) {
            // this shant happen
            throw new Error(ex);
        }
    }

    /*
     * Mouse moved: move the highlighting
     */
    protected void mouseMoved(Point p) {
        int index = viewToModel(p);
        try {
            if (index >= 0 && index < annotatedString.length()) {
                int begin = annotatedString.getBeginAt(index);
                int end = annotatedString.getEndAt(index);
                getHighlighter().changeHighlight(theHighlight, begin, end);
                
                mouseSelection = annotatedString.getAttributeAt(index);
                setToolTipText(makeTermToolTip(mouseSelection));
            } else {
                getHighlighter().changeHighlight(theHighlight, 0, 0);
                mouseSelection = null;
            }
        } catch (BadLocationException ex) {
            // TODO just ignore this for now
            ex.printStackTrace();
        }
    }

    /**
     * calculate the text to display in tooltip for a term. This is a rather
     * complex document: 
     * <ol>
     * <li>the type
     * <li>For programs, print the current statement
     * <li>The history, at least the first 60 elements
     * </li>
     * 
     * TODO Have something like "F2" to focus on the content and provide links ;)
     * 
     * @param termTag
     *            tag to print info on
     * 
     * @return tooltip text
     */
    private String makeTermToolTip(TermTag termTag) {
        Term term = termTag.getTerm();
    
        StringBuilder sb = new StringBuilder();
    
        //
        // type
        sb.append("<html><dl><dt>Type:</dt><dd>").append(term.getType())
                .append("</dd>\n");
        
        //
        // statement
        if (term instanceof LiteralProgramTerm) {
            LiteralProgramTerm prog = (LiteralProgramTerm) term;
            String stm = prettyPrinter.print(prog.getStatement()).toString();
            sb.append("<dt>Statement:</dt><dd>").append(stm).append("</dd>\n");
        }

        //
        // history
        Annotation h = history;
        sb.append("<dt>History:</dt><dd><ol>");
        int len = 0;
        while (h != null && len < 60) {
            ProofNode creatingProofNode = h.getCreatingProofNode();
            if(creatingProofNode == null || shouldShow(creatingProofNode)) {
                String text = h.getText();
                
                if(creatingProofNode != null)
                    text = instantiateString(creatingProofNode.getAppliedRuleApp(), text);
                
                sb.append("<li>").append(text);
                
                sb.append("</li>\n");
                len++;
            }
            h = h.getParentAnnotation();
        }
        sb.append("</ol>\n");
        if(len == 60)
            sb.append("... truncated history");
    
        sb.append("</dl>");
        
        // System.out.println(sb);
        return sb.toString();
    }
    
    /**
     * check whether verbosity makes us show this node:
     * - verbosity of node <= set verbosity
     * 
     * @see ProofComponentModel#shouldShow(ProofNode)
     */
    private boolean shouldShow(ProofNode node) {
        RuleApplication ruleApp = node.getAppliedRuleApp();
        
        if(ruleApp == null)
            return true;
        
        // not here
        // if(node.getChildren() == null)
        //     return true;
        
        Rule rule = ruleApp.getRule();
        String string = rule.getProperty(RuleTagConstants.KEY_VERBOSITY);
        int value;
        try {
            value = Integer.parseInt(string);
        } catch (NumberFormatException e) {
            return true;
        }
        
        return value <= verbosityLevel;
    }
    
    /*
     * instantiate the schema variables in a string.
     * @see ProofComponentModel
     * 
     */
    private String instantiateString(RuleApplication ruleApp, String string) {
        TextInstantiator textInst = new TextInstantiator(ruleApp);
        return textInst.replaceInString(string, prettyPrinter);
    }

    /**
     * Gets the termselector for a subterm at a certain point in the component
     * 
     * @param point
     *            the point within the component
     * 
     * @return the selector for the subterm at this point or null if there is none
     */
    public TermSelector getTermAt(Point point) {
        int charIndex = viewToModel(point);
        TermTag termTag = annotatedString.getAttributeAt(charIndex);
        if(termTag == null)
            return null;
        
        return termTag.getTermSelector(termSelector);
    }

    // stolen from KeY
    public int viewToModel(Point p) {
        String seqText = getText();

        // bugfix for empty strings
        if (seqText.length() == 0)
            return 0;

        int cursorPosition = super.viewToModel(p);

        cursorPosition -= (cursorPosition > 0 ? 1 : 0);

        cursorPosition = (cursorPosition >= seqText.length() ? seqText.length() - 1
                : cursorPosition);
        cursorPosition = (cursorPosition >= 0 ? cursorPosition : 0);

        int previousCharacterWidth = getFontMetrics(getFont()).charWidth(
                seqText.charAt(cursorPosition));

        int characterIndex = super.viewToModel(new Point((int) p.getX()
                - (previousCharacterWidth / 2), (int) p.getY()));

        characterIndex = Math.max(0, characterIndex);
        
        return characterIndex;
    }

    public void markSubterm(SubtermSelector selector, int type) {
        if (type < 0 || type >= MARKINGS.length) {
            throw new IndexOutOfBoundsException();
        }
        
        int termNo = selector.getLinearIndex(term);

        int begin = -1;
        int end = -1;
        for(Element<TermTag> block : annotatedString.getAllAnnotations()) {
            TermTag tag = block.getAttr();
            if(tag.getTotalPos() == termNo) {
                begin = block.getBegin();
                end = block.getEnd();
                break;
            }
        }
        
        if(begin == -1) {
            Log.println("cannot mark subterm number " + termNo + " in " + annotatedString);
            return;
        }

        try {
            Object mark = getHighlighter().addHighlight(begin, end,
                    MARKINGS[type]);
            marks.add(mark);
        } catch (BadLocationException e) {
            throw new Error(e);
        }
    }

    public void clearMarks() {
        for (Object highlight : marks) {
            getHighlighter().removeHighlight(highlight);
        }
        marks.clear();
    }

    public Transferable createTransferable() {
        if(mouseSelection == null)
            return null;
        
        TermSelector ts = new TermSelector(termSelector, mouseSelection.getTotalPos());
        String string = mouseSelection.getTerm().toString(false);
        return new TermSelectionTransferable(ts, string);
    }

    public boolean dropTermOnLocation(TermSelector ts, Point point) {
        // TODO Implement drag and drop between terms
        Log.log(Log.WARNING, "NOT IMPLEMENTED YET");
        Log.log(Log.WARNING, ts +" on " + getTermAt(point));
        return false;
    }


}
