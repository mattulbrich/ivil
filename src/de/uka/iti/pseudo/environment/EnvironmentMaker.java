/*
 * This file is part of PSEUDO
 * Copyright (C) 2009 Universitaet Karlsruhe, Germany
 *    written by Mattias Ulbrich
 * 
 * The system is protected by the GNU General Public License. 
 * See LICENSE.TXT for details.
 */

package de.uka.iti.pseudo.environment;

import java.io.File;
import java.io.FileNotFoundException;

import nonnull.NonNull;
import nonnull.Nullable;
import de.uka.iti.pseudo.gui.Main;
import de.uka.iti.pseudo.parser.ASTVisitException;
import de.uka.iti.pseudo.parser.ParseException;
import de.uka.iti.pseudo.parser.Parser;
import de.uka.iti.pseudo.parser.Token;
import de.uka.iti.pseudo.parser.file.ASTFile;
import de.uka.iti.pseudo.parser.file.ASTIncludeDeclarationBlock;
import de.uka.iti.pseudo.parser.term.ASTTerm;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.creation.TermMaker;
import de.uka.iti.pseudo.term.creation.TermUnification;
import de.uka.iti.pseudo.util.SelectList;
import de.uka.iti.pseudo.util.Util;

/**
 * The Class EnvironmentMaker traverses an {@link ASTFile} to extract
 * information about sors, functions, binders, fix operators, etc. etc.
 */
public class EnvironmentMaker {

    /**
     * the directory where to search for system include files.
     */
    private static File SYS_DIR = new File(Main.SYSTEM_DIRECTORY);

    /**
     * The environment that is being built.
     */
    private Environment env;

    /**
     * The problem term possibly found in the {@link ASTFile}
     */
    private @Nullable Term problemTerm;

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
     * @throws FileNotFoundException
     *             the file to parse does not exist
     * @throws ParseException
     *             some parse error appeared
     * @throws ASTVisitException
     *             some error happened during ast traversal.
     */
    public EnvironmentMaker(Parser parser, File file)
    throws FileNotFoundException, ParseException, ASTVisitException {
        this(parser, file, Environment.BUILT_IN_ENV);
    }

    /**
     * Instantiates a new environment maker.
     * 
     * The file is parsed and the environment created automatically.
     * 
     * @param parser
     *            the parser to use for include instructions
     * @param file
     *            the file to parse, its name is used as name for the
     *            environment
     * @param parent
     *            the parent environment to rely upon
     * 
     * @throws FileNotFoundException
     *             the file to parse does not exist
     * @throws ParseException
     *             some parse error appeared
     * @throws ASTVisitException
     *             some error happened during ast traversal.
     */
    private EnvironmentMaker(Parser parser, File file, Environment parent)
    throws FileNotFoundException, ASTVisitException, ParseException {
        this(parser, parser.parseFile(file), file.getPath(), parent);

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
     * @param parent
     *            the parent environment to rely upon
     * 
     * @throws ASTVisitException
     *             some error happened during ast traversal.
     */
    private EnvironmentMaker(@NonNull Parser parser,
            @NonNull ASTFile astFile, @NonNull String name,
            @NonNull Environment parent) throws ASTVisitException {
        this.parser = parser;
        this.env = new Environment(name, parent);

        doIncludes(astFile);

        astFile.visit(new EnvironmentDefinitionVisitor(env));
        astFile.visit(new EnvironmentTypingResolver(env));
        astFile.visit(new EnvironmentRuleDefinitionVisitor(env));

        doProblem(astFile);
    }



    /*
     * convert the AST term problem description to a real term object.
     */
    private void doProblem(ASTFile astFile) throws ASTVisitException {
        ASTTerm term = astFile.getProblemTerm();
        if(term != null) {
            problemTerm = TermMaker.makeTerm(term, env);

            if(TermUnification.containsSchemaIdentifier(problemTerm))
                throw new ASTVisitException("Problem term contains schema identifier", term);
        }

    }

    private void doIncludes(ASTFile astFile) throws ASTVisitException  {
        // TODO method documentation
        SelectList<ASTIncludeDeclarationBlock> includes = SelectList.select(ASTIncludeDeclarationBlock.class, astFile.getChildren());
        for (ASTIncludeDeclarationBlock block : includes) {
            for (Token token : block.getIncludeStrings()) {
                String filename = Util.stripQuotes(token.image);
                File file = mkFile(astFile.getFileName(), filename);
                
                if(env.hasParentResource(file.getPath()))
                    continue;
                
                try {
                    EnvironmentMaker includeMaker = new EnvironmentMaker(
                            parser, file, env.getParent());
                    env.setParent(includeMaker.getEnvironment());
                } catch (FileNotFoundException e) {
                    throw new ASTVisitException("Cannot include " + file
                            + " (not found)", block, e);
                } catch (ParseException e) {
                    throw new ASTVisitException("Error while parsing file "
                            + file, block, e);
                } catch (EnvironmentException e) {
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
     * Make file name for an include. Leading $ is replaced by the system
     * directory.
     */
    private File mkFile(String toplevel, String filename) {
        File ret;
        if (filename.charAt(0) == '$') {
            ret = new File(SYS_DIR, filename.substring(1));
        } else {
            ret = new File(new File(toplevel).getParentFile(), filename);
        }
        return ret;
    }

}