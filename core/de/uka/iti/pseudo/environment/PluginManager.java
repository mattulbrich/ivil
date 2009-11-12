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

public class PluginManager {
    
    private PluginManager parentManager;
    
    private Map<String, Service> serviceMap = new HashMap<String, Service>();
    
    private static class Service {
        Class<?> type;
        Map<Object, Object> table = new HashMap<Object, Object>();
        List<Object> list = new LinkedList<Object>();
    }
    
    public PluginManager(PluginManager parentManager) throws EnvironmentException {
        this.parentManager = parentManager;
        try {
            makeServiceTable();
        } catch (Exception e) {
            throw new EnvironmentException("Error while loading plugin manager settings", e);
        }
    }

    private void makeServiceTable() throws IOException, ClassNotFoundException {
        Properties prop = new Properties();
        InputStream stream = getClass().getResourceAsStream("PluginManager.properties");
        if(stream == null)
            throw new FileNotFoundException("PluginManager.properties not present");
        prop.load(stream);
        
        for(Map.Entry<Object, Object> entry : prop.entrySet()) {
            Class<?> clss = Class.forName(entry.getValue().toString());
            Service service = new Service();
            service.type = clss;
            serviceMap.put(entry.getKey().toString(), service);
        }
    }

    public void register(String serviceName, String implementation) throws EnvironmentException {
        
        Service service = serviceMap.get(serviceName);
        if(service == null)
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
    
    public <T> List<T> getPlugins(String serviceName, Class<T> serviceClass) throws EnvironmentException {
        return getPlugins0(serviceName, serviceClass, new ArrayList<T>());
    }
    
    @SuppressWarnings("unchecked") 
    public <T> List<T> getLocalPlugins(String serviceName, Class<T> serviceClass) throws EnvironmentException {
        Service service = serviceMap.get(serviceName);
        if(service == null)
            throw new EnvironmentException("Unknown service: " + serviceName);
        
        if (service.type != serviceClass)
            throw new EnvironmentException(
                    "The service class and the type of the service do not conincide: "
                            + service.type + ", " + serviceName);
        
        return (List<T>) Collections.unmodifiableList(service.list);
    }
    
    private <T> List<T> getPlugins0(String serviceName, Class<T> serviceClass, ArrayList<T> arrayList) throws EnvironmentException {
        arrayList.addAll(getLocalPlugins(serviceName, serviceClass));
        
        if(parentManager != null)
            parentManager.getPlugins0(serviceName, serviceClass, arrayList);
        
        return arrayList;
    }

    @SuppressWarnings("unchecked") // The content of the maps is checked! 
    public <T> T getPlugin(String serviceName, Class<T> serviceClass, Object key) throws EnvironmentException {
        Service service = serviceMap.get(serviceName);
        if(service == null)
            throw new EnvironmentException("Unknown service: " + serviceName);
        
        if (service.type != serviceClass)
            throw new EnvironmentException(
                    "The service class and the type of the service do not conincide: "
                            + service.type + ", " + serviceName);
        
        Object result = service.table.get(key);
        if(result == null && parentManager != null)
            result = parentManager.getPlugin(serviceName, serviceClass, key);
        
        return (T) result;
    }

}
