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
import de.uka.iti.pseudo.parser.program.ASTGotoStatement;
import de.uka.iti.pseudo.parser.program.ASTLabeledStatement;
import de.uka.iti.pseudo.parser.program.ASTSourceStatement;
import de.uka.iti.pseudo.parser.program.ASTStatement;
import de.uka.iti.pseudo.parser.program.ASTStatementList;
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
public class ProgramMaker extends ASTDefaultVisitor {

    private Environment env;
    private List<ASTStatement> rawStatements = new ArrayList<ASTStatement>();
    private List<Statement> statements = new ArrayList<Statement>();
    private List<SourceAnnotation> sourceAnnotations = new ArrayList<SourceAnnotation>();
    private List<LabelAnnotation> labelAnnotations = new ArrayList<LabelAnnotation>();
    private Map<String, Integer> labelMap = new HashMap<String, Integer>();
    
    public ProgramMaker(Environment env) {
        this.env = env;
    }

    public static Program makeProgram(ASTStatementList statementList, Environment env) 
                throws ASTVisitException {
        ProgramMaker programMaker = new ProgramMaker(env);
        programMaker.visit(statementList);
        programMaker.resolveLables();
        programMaker.makeStatements();
        
        Program program;
        try {
            program = new Program(programMaker.statements, 
                    programMaker.sourceAnnotations,
                    programMaker.labelAnnotations);
        } catch (EnvironmentException e) {
            throw new ASTVisitException(statementList, e);
        }
        
        return program;
    }

    private void resolveLables() throws ASTVisitException {
        for (ASTStatement ast : rawStatements) {
            if (ast instanceof ASTGotoStatement) {
                List<ASTTerm> targets = SelectList.select(ASTTerm.class, ast.getChildren());
                for (ASTTerm term : targets) {
                    if (term instanceof ASTIdentifierTerm) {
                        ASTIdentifierTerm id = (ASTIdentifierTerm) term;
                        String label = id.getSymbol().image;
                        Integer val = labelMap.get(label);
                        if(val == null)
                            throw new ASTVisitException("Unknown label in goto statement: " + label, ast);
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
            Statement statement = TermMaker.makeStatement(ast, env);
            if(detectSchemaVariables(statement))
                throw new ASTVisitException("Unallowed schema entity in statement", ast);
            statements.add(statement);
        }
    }
    
    protected void visitDefault(ASTElement arg) throws ASTVisitException {
    }
    
    private boolean detectSchemaVariables(Statement statement) {
        for (Term subterm : statement.getSubterms()) {
            if(TermUnification.containsSchemaIdentifier(subterm))
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

    public void visit(ASTLabeledStatement arg) throws ASTVisitException {
        String label = arg.getLabel().image;
        labelAnnotations.add(new LabelAnnotation(label, rawStatements.size()));
        super.visit(arg);
    }
    
    public void visit(ASTStatementList arg) throws ASTVisitException {
        for (ASTElement child : arg.getChildren()) {
            child.visit(this);
        }
    }

}
