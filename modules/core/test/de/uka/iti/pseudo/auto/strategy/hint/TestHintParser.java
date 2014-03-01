package de.uka.iti.pseudo.auto.strategy.hint;

import java.util.List;

import de.uka.iti.pseudo.TestCaseWithEnv;
import de.uka.iti.pseudo.auto.strategy.HintStrategy;
import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.environment.EnvironmentException;

public class TestHintParser extends TestCaseWithEnv {

    public TestHintParser() throws Exception {
        this.env = new Environment("none:*mockHints*", env);
        this.env.getPluginManager().register(null,
                HintStrategy.PROOF_HINT_SERVICE_NAME,
                "de.uka.iti.pseudo.auto.strategy.hint.MockProofHint");
    }

    public void testSimpleHints() throws Exception {
        HintParser parser = new HintParser(env);

        List<HintRuleAppFinder> result = parser.parse("irrelevant text §mock between §mock trailing");
        assertEquals(2, result.size());
        assertEquals("[[mock], [mock]]", result.toString());
    }

    public void testSimpleUnknownHint() throws Exception {
        HintParser parser = new HintParser(env);

        try {
            parser.parse("§unknown");
            fail("should have thrown an EnvironmentExc");
        } catch (EnvironmentException e) {
            // this is intended
        }
    }

    public void testCompoundHint() throws Exception {
        HintParser parser = new HintParser(env);

        List<HintRuleAppFinder> result = parser.parse("before §(mock arg1 arg2   arg3) after");
        assertEquals("[[mock, arg1, arg2, arg3]]", result.toString());

        result = parser.parse("before §(mock  ) after");
        assertEquals("[[mock]]", result.toString());
    }

    // possibly a bug
    public void testEmptyCompound1() throws Exception {
        HintParser parser = new HintParser(env);

        List<HintRuleAppFinder> result = parser.parse("before §() after");
        assertEquals(0, result.size());
    }

    public void testSpacedCompound() throws Exception {
        HintParser parser = new HintParser(env);

        List<HintRuleAppFinder> result = parser.parse("before §( mock ) after");
        assertEquals("[[mock]]", result.toString());
    }

    public void testQuoted() throws Exception {
        HintParser parser = new HintParser(env);

        List<HintRuleAppFinder> result = parser.parse("before §(mock 'quoted string' unquoted semi' quoted') after");
        assertEquals("[[mock, quoted string, unquoted, semi quoted]]", result.toString());
    }

    public void testTwoHints() throws Exception {
        HintParser parser = new HintParser(env);

        List<HintRuleAppFinder> result = parser.parse("before §(mock 'quoted string') §(mock unquoted) after");
        assertEquals("[[mock, quoted string], [mock, unquoted]]", result.toString());
    }

}
