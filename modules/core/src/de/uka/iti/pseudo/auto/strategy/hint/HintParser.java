/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2012 Karlsruhe Institute of Technology
 *
 * The system is protected by the GNU General Public License.
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.auto.strategy.hint;

import java.io.IOException;
import java.io.PushbackReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import nonnull.DeepNonNull;
import nonnull.NonNull;

import de.uka.iti.pseudo.auto.strategy.HintStrategy;
import de.uka.iti.pseudo.auto.strategy.StrategyException;
import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.environment.EnvironmentException;

/**
 * The Class HintParser parses a string for potential proof hint annotations.
 * 
 * <h3>Proof hints</h3>
 * 
 * Hints are always initiated with a special character ({@link #INIT_CHAR},
 * followed by either an identifier or a list of space separated identifier in
 * parentheses. Extended identifiers containing spaces or any other character
 * can be quoted using 'single quotes'.
 * 
 * <h3>Returned objects</h3>
 */
public final class HintParser {

    /**
     * The initial character with which every hint begins.
     */
    public static final char INIT_CHAR = '§';

    /**
     * The environment in which we work.
     */
    private final Environment env;

    /**
     * Instantiates a new hint parser.
     * 
     * @param env
     *            the environment to work in
     */
    public HintParser(Environment env) {
        this.env = env;
    }

    /**
     * Parses hints from a string.
     * 
     * @param string
     *            the string to parse
     * @return a list of freshly created rule finders.
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     * @throws EnvironmentException
     *             if the plugin named by the hint cannot be found.
     * @throws StrategyException
     *             if the hint is unknown or malformed.
     */
    public @DeepNonNull
    List<HintRuleAppFinder> parse(@NonNull String string) throws IOException,
            EnvironmentException, StrategyException {
        return parse(new StringReader(string));
    }

    /**
     * Parses hints from a reader.
     * 
     * @param reader
     *            the reader to take characters from
     * @return a list of freshly created rule finders.
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     * @throws EnvironmentException
     *             if the plugin named by the hint cannot be found.
     * @throws StrategyException
     *             if the hint is unknown or malformed.
     */
    public @DeepNonNull
    List<HintRuleAppFinder> parse(@NonNull Reader reader) throws IOException,
            EnvironmentException, StrategyException {
        return parse(new PushbackReader(reader));
    }

    /**
     * Parses hints from a {@link PushbackReader}.
     * 
     * The reader must be capable to accomodate another character.
     * 
     * @param string
     *            the reader to take characters from.
     * @return a list of freshly created rule finders.
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     * @throws EnvironmentException
     *             if the plugin named by the hint cannot be found.
     * @throws StrategyException
     *             if the hint is unknown or malformed.
     */
    public List<HintRuleAppFinder> parse(PushbackReader reader)
            throws IOException, EnvironmentException, StrategyException {

        List<HintRuleAppFinder> result = new LinkedList<HintRuleAppFinder>();
        int c;

        while ((c = reader.read()) != -1) {
            if (c == INIT_CHAR) {
                HintRuleAppFinder h = parseHint(reader, result);
                if (h != null) {
                    result.add(h);
                }
            }
        }

        return result;
    }

    /**
     * The internal state for a HintParser.
     */
    private enum State {
        /**
         * The hint consists only of a single identifier.
         */
        SIMPLE, /**
         * Extended mode, i.e. within "(", ")". Not in quoted mode
         */
        NORMAL, /**
         * Extended mode, within a quoted string.
         */
        LITERAL
    };

    /*
     * Parses a hint.
     * 
     * This assumes that the {@link #INIT_CHAR} has been parsed and we continue
     * from that point on. Is a state machine using states from State.
     */
    private HintRuleAppFinder parseHint(PushbackReader reader,
            List<HintRuleAppFinder> hintList) throws IOException,
            EnvironmentException, StrategyException {
        int c = reader.read();

        State state;
        if (c == '(') {
            state = State.NORMAL;
        } else {
            state = State.SIMPLE;
            reader.unread(c);
        }

        List<String> parts = new ArrayList<String>();
        StringBuilder sb = new StringBuilder();
        while ((c = reader.read()) != -1) {
            switch (state) {
            case SIMPLE:
                if (Character.isLetterOrDigit((char) c) || c == '.') {
                    sb.append((char) c);
                } else {
                    reader.unread(c);
                    return makeHint(Collections.singletonList(sb.toString()));
                }
                break;

            case NORMAL:
                if (c == ')') {
                    if (sb.length() > 0) {
                        parts.add(sb.toString());
                    }
                    return makeHint(parts);
                } else if (c == '\'') {
                    state = State.LITERAL;
                } else if (Character.isSpaceChar((char) c)) {
                    if (sb.length() > 0) {
                        parts.add(sb.toString());
                        sb.setLength(0);
                    }
                } else {
                    sb.append((char) c);
                }
                break;

            case LITERAL:
                if (c == '\'') {
                    state = State.NORMAL;
                } else {
                    sb.append((char) c);
                }
                break;
            }
        }

        if (state != State.SIMPLE) {
            throw new StrategyException("Unclosed hint!");
        } else {
            return makeHint(Collections.singletonList(sb.toString()));
        }
    }

    /**
     * A hint has been parsed into a list of strings. Make a hint rule app
     * finder from that:
     * 
     * (1) Get the corresponding ProofHint plugin from the environment (2)
     * create the rule app finder from the plugin
     * 
     * @param list
     *            arguments to the hint
     * @return a fresh hint rule app finder
     * @throws EnvironmentException
     *             if plugin not found or exception in environment
     * @throws StrategyException
     *             if the creation failed.
     */
    private HintRuleAppFinder makeHint(List<String> list)
            throws EnvironmentException, StrategyException {
        if (list.isEmpty()) {
            return null;
        }

        String key = list.get(0);
        ProofHint hint = env.getPluginManager().getPlugin(
                HintStrategy.PROOF_HINT_SERVICE_NAME, ProofHint.class, key);

        if (hint == null) {
            throw new EnvironmentException(key
                    + " does not denote a proof hint");
        }

        return hint.createRuleAppFinder(env, list);
    }
}
