package de.uka.iti.pseudo.gui;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;

import nonnull.NonNull;

import de.uka.iti.pseudo.environment.Program;
import de.uka.iti.pseudo.environment.SourceAnnotation;
import de.uka.iti.pseudo.gui.bar.BarManager;
import de.uka.iti.pseudo.proof.ProofNode;
import de.uka.iti.pseudo.proof.RuleApplication;
import de.uka.iti.pseudo.term.LiteralProgramTerm;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.TermException;
import de.uka.iti.pseudo.term.TermVisitor;
import de.uka.iti.pseudo.term.creation.DefaultTermVisitor;

// PROOF OF CONCEPT ONLY --- change all those numbers to 
// things calc'ed from FrontMetrics
// plus make it configurable.

public class ProgramComponent extends JComponent  {

    private static final Font FONT_LARGE = new Font(Font.MONOSPACED, Font.PLAIN, 14);
    private static final Font FONT_SMALL = new Font(Font.MONOSPACED, Font.PLAIN, 12);
    
    private static final Icon PLUS_ICON = BarManager
            .makeIcon(ProgramComponent.class
                    .getResource("img/bullet_toggle_plus.png"));
    
    private static final Icon MINUS_ICON = BarManager
            .makeIcon(ProgramComponent.class
                    .getResource("img/bullet_toggle_minus.png"));
    
    private static final Color LIGHT_BLUE = new Color(192, 192, 255);

    private ProofNode proofNode;
    private Program program;
    private List<Node> statementNodes = new ArrayList<Node>();
    
    private Node root;
    private Set<Node> selectedNodes = new HashSet<Node>();
    private PrettyPrint prettyPrinter;
    
    private static class Node {
        public Node(String string) {
            text = string;
        }
        boolean expanded = true;
        String text;
        Node next = null;
        Node child = null;
        Node top = null;
        int ypos;
    }
    
    public ProgramComponent(ProofCenter proofCenter) {
        setBackground(Color.white);
        
        addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 1
                        && SwingUtilities.isLeftMouseButton(e))
                    expandClick(e.getPoint());
            }
        });
        
        this.prettyPrinter = proofCenter.getPrettyPrinter();
        prettyPrinter.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                ProgramComponent.this.repaint();
            }
        });
    }
    
    private void makeModel() {
        
        if(program == null) {
            root = null;
            return;
        }
        
        List<SourceAnnotation> sources = program.getSourceAnnotations();
        statementNodes.clear();
        
        int sourcePtr = 0;

        root = new Node(null);
        root.expanded = true;
        
        Node lastTop = root;
        Node last = root;
        
        int len = program.countStatements();
        for(int i = 0; i < len; i++) {
            while(sourcePtr < sources.size() && 
                    sources.get(sourcePtr).getStatementNo() == i) {
                Node node = new Node(sources.get(sourcePtr).toString());
                lastTop.next = node;
                last = lastTop = node;
                sourcePtr++;
            }
            String text = prettyPrinter.print(program.getStatement(i)).toString();
            Node prgLine = new Node(i + ": " + text);
            statementNodes.add(prgLine);
            last.child = prgLine;
            prgLine.top = lastTop;
            last = prgLine;
        }
        
        while(sourcePtr < sources.size()) {
            Node node = new Node(sources.get(sourcePtr).toString());
            lastTop.next = node;
            last = lastTop = node;
            sourcePtr++;
        }

    }
    
    public void paintComponent(Graphics g) {
        g.setColor(getBackground());
        g.fillRect(0, 0, getWidth(), getHeight());
        
        if(program == null)
            return;
        
        int y = 0;
        
        Node node = root;
        while(node != null) {
            
            if(node.text != null) {
                g.setFont(FONT_LARGE);
                y += 16;
                node.ypos = y;
                if(selectedNodes.contains(node)) {
                    g.setColor(LIGHT_BLUE);
                    g.fillRect(0, y-14, getWidth(), 18);
                }
                g.setColor(Color.blue);
                g.drawString(node.text, 20, y);
                if(node.child != null) {
                    Icon icon = node.expanded ? MINUS_ICON : PLUS_ICON;
                    icon.paintIcon(this, g, 5, y - 12);
                }
            }
            
            if(node.expanded && node.child != null) {
                Node child = node.child;
                g.setFont(FONT_SMALL);
                while(child != null) {
                    y += 14;
                    if(selectedNodes.contains(child)) {
                        g.setColor(Color.lightGray);
                        g.fillRect(0, y-14, getWidth(), 18);
                    }
                    g.setColor(Color.gray);
                    g.drawString(child.text, 40, y);
                    child = child.child;
                }
            }
            node = node.next;
        }
    }

    private void expandClick(Point p) {
        
        if(p.x < 5 || p.x > 5 + PLUS_ICON.getIconWidth())
            return;
        
        // do not consider root
        Node node = root.next;
        while(node != null) {
            if(p.y > node.ypos - 12 && p.y < node.ypos - 12 + PLUS_ICON.getIconHeight()) {
                node.expanded = !node.expanded;
                repaint();
                return;
            }
            node = node.next;
        }
    }
    
    private TermVisitor selectionFindVisitor = new DefaultTermVisitor.DepthTermVisitor() {
        public void visit(LiteralProgramTerm progTerm)
                throws TermException {
            int index = progTerm.getProgramIndex();
            if(index < statementNodes.size() && progTerm.getProgram() == program) {
                Node node = statementNodes.get(index);
                selectedNodes.add(node);
                selectedNodes.add(node.top);
            }
        }
    };

    public void setProofNode(ProofNode node) {
        proofNode = node;
        selectTerms();
        repaint();
    }

    private void selectTerms() {
        
        if(proofNode == null || program == null)
            return;
        
        try {
            selectedNodes.clear();
            for (Term term : proofNode.getSequent().getAntecedent()) {
                term.visit(selectionFindVisitor);
            }
            for (Term term : proofNode.getSequent().getSuccedent()) {
                term.visit(selectionFindVisitor);
            }
        } catch (TermException e) {
            // should not happen
            throw new Error(e);
        }
    }

    public Program getProgram() {
        return program;
    }

    public void setProgram(Program program) {
        this.program = program;
        makeModel();
        selectTerms();
        repaint();
    }
    
    private void dump() {
        Node n = root;
        while(n != null) {
            System.out.println(n + " " + n.text);
            Node m = n.child;
            while(m != null) {
                System.out.println("  " + m + " " + m.text);
                m = m.child;
            }
            n = n.next;
        }
    }

    
}
