package de.uka.iti.pseudo.gui;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;

import javax.swing.Icon;
import javax.swing.JComponent;

import de.uka.iti.pseudo.gui.bar.BarManager;

// PROOF OF CONCEPT ONLY --- change all those numbers to 
// things calc'ed from FrontMetrics
// plus make it configurable.

public class ProgramComponent extends JComponent {
    
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
    
    public ProgramComponent(ProofCenter proofCenter) {
        // TODO Auto-generated constructor stub
    }

    @Override public void paint(Graphics g) {
        g.setColor(Color.red);
        g.clearRect(0, 0, getWidth(), getHeight());
    }
    
}
