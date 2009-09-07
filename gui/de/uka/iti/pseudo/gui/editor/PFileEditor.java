package de.uka.iti.pseudo.gui.editor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.net.URL;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;

import de.uka.iti.pseudo.environment.EnvironmentMaker;
import de.uka.iti.pseudo.gui.BracketMatchingTextArea;
import de.uka.iti.pseudo.gui.Main;
import de.uka.iti.pseudo.gui.bar.BarAction;
import de.uka.iti.pseudo.gui.bar.BarManager;
import de.uka.iti.pseudo.gui.bar.CloseEditorAction;
import de.uka.iti.pseudo.parser.ASTElement;
import de.uka.iti.pseudo.parser.ASTLocatedElement;
import de.uka.iti.pseudo.parser.ASTVisitException;
import de.uka.iti.pseudo.parser.ParseException;
import de.uka.iti.pseudo.parser.Parser;
import de.uka.iti.pseudo.parser.Token;
import de.uka.iti.pseudo.parser.file.ASTFile;
import de.uka.iti.pseudo.util.settings.Settings;

// TODO in some future: syntax highlighting

public class PFileEditor extends JFrame implements ActionListener {
    
    private static final long serialVersionUID = 8116827588545997986L;
    private static final String ERROR_FILE_PROPERTY = "errorFile";
    private static final String ERROR_LINE_PROPERTY = "errorLine";
    private JTextArea editor;
    private File editedFile;
    private Object errorHighlighting;
    private JLabel statusLine;
    private BarManager barManager;
    private UpdateThread updateThread;
    private String errorFilename;
    private DocumentListener doclistener = new DocumentListener() {

        public void changedUpdate(DocumentEvent e) {
            updateThread.changed();
            setHasChanges(true);
        }

        public void insertUpdate(DocumentEvent e) {
            updateThread.changed();
            setHasChanges(true);
        }

        public void removeUpdate(DocumentEvent e) {
            updateThread.changed();
            setHasChanges(true);
        }
        
    };
    private int errorLine;
    private boolean hasChanged;

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
    
    public PFileEditor() throws IOException {
        this(null);
    }

    
    public PFileEditor(File file) throws IOException {
        init();
        loadFile(file);
    }


