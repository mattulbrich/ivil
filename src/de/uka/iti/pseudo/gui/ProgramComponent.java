package de.uka.iti.pseudo.gui;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JComponent;

import de.uka.iti.pseudo.environment.LabelAnnotation;
import de.uka.iti.pseudo.environment.Program;
import de.uka.iti.pseudo.environment.SourceAnnotation;

public class ProgramComponent extends JComponent {

    private Program program;
    
    List<Node> nodes = new ArrayList<Node>();
    
    private static class Node {
        public Node(String string) {
            text = string;
        }
        boolean exanded = false;
        String text;
        List<String> lines = new LinkedList<String>();
        public boolean label;
    }
    
    public ProgramComponent(Program program) {

        setBackground(Color.white);
        this.program = program;
        makeModel();
        
    }   

    private void makeModel() {
        
        List<SourceAnnotation> sources = program.getSourceAnnotations();
        List<LabelAnnotation> labels = program.getLabelAnnotations();
        
        int sourcePtr = 0, labelPtr = 0;
        
        Node node = new Node("");
        nodes.add(node);
        
        int len = program.countStatements();
        for(int i = 0; i < len; i++) {
            while(sourcePtr < sources.size() && 
                    sources.get(sourcePtr).getStatementNo() == i) {
                node = new Node(sources.get(sourcePtr).toString());
                node.label = true;
                nodes.add(node);
                sourcePtr++;
            }
            while(labelPtr < labels.size() && 
                    labels.get(labelPtr).getStatementNo() == i) {
                node = new Node(labels.get(labelPtr).toString());
                nodes.add(node);
                labelPtr++;
            }
            node.lines.add(program.getStatement(i).toString());
        }
    }
    
    public void paintComponent(Graphics g) {
        int y = 20;
        g.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
        g.setColor(getBackground());
        g.fillRect(0, 0, getWidth(), getHeight());
        int stm = 0;
        for (Node node : nodes) {
            if(node.label) {
                g.setColor(Color.orange);
                g.drawString(node.text, 5, y);
            } else {
//                g.setColor(Color.blue);
//                g.drawString(node.text, 20, y);
            }
            y += 20;
            for (String s : node.lines) {
                g.setColor(Color.blue);
                g.drawString(stm + ": " + s, 35, y);
                y += 20;
                stm ++;
            }
        }
    }

    
}
