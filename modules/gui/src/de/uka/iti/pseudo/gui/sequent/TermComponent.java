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

package de.uka.iti.pseudo.gui.sequent;

import java.awt.Color;
import java.awt.Font;
import java.awt.Point;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
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
import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.gui.ProofCenter;
import de.uka.iti.pseudo.parser.ASTVisitException;
import de.uka.iti.pseudo.parser.ParseException;
import de.uka.iti.pseudo.prettyprint.PrettyPrint;
import de.uka.iti.pseudo.prettyprint.TermTag;
import de.uka.iti.pseudo.proof.MutableRuleApplication;
import de.uka.iti.pseudo.proof.ProofException;
import de.uka.iti.pseudo.proof.ProofNode;
import de.uka.iti.pseudo.proof.RuleApplication;
import de.uka.iti.pseudo.proof.SubtermSelector;
import de.uka.iti.pseudo.proof.TermSelector;
import de.uka.iti.pseudo.proof.SequentHistory.Annotation;
import de.uka.iti.pseudo.rule.Rule;
import de.uka.iti.pseudo.rule.RuleTagConstants;
import de.uka.iti.pseudo.rule.where.Interactive;
import de.uka.iti.pseudo.term.LiteralProgramTerm;
import de.uka.iti.pseudo.term.SchemaType;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.Type;
import de.uka.iti.pseudo.term.creation.TermMaker;
import de.uka.iti.pseudo.util.AnnotatedStringWithStyles;
import de.uka.iti.pseudo.util.ExceptionDialog;
import de.uka.iti.pseudo.util.Log;
import de.uka.iti.pseudo.util.NotScrollingCaret;
import de.uka.iti.pseudo.util.TermSelectionTransfer;
import de.uka.iti.pseudo.util.TextInstantiator;
import de.uka.iti.pseudo.util.Util;
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
    
    // types should be painted less noticeable
    private static final Color TYPE_FOREGROUND = 
        S.getColor("pseudo.termcomponent.typeforeground", Color.LIGHT_GRAY);
    
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
            // Log.enter(e);
            // proofCenter.firePropertyChange(TERM_COMPONENT_SELECTED_TAG,
            // TermComponent.this, null);
            //
            // if (null != mouseSelection)
            // Log.log(Log.VERBOSE, mouseSelection);
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
     * @param termSelector
     *                selector object describing the position of the displayed
     *                term in its sequent
     */
    public TermComponent(@NonNull Term t, Annotation history, boolean open,
            @NonNull ProofCenter proofCenter, @NonNull TermSelector termSelector)  {
        this.term = t;
        this.history = history;
        this.termSelector = termSelector;
        this.proofCenter = proofCenter;
        this.prettyPrinter = proofCenter.getPrettyPrinter();
        this.verbosityLevel = (Integer)proofCenter.getProperty(ProofCenter.TREE_VERBOSITY);
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
            Log.log(Log.DEBUG, "Disabling popup menu in term component");
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

            if (descr.contains("type"))
                StyleConstants.setForeground(retval, TYPE_FOREGROUND);
            
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
                setSelectionStart(begin);
                setSelectionEnd(end);
                
                mouseSelection = annotatedString.getAttributeAt(index);
                setToolTipText(makeTermToolTip(mouseSelection));

                Log.enter(p);
                proofCenter.fireNotification(TERM_COMPONENT_SELECTED_TAG, TermComponent.this);

                if (null != mouseSelection)
                    Log.log(Log.VERBOSE, mouseSelection);
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
     * Returns the unpretty text representation which is needed sometimes for
     * interactions.
     */
    private String makeTermToolTip(TermTag termTag) {
        String rval = termTag.getTerm().toString();
        if (rval.length() > 128)
            rval = rval.substring(0, 125) + "...";
        return rval;
    }

    /**
     * calculate the text to display in "information for this formula" for a
     * term. This is a rather complex document:
     * 
     * <ol>
     * <li>the type
     * <li>For programs, print the current statement
     * <li>The history, at least the first 60 elements
     * </ol>
     * 
     * TODO Have something like "F2" to focus on the content and provide links
     * ;)
     * 
     * @param termTag
     *            tag to print info on
     * 
     */
    public String makeFormatedTermHistory(TermTag termTag) {
        Term term = termTag.getTerm();

        StringBuilder sb = new StringBuilder();

        //
        // type
        sb.append("<html><dl><dt>Type:</dt><dd>").append(term.getType()).append("</dd>\n");

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
            if (creatingProofNode == null || shouldShow(creatingProofNode)) {
                String text = h.getText();

                if (creatingProofNode != null)
                    text = instantiateString(creatingProofNode.getAppliedRuleApp(), text);

                sb.append("<li>").append(text);

                sb.append("</li>\n");
                len++;
            }
            h = h.getParentAnnotation();
        }
        sb.append("</ol>\n");
        if (len == 60)
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
            Log.log(Log.DEBUG, "cannot mark subterm number " + termNo + " in " + annotatedString);
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
        
        // Note: it might be necessary to transfer typed formulas
        return new StringSelection(mouseSelection.getTerm().toString(false));
    }

    /**
     * Drops a dragged term on this term component, which results in a rule
     * application pop where only interactive rules are displayed, which are
     * initialized with the dragged term.
     */
    @SuppressWarnings("unchecked")
    public boolean dropTermOnLocation(String term, Point point) {
        final Environment env = proofCenter.getEnvironment();
        
        try {
            Term instantiation;
            try{
                instantiation = TermMaker.makeAndTypeTerm(term, env);
            } catch (Exception e) {
                // if an exception occurs here, the dropped text is not a term

                // this behavior is needed to allow terms to be dragged out of
                // other applications such as browsers or document viewers
                return false;
            }

            List<RuleApplication> rulesApplicable = proofCenter.getApplicableRules(termSelector), ruleApps = new LinkedList<RuleApplication>();
            
            // buckets for priority 0 - 9
            List<RuleApplication>[] bucket = new List[10];
            for(int i = 0 ; i < 10; i++)
                bucket[i] = new ArrayList<RuleApplication>();

            // filter rules
            for (RuleApplication ra : rulesApplicable) {
                final String level = ra.getRule().getProperty("interactive");
                if(null == level)
                    continue;
                
                int lvl = Integer.parseInt(level);
                
                // set the interactive field to match instantiation
                for (Map.Entry<String, String> entry : ra.getProperties().entrySet()) {
                    String key = entry.getKey();
                    if(!key.startsWith(Interactive.INTERACTION + "("))
                       continue;

                    String value = entry.getValue();
                    boolean typeMode = false;

                    if (value.startsWith(Interactive.INSTANTIATE_PREFIX)) {
                        typeMode = true;
                        value = value.substring(Interactive.INSTANTIATE_PREFIX.length());
                    }

                    String svName = Util.stripQuotes(key.substring(Interactive.INTERACTION.length()));
                    Type svType;
                    try {
                        svType = TermMaker.makeType(value, env);
                    } catch (ASTVisitException e) {
                        Log.log(Log.WARNING, "cannot parseType: " + value + ", continue anyway");
                        continue;
                    } catch (ParseException e) {
                        Log.log(Log.WARNING, "cannot parseType: " + value + ", continue anyway");
                        continue;
                    }
                    
                    ra = new MutableRuleApplication(ra);

                    ra.getSchemaVariableMapping().put(svName, instantiation);
                    if(typeMode)
                        ra.getTypeVariableMapping().put(((SchemaType) svType).getVariableName(),
                                instantiation.getType());
                }

                // adjust level; if level is invalid, map it to 0
                lvl = lvl > 0 && lvl < 10? lvl : 0;
                bucket[lvl].add(ra);
            }
            // the user might have specified, that he wants allways the rule in
            // the highest bucket to be applied
            if (true) {
                for (int i = 9; i >= 0; i--) {
                    for (RuleApplication ra : bucket[i]) {
                        proofCenter.apply(ra);
                        return true;
                    }
                }
            }

            for (int i = 0; i < 10; i++)
                ruleApps.addAll(0, bucket[i]);

            // if no rules are applicable, the drop failed
            if (ruleApps.size() == 0)
                return false;

            // only one rule is applicable, so apply it
            if (ruleApps.size() == 1) {
                proofCenter.apply(ruleApps.get(0));
                return true;
            }

            // we dont know, so let the user decide what he wants

            // TODO implement small dialog which will let the user choose

        } catch (ProofException ex) {
            ExceptionDialog.showExceptionDialog(proofCenter.getMainWindow(), ex);
        }
        return false;
    }

    public TermTag getMouseSelection() {
        return mouseSelection;
    }

}
