package de.uka.iti.pseudo.environment.creation;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.uka.iti.pseudo.auto.script.ProofScript;
import de.uka.iti.pseudo.auto.script.ProofScriptCommand;
import de.uka.iti.pseudo.auto.script.ProofScriptNode;
import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.environment.EnvironmentException;
import de.uka.iti.pseudo.environment.PluginManager;
import de.uka.iti.pseudo.parser.ASTDefaultVisitor;
import de.uka.iti.pseudo.parser.ASTElement;
import de.uka.iti.pseudo.parser.ASTVisitException;
import de.uka.iti.pseudo.parser.ParseException;
import de.uka.iti.pseudo.parser.Parser;
import de.uka.iti.pseudo.parser.Token;
import de.uka.iti.pseudo.parser.file.ASTAxiomDeclaration;
import de.uka.iti.pseudo.parser.file.ASTBinderDeclarationBlock;
import de.uka.iti.pseudo.parser.file.ASTFile;
import de.uka.iti.pseudo.parser.file.ASTFunctionDeclarationBlock;
import de.uka.iti.pseudo.parser.file.ASTIncludeDeclarationBlock;
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

public class ProofScriptExtractor extends ASTDefaultVisitor {

    public static final String PROOF_SOURCE_PROPERTY = "proof.sourcefile";

    private final Environment env;
    private final PluginManager pluginManager;
    private Map<String, ProofScript> result;
    private String lastObligation = null;
    private ProofScriptNode node = null;

    private final Parser parser;

    public ProofScriptExtractor(Parser parser, Environment env) throws ASTVisitException  {
        this.parser = parser;
        this.env = env;
        try {
            this.pluginManager = env.getPluginManager();
        } catch (EnvironmentException e) {
            throw new ASTVisitException(e);
        }
    }

    public Map<String, ProofScript> extractFrom(ASTFile astFile) throws ASTVisitException {
        result = new HashMap<String, ProofScript>();
        astFile.visit(this);
        return result;
    }

    /*
     * Some blocks allow for a directly following proof.
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

    @Override
    public void visit(ASTProofScript arg) throws ASTVisitException {

        String obligationName;

        // no explicit obligation is given: refer to lastObligation
        if(arg.getName() == null) {
            if(lastObligation == null) {
                throw new ASTVisitException(
                        "A proof script without explict proof obligation can only be " +
                        "stated directly after a program, rule or problem declaration.", arg);
            }
            obligationName = lastObligation;
        } else {
            obligationName = arg.getName();
        }

        arg.getChildren().get(0).visit(this);

        if(result.containsKey(obligationName)) {
            throw new ASTVisitException("The proof script for " + obligationName +
                    " has already been defined earlier.", arg);
        }

        result.put(obligationName, new ProofScript(obligationName, node));

        lastObligation = null;
    }

    @Override
    public void visit(ASTProofScriptNode arg) throws ASTVisitException {

        Token commandToken = arg.getCommand();
        String command = commandToken.image;
        ProofScriptCommand plugin;
        try {
            plugin = pluginManager.getPlugin(ProofScriptCommand.SERVICE,
                    ProofScriptCommand.class, command);
        } catch (EnvironmentException e) {
            throw new ASTVisitException(arg, e);
        }

        if(plugin == null) {
            throw new ASTVisitException("Unknown proof script command '" + command + "'.",
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

        node = new ProofScriptNode(plugin, arguments, children, arg);

    };

    @Override
    public void visit(ASTProofSourceFile arg) throws ASTVisitException {
        // 'proof sourcefile "/path/filename.p"'
        // is interpreted as 'properties proof.sourcefile "/path/filename.p"'
        if (env.getLocalProperties().containsKey(PROOF_SOURCE_PROPERTY)) {
            throw new ASTVisitException("There is more than one 'proof source' directive or " +
                    "definitions of property " + PROOF_SOURCE_PROPERTY, arg);
        }

        env.addProperty(PROOF_SOURCE_PROPERTY, arg.getPath());
        extractSourcedScripts(arg);
    }

    /*
     * Some blocks allow for a directly following proof.
     */

    @Override
    public void visit(ASTFile arg) throws ASTVisitException {
        for (ASTElement child : arg.getChildren()) {
            child.visit(this);
        }
    }

    @Override
    public void visit(ASTProblemSequent arg) throws ASTVisitException {
        // Only if the problem is named can we remember a named obligation.
        Token problemId = arg.getIdentifier();
        if(problemId != null) {
            lastObligation = ProofScript.LEMMA_IDENTIFIER_PREFIX + problemId;
        } else {
            lastObligation = null;
        }

    }

    @Override
    public void visit(ASTRule arg) throws ASTVisitException {
        lastObligation = ProofScript.RULE_IDENTIFIER_PREFIX + arg.getName().image;
    }

    @Override
    public void visit(ASTProgramDeclaration arg) throws ASTVisitException {
        lastObligation = ProofScript.PROGRAM_IDENTIFIER_PREFIX + arg.getName().image;
    }

    /*
     * In all other cases, all I have to do is to forget about a last obligation.
     */
    @Override
    public void visit(ASTAxiomDeclaration arg) throws ASTVisitException {
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
