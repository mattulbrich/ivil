/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2010 Universitaet Karlsruhe, Germany
 *    written by Mattias Ulbrich
 * 
 * The system is protected by the GNU General Public License. 
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.gui.editor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringBufferInputStream;
import java.net.URL;

import javax.swing.Action;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPopupMenu;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;

import nonnull.Nullable;

import org.fife.ui.rsyntaxtextarea.RSyntaxDocument;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextAreaEditorKit;
import org.fife.ui.rsyntaxtextarea.SyntaxScheme;
import org.fife.ui.rsyntaxtextarea.TokenMaker;
import org.fife.ui.rsyntaxtextarea.modes.PlainTextTokenMaker;
import org.fife.ui.rtextarea.RTextScrollPane;

import de.uka.iti.pseudo.environment.EnvironmentException;
import de.uka.iti.pseudo.environment.creation.EnvironmentCreationService;
import de.uka.iti.pseudo.environment.creation.PFileEnvironmentCreationService;
import de.uka.iti.pseudo.gui.actions.BarAction;
import de.uka.iti.pseudo.gui.actions.BarManager;
import de.uka.iti.pseudo.util.GUIUtil;
import de.uka.iti.pseudo.util.Log;
import de.uka.iti.pseudo.util.Util;
import de.uka.iti.pseudo.util.settings.Settings;

// TODO DOC
// TODO This editor has problems with "\r" characters!
@SuppressWarnings("deprecation")
public class PFileEditor extends JFrame implements ActionListener {
    
    private static final long serialVersionUID = 8116827588545997986L;
    private static final String ERROR_FILE_PROPERTY = "errorFile";
    private static final String ERROR_LINE_PROPERTY = "errorLine";
    public static final String SYNTAX_CHECKER_PROPERTY = "syntaxChecker";
    public static final String SYNTAX_CHECKING_PROPERTY = "syntaxCheck";
    private RSyntaxTextArea editor;
    private File editedFile;
    private Object errorHighlighting;
    private JLabel statusLine;
    private BarManager barManager;
    private UpdateThread updateThread;
    private String errorFilename;
    
    private ModificationListener modListener = 
        new ModificationListener(this);
    
