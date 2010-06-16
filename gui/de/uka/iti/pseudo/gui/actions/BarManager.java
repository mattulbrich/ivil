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
package de.uka.iti.pseudo.gui.actions;

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
import de.uka.iti.pseudo.util.GUIUtil;

/**
 * The Class BarManager is a pretty generic framework to allow menu bars and
 * tool bars to be configured via a <code>.properties</code> file declaring
 * all actions whose classes will then be loaded.
 * 
 * <h2>The actions</h2>
 * 
 * Actions can be both registered for toolbars and menu bars. There is at most
 * one object for a certain class within one bar manager. They are reused upon a
 * second registration.
 * 
 * <p>
 * When an action is created, the created object is furnished with the default
 * properties first. If it also implements the interface
 * {@link InitialisingAction}, the initialised method is invoked, to give the
 * the action the opportunity to set itself up using the provided properties.
 * 
 * <h2>The configuration</h2>
 * 
 * Configuration has to be provided in a <code>.properties</code> file with
 * the following special keys: <table border=1 cellspacing=5>
 * <tr>
 * <td><code>menubar</code></td>
 * <td>Key for the menubar</td>
 * <td>Listing all keys for top level menus in the bar in proper order</td>
 * </tr>
 * <tr>
 * <td><code>toolbar</code></td>
 * <td>Key for the toolbar</td>
 * <td>Listing all keys for all buttons the bar in proper order. You may use
 * <code>SEPARATOR</code> to create empty space between buttons.</td>
 * </tr>
 * <tr>
 * <td>menu (as appearing in menubar)</td>
 * <td>Key for a menu</td>
 * <td>Listing all keys for menu items in the menu in proper order. You may use
 * <code>SEPARATOR</code> to create a separating line between entries. An
 * entry for the subkey <code>.text</code> defines the title of the menu</td>
 * </tr>
 * <tr>
 * <td>item (as appearing in a menu/toolbar)</td>
 * <td>Definition of an action</td>
 * <td>Keyword and further information, see below</td>
 * </tr>
 * </table>
 * 
 * <h3>Action Types</h3>
 * 
 * <table border=1 cellspacing=5>
 * <tr>
 * <th>Keyword</th>
 * <th>Menu/Toolbar</th>
 * <th>Parameters</th>
 * <th>Expl.</th>
 * </tr>
 * <tr>
 * <td><code>ACTION</code></td>
 * <td>M/T</td>
 * <td>class name</td>
 * <td>create an action object of the given class name and add a button or menu
 * item for it</td>
 * </tr>
 * <tr>
 * <td><code>TOGGLE_ACTION</code></td>
 * <td>M/T</td>
 * <td>class name</td>
 * <td>create an action object of the given class name and add a toggle button
 * or checkable menu item for it</td>
 * </tr>
 * <tr>
 * <td><code>COMMAND</code></td>
 * <td>M/T</td>
 * <td>command string</td>
 * <td>create a button or menu item with the given command set as command. You
 * can use subkeys <code>.text</code>, <code>.icon</code> and
 * <code>.tooltip</code> to set the text for the button/menu or the resource
 * for the image.</td>
 * </tr>
 * <tr>
 * <td><code>TODO</code></td>
 * <td>M/T</td>
 * <td>Name</td>
 * <td>create a new deactivated button or menu item with the given title.
 * Placeholder for future functionality</td>
 * </tr>
 * <tr>
 * <td><code>SUBMENU</code></td>
 * <td>M/-</td>
 * <td>key</td>
 * <td>create a submenu and create it using the specified key</td>
 * </tr>
 * </table>
 * 
 * To faciliate the specification of classnames, you can provide a key
 * <code>package</code> into which all class names are assumed if they do not
 * contain a "." (dot).
 * 
 * <h3>Example configuration</h3>
 * Here a small but typical example:
 * 
 * <pre>
 * package = org.example.barmanager
 * menubar = fileMenu editorMenu helpMenu
 * toolbar = file.open SEPARATOR editor.paste
 * 
 * fileMenu = file.open file.close SEPARATOR exit
 * editorMenu = editor.copy editor.cut editor.paste
 * helpMenu = help
 * 
 * file.open = ACTION FileOpenAction
 * # ...
 * 
 * editor.copy = COMMAND copy-to-clipboard
 * editor.copy.text = Copy
 * editor.copy.tooltip = Copy to clipboard
 * editor.copy.icon = img/copy.png
 * # ...
 * 
 * help = help.index help.search
 * help.index = TODO Index
 * # ...
 * </pre>
 * 
 * <h2>Example usage</h2>
 * 
 * To create a bar manager do something like:
 * 
 * <pre>
 * BarManager barManager = new BarManager(null, resource);
 * barManager.putProperty(PARENT_FRAME, jframe);
 * barManager.putProperty(SOME_PROP, somevalue);
 * jframe.setJMenuBar(barManager.makeMenubar());
 * </pre>
 * 
 */
