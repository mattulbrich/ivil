/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2012 Karlsruhe Institute of Technology
 *
 * The system is protected by the GNU General Public License.
 * See LICENSE.TXT (distributed with this file) for details.
 */
///*
// * This file is part of
// *    ivil - Interactive Verification on Intermediate Language
// *
// * Copyright (C) 2009-2012 Karlsruhe Institute of Technology
// *
// * The system is protected by the GNU General Public License.
// * See LICENSE.TXT (distributed with this file) for details.
// */
//package de.uka.iti.pseudo.algo;
//
//import java.util.ArrayList;
//import java.util.Date;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//import de.uka.iti.pseudo.algo.data.ParsedAlgorithm;
//import de.uka.iti.pseudo.algo.data.ParsedData;
//import de.uka.iti.pseudo.util.Pair;
//import de.uka.iti.pseudo.util.Util;
//
///**
// * The TranslationVisitor does the toplevel translation work.
// *
// * It delegates part of the translation to the TermVisitor, AlgoVisitor,
// * JavaVisitor, MethodRefVisitor.
// */
//public class TranslationVisitor extends DefaultAlgoParserVisitor {
//
//    private final List<String> programs = new ArrayList<String>();
//
//    private final ParsedData parsedData;
//
//    private final Map<String, ParsedAlgorithm> algos = new HashMap<String, ParsedAlgorithm>();
//
//    private final boolean refinementMode;
//
//    private final TermVisitor termVisitor;
//
//    public TranslationVisitor(ParsedData parsedData, boolean refinementMode) {
//        this.parsedData = parsedData;
//        this.refinementMode = refinementMode;
//        this.termVisitor = new TermVisitor(parsedData);
//    }
//
//    public List<String> getPrograms() {
//        return programs;
//    }
//
//    @Override
//    public String visit(ASTAbbrevIdentifier node, Object data) {
//        return node.jjtGetValue().toString();
//    }
//
//    @Override
//    public String visit(ASTAlgo node, Object data) {
//        AlgoVisitor visitor = new AlgoVisitor(parsedData, refinementMode);
//        node.jjtAccept(visitor, data);
//        programs.addAll(visitor.extractProgram(node));
//        return null;
//    }
//
//    @Override
//    public String visitDefault(SimpleNode node, Object data) {
//        throw new Error("TranslationVisitor must not visit a node of type "
//                + node.getClass().getSimpleName());
//    }
//
//}
