package de.uka.iti.pseudo.gui.bar;

import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JSeparator;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;

import nonnull.NonNull;
import de.uka.iti.pseudo.util.Util;

// TODO Documentation needed DOC!
// TODO cache all classes -> objects and create only one instance per class. ...
public class BarManager {
    
    public static interface InitialisingAction extends Action {
        public void initialised();
    }

    private static final String DEFAULT_MENUBAR_PROPERTY = "menubar";
    private static final String DEFAULT_TOOLBAR_PROPERTY = "toolbar";
    
    private ActionListener actionListener;
    
    private Map<String, Object> defaultActionProperties = new HashMap<String, Object>();

    private Properties properties;
    
    private URL resource;
    
    private Map<Class<?>, Action> actionCache = 
        new HashMap<Class<?>, Action>();

    private String packagePrefix;

    private boolean toolbarOnlyIcons;
    
    public BarManager(ActionListener actionListener, URL resource) {
        super();
        this.actionListener = actionListener;
        this.resource = resource;
    }
    
    private void prepareProperties() throws IOException {
        if(properties == null) {
            properties = new Properties();
            InputStream stream = resource.openStream();
            properties.load(stream);
            
            properties.put("SEPARATOR", "SEPARATOR");
            
            packagePrefix = properties.getProperty("package");
            if(packagePrefix == null)
                packagePrefix = "";
            else
                packagePrefix = packagePrefix + ".";
            
            toolbarOnlyIcons = "true".equals(properties.getProperty("toolbar.onlyIcons"));
        }
    }
    
    public JToolBar makeToolbar() throws IOException {
        return makeToolbar(DEFAULT_TOOLBAR_PROPERTY);
    }
    
    public JToolBar makeToolbar(String propertyName) throws IOException {
        
        prepareProperties();
        
        JToolBar result = new JToolBar();
        
        String[] elements = getPropertyOrFail(propertyName).split(" +");
        
        for (String element : elements) {
            result.add(makeToolbarItem(element));
        }
        
        return result;
    }

    private JComponent makeToolbarItem(String element)
            throws IOException {
        
        String value = getPropertyOrFail(element);
        String args[] = value.split(" ", 3);
        JComponent result;
        
        try {
            if(args[0].equals("SEPARATOR")) {
                if(args.length == 3) {
                    int h = Integer.parseInt(args[1]);
                    int w = Integer.parseInt(args[2]);
                    result = new JToolBar.Separator(new Dimension(h, w));
                } else {
                    result = new JToolBar.Separator();
                }
                
            } else if(args[0].equals("ACTION")) {
                String className = args[1]; 
                Action action = makeAction(className);
                JButton button = new JButton(action);
                
                if(actionListener != null)
                    button.addActionListener(actionListener);

                if(toolbarOnlyIcons && button.getIcon() != null)
                    button.setText(null);
                
                result = button;
                
            } else if(args[0].equals("TOGGLE_ACTION")) {
                String className = args[1]; 
                Action action = makeAction(className);
                JToggleButton button = new JToggleButton(action);
                
                if(actionListener != null)
                    button.addActionListener(actionListener);

                if(toolbarOnlyIcons && button.getIcon() != null)
                    button.setText(null);
                
                result = button;

            } else if(args[0].equals("COMMAND")) {
                String command = args[1];
                JButton button = new JButton();
                button.setActionCommand(command);
                
                String val =  properties.getProperty(element + ".text");
                if(val != null && !toolbarOnlyIcons)
                    button.setText(val);
                
                val = properties.getProperty(element + ".icon");
                if(val != null) {
                    String location = packagePrefix.replace('.', '/') + val;
                    button.setIcon(makeIcon(ClassLoader.getSystemResource(location)));
                }
                
                val = properties.getProperty(element + ".tooltip");
                if(val != null)
                    button.setToolTipText(val);
                
                if(actionListener != null)
                    button.addActionListener(actionListener);
                
                result = button;
                
            } else if(args[0].equals("TODO")){
                JButton button = new JButton(value.substring(5));
                button.setEnabled(false);
                result = button;
                
            } else {
                throw new IOException("invalid toolbar description: " + element + " = " + value);
            }
        } catch (RuntimeException e) {
            throw new IOException("Illegal format in " + element + " = " + value);
        }
        
        return result;
    }

    public JMenuBar makeMenubar() throws IOException {
        return makeMenubar(DEFAULT_MENUBAR_PROPERTY);
    }
    
    public JMenuBar makeMenubar(String propertyName) throws IOException {
        prepareProperties();
        
        String[] menus = getPropertyOrFail(propertyName).split(" +");
        
        JMenuBar result = new JMenuBar();
        
        for (String element : menus) {
            String value = properties.getProperty(element);
            
            if(value == null)
                throw new IOException("cannot create menubar, missing property '" + element + "'");
            
            result.add(makeMenu(element));
        }
        
        return result;
    }
    
