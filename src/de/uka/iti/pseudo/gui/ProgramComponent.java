package de.uka.iti.pseudo.gui;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.IntrospectionException;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

import de.uka.iti.pseudo.environment.Program;
import de.uka.iti.pseudo.environment.SourceAnnotation;
import de.uka.iti.pseudo.gui.bar.BarManager;
import de.uka.iti.pseudo.proof.ProofNode;
import de.uka.iti.pseudo.term.LiteralProgramTerm;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.TermException;
import de.uka.iti.pseudo.term.TermVisitor;
import de.uka.iti.pseudo.term.creation.DefaultTermVisitor;

// PROOF OF CONCEPT ONLY --- change all those numbers to 
// things calc'ed from FrontMetrics
// plus make it configurable.

public class ProgramComponent extends JComponent implements ActionListener  {
    
    /**
     * some UI properties which can be modified using editors.
     */
    private Font sourceFont = Main.getFont("pseudo.program.sourcefont");
    private Font boogieFont = Main.getFont("pseudo.program.boogiefont");
    private Color sourceHighlightColor = Main.getColor("pseudo.program.sourcehighlight");
    private Color sourceColor = Main.getColor("pseudo.program.sourcecolor");
    private Color boogieHighlightColor = Main.getColor("pseudo.program.boogiehighlight");
    private Color boogieColor = Main.getColor("pseudo.program.boogiecolor");
    
    private static final Icon PLUS_ICON = BarManager
            .makeIcon(ProgramComponent.class
                    .getResource("img/bullet_toggle_plus.png"));
    
    private static final Icon MINUS_ICON = BarManager
            .makeIcon(ProgramComponent.class
                    .getResource("img/bullet_toggle_minus.png"));
    
    private int sourceTextHeight = -1;
    private int sourceTextDescent = -1;
    private int boogieTextHeight = -1;
    private int boogieTextDescent = -1;
    
    private ProofNode proofNode;
    private Program program;
    private List<Node> statementNodes = new ArrayList<Node>();
    
    private Node root;
    private Set<Node> selectedNodes = new HashSet<Node>();
    private PrettyPrint prettyPrinter;
    private JPopupMenu popup;
    
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
    
    public ProgramComponent(ProofCenter proofCenter) throws IOException {
        setBackground(Color.white);
        
        addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 1
                        && SwingUtilities.isLeftMouseButton(e))
                    expandClick(e.getPoint());
                else if(e.getClickCount() == 1
                        && SwingUtilities.isRightMouseButton(e)) {
                    Point p = e.getPoint(); 
                    popup.show(ProgramComponent.this, p.x, p.y);
                }
            }
        });
        
        this.prettyPrinter = proofCenter.getPrettyPrinter();
        prettyPrinter.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                ProgramComponent.this.repaint();
            }
        });
        
        URL resource = getClass().getResource("bar/popup.menu.properties");
        if(resource == null)
            throw new IOException("resource bar/popup.menu.properties not found");
        BarManager barManager = new BarManager(this, resource);
        popup = barManager.makePopup("program.popup");
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
        
        calcTextHeights(g);
        
        int iconheight = PLUS_ICON.getIconHeight();
        int iconoffset = (sourceTextHeight + iconheight)/2;
        int y = 0;
        
        Node node = root;
        while(node != null) {
            
            if(node.text != null) {
                g.setFont(sourceFont);
                y += sourceTextHeight;
                node.ypos = y;
                if(selectedNodes.contains(node)) {
                    g.setColor(sourceHighlightColor);
                    g.fillRect(0, y-sourceTextHeight, getWidth(), sourceTextHeight);
                }
                g.setColor(sourceColor);
                g.drawString(node.text, 20, y-sourceTextDescent);
                if(node.child != null) {
                    Icon icon = node.expanded ? MINUS_ICON : PLUS_ICON;
                    icon.paintIcon(this, g, 5, y - iconoffset);
                }
            }
            
            if(node.expanded && node.child != null) {
                Node child = node.child;
                g.setFont(boogieFont);
                while(child != null) {
                    y += boogieTextHeight;
                    if(selectedNodes.contains(child)) {
                        g.setColor(boogieHighlightColor);
                        g.fillRect(0, y-boogieTextHeight, getWidth(), boogieTextHeight);
                    }
                    g.setColor(boogieColor);
                    g.drawString(child.text, 40, y-boogieTextDescent);
                    child = child.child;
                }
            }
            node = node.next;
        }
    }

    private void calcTextHeights(Graphics g) {
        if(sourceTextHeight == -1) {
            g.setFont(sourceFont);
            FontMetrics fm = g.getFontMetrics();
            sourceTextHeight = fm.getHeight();
            sourceTextDescent = fm.getDescent();
        }
        if(boogieTextHeight == -1) {
            g.setFont(boogieFont);
            FontMetrics fm = g.getFontMetrics();
            boogieTextHeight = fm.getHeight();
            boogieTextDescent = fm.getDescent();
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

    public Font getSourceFont() {
        return sourceFont;
    }

    public void setSourceFont(Font sourceFont) {
        this.sourceFont = sourceFont;
        repaint();
    }

    public Font getBoogieFont() {
        return boogieFont;
    }

    public void setBoogieFont(Font boogieFont) {
        this.boogieFont = boogieFont;
        repaint();
    }

    public Color getSourceHighlightColor() {
        return sourceHighlightColor;
    }

    public void setSourceHighlightColor(Color sourceHighlightColor) {
        this.sourceHighlightColor = sourceHighlightColor;
        repaint();
    }

    public Color getSourceColor() {
        return sourceColor;
    }

    public void setSourceColor(Color sourceColor) {
        this.sourceColor = sourceColor;
        repaint();
    }

    public Color getBoogieHighlightColor() {
        return boogieHighlightColor;
    }

    public void setBoogieHighlightColor(Color boogieHighlightColor) {
        this.boogieHighlightColor = boogieHighlightColor;
        repaint();
    }

    public Color getBoogieColor() {
        return boogieColor;
    }

    public void setBoogieColor(Color boogieColor) {
        this.boogieColor = boogieColor;
        repaint();
    }

    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();
        if("expandAll".equals(command)) {
            for(Node n = root; n != null; n = n.next) {
                n.expanded = true;
            }
        } else if("collapseAll".equals(command)) {
            for(Node n = root; n != null; n = n.next) {
                n.expanded = false;
            }
        } else if("expandSelected".equals(command)) {
            for(Node n = root; n != null; n = n.next) {
                n.expanded = selectedNodes.contains(n);
            }
        }
        repaint();
    }

}


class ProgramComponentBeanInfo extends SimpleBeanInfo {
    
    private static final PropertyDescriptor[] PROPERTIES = {
        makeProperty("sourceFont", "Font for source code"),
        makeProperty("sourceColor", "Color for source code"),
        makeProperty("sourceHighlightColor", "Color for source code highlighting"),
        makeProperty("boogieFont", "Font for boogie code"),
        makeProperty("boogieColor", "Color for boogie code"),
        makeProperty("boogieHighlightColor", "Color for boogie code highlighting")
    };

    public PropertyDescriptor[] getPropertyDescriptors() {
        return super.getPropertyDescriptors();
    }
    
    private static PropertyDescriptor makeProperty(String name, String descr) {
        try {
            PropertyDescriptor pd = new PropertyDescriptor(name, ProgramComponent.class);
            pd.setName(descr);
            return pd;
        } catch (IntrospectionException e) {
            throw new Error(e);
        }
    }
}
