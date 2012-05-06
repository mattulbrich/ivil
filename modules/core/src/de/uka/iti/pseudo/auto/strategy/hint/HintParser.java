package de.uka.iti.pseudo.auto.strategy.hint;

import java.io.IOException;
import java.io.PushbackReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import de.uka.iti.pseudo.auto.strategy.StrategyException;
import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.environment.EnvironmentException;

// TODO DOC
public final class HintParser {

    public static final String PROOF_HINT_SERVICE_NAME = "proofHint";

    private final Environment env;

    public HintParser(Environment env) {
        this.env = env;
    }
    
    public List<HintRuleAppFinder> parse(String string) throws IOException, EnvironmentException, StrategyException {
        return parse(new StringReader(string));
    }

    public List<HintRuleAppFinder> parse(Reader reader) throws IOException, EnvironmentException, StrategyException {
        return parse(new PushbackReader(reader));
    }
    
    public List<HintRuleAppFinder> parse(PushbackReader reader) throws IOException, EnvironmentException, StrategyException {
        List<HintRuleAppFinder> result = new LinkedList<HintRuleAppFinder>();
        int c;

        while((c=reader.read()) != -1) {
            if(c == '§') {
                HintRuleAppFinder h = parseHint(reader, result);
                if(h != null) {
                    result.add(h);
                }
            }
        }
        
        return result;
    }
    
    private enum State { SIMPLE, NORMAL, LITERAL };

    private HintRuleAppFinder parseHint(PushbackReader reader, List<HintRuleAppFinder> hintList) throws IOException, EnvironmentException, StrategyException {
        int c = reader.read();
        
        State state;
        if(c == '(') {
            state = State.NORMAL;
        } else {
            state = State.SIMPLE;
            reader.unread(c);
        }

        List<String> parts = new ArrayList<String>();
        StringBuilder sb = new StringBuilder();
        while((c=reader.read()) != -1) {
            switch(state) {
            case SIMPLE:
                if(Character.isLetterOrDigit((char)c)) {
                    sb.append((char)c);
                } else {
                    reader.unread(c);
                    return makeHint(Collections.singletonList(sb.toString()));
                }
                break;

            case NORMAL:
                if(c == ')') {
                    if(sb.length() > 0) {
                        parts.add(sb.toString());
                    }
                    return makeHint(parts);
                } else if(c == '\'') {
                    state = State.LITERAL;
                } else if (Character.isSpaceChar((char)c)) {
                    if(sb.length() > 0) {
                        parts.add(sb.toString());
                        sb.setLength(0);
                    }
                } else {
                    sb.append((char)c);
                }
                break;
                
            case LITERAL:
                if(c == '\'') {
                    state = State.NORMAL;
                } else {
                    sb.append((char)c);
                }
                break;
            }
        }
        
        if(state != State.SIMPLE) {
            // TODO do this or ignore that last hint? IOException?
            throw new IllegalArgumentException("Unclosed hint!");
        } else {
            return makeHint(Collections.singletonList(sb.toString()));
        }
    }

    private HintRuleAppFinder makeHint(List<String> list) throws EnvironmentException, StrategyException {
        if(list.isEmpty()) {
            return null;
        }
        
        String key = list.get(0);
        ProofHint hint = env.getPluginManager().getPlugin(PROOF_HINT_SERVICE_NAME, ProofHint.class, key);
        
        if(hint == null) {
            throw new EnvironmentException(key + " does not denote a proof hint");
        }
        
        return hint.createRuleAppFinder(env, list);
    }
}