/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2010 Universitaet Karlsruhe, Germany
 * 
 * The system is protected by the GNU General Public License. 
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.environment.creation;

import java.io.IOException;
import java.net.URL;
import java.util.ServiceLoader;

import nonnull.NonNull;
import nonnull.Nullable;
import checkers.nullness.quals.Pure;
import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.environment.EnvironmentException;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.util.Pair;

/**
 * Implementations of this class provide means to create environments from
 * streams in various formats.
 * 
 * This class provides also static methods which allow to conveniently work with
 * the services.
 * 
 * To add your implementation, be sure to add an appropriate line to the file
 * <code>META-INF/services/de.uka.iti.pseudo.environment.creation.EnvironmentCreationService</code>.
 * 
 * @see PFileEnvironmentCreationService
 * 
 */
public abstract class EnvironmentCreationService {

    /**
     * Get the description of this creation service.
     * 
     * It should briefly describe the input format that is supports. It must at
     * all calls return the same description and must change anything.
     * 
     * @return the description of the service.
     */
    public abstract @NonNull @Pure String getDescription();

    /**
     * Gets the extension which is typical for that service.
     * 
     * It must return a string <b>without leading dot '.'</p> which is the
     * typical suffix of files to be loaded using the service. It must at all
     * calls return the same description and must change anything.
     * 
     * @return the default extension
     */
    public abstract @NonNull @Pure String getDefaultExtension();

    /**
     * Actually load the environment from the resource.
     * 
     * It returns the environment plus the embedded problem term if there is
     * one. The second part of the return pair may be <code>null</code> if no
     * problem term has been specified.
     * 
     * @param url
     *            the resource to read the environment from.
     * 
     * @return a pair of environment and problem term. The latter may be
     *         <code>null</code>.
     * 
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     * @throws EnvironmentException
     *             if loading fails for some reason
     */
    public abstract @NonNull Pair<Environment, /*@Nullable*/ Term> createEnvironment(@NonNull URL url) 
        throws IOException, EnvironmentException;
    
    /**
     * The service loader is used to create instances from the services file.
     */
    private static ServiceLoader<EnvironmentCreationService> serviceLoader =
        ServiceLoader.load(EnvironmentCreationService.class);
    
    /**
     * Retrieves an iterable object which allows to walk over all installed services.
     * 
     * @return all installed services.
     */
    public static @NonNull Iterable<EnvironmentCreationService> getServices() {
        return serviceLoader;
    }

    /**
     * Creates an environment from an url resource.
     * 
     * The service to be used is determined using the extension of the url.
     * 
     * @param url
     *            the resource to load
     * 
     * @return a pair of environment and problem term. The latter may be
     *         <code>null</code>.
     * 
     * @throws EnvironmentException
     *             if loading fails for some reason or if no loader is installed
     *             for the given extension.
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public static Pair<Environment, /*@Nullable*/ Term> createEnvironmentByExtension(URL url)
            throws EnvironmentException, IOException {

        int dotPos = url.getPath().lastIndexOf('.');
        String ext = url.getPath().substring(dotPos + 1);

        EnvironmentCreationService service = getServiceByExtension(ext);
        if (service == null) {
            throw new EnvironmentException(
                    "No environment creation service for extension '." + ext
                            + "'.");
        }

        return service.createEnvironment(url);
    }

    /**
     * Gets an service by extension.
     * 
     * @param extension
     *            the file extension
     * 
     * @return a service with the default extension <code>extension</code>,
     *         <code>null</code> if no such service has been installed.
     */
    public static @Nullable EnvironmentCreationService getServiceByExtension(String extension) {
        for (EnvironmentCreationService service : serviceLoader) {
            if(extension.equals(service.getDefaultExtension()))
                return service;
        }
        
        return null;
    }
    
}
