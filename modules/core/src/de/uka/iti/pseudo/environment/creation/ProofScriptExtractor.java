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
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nonnull.DeepNonNull;
import nonnull.NonNull;
import nonnull.Nullable;
import de.uka.iti.pseudo.auto.script.ProofScript;
import de.uka.iti.pseudo.auto.script.ProofScriptCommand;
import de.uka.iti.pseudo.auto.script.ProofScriptNode;
import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.environment.EnvironmentException;
import de.uka.iti.pseudo.environment.PluginManager;
import de.uka.iti.pseudo.environment.ProofObligation;
import de.uka.iti.pseudo.parser.ASTDefaultVisitor;
import de.uka.iti.pseudo.parser.ASTElement;
import de.uka.iti.pseudo.parser.ASTVisitException;
import de.uka.iti.pseudo.parser.ParseException;
import de.uka.iti.pseudo.parser.Parser;
import de.uka.iti.pseudo.parser.Token;
import de.uka.iti.pseudo.parser.file.ASTBinderDeclarationBlock;
import de.uka.iti.pseudo.parser.file.ASTFile;
import de.uka.iti.pseudo.parser.file.ASTFunctionDeclarationBlock;
import de.uka.iti.pseudo.parser.file.ASTIncludeDeclarationBlock;
import de.uka.iti.pseudo.parser.file.ASTLemmaDeclaration;
import de.uka.iti.pseudo.parser.file.ASTPlugins;
import de.uka.iti.pseudo.parser.file.ASTProblemSequent;
import de.uka.iti.pseudo.parser.file.ASTProgramDeclaration;
import de.uka.iti.pseudo.parser.file.ASTProperties;
import de.uka.iti.pseudo.parser.file.ASTRule;
import de.uka.iti.pseudo.parser.file.ASTSortDeclarationBlock;
import de.uka.iti.pseudo.parser.proof.ASTProofScript;
import de.uka.iti.pseudo.parser.proof.ASTProofScriptNode;
import de.uka.iti.pseudo.parser.proof.ASTProofSourceFile;
import de.uka.iti.pseudo.util.Triple;

/**
 * This is an AST visitor to extract proof scripts.
 *
 * All read proof scripts are stored together with their proof obligations or in
 * a map from their name if they are not associated with a proof obligation.
 */
class ProofScriptExtractor extends ASTDefaultVisitor {

    /**
     * The environment to operate on.
     */
    private final Environment env;

    /**
     * The plugin manager of the the environment.
     */
    private final PluginManager pluginManager;

    /**
     * The identifier for the last obligation that has been parsed.
     * <code>null</code> if the last element in the AST has no proof obligation.
     */
    private @Nullable String lastObligation = null;

    /**
     * The currently parsed node. Used as return value.
     */
    private ProofScriptNode node = null;

    /**
     * The parser.
     *
     * Needed to parse "proof source" include files.
     */
    private final Parser parser;

    /**
     * The proof scripts, mapped by name.
     */
    private final Map<String, ProofScript> proofScripts;

    /**
     * Instantiates a new proof script extractor.
     *
     * @param parser
     *            the parser to read .p files
     * @param env
     *            the environment to operate on
     * @param proofScripts
     *            the proof scripts map, mapped by name. Scripts will be added here
     * @throws ASTVisitException
     *             if parsing fails
     */
    public ProofScriptExtractor(@NonNull Parser parser, @NonNull Environment env,
            @DeepNonNull Map<String, ProofScript> proofScripts)
                    throws ASTVisitException  {
        this.parser = parser;
        this.env = env;
        this.proofScripts = proofScripts;

        try {
            this.pluginManager = env.getPluginManager();
        } catch (EnvironmentException e) {
            throw new ASTVisitException(e);
        }
    }

    /**
     * Extract proof scripts from the file included via "proof source".
     *
     * @param ast the ast
     * @throws ASTVisitException the aST visit exception
     */
    private void extractSourcedScripts(ASTProofSourceFile ast)
            throws ASTVisitException {
        try {
            URL topURL = new URL(ast.getFileName());
            URL url = new URL(topURL, ast.getPath());
            Reader reader = new InputStreamReader(url.openStream());
            List<ASTProofScript> astScripts = parser.parseProofScripts(reader, url.toString());
            for (ASTProofScript astProofScript : astScripts) {
                astProofScript.visit(this);
            }
        } catch (IOException e) {
            throw new ASTVisitException("Cannot parse proofs included via 'proof source'", ast, e);
        } catch (ParseException e) {
            throw new ASTVisitException("Cannot parse proofs included via 'proof source'", ast, e);
        }
    }

    @Override
    protected void visitDefault(ASTElement arg) throws ASTVisitException {
        assert false : "This visitor is not intended to reach this point: " + arg.getClass();
    }

    /*
     *
     */
    @Override
    public void visit(ASTProofScript arg) throws ASTVisitException {

        String obligationName;

        // no explicit obligation is given: refer to lastObligation
        if(arg.getName() == null) {
            if(lastObligation == null) {
                throw new ASTVisitException(
                        "A proof script without explict proof obligation can only be " +
                        "stated directly after a program, rule or lemma declaration.", arg);
            }
            obligationName = lastObligation;
        } else {
            obligationName = arg.getName();
        }

        arg.getChildren().get(0).visit(this);

        registerProofScript(arg, obligationName);

        lastObligation = null;
    }

