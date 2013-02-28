/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2012 Karlsruhe Institute of Technology
 *
 * The system is protected by the GNU General Public License.
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.auto;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import nonnull.NonNull;
import nonnull.Nullable;

/**
 * The Class SExpr captures so called "s-expressions" which are a representation of a syntax tree using parentheses. It
 * well-known from its usage in Lisp. SMT-Lib uses them as well.
 *
 * This is a mutable data structure which can be exported as Strings.
 * Caution must be paid to not accidently create cyclic descriptions! No checks are performed.
 *
 * An SExpr without children is exported without parentheses.
 *
 * @see SMTLib2Translator
 */
final class SExpr implements Iterable<SExpr> {

    /**
     * The children expressions, i.e. the arguments.
     * May be null for no children
     */
    private @Nullable List<SExpr> children;

    /**
     * The "tag" string is the content
     */
    private final @NonNull String content;

    /**
     * Instantiates a new s-expression with the given tag name.
     * @param content the content to use for the tag
     */
    public SExpr(@NonNull String content) {
        this.content = content;
    }

    /**
     * Instantiates a new s-expression with an empty tag.
     */
    public SExpr() {
        this("");
    }

    /**
     * Instantiates a new s-expression with an empty tag and the argument as only child.
     *
     * @param child
     *            the child to add
     */
    public SExpr(@NonNull SExpr child) {
        this("");
        add(child);
    }

    /**
     * Gets the tag content of this s-expression.
     * @return the tag
     */
    public @NonNull String getContent() {
        return content;
    }

    /**
     * Adds an argument s-expression. The expression is created from the string and is without children-
     *
     * @param string
     *            the string to create an expression from
     * @return the {@code this} reference (for chaining)
     */
    public SExpr add(@NonNull String string) {
        return add(new SExpr(string));
    }

    /**
     * Adds an argument s-expression.
     *
     * @param expr
     *            the expression to add as a child
     * @return the {@code this} reference (for chaining)
     */
    public SExpr add(@NonNull SExpr expr) {
        if(children == null) {
            children = new LinkedList<SExpr>();
        }
        children.add(expr);
        return this;
    }

    /**
     * Gets the i-th child.
     *
     * If the index is invalid, an exception will be thrown.
     *
     * @param idx
     *            a valid index ({@code 0 <= i < size()}
     * @return the i-th argument to the expression
     * @throws IndexOutOfBoundsException if the index is out of bounds
     */
    public SExpr get(int idx) {
        if(children == null) {
            throw new IndexOutOfBoundsException();
        }
        return children.get(idx);
    }

    /**
     * Gets the size of the s-expression, i.e. the number of children.
     *
     * @return a number {@literal >= 0}
     */
    public int size() {
        if(children == null) {
            return 0;
        } else {
            return children.size();
        }
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public @NonNull String toString() {
        if(size() == 0) {
            return content;
        } else {
            StringBuilder sb = new StringBuilder();
            appendTo(sb);
            return sb.toString();
        }
    }

    /*
     * Append my string to the string buffer. This makes toString more
     * efficient.
     */
    private void appendTo(StringBuilder buf) {
        if(size() == 0) {
            buf.append(content);
        } else {
            buf.append("(").append(content);
            boolean passedFirst = false;
            for (SExpr child : children) {
                if(passedFirst || content.length() > 0) {
                    buf.append(" ");
                }
                child.appendTo(buf);
                passedFirst = true;
            }
            buf.append(")");
        }
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        int result = content.hashCode();
        if(children != null) {
            for (SExpr child : children) {
                result = (result * 3) | child.hashCode();
            }
        }
        return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof SExpr) {
            SExpr sexpr = (SExpr) obj;
            if(!content.equals(sexpr.content)) {
                return false;
            }

            int size = size();
            if(size != sexpr.size()) {
                return false;
            }

            for (int i = 0; i < size; i++) {
                if(!get(i).equals(sexpr.get(i))) {
                    return false;
                }
            }

            return true;
        }
        return false;
    }

    /* (non-Javadoc)
     * @see java.lang.Iterable#iterator()
     */
    @Override
    public Iterator<SExpr> iterator() {
        return children.iterator();
    }
}
