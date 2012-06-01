package de.uka.iti.ivil.jbc.util;

import java.util.Random;

import junit.framework.TestCase;

public class EscapingTests extends TestCase {

    public void name() throws Exception {
        String data = "Zahl, ∀";
        assertTrue("legal build output", EscapeName.build(data).matches("[a-zA-Z][a-zA-Z0-9]*"));
        assertEquals("invertability", data, EscapeName.revert(EscapeName.build(data)));
    }

    public void nameRandom() throws Exception {
        char[] d = new char[32];
        Random rand = new Random();
        for (int i = 0; i < d.length; i++)
            d[i] = (char) rand.nextInt();

        String data = new String(d);

        assertTrue("legal build output", EscapeName.build(data).matches("[a-zA-Z][a-zA-Z0-9]*"));
        assertEquals("invertability", data, EscapeName.revert(EscapeName.build(data)));
    }

    public void program() throws Exception {
        String pack = "foo.bla", cls = "C";

        assertEquals("translation", "foo_bla_C_f__int__int",
                EscapeProgram.build(pack, cls, "f", new String[] { "int", "int" }));
        assertEquals("translation", "foo_bla_C_f__int", EscapeProgram.build(pack, cls, "f", new String[] { "int" }));

        String[] sig = new String[] { "int", "int", "boolean" };
        assertEquals("translation", "foo_bla_C_g__int__int__boolean", EscapeProgram.build(pack, cls, "g", sig));

        EscapeProgram.Info info = EscapeProgram.revert(EscapeProgram.build(pack, cls, "g", sig));

        assertEquals("reverse pack", info.pack, pack);
        assertEquals("reverse class", info.className, cls);
        assertEquals("reverse function", info.methodName, "g");

        for (int i = 0; i < sig.length; i++)
            assertEquals("reverse signature " + i, info.signature[i], sig[i]);
    }
}
