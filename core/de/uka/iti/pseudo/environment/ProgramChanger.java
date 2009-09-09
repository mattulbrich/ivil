package de.uka.iti.pseudo.environment;

import java.io.File;
import java.math.BigInteger;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import de.uka.iti.pseudo.parser.ASTLocatedElement;
import de.uka.iti.pseudo.term.Application;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.TermException;
import de.uka.iti.pseudo.term.statement.GotoStatement;
import de.uka.iti.pseudo.term.statement.Statement;

// TODO DOC
// TODO write tests
public class ProgramChanger {
    
    private LinkedList<Statement> statements;
    private Environment env;
    private File sourceFile;


    public ProgramChanger(Program program, Environment env) {
        this.env = env;
        this.sourceFile = program.getSourceFile();
        this.statements = new LinkedList<Statement>(program.getStatements());
    }


    public void insertAt(int index, Statement statement) throws TermException {
        updateTargets(index, +1);
        statements.add(index, statement);
    }
    
    public void replaceAt(int index, Statement statement) throws TermException {
        statements.set(index, statement);
    }

    public void deleteAt(int index) throws TermException {
        updateTargets(index + 1, -1);
        statements.remove(index);
    }
    
    private void updateTargets(int index, int offset) throws TermException {
        updateGotoStatements(index, offset);
    }


    private void updateGotoStatements(int index, int offset) throws TermException {
        ListIterator<Statement> it = statements.listIterator();
        while(it.hasNext()) {
            Statement statement = it.next();
            if (statement instanceof GotoStatement) {
                GotoStatement gotoSt = (GotoStatement) statement;
                int srcLine = gotoSt.getSourceLineNumber();
                Term[] newTargets = updateGotoStatement(index, offset, gotoSt);
                if(newTargets != null) {
                    it.set(new GotoStatement(srcLine, newTargets));
                }
            }
        }
    }


    private Term[] updateGotoStatement(int index, int offset,
            GotoStatement gotoSt) throws TermException {
        List<Term> orgTargets = gotoSt.getSubterms();
        Term[] newTargets = null;
        for(int i = 0; i < gotoSt.countSubterms(); i++) {
            int val = toInt(orgTargets.get(i));
            if(val >= index) {
                if(newTargets == null) {
                    newTargets = new Term[orgTargets.size()];
                }
                newTargets[i] = fromInt(val + offset);
            }
        }
        return newTargets;
    }


    private Term fromInt(int number) throws TermException {
        Function f = env.getNumberLiteral(BigInteger.valueOf(number));
        return new Application(f, Environment.getIntType(), new Term[0]);
    }


    private int toInt(Term term) throws TermException {
        if (term instanceof Application) {
            Application appl = (Application) term;
            Function f = appl.getFunction();
            if (f instanceof NumberLiteral) {
                NumberLiteral literal = (NumberLiteral) f;
                return literal.getValue().intValue();
            }
        }
        throw new TermException("The term " + term + " is not a number literal");
    }


    public Program makeProgram(String name) throws EnvironmentException {
        
        Program p = new Program(name, sourceFile, statements, ASTLocatedElement.BUILTIN);
        return p;
    }

}
