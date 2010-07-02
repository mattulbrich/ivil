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
package de.uka.iti.pseudo.gui.parameters;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.JTextPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import de.uka.iti.pseudo.gui.VerticalLayout;

/**
 * A component which can be used to set or change the parameters of an object.
 * The information on the possible parameters of the object are given by a
 * properties file which describes the parameters of a class.
 * 
 * <h2>Properties File format</h2>
 * The key <code>parameters</code> needs to be present. It enumerates as space
 * separated list all available parameters. The name of a parameter decides also
 * on the setter and getter method which is used to write and read information
 * of this parameter. The getter is expected to be called <code>get</code>
 * followed by the parameter name with the first letter capitalised, and the
 * setter is expected to be called <code>set</code> followed by the parameter
 * name with the first letter capitalised. <b>Please note</b> that also boolean
 * parameters need to have a getter with "get" instead of "is".
 * 
 * <p>
 * For parameters <code>p</code> the following keys may be present: <table
 * border=1 cellpadding=4>
 * <tr>
 * <th>Key</th>
 * <th>Purpose</th>
 * <th>Notes</th>
 * </tr>
 * <tr>
 * <td><code>p.shortDesc</code></td>
 * <td>The description which will be used to label the entry in the UI.</td>
 * <td>defaults to name of the parameter</td>
 * </tr>
 * <tr>
 * <td><code>p.longDesc</code></td>
 * <td>The description which will be used as tooltip.</td>
 * <td>defaults to null</td>
 * </tr>
 * <tr>
 * <td><code>p.min</code></td>
 * <td>The minimum value used for a slider component.</td>
 * <td>only used for integer parameters</td>
 * </tr>
 * <tr>
 * <td><code>p.max</code></td>
 * <td>The maximum value used for a slider component.</td>
 * <td>only used for integer parameters</td>
 * </tr>
 * <tr>
 * <td><code>p.enum.XYZ</code></td>
 * <td>The label for an enumerated element which is named XYZ</td>
 * <td>only used for enum parameters</td>
 * </tr>
 * <tr>
 * <td><code>p.component</code></td>
 * <td>For parameters other than integer, boolean, string and enum, you need to
 * specify a class extending JComponent (see below). </td>
 * <td>defaults to null</td>
 * </tr>
 * </table>
 * 
 * <h3>Specifying a special component for a parameter</h3>
 * 
 * If you want/need a special {@link JComponent} to display/edit your parameter,
 * set this using the <code>p.component</code> syntax described above. The
 * component class needs a nullary constructor. The following properities are
 * set in this order using {@link JComponent#putClientProperty(Object, Object)}:
 * <table>
 * <tr>
 * <th>Symbolic Property</th>
 * <th>Property value</th>
 * <th>Meaning</th>
 * </tr>
 * <tr>
 * <td><code>PARAM_OBJECT</code></td>
 * <td><code>"param.object"</code></td>
 * <td>The object which is to be parametrised</td>
 * </tr>
 * <tr>
 * <td><code>PARAM_PARAM</code></td>
 * <td><code>"param.param"</code></td>
 * <td>The parameter which is to be set</td>
 * </tr>
 * <tr>
 * <td><code>PARAM_SHORTDESC</code></td>
 * <td><code>"param.shortDesc"</code></td>
 * <td>The parameter's short description (can be used as caption)</td>
 * </tr>
 * <tr>
 * <td><code>PARAM_LONGDESC</code></td>
 * <td><code>"param.longDesc"</code></td>
 * <td>The parameter's long description (e.g. as tooltip)</td>
 * </tr>
 * <tr>
 * <td><code>PARAM_GETTER</code></td>
 * <td><code>"param.getter"</code></td>
 * <td>The corresponding getter method</td>
 * </tr>
 * <tr>
 * <td><code>PARAM_SETTER</code></td>
 * <td><code>"param.setter"</code></td>
 * <td>The corresponding setter method</td>
 * </tr>
 * </table>
 * 
 * A custom component can provide a {@link PropertyChangeListener} noticing
 * changes in these parameters. You can but do not have to use the provided
 * getter and setter methods.
 */
public class ParameterSheet extends JPanel {

    private static final long serialVersionUID = -7751152514376689123L;

    /**
     * That is the private data container for the information on a parameter. It
     * does not provide any functionality.
     */
    private static class Parameter {

        /**
         * The name of a parameter decides on the name of the getter and setter
         * methods
         */
        String name;

        /**
         * The type of the parameter is retrieved as the result type of the
         * getter method
         */
        Class<?> type;

        /**
         * The short description is provided in properties. If not defaults to
         * name
         */
        String shortDesc;

