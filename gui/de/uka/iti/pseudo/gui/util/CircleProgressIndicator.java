/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2010 Universitaet Karlsruhe, Germany
 * 
 * The system is protected by the GNU General Public License. 
 * See LICENSE.TXT (distributed with this file) for details.
 * 
 */
package de.uka.iti.pseudo.gui.util;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.WindowConstants;

import de.uka.iti.pseudo.util.Log;
import de.uka.iti.pseudo.util.SimpleLog;

/**
 * A CircleProgressIndicator can be used to give a visual indication that a
 * process is running indeterministically long.
 * 
 * @author mattias ulbrich (c) 2005
 */
public class CircleProgressIndicator extends JComponent implements HierarchyListener, ActionListener {

    private static final long serialVersionUID = 8404173462347770171L;
    
    private static final Dimension PREF_DIM = new Dimension(60,60);
    
    private static final Color DEFAULT_COLOR = UIManager.getColor("ProgressBar.foreground");
    
    private int steps = 8;
    private int delay = 200;
    private int radiusPercent = 8;
    private int borderSpace = 10;
    
    private Color[] colors;
    private double sin[];
    private double cos[];
    private Timer timer = new Timer(delay, this);
    private int animIndex;
    
    public CircleProgressIndicator(Color foreground) {
        setPreferredSize(PREF_DIM);
        setSize(PREF_DIM);
        calcSinCos();
        setForeground(foreground);
        addHierarchyListener(this);
    }

    private void calcSinCos() {
        sin = new double[steps];
        cos = new double[steps];
        
        for (int i = 0; i < steps; i++) {
            sin[i] = Math.sin(Math.PI * 2 * i / steps);
            cos[i] = -Math.cos(Math.PI * 2 * i / steps);
        }
    }

    public CircleProgressIndicator() {
        this(DEFAULT_COLOR);
    }
    
    @Override
    public void setForeground(Color fg) {
        super.setForeground(fg);
        
        // w/o alpha
        int rgb = fg.getRGB() & 0xffffff;
        
        colors = new Color[steps];
        for(int i = 0; i < steps; i++) {
            colors[steps-1-i] = new Color(rgb | (((i*255)/steps) << 24), true);
        }
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        // Log.enter();
        
        Graphics2D g2 = (Graphics2D)g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                             RenderingHints.VALUE_ANTIALIAS_ON);
        
        int h = getHeight();
        int w = getWidth();
        int m = Math.min(w, h);
        int radius = radiusPercent * m / 100;
        int R = m/2 - borderSpace - radius;
        
        for(int i = 0; i < steps; i++) {
            
            g.setColor(colors[(animIndex + i) % steps]);
            
            int x = (int) (cos[i] * R + w/2);
            int y = (int) (sin[i] * R + h/2);
            
            g.fillOval(x-radius, y-radius, 2*radius, 2*radius);
        }
    }
    
    public static void main(String[] args) {
        Log.setLogImplementation(new SimpleLog(1));
        JFrame f = new JFrame("CPI");
        f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        
        f.getContentPane().add(new CircleProgressIndicator(Color.green.darker()));
        f.pack();
        f.show();
    }
    
 // we don't want the animation to keep running if we're not displayable
    public void hierarchyChanged(HierarchyEvent he) {
        // Log.enter(he);
        if ((he.getChangeFlags() & HierarchyEvent.DISPLAYABILITY_CHANGED) != 0) {
            if (isDisplayable()) {
                timer.start();
            } else {
                timer.stop();
            }
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // Log.enter();
        animIndex = (animIndex + 1) % steps;
        repaint();
    }
    
    /**
     * @return the steps
     */
    public int getSteps() {
        return steps;
    }

    /**
     * @param steps the steps to set
     */
    public void setSteps(int steps) {
        int oldVal = this.steps;
        this.steps = steps;
        calcSinCos();
        setForeground(getForeground());
        firePropertyChange("CircleProgressIndicator.steps", oldVal, steps);
        repaint();
    }

    /**
     * @return the delay
     */
    public int getDelay() {
        return delay;
    }

    /**
     * @param delay the delay to set
     */
    public void setDelay(int delay) {
        int oldVal = this.delay;
        this.delay = delay;
        firePropertyChange("CircleProgressIndicator.delay", oldVal, delay);
    }

    /**
     * @return the radiusPercent
     */
    public int getRadiusPercent() {
        return radiusPercent;
    }

    /**
     * @param radiusPercent the radiusPercent to set
     */
    public void setRadiusPercent(int radiusPercent) {
        int oldVal = this.radiusPercent;
        this.radiusPercent = radiusPercent;
        firePropertyChange("CircleProgressIndicator.radiusPercent", oldVal, radiusPercent);
        repaint();
    }

    /**
     * @return the borderSpace
     */
    public int getBorderSpace() {
        return borderSpace;
    }

    /**
     * @param borderSpace the borderSpace to set
     */
    public void setBorderSpace(int borderSpace) {
        int oldVal = this.borderSpace;
        this.borderSpace = borderSpace;
        firePropertyChange("CircleProgressIndicator.borderSpace", oldVal, borderSpace);
        repaint();
    }
}
