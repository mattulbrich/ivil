package de.uka.iti.pseudo.gui.editor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Font;
import java.awt.HeadlessException;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;

import de.uka.iti.pseudo.environment.EnvironmentMaker;
import de.uka.iti.pseudo.gui.bar.BarManager;
import de.uka.iti.pseudo.gui.bar.CloseAction;
import de.uka.iti.pseudo.gui.bar.CloseEditorAction;
import de.uka.iti.pseudo.parser.ASTElement;
import de.uka.iti.pseudo.parser.ASTLocatedElement;
import de.uka.iti.pseudo.parser.ASTVisitException;
import de.uka.iti.pseudo.parser.ParseException;
import de.uka.iti.pseudo.parser.Parser;
import de.uka.iti.pseudo.parser.Token;
import de.uka.iti.pseudo.parser.file.ASTFile;

// TODO in some future: syntax highlighting

public class PFileEditor extends JFrame {
    
    private static final long serialVersionUID = 8116827588545997986L;
    private JTextArea editor;
    private File editedFile;
    private String content;
    private Object errorHighlighting;
    private JLabel statusLine;
    private BarManager barManager;
    private UpdateThread updateThread;
    private DocumentListener doclistener = new DocumentListener() {

        public void changedUpdate(DocumentEvent e) {
            updateThread.changed();
        }

        public void insertUpdate(DocumentEvent e) {
            updateThread.changed();
        }

        public void removeUpdate(DocumentEvent e) {
            updateThread.changed();
        }
        
    };
    
    
    
    private class UpdateThread extends Thread {
        public UpdateThread() {
            super("UpdateThread");
        }
        boolean disposed = false;
        Object lock = new Object();
        public void run() {
            while(!disposed) {
                try {
                    Thread.sleep(500);
                    addErrorHighlighting();
                    synchronized(lock) {
                        lock.wait();
                    }
                } catch (InterruptedException e) {
                    // start another round if we are interrupted
                } catch (RuntimeException e) {
                    System.err.println("Unexpected runtime exception");
                    e.printStackTrace();
                }
            }
        }
        
        public void changed() {
            synchronized(lock) {
                interrupt();
                lock.notify();
            }
        }
    }
    
    public PFileEditor(File file) throws IOException {
        editedFile = file;
        content = readFileAsString(file);
        init();
    }

    
    private void init() throws IOException {
        setLayout(new BorderLayout());
        Container contentPane = getContentPane();
        {
            barManager = new BarManager(null);
            barManager.putProperty(BarManager.PARENT_FRAME, this);
            JToolBar toolbar = barManager.makeToolbar(getClass().getResource("menu.properties")); 
            contentPane.add(toolbar, BorderLayout.NORTH);
            setJMenuBar(barManager.makeMenubar(getClass().getResource("menu.properties")));
        }
        {
            addWindowListener((WindowListener) barManager.makeAction(CloseEditorAction.class.getName()));    
            setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        }
        {
            LineNrPane lineNrPane = new LineNrPane();
            editor = lineNrPane.getPane();
            editor.setText(content);
            editor.setLineWrap(false);
            // TODO make this configurable
            editor.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
            editor.getDocument().addDocumentListener(doclistener);
            try {
                errorHighlighting = editor.getHighlighter().addHighlight(12, 19, new CurlyHighlightPainter());
            } catch (BadLocationException e) {
               // cannot happen
                throw new Error(e);
            }
            JScrollPane scroll = new JScrollPane(lineNrPane);
            contentPane.add(scroll, BorderLayout.CENTER);
        }
        {
            statusLine = new JLabel();
            contentPane.add(statusLine, BorderLayout.SOUTH);
        }
        {
            updateThread = new UpdateThread();
            updateThread.start();
        }
    }
    
    private void addErrorHighlighting() {
        Parser p = new Parser(new StringReader(editor.getText()));
        String filename = editedFile.getName();
        try {
            ASTFile file = p.File();
            file.setFilename(filename);
            new EnvironmentMaker(p, file, filename);
            
            System.err.println("Syntax checked ... no more errors");
            markError(null, null);
        } catch (ParseException e) {
            e.printStackTrace();
            Token problemtoken = e.currentToken.next;
            markError(e, problemtoken);
        } catch (ASTVisitException e) {
            e.printStackTrace();
            ASTLocatedElement location = e.getLocation();
            if (location instanceof ASTElement) {
                ASTElement ast = (ASTElement) location;
                if(ast.getFileName().equals(filename))
                    markError(e, ast.getLocationToken());
                else
                    markError(e, null);
            } else {
                markError(e, null);
            }
        }
        
    }

    private void markError(final Exception exc, final Token token) {
        Runnable action = new Runnable() {
            private int errorLine;
            public void run() {
                int from, to;
                try {
                    if(token == null) {
                        from = to = 0;
                        errorLine = -1;
                    } else {
                        from = toIndex(token.beginLine, token.beginColumn);
                        to = toIndex(token.endLine, token.endColumn) + 1;
                        errorLine = editor.getLineOfOffset(from) + 1;
                    }
                    editor.getHighlighter().changeHighlight(errorHighlighting, from, to);
                } catch (BadLocationException e) {
                    // really should not happen
                    throw new Error(e);
                }
                
                if(exc == null) {
                    statusLine.setForeground(Color.black);
                    statusLine.setText("syntax check succesful");
                    statusLine.setToolTipText("");
                } else {
                    statusLine.setForeground(Color.red);
                    if(errorLine == -1)
                        statusLine.setText("Error outside this file while parsing");
                    else
                        statusLine.setText("Error in line " + errorLine);
                    statusLine.setToolTipText(htmlize(exc.getMessage()));
                }

                getContentPane().invalidate();
                getContentPane().doLayout();
                repaint();
            }

            private String htmlize(String message) {
                message = message.replace("&", "&amp;");
                message = message.replace("<", "&lt;");
                message = message.replace("\n", "<br>");
                return "<html><pre>" + message + "</pre>";
            }
        };
        
        SwingUtilities.invokeLater(action);
    }

    private int toIndex(int line, int col) {
        try {
            return editor.getLineStartOffset(line-1) + col -1;
        } catch (BadLocationException e) {
            e.printStackTrace();
            return 0;
        }
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

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                try {
                    PFileEditor editor;
                    editor = new PFileEditor(new File("sys/proposition.p"));
                    editor.setSize(300,600);
                    editor.setVisible(true);
                } catch (HeadlessException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        });
        
    }

    public boolean hasUnsafedChanges() {
        // TODO Implement PFileEditor.hasUnsafedChanges
        return true;
    }


    public void changesSaved() {
        // TODO Implement PFileEditor.changesSaved
        
    }


    public char[] getContent() {
        // TODO Implement PFileEditor.getContent
        return null;
    }


    public File getFile() {
        // TODO Implement PFileEditor.getFile
        return null;
    }
}
