//package de.uka.iti.pseudo.environment.boogie;
//
//import java.util.Comparator;
//import java.util.LinkedList;
//import java.util.List;
//
//import de.uka.iti.pseudo.environment.boogie.MapTypeDatabase.UnfoldedMap;
//import de.uka.iti.pseudo.term.Type;
//import de.uka.iti.pseudo.term.TypeVariable;
//
///**
// * Objects of this class represent paths that can be used to infer types for
// * polymorphic typevariables. These paths are stored as integers, which are
// * equivalent to index in the getChild(index) operation.
// * 
// * @author timm.felden@felden.com
// * 
// */
//public final class InferencePath implements Comparable<InferencePath> {
//
//    static final class PathListComparator implements Comparator<List<InferencePath>> {
//
//        /**
//         * requires arguments to be sorted themselves.
//         */
//        @Override
//        public int compare(List<InferencePath> o1, List<InferencePath> o2) {
//            return o1.get(0).compareTo(o2.get(0));
//        }
//
//    }
//
//    static public final PathListComparator listComparator = new PathListComparator();
//
//    private final List<Integer> path;
//
//    private final TypeVariable tvar;
//
//    private InferencePath(List<Integer> path, TypeVariable tvar) {
//        this.path = path;
//        this.tvar = tvar;
//    }
//
//    static LinkedList<InferencePath> getPaths(UnfoldedMap root, TypeVariable target, MapTypeDatabase mapDB) {
//
//        LinkedList<Integer> path = new LinkedList<Integer>();
//        LinkedList<InferencePath> rval = new LinkedList<InferencePath>();
//
//        addChildren(root, target, path, rval, mapDB);
//
//        return rval;
//    }
//
//    private static void addChildren(Type root, final TypeVariable target, LinkedList<Integer> path,
//            LinkedList<InferencePath> rval, MapTypeDatabase mapDB) {
//        if (root.equals(target)) {
//            rval.add(new InferencePath(new LinkedList<Integer>(path), target));
//        } else if (root instanceof TypeVariable) {
//            return;
//        } else if (root instanceof UnfoldedMap || mapDB.hasType(root)) {
//            UnfoldedMap m = (UnfoldedMap) (root instanceof UnfoldedMap ? root : mapDB.getUnfoldedMap(root));
//
//            for (int i = 0; i < m.domain.length; i++) {
//                path.add(i);
//                addChildren(m.domain[i], target, path, rval, mapDB);
//                path.removeLast();
//            }
//            path.add(-1);
//            addChildren(m.range, target, path, rval, mapDB);
//            path.removeLast();
//        }
//    }
//
//    @Override
//    public int compareTo(InferencePath p) {
//        // its impossible, that one path is a prefix of another, so its safe to
//        // compare to the end of the shorter path
//        for (int i = 0; i < path.size() && i < p.path.size(); i++) {
//            if (path.get(i).intValue() != p.path.get(i).intValue())
//                return path.get(i) < p.path.get(i) ? -1 : 1;
//        }
//
//        if (path.size() == p.path.size())
//            return 0;
//        else
//            return path.size() < p.path.size() ? -1 : 1;
//    }
//
//    @Override
//    public boolean equals(Object t) {
//        if (!(t instanceof InferencePath))
//            return false;
//
//        InferencePath p = (InferencePath) t;
//
//        if (path.size() != p.path.size())
//            return false;
//
//        for (int i = 0; i < path.size(); i++)
//            if (!path.get(i).equals(p.path.get(i)))
//                return false;
//
//        return true;
//    }
//
//    public TypeVariable getTypeVariable() {
//        return tvar;
//    }
//
//    @Override
//    public String toString() {
//        StringBuilder b = new StringBuilder("< ");
//        for (Integer i : path) {
//            b.append(i);
//            b.append(' ');
//        }
//        b.append('>');
//
//        return b.toString();
//
//    }
//
// }
