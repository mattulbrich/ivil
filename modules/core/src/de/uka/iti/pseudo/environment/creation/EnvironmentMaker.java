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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import nonnull.NonNull;
import de.uka.iti.pseudo.auto.script.ProofScript;
import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.environment.EnvironmentException;
import de.uka.iti.pseudo.parser.ASTLocatedElement;
import de.uka.iti.pseudo.parser.ASTVisitException;
import de.uka.iti.pseudo.parser.ParseException;
import de.uka.iti.pseudo.parser.Parser;
import de.uka.iti.pseudo.parser.Token;
import de.uka.iti.pseudo.parser.file.ASTFile;
import de.uka.iti.pseudo.parser.file.ASTIncludeDeclarationBlock;
import de.uka.iti.pseudo.parser.file.ASTPluginDeclaration;
import de.uka.iti.pseudo.parser.file.ASTPlugins;
import de.uka.iti.pseudo.term.Sequent;
import de.uka.iti.pseudo.util.Log;
import de.uka.iti.pseudo.util.SelectList;
import de.uka.iti.pseudo.util.Util;
import de.uka.iti.pseudo.util.settings.Settings;

/**
 * The Class EnvironmentMaker traverses an {@link ASTFile} to extract
 * information about sorts, functions, binders, fix operators, etc. etc.
 */
public class EnvironmentMaker {

    /**
     * the directory where to search for system include files.
     */
    private static final String SYS_DIR = Settings.getInstance().
            getExpandedProperty(Settings.SYSTEM_DIRECTORY_KEY, "./sys");

    /**
     * The environment that is being built.
     */
    private Environment env;

    /**
     * remember the list of included files. This is not stored in the
     * environment can only be retrieved here.
     */
    private final List<String> importedFilenames = new ArrayList<String>();

    /**
     * The parser to use to parse include files.
     */
    private final Parser parser;

    /**
     * A map from names to problem sequents.
     */
    private final Map<String, Sequent> problemSequents;

    /**
     * A map of all proofs that have been scanned.
     */
    private final Map<String, ProofScript> proofScripts;

    /**
     * Instantiates a new environment maker.
     *
     * The file is parsed and the environment created automatically. The
     * environment has the builtin environment {@link Environment#BUILT_IN_ENV}
     * as parent.
     *
     * @param parser
     *            the parser to use for include instructions
     * @param file
     *            the file to parse, its name is used as name for the
     *            environment
     *
     * @throws ParseException
     *             some parse error appeared
     * @throws ASTVisitException
     *             some error happened during ast traversal.
     * @throws IOException
     *             if the URL/file cannot be read successfully.
     */
    public EnvironmentMaker(Parser parser, File file)
            throws ParseException, ASTVisitException, IOException {
        this(parser, file.toURI().toURL(), Environment.BUILT_IN_ENV);
    }

    /**
     * Instantiates a new environment maker.
     *
     * The file is parsed and the environment created automatically. The
     * environment has the builtin environment {@link Environment#BUILT_IN_ENV}
     * as parent.
     *
     * @param parser
     *            the parser to use for include instructions
     * @param url
     *            the url to parse, its name is used as name for the
     *            environment
     *
     * @throws ParseException
     *             some parse error appeared
     * @throws ASTVisitException
     *             some error happened during ast traversal.
     * @throws IOException
     *             if the URL/file cannot be read successfully.
     */
    public EnvironmentMaker(Parser parser, URL url)
            throws ParseException, ASTVisitException, IOException {
        this(parser, url, Environment.BUILT_IN_ENV);
    }

    /**
     * Instantiates a new environment maker.
     *
     * The file is parsed and the environment created automatically. The
     * environment has the builtin environment {@link Environment#BUILT_IN_ENV}
     * as parent.
     *
     * @param parser
     *            the parser to use for include instructions
     * @param stream
     *            the input stream from which the environment is to be parsed.
     * @param resource
     *            the url to parse, its name is used as name for the environment
     *
     * @throws ParseException
     *             some parse error appeared
     * @throws ASTVisitException
     *             some error happened during ast traversal.
     * @throws IOException
     *             if the URL/file cannot be read successfully.
     */
    public EnvironmentMaker(Parser parser, @NonNull InputStream stream, URL resource)
            throws ParseException, ASTVisitException, IOException {
        this(parser,
                parser.parseFile(new InputStreamReader(stream), resource.toString()),
                resource.toExternalForm(),
                Environment.BUILT_IN_ENV);
    }

