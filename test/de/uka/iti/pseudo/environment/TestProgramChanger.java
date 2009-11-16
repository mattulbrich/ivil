package de.uka.iti.pseudo.environment;

import de.uka.iti.pseudo.TestCaseWithEnv;
import de.uka.iti.pseudo.term.statement.SkipStatement;
import de.uka.iti.pseudo.term.statement.Statement;

public class TestProgramChanger extends TestCaseWithEnv {
    
    private Environment locEnv;
    
    public TestProgramChanger() throws Exception {
        locEnv = makeEnv("include \"$int.p\" " 
                + "program P1 "
                + "assert 0>0 ; \"Statement 0\" "
                + "assert 1>0 ; \"Statement 1\" "
                + "assert 2>0 ; \"Statement 2\" "
                + "assert 3>0 ; \"Statement 3\" "
                + "assert 4>0 ; \"Statement 4\" ");
            
    }

    public void testInsert() throws Exception {
        Program P = locEnv.getProgram("P1");
        Statement s3 = P.getStatement(3);
        String a3 = P.getTextAnnotation(3);
        Statement s4 = P.getStatement(4);
        String a4 = P.getTextAnnotation(4);
        Statement skip = new SkipStatement();
        
        ProgramChanger pc = new ProgramChanger(P, locEnv);
        pc.insertAt(4, skip, "new");
        Program Pafter = pc.makeProgram("P");
        
        // Pafter.dump();
        
        assertEquals(s3, Pafter.getStatement(3));
        assertEquals(skip, Pafter.getStatement(4));
        assertEquals(s4, Pafter.getStatement(5));
        
        assertEquals(a3, Pafter.getTextAnnotation(3));
        assertEquals("new", Pafter.getTextAnnotation(4));
        assertEquals(a4, Pafter.getTextAnnotation(5));
    }
    
    public void testDelete() throws Exception {
        Program P = locEnv.getProgram("P1");
        Statement s3 = P.getStatement(3);
        String a3 = P.getTextAnnotation(3);
        Statement s5 = P.getStatement(5);
        String a5 = P.getTextAnnotation(5);
        
        ProgramChanger pc = new ProgramChanger(P, locEnv);
        pc.deleteAt(4);
        Program Pafter = pc.makeProgram("P");
        
        // Pafter.dump();
        
        assertEquals(s3, Pafter.getStatement(3));
        assertEquals(s5, Pafter.getStatement(4));
        
        assertEquals(a3, Pafter.getTextAnnotation(3));
        assertEquals(a5, Pafter.getTextAnnotation(4)); 
    }
    
    public void testReplace() throws Exception {
        Program P = locEnv.getProgram("P1");
        Statement skip = new SkipStatement();
        
        ProgramChanger pc = new ProgramChanger(P, locEnv);
        pc.replaceAt(4, skip, "new");
        Program Pafter = pc.makeProgram("P");
        
        assertEquals(skip, Pafter.getStatement(4));
        assertEquals("new", Pafter.getTextAnnotation(4)); 
    }
    
    public void testGotoUpdate() throws Exception {
        Environment env = makeEnv("include \"$int.p\" " +
                "program Q " +
                "goto 3 " +
                "goto 0 " +
                "goto 6 " +
                "goto 0 " +
                "goto 5");
        
        Program Q = env.getProgram("Q");
        ProgramChanger pc = new ProgramChanger(Q, env);
        pc.insertAt(3, new SkipStatement());
        pc.deleteAt(3);
        Program Q1 = pc.makeProgram("Q1");
        
        //Q1.dump();
        
        assertEquals(Q.countStatements(), Q1.countStatements());
        for (int i = 0; i < Q.countStatements(); i++) {
            assertEquals(Q.getStatement(i), Q1.getStatement(i));
        }
    }
    
    // from a bug
    public void testUpdateInsert() throws Exception {
        Environment env = makeEnv("include \"$int.p\" " +
                "program Q " +
                "skip " +
                "goto 0 ");
        
        Program Q = env.getProgram("Q");
        ProgramChanger pc = new ProgramChanger(Q, env);
        pc.insertAt(0, new SkipStatement());
        Program Q1 = pc.makeProgram("Q1");
        
        // Q1.dump();
        
        assertEquals(3, Q1.countStatements());
        assertEquals("goto 0", Q1.getStatement(2).toString());
    }
    
    
}
