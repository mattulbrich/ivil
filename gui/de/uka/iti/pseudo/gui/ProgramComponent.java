package de.uka.iti.pseudo.gui;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;

import javax.swing.Icon;
import javax.swing.JComponent;

import de.uka.iti.pseudo.gui.bar.BarManager;
import de.uka.iti.pseudo.util.settings.Settings;

// PROOF OF CONCEPT ONLY --- change all those numbers to 
// things calc'ed from FrontMetrics
// plus make it configurable.

public class ProgramComponent extends JComponent {
    
    private static Settings SETTINGS = Settings.getInstance(); 
    
    /**
     * some UI properties which can be modified using editors.
     */
    private Font sourceFont = SETTINGS.getFont("pseudo.program.sourcefont");
    private Font boogieFont = SETTINGS.getFont("pseudo.program.boogiefont");
    private Color sourceHighlightColor = SETTINGS.getColor("pseudo.program.sourcehighlight");
    private Color sourceColor = SETTINGS.getColor("pseudo.program.sourcecolor");
    private Color boogieHighlightColor = SETTINGS.getColor("pseudo.program.boogiehighlight");
    private Color boogieColor = SETTINGS.getColor("pseudo.program.boogiecolor");
    
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