        /**
         * The long description is provided in the properties. If not defaults
         * to null
         */
        String longDesc;

        /**
         * The getter method is retrieved by reflection
         */
        Method getterMethod;

        /**
         * The setter method is retrieved by reflection
         */
        Method setterMethod;

        /**
         * If a custom component has been specified, this is not null
         */
        JComponent customComponent;
    }

    //
    // Suffixes for the properties to read from the properties file
    private static final String SUFFIX_SHORT_DESC = ".shortDesc";
    private static final String SUFFIX_LONG_DESC = ".longDesc";
    private static final String SUFFIX_ENUM = ".enum.";
    private static final String SUFFIX_COMPONENT = ".component";
    private static final String SUFFIX_MIN = ".min";
    private static final String SUFFIX_MAX = ".max";
    private static final String DESCRIPTION_KEY = "description";

    //
    // Attributes to custom components
    public static final String PARAM_OBJECT = "param.object";
    public static final String PARAM_PARAM = "param.param";
    public static final String PARAM_GETTER = "param.getter";
    public static final String PARAM_SETTER = "param.setter";
    public static final String PARAM_LONGDESC = "param.longDesc";
    public static final String PARAM_SHORTDESC = "param.shortDesc";

    /**
     * the object under inspection
     */
    private Object object;

    /**
     * The type of the object under inspection
     */
    private Class<?> objectClass;

    /**
     * The properties from which parameter information is read
     */
    private Properties properties;

    /**
     * Instantiates a new parameter sheet for a certain object using its dynamic
     * type to find properties on the parameters.
     * 
     * @param object
     *            the object to inspect
     * 
     * @throws SecurityException
     *             may be thrown by reflections
     * @throws IllegalArgumentException
     *             may be thrown by reflections
     * @throws IOException
     *             if parameters are not present or illegally formatted
     * @throws NoSuchMethodException
     *             may be thrown by reflections
     * @throws IllegalAccessException
     *             may be thrown by reflections
     * @throws InvocationTargetException
     *             may be thrown by reflections
     * @throws ClassNotFoundException
     *             may be thrown by reflections
     * @throws InstantiationException
     *             may be thrown by reflections
     * @throws ClassCastException
     *             may be thrown by reflections
     */
    public <T> ParameterSheet(T object) throws SecurityException,
            IllegalArgumentException, IOException, NoSuchMethodException,
            IllegalAccessException, InvocationTargetException,
            ClassCastException, InstantiationException, ClassNotFoundException {
        this((Class<T>) object.getClass(), object);
    }

    /**
     * Instantiates a new parameter sheet for a certain object using the
     * information for a class which may be a super class of the actual class of
     * object.
     * 
     * @param clss
     *            the Class to be used to gather parameter information
     * @param object
     *            the object to inspect
     * 
     * @throws SecurityException
     *             may be thrown by reflections
     * @throws IllegalArgumentException
     *             may be thrown by reflections
     * @throws IOException
     *             if parameters are not present or illegally formatted
     * @throws NoSuchMethodException
     *             may be thrown by reflections
     * @throws IllegalAccessException
     *             may be thrown by reflections
     * @throws InvocationTargetException
     *             may be thrown by reflections
     * @throws ClassNotFoundException
     *             may be thrown by reflections
     * @throws InstantiationException
     *             may be thrown by reflections
     * @throws ClassCastException
     *             may be thrown by reflections
     */
    public <T> ParameterSheet(Class<T> clss, T object) throws IOException,
            SecurityException, NoSuchMethodException, IllegalArgumentException,
            IllegalAccessException, InvocationTargetException,
            ClassCastException, InstantiationException, ClassNotFoundException {
        this(clss, null, object);
    }

    /**
     * Instantiates a new parameter sheet for a certain object using the
     * information for a class which may be a super class of the actual class of
     * object.
     * 
     * @param clss
     *            the Class to be used to gather parameter information
     * @param propertyResource
     *            the URL of the properties file to retrieve information on the
     *            object from.
     * @param object
     *            the object to inspect
     * 
     * @throws SecurityException
     *             may be thrown by reflections
     * @throws IllegalArgumentException
     *             may be thrown by reflections
     * @throws IOException
     *             if parameters are not present or illegally formatted
     * @throws NoSuchMethodException
     *             may be thrown by reflections
     * @throws IllegalAccessException
     *             may be thrown by reflections
     * @throws InvocationTargetException
     *             may be thrown by reflections
     * @throws ClassNotFoundException
     *             may be thrown by reflections
     * @throws InstantiationException
     *             may be thrown by reflections
     * @throws ClassCastException
     *             may be thrown by reflections
     */
    public <T> ParameterSheet(Class<T> clss, URL propertyResource, T object)
            throws IOException, SecurityException, NoSuchMethodException,
            IllegalArgumentException, IllegalAccessException,
            InvocationTargetException, ClassCastException,
            InstantiationException, ClassNotFoundException {

        this.objectClass = clss;
        this.object = object;

        properties = loadProperties(propertyResource);
        List<Parameter> parameters = makeParameters();

        if (parameters == null || parameters.size() == 0) {
            if(properties.containsKey("description")){
                setLayout(new VerticalLayout());
                addPreamble();
            } else {
                JLabel empty = new JLabel("No properties to edit");
                empty.setFont(empty.getFont().deriveFont(Font.ITALIC));
                add(empty);
            }
        } else {
            setLayout(new VerticalLayout());
            addPreamble();
            for (Parameter parameter : parameters) {
                addComponentsFor(parameter);
            }
        }
    }

