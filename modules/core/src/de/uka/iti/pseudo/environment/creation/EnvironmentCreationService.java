/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2012 Karlsruhe Institute of Technology
 *
 * The system is protected by the GNU General Public License.
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.environment.creation;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;
import java.util.ServiceLoader;

import nonnull.NonNull;
import nonnull.Nullable;
import checkers.nullness.quals.Pure;
import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.environment.EnvironmentException;
import de.uka.iti.pseudo.environment.ProofObligation;
import de.uka.iti.pseudo.term.Sequent;
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
     * all calls return the same description and must not change anything.
     *
     * @return the description of the service.
     */
    public abstract @NonNull @Pure String getDescription();

    /**
     * Gets the extension which is typical for that service.
     *
     * It must return a string <b>without leading dot '.'</b> which is the
     * typical suffix of files to be loaded using the service. It must at all
     * calls return the same description and must change anything.
     *
     * @return the default extension
     */
    public abstract @NonNull @Pure String getDefaultExtension();

    /**
     * Actually load the environment from the resource.
     *
     * It returns the environment plus the embedded problem sequent if there is
     * one. The second part of the return pair may be <code>null</code> if no
     * problem sequent has been specified.
     *
     * <p>The given URL is used both as source for the data and serves as the
     * name of the resource.
     *
     * @param url
     *            the resource to read the environment from.
     *
     * @return a pair of environment and problem sequent. The latter may be
     *         <code>null</code>.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     * @throws EnvironmentException
     *             if loading fails for some reason
     */
    public final @NonNull Pair<Environment, Map<String, ProofObligation>>
        createEnvironment(@NonNull URL url) throws IOException, EnvironmentException {

        return createEnvironment(url.openStream(), url);

    }

    /**
     * Actually load the environment from the resource.
     *
     * It returns the environment plus the embedded problem sequent if there is
     * one. The second part of the return pair may be <code>null</code> if no
     * problem sequent has been specified.
     *
     * @param stream
     *            the source the data should be taken from.
     * @param resource
     *            the resource name to set in the environment.
     *
     * @return a pair of environment and problem sequent. The latter may be
     *         <code>null</code>.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     * @throws EnvironmentException
     *             if loading fails for some reason
     */
    public abstract Pair<Environment, Map<String, ProofObligation>>
          createEnvironment(InputStream stream, URL resource)
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
     * @return a pair of environment and problem sequent. The latter may be
     *         <code>null</code>.
     *
     * @throws EnvironmentException
     *             if loading fails for some reason or if no loader is installed
     *             for the given extension.
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public static Pair<Environment, Map<String, ProofObligation>> createEnvironmentByExtension(URL url)
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
            if(extension.equals(service.getDefaultExtension())) {
                return service;
            }
        }

        return null;
    }

}