    /**
     * Instantiates a new environment maker.
     *
     * The file is parsed and the environment created automatically.
     *
     * @param parser
     *            the parser to use for include instructions
     * @param res
     *            the url to parse, its name is used as name for the
     *            environment
     * @param parent
     *            the parent environment to rely upon
     *
     * @throws ParseException
     *             some parse error appeared
     * @throws ASTVisitException
     *             some error happened during ast traversal.
     * @throws IOException
     *             if the URL/file cannot be read successfully.
     */
    private EnvironmentMaker(Parser parser, URL res, Environment parent)
            throws ASTVisitException, ParseException, IOException {
        this(parser, parser.parseURL(res), res.toExternalForm(), parent);

    }

    /**
     * Instantiates a new environment maker.
     *
     * @param parser
     *            the parser to use for include instructions
     * @param astFile
     *            the ast structure to traverse
     * @param resource
     *            the url to store in the environment
     * @param parent
     *            the parent environment to rely upon
     *
     * @throws ASTVisitException
     *             some error happened during ast traversal.
     */
    public EnvironmentMaker(@NonNull Parser parser,
            @NonNull ASTFile astFile, @NonNull String resource,
            @NonNull Environment parent) throws ASTVisitException {
        this.parser = parser;

        try {
            this.env = new Environment(resource, parent);
        } catch (EnvironmentException e) {
            throw new ASTVisitException(e);
        }

        doIncludes(astFile);
        // first includes, then plugins!
        doPlugins(astFile);

        astFile.visit(new EnvironmentDefinitionVisitor(env));
        astFile.visit(new MapTypeDefinitionVisitor(env));
        astFile.visit(new EnvironmentTypingResolver(env));
        astFile.visit(new EnvironmentProgramMaker(env));
        astFile.visit(new EnvironmentRuleDefinitionVisitor(env));
        // call this after the EnvironmentProgramMaker
        problemSequents = new EnvironmentProblemExtractor(env).handle(astFile);
        proofScripts = new ProofScriptExtractor(parser, env).extractFrom(astFile);

        new RuleAxiomExtractor(env).extractAxioms();
    }

    /**
     * Instantiates a new environment maker.
     *
     * @param parser
     *            the parser to use for include instructions
     * @param astFile
     *            the ast structure to traverse
     * @param name
     *            the name of the environment
     *
     * @throws ASTVisitException
     *             some error happened during ast traversal.
     */
    public EnvironmentMaker(Parser parser, ASTFile astFile, String name)
            throws ASTVisitException {
        this(parser, astFile, name, Environment.BUILT_IN_ENV);
    }

    /*
     * register all defined plugins with the plugin manager.
     */
    private void doPlugins(ASTFile astFile) throws ASTVisitException {

        List<ASTPlugins> plugins = SelectList.select(ASTPlugins.class, astFile.getChildren());
        for (ASTPlugins block : plugins) {
            List<ASTPluginDeclaration> pairs =
                    SelectList.select(ASTPluginDeclaration.class, block.getChildren());
            for (ASTPluginDeclaration decl : pairs) {
                String classPath = null;
                if(decl.getClasspath() != null) {
                    classPath = Util.stripQuotes(decl.getClasspath().image);
                }
                String serviceName = decl.getServiceName().image;
                String implementation = Util.stripQuotes(decl.getImplementationClass().image);
                try {
                    env.getPluginManager().register(classPath, serviceName, implementation);
                } catch (EnvironmentException e) {
                    throw new ASTVisitException(decl, e);
                }
            }
        }

        try {
            env.registerPlugins();
        } catch (EnvironmentException e) {
            throw new ASTVisitException(e);
        }
    }

