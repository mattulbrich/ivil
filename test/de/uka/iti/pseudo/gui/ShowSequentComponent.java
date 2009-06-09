package de.uka.iti.pseudo.gui;

import javax.swing.JFrame;
import javax.swing.WindowConstants;

import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.parser.term.TestTermParser;
import de.uka.iti.pseudo.term.Sequent;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.creation.TermMaker;

public class ShowSequentComponent {
    
    private static Environment env;

    static {
        try {
            env = TestTermParser.loadEnv();
        } catch (Exception e) {
            throw new Error(e);
        }
    }

    private static Term makeTerm(String string) throws Exception {
        return TermMaker.makeTerm(string, env);
    }

    public static void main(String[] args) throws Exception {
        JFrame f = new JFrame();
        SequentComponent sc = new SequentComponent(env);
        Sequent s = new Sequent(new Term[] { makeTerm("true"), makeTerm("[i1:=33] (\\forall x; x>5 & x < 100)") }, new Term[] { makeTerm("false") });
        sc.setSequent(s);
        f.getContentPane().add(sc);
        f.setSize(300, 300);
        //f.pack();
        f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        f.setVisible(true);
    }

}