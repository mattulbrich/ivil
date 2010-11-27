package de.uka.iti.pseudo.parser.boogie.environment;

import java.util.LinkedList;
import java.util.List;

import de.uka.iti.pseudo.parser.boogie.ASTElement;

/**
 * Objects of this class represent paths that can be used to infer types for
 * polymorphic typevariables. These paths are stored as integers, which are
 * equivalent to index in the getChild(index) operation.
 * 
 * @author timm.felden@felden.com
 * 
 */
public final class InferencePath {

    private final List<Integer> path;

    private InferencePath(List<Integer> path) {
        this.path = path;
    }

    UniversalType inferType(ASTElement node, final EnvironmentCreationState state) {

        UniversalType t = state.typeMap.get(node.getChildren().get(path.get(0)));

        for (int i = 1; i < path.size(); i++){
            if(t.domain.length!=0){
                t = t.domain[path.get(i)];
            } else {
                t = t.templateArguments[path.get(i)];
            }
        }

        return t;
    }

    static List<InferencePath> getPaths(UniversalType root, UniversalType target) {
        assert !root.isTypeVariable && target.isTypeVariable : "Type missmatch";

        LinkedList<Integer> path = new LinkedList<Integer>();
        LinkedList<InferencePath> rval = new LinkedList<InferencePath>();
        
        addChildren(root, target, path, rval);

        return rval;
    }

    private static void addChildren(UniversalType root, final UniversalType target, LinkedList<Integer> path,
            LinkedList<InferencePath> rval) {
        if (root == target) {
            rval.add(new InferencePath(new LinkedList<Integer>(path)));
        } else if (root.isTypeVariable) {
            return;
        } else if (root.domain.length != 0) {
            for (int i = 0; i < root.domain.length; i++) {
                path.push(i);
                addChildren(root.domain[i], target, path, rval);
                path.pop();
            }
        } else if (root.templateArguments.length != 0) {
            for (int i = 0; i < root.templateArguments.length; i++) {
                path.push(i);
                addChildren(root.templateArguments[i], target, path, rval);
                path.pop();
            }
        }
    }
}
