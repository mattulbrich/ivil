/*
 * This file is part of PSEUDO
 * Copyright (C) 2009 Universitaet Karlsruhe, Germany
 *    written by Mattias Ulbrich
 * 
 * The system is protected by the GNU General Public License. 
 * See LICENSE.TXT for details.
 */
package de.uka.iti.pseudo.environment;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * The Class PluginManager.
 */
public class PluginManager {

    /**
     * The parent plugin manager upon which this manager relies. May be null.
     */
    private PluginManager parentManager;

    /**
     * Mapping names of services to service structure instances.
     */
    private Map<String, Service> serviceMap = new HashMap<String, Service>();

    /**
     * An instance of class Service holds more information on a particular
     * plugin service which is provided by this manager.
     */
    private static class Service {

        /**
         * The class type of which all implementations have to be.
         */
        Class<?> type;

        /**
         * A service may implement {@link Mappable}. If it does, it is added to
         * this table under its key.
         */
        Map<Object, Object> table = new HashMap<Object, Object>();

        /**
         * The list.
         */
        List<Object> list = new LinkedList<Object>();
    }

    /**
     * Instantiates a new plugin manager with a given parent reference.
     * 
     * The configuration is read from resource
     * <code>PluginManager.properties</code>.
     * 
     * @param parentManager
     *            the parent manager to set
     * 
     * @throws EnvironmentException
     *             if reading the configuration fails
     */
    public PluginManager(PluginManager parentManager)
            throws EnvironmentException {
        this.parentManager = parentManager;
        try {
            makeServiceTable();
        } catch (Exception e) {
            throw new EnvironmentException(
                    "Error while loading plugin manager settings", e);
        }
    }

    /**
     * Make service table.
     * 
     * Read configuration as properties and fill a map from names to services
     * accordingly.
     */
    private void makeServiceTable() throws IOException, ClassNotFoundException {
        Properties prop = new Properties();
        InputStream stream = getClass().getResourceAsStream(
                "PluginManager.properties");
        if (stream == null)
            throw new FileNotFoundException(
                    "PluginManager.properties not present");
        prop.load(stream);

        for (Map.Entry<Object, Object> entry : prop.entrySet()) {
            Class<?> clss = Class.forName(entry.getValue().toString());
            Service service = new Service();
            service.type = clss;
            serviceMap.put(entry.getKey().toString(), service);
        }
    }

    /**
     * Register a new plugin.
     * 
     * The service must be known to this manager and the implementation that has
     * been provided must be instantiateable.
     * 
     * @param serviceName
     *            the name of the service to register with.
     * @param implementation
     *            the name of the implementating class (the plugin
     *            implementation)
     * 
     * @throws EnvironmentException
     *             if no creating an instance of the class fails or if the class
     *             does not have the needed type
     */
    public void register(String serviceName, String implementation)
            throws EnvironmentException {

        Service service = serviceMap.get(serviceName);
        if (service == null)
            throw new EnvironmentException("Unknown service: " + serviceName);

        Object instance;
        try {
            Class<?> clss = Class.forName(implementation);
            instance = clss.newInstance();
        } catch (Exception e) {
            throw new EnvironmentException("Class " + implementation
                    + " cannot be instantiated.", e);
        }

        if (!service.type.isInstance(instance))
            throw new EnvironmentException("Class " + implementation
                    + " is not a subtype of " + service.type + " for service "
                    + serviceName);

        service.list.add(instance);
        if (instance instanceof Mappable) {
            Mappable mappable = (Mappable) instance;
            service.table.put(mappable.getKey(), instance);
        }
    }
 // TODO: Auto-generated Javadoc
    /**
     * Gets the plugins.
     * 
     * @param serviceName
     *            the service name
     * @param serviceClass
     *            the service class
     * 
     * @return the plugins
     * 
     * @throws EnvironmentException
     *             the environment exception
     */
    public <T> List<T> getPlugins(String serviceName, Class<T> serviceClass)
            throws EnvironmentException {
        return getPlugins0(serviceName, serviceClass, new ArrayList<T>());
    }

    /**
     * Gets the local plugins.
     * 
     * @param serviceName
     *            the service name
     * @param serviceClass
     *            the service class
     * 
     * @return the local plugins
     * 
     * @throws EnvironmentException
     *             the environment exception
     */
    @SuppressWarnings("unchecked") public <T> List<T> getLocalPlugins(
            String serviceName, Class<T> serviceClass)
            throws EnvironmentException {
        Service service = serviceMap.get(serviceName);
        if (service == null)
            throw new EnvironmentException("Unknown service: " + serviceName);

        if (service.type != serviceClass)
            throw new EnvironmentException(
                    "The service class and the type of the service do not coincide: "
                            + service.type + ", " + serviceName);

        return (List<T>) Collections.unmodifiableList(service.list);
    }

    /**
     * Gets the plugins0.
     * 
     * @param serviceName
     *            the service name
     * @param serviceClass
     *            the service class
     * @param arrayList
     *            the array list
     * 
     * @return the plugins0
     * 
     * @throws EnvironmentException
     *             the environment exception
     */
    private <T> List<T> getPlugins0(String serviceName, Class<T> serviceClass,
            ArrayList<T> arrayList) throws EnvironmentException {
        arrayList.addAll(getLocalPlugins(serviceName, serviceClass));

        if (parentManager != null)
            parentManager.getPlugins0(serviceName, serviceClass, arrayList);

        return arrayList;
    }

    /**
     * Gets the plugin.
     * 
     * @param serviceName
     *            the service name
     * @param serviceClass
     *            the service class
     * @param key
     *            the key
     * 
     * @return the plugin
     * 
     * @throws EnvironmentException
     *             the environment exception
     */
    @SuppressWarnings("unchecked")// The content of the maps is checked!
    public <T> T getPlugin(String serviceName, Class<T> serviceClass, Object key)
            throws EnvironmentException {
        Service service = serviceMap.get(serviceName);
        if (service == null)
            throw new EnvironmentException("Unknown service: " + serviceName);

        if (service.type != serviceClass)
            throw new EnvironmentException(
                    "The service class and the type of the service do not conincide: "
                            + service.type + ", " + serviceName);

        Object result = service.table.get(key);
        if (result == null && parentManager != null)
            result = parentManager.getPlugin(serviceName, serviceClass, key);

        return (T) result;
    }

}
