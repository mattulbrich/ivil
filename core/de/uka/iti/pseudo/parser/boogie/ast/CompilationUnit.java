package de.uka.iti.pseudo.parser.boogie.ast;

import java.util.Collections;
import java.util.List;

import de.uka.iti.pseudo.parser.boogie.ASTElement;
import de.uka.iti.pseudo.parser.boogie.ASTVisitException;
import de.uka.iti.pseudo.parser.boogie.ASTVisitor;
import de.uka.iti.pseudo.parser.boogie.Token;

/**
 * A compilation unit contains the content of a compelete boogie file. In
 * contrast to ASTFile, no Problem can be specified here, as boogie has the
 * implicit problem of proofing all Procedures correct. Therefore a
 * corresponding Problem will be generated at environment construction time.
 * 
 * @author timm.felden@felden.com
 * 
 */
public class CompilationUnit extends ASTElement {

    private List<DeclarationBlock> declarationBlocks;

    public CompilationUnit(List<DeclarationBlock> blocks) {
        this.declarationBlocks = blocks;
        addChildren(blocks);
    }

    public List<DeclarationBlock> getDeclarationBlocks() {
        return Collections.unmodifiableList(declarationBlocks);
    }

    @Override
    public Token getLocationToken() {
        // no location can be provided for a complete compilation unit
        return null;
    }

    @Override
    public void visit(ASTVisitor v) throws ASTVisitException {
        v.visit(this);
    }

}
