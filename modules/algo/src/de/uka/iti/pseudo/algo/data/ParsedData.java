/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2012 Karlsruhe Institute of Technology
 *
 * The system is protected by the GNU General Public License.
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.algo.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import nonnull.NonNull;
import nonnull.Nullable;
import de.uka.iti.pseudo.algo.ASTHint;
import de.uka.iti.pseudo.algo.Node;
import de.uka.iti.pseudo.util.Pair;
import de.uka.iti.pseudo.util.Util;

/**
 * This class is used to store and access data from a parsed algo file.
 *
 * @author mattias
 */
@NonNull
public final class ParsedData {

    /**
     * The global declarations which are common to all algorithms in the file.
     */
    private final List<String> declarations = new ArrayList<String>();

    /**
     * The options set in the "options" section of the file.
     */
    private final Map<String, String> options = new HashMap<String, String>();

    /**
     * The map of all defined abbreviations.
     * Maps from their name to their value.
     */
    private final Map<String, String> abbreviations = new HashMap<String, String>();

    private RefinementDeclaration refinementDeclartion;

    private final Set<String> functionNames = new HashSet<String>();

    private final Map<String, ParsedAlgorithm> algorithms =
            new LinkedHashMap<String, ParsedAlgorithm>();



    /**
     * The source file for this data.
     */
    private @Nullable String sourceFile;

    /**
     * Add or override an option.
     * @param option the option to set
     * @param value the string to store for the option
     */
    public void setOption(String option, String value) {
        options.put(option, value);
    }

    /**
     * Retrieve an option.
     *
     * This method returns <code>null</code> if the option has not been set.
     *
     * @param option the name of the option to retrieve
     * @return the value of the option, <code>null</code> if not set
     */
    public @Nullable String getOption(String option) {
        return options.get(option);
    }

    public void addDeclaration(String string) {
        declarations.add(string);
    }

    public void addFunctionSymbol(String name, String type) {
        addFunctionSymbol(name, type, "");
    }

    public void addFunctionSymbol(String name, String type, String mode) {
        if(functionNames.contains(name)) {
            throw new RuntimeException(name + " is already used as function symbol!");
        }
        declarations.add("function " + type + " " + name + " " + mode);
        functionNames.add(name);
    }


    /**
     * Get the list of global decarations for the file.
     *
     * @return an unmodifiable version of the list of declarations
     */
    public List<String> getDeclarations() {
        return Collections.unmodifiableList(declarations);
    }

    public String getAbbreviation(Object key) {
        String result = abbreviations.get(key);
        if(result == null) {
            System.out.println(abbreviations);
            throw new IllegalStateException("Abbreviation " + key + " not defined");
        }
        return result;
    }

    public void putAbbreviation(String name, String term) {
        if(abbreviations.containsKey(name)) {
            throw new IllegalStateException("Abbreviation " + name + " already defined");
        }
        abbreviations.put(name, term);
    }

    public @Nullable String getSourceFile() {
        return sourceFile;
    }

    public String retrieveHint(Node node, String... kind) {
        List<String> kindList = Util.readOnlyArrayList(kind);
        int count = node.jjtGetNumChildren();
        for (int i = 0; i < count; i++) {
            Node child = node.jjtGetChild(i);
            if (child instanceof ASTHint) {
                ASTHint hint = (ASTHint) child;
                Pair<?,?> data = (Pair<?, ?>) hint.jjtGetValue();
                if(kindList.contains(data.fst().toString())) {
                    return data.snd().toString();
                }
            }
        }
        return null;
    }

    public void addAlgo(ParsedAlgorithm currentAlgo) {
        getAlgorithms().put(currentAlgo.getName(), currentAlgo);
    }

    public Map<String, ParsedAlgorithm> getAlgorithms() {
        return algorithms;
    }

    public RefinementDeclaration getRefinementDeclartion() {
        return refinementDeclartion;
    }

    public void setRefinementDeclartion(RefinementDeclaration refinementDeclartion) {
        this.refinementDeclartion = refinementDeclartion;
    }

}