    private DocumentListener doclistener = new DocumentListener() {

        public void changedUpdate(DocumentEvent e) {
            Log.enter(e);
            updateThread.changed();
            setHasUnsavedChanges(true);
        }

        public void insertUpdate(DocumentEvent e) {
            Log.enter(e);
            updateThread.changed();
            setHasUnsavedChanges(true);
        }

        public void removeUpdate(DocumentEvent e) {
            Log.enter(e);
            updateThread.changed();
            setHasUnsavedChanges(true);
        }
        
    };
    private int errorLine;
    private EnvironmentCreationService syntaxChecker;
    private boolean hasChanged;
    public boolean syntaxChecking = false;
    private boolean syntaxHighlighting = true;

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
                    Log.log(Log.WARNING, "Unexpected runtime exception");
                    Log.stacktrace(e);
                }
            }
        }
        
        public void changed() {
            if(syntaxChecking ) {
                synchronized(lock) {
                    interrupt();
                    lock.notify();
                }
            }
        }
    }
    
    public PFileEditor() throws IOException {
        this(null);
    }

    
    public PFileEditor(@Nullable File file) throws IOException {
        init();
        loadFile(file);
        
        // send property change to wordwrap to the editor.
        addPropertyChangeListener("editor.wordwrap", new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                editor.setLineWrap((Boolean) evt.getNewValue());
            }
        });
    }


    private void init() throws IOException {
        setLayout(new BorderLayout());
        Container contentPane = getContentPane();
        {
            URL resource = BarManager.class.getResource("menu.xml");
            if(resource == null)
                throw new IOException("cannot find menu.xml");
            
            barManager = new BarManager(this, resource);
            barManager.putProperty(BarAction.PARENT_FRAME, this);
            barManager.putProperty(BarAction.EDITOR_FRAME, this);
        }
        {
            editor = new RSyntaxTextArea();
            editor.setLineWrap(false);
            editor.setTextAntiAliasHint("VALUE_TEXT_ANTIALIAS_ON");
            editor.setBracketMatchingEnabled(true);
            editor.setPopupMenu(barManager.makePopup("editor.popup"));
            ((RSyntaxDocument) editor.getDocument()).setSyntaxStyle((TokenMaker) new IvilTokenMaker());
            Font font = Settings.getInstance().getFont("pseudo.editor.font", editor.getFont());
            editor.setFont(font);
            SyntaxScheme schema = SyntaxScheme.load(font, getClass().getResourceAsStream("syntaxScheme.xml"));
            editor.setSyntaxScheme(schema);
            editor.getInputMap().put(KeyStroke.getKeyStroke("control shift C"), RSyntaxTextAreaEditorKit.rstaToggleCommentAction);
//            installUndoManager(editor);
            // TODO make this configurable
            // TODO use the parser manager from RSyntax...
            editor.getDocument().addDocumentListener(doclistener);
            editor.getDocument().addDocumentListener(modListener);
            try {
                errorHighlighting = editor.getHighlighter().addHighlight(0, 0, new CurlyHighlightPainter());
            } catch (BadLocationException e) {
               // cannot happen
                throw new Error(e);
            }
            RTextScrollPane scroll = new RTextScrollPane(editor);
            contentPane.add(scroll, BorderLayout.CENTER);
            editor.requestFocus();
        }
        {
            statusLine = new JLabel();
            statusLine.setFont(statusLine.getFont().deriveFont(Font.PLAIN));
            final JPopupMenu popup = barManager.makePopup("editor.errorpopup");
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
        {
            JToolBar toolbar = barManager.makeToolbar("editor.toolbar"); 
            contentPane.add(toolbar, BorderLayout.NORTH);
            setJMenuBar(barManager.makeMenubar("editor.menubar"));
        }
        {
            addWindowListener((WindowListener) barManager.makeAction("general.close"));    
            setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
            addWindowListener(modListener);
        }
    }
    
    private void addErrorHighlighting() {
        URL url = null;
        try {
            // TODO is this conversion to a string too expensive?
            // TODO find sth better here
            String content = editor.getText();
            InputStream inputStream = new StringBufferInputStream(content);
            if(getFile() != null) {
                url = getFile().toURI().toURL();
            } else {
                url = new URL("none:unnamed.p");
            }
            
            // the checker may be null during initialisation. ...
            if(syntaxChecking) {
                syntaxChecker.createEnvironment(inputStream, url);
            }
            
            Log.log(Log.VERBOSE, "Syntax checked ... no errors");
            
            setErrorFilename(null);
            markError(null, true);
        } catch (EnvironmentException e) {
            markError(e, url.toString().equals(e.getResource()));
        } catch (Exception e) {
            Log.stacktrace(Log.VERBOSE, e);
            markError(e, false);
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


    private void markError(final Exception exc, final boolean local) {
        assert !local || exc == null || exc instanceof EnvironmentException;
        Log.enter(Log.VERBOSE, exc);
        
        Runnable action = new Runnable() {
            public void run() {
                int from, to;
                try {
                    if(exc == null || !local) {
                        from = to = 0;
                        setErrorLine(0);
                    } else {
                        EnvironmentException envEx = (EnvironmentException) exc; 
                        if(envEx.hasErrorInformation()) { 
                            from = toIndex(envEx.getBeginLine(), envEx.getBeginColumn());
                            to = toIndex(envEx.getEndLine(), envEx.getEndColumn()) + 1;
                            setErrorLine(envEx.getBeginLine());
                        } else {
                            from = to = 0;
                            setErrorLine(0);
                        }
                    }
                    editor.getHighlighter().changeHighlight(errorHighlighting, from, to);
                } catch (BadLocationException e) {
                    // really should not happen
                    throw new Error(e);
                }
                
                if(exc == null) {
                    statusLine.setForeground(Color.black);
                    String text = syntaxChecking ?
                            "syntax check successful" :
                            "syntax check disabled";
                    statusLine.setText(text);
                    statusLine.setToolTipText(null);
                } else {
                    statusLine.setForeground(Color.red);
                    
                    String message = exc.getMessage();
                    if(message == null)
                        message = "";
                    
                    if(errorLine == 0)
                        statusLine.setText("Error outside this file while parsing: " + shortMessage(message));
                    else
                        statusLine.setText("Error in line " + errorLine + ": " + shortMessage(message));
                    
                    statusLine.setToolTipText("<html><pre>" + GUIUtil.htmlentities(message).replace("\n", "<br/>") + "</pre>");
                }

                getContentPane().invalidate();
                getContentPane().doLayout();
                repaint();
            }

            private String shortMessage(String message) {
                
                int index = message.indexOf('\n');
                if(index != -1)
                    return message.substring(0, index);
                else
                    return message;
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
    
    // from http://www.java2s.com/Code/Java/Swing-JFC/AddingUndoandRedotoaTextComponent.htm
   
//    @SuppressWarnings("serial")
//    private static void installUndoManager(JTextComponent textcomp) {
//        final UndoManager undo = new UndoManager();
//        Document doc = textcomp.getDocument();
//
//        doc.addUndoableEditListener(new UndoableEditListener() {
//            public void undoableEditHappened(UndoableEditEvent evt) {
//                undo.addEdit(evt.getEdit());
//            }
//        });
//        
//        textcomp.getActionMap().put("Undo",
//            new AbstractAction("Undo") {
//                public void actionPerformed(ActionEvent evt) {
//                    try {
//                        if (undo.canUndo()) {
//                            undo.undo();
//                        }
//                    } catch (CannotUndoException e) {
//                    }
//                }
//           });
//        
//        textcomp.getInputMap().put(KeyStroke.getKeyStroke("control Z"), "Undo");
//        
//        textcomp.getActionMap().put("Redo",
//            new AbstractAction("Redo") {
//                public void actionPerformed(ActionEvent evt) {
//                    try {
//                        if (undo.canRedo()) {
//                            undo.redo();
//                        }
//                    } catch (CannotRedoException e) {
//                    }
//                }
//            });
//        
//        textcomp.getInputMap().put(KeyStroke.getKeyStroke("control Y"), "Redo");
//    }
    

    public static void main(String[] args) {
        Util.registerURLHandlers();
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                try {
                    PFileEditor editor;
                    editor = new PFileEditor(); //new File("sys/fol.p"));
                    editor.setSize(600,600);
                    editor.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                    editor.setVisible(true);
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        });
        
    }
    
    public void actionPerformed(ActionEvent evt) {
        Action action = editor.getActionMap().get(evt.getActionCommand());
        if(action != null)
            action.actionPerformed(evt);
    }

    public boolean hasUnsavedChanges() {
        return hasChanged;
    }

    public void setHasUnsavedChanges(boolean b) {
        boolean old = hasChanged;
        hasChanged = b;
        if(hasChanged != old)
            updateTitle();
    }

    public String getContent() {
        return editor.getText();
    }
    
    public void loadFile(File file) throws IOException {
        EnvironmentCreationService checker; 
        
        Log.enter(file);
        assert EventQueue.isDispatchThread();
            
        if(file != null) {
            String content = Util.readFileAsString(file);
            editor.setText(content);
            editor.setCaretPosition(0);
            
            String path = file.getPath();
            int dotPos = path.lastIndexOf('.');
            String ext = path.substring(dotPos + 1);

            checker =
                EnvironmentCreationService.getServiceByExtension(ext);
            if(checker == null) {
                // take this as default.
                checker = new PFileEnvironmentCreationService();
            } else {
                setProperty(SYNTAX_CHECKING_PROPERTY, true);
            }
            
            modListener.setEditedFile(file);
            
        } else {
            editor.setText("");
            checker = new PFileEnvironmentCreationService();
            setProperty(SYNTAX_CHECKING_PROPERTY, true);
            modListener.setEditedFile(null);
        }
        
        this.editedFile = file;
        
        editor.discardAllEdits();
        setProperty(SYNTAX_CHECKER_PROPERTY, checker);
        setHasUnsavedChanges(false);
        updateTitle();
        
        Log.leave();
    }

    private void updateTitle() {
        StringBuilder sb = new StringBuilder();
        sb.append("ivil - Editor");
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
        modListener.setEditedFile(path);
        editedFile = path;
        updateTitle();
    }


    public void setProperty(String property, Object newValue) {
        Object old = getProperty(property);
        
        if("lineWrap".equals(property)) {
            editor.setLineWrap((Boolean)newValue);
        }
        
        if(SYNTAX_CHECKING_PROPERTY.equals(property)) {
            syntaxChecking = (Boolean)newValue;
            if(syntaxChecking) {
                updateThread.changed();
            } else {
                markError(null, true);
            }
        }
        
        if(SYNTAX_CHECKER_PROPERTY.equals(property)) {
            syntaxChecker = (EnvironmentCreationService) newValue;
            updateThread.changed();
            System.out.println(syntaxChecker);
        }
        
        if("syntaxHighlight".equals(property)) {
            RSyntaxDocument rSyntaxDocument = (RSyntaxDocument) editor.getDocument();
            syntaxHighlighting = (Boolean)newValue;
            if(syntaxHighlighting) { 
                rSyntaxDocument.setSyntaxStyle(new IvilTokenMaker());
            } else {
                rSyntaxDocument.setSyntaxStyle(new PlainTextTokenMaker());
            }
        }
        
        firePropertyChange(property, old, newValue);
    }


    public Object getProperty(String property) {
        if("lineWrap".equals(property)) {
            return editor.getLineWrap();
        }
        
        if(SYNTAX_CHECKING_PROPERTY.equals(property)) {
            return syntaxChecking; 
        }
        
        if(SYNTAX_CHECKER_PROPERTY.equals(property)) {
            return syntaxChecking; 
        }
        
        if("syntaxHighlight".equals(property)) {
            return syntaxHighlighting ;
        }
        
        return null;
    }
}