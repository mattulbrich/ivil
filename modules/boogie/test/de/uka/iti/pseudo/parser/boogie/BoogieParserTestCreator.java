package de.uka.iti.pseudo.parser.boogie;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;

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

    private static String readFile(String path) throws IOException {
        FileInputStream stream = new FileInputStream(new File(path));
        try {
            FileChannel fc = stream.getChannel();
            MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
            /* Instead of using default, pass in a decoder. */
            return Charset.defaultCharset().decode(bb).toString();
        } finally {
            stream.close();
        }
    }

    // TODO impelement autoproving for tests
    private static void append(final BufferedWriter out, final String path, final String pattern) throws IOException {
        File f;
        if (path.endsWith(".bpl")) {
            out.write(pattern.replace("%%", path).replace("$$",
                    path.replace('/', '_').replace('-', '_').replace('.', '_')));
            System.out.println("Created test for " + path);

        } else if ((f = new File(path)).isDirectory())
            for (String sub : f.list())
                append(out, path + "/" + sub, pattern);

    }

    public static void main(String[] args) {
        final String PATH = "modules/boogie/test/de/uka/iti/pseudo/parser/boogie/GenTest";
        final String DATA = "modules/boogie/test/data";

        try {
            // create tests for each context
            for (String context : new File(DATA).list()) {
                if (!new File(DATA + "/" + context).isDirectory())
                    continue;

                final String path = PATH + context + ".java";
                {
                    File tests = new File(path);
                    tests.delete();
                }

                FileWriter fstream = new FileWriter(path);
                BufferedWriter out = new BufferedWriter(fstream);

                out.write("package de.uka.iti.pseudo.parser.boogie;\n" + "import junit.framework.TestCase;\n"
                        + "import de.uka.iti.pseudo.environment.boogie.EnvironmentCreationException;\n"
                        + "import de.uka.iti.pseudo.environment.boogie.TypeSystemException;\n" + "public class GenTest"
                        + context + " extends TestCase {\n\n");


                append(out, DATA + "/" + context, readFile(DATA + "/" + context + ".pattern"));


                out.write("}");
                out.close();

            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
