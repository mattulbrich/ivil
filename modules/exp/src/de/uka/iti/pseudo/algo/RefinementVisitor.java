package de.uka.iti.pseudo.algo;



public class RefinementVisitor extends DefaultAlgoParserVisitor {

    private String methodName;
    private String className;
    private String methodSignaure;
    private final TermVisitor termVisitor;
    private final Translation translation;

    public RefinementVisitor(Translation translation) {
        this.translation = translation;
        this.termVisitor = new TermVisitor(translation);
    }

    @Override
    public String visit(ASTIdentifier node, Object data) {
        return node.jjtGetValue().toString();
    }

    @Override
    public String visit(ASTCouplingFormula node, Object data) {
        String key = (String) node.jjtGetValue();
        String value = node.jjtGetChild(0).jjtAccept(termVisitor, null);
        translation.putCouplingInvariant(key, value);
        if(node.jjtGetNumChildren() > 1) {
            // there is a variant too!
            value = node.jjtGetChild(1).jjtAccept(termVisitor, null);
            translation.putCouplingVariant(key, value);
        }
        return null;
    }

    @Override
    public String visitDefault(SimpleNode node, Object data) {
        throw new Error("JavaVisitor must not visit a node of type "
                + node.getClass().getSimpleName());
    }

    @Override
    public String visit(ASTRefinement node, Object data) {

        String abstrProg = visitChild(node, 0);
        String concrProg = visitChild(node, 1);

        node.childrenAccept(this, data);

        String pre = translation.getCouplingInvariant("-1");
        String post = translation.getCouplingInvariant("0");

        return pre + " |- [0; " + concrProg + "][<0;" + abstrProg + ">](" + post + ")";
    }

}
