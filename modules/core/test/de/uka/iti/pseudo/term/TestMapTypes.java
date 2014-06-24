package de.uka.iti.pseudo.term;

import java.io.StringReader;

import de.uka.iti.pseudo.TestCaseWithEnv;
import de.uka.iti.pseudo.parser.ASTVisitException;
import de.uka.iti.pseudo.parser.ParseException;
import de.uka.iti.pseudo.parser.Parser;
import de.uka.iti.pseudo.parser.file.ASTFile;

public class TestMapTypes extends TestCaseWithEnv {

    // was a bug - could not be parsed
    public void testMapApplicationOnTerm() throws Exception {
        makeEnv("sort S as [int]int\n" +
        		"function S map assignable\n" +
        		"lemma L1 ({map := map}map)[5] = 5");
    }

    public void testMapDeclarations() throws Exception {
        makeEnv("sort S T as [S]S");
        // Self referencing is ok
        makeEnv("sort S as [S]S");

        makeEnv("sort R as {'a}['a]'a");
        makeEnv("sort U('a) as ['a]int");

        try {
            makeEnv("sort V as ['a]int");
            fail("should be V('a)");
        } catch (ASTVisitException ex) {
            out(ex);
            // assertNull("Must be a cause-less exception", ex.getCause());
        }

        try {
            makeEnv("sort S sort T as [%'a]S");
            fail("Should not be allowed");
        } catch (ASTVisitException ex) {
            out(ex);
            assertNull("Must be a cause-less exception", ex.getCause());
        }

    }

    // reveiled bugs
    public void testHeapAsMap() throws Exception {

        env = makeEnv("sort Ref\n" +
        		"  Field('a)\n" +
        		"  Heap as {'a}[Ref, Field('a)]'a\n" +
        		"function Heap h\n" +
        		"  Ref r" +
        		"  Field(int) fi" +
        		"  Field(bool) fb");
        // Some terms
        Term t, t2;
        t = makeTerm("h[r,fi] as int");
        t = makeTerm("h[r,fb] as bool");
        t = makeTerm("h[r,fi := 5] as Heap");
        t = makeTerm("h[r,fi := 5][r,fi]");
        t2 = makeTerm("(h[r,fi := 5])[r,fi]");
        assertEquals(t, t2);
        String s = t.toString(false);
        assertEquals("$load_Heap($store_Heap(h,r,fi,5),r,fi)", s);
    }

    private ASTFile parseFile(String s) throws ParseException {
        Parser fp = new Parser();
        return fp.parseFile(new StringReader(s), "*test*");
    }

    public void testParserCases() throws Exception {
        try {
            parseFile("sort S sort T as {%'a}[S]S");
            fail("Should not be allowed");
        } catch (ParseException e) {
            out(e);
        }
    }

    public void testArgumentAssignmentInPrograms() throws Exception {
        env = makeEnv("sort m as [int]int    function m x assignable " +
        		"program P   x:=x[4:=4] " +
        		"program Q   x[4]:=4");

        assertEquals(
                env.getProgram("P").getStatement(0),
                env.getProgram("Q").getStatement(0));
    }

    /*   seems we cannot implement at the moment ...
    public void testMatchingInRules() throws Exception {

        env = makeEnv("sort m as [int]int " +
                "rule R find %m[%i]=0 & (%m as m) = %m replace true");
    }
    */
}
