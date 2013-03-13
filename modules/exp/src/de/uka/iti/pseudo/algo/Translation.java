/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2012 Karlsruhe Institute of Technology
 *
 * The system is protected by the GNU General Public License.
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.algo;

import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import nonnull.Nullable;
import de.uka.iti.pseudo.util.CommandLine;
import de.uka.iti.pseudo.util.CommandLineException;
import de.uka.iti.pseudo.util.Pair;
import de.uka.iti.pseudo.util.Util;

public class Translation {

    private AlgoParser parser;

    private final List<String> declarations = new ArrayList<String>();

    private final String sourceFile;

    private final Set<String> functionNames = new HashSet<String>();

    private boolean refinementMode;

    final Map<String, String> abbreviations = new HashMap<String, String>();
    private final Map<String, String> couplingInvariantMap = new HashMap<String, String>();
    private final Map<String, String> couplingVariantMap = new HashMap<String, String>();
    private final Map<String, String> options = new HashMap<String, String>();

    public static void main(String[] args) throws IOException, CommandLineException {
        CommandLine cl = createCommandLine();
        List<String> clArgs = cl.getArguments();
        cl.parse(args);

        String source = null;
        try {
            if(clArgs.size() > 0) {
                source = clArgs.get(0);
            } else {
                source = null;
            }

            PrintWriter target;
            if(clArgs.size() > 1) {
                target = new PrintWriter(new FileWriter(clArgs.get(1)));
            } else if(clArgs.size() > 0) {
                target = new PrintWriter(new FileWriter(clArgs.get(0) + ".p"));
            } else {
                target = new PrintWriter(System.out);
            }

            Translation translation = new Translation(source);

            if(cl.isSet("-ref")) {
                translation.refinementMode = true;
            }

            translation.exportTo(target);
        } catch (Exception e) {
            System.err.println("Error while reading " + (source == null ? "<in>" : source));
            e.printStackTrace();
        }
    }

    private static CommandLine createCommandLine() {
        CommandLine cl = new CommandLine();
        cl.addOption("-ref", null, "Extract the refinement from the algorithm");
        return cl;
    }

    public Translation(String sourceFile) throws IOException, ParseException {
        this.sourceFile = sourceFile;

        if(sourceFile == null) {
            parser = new AlgoParser(System.in);
        } else {
            parser = new AlgoParser(new FileInputStream(sourceFile));
        }
    }

    public Translation(AlgoParser p) {
        this.sourceFile = "<none>";
        this.parser = p;
    }

    public void exportTo(PrintWriter pw) throws ParseException {

        ASTStart result = parser.Start();

        // preprocess by some visitors:
        ChainedRelationVisitor crv = new ChainedRelationVisitor();
        result.jjtAccept(crv, null);

        TranslationVisitor visitor = new TranslationVisitor(this, refinementMode);
        result.jjtAccept(visitor, null);

        for (String string : declarations) {
            pw.println(string);
        }

        pw.println();

        for (String string : visitor.getPrograms()) {
            pw.println(string);
        }

        pw.flush();

    }

    public @Nullable String getSourceFile() {
        return sourceFile;
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

    public void addDeclaration(String string) {
        declarations.add(string);
    }

    public String getAbbreviatedTerm(Object name) {
        String result = abbreviations.get(name);
        if(result == null) {
            System.out.println(abbreviations);
            throw new IllegalStateException("Abbreviation " + name + " not defined");
        }
        return result;
    }

    public void putAbbreviation(String name, String term) {
        if(abbreviations.containsKey(name)) {
            throw new IllegalStateException("Abbreviation " + name + " already defined");
        }
        abbreviations.put(name, term);
    }

    public void putCouplingInvariant(String key, String value) {
        if(couplingInvariantMap.containsKey(key)) {
            throw new IllegalStateException("Coupling invariant for " + key + " already defined");
        }
        couplingInvariantMap.put(key, value);
    }

    public void putCouplingVariant(String key, String value) {
        if(couplingVariantMap.containsKey(key)) {
            throw new IllegalStateException("Coupling variant for " + key + " already defined");
        }
        couplingVariantMap.put(key, value);
    }

    public String getCouplingInvariant(String name) {
        String result = couplingInvariantMap.get(name);
        if(result == null) {
            throw new IllegalStateException("Coupling invariant " + name + " not defined");
        }
        return result;
    }

    public String getCouplingVariant(String name) {
        String result = couplingVariantMap.get(name);
        if(result == null) {
            throw new IllegalStateException("Coupling variant " + name + " not defined");
        }
        return result;
    }

    public void setOption(String option, String value) {
        options.put(option, value);
    }

    public String getOption(String option) {
        return options.get(option);
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

}
