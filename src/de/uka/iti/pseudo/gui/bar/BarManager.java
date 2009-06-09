package de.uka.iti.pseudo.gui.bar;

import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JSeparator;
import javax.swing.JToolBar;

import de.uka.iti.pseudo.gui.bar.StateListener.StateListeningAction;
import de.uka.iti.pseudo.util.Util;

// TODO Documentation needed
// TODO cache all classes -> objects and create only one instance per class. ...
public class BarManager {
    
    static final String CENTER = "barmanager.center";

    private List<StateListener> listeners = 
        new ArrayList<StateListener>();
    
    private Object centerObject;
    private ActionListener actionListener;

    private Map<Class<?>, StateListeningAction> actionCache = 
        new HashMap<Class<?>, StateListeningAction>();
    
    public BarManager(Object centerObject, ActionListener actionListener) {
        super();
        this.centerObject = centerObject;
        this.actionListener = actionListener;
    }
    
    public JToolBar makeToolbar(URL resource) throws IOException {
        
        Properties properties = new Properties();
        
        InputStream stream = resource.openStream();
        
        properties.load(stream);
        
        String packagePrefix = properties.getProperty("package");
        if(packagePrefix == null)
            packagePrefix = "";
        else
            packagePrefix = packagePrefix + ".";
        
        JToolBar result = new JToolBar();
        
        int buttonNo = 1;
        while(true) {
            String name = "toolbar" + buttonNo;
            String value = properties.getProperty(name);
            
            if(value == null)
                break;
            
            String args[] = value.split(" ", 3);
            try {
                if(args[0].equals("SEPARATOR")) {
                    if(args.length == 3) {
                        int h = Integer.parseInt(args[1]);
                        int w = Integer.parseInt(args[1]);
                        result.addSeparator(new Dimension(h, w));
                    } else {
                        result.addSeparator();
                    }
                    
                } else if(args[0].equals("ACTION")) {
                    String className = packagePrefix + args[1];
                    StateListeningAction action = makeAction(className);
                    JButton menuItem = new JButton(action);
                    if(actionListener != null)
                        menuItem.addActionListener(actionListener);
                    
                    if(args.length == 3) {
                        if(args[2].equals("ICON")) {
                            menuItem.setText("");
                        } else if(args[2].equals("TEXT")) {
                            menuItem.setIcon(null);
                        }
                    }
                    
                    result.add(menuItem);
                    
                } else if(args[0].equals("COMMAND")) {
                    String command = args[1];
                    String menuTitle = args[2];
                    JButton menuItem = new JButton(menuTitle);
                    menuItem.setActionCommand(command);
                    
                    if(actionListener != null)
                        menuItem.addActionListener(actionListener);
                    
                    result.add(menuItem);
                } else if(args[0].equals("DELOCATED_ACTION ")) {
                    String className = args[1];
                    StateListeningAction action = makeAction(className);
                    JButton menuItem = new JButton(action);
                    if(actionListener != null)
                        menuItem.addActionListener(actionListener);
                    
                    if(args.length == 3) {
                        if(args[2].equals("ICON")) {
                            menuItem.setText("");
                        } else if(args[2].equals("TEXT")) {
                            menuItem.setIcon(null);
                        }
                    }
                    
                    result.add(menuItem);
                    
                } else if(args[0].equals("TODO")){
                    JButton menuItem = new JButton(args[1]);
                    menuItem.setEnabled(false);
                    result.add(menuItem);
                    
                } else {
                    throw new IOException("invalid menu description: " + name + " = " + value);
                }
            } catch (RuntimeException e) {
                throw new IOException("Illegal format in " + name + " = " + value);
            }
            
            buttonNo ++;
        }
        
        return result;
    }

    public JMenuBar makeMenubar(URL resource) throws IOException {
        
        Properties properties = new Properties();
        
        InputStream stream = resource.openStream();
        
        properties.load(stream);
        
        String packagePrefix = properties.getProperty("package");
        if(packagePrefix == null)
            packagePrefix = "";
        else
            packagePrefix = packagePrefix + ".";
        
        JMenuBar result = new JMenuBar();
        int menuNo = 1;
        while(true) {
            String name = "menu" + menuNo;
            String value = properties.getProperty(name);
            
            if(value == null)
                break;
            
            result.add(makeMenu(value, name + ".", packagePrefix, properties));
            
            menuNo ++;
        }
        
        return result;
    }

