package de.uka.iti.pseudo.parser.boogie.ast;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.LinkedList;
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
final public class CompilationUnit extends ASTElement {

    private final String name;
    private URL url;
    /**
     * Guaranteed to be in order "types, consts, globalvars, functions, axiom,
     * procdec, procimp".
     * 
     * This guarantee is used heavily by type and program transformation.
     */
    private List<DeclarationBlock> declarationBlocks;

    public CompilationUnit(String name, List<DeclarationBlock> blocks) {
        this.name = name;
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

        File f = new File(name);
        try {
            url = f.toURI().toURL();
        } catch (MalformedURLException e) {
            e.printStackTrace();
            url = null;
        }

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
        return "CompilationUnit" + (null == name ? "" : " [" + name + "]");
    }

    public URL getURL() {
        return url;
    }

}
