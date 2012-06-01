package de.uka.iti.ivil.jbc.environment;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * This class creates the right POG for a specification file, based on the files
 * extension. As default, only the jml POG is registered.
 *
 * @author timm.felden@felden.com
 */
// FIXME make Factory not an enum, use services to register generators
public enum POGeneratorFactory {
    instance;

    private final Map<String, Class<? extends ProofObligationGenerator>> knownGenerators = Collections
            .synchronizedMap(new HashMap<String, Class<? extends ProofObligationGenerator>>());

    private POGeneratorFactory() {
        // register default generators
       registerGenerator("jml", de.uka.iti.ivil.jml.ProofObligationGenerator.class);
    }

    public static ProofObligationGenerator getGeneratorByPath(final String path) throws InstantiationException,
            IllegalAccessException {
        final String extension = path.substring(path.lastIndexOf('.') + 1);

        Class<? extends ProofObligationGenerator> gen = instance.knownGenerators.get(extension);
        return null == gen ? null : gen.newInstance();
    }

    public void registerGenerator(final String extension, Class<? extends ProofObligationGenerator> generator) {
        knownGenerators.put(extension, generator);
    }
}
