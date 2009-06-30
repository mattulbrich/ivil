package de.uka.iti.pseudo.environment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.uka.iti.pseudo.parser.ASTDefaultVisitor;
import de.uka.iti.pseudo.parser.ASTElement;
import de.uka.iti.pseudo.parser.ASTVisitException;
import de.uka.iti.pseudo.parser.ParserConstants;
import de.uka.iti.pseudo.parser.Token;
import de.uka.iti.pseudo.parser.file.ASTProgramDeclaration;
import de.uka.iti.pseudo.parser.program.ASTGotoStatement;
import de.uka.iti.pseudo.parser.program.ASTLabelStatement;
import de.uka.iti.pseudo.parser.program.ASTSourceStatement;
import de.uka.iti.pseudo.parser.program.ASTStatement;
import de.uka.iti.pseudo.parser.term.ASTIdentifierTerm;
import de.uka.iti.pseudo.parser.term.ASTNumberLiteralTerm;
import de.uka.iti.pseudo.parser.term.ASTTerm;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.creation.TermMaker;
import de.uka.iti.pseudo.term.creation.TermUnification;
import de.uka.iti.pseudo.term.statement.Statement;
import de.uka.iti.pseudo.util.SelectList;
import de.uka.iti.pseudo.util.Util;

// TODO DOC
public class EnvironmentProgramMaker extends ASTDefaultVisitor {

    private Environment env;
    private List<ASTStatement> rawStatements = new ArrayList<ASTStatement>();
    private List<Statement> statements = new ArrayList<Statement>();
    private List<SourceAnnotation> sourceAnnotations = new ArrayList<SourceAnnotation>();
    private Map<String, Integer> labelMap = new HashMap<String, Integer>();
    
    public EnvironmentProgramMaker(Environment env) {
        this.env = env;
    }

    private void resolveLabels() throws ASTVisitException {
        for (ASTStatement ast : rawStatements) {
            if (ast instanceof ASTGotoStatement) {
                List<ASTTerm> targets = SelectList.select(ASTTerm.class, ast.getChildren());
                for (ASTTerm term : targets) {
                    if (term instanceof ASTIdentifierTerm) {
                        ASTIdentifierTerm id = (ASTIdentifierTerm) term;
                        String label = id.getSymbol().image;
                        Integer val = labelMap.get(label);
                        if(val == null)
                            throw new ASTVisitException("Unknown label in goto statement: " + label, id);
                        ast.replaceChild(term, new ASTNumberLiteralTerm(mkToken(val)));
                    }
                }
            }
        }
    }

    private Token mkToken(Integer val) {
        Token t = new Token();
        t.image = val.toString();
        t.kind = ParserConstants.NATURAL;
        return t;
    }

    private void makeStatements() throws ASTVisitException {
        for (ASTStatement ast : rawStatements) {
            Statement statement = TermMaker.makeAndTypeStatement(ast, env);
            if(detectSchemaVariables(statement))
                throw new ASTVisitException("Unallowed schema entity in statement", ast);
            statements.add(statement);
        }
    }
    
    protected void visitDefault(ASTElement arg) throws ASTVisitException {
        for (ASTElement child : arg.getChildren()) {
            child.visit(this);
        }
    }
    
    private boolean detectSchemaVariables(Statement statement) {
        for (Term subterm : statement.getSubterms()) {
            if(TermUnification.containsSchemaVariables(subterm))
                return true;
        }
        return false;
    }

    protected void visitDefaultStatement(ASTStatement arg) throws ASTVisitException {
        rawStatements.add(arg);
    }
    
    public void visit(ASTSourceStatement arg) throws ASTVisitException {
        String argument = Util.stripQuotes(arg.getArgumentToken().image);
        sourceAnnotations.add(new SourceAnnotation(argument, rawStatements.size()));
    }

    public void visit(ASTLabelStatement arg) throws ASTVisitException {
        String label = arg.getLabel().image;
        if(labelMap.containsKey(label)) {
            throw new ASTVisitException("The label " + label + " has already been defined earlier", arg);
        }
        labelMap.put(label, rawStatements.size());
    }
    
    public void visit(ASTProgramDeclaration arg) throws ASTVisitException {
        
        rawStatements.clear();
        statements.clear();
        sourceAnnotations.clear();
        labelMap.clear();
        
        for (ASTElement child : arg.getChildren()) {
            child.visit(this);
        }
        
        resolveLabels();
        makeStatements();
        
        try {
            String name = arg.getName().image;
            Program program = new Program(name, statements, 
                    sourceAnnotations, arg);
            env.addProgram(program);
        } catch (EnvironmentException e) {
            throw new ASTVisitException(arg, e);
        }
    }

}
