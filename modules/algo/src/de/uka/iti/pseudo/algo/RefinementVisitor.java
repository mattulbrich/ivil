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

import de.uka.iti.pseudo.algo.data.ParsedData;
import de.uka.iti.pseudo.algo.data.RefinementDeclaration;

public class RefinementVisitor extends DefaultAlgoParserVisitor {

    private final TermVisitor termVisitor;
    private final ParsedData parsedData;
    private RefinementDeclaration refinementDecl;

    public RefinementVisitor(ParsedData parsedData) {
        this.parsedData = parsedData;
        this.termVisitor = new TermVisitor(parsedData);
    }

    @Override
    public String visit(ASTCouplingFormula node, Object data) {
        String key = (String) node.jjtGetValue();
        String value = node.jjtGetChild(0).jjtAccept(termVisitor, null);
        refinementDecl.putCouplingInvariant(key, value);
        if(node.jjtGetNumChildren() > 1) {
            // there is a variant too!
            value = node.jjtGetChild(1).jjtAccept(termVisitor, null);
            refinementDecl.putCouplingVariant(key, value);
        }
        return null;
    }

    @Override
    public String visit(ASTRefinement node, Object data) {

        String abstrProg = visitChild(node, 0);
        String concrProg = visitChild(node, 1);

        node.childrenAccept(this, data);

        refinementDecl = new RefinementDeclaration(abstrProg, concrProg);
        parsedData.setRefinementDeclartion(refinementDecl);

        return null;
    }

    @Override
    public String visit(ASTIdentifier node, Object data) {
        return node.jjtGetValue().toString();
    }

    @Override
    public String visitDefault(SimpleNode node, Object data) {
        throw new Error("JavaVisitor must not visit a node of type "
                + node.getClass().getSimpleName());
    }


}
