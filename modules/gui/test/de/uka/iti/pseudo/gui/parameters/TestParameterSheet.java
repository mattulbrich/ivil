package de.uka.iti.pseudo.gui.parameters;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import javax.swing.JFrame;

import de.uka.iti.pseudo.TestCaseWithEnv;

public class TestParameterSheet extends TestCaseWithEnv {

    // TODO add test cases here
    
    public void testParameterSheet() throws Exception {
        ParameterTest pt = new ParameterTest();
        ParameterSheet ps = new ParameterSheet(ParameterTest.class, pt);

        // assertEquals(42, ps.callGetter("intval"));
    }
    
    public void testNoProperties() throws Exception {
        try {
            // typing test also
            ParameterSheet ps = new ParameterSheet(Object.class, "Hello");
            fail("Should have failed");
        } catch (FileNotFoundException e) {
            if(VERBOSE)
                e.printStackTrace();
        }
    }
    
    public static void main(String[] args) throws SecurityException, IllegalArgumentException, ClassCastException, IOException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException, ClassNotFoundException {
        ParameterTest pt = new ParameterTest();
        ParameterSheet ps = new ParameterSheet(ParameterTest.class, pt);
        
        JFrame f = new JFrame();
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.getContentPane().add(ps);
        f.pack();
        f.setVisible(true);
    }
    
}
