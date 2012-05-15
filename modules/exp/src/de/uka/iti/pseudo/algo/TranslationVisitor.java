package de.uka.iti.pseudo.algo;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * The TranslationVisitor does the toplevel translation work.
 *
 * It delegates part of the translation to the TermVisitor, AlgoVisitor,
 * JavaVisitor, MethodRefVisitor.
 */
public class TranslationVisitor extends DefaultAlgoVisitor {

    private final List<String> programs = new ArrayList<String>();

    private final Translation translation;

    private final boolean refinementMode;

    public TranslationVisitor(Translation translation, boolean refinementMode) {
        this.translation = translation;
        this.refinementMode = refinementMode;
    }

    public List<String> getPrograms() {
        return programs;
    }

    @Override
    public String visit(ASTStart node, Object data) {
        translation.addDeclaration("# Automatically created on " + new Date());
        node.childrenAccept(this, data);
        return null;
    }

    @Override
    public String visit(ASTUsesInputDeclaration node, Object data) {
        translation.addDeclaration("include \"" + node.jjtGetValue() + "\"");
        return null;
    }

    @Override
    public String visit(ASTUsesInlineDeclaration node, Object data) {
        translation.addDeclaration(node.jjtGetValue().toString());
        return null;
    }

    @Override
    public String visit(ASTAlgo node, Object data) {

        AlgoVisitor visitor = new AlgoVisitor(translation, refinementMode);

        programs.addAll(visitor.extractProgram(node));
        return null;
    }


    @Override
    public String visit(ASTRefinement node, Object data) {
        if(refinementMode) {
            JavaVisitor javaVisitor = new JavaVisitor(translation);
            node.jjtAccept(javaVisitor, data);
        }
        return "";
    }

    @Override
    public String visitDefault(SimpleNode node, Object data) {
        throw new Error("TranslationVisitor must not visit a node of type "
                + node.getClass().getSimpleName());
    }

}
