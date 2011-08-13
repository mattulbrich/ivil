package de.uka.iti.pseudo.parser.boogie;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.environment.boogie.EnvironmentCreationState;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.util.Pair;

/**
 * Creates unit tests out of .bpl files in ./examples/boogie/test/*<br>
 * 
 * The direct subdirectory name tells the generator which type of Exception to
 * expect.<br>
 * 
 * The special subdircetory closable tells the generator, that the example is
 * intended to be automatically proven without human interaction.
 * 
 * @author timm.felden@felden.com
 * 
 *         NOTE: performance is not an issue here, as the generator is only run
 *         rarely to rebuild unittests
 */
public class BoogieParserTestCreator {

    // TODO impelement autoproving for tests
    private static void append(BufferedWriter out, String path, String context) throws IOException {
        File f;
        if (path.endsWith(".bpl")) {
            // create a test
            out.write("// generated test for " + path + "\n");

            out.write("public void testBoogieParse" + path.replace("/", "_").replace(".bpl", "").replace("-", "_")
                    + "() throws Exception {\n");
            
            if (!context.contains("valid")) {
                out.write("try{\n");
            }

            out.write("BPLParser.main(new String[] { \"" + path + "\"});\n");
            
            if (!context.contains("valid")) {
                out.write("} catch(" + context + " ex){\nreturn;\n}\nfail(\"expected " + context
                        + " to be trown\");\n");
            }

            out.write("}\n\n");

            System.out.println("Created test for " + path);

        } else if ((f = new File(path)).isDirectory())
            for (String sub : f.list())
                append(out, path + "/" + sub, context);

    }

    public static void main(String[] args) {
        final String PATH = "modules/boogie/test/de/uka/iti/pseudo/parser/boogie/TestFor";
        final String DATA = "modules/boogie/test/data";


        try {

            // create tests for each context
            for (String context : new File(DATA).list()) {
                final String path = PATH + context + ".java";
                {
                    File tests = new File(path);
                    tests.delete();
                }

                FileWriter fstream = new FileWriter(path);
            BufferedWriter out = new BufferedWriter(fstream);

            out.write("package de.uka.iti.pseudo.parser.boogie;\n" + "import de.uka.iti.pseudo.TestCaseWithEnv;\n"
                    + "import de.uka.iti.pseudo.environment.boogie.EnvironmentCreationException;\n"
                    + "import de.uka.iti.pseudo.environment.boogie.TypeSystemException;\n"
 + "public class TestFor"
                        + context + " extends TestCaseWithEnv {\n\n");

                if (new File(DATA + "/" + context).isDirectory())
                    append(out, DATA + "/" + context, context);

            out.write("}");
            out.close();

            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
