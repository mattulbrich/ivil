/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2012 Karlsruhe Institute of Technology
 *
 * The system is protected by the GNU General Public License.
 * See LICENSE.TXT (distributed with this file) for details.
 */

package de.uka.iti.pseudo.environment;

import java.util.Iterator;

import nonnull.NonNull;
import nonnull.Nullable;
import de.uka.iti.pseudo.proof.ProofNode;

/**
 * A LocalSymbolTable provides access to all locally defined symbols. Function,
 * binder, sort symbols and programs can be locally added. Addition and
 * retrieval functions are available. Like {@link Environment}s, local tables
 * can be fixed such that their contents can no longer be modified.
 *
 * Every proof node is furnished with a reference to a local symbol table. Each
 * child of a proof node either refers to the <em>same</em> local symbol table
 * or to an extension of it such that all symbols are available as linked lists.
 *
 * All local lists are kept as singly linked lists and heads are stored here
 * locally. Reuse of tails can be made since existing list chunks are never
 * changed.
 *
 * Since only relatively few symbols are introduced in the course of one path of
 * the proof, having only a linked list for name lookup does not cause too much
 * time overhead since this is only relevant for string parsing.
 *
 * TODO Rename SymbolTable
 *
 * @see Environment
 * @see ProofNode
 */
public final class LocalSymbolTable {

    /**
     * An empty local symbol table for reference. It has been fixed.
     */
    public static final LocalSymbolTable EMPTY;
    static {
        EMPTY = new LocalSymbolTable(Environment.BUILT_IN_ENV);
        EMPTY.setFixed();
    }

    /**
     * Node is used for the singly linked list.
     *
     * @param <T> the payload type
     */
    private static class Node<T extends Named> {
        // protected is to make life easier for compiler (no accessor functions needed)
        protected T entry;
        protected Node<T> next;
    }

    /**
     * NodeIterable provides means to iterate the tables.
     *
     * @param <T> the payload type
     */
    private static class NodeIterable<T extends Named> implements Iterable<T> {

        private final Node<T> head;

        public NodeIterable(Node<T> head) {
            this.head = head;
        }

        @Override
        public Iterator<T> iterator() {
            return new NodeIterator<T>(head);
        }

        @Override
        public int hashCode() {
            // taken from AbstractList
            int hashCode = 1;
            for (T e : this) {
                hashCode = 31*hashCode + (e==null ? 0 : e.hashCode());
            }
            return hashCode;
        }
    }

    /**
     * NodeIterator provides means to iterate the tables.
     *
     * @param <T> the payload type
     */
    private static class NodeIterator<T extends Named> implements Iterator<T> {

        private Node<T> node;

        public NodeIterator(Node<T> head) {
            this.node = head;
        }

        @Override
        public boolean hasNext() {
            return node != null;
        }

        @Override
        public T next() {
            T result = node.entry;
            node = node.next;
            return result;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }

    }

    /**
     * The environment to which this symbol table is an extension.
     */
    private final @NonNull Environment env;

    /*
     * These hold the headers to the symbol linked lists
     */
    private @Nullable Node<Function> functionHead;
    private @Nullable Node<Binder> binderHead;
    private @Nullable Node<Sort> sortHead;
    private @Nullable Node<Program> programHead;

    private boolean fixed;

    /**
     * Instantiates a new and empty local symbol table.
     * The table is not fixed.
     */
    public LocalSymbolTable(@NonNull Environment env) {
        this.env = env;
        this.fixed = false;
        // all headers point to null
    }

    /**
     * Instantiates a new local symbol table.
     *
     * The lists are copied from the argument.
     * The table is not fixed.
     *
     * @param lst
     *            the list to copy from
     */
    public LocalSymbolTable(@NonNull LocalSymbolTable lst) {
        this.env = lst.env;
        this.binderHead = lst.binderHead;
        this.functionHead = lst.functionHead;
        this.programHead = lst.programHead;
        this.sortHead = lst.sortHead;
        this.fixed = false;
    }

    /**
     * Set this local symbol table as fixed.
     *
     * After this, no new symbols may be added to it. A call to
     * {@link #addBinder(Binder)}, {@link #addFunction(Function)}, ... yields an
     * {@link EnvironmentException}.
     */
    public void setFixed() {
        fixed = true;
    }

    /**
     * Has this table already been fixed? Or may new symbols still be declared?
     *
     * @return true if this environment has been fixed and does not allow to add
     *         new symbols
     */
    public boolean isFixed() {
        return fixed;
    }

    /**
     * Get a table equivalent to this which is open.
     *
     * If this object is open, return it, otherwise create a fresh copy.
     *
     * @return the local symbol table
     */
    public LocalSymbolTable ensureOpenTable() {
        if (isFixed()) {
            return new LocalSymbolTable(this);
        } else {
            return this;
        }
    }

    /*
     * lookup the value for a name in a linked list given by node.
     */
    private <T extends Named> T find(Node<T> node, String name) {
        while (node != null) {
            if (name.equals(node.entry.getName())) {
                return node.entry;
            }
            node = node.next;
        }
        return null;
    }

