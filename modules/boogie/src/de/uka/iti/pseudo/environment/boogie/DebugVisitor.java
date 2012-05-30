/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2012 Karlsruhe Institute of Technology
 *
 * The system is protected by the GNU General Public License.
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.environment.boogie;

import java.util.List;

import de.uka.iti.pseudo.parser.boogie.ASTElement;
import de.uka.iti.pseudo.parser.boogie.ASTVisitException;
import de.uka.iti.pseudo.parser.boogie.util.DefaultASTVisitor;

/**
 * Prints indented information about ASTElements and their decorations.
 * 
 * @author timm.felden@felden.com
 */
public final class DebugVisitor extends DefaultASTVisitor {

    private int depth = 0;
    private final List<Decoration<?>> decorations;

    public DebugVisitor(List<Decoration<?>> decorations) {
        this.decorations = decorations;
    }

    @Override
    protected void defaultAction(ASTElement node) throws ASTVisitException {

        String prefix = "";
        for (int i = 0; i < depth; i++)
            prefix += "  ";

        System.out.println(prefix + node.toString());

        // print decorations
        for (Decoration<?> decor : decorations) {
            if (decor.has(node)) {
                Object d = decor.get(node);
                System.out.println(prefix + "\u21B3" + (null == d ? "(null)" : d.toString()));
            } else
                System.out.println(prefix + "\u21B3\u2205");
        }


        depth++;
        for (ASTElement n : node.getChildren())
            n.visit(this);

        System.out.println("");
        depth--;
    }

}