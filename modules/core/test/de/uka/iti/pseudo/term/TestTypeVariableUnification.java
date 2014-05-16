package de.uka.iti.pseudo.term;

import de.uka.iti.pseudo.TestCaseWithEnv;
import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.environment.LocalSymbolTable;
import de.uka.iti.pseudo.parser.ASTVisitException;
import de.uka.iti.pseudo.parser.ParseException;
import de.uka.iti.pseudo.term.creation.TermMaker;

public class TestTypeVariableUnification extends TestCaseWithEnv {

    private Type makeType(String string) throws ASTVisitException, ParseException {
        return TermMaker.makeType(string, new LocalSymbolTable(env));
    }

    public void testTypeVariableUnification1() throws Exception {
        TypeVariableUnification tvu = new TypeVariableUnification();
        tvu.unify(makeType("poly(int, 'a)"), makeType("poly('b, bool)"));
        assertEquals(Environment.getBoolType(), tvu.getMap().get(TypeVariable.ALPHA));
        assertEquals(Environment.getIntType(), tvu.getMap().get(TypeVariable.BETA));
    }


    public void testTypeVariableUnification2() throws Exception {
        TypeVariableUnification tvu = new TypeVariableUnification();
        tvu.unify(makeType("'a"), makeType("bool"));
        try {
            tvu.unify(makeType("'a"), makeType("int"));
            fail("Should have failed");
        } catch (UnificationException e) {
            out(e);
        }
        tvu.unify(makeType("int"), makeType("'b"));
        assertEquals(Environment.getBoolType(), tvu.getMap().get(TypeVariable.ALPHA));
        assertEquals(Environment.getIntType(), tvu.getMap().get(TypeVariable.BETA));
    }

    public void testTypeVariableUnification3() throws Exception {
        TypeVariableUnification tvu = new TypeVariableUnification();
        try {
            tvu.unify(makeType("poly('a,'a)"), makeType("poly(int, bool)"));
            fail("Should have failed");
        } catch (UnificationException e) {
            out(e);
        }
    }

    public void testSchemaTypes() throws Exception {

        TypeVariableUnification tvu = new TypeVariableUnification();

        try {
            tvu.unify(makeType("set(%'a)"), makeType("set('a)"));
            fail("Should have failed");
        } catch(TermException e) {
            out(e);
        }

        try {
            tvu.unify(makeType("set('a)"), makeType("set(%'a)"));
            fail("Should have failed");
        } catch(TermException e) {
            out(e);
        }

    }

}
