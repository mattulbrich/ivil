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

package de.uka.iti.pseudo.environment.creation;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import nonnull.NonNull;
import nonnull.Nullable;
import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.environment.EnvironmentException;
import de.uka.iti.pseudo.parser.ASTVisitException;
import de.uka.iti.pseudo.parser.ParseException;
import de.uka.iti.pseudo.parser.Parser;
import de.uka.iti.pseudo.parser.Token;
import de.uka.iti.pseudo.parser.file.ASTFile;
import de.uka.iti.pseudo.parser.file.ASTIncludeDeclarationBlock;
import de.uka.iti.pseudo.parser.file.ASTPluginDeclaration;
import de.uka.iti.pseudo.parser.file.ASTPlugins;
import de.uka.iti.pseudo.parser.term.ASTTerm;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.TermException;
import de.uka.iti.pseudo.term.creation.TermMaker;
import de.uka.iti.pseudo.term.creation.TermMatcher;
import de.uka.iti.pseudo.term.creation.ToplevelCheckVisitor;
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
    private static final String SYS_DIR =
        Settings.getInstance().getExpandedProperty("pseudo.sysDir", "./sys");

    /**
     * The environment that is being built.
     */
    private Environment env;

    /**
     * The problem term possibly found in the {@link ASTFile}
     */
    private @Nullable Term problemTerm;

    /**
     * remember the list of included files. This is not stored in the
     * environment can only be retrieved here.
     */
    private List<String> importedFilenames = new ArrayList<String>();

    /**
     * The parser to use to parse include files
     */
    private Parser parser;

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
     * @throws MalformedURLException
     */
    public EnvironmentMaker(Parser parser, File file)
    throws ParseException, ASTVisitException, MalformedURLException, IOException {
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
     * @throws MalformedURLException
     */
    public EnvironmentMaker(Parser parser, URL url)
    throws ParseException, ASTVisitException, MalformedURLException, IOException {
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
     * @param resource
     *            the url to parse, its name is used as name for the environment
     * 
     * @throws ParseException
     *             some parse error appeared
     * @throws ASTVisitException
     *             some error happened during ast traversal.
     * @throws IOException
     *             if the URL/file cannot be read successfully.
     * @throws MalformedURLException
     */
    public EnvironmentMaker(Parser parser, InputStream stream, URL resource)
    throws ParseException, ASTVisitException, MalformedURLException, IOException {
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

        new RuleAxiomExtractor(env).extractAxioms();

        doProblem(astFile);
    }

    /*
     * convert the AST term problem description to a real term object.
     */
    private void doProblem(ASTFile astFile) throws ASTVisitException {
        ASTTerm term = astFile.getProblemTerm();
        if(term != null) {
            problemTerm = TermMaker.makeTerm(term, env);

            if(TermMatcher.containsSchemaVariables(problemTerm))
                throw new ASTVisitException("Problem term contains schema identifier", term);

            try {
                problemTerm.visit(new ToplevelCheckVisitor());
            } catch (TermException e) {
                throw new ASTVisitException(term, e);
            }
        }

    }

    /*
     * register all defined plugins with the plugin manager.
     */
    private void doPlugins(ASTFile astFile) throws ASTVisitException {

        List<ASTPlugins> plugins = SelectList.select(ASTPlugins.class, astFile.getChildren());
        for (ASTPlugins block : plugins) {
            List<ASTPluginDeclaration> pairs = SelectList.select(ASTPluginDeclaration.class, block.getChildren());
            for (ASTPluginDeclaration decl : pairs) {
                String serviceName = decl.getServiceName().image;
                String implementation = Util.stripQuotes(decl.getImplementationClass().image);
                try {
                    env.getPluginManager().register(serviceName, implementation);
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

    private void doIncludes(ASTFile astFile) throws ASTVisitException  {
        // TODO method documentation
        SelectList<ASTIncludeDeclarationBlock> includes = SelectList.select(ASTIncludeDeclarationBlock.class, astFile.getChildren());
        for (ASTIncludeDeclarationBlock block : includes) {
            for (Token token : block.getIncludeStrings()) {
                String filename = Util.stripQuotes(token.image);
                importedFilenames.add(filename);
                try {
                    URL res = mkFile(astFile.getFileName(), filename);

                    if(env.hasParentResource(res.toExternalForm())) {
                        // System.err.println("WARNING: cyclicly including environments, involving: " + file);
                        continue;
                    }

                    EnvironmentMaker includeMaker = new EnvironmentMaker(
                            parser, res, env.getParent());
                    Environment innerEnv = includeMaker.getEnvironment();
                    innerEnv.setFixed();
                    env.setParent(innerEnv);
                    // env.dumpResourceHierarchy();
                } catch (FileNotFoundException e) {
                    throw new ASTVisitException("Cannot include " + filename
                            + " (not found)", block, e);
                } catch (ParseException e) {
                    throw new ASTVisitException("Error while parsing file "
                            + filename, block, e);
                } catch (EnvironmentException e) {
                    throw new ASTVisitException(block, e);
                } catch (IOException e) {
                    throw new ASTVisitException(block, e);
                }
            }
        }
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

    /**
     * Get the environment which has been created during the constructor call
     *
     * @return the environment
     */
    public @NonNull Environment getEnvironment() {
        return env;
    }

    /**
     * Gets the problem term if there is any in the environment.
     *
     * Returns null if the environment does not define a problem term.
     *
     * @return the problem term, possibly null
     */
    public @Nullable Term getProblemTerm() {
        return problemTerm;
    }

    /*
     * Make file name for an include. Files with a leading $ are searched for in
     * the system directories.
     * The paths in SYS_DIR are then searched for the file.
     * If not found, a resource of that name is searched for.
     */
    private URL mkFile(String toplevel, String filename) throws IOException {

        if (filename.charAt(0) == '$') {
            filename = filename.substring(1);

            // search in SYS_DIR
            String[] paths = SYS_DIR.split(File.pathSeparator);
            for (String path : paths) {
                File file = new File(path, filename);
                if(file.exists()) {
                    URL url = file.toURI().toURL();
                    return url;
                }
            }

            // then as resource, particularly for webstart. 
            URL resource = getClass().getResource("/sys/" + filename);
            if(resource != null)
                return resource;

            // then fail
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