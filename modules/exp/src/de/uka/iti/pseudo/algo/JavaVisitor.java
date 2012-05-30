package de.uka.iti.pseudo.algo;

import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import de.uka.iti.pseudo.util.Util;


public class JavaVisitor extends DefaultAlgoParserVisitor {

    private String methodName;
    private String className;
    private String methodSignaure;
    private final TermVisitor termVisitor;
    private final Map<Object, String> couplingMap = new HashMap<Object, String>();
    private final Translation translation;

    public JavaVisitor(Translation translation) {
        this.translation = translation;
        this.termVisitor = new TermVisitor(translation);
    }

    @Override
    public String visit(ASTMethodReference node, Object data) {
        Deque<String> methodDeclaration = new LinkedList<String>();
        StringBuilder sigBuilder = new StringBuilder();
        for (int i = 0; i < node.jjtGetNumChildren(); i++) {
            Node n = node.jjtGetChild(i);
            if (n instanceof ASTIdentifier) {
                methodDeclaration.add(n.jjtAccept(this, data));
            } else {
                sigBuilder.append(n.jjtAccept(this, data));
            }
        }

        methodName = methodDeclaration.removeLast();
        className = Util.join(methodDeclaration, "/");
        methodSignaure = sigBuilder.toString();

        return null;
    }

    @Override
    public String visit(ASTMethodParameterType node, Object data) {
        int dimension = (Integer)node.jjtGetValue();
        StringBuilder typeBuilder = new StringBuilder();
        for (int i = 0; i < node.jjtGetNumChildren(); i++) {
            if(i > 0 ) {
                typeBuilder.append("/");
            }
            typeBuilder.append(visitChild(node, i));
        }
        String type = typeBuilder.toString();

        // TODO use classlib function
        // TODO Dimension
        return "L" + type + ";";
    }

    @Override
    public String visit(ASTIdentifier node, Object data) {
        return node.jjtGetValue().toString();
    }

    @Override
    public String visit(ASTCouplingFormula node, Object data) {
        Object key = node.jjtGetValue();
        String value = node.jjtGetChild(0).jjtAccept(termVisitor, null);
        couplingMap.put(key, value);
        return null;
    }

    @Override
    public String visitDefault(SimpleNode node, Object data) {
        throw new Error("JavaVisitor must not visit a node of type "
                + node.getClass().getSimpleName());
    }

}
