package de.uka.iti.pseudo.parser.boogie;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

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
            
            if(!context.equals("closable")){
                out.write("try{\n");
            }

            out.write("BPLParser.main(new String[] { \"" + path + "\"});\n");
            
            if(!context.equals("closable")){
                out.write("} catch(" + context + " ex){\n" +
                        "return;\n"+
                        "}\n"+
                        "assert false: \"expected "
                        + context + " to be trown\";");
            }

            out.write("}\n\n");

            System.out.println("Created test for " + path);

        } else if ((f = new File(path)).isDirectory())
            for (String sub : f.list())
                append(out, path + "/" + sub, context);

    }

    public static void main(String[] args) {
        final String PATH = "modules/boogie/test/de/uka/iti/pseudo/parser/boogie/TestBoogieParser.java";
        final String DATA = "modules/boogie/test/data";

        {
            File tests = new File(PATH);
            tests.delete();
        }
        try {
            FileWriter fstream = new FileWriter(PATH);
            BufferedWriter out = new BufferedWriter(fstream);

            out.write("package de.uka.iti.pseudo.parser.boogie;\n"
                    + "import de.uka.iti.pseudo.TestCaseWithEnv;\n"
                    + "import de.uka.iti.pseudo.parser.boogie.environment.*;\n"
                    + "public class TestBoogieParser extends TestCaseWithEnv {\n\n");

            // create tests for each context
            for (String context : new File(DATA).list())
                if (new File(DATA + "/" + context).isDirectory())
                    append(out, DATA + "/" + context, context);

            out.write("}");
            out.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