    // can be added

    /*
     * read the properties and provide a list of parameters from them
     */
    private List<Parameter> makeParameters() throws SecurityException,
            NoSuchMethodException, IOException, ClassCastException,
            InstantiationException, IllegalAccessException,
            ClassNotFoundException {
        ArrayList<Parameter> result = new ArrayList<Parameter>();

        String parameterKey = properties.getProperty("parameters");
        if (parameterKey == null)
            throw new IOException("Key parameters is not defined");

        for (String param : parameterKey.split(" +")) {
            if (param.length() > 0)
                result.add(readParameter(param));
        }
        return result;
    }

    /*
     * Read a single parameter from the properties. The parameter has set its
     * name, shortDesc, longDesc, reader and setter fields. The user component
     * field is only created if set.
     * 
     * If a method cannot be found or is of illegal type, an exception is
     * issued.
     */
    private Parameter readParameter(String name) throws SecurityException,
            NoSuchMethodException, ClassCastException, InstantiationException,
            IllegalAccessException, ClassNotFoundException {
        Parameter p = new Parameter();
        p.name = name;
        p.shortDesc = properties.getProperty(name + SUFFIX_SHORT_DESC, name);
        p.longDesc = properties.getProperty(name + SUFFIX_LONG_DESC);

        String bigName = Character.toUpperCase(name.charAt(0))
                + name.substring(1);
        String getterMethodName = "get" + bigName;
        String setterMethodName = "set" + bigName;

        p.getterMethod = objectClass.getMethod(getterMethodName);
        p.type = p.getterMethod.getReturnType();
        p.setterMethod = objectClass.getMethod(setterMethodName, p.type);
        
        String userComponentClass = properties.getProperty(name
                + SUFFIX_COMPONENT);
        if (userComponentClass != null) {
            p.customComponent = makeCustomComponent(userComponentClass, p);
        }

        return p;
    }

    /*
     * using the specified class name create an object which has the necessary
     * custom fields set.
     */
    private JComponent makeCustomComponent(String userComponentClass,
            Parameter p) throws InstantiationException, IllegalAccessException,
            ClassNotFoundException, ClassCastException {

        JComponent component = (JComponent) Class.forName(userComponentClass)
                .newInstance();
        component.putClientProperty(PARAM_OBJECT, object);
        component.putClientProperty(PARAM_PARAM, p.name);
        component.putClientProperty(PARAM_SHORTDESC, p.shortDesc);
        component.putClientProperty(PARAM_LONGDESC, p.longDesc);
        component.putClientProperty(PARAM_GETTER, p.getterMethod);
        component.putClientProperty(PARAM_SETTER, p.setterMethod.toString());
        return component;
    }

    /*
     * Load properties from file
     */
    private Properties loadProperties(URL propertyResource) throws IOException {

        if (propertyResource == null) {
            String resource = "/"
                    + objectClass.getCanonicalName().replace('.', '/')
                    + "_parameters.properties";
            propertyResource = objectClass.getResource(resource);
            if (propertyResource == null)
                throw new FileNotFoundException("Cannot find resource " + resource);
        }

        InputStream instream = propertyResource.openStream();

        Properties result = new Properties();
        result.load(instream);
        return result;
    }

    /*
     * Adds a text component before the edit components.
     */
    private void addPreamble() {
        String descr = properties.getProperty(DESCRIPTION_KEY);
        if (descr != null) {
            JTextPane pane = new JTextPane();
            pane.setEditable(false);
            pane.setText(descr);
            pane.setBackground(getBackground());
            add(pane);
        }

    }