public class BarManager {
    
    /**
     * An Action implementing this interface will have its initialised method
     * invoked if created by a bar manager. This invocation happens after all
     * properties have been set in the action, so that the information can be
     * used to set the action up.
     */
    public static interface InitialisingAction extends Action {
        
        /**
         * Implementing classes can provide code which sets up the action.
         * This method is invoked after all relevant properties have been set.
         */
        public void initialised();
    }

    /**
     * The property name for the default menubar.
     */
    private static final String DEFAULT_MENUBAR_PROPERTY = "menubar";
    
    /**
     * The property name for the default toolbar.
     */
    private static final String DEFAULT_TOOLBAR_PROPERTY = "toolbar";
    
    /**
     * The general action listener which is set on all created buttons, menu
     * items, etc.
     */
    private ActionListener actionListener;
    
    /**
     * The map of properties provided to the freshly created actions.
     */
    private Map<String, Object> defaultActionProperties = new HashMap<String, Object>();

    /**
     * The properties with which the bar manager is configured
     */
    private Properties properties;
    
    /**
     * The resource from where the properties are read
     */
    private URL resource;
    
    /**
     * The action cache stores all created objects so that objected created once
     * will be reused.
     */
    private Map<Class<?>, Action> actionCache = 
        new HashMap<Class<?>, Action>();

    /**
     * The package prefix if one is provided in the properties, null only in the
     * beginning
     */
    private String packagePrefix;

    /**
     * A flag to indicate whether toolbar buttons should have text or images
     * only.
     */
    private boolean toolbarOnlyIcons;
    
    /**
     * Instantiates a new bar manager. The configuration is to be read from a
     * URL.
     * 
     * @param actionListener
     *            the action listener
     * @param resource
     *            the resource
     */
    public BarManager(ActionListener actionListener, URL resource) {
        this.actionListener = actionListener;
        this.resource = resource;
    }
    
    /*
     * Prepare properties: load from URL, read package info, make flags.
     */
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
    
    /**
     * Make the default toolbar from the property resources. It uses the key
     * {@value #DEFAULT_TOOLBAR_PROPERTY}.
     * 
     * @return a freshly created toolbar object
     * 
     * @throws IOException
     *             on read errors, or configuration error
     */
    public JToolBar makeToolbar() throws IOException {
        return makeToolbar(DEFAULT_TOOLBAR_PROPERTY);
    }
    
    /**
     * Make a toolbar from the property resources. It uses the key the
     * specified key as starting point
     * 
     * @param propertyName
     *            the key in the properties to be used as toolbar definition.
     * 
     * @return a freshly created toolbar object
     * 
     * @throws IOException
     *             on read errors, or configuration error
     */
    public JToolBar makeToolbar(@NonNull String propertyName) throws IOException {
        
        prepareProperties();
        
        JToolBar result = new JToolBar();
        
        String[] elements = getPropertyOrFail(propertyName).split(" +");
        
        for (String element : elements) {
            result.add(makeToolbarItem(element));
        }
        
        return result;
    }

