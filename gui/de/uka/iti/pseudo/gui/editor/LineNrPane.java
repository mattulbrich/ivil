/*
 * This file is part of PSEUDO
 * Copyright (C) 2009-2010 Universitaet Karlsruhe, Germany
 *    written by Mattias Ulbrich
 * 
 * The system is protected by the GNU General Public License. 
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.gui.editor;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Point;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import de.uka.iti.pseudo.gui.BracketMatchingTextArea;

/**
 * A class illustrating running line number count on JTextPane. Nothing is
 * painted on the pane itself, but a separate JPanel handles painting the line
 * numbers.<br>
 * 
 * @author Daniel Sj�blom<br>
 *         Created on Mar 3, 2004<br>
 *         Copyright (c) 2004<br>
 * @version 1.0<br>
 */
@Deprecated
class LineNrPane extends JPanel {
	// for this simple experiment, we keep the pane + scrollpane as members.
	private JTextArea pane;
	private JScrollPane scrollPane;
	private LineNr linenr;
	
	public LineNrPane() {
		super();
		linenr = new LineNr();
		//linenr.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));
		
		pane = new BracketMatchingTextArea()
		// we need to override paint so that the
		// linenumbers stay in sync
		{
			public void paint(Graphics g) {
				super.paint(g);
				linenr.repaint();
			}
		};
		scrollPane = new JScrollPane(pane);
		
		setLayout(new BorderLayout());
		add(linenr, BorderLayout.WEST);
		add(scrollPane, BorderLayout.CENTER);
		
		linenr.setMinimumSize(new Dimension(40,30));
		linenr.setPreferredSize(new Dimension(40,30));
	}

	@SuppressWarnings("serial") 
	private class LineNr extends JComponent {
		public void paint(Graphics g) {
			super.paint(g);

			int fontHeight = g.getFontMetrics(pane.getFont()).getHeight(); // font height
			Point viewPosition = scrollPane.getViewport().getViewPosition();

			int start = viewPosition.y / fontHeight; // pane.viewToModel(viewPosition); // starting pos in document

			int end = (viewPosition.y + pane.getHeight()) / fontHeight; // end pos in doc

			// translate offsets to lines
			//Document doc = pane.getDocument();

			int offset = viewPosition.y % fontHeight;
			//System.out.println(viewPosition + " " + start + " " + end);
			
			g.setFont(new Font("Monospaced", Font.PLAIN, 12));
			for (int line = start, y = -offset-2; line <= end; line++, y += fontHeight) {
				g.drawString(String.format("%4d", line), 0, y);
			}

		}
	}

	// test main
	public static void main(String[] args) {
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		final LineNrPane nr = new LineNrPane();
		frame.getContentPane().add(nr);
		frame.setSize(new Dimension(400, 400));
		frame.show();
	}

	public JTextArea getPane() {
		return pane;
	}
}