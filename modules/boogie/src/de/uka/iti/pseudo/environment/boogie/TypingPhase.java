package de.uka.iti.pseudo.environment.boogie;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.uka.iti.pseudo.environment.Sort;
import de.uka.iti.pseudo.parser.boogie.ASTVisitException;
import de.uka.iti.pseudo.parser.boogie.ast.ProcedureDeclaration;
import de.uka.iti.pseudo.parser.boogie.ast.VariableDeclaration;

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
    final Map<ProcedureDeclaration, List<VariableDeclaration>> modifiable = new HashMap<ProcedureDeclaration, List<VariableDeclaration>>();

    void create(EnvironmentCreationState state) throws TypeSystemException {
        try {
            new TypeMapBuilder(state);
        } catch (ASTVisitException e) {

            // this exception is expected
	    // TODO consider giving as cause to TypeSystemException for better debug-ability
            throw new TypeSystemException("TypeMap creation failed because of " + e.toString(), e);

        }

        // make sure we did not forget something
        assert state.scopeMap.size() == state.typeMap.size() || state.printDebugInformation() : "found "
                + (state.scopeMap.size() - state.typeMap.size()) + " untyped ASTElements";

        // TODO this should not be needed
        // new TypeChecker(state);

        // ensure correctness of modifies
        new ModifiesChecker(state);

        // create sorts
        // {
        // // create builtin, int and bool are allways defined in the built-in
        // // environment, so dont add them
        // sortMap.put("int", null);
        // sortMap.put("bool", null);
        // sortMap.put("bitvector", new Sort("bitvector", 0, state.root));
        //
        // for (String name : state.names.typeSpace.keySet()) {
        // if (sortMap.containsKey(name) && (!name.equals("bitvector") ||
        // sortMap.containsKey("utt_bitvector")) )
        // continue;
        //
        // ASTElement decl = state.names.typeSpace.get(name);
        // UniversalType t = state.typeMap.get(decl);
        //
        // if (null != t.aliasname) // type synonyms dont need a sort
        // continue;
        //
        // sortMap.put(name, new Sort("utt_" + name, t.templateArguments.length,
        // decl));
        // }
        //
        // for (Sort s : sortMap.values()) {
        // try {
        // if (s != null && !s.getName().equals("bitvector"))
        // state.env.addSort(s);
        // } catch (EnvironmentException e) {
        // e.printStackTrace();
        // }
        // }
        //
        // sortMap.put("int", state.env.getSort("int"));
        // sortMap.put("bool", state.env.getSort("bool"));
        // }
    }

}
