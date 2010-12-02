/*
 * This file is part of PSEUDO
 * Copyright (C) 2009 Universitaet Karlsruhe, Germany
 *    written by Mattias Ulbrich
 * 
 * The system is protected by the GNU General Public License. 
 * See LICENSE.TXT for details.
 */
package de.uka.iti.pseudo.environment;

/**
 * A source annotation is a special kind of statement which has no
 * influence on the execution but is there for documentation.
 * 
 * <p>It contains a line of code (a comment) and a pointer to the
 * statement to which it points.
 * 
 * <p>SourceAnnotations can be compared, comparison is done on the
 * referenced statement number.
 *
 */
public class SourceAnnotation implements Comparable<SourceAnnotation> {
    
    private String source;
    private int statementNo;

    public SourceAnnotation(String source, int statementNo) {
        this.statementNo = statementNo;
        this.source = source;
    }

    public String toString() {
        return source;
    }

    public int getStatementNo() {
        return statementNo;
    }

    public int compareTo(SourceAnnotation o) {
        return statementNo - o.statementNo;
    }

}