    /**
     * Allow iteration over all locally declared function symbols.
     *
     * Iteration proceeds "newest symbol first".
     *
     * @return an iterator that traverses all local function symbols.
     */
    public @NonNull Iterable<Function> getFunctions() {
        return new NodeIterable<Function>(functionHead);
    }

    /**
     * Allow iteration over all locally declared binder symbols.
     *
     * Iteration proceeds "newest symbol first".
     *
     * @return an iterator that traverses all local binder symbols.
     */
    public @NonNull Iterable<Binder> getBinders() {
        return new NodeIterable<Binder>(binderHead);
    }

    /**
     * Allow iteration over all locally declared sort symbols.
     *
     * Iteration proceeds "newest symbol first".
     *
     * @return an iterator that traverses all local sort symbols.
     */
    public @NonNull Iterable<Sort> getSorts() {
        return new NodeIterable<Sort>(sortHead);
    }

    /**
     * Allow iteration over all locally declared programs.
     *
     * Iteration proceeds "newest symbol first".
     *
     * @return an iterator that traverses all local programs.
     */
    public @NonNull Iterable<Program> getPrograms() {
        return new NodeIterable<Program>(programHead);
    }

    /**
     * Gets a function for a name. Returns null if no symbol for the name can be
     * found in this table. The environment is not searched.
     *
     * @param name
     *            the name to look up
     *
     * @return a function with the name <code>name</code>, null if none found
     */
    public @Nullable Function getLocalFunction(String name) {
        return find(functionHead, name);
    }

    /**
     * Gets a function for a name. Returns null if no symbol for the name can be
     * found in this table or the environment to which it belongs.
     *
     * @param name
     *            the name to look up
     *
     * @return a function with the name <code>name</code>, null if none found
     */
    public @Nullable Function getFunction(String name) {
        Function result = getLocalFunction(name);
        if(result == null) {
            result = env.getFunction(name);
        }
        return result;
    }


    /**
     * Gets a binder for a name. Returns null if no symbol for the name can be
     * found in this table. The environment is not searched.
     *
     * @param name
     *            the name to look up
     *
     * @return a binder with the name <code>name</code>, null if none found
     */
    public @Nullable Binder getLocalBinder(String name) {
        return find(binderHead, name);
    }

    /**
     * Gets a binder for a name. Returns null if no symbol for the name can be
     * found in this table or the environment to which it belongs.
     *
     * @param name
     *            the name to look up
     *
     * @return a binder with the name <code>name</code>, null if none found
     */
    public @Nullable Binder getBinder(String name) {
        Binder result = getLocalBinder(name);
        if(result == null) {
            result = env.getBinder(name);
        }
        return result;
    }

    /**
     * Gets a sort for a name. Returns null if no symbol for the name can be
     * found in this table. The environment is not searched.
     *
     * @param name
     *            the name to look up
     *
     * @return a sort with the name <code>name</code>, null if none found
     */
    public @Nullable Sort getLocalSort(String name) {
        return find(sortHead, name);
    }

    /**
     * Gets a sort for a name. Returns null if no symbol for the name can be
     * found in this table or the environment to which it belongs.
     *
     * @param name
     *            the name to look up
     *
     * @return a sort with the name <code>name</code>, null if none found
     */
    public @Nullable Sort getSort(String name) {
        Sort result = getLocalSort(name);
        if(result == null) {
            result = env.getSort(name);
        }
        return result;
    }

    /**
     * Gets a program for a name. Returns null if no symbol for the name can be
     * found in this table. The environment is not searched.
     *
     * @param name
     *            the name to look up
     *
     * @return a program with the name <code>name</code>, null if none found
     */
    public @Nullable Program getLocalProgram(String name) {
        return find(programHead, name);
    }

    /**
     * Gets a program for a name. Returns null if no symbol for the name can be
     * found in this table or the environment to which it belongs.
     *
     * @param name
     *            the name to look up
     *
     * @return a program with the name <code>name</code>, null if none found
     */
    public @Nullable Program getProgram(String name) {
        Program result = getLocalProgram(name);
        if(result == null) {
            result = env.getProgram(name);
        }
        return result;
    }

    /**
     * Adds a function to this symbol table.
     *
     * It is added to the front of the according singly linked list.
     *
     * @param f
     *            the function symbol to add
     *
     * @throws EnvironmentException
     *             if a symbol of that name already exists or if the table has
     *             already been fixed.
     */
    public void addFunction(@NonNull Function f) throws EnvironmentException {
        if (isFixed()) {
            throw new EnvironmentException(
                    "cannot add to this local symbol table, it has been fixed already.");
        }

        String name = f.getName();
        Function existing = getFunction(name);

        if (existing != null) {
            throw new EnvironmentException("Function " + name
                    + " has already been defined at "
                    + existing.getDeclaration().getLocation());
        }

        functionHead = prepend(functionHead, f);
    }

