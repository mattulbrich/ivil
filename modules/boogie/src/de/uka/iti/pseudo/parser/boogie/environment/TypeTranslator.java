package de.uka.iti.pseudo.parser.boogie.environment;

import de.uka.iti.pseudo.parser.boogie.ASTElement;
import de.uka.iti.pseudo.parser.boogie.ASTVisitException;
import de.uka.iti.pseudo.parser.boogie.util.DefaultASTVisitor;

/**
 * translates universal types to ivil types
 * 
 * @author timm.felden@felden.com
 * 
 */
public final class TypeTranslator extends DefaultASTVisitor {
    private final EnvironmentCreationState state;

    public TypeTranslator(EnvironmentCreationState state) throws ASTVisitException {
        this.state = state;

        state.root.visit(this);
    }

    @Override
    protected void defaultAction(ASTElement node) throws ASTVisitException {
        if (null != state.typeMap.get(node)) {
            state.ivilTypeMap.add(node, state.typeMap.get(node).toIvilType(state));
        } else
            state.ivilTypeMap.add(node, null);

        for (ASTElement e : node.getChildren())
            e.visit(this);
    }
}
