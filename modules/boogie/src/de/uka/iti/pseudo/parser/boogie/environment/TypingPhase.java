package de.uka.iti.pseudo.parser.boogie.environment;

import java.util.HashMap;
import java.util.Map;

import de.uka.iti.pseudo.environment.EnvironmentException;
import de.uka.iti.pseudo.environment.Sort;
import de.uka.iti.pseudo.parser.boogie.ASTElement;
import de.uka.iti.pseudo.parser.boogie.ASTVisitException;

/**
 * Extracts type information out of an AST and creates sorts in the output
 * Environment.
 * 
 * 
 * @author timm.felden@felden.com
 * 
 */
public final class TypingPhase {

    final Map<String, Sort> sortMap = new HashMap<String, Sort>();

    void create(EnvironmentCreationState state) throws TypeSystemException {
        try {
            new TypeMapBuilder(state);
        } catch (ASTVisitException e) {

            // this exception is expected
            throw new TypeSystemException("TypeMap creation failed because of " + e.toString());

        }

        // make sure we did not forget something
        assert state.scopeMap.size() == state.typeMap.size() || state.printDebugInformation() : "found "
                + (state.scopeMap.size() - state.typeMap.size()) + " untyped ASTElements";

        new TypeChecker(state);

        // create sorts for nonmap types
        {
            // create builtin, int and bool are allways defined in the built-in
            // environment, so dont add them
            sortMap.put("int", null);
            sortMap.put("bool", null);
            sortMap.put("bitvector", new Sort("bitvector", 0, state.root));

            for (String name : state.names.typeSpace.keySet()) {
                if (sortMap.containsKey(name))
                    continue;

                ASTElement decl = state.names.typeSpace.get(name);
                UniversalType t = state.typeMap.get(decl);

                if (null != t.aliasname) // type synonyms dont need a sort
                    continue;

                sortMap.put(name, new Sort("utt_" + name, t.templateArguments.length, decl));
            }

            for (Sort s : sortMap.values()) {
                try {
                    if (s != null && !s.getName().equals("bitvector"))
                        state.env.addSort(s);
                } catch (EnvironmentException e) {
                    e.printStackTrace();
                }
            }

            sortMap.put("int", state.env.getSort("int"));
            sortMap.put("bool", state.env.getSort("bool"));
        }

        // create a mapping from table types to ivil types
        try {
            new TypeTranslator(state);

        } catch (ASTVisitException e) {
            e.printStackTrace();
            throw new TypeSystemException("Type translation failed because of:\n" + e.toString());
        }
    }

}
