/*
 * This file is part of PSEUDO
 * Copyright (C) 2009 Universitaet Karlsruhe, Germany
 *    written by Mattias Ulbrich
 * 
 * The system is protected by the GNU General Public License. 
 * See LICENSE.TXT for details.
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JTextPane;
import javax.swing.border.Border;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.Highlighter.HighlightPainter;

import nonnull.NonNull;
import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.proof.TermSelector;
import de.uka.iti.pseudo.term.LiteralProgramTerm;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.TermException;
import de.uka.iti.pseudo.util.AnnotatedStringWithStyles;
import de.uka.iti.pseudo.util.TermSelectionTransfer;
import de.uka.iti.pseudo.util.TermSelectionTransferable;
import de.uka.iti.pseudo.util.settings.Settings;

/**
 * The Class TermComponent is used to show terms, it allows highlighting.
 */
public class TermComponent extends JTextPane {

    private static Settings S = Settings.getInstance();
    
    private static final long serialVersionUID = -4415736579829917335L;

    /**
     * some UI constants
     */
    private static final Font FONT = S.getFont("pseudo.termcomponent.font");
    
    // the highlight color should be bright
    private static final Color HIGHLIGHT_COLOR = S.getColor("pseudo.termcomponent.highlightcolor");

    // the modality background should be rather unnoticed
    private static final Color MODALITY_BACKGROUND = S.getColor("pseudo.termcomponent.modalitybackground");
    
    // border color needs to match background of sequent view
    private static final Color BORDER_COLOR = S.getColor("pseudo.termcomponent.bordercolor");

    // empty border
    private static final Border BORDER = BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR), BorderFactory
                    .createEmptyBorder(5, 5, 5, 5));

    // darker and a lighter color for marking
    private HighlightPainter[] MARKINGS = {
            new DefaultHighlighter.DefaultHighlightPainter(new Color(0x9999FF)),
            new DefaultHighlighter.DefaultHighlightPainter(new Color(0x99CCFF)) };

    /**
     * The term displayed
     */
    private Term term;
    
    /**
     * the term which is currently highlighted by the mouse
     * can be null
     */
    private int mouseSelectedSubterm;
    
    /**
     * the position of term within its sequent
     */
    private TermSelector termSelector;

    /**
     * The environment in which term lives
     */
    private Environment env;

    /**
     * The annotated string containing the pretty printed term
     */
    private AnnotatedStringWithStyles<Term> annotatedString;

    /**
     * The highlight object as returned by the highlighter
     */
    private Object theHighlight;

    // TODO DOC
    private List<Object> marks = new ArrayList<Object>();

    private boolean open;

    // TODO DOC
    /**
     * Instantiates a new term component.
     * 
     * @param t
     *            the term to display
     * @param open
     * @param env
     *            the environment to use for pretty printing
     * @param termSelector
     *            selector object describing the position of the displayed term
     *            in its sequent
     * @param prettyPrinter
     *            the pretty printer to print the term in this component
     */
    public TermComponent(@NonNull Term t, boolean open,
            @NonNull Environment env, @NonNull TermSelector termSelector,
            PrettyPrint prettyPrinter) {
        this.env = env;
        this.term = t;
        this.termSelector = termSelector;
        this.annotatedString = prettyPrinter.print(t);
        this.open = open;

        //
        // Set display properties
        setEditable(false);
        setBorder(BORDER);
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
    }

    private static Map<String, AttributeSet> attributeCache = new HashMap<String, AttributeSet>();
    private AnnotatedStringWithStyles.AttributeSetFactory attributeFactory = new AnnotatedStringWithStyles.AttributeSetFactory() {
        public AttributeSet makeStyle(String descr) {

            if (!open)
                descr += " closed";

            AttributeSet cached = attributeCache.get(descr);

            if (cached != null)
                return cached;

            MutableAttributeSet retval = new SimpleAttributeSet();
            StyleConstants.setFontFamily(retval, FONT.getFamily());
            StyleConstants.setFontSize(retval, FONT.getSize());

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
                StyleConstants.setForeground(retval, Color.magenta);
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
            mouseSelectedSubterm = -1;
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
                
                mouseSelectedSubterm = annotatedString.getAttributeIndexAt(index);
                Term term = annotatedString.getAttributeAt(index);
                setToolTipText(makeTermToolTip(term));
            } else {
                getHighlighter().changeHighlight(theHighlight, 0, 0);
                mouseSelectedSubterm = -1;
            }
        } catch (BadLocationException ex) {
            // TODO just ignore this for now
            ex.printStackTrace();
        }
    }

    /**
     * calculate the text to display in tooltip for a term Usually this is the
     * type. For programs, however, print the current statement
     * 
     * @param term
     *            to print
     * @return tooltip text
     */
    private String makeTermToolTip(Term term) {
        if (term instanceof LiteralProgramTerm) {
            LiteralProgramTerm prog = (LiteralProgramTerm) term;
//            try {
                return prog.getStatement().toString();
//            } catch (TermException e) {
//            }
        }

        return term.getType().toString();
    }

    /**
     * Gets the termselector for a subterm at a certain point in the component
     * 
     * @param point
     *            the point within the component
     * 
     * @return the selector for the subterm at this point
     */
    public TermSelector getTermAt(Point point) {
        int charIndex = viewToModel(point);
        int termIndex = annotatedString.getAttributeIndexAt(charIndex);
        
        if(termIndex == -1)
            return null;
        
        return termSelector.selectSubterm(termIndex);
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

    public void markSubterm(int subtermNo, int type) {
        if (type < 0 || type >= MARKINGS.length) {
            throw new IndexOutOfBoundsException();
        }

        int begin = annotatedString.getBeginOf(subtermNo);
        int end = annotatedString.getEndOf(subtermNo);

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
        if(mouseSelectedSubterm == -1)
            return null;
        
        TermSelector ts = new TermSelector(termSelector, mouseSelectedSubterm);
        String string = annotatedString.getAttributeOf(mouseSelectedSubterm).toString(false);
        return new TermSelectionTransferable(ts, string);
    }

    public boolean dropTermOnLocation(TermSelector ts, Point point) {
        // TODO Implement drag and drop between terms
        System.out.println("NOT IMPLEMENTED YET");
        System.out.println(ts +" on " + getTermAt(point));
        return false;
    }


}
