package de.uka.iti.pseudo.parser.boogie.ast;

import java.net.URL;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import de.uka.iti.pseudo.parser.boogie.ASTElement;
import de.uka.iti.pseudo.parser.boogie.ASTVisitException;
import de.uka.iti.pseudo.parser.boogie.ASTVisitor;
import de.uka.iti.pseudo.parser.boogie.Token;
import de.uka.iti.pseudo.parser.boogie.ast.type.UserDefinedTypeDeclaration;

/**
 * A compilation unit contains the content of a compelete boogie file. In
 * contrast to ASTFile, no Problem can be specified here, as boogie has the
 * implicit problem of proofing all Procedures correct. Therefore a
 * corresponding Problem will be generated at environment construction time.
 * 
 * @author timm.felden@felden.com
 * 
 */
final public class CompilationUnit extends ASTElement {

    private URL url;
    /**
     * Guaranteed to be in order "types, consts, globalvars, functions, axiom,
     * procdec, procimp".
     * 
     * This guarantee is used heavily by type and program transformation.
     */
    private List<DeclarationBlock> declarationBlocks;

    public CompilationUnit(URL location, List<DeclarationBlock> blocks) {
        this.declarationBlocks = new LinkedList<DeclarationBlock>();

        for (DeclarationBlock d : blocks)
            if (d instanceof UserDefinedTypeDeclaration)
                declarationBlocks.add(d);

        for (DeclarationBlock d : blocks)
            if (d instanceof ConstantDeclaration)
                declarationBlocks.add(d);

        for (DeclarationBlock d : blocks)
            if (d instanceof GlobalVariableDeclaration)
                declarationBlocks.add(d);

        for (DeclarationBlock d : blocks)
            if (d instanceof FunctionDeclaration)
                declarationBlocks.add(d);

        for (DeclarationBlock d : blocks)
            if (d instanceof AxiomDeclaration)
                declarationBlocks.add(d);

        for (DeclarationBlock d : blocks)
            if (d instanceof ProcedureDeclaration)
                declarationBlocks.add(d);

        for (DeclarationBlock d : blocks)
            if (d instanceof ProcedureImplementation)
                declarationBlocks.add(d);

        this.url = location;
        assert null != location;

        addChildren(declarationBlocks);
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

    @Override
    public String toString(){
        return "CompilationUnit" + " [" + url + "]";
    }

    public URL getURL() {
        return url;
    }

}