    private JMenu makeMenu(String title, String baseName, String packagePrefix, Properties properties) throws IOException {
        JMenu result = new JMenu(title);

        int menuNo = 1;
        while(true) {
            String name = baseName + menuNo;
            String value = properties.getProperty(name);

            if(value == null)
                break;
            
            String args[] = value.split(" ", 3);

            try {
                if(args[0].equals("SEPARATOR")) {
                    result.add(new JSeparator());
                    
                } else if(args[0].equals("SUBMENU")) {
                    String subName = value.substring(8);
                    result.add(makeMenu(subName, name + ".", packagePrefix, properties));
                    
                } else if(args[0].equals("ACTION")) {
                    String className = packagePrefix + args[1];
                    StateListeningAction action = makeAction(className);
                    JMenuItem menuItem = new JMenuItem(action);
                    if(actionListener != null)
                        menuItem.addActionListener(actionListener);
                    
                    result.add(menuItem);
                    
                } else if(args[0].equals("RADIO_ACTION")) {
                    String className = packagePrefix + args[1];
                    StateListeningAction action = makeAction(className);
                    JRadioButtonMenuItem menuItem = new JRadioButtonMenuItem(action);
                    if(actionListener != null)
                        menuItem.addActionListener(actionListener);
                    
                    result.add(menuItem);
                 
                } else if(args[0].equals("CHECKBOX_ACTION")) {
                    String className = packagePrefix + args[1];
                    StateListeningAction action = makeAction(className);
                    JCheckBoxMenuItem menuItem = new JCheckBoxMenuItem(action);
                    if(actionListener != null)
                        menuItem.addActionListener(actionListener);
                    
                    result.add(menuItem);
                    
                } else if(args[0].equals("COMMAND")) {
                    String command = args[1];
                    String menuTitle = args[2];
                    JMenuItem menuItem = new JMenuItem(menuTitle);
                    menuItem.setActionCommand(command);
                    
                    if(actionListener != null)
                        menuItem.addActionListener(actionListener);
                    
                    result.add(menuItem);
                } else if(args[0].equals("DELOCATED_ACTION ")) {
                    String className = args[1];
                    StateListeningAction action = makeAction(className);
                    JMenuItem menuItem = new JMenuItem(action);
                    if(actionListener != null)
                        menuItem.addActionListener(actionListener);
                    
                    result.add(menuItem);
                    
                } else if(args[0].equals("TODO")){
                    JMenuItem menuItem = new JMenuItem(value.substring(5));
                    menuItem.setEnabled(false);
                    result.add(menuItem);
                    
                } else {
                    throw new IOException("invalid menu description: " + name + " = " + value);
                }
            } catch (IndexOutOfBoundsException e) {
                throw new IOException("Illegal format in " + name + " = " + value);
            }
            
            menuNo ++;
        }
        
        return result;
    }


    public StateListeningAction makeAction(String className) throws IOException {
        try {
            Class<?> clss = Class.forName(className);
            
            StateListeningAction cached = actionCache.get(clss);
            if(cached != null) {
                return cached;
            }
            
            StateListeningAction action = (StateListeningAction) clss.newInstance();
            actionCache.put(clss, action);
            addStateListener(action);
            
            if(CENTER != null)
                action.putValue(CENTER, centerObject);
            return action;
        } catch (Exception e) {
            throw new IOException("cannot create Action instance of " + className, e);
        }
    }
    
    public static Icon makeIcon(URL resource) {
        try {
            return new ImageIcon(resource);
        } catch (Exception e) {
            System.err.println("Cannot load icon " + resource + ", continuing anyway ...");
            e.printStackTrace();
            return Util.UNKNOWN_ICON;
        }
    }
    
    public void clearCache() {
        actionCache.clear();
    }

    /**
     * add a state listener.
     * 
     * If the same object has already been registered as state listener, nothing
     * is done.
     * 
     * @param listener
     *            listener to register for future events
     */
    public void addStateListener(StateListener listener) {
        if(!listeners.contains(listener))
            listeners.add(listener);
    }
    
    public void removeStateListener(StateListener listener) {
        listeners.remove(listener);
    }
    
    public void fireStateChange(StateListener.StateChangeEvent e) {
        for (StateListener listener : listeners) {
            listener.stateChanged(e);
        }
    }
}