    /**
     * Register a proof script.
     *
     * Either with a proof obligation or as an associated proof obligation.
     *
     * @param arg
     *            the proof script
     * @param obligationName
     *            the name
     * @throws ASTVisitException
     *             if name is already registered
     */
    private void registerProofScript(ASTProofScript arg, String obligationName)
            throws ASTVisitException {

        ProofScript po = proofScripts.get(obligationName);
        if(po != null) {
            throw new ASTVisitException("There is already a proof script for "
                    + obligationName, arg);
        }

        proofScripts.put(obligationName, new ProofScript(obligationName, node));

    }

    /*
     * parse a proof node.
     *
     * The empty () command gives a YIELD_COMMAND.
     */
    @Override
    public void visit(ASTProofScriptNode arg) throws ASTVisitException {

        Token commandToken = arg.getCommand();
        ProofScriptCommand command;

        if(commandToken == null) {
            command = ProofScriptCommand.YIELD_COMMAND;
        } else {

            String commandName = commandToken.image;
            try {
                command = pluginManager.getPlugin(ProofScriptCommand.SERVICE,
                        ProofScriptCommand.class, commandName);
            } catch (EnvironmentException e) {
                throw new ASTVisitException(arg, e);
            }
        }

        if(command == null) {
            throw new ASTVisitException("Unknown proof script command '" + commandToken + "'.",
                    arg);
        }

        Map<String, String> arguments = new HashMap<String, String>();
        int counter = 1;
        for (Triple<Token,Token,String> a : arg.getArguments()) {
            if(a.fst() == null) {
                // unnamed argument:
                arguments.put("#" + counter, a.trd());
                counter ++;
            } else {
                arguments.put(a.fst().image, a.trd());
            }
        }

        // perhaps do a syntax check on arguments.

        List<ProofScriptNode> children = new ArrayList<ProofScriptNode>();
        for (ASTElement child : arg.getChildren()) {
            child.visit(this);
            children.add(node);
        }

        node = new ProofScriptNode(command, arguments, children, arg);

    };

    /*
     * Handle an inlcusion via "proof source"
     */
    @Override
    public void visit(ASTProofSourceFile arg) throws ASTVisitException {
        // 'proof sourcefile "/path/filename.p"'
        // is interpreted as 'properties proof.sourcefile "/path/filename.p"'
        if (env.getLocalProperties().containsKey(ProofScript.PROOF_SOURCE_PROPERTY)) {
            throw new ASTVisitException("There is more than one 'proof source' directive", arg);
        }

        env.addProperty(ProofScript.PROOF_SOURCE_PROPERTY, arg.getPath());
        extractSourcedScripts(arg);
    }

    /*
     * Some blocks allow for a directly following proof.
     */

    /* (non-Javadoc)
     * @see de.uka.iti.pseudo.parser.ASTDefaultVisitor#visit(de.uka.iti.pseudo.parser.file.ASTFile)
     */
    @Override
    public void visit(ASTFile arg) throws ASTVisitException {
        for (ASTElement child : arg.getChildren()) {
            child.visit(this);
        }
    }

    /*
     * Some blocks allow for a directly following proof.
     */
    @Override
    public void visit(ASTLemmaDeclaration arg) throws ASTVisitException {
        // Only if the problem is named can we remember a named obligation.
        Token name = arg.getName();
        lastObligation = ProofObligation.LemmaPO.PREFIX + name;
    }

    @Override
    public void visit(ASTRule arg) throws ASTVisitException {
        lastObligation = ProofObligation.RulePO.PREFIX + arg.getName().image;
    }

    /*
    public void visit(ASTProgramDeclaration arg) throws ASTVisitException {
        lastObligation = ProofObligation.ProgramPO.PREFIX +
                arg.getName().image +
                ProofObligation.ProgramPO.SUFFIX_TOTAL;
    }*/

    /*
     * In all other cases, all I have to do is to forget about a last obligation.
     */
    @Override
    public void visit(ASTProgramDeclaration arg) throws ASTVisitException {
        lastObligation = null;
    }

    @Override
    public void visit(ASTProblemSequent arg) throws ASTVisitException {
        lastObligation = null;
    }

    @Override
    public void visit(ASTPlugins arg) throws ASTVisitException {
        lastObligation = null;
    }

    @Override
    public void visit(ASTIncludeDeclarationBlock arg) throws ASTVisitException {
        lastObligation = null;
    }

    @Override
    public void visit(ASTSortDeclarationBlock arg) throws ASTVisitException {
        lastObligation = null;
    }

    @Override
    public void visit(ASTFunctionDeclarationBlock arg) throws ASTVisitException {
        lastObligation = null;
    }

    @Override
    public void visit(ASTBinderDeclarationBlock arg) throws ASTVisitException {
        lastObligation = null;
    }

    @Override
    public void visit(ASTProperties arg) throws ASTVisitException {
        lastObligation = null;
    }
}
