/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2012 Karlsruhe Institute of Technology
 *
 * The system is protected by the GNU General Public License.
 * See LICENSE.TXT (distributed with this file) for details.
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

import nonnull.NonNull;
import nonnull.Nullable;

/**
 * The PluginManager is the is the management unit at which plugins can be
 * registered and from which they can be retrieved.
 *
 * A plugin can be registered for a particular <i>service</i>. A service has got
 * a name and a class associated. An implementation of a service needs to extend
 * (or implement) that associated class.
 *
 * A plugin manager may (or may not) hold a reference to a parent manager. It
 * includes the results of that parent manager (if set) to answer requests.
 *
 * <p>
 * Plugins can be registereed using the {@link #register(String, String)} method
 * which expects a service name and a (complete) class name that can be used
 * with {@link Class#forName(String)}. The list of all plugins for a particular
 * type can be retrieved using {@link #getPlugins(String, Class)}, or if the
 * plugins of the parent manger should not be included by
 * {@link #getLocalPlugins(String, Class)}. The class to be taken has to be
 * added explicitly to avoid unexpected class cast exception at later points
 * where they would be difficult to trace.
 *
 * <p>
 * If a service implements the {@link Mappable} interface, which provides a key
 * via the method {@link Mappable#getKey()}, any implementation is also added to
 * hash table under that key from which it can be retrieved using the method
 * {@link #getPlugin(String, Class, Object)}.
 *
 * <h2>Configuration file</h2>
 *
 * The resource <code>PluginManager.properties</code> is parsed to learn about
 * the services to be provided. This properties file contains lines of the form:
 *
 * <pre>
 *    serviceName : org.foo.bar.ClassName
 * </pre>
 *
 * in which a service of name <code>serviceName</code> and with an associated
 * class <code>org.foo.bar.ClassName</code> is described. That class must be be
 * accessible at runtime.
 *
 * <h2>Sample scenario</h2>
 *
 * <pre>
 *   PluginManager.properties:
 *     prettyPrinter : org.foo.bar.PrettyPrinter
 *     comparator : java.lang.Comparator
 *
 *   PluginManager pm = new PluginManager(null);  // no parent manager
 *   pm.register("comparator", new Comparator() { ... });
 *
 *   PrettyPrinter pp;
 *   // ...
 *   pm.register("prettyPrinter", pp);
 * </pre>
 *
 * @ivildoc "Plugin Configuration"
 * <h2>Configurating the Plugin Manager</h2>
 * <dl>
 * <dt>Meta function</dt>
 * <dd>
 * Meta functions are functions which are translated to terms during rule
 * application. The instances of class
 * <tt>de.uka.iti.pseudo.environment.MetaFunction</tt> are registered under the
 * key <tt>metaFunction</tt>.</dd>
 * <dt>Where condition</dt>
 * <dd>Where conditions decide when rules are applicable. The instances of class
 * <tt>de.uka.iti.pseudo.environment.WhereCondition</tt> are registered under
 * the key <tt>whereCondition</tt>.</dd>
 * <dt>Pretty printer</dt>
 * <dd>Pretty printer plugins allow the configuration of presentation of the
 * logic. The instances of class
 * <tt>de.uka.iti.pseudo.prettyprint.PrettyPrintPlugin</tt> are registered under
 * the key <tt>prettyPrinter</tt>.</dd>
 * <dt>Proof hints</dt>
 * <dd>
 * Hints to the proof are given in statement annotations. The instances of
 * interface <tt>de.uka.iti.pseudo.auto.strategy.hint.ProofHint</tt> are
 * registered under the key <tt>proofHint</tt>.</dd>
 * <dt>Decision procedures</dt>
 * <dd>
 * Decision procedures can be used to discharge proof obligations by external
 * solver programs. The instances of interface
 * <tt>de.uka.iti.pseudo.auto.DecisionProcedure</tt> are registered under the
 * key <tt>decisionProcedure</tt>.</dd>
 * </dl>
 */
public final class PluginManager {

    /**
     * The parent plugin manager upon which this manager relies. May be null.
     */
    private @Nullable final PluginManager parentManager;

    /**
     * Mapping names of services to service structure instances.
     */
    private final Map<String, Service> serviceMap = new HashMap<String, Service>();

    /**
     * An instance of class Service holds more information on a particular
     * plugin service which is provided by this manager.
     */
    private final static class Service {

        /**
         * The class type of which all implementations have to be.
         */
        private final Class<?> type;

        /**
         * A service may implement {@link Mappable}. If it does, it is added to
         * this table under its key.
         */
        private final Map<Object, Object> table = new HashMap<Object, Object>();

        /**
         * The list holding all registered plugins for that service.
         */
        private final List<Object> list = new LinkedList<Object>();

        private  Service(Class<?> type) {
            this.type = type;
        }

    }

    /**
     * Instantiates a new plugin manager with a given parent reference.
     *
     * The configuration is read from the resource <code>PluginManager.properties</code>.
     *
     * @param parentManager
     *            the parent manager to set
     *
     * @throws EnvironmentException
     *             if reading the configuration fails
     */
    public PluginManager(@Nullable PluginManager parentManager)
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
        if (stream == null) {
            throw new FileNotFoundException(
                    "PluginManager.properties not present");
        }
        prop.load(stream);