    private void init() throws IOException {
        setLayout(new BorderLayout());
        Container contentPane = getContentPane();
        {
            URL resource = BarManager.class.getResource("menu.properties");
            if(resource == null)
                throw new IOException("cannot find menu.properties");
            
            barManager = new BarManager(this, resource);
            barManager.putProperty(BarAction.PARENT_FRAME, this);
            barManager.putProperty(BarAction.EDITOR_FRAME, this);
            JToolBar toolbar = barManager.makeToolbar("editor.toolbar"); 
            contentPane.add(toolbar, BorderLayout.NORTH);
            setJMenuBar(barManager.makeMenubar("editor.menubar"));
        }
        {
            addWindowListener((WindowListener) barManager.makeAction(CloseEditorAction.class.getName()));    
            setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        }
        {
            editor = new BracketMatchingTextArea();
            editor.setLineWrap(false);
            editor.setFont(Settings.getInstance().getFont("pseudo.editor.font"));
            editor.setBorder(new LineNrBorder(Color.lightGray));
            installUndoManager(editor);
            // TODO make this configurable
            editor.getDocument().addDocumentListener(doclistener);
            try {
                errorHighlighting = editor.getHighlighter().addHighlight(0, 0, new CurlyHighlightPainter());
            } catch (BadLocationException e) {
               // cannot happen
                throw new Error(e);
            }
            JScrollPane scroll = new JScrollPane(editor);
            contentPane.add(scroll, BorderLayout.CENTER);
        }
        {
            statusLine = new JLabel();
            final JPopupMenu popup = barManager.makePopup("editor.popup");
            statusLine.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    if(SwingUtilities.isRightMouseButton(e)) {
                        Point p = e.getPoint();
                        popup.show(statusLine, p.x, p.y);
                    }
                }
            });
            contentPane.add(statusLine, BorderLayout.SOUTH);
        }
        {
            updateThread = new UpdateThread();
            updateThread.start();
        }
    }
    
    private void addErrorHighlighting() {
        Parser p = new Parser(new StringReader(editor.getText()));
        String filename = editedFile == null ? "<unnamed>" : editedFile.getName();
        try {
            ASTFile file = p.File();
            file.setFilename(filename);
            new EnvironmentMaker(p, file, filename);
            
            System.err.println("Syntax checked ... no more errors");
            
            setErrorFilename(null);
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
                if(ast.getFileName().equals(filename)) {
                    setErrorFilename(null);
                    markError(e, ast.getLocationToken());
                }
                else {
                    // error outside this thing here
                    setErrorFilename(ast.getFileName());
                    markError(e, null);
                }
            } else {
                markError(e, null);
            }
        }
        
    }

    private void setErrorFilename(String fileName) {
        firePropertyChange(ERROR_FILE_PROPERTY, errorFilename, fileName);
        errorFilename = fileName;
    }
    
    private void setErrorLine(int line) {
        firePropertyChange(ERROR_LINE_PROPERTY, errorLine, line);
        errorLine = line;
    }


    private void markError(final Exception exc, final Token token) {
        Runnable action = new Runnable() {
            public void run() {
                int from, to;
                try {
                    if(token == null) {
                        from = to = 0;
                        setErrorLine(0);
                    } else {
                        from = toIndex(token.beginLine, token.beginColumn);
                        to = toIndex(token.endLine, token.endColumn) + 1;
                        setErrorLine(token.beginLine);
                    }
                    editor.getHighlighter().changeHighlight(errorHighlighting, from, to);
                } catch (BadLocationException e) {
                    // really should not happen
                    throw new Error(e);
                }
                
                if(exc == null) {
                    statusLine.setForeground(Color.black);
                    statusLine.setText("syntax check succesful");
                    statusLine.setToolTipText(null);
                } else {
                    statusLine.setForeground(Color.red);
                    if(errorLine == 0)
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
    
    // from http://www.java2s.com/Code/Java/Swing-JFC/AddingUndoandRedotoaTextComponent.htm
    private static void installUndoManager(JTextComponent textcomp) {
        final UndoManager undo = new UndoManager();
        Document doc = textcomp.getDocument();

        doc.addUndoableEditListener(new UndoableEditListener() {
            public void undoableEditHappened(UndoableEditEvent evt) {
                undo.addEdit(evt.getEdit());
            }
        });
        
        textcomp.getActionMap().put("Undo",
            new AbstractAction("Undo") {
                public void actionPerformed(ActionEvent evt) {
                    try {
                        if (undo.canUndo()) {
                            undo.undo();
                        }
                    } catch (CannotUndoException e) {
                    }
                }
           });
        
        textcomp.getInputMap().put(KeyStroke.getKeyStroke("control Z"), "Undo");
        
        textcomp.getActionMap().put("Redo",
            new AbstractAction("Redo") {
                public void actionPerformed(ActionEvent evt) {
                    try {
                        if (undo.canRedo()) {
                            undo.redo();
                        }
                    } catch (CannotRedoException e) {
                    }
                }
            });
        
        textcomp.getInputMap().put(KeyStroke.getKeyStroke("control Y"), "Redo");
    }
    

//    public static void main(String[] args) {
//        SwingUtilities.invokeLater(new Runnable() {
//            public void run() {
//                try {
//                    PFileEditor editor;
//                    editor = new PFileEditor(new File("sys/proposition.p"));
//                    editor.setSize(600,600);
//                    editor.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//                    editor.setVisible(true);
//                } catch (HeadlessException e) {
//                    // TODO Auto-generated catch block
//                    e.printStackTrace();
//                } catch (IOException e) {
//                    // TODO Auto-generated catch block
//                    e.printStackTrace();
//                }
//            }
//        });
//        
//    }
    
    public void actionPerformed(ActionEvent evt) {
        Action action = editor.getActionMap().get(evt.getActionCommand());
        if(action != null)
            action.actionPerformed(evt);
    }

    public boolean hasUnsafedChanges() {
        return hasChanged;
    }

    public void setHasChanges(boolean b) {
        boolean old = hasChanged;
        hasChanged = b;
        if(hasChanged != old)
            updateTitle();
    }

    public String getContent() {
        return editor.getText();
    }
    
    public void loadFile(File file) throws IOException {
        if(file != null) {
            String content = readFileAsString(file);
            editor.setText(content);
        } else {
            editor.setText("");
        }
        this.editedFile = file;
        
        setHasChanges(false);
        updateTitle();
    }

    private void updateTitle() {
        StringBuilder sb = new StringBuilder();
        sb.append("Pseudo - Editor");
        if(editedFile != null)
            sb.append(" [" + editedFile + "]");
        else
            sb.append(" <untitled>");
        if(hasChanged)
            sb.append(" *");
        setTitle(sb.toString());
    }


    public File getFile() {
        return editedFile;
    }


    public String getErrorFilename() {
        return errorFilename;
    }

    public int getErrorLine() {
        return errorLine;
    }


    public JTextArea getEditPane() {
        return editor;
    }


    public void setFilename(File path) {
        editedFile = path;
        updateTitle();
    }
}
