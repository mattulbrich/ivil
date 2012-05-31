package de.uka.iti.pseudo.gui.parameters;

import javax.swing.JFrame;

import de.uka.iti.pseudo.TestCaseWithEnv;
import de.uka.iti.pseudo.auto.strategy.SMTStrategy;
import de.uka.iti.pseudo.environment.Environment;

public class TestSMTParameterSheet extends TestCaseWithEnv {

    public static void main(String[] args) throws Exception {

        Environment env = makeEnv("rule rule1 closegoal tags decisionProcedure " +
        		"rule rule2 closegoal tags decisionProcedure ");

        SMTStrategy s = new SMTStrategy();
        // we can deal with null here ...
        s.init(null, env, null);
        ParameterSheet ps = new ParameterSheet(SMTStrategy.class, s);

        JFrame f = new JFrame();
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.getContentPane().add(ps);
        f.pack();
        f.setVisible(true);
    }

}
