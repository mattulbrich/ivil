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
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import nonnull.Nullable;
import de.uka.iti.pseudo.algo.data.ParsedAlgorithm;
import de.uka.iti.pseudo.algo.data.ParsedData;
import de.uka.iti.pseudo.algo.data.RefinementDeclaration;
import de.uka.iti.pseudo.util.CommandLine;
import de.uka.iti.pseudo.util.CommandLineException;

public class Translation {

    private AlgoParser parser;

    private final String sourceFile;

    private boolean refinementMode;

    private @Nullable String algos;

    private ParsedData parsedData;

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
                target = new PrintWriter(new FileWriter(source + ".p"));
            } else {
                target = new PrintWriter(System.out);
            }

            Translation translation = new Translation(source);

            translation.refinementMode = cl.isSet("-ref");

            if(cl.isSet("-prog")) {
                translation.algos = cl.getString("-prog", null);
            }

            translation.exportTo(target);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static CommandLine createCommandLine() {
        CommandLine cl = new CommandLine();
        cl.addOption("-ref", null, "Extract the refinement from the algorithm");
        cl.addOption("-algo", "algoName", "Choose the algorithms for which to export a PO (comma-sep)");
        return cl;
    }

    public Translation(String sourceFile) throws IOException, ParseException {
        if(sourceFile == null) {
            this.sourceFile = "<in>";
            this.parser = new AlgoParser(System.in);
        } else {
            this.sourceFile = sourceFile;
            this.parser = new AlgoParser(new FileInputStream(sourceFile));
        }
    }

    public Translation(AlgoParser p) {
        this.sourceFile = "<none>";
        this.parser = p;
    }

    public void exportTo(PrintWriter pw) throws ParseException {

        ASTStart result = parser.Start();
        this.parsedData = new ParsedData();
        parsedData.addDeclaration("# Automatically created on " + new Date());

        //
        // preprocess:
        //

        // change a < b < c to  a < b & b < c
        ChainedRelationVisitor crv = new ChainedRelationVisitor();
        result.jjtAccept(crv, null);

        // extract the algorithm declarations from the file
        AlgoDeclarationVisitor adv = new AlgoDeclarationVisitor(parsedData);
        result.jjtAccept(adv, null);


        if(refinementMode) {
            // extract the refinement declarations from the file
            RefinementVisitor rv = new RefinementVisitor(parsedData);
            result.jjtAccept(rv, null);
        }

        Collection<String> algosToExport = determineAlgosToExtract();

        for (String string : algosToExport) {
            ParsedAlgorithm algo = parsedData.getAlgorithms().get(string);
            AlgoVisitor algoVisitor = new AlgoVisitor(parsedData, algo, refinementMode);
            algoVisitor.extractProgram();
        }

        for (String string : parsedData.getDeclarations()) {
            pw.println(string);
        }

        pw.println();

//        for (String string : visitor.getPrograms()) {
//            pw.println(string);
//        }

        pw.flush();

    }

    public Collection<String> determineAlgosToExtract() {
        Collection<String> algosToExport;
        if(refinementMode) {
            algosToExport = new ArrayList<String>();
            RefinementDeclaration refDecl = parsedData.getRefinementDeclartion();
            if(refDecl == null) {
                throw new RuntimeException("Missing refinement declaration");
            }
            algosToExport.add(refDecl.getAbstrAlgoName());
            algosToExport.add(refDecl.getConcrAlgoName());
        } else if(algos != null) {
            algosToExport = Arrays.asList(algos.split(","));
        } else {
            algosToExport = parsedData.getAlgorithms().keySet();
        }

        return algosToExport;
    }

}