    /*
     * read all includes. The resulting environments are cascaded and set as the
     * parent to the currently built environment.
     */
    private void doIncludes(ASTFile astFile) throws ASTVisitException  {
        SelectList<ASTIncludeDeclarationBlock> includes =
                SelectList.select(ASTIncludeDeclarationBlock.class, astFile.getChildren());
        for (ASTIncludeDeclarationBlock block : includes) {
            for (Token token : block.getIncludeStrings()) {
                String filename = Util.stripQuotes(token.image);
                importedFilenames.add(filename);
                try {
                    assert astFile.getFileName() != null : "filename must be set (nullness-check)";
                    URL res = mkFile(astFile.getFileName(), filename);

                    if(env.hasParentResource(res.toExternalForm())) {
                        continue;
                    }

                    EnvironmentMaker includeMaker;
                    try {
                        includeMaker = new EnvironmentMaker(
                                parser, res, env.getParent());
                    } catch (ParseException e) {
                        throw new ASTVisitException("Error while parsing " + filename,
                                new ASTLocatedElement.Fixed(res.toExternalForm()), e);
                    }
                    Environment innerEnv = includeMaker.getEnvironment();
                    innerEnv.setFixed();
                    env.setParent(innerEnv);
                    // env.dumpResourceHierarchy();
                } catch (FileNotFoundException e) {
                    throw new ASTVisitException("Cannot include " + filename
                            + " (not found)", block, e);
                } catch (EnvironmentException e) {
                    throw new ASTVisitException(block, e);
                } catch (IOException e) {
                    throw new ASTVisitException(block, e);
                } catch (ASTVisitException e) {
                    throw new ASTVisitException(block, e);
                }
            }
        }
    }

    /**
     * Get the environment which has been created during the constructor call.
     *
     * @return the environment
     */
    public @NonNull Environment getEnvironment() {
        return env;
    }

    /**
     * Gets the collection of problem terms as specified in the source file.
     *
     * If the environment file does not specify any problems, the set of
     * programs defined in the environment is inspected to create problems from
     * them.
     *
     * Returns an empty map null if the environment does not define a problem
     * term and has no program.
     *
     * @return an unmodifiable map from names to sequents.
     * @see EnvironmentProblemExtractor
     */
    public @NonNull Map<String, Sequent> getProblemSequents() {
        return Collections.unmodifiableMap(problemSequents);
    }

    /**
     * Gets the collection of proof scripts that are included in this source files.
     *
     * Returns an empty map null if the environment does not define proof scripts.
     *
     * @return an unmodifiable map from names to proof scripts.
     * @see ProofScriptExtractor
     */
    public Map<String, ProofScript> getProofScripts() {
        return Collections.unmodifiableMap(proofScripts);
    }

    /*
     * Make file name for an include. Files with a leading $ are searched for in
     * the system directories.
     * The paths in SYS_DIR are then searched for the file.
     * If not found, a resource of that name is searched for.
     */
    private URL mkFile(String toplevel, String filename) throws IOException {

        if (filename.charAt(0) == '$') {
            String cleanfilename = filename.substring(1);

            // search in SYS_DIR
            String[] paths = SYS_DIR.split(File.pathSeparator);
            for (String path : paths) {
                File file = new File(path, cleanfilename);
                if(file.exists()) {
                    URL url = file.toURI().toURL();
                    return url;
                }
            }

            // then as resource, particularly for webstart.
            URL resource = getClass().getResource("/sys/" + cleanfilename);
            if(resource != null) {
                return resource;
            }

            // then fail
            Log.log(Log.DEBUG, filename + " not found in system directories: " + SYS_DIR);
            throw new FileNotFoundException(filename + " not found in any system directory");
        } else {
            URL topURL = new URL(toplevel);
            URL url = new URL(topURL, filename);
            return url;
        }
    }

    /**
     * Get a list containing all filenames that have been included directly into this
     * environment.
     *
     * It contains the strings verbatim, not resolved file names
     *
     * @return an immutable view on the list of included filenames.
     */
    public List<String> getImportedFilenames() {
        return Collections.unmodifiableList(importedFilenames);
    }

}