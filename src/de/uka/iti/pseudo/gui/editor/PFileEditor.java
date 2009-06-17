package de.uka.iti.pseudo.gui.editor;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.net.URL;

import javax.swing.AbstractButton;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;

import de.uka.iti.pseudo.parser.ParseException;
import de.uka.iti.pseudo.parser.Parser;
import de.uka.iti.pseudo.parser.Token;
import de.uka.iti.pseudo.util.Util;

public class PFileEditor extends JPanel implements ActionListener {
    
    private static final long serialVersionUID = 8116827588545997986L;
    private JTextPane editor;
    private File editedFile;
    private String fileContent;
    private boolean editing = false;
    
    public PFileEditor(File file) throws IOException {
        editedFile = file;
        fileContent = readFileAsString(file);
        init();
    }

    private void init() throws IOException {
        setLayout(new BorderLayout());
        {
            // TODO Do this right. Use a BarManager.
            JToolBar toolbar = new JToolBar();
            toolbar.add(makeCommand(new JButton(), "save", "img/disk.png"));
            toolbar.add(makeCommand(new JToggleButton(), "edit", "img/pencil.png"));
            toolbar.add(new JToolBar.Separator());
            toolbar.add(makeCommand(new JButton(), "cut", "img/cut.png"));
            toolbar.add(makeCommand(new JButton(), "copy", "img/copy.png"));
            toolbar.add(makeCommand(new JButton(), "pase", "img/paste.png"));
            toolbar.setFloatable(false);
            add(toolbar, BorderLayout.NORTH);
        }
        {
            editor = new JTextPane();
            editor.setText(fileContent);
            editor.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
            addSyntaxHighlighting();
            editor.setEditable(editing);
            JScrollPane scroll = new JScrollPane(editor);
            add(scroll, BorderLayout.CENTER);
        }
    }
    
    private void addSyntaxHighlighting() {
        Parser p = new Parser(new StringReader(fileContent));
        Token first = p.getToken(1);
        try {
            p.File();
            // set status ... everything is ok.
        } catch (ParseException e) {
            e.printStackTrace();
            Token problemtoken = e.currentToken.next;
        }
        
    }

    private AbstractButton makeCommand(AbstractButton but, String command, String iconLoc) {
        URL url = getClass().getResource(iconLoc);
        Icon icon;
        if(url == null)
            icon = Util.UNKNOWN_ICON;
        else
            icon = new ImageIcon(url);
        but.setIcon(icon);
        but.setActionCommand(command);
        return but;
    }

    /** 
     * @param filePath      name of file to open. The file can reside
     *                      anywhere in the classpath
     */
    private static String readFileAsString(File file)
            throws java.io.IOException {
        StringBuffer fileData = new StringBuffer();
        Reader reader = new FileReader(file);
        char[] buf = new char[1024];
        int numRead = 0;
        while ((numRead = reader.read(buf)) != -1) {
            fileData.append(buf, 0, numRead);
        }
        reader.close();
        return fileData.toString();
    }

    @Override public void actionPerformed(ActionEvent e) {
        // TODO Implement PFileEditor.actionPerformed
        
    }
    
    public static void main(String[] args) throws IOException {
        JFrame f = new JFrame("Pseudo Test Editor");
        PFileEditor editor = new PFileEditor(new File("examples/first.p"));
        f.getContentPane().add(editor, BorderLayout.CENTER);
        f.setSize(300,600);
        f.setVisible(true);
    }
}