    /**
     * Adds a sort to this symbol table.
     *
     * It is added to the front of the according singly linked list.
     *
     * @param s
     *            the sort symbol to add
     *
     * @throws EnvironmentException
     *             if a symbol of that name already exists or if the table has
     *             already been fixed.
     */
    public void addSort(@NonNull Sort s) throws EnvironmentException {

        if (isFixed()) {
            throw new EnvironmentException(
                    "cannot add to this local symbol table, it has been fixed already.");
        }

        String name = s.getName();
        Sort existing = getSort(name);

        if (existing != null) {
            throw new EnvironmentException("Sort " + name
                    + " has already been defined at "
                    + existing.getDeclaration().getLocation());
        }

        sortHead = prepend(sortHead, s);
    }

    /**
     * Adds a binder to this symbol table.
     *
     * It is added to the front of the according singly linked list
     *
     * @param b
     *            the binder symbol to add
     *
     * @throws EnvironmentException
     *             if a symbol of that name already exists or if the table has
     *             already been fixed.
     */
    public void addBinder(@NonNull Binder b) throws EnvironmentException {
        if (isFixed()) {
            throw new EnvironmentException(
                    "cannot add to this local symbol table, it has been fixed already.");
        }

        String name = b.getName();
        Binder existing = getBinder(name);

        if (existing != null) {
            throw new EnvironmentException("Binder " + name
                    + " has already been defined at "
                    + existing.getDeclaration().getLocation());
        }

        binderHead = prepend(binderHead, b);
    }

    /**
     * Adds a function to this symbol table.
     *
     * It is added to the front of the according singly linked list.
     *
     * @param p
     *            the function symbol to add
     *
     * @throws EnvironmentException
     *             if a symbol of that name already exists or if the table has
     *             already been fixed.
     */
    public void addProgram(@NonNull Program p) throws EnvironmentException {
        if (isFixed()) {
            throw new EnvironmentException(
                    "cannot add to this local symbol table, it has been fixed already.");
        }

        String name = p.getName();
        Program existing = getProgram(name);

        if (existing != null) {
            throw new EnvironmentException("Function " + name
                    + " has already been defined at "
                    + existing.getDeclaration().getLocation());
        }

        programHead = prepend(programHead, p);
    }

    /**
     * create a new symbol name which is not yet used.
     *
     * We append natural numbers starting with 1. The first one which is not yet
     * used is the candidate to choose.
     *
     * @param prefix
     *            the resulting function name will start with this prefix
     *
     * @return an identifier that can be used as a function name for this
     *         environment
     *
     * @see Environment#createNewFunctionName(String)
     */
    public String createNewFunctionName(String prefix) {
        String newName = prefix;

        for (int counter = 1; null != getFunction(newName); counter++) {
            newName = prefix + counter;
        }

        return newName;
    }

    /**
     * Gets a program name which starts with the given prefix and which has not
     * yet been bound in the environment.
     *
     * A number of 0 or more ticks (') are appended to make the name unique.
     *
     * @param prefix
     *            the prefix of the name to return.
     *
     * @return the fresh program name
     *
     * @see Environment#createNewProgramName(String)
     */
    public @NonNull String createNewProgramName(@NonNull String prefix) {
        while (getProgram(prefix) != null) {
            prefix += "'";
        }

        return prefix;
    }


    @Override
    public int hashCode() {
        return getFunctions().hashCode() ^
                getBinders().hashCode() ^
                getSorts().hashCode() ^
                getPrograms().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof LocalSymbolTable) {
            LocalSymbolTable lst = (LocalSymbolTable) obj;
            return env == lst.env
                && functionHead == lst.functionHead
                && binderHead == lst.binderHead
                && sortHead == lst.sortHead
                && programHead == lst.programHead;
        }
        return false;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("LocalSymbolTable[F:");
        append(sb, functionHead);
        sb.append(" B:");
        append(sb, binderHead);
        sb.append(" S:");
        append(sb, sortHead);
        sb.append(" P:");
        append(sb, programHead);
        sb.append("]");
        return sb.toString();
    }

    private void append(StringBuilder sb, Node<?> n) {
        while(n != null) {
            sb.append(n.entry.getName());
            n = n.next;
            if(n != null) {
                sb.append(",");
            }
        }
    }

//    /*
//     * check for equality (modulo ==) of two linked lists.
//     */
//    private boolean equalList(Node<?> n1, Node<?> n2) {
//        while(n1 != null & n2 != null) {
//            if(n1.entry != n2.entry) {
//                return false;
//            }
//            n1 = n1.next;
//            n2 = n2.next;
//        }
//        if(n1 != null || n2 != null) {
//            return false;
//        }
//        return true;
//    }

    private static <T extends Named> Node<T> prepend(Node<T> head, T symb) {
        Node<T> result = new Node<T>();
        result.entry = symb;
        result.next = head;
        return result;
    }

    /**
     * @return the env
     */
    public Environment getEnvironment() {
        return env;
    }

}