    /*
     * Adds the components for a parameter to the panel (i.e. this)
     */
    private void addComponentsFor(final Parameter parameter)
            throws IllegalArgumentException, IllegalAccessException,
            InvocationTargetException {

        Object value = parameter.getterMethod.invoke(object);

        if (parameter.customComponent != null) {
            add(parameter.customComponent);

        } else if (parameter.type == Boolean.TYPE) {
            addComponentsForBoolean(parameter, value);

        } else if (parameter.type == String.class) {
            addComponentsForString(parameter, value);

        } else if (parameter.type == Integer.TYPE) {
            addComponentsForInteger(parameter, value);

        } else if (parameter.type.isEnum()) {
            addComponentsForEnum(parameter, value);

        } else {
            JPanel panel = makeTitledPanel(parameter.shortDesc);
            JLabel cannot = new JLabel("Cannot be set");
            cannot.setFont(cannot.getFont().deriveFont(Font.ITALIC));
            panel.add(cannot);
            add(panel);
        }
    }

    /*
     * Create a new panel with titled frame and add a radiobutton for every
     * fields of an enum type. Register listeners to notice changes.
     */
    private void addComponentsForEnum(final Parameter parameter, Object value) {
        Enum<?>[] enums = (Enum[]) parameter.type.getEnumConstants();
        ButtonGroup group = new ButtonGroup();
        JPanel panel = makeTitledPanel(parameter.shortDesc);
        panel.setToolTipText(parameter.longDesc);
        for (final Enum<?> o : enums) {
            final JRadioButton r = new JRadioButton();
            r.setText(properties.getProperty(parameter.name + SUFFIX_ENUM
                    + o.name()));
            r.setSelected(o == value);
            r.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if (r.isSelected()) {
                        callSetter(parameter, o);
                    }
                }

            });
            group.add(r);
            panel.add(r);
        }
        add(panel);
    }

    /*
     * Create a spinner of a certain interval in a panel with titled frame, add
     * a textfield indicating the value. Register listeners to notice changes.
     */
    private void addComponentsForInteger(final Parameter parameter, Object value) {
        JPanel panel = makeTitledPanel(parameter.shortDesc);
        panel.setToolTipText(parameter.longDesc);

        int min = Integer.decode(properties.getProperty(parameter.name
                + SUFFIX_MIN, "0"));
        int max = Integer.decode(properties.getProperty(parameter.name
                + SUFFIX_MAX, Integer.toString(min + 1)));

        final JSlider slider = new JSlider(min, max);
        final JLabel label = new JLabel("uuu");
        slider.setMajorTickSpacing((max - min) / 3);
        slider.setMinorTickSpacing(1);
        slider.setSnapToTicks(true);
        slider.setPaintTicks(true);
        slider.setPaintLabels(true);
        slider.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                label.setText("Value: " + slider.getValue());
                callSetter(parameter, (Integer) slider.getValue());
            }
        });

        slider.setValue((Integer) value);
        panel.add(slider);
        panel.add(label);
        add(panel);
    }

    /*
     * Create a text field in a panel with titled frame. Register listeners to
     * notice changes.
     */
    private void addComponentsForString(final Parameter parameter, Object value) {
        JPanel panel = makeTitledPanel(parameter.shortDesc);
        panel.setToolTipText(parameter.longDesc);

        final JFormattedTextField text = new JFormattedTextField();
        text.setText(value == null ? "" : value.toString());
        text.addPropertyChangeListener("value", new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                callSetter(parameter, text.getText());
            }
        });
        panel.add(text);
        add(panel);
    }

    /*
     * Create a check box. Register listeners to notice changes.
     */
    private void addComponentsForBoolean(final Parameter parameter, Object value) {
        final JCheckBox checkBox = new JCheckBox(parameter.shortDesc);
        checkBox.setToolTipText(parameter.longDesc);
        checkBox.setSelected((Boolean) value);
        checkBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                callSetter(parameter, Boolean.valueOf(checkBox.isSelected()));
            }
        });
        add(checkBox);
    }

    /*
     * Call getter on the inspected object for a parameter. All exceptions are
     * wrapped in a runtime exception.
     */
    protected Object callGetter(Parameter parameter) {
        try {
            return parameter.getterMethod.invoke(object);
        } catch (Exception e) {
            throw new RuntimeException("cannot call getter method");
        }
    }

    /*
     * Call setter on the inspected object for a parameter. All exceptions are
     * wrapped in a runtime exception.
     */
    private void callSetter(Parameter parameter, Object value) {
        try {
            parameter.setterMethod.invoke(object, value);
        } catch (Exception e) {
            throw new RuntimeException("cannot call setter method");
        }
    }

    /*
     * Make a new titled panel using the BorderFactory
     */
    private JPanel makeTitledPanel(String title) {
        JPanel result = new JPanel(new VerticalLayout());
        result.setBorder(BorderFactory.createTitledBorder(title));
        return result;
    }

}
