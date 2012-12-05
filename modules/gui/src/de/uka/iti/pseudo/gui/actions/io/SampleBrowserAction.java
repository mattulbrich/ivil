/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2012 Karlsruhe Institute of Technology
 *
 * The system is protected by the GNU General Public License.
 * See LICENSE.TXT (distributed with this file) for details.
 */

package de.uka.iti.pseudo.gui.actions.io;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Collections;
import java.util.Properties;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.fife.ui.rsyntaxtextarea.RSyntaxDocument;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxScheme;
import org.fife.ui.rsyntaxtextarea.TokenMaker;

import de.uka.iti.pseudo.gui.Main;
import de.uka.iti.pseudo.gui.actions.BarAction;
import de.uka.iti.pseudo.gui.actions.BarManager.InitialisingAction;
import de.uka.iti.pseudo.gui.editor.IvilTokenMaker;
import de.uka.iti.pseudo.gui.editor.PFileEditor;
import de.uka.iti.pseudo.util.ExceptionDialog;
import de.uka.iti.pseudo.util.GUIUtil;
import de.uka.iti.pseudo.util.Log;
import de.uka.iti.pseudo.util.settings.Settings;

public class SampleBrowserAction extends BarAction implements InitialisingAction {

    public static final String SAMPLES_DIR = "/loadsamples/";

    private SampleBrowser sampleBrowser;

    @Override
    public void initialised() {
        URL existURL = getClass().getResource(SAMPLES_DIR + "samples");
        if(existURL == null) {
            setEnabled(false);
            Log.log(Log.WARNING, "The sample data has not been found, disabling sample browser");
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if(sampleBrowser == null) {
            sampleBrowser = new SampleBrowser(getParentFrame());
            sampleBrowser.setSize(700,500);
        }
        sampleBrowser.setVisible(true);
    }

}

class Sample extends Properties implements Comparable<Sample> {

    @Override
    public String toString() {
        return getName();
    }

    @Override
    public int compareTo(Sample o) {
        return getOrder().compareTo(o.getOrder());
    }

    public String getName() {
        return getProperty("name", "<no name>");
    }

    public String getOrder() {
        return getProperty("order", "ZZZ <no order>");
    }

    public String getDescription() {
        return getProperty("description",
                "<center><h1>Missing description</h1></center>");
    }

    public String getSource() {
        return getProperty("ivil", null);
    }

    public String getFile(int pos) {
        return getProperty("file" + pos, null);
    }

    public String getPath() {
        return getProperty("path", "");
    }

    public void setURLFrom(String file) {
        int index = file.lastIndexOf('/');
        if(index != -1) {
            put("path", file.substring(0, index+1));
        }
    }
}

class SampleBrowser extends JDialog {
    protected Sample selectedSample;
    private final Vector<Sample> allSamples = new Vector<Sample>();
    private JTabbedPane tabs;

    public SampleBrowser(Frame owner) {
        super(owner, "Sample Browser", true);
        initData();
        initGUI();
    }

    private void initData() {
        InputStream is = getClass().getResourceAsStream(SampleBrowserAction.SAMPLES_DIR + "samples");
        if(is == null) {
            Log.log(Log.ERROR, "Resource not found, the action should have been disabled earlier!");
            throw new RuntimeException("Resource not found, the action should have been disabled earlier!");
        }
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String line;
        try {
            while((line = br.readLine()) != null) {
                line = line.trim();
                if(line.startsWith("#") || line.length() == 0) {
                    continue;
                }

                Sample sd = new Sample();
                URL url = getClass().getResource(SampleBrowserAction.SAMPLES_DIR + line);
                sd.loadFromXML(url.openStream());
                sd.setURLFrom(line);
                allSamples.add(sd);
            }
        } catch (Exception e) {
            Log.stacktrace(Log.ERROR, e);
        }

        Collections.sort(allSamples);
    }

