package de.uka.iti.pseudo.gui.editor.help;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Vector;

import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.WindowConstants;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

@SuppressWarnings("serial")
public class ReferenceManualWindow extends JFrame {
    
    private RMTreeNode rootNode;
    private JEditorPane contentPane;
    
    public ReferenceManualWindow() throws ParserConfigurationException, SAXException, IOException {
        super("ivil - Reference Manual");
        xmlInit();
        guiInit();
    }
    
    private void guiInit() {
        JSplitPane splitPane = new JSplitPane();
        getContentPane().add(splitPane);
        {
            JTree categoryTree = new JTree(rootNode);
            categoryTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
            categoryTree.setRootVisible(false);
            categoryTree.addTreeSelectionListener(new TreeSelectionListener() {
                public void valueChanged(TreeSelectionEvent e) {
                    setSelection(e.getPath());
                }
            });
            JScrollPane scrollPane = new JScrollPane(categoryTree); 
            splitPane.setLeftComponent(scrollPane);
        }
        {
            contentPane = new JEditorPane();
            contentPane.setEditable(false);
            contentPane.setContentType("text/html");
            JScrollPane scrollPane = new JScrollPane(contentPane); 
            splitPane.setRightComponent(scrollPane);
        }
        setSize(1000, 400);
        splitPane.setDividerLocation(250);
    }

    protected void setSelection(TreePath path) {
        Object lastComp = path.getLastPathComponent();
        if (lastComp instanceof RMTreeNode) {
            RMTreeNode entryElem = (RMTreeNode) lastComp;
            contentPane.setText(entryElem.getContent());
            contentPane.setCaretPosition(0);
        } else {
            contentPane.setText("");
        }
    }

    private void xmlInit() throws ParserConfigurationException, SAXException, IOException {
        InputStream is = getClass().getResourceAsStream("referenceManual.xml");
        if(is == null) {
            throw new FileNotFoundException("referenceManual.xml not at its place");
        }
        
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(is);
        
        rootNode = new RMTreeNode("","");
        
        NodeList nodeLst = doc.getElementsByTagName("category");
        for (int s = 0; s < nodeLst.getLength(); s++) {
            Element catElement = (Element) nodeLst.item(s);
            String catName = catElement.getAttribute("name");
            RMTreeNode catNode = new RMTreeNode(catName, "");
            rootNode.add(catNode);
            NodeList entryList = catElement.getElementsByTagName("entry");
            for (int r = 0; r < entryList.getLength(); r++) {
                Element entryElement = (Element) entryList.item(r);
                String entryName = entryElement.getAttribute("name");
                String content = entryElement.getTextContent();
                catNode.add(new RMTreeNode(entryName, content));
            }
        }
        
        for (RMTreeNode node : rootNode.getChildren()) {
            Collections.sort(node.getChildren());
        }
        
    }

    private static class RMTreeNode extends DefaultMutableTreeNode implements Comparable<RMTreeNode> {

        private final String content;

        public RMTreeNode(String name, String content) {
            super(name);
            this.content = content;
        }

        public String getContent() {
            return content;
        }
        
        public String getName() {
            return (String)getUserObject();
        }
        
        public Vector<RMTreeNode> getChildren() {
            return children;
        }

        public int compareTo(RMTreeNode o) {
            return getName().compareToIgnoreCase(o.getName());
        }
    }
    
    public static void main(String[] args) throws ParserConfigurationException, SAXException, IOException {
        JFrame f = new ReferenceManualWindow();
        f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        f.setVisible(true);
    }
    
}