    public JPopupMenu makePopup(String propertyName) throws IOException {
        
        prepareProperties();
        
        String items[] = getPropertyOrFail(propertyName).split(" +");
        JPopupMenu result = new JPopupMenu();
        
        for (String item : items) {
            result.add(makeMenuItem(item));
        }
        
        return result;
    }

    private JMenu makeMenu(String property) throws IOException {
        
        String items[] = getPropertyOrFail(property).split(" +");
        JMenu result = new JMenu(getPropertyOrFail(property + ".text"));
        
        for (String item : items) {
            // submenu must be ignored - it may appear however
            if(item.equals("SUBMENU"))
                continue;
            result.add(makeMenuItem(item));
        }
        
        return result;
    }
    
    private JComponent makeMenuItem(String property) throws IOException {
        
        JComponent result;
        
        String value = getPropertyOrFail(property);

        String args[] = value.split(" ", 3);

        try {
            if(args[0].equals("SEPARATOR")) {
                result = new JSeparator();

            } else if(args[0].equals("SUBMENU")) {
                result = makeMenu(property);

            } else if(args[0].equals("ACTION")) {
                String className = args[1]; 
                Action action = makeAction(className);
                JMenuItem menuItem = new JMenuItem(action);
                
                if(actionListener != null)
                    menuItem.addActionListener(actionListener);

                result = menuItem;

            } else if(args[0].equals("RADIO_ACTION")) {
                String className = args[1];
                Action action = makeAction(className);
                JRadioButtonMenuItem menuItem = new JRadioButtonMenuItem(action);
                if(actionListener != null)
                    menuItem.addActionListener(actionListener);

                result = menuItem;

            } else if(args[0].equals("TOGGLE_ACTION")) {
                String className = args[1];
                Action action = makeAction(className);
                JCheckBoxMenuItem menuItem = new JCheckBoxMenuItem(action);
                if(actionListener != null)
                    menuItem.addActionListener(actionListener);

                result = menuItem;

            } else if(args[0].equals("COMMAND")) {
                String command = args[1];
                JMenuItem menuItem = new JMenuItem();
                menuItem.setActionCommand(command);
                
                String val = properties.getProperty(property + ".text");
                if(val != null)
                    menuItem.setText(val);
                
                val = properties.getProperty(property + ".icon");
                if(val != null) {
                    String location = packagePrefix.replace('.', '/') + val;
                    menuItem.setIcon(makeIcon(ClassLoader.getSystemResource(location)));
                }
                
                val = properties.getProperty(property + ".tooltip");
                if(val != null)
                    menuItem.setToolTipText(val);

                if(actionListener != null)
                    menuItem.addActionListener(actionListener);

                result = menuItem;

            } else if(args[0].equals("TODO")){
                JMenuItem menuItem = new JMenuItem(value.substring(5));
                menuItem.setEnabled(false);
                result = menuItem;

            } else {
                throw new IOException("invalid menu description: " + property + " = " + value);
            }
        } catch (RuntimeException e) {
            throw new IOException("Illegal format in " + property + " = " + value, e);
        }

        return result;
    }

    


    private String getPropertyOrFail(String property) throws IOException {
        String value = properties.getProperty(property);
        if(value == null) {
            throw new IOException("BarManager: Missing property '" + property +"' in " + resource);
        }
        return value;
    }


    public Action makeAction(String className) throws IOException {
        
        if(!className.contains("."))
            className = packagePrefix + className;
        
        try {
            Class<?> clss = Class.forName(className);
            
            Action cached = actionCache.get(clss);
            if(cached != null) {
                return cached;
            }
            
            Action action = (Action) clss.newInstance();
            actionCache.put(clss, action);
            
            for (Entry<String, Object> entry : defaultActionProperties.entrySet()) {
                action.putValue(entry.getKey(), entry.getValue());
            }
            
            if (action instanceof InitialisingAction) {
                InitialisingAction initAction = (InitialisingAction) action;
                initAction.initialised();
            }

            return action;
        } catch (Exception e) {
            throw new IOException("cannot create Action instance of " + className, e);
        }
    }
    
    public static Icon makeIcon(@NonNull URL resource) {
        try {
            if(resource != null)
                return new ImageIcon(resource);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        System.err.println("Cannot load icon " + resource + ", continuing anyway ...");
        return Util.UNKNOWN_ICON;
    }
    
    public void clearCache() {
        actionCache.clear();
    }

    public void putProperty(String property, Object value) {
        defaultActionProperties.put(property, value);
    }
}
