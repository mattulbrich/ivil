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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import nonnull.Nullable;
import de.uka.iti.pseudo.util.CommandLine;
import de.uka.iti.pseudo.util.CommandLineException;

public class Translation {

    public static final String ALGO_MARK_VARIABLE = "$algoMark";

    private AlgoParser parser;

    private final List<String> declarations = new ArrayList<String>();

    private final String sourceFile;

    private final Set<String> functionNames = new HashSet<String>();

    private boolean refinementMode;

    public static void main(String[] args) throws ParseException, IOException, CommandLineException {
        String source;
        CommandLine cl = createCommandLine();
        cl.parse(args);

        List<String> clArgs = cl.getArguments();
        if(clArgs.size() > 0) {
            source = clArgs.get(0);
        } else {
            source = null;
        }

        PrintWriter target;
        if(clArgs.size() > 1) {
            target = new PrintWriter(new FileWriter(clArgs.get(1)));
        } else {
            target = new PrintWriter(System.out);
        }

        Translation translation = new Translation(source);

        if(cl.isSet("-ref")) {
            translation.refinementMode = true;
        }

        translation.exportTo(target);
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
        functionNames .add(name);
    }

    public void addDeclaration(String string) {
        declarations.add(string);
    }

}
