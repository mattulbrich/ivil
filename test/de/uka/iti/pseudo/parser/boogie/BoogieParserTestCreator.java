package de.uka.iti.pseudo.parser.boogie;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Creates unit tests out of .bpl files in ./examples/boogie/test/*<br>
 * <b>NOTE:</b> It looks as if some examples were intended to fail. This will
 * cause the Testcases to fail, although everything is fine. Maybe we can
 * implment a way to tell the creator which tests should fail and which
 * shouldnt.
 * 
 * @author timm.felden@felden.com
 * 
 */
public class BoogieParserTestCreator {

    private static void append(BufferedWriter out, String path) throws IOException {
        File f;
        if (path.endsWith(".bpl")) {
            // create a test
            out.write("// generated test for " + path + "\n");

            out.write("public void testBoogieParse" + path.replace("/", "_").replace(".bpl", "").replace("-", "_")
                    + "() throws Exception {\n");

            out.write("Parser.main(new String[] { \"" + path + "\"});\n");

            out.write("}\n\n");

            System.out.println("Created test for " + path);

        } else if ((f = new File(path)).isDirectory())
            for (String sub : f.list())
                append(out, path + "/" + sub);

    }

    public static void main(String[] args) {
        {
            File tests = new File("test/de/uka/iti/pseudo/parser/boogie/TestBoogieParser.java");
            tests.delete();
        }
        try {
            FileWriter fstream = new FileWriter("test/de/uka/iti/pseudo/parser/boogie/TestBoogieParser.java");
            BufferedWriter out = new BufferedWriter(fstream);

            out.write("package de.uka.iti.pseudo.parser.boogie;\n"
                    + "import de.uka.iti.pseudo.TestCaseWithEnv;\n"
                    + "public class TestBoogieParser extends TestCaseWithEnv {\n\n");

            append(out, "examples/boogie/test");

            out.write("}");
            out.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