        for (Map.Entry<Object, Object> entry : prop.entrySet()) {
            Class<?> clss = Class.forName(entry.getValue().toString());
            Service service = new Service(clss);
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
     *            the name of the implementing class (the plugin
     *            implementation)
     *
     * @throws EnvironmentException
     *             if no creating an instance of the class fails or if the class
     *             does not have the needed type
     */
    public void register(@NonNull String serviceName, @NonNull String implementation)
            throws EnvironmentException {

        Service service = serviceMap.get(serviceName);
        if (service == null) {
            throw new EnvironmentException("Unknown service: " + serviceName);
        }

        Object instance;
        try {
            Class<?> clss = Class.forName(implementation);
            instance = clss.newInstance();
        } catch (Exception e) {
            throw new EnvironmentException("Class " + implementation
                    + " cannot be instantiated.", e);
        }

        if (!service.type.isInstance(instance)) {
            throw new EnvironmentException("Class " + implementation
                    + " is not a subtype of " + service.type + " for service "
                    + serviceName);
        }

        service.list.add(instance);
        if (instance instanceof Mappable) {
            Mappable<?> mappable = (Mappable<?>) instance;
            service.table.put(mappable.getKey(), instance);
        }
    }

    /**
     * Gets a list of all plugins for a service.
     *
     * If a parent manger is set, the call is also delegated to that manager and
     * the results combined.
     *
     * @param serviceName
     *            the service name
     * @param serviceClass
     *            the service class
     *
     * @param <T>
     *            The class type of the plugins to be retrieved.
     *
     * @return a freshly create and mutuable list of plugins.
     *
     * @throws EnvironmentException
     *             <ul>
     *             <li>if the service name is not a service known to the
     *             configuration
     *             <li>If the service class and the type of the service do not
     *             coincide
     *             </ul>
     */
    public @NonNull <T> List<T> getPlugins(@NonNull String serviceName,
            @NonNull Class<T> serviceClass)
        throws EnvironmentException {

        return getPlugins0(serviceName, serviceClass, new ArrayList<T>());
    }

    /**
     * Gets a list of all local plugins for a service.
     *
     * The call is not delegated to a potentially set parent manger.
     *
     * @param serviceName
     *            the service name
     * @param serviceClass
     *            the service class
     * @param <T>
     *            The class type of the plugins to be retrieved.
     *
     * @return an immutable list of plugins of type service classe
     *
     * @throws EnvironmentException
     *             <ul>
     *             <li>if the service name is not a service known to the
     *             configuration
     *             <li>If the service class and the type of the service do not
     *             coincide
     *             </ul>
     */
    @SuppressWarnings("unchecked") public @NonNull <T> List<T> getLocalPlugins(
            @NonNull String serviceName, @NonNull Class<T> serviceClass)
            throws EnvironmentException {
        Service service = serviceMap.get(serviceName);
        if (service == null) {
            throw new EnvironmentException("Unknown service: " + serviceName);
        }

        if (service.type != serviceClass) {
            throw new EnvironmentException(
                    "The service class and the type of the service do not coincide: "
                            + service.type + ", " + serviceName);
        }

        return (List<T>) Collections.unmodifiableList(service.list);
    }

    /*
     * Aggregating method to recursively collect all plugins into an existing
     * ArrayList
     */
    private <T> List<T> getPlugins0(String serviceName, Class<T> serviceClass,
            ArrayList<T> arrayList) throws EnvironmentException {
        arrayList.addAll(getLocalPlugins(serviceName, serviceClass));

        if (parentManager != null) {
            parentManager.getPlugins0(serviceName, serviceClass, arrayList);
        }

        return arrayList;
    }

    /**
     * Gets a particular plugin by key.
     *
     * If no plugin is registered under that key and there is a parent manager,
     * the call is delegated to that manager. If that call returns
     * <code>null</code>, null is returned, otherwise the first found plugin is
     * returned.
     *
     * @param serviceName
     *            the service name
     * @param serviceClass
     *            the service class
     * @param key
     *            the key of the plugin to retrieve
     * @param <T>
     *            The class type of the plugin to be retrieved.
     *
     * @return the found plugin or null
     *
     * @throws EnvironmentException
     *             <ul>
     *             <li>if the service name is not a service known to the
     *             configuration
     *             <li>If the service class and the type of the service do not
     *             coincide
     *             </ul>
     */
    @SuppressWarnings("unchecked")
    // The content of the maps is checked!
    public @Nullable <T> T getPlugin(@NonNull String serviceName,
            @NonNull Class<T> serviceClass, @NonNull Object key)
        throws EnvironmentException {

        Service service = serviceMap.get(serviceName);
        if (service == null) {
            throw new EnvironmentException("Unknown service: " + serviceName);
        }

        if (service.type != serviceClass) {
            throw new EnvironmentException(
                    "The service class and the type of the service do not coincide: "
                            + service.type + ", " + serviceName);
        }

        Object result = service.table.get(key);
        if (result == null && parentManager != null) {
            result = parentManager.getPlugin(serviceName, serviceClass, key);
        }

        return (T) result;
    }

}
