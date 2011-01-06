package de.uka.iti.pseudo.environment.boogie;

import java.util.Comparator;
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
public final class InferencePath implements Comparable<InferencePath> {

    static final class PathListComparator implements Comparator<List<InferencePath>> {

        /**
         * requires arguments to be sorted themselves.
         */
        @Override
        public int compare(List<InferencePath> o1, List<InferencePath> o2) {
            return o1.get(0).compareTo(o2.get(0));
        }

    }
    
    static public final PathListComparator listComparator = new PathListComparator();

    private final List<Integer> path;
    private final UniversalType variable;

    private InferencePath(List<Integer> path, UniversalType variable) throws TypeSystemException {
        this.path = path;
        this.variable = variable;

        if (path.get(0) == -1)
            throw new TypeSystemException(
                    "You can not safely infer type parameters from the range of an polymorphic type.");
    }

    UniversalType inferType(ASTElement node, final EnvironmentCreationState state) throws TypeSystemException {

        assert (path.get(0) != -1);

        UniversalType t = state.typeMap.get(node.getChildren().get(path.get(0)));

        try {
        for (int i = 1; i < path.size(); i++){
            if (t.domain.length != 0 || t.range != null) {
                if (path.get(i) == -1)
                    t = t.range;
                else
                    t = t.domain[path.get(i)];
            } else {
                t = t.templateArguments[path.get(i)];
            }
        }
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new TypeSystemException("\n" + node.getLocation()
                    + "\tyou supplied an incompatible type argument, so the type of this map cannot be inferred");
        }

        return t;
    }

    @Override
    public boolean equals(Object t) {
        if (!(t instanceof InferencePath))
            return false;

        InferencePath p = (InferencePath) t;

        if (path.size() != p.path.size())
            return false;

        for (int i = 0; i < path.size(); i++)
            if (!path.get(i).equals(p.path.get(i)))
                return false;

        return true;
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
        if (root == target || root.name.equals(target.name)) {
            try {
                rval.add(new InferencePath(new LinkedList<Integer>(path), target));
            } catch (TypeSystemException e) {
                // dont do anything, this only happens if illegal paths would
                // have been added
            }
        } else if (root.isTypeVariable) {
            return;
        } else if (root.domain.length != 0) {
            for (int i = 0; i < root.domain.length; i++) {
                path.add(i);
                addChildren(root.domain[i], target, path, rval);
                path.removeLast();
            }
            path.add(-1);
            addChildren(root.range, target, path, rval);
            path.removeLast();
        } else if (root.templateArguments.length != 0) {
            for (int i = 0; i < root.templateArguments.length; i++) {
                path.add(i);
                addChildren(root.templateArguments[i], target, path, rval);
                path.removeLast();
            }
        }
    }

    @Override
    public int compareTo(InferencePath p) {

        // its impossible, that one path is a prefix of another, so its safe to
        // compare to the end of the shorter path
        for (int i = 0; i < path.size() && i < p.path.size(); i++) {
            if (path.get(i).intValue() != p.path.get(i).intValue())
                return path.get(i) < p.path.get(i) ? -1 : 1;
        }

        if (path.size() == p.path.size())
            return 0;
        else
            return path.size() < p.path.size() ? -1 : 1;
    }

    public UniversalType getVariable() {
        return variable;
    }
}