    private void initGUI() {
        Container cp = getContentPane();
        cp.setLayout(new BorderLayout());
        {
            JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
            {
                // list on the left
                final JList list = new JList(allSamples);
                list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
                list.setBorder(BorderFactory.createTitledBorder("Select sample:"));
                list.addListSelectionListener(new ListSelectionListener() {
                    @Override public void valueChanged(ListSelectionEvent e) {
                        if(!e.getValueIsAdjusting()) {
                            selectedSample = (Sample) list.getSelectedValue();
                            createTabs();
                        }
                    }
                });
                JScrollPane scroll = new JScrollPane(list);
                split.add(scroll, JSplitPane.LEFT);
            }
            {
                // right tabbed pane
                this.tabs = new JTabbedPane();
                split.add(tabs, JSplitPane.RIGHT);
            }
            cp.add(split, BorderLayout.CENTER);
        }
        {
            // button area
            JPanel buttons = new JPanel();
            cp.add(buttons, BorderLayout.SOUTH);
            {
                JButton button = new JButton("OK");
                button.addActionListener(new ActionListener() {
                    @Override public void actionPerformed(ActionEvent e) {
                        try {
                            URL resource = getClass().getResource(SampleBrowserAction.SAMPLES_DIR +
                                    selectedSample.getPath() + selectedSample.getSource());
                            Main.openProverFromURL(resource);
                        } catch (Exception ex) {
                            ExceptionDialog.showExceptionDialog(SampleBrowser.this, ex);
                        }
                        SampleBrowser.this.setVisible(false);
                    }
                });
                getRootPane().setDefaultButton(button);
                buttons.add(button);
            }
            {
                JButton button = new JButton("Cancel");
                button.addActionListener(new ActionListener() {
                    @Override public void actionPerformed(ActionEvent e) {
                        SampleBrowser.this.setVisible(false);
                    }
                });
                buttons.add(button);
            }
        }
    }

    protected void createTabs() {
        tabs.removeAll();
        if(selectedSample == null) {
            return;
        }


        String description = selectedSample.getDescription();
        {
            JEditorPane descPane = new JEditorPane();
            descPane.setContentType("text/html");
            descPane.setText(description);
            descPane.setCaretPosition(0);
            Component scroll = new JScrollPane(descPane);
            tabs.addTab("Description", scroll);
        }

        String source = selectedSample.getSource();
        if(source != null) {
            Component scroll = new JScrollPane(makeTab(selectedSample.getPath() + source));
            tabs.addTab(source, scroll);
        }

        int pos = 1;
        String file = selectedSample.getFile(pos);
        while(file != null) {
            Component scroll = new JScrollPane(makeTab(selectedSample.getPath() + file));
            tabs.addTab(file, scroll);
            pos ++;
            file = selectedSample.getFile(pos);
        }
    }

    private Component makeTab(String file) {
        try {
            InputStream is = getClass().getResourceAsStream(SampleBrowserAction.SAMPLES_DIR + file);
            if(is == null) {
                throw new NullPointerException();
            }

            String content = GUIUtil.drainStream(is);
            RSyntaxTextArea area = new RSyntaxTextArea();
            area.setText(content);
            area.setEditable(false);
            Font font = Settings.getInstance().getFont("pseudo.editor.font", area.getFont());
            area.setFont(font);

            if(file.endsWith(".p")) {
                ((RSyntaxDocument) area.getDocument()).
                setSyntaxStyle((TokenMaker) new IvilTokenMaker());
                SyntaxScheme schema = SyntaxScheme.load(font,
                        PFileEditor.class.getResourceAsStream("syntaxScheme.xml"));
                area.setSyntaxScheme(schema);
            }

            area.setCaretPosition(0);
            return area;

        } catch (Exception e) {
            Log.stacktrace(Log.ERROR, e);
            JPanel result = new JPanel();
            result.add(new JLabel("Resource '" + file + "' cannot be read."));
            return result;
        }
    }
}