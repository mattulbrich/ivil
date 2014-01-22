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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class ChainedRelationVisitor extends DefaultAlgoParserVisitor {

    private static final Set<String> chainableRelations =
            new HashSet<String>(Arrays.asList("<", "<="));

    @Override
    public String visitDefault(SimpleNode node, Object data) {
        node.childrenAccept(this, data);
        return null;
    }

    /* Check if the node embeds a formula of form
     * a < (b < c). Delegate to chainRelations then
     * which renders it to a < b & b < c
     */
    @Override
    public String visit(ASTBinaryExpression node, Object data) {
        String op = node.jjtGetValue().toString();
        if(chainableRelations.contains(op)) {
            Node child = node.jjtGetChild(1);
            if (child instanceof ASTBinaryExpression) {
                ASTBinaryExpression inner = (ASTBinaryExpression) child;
                String innerOp = inner.jjtGetValue().toString();
                if(chainableRelations.contains(innerOp)) {
                    chainRelations(node);
                }
            }
        }

        visitDefault(node, data);
        return null;
    }

    /*
     * left < right
     *
     * with
     *
     * left < (rightleft < rightright)
     */
    private void chainRelations(ASTBinaryExpression node) {
        SimpleNode left = (SimpleNode) node.jjtGetChild(0);
        SimpleNode right = (SimpleNode) node.jjtGetChild(1);
        SimpleNode rightleft = (SimpleNode) right.jjtGetChild(0);

        SimpleNode conj1 =
                new ASTBinaryExpression(AlgoParser.JJTBINARYEXPRESSION);
        conj1.children = new Node[] { left, rightleft };
        conj1.firstToken = left.firstToken;
        conj1.jjtSetValue(node.jjtGetValue());

        SimpleNode conj2 = right;

        node.children = new Node[] { conj1, conj2 };
        node.jjtSetValue("&");
    }

}
