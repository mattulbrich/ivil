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

import checkers.nullness.quals.NonNull;
import checkers.nullness.quals.Nullable;
import de.uka.iti.pseudo.util.Util;

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
 * @see Environment
 * @see ProofNode
 */
public final class LocalSymbolTable {

    /**
     * An empty local symbol table for reference. It has been fixed.
     */
    public static final LocalSymbolTable EMPTY;
    static {
        EMPTY = new LocalSymbolTable();
        EMPTY.setFixed();
    }

    /**
     * Node is used for the singly linked list.
     *
     * @param <T> the payload type
     */
    private static class Node<T extends Named> {
        private T entry;
        private Node<T> next;
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
    public LocalSymbolTable() {
        // all headers point to null
        this.fixed = false;
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
     * Allow iteration over all declared function symbols.
     *
     * Iteration proceeds "newest symbol first".
     *
     * @return an iterator that traverses all local function symbols.
     */
    public @NonNull Iterable<Function> getFunctions() {
        return new NodeIterable<Function>(functionHead);
    }

    /**
     * Allow iteration over all declared binder symbols.
     *
     * Iteration proceeds "newest symbol first".
     *
     * @return an iterator that traverses all local binder symbols.
     */
    public @NonNull Iterable<Binder> getBinders() {
        return new NodeIterable<Binder>(binderHead);
    }

    /**
     * Allow iteration over all declared sort symbols.
     *
     * Iteration proceeds "newest symbol first".
     *
     * @return an iterator that traverses all local sort symbols.
     */
    public @NonNull Iterable<Sort> getSorts() {
        return new NodeIterable<Sort>(sortHead);
    }

    /**
     * Allow iteration over all declared programs.
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
     * found in this table.
     *
     * @param name
     *            the name to look up
     *
     * @return a function with the name <code>name</code>, null if none found
     */
    public @Nullable Function getFunction(String name) {
        return find(functionHead, name);
    }

    /**
     * Gets a binder for a name. Returns null if no symbol for the name can be
     * found in this table.
     *
     * @param name
     *            the name to look up
     *
     * @return a binder with the name <code>name</code>, null if none found
     */
    public @Nullable Binder getBinder(String name) {
        return find(binderHead, name);
    }

    /**
     * Gets a sort for a name. Returns null if no symbol for the name can be
     * found in this table.
     *
     * @param name
     *            the name to look up
     *
     * @return a sort with the name <code>name</code>, null if none found
     */
    public @Nullable Sort getSort(String name) {
        return find(sortHead, name);
    }

    /**
     * Gets a program for a name. Returns null if no symbol for the name can be
     * found in this table.
     *
     * @param name
     *            the name to look up
     *
     * @return a program with the name <code>name</code>, null if none found
     */
    public @Nullable Program getProgram(String name) {
        return find(programHead, name);
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
            return equalList(functionHead, lst.functionHead)
                && equalList(binderHead, lst.binderHead)
                && equalList(sortHead, lst.sortHead)
                && equalList(programHead, lst.programHead);
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

    /*
     * check for equality (modulo ==) of two linked lists.
     */
    private boolean equalList(Node<?> n1, Node<?> n2) {
        while(n1 != null & n2 != null) {
            if(n1.entry != n2.entry) {
                return false;
            }
            n1 = n1.next;
            n2 = n2.next;
        }
        if(n1 != null || n2 != null) {
            return false;
        }
        return true;
    }

    private static <T extends Named> Node<T> prepend(Node<T> head, T symb) {
        Node<T> result = new Node<T>();
        result.entry = symb;
        result.next = head;
        return result;
    }
}
