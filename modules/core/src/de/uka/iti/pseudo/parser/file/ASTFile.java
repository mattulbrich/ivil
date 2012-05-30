/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2012 Karlsruhe Institute of Technology
 *
 * The system is protected by the GNU General Public License.
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.parser.file;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import nonnull.NonNull;
import nonnull.Nullable;
import de.uka.iti.pseudo.parser.ASTElement;
import de.uka.iti.pseudo.parser.ASTVisitException;
import de.uka.iti.pseudo.parser.ASTVisitor;
import de.uka.iti.pseudo.parser.Token;
import de.uka.iti.pseudo.util.SelectList;

public class ASTFile extends ASTElement {

	protected final static boolean DEBUG = false;

	private final List<ASTDeclarationBlock> declarationBlocks;

	public ASTFile(List<ASTDeclarationBlock> blocks) {

		this.declarationBlocks = blocks;
		addChildren(blocks);
	}

	@Override
	public void visit(ASTVisitor v) throws ASTVisitException {
		v.visit(this);
	}

	public static boolean isDEBUG() {
		return DEBUG;
	}

	public List<ASTDeclarationBlock> getDeclarationBlocks() {
		return Collections.unmodifiableList(declarationBlocks);
	}

	public @NonNull Collection<ASTProblemSequent> getProblemSequents() {
	    return SelectList.select(ASTProblemSequent.class, getChildren());
	}

	@Override
	public @Nullable Token getLocationToken() {
		return null;
	}

}