    /*
     * Make a toolbar item from a given key.
     * 
     * SEPARATOR, ACTION, TOGGLE_ACION, TODO, COMMAND
     */
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
                    String location = "/" + packagePrefix.replace('.', '/') + val;
                    URL systemResource = BarManager.class.getResource(location);
                    if(systemResource == null)
                        System.err.println("Warning: Unknown icon resource " + location);
                    button.setIcon(GUIUtil.makeIcon(systemResource));
                    System.err.println(packagePrefix + "|" +  val + "|" + location);
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

    /**
     * Make the default menubar from the property resources. It uses the key
     * {@value #DEFAULT_MENUBAR_PROPERTY}.
     * 
     * @return a freshly created menubar object
     * 
     * @throws IOException
     *             on read errors, or configuration error
     */
    public JMenuBar makeMenubar() throws IOException {
        return makeMenubar(DEFAULT_MENUBAR_PROPERTY);
    }
    
    /**
     * Make a menubar from the property resources. It uses the key the specified
     * key as starting point
     * 
     * @param propertyName
     *            the key in the properties to be used as menubar definition.
     * 
     * @return a freshly created menubar object
     * 
     * @throws IOException
     *             on read errors, or configuration error
     */
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
    
    /**
     * Make a popup menu from the property resources. It uses the key the
     * specified key as starting point
     * 
     * @param propertyName
     *            the key in the properties to be used as menu definition.
     * 
     * @return a freshly created popup menu object
     * 
     * @throws IOException
     *             on read errors, or configuration error
     */
    public JPopupMenu makePopup(String propertyName) throws IOException {
        
        prepareProperties();
        
        String items[] = getPropertyOrFail(propertyName).split(" +");
        JPopupMenu result = new JPopupMenu();
        
        for (String item : items) {
            result.add(makeMenuItem(item));
        }
        
        return result;
    }

    /*
     * Make a menu from a given property key.
     */
    private JMenu makeMenu(String property) throws IOException {
        
        String items[] = getPropertyOrFail(property).split(" +");
        JMenu result = new JMenu(getPropertyOrFail(property + ".text"));
        
        for (String item : items) {
            // submenu must be ignored - it may appear however as first item.
            // just skip it
            if(item.equals("SUBMENU"))
                continue;
            result.add(makeMenuItem(item));
        }
        
        return result;
    }
    
    /*
     * Make menu item:
     * ACTION, TOGGLE_ACTION, TODO, SUBMENU, RADIO_ACTION, COMMAND
     */
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
                    menuItem.setIcon(GUIUtil.makeIcon(ClassLoader.getSystemResource(location)));
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

    /**
     * Get a property; fail if it is not present.
     * @throws IOException if the property is not set in the resources.
     */
    private String getPropertyOrFail(String property) throws IOException {
        String value = properties.getProperty(property);
        if(value == null) {
            throw new IOException("BarManager: Missing property '" + property +"' in " + resource);
        }
        return value;
    }


    /**
     * Get an action object for a class name.
     * 
     * <p>
     * The object is created, has the properties set and is initialised (if it
     * implements {@link InitialisingAction}.)
     * 
     * <p>
     * If the method has been called earlier with the same argument, no new
     * object is created, but the old is returned.
     * 
     * @param className
     *            the class name
     * 
     * @return the initilised action
     * 
     * @throws IOException
     *             wrapping any exception
     */
    public @NonNull Action makeAction(@NonNull String className) throws IOException {
        
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

    /**
     * Clear the cache of actions.
     */
//    @Deprecated
//    public void clearCache() {
//        actionCache.clear();
//    }

    /**
     * Put property into the map of properties to be provided to the actions.
     * 
     * <p>
     * <b>Please note:</b> Properties that are set after an action has been
     * created have no effect on already created actions. The will not see the
     * new values.
     * 
     * @param property
     *            the property key to set a value for
     * @param value
     *            the value to set for the key.
     */
    public void putProperty(@NonNull String property, Object value) {
        defaultActionProperties.put(property, value);
    }
}