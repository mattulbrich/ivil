package de.uka.iti.pseudo.environment.boogie;

import de.uka.iti.pseudo.environment.EnvironmentException;
import de.uka.iti.pseudo.environment.Function;
import de.uka.iti.pseudo.parser.boogie.ASTElement;
import de.uka.iti.pseudo.parser.boogie.ASTVisitException;
import de.uka.iti.pseudo.parser.boogie.ast.FunctionDeclaration;
import de.uka.iti.pseudo.parser.boogie.ast.ModifiesClause;
import de.uka.iti.pseudo.parser.boogie.ast.ProcedureDeclaration;
import de.uka.iti.pseudo.parser.boogie.ast.ProcedureImplementation;
import de.uka.iti.pseudo.parser.boogie.ast.VariableDeclaration;
import de.uka.iti.pseudo.parser.boogie.util.DefaultASTVisitor;
import de.uka.iti.pseudo.term.Type;

/**
 * Processes global and local variable declarations. This is needed to allow
 * ProgramMaker to assume all variables to be present.
 * 
 * @see ProgramMaker
 * 
 * @author timm.felden@felden.com
 */
public class PreDefinitionVisitor extends DefaultASTVisitor {

    static private final Type[] NO_TYPE = new Type[0];

    private final EnvironmentCreationState state;

    private String desiredName = null;

    public PreDefinitionVisitor(EnvironmentCreationState state) {
        this.state = state;

        try {
            state.root.visit(this);
        } catch (ASTVisitException e) {
            e.printStackTrace();
            assert state.printDebugInformation() : "precreation failed somehow";
        }
    }

    @Override
    protected void defaultAction(ASTElement node) throws ASTVisitException {
        for (ASTElement element : node.getChildren())
            element.visit(this);
    }

    @Override
    public void visit(FunctionDeclaration node) throws ASTVisitException {
        for (ASTElement e : node.getInParameters())
            e.visit(this);
        node.getOutParemeter().visit(this);

        // add declaration
        {
            Type[] arguments = new Type[node.getInParameters().size()];
            for (int i = 0; i < node.getInParameters().size(); i++)
                arguments[i] = state.typeMap.get(node.getInParameters().get(i));

            try {
                state.env.addFunction(new Function("fun__" + node.getName(), state.typeMap.get(node
                        .getOutParemeter()), arguments, false, false, node));

            } catch (EnvironmentException e) {
                e.printStackTrace();
                throw new ASTVisitException("Function declaration failed because of:\n", e);
            }
        }
    }

    @Override
    public void visit(VariableDeclaration node) throws ASTVisitException {

        // names are somesort of magic, if no other name is desired, the name
        // will be var_<line>_<colon>__name

        String name = null != desiredName ? desiredName : "var" + node.getLocation().replace(':', '_') + "__"
                + node.getName();
        desiredName = null;

        // allready processed by predeclare visitor
        if (state.translation.variableNames.containsKey(node))
            return;

        state.translation.variableNames.put(node, name);

        // quantified variables differ from ordinary variables, as they aren't
        // functions
        if (node.isQuantified() || node.getParent() instanceof FunctionDeclaration) {
            return;

        } else {
            try {
                if (null == state.env.getFunction(name)) {
                    Function var = new Function(name, state.typeMap.get(node), NO_TYPE, node.isUnique(),
                            !node.isConstant(), node);
                    state.env.addFunction(var);
                }

            } catch (EnvironmentException e) {
                e.printStackTrace();
                throw new ASTVisitException(node.getLocation(), e);

            }
        }
    }

    @Override
    public void visit(ProcedureDeclaration node) throws ASTVisitException {

        // we want in parameters as in0, in1, ... and out parameters in the same
        // form, so we can simply join contracts from declarations and
        // implementations

        for (int i = 0; i < node.getInParameters().size(); i++) {
            desiredName = "in_" + node.getName() + "_" + i;
            node.getInParameters().get(i).visit(this);
        }

        for (int i = 0; i < node.getOutParameters().size(); i++) {
            desiredName = "out_" + node.getName() + "_" + i;
            node.getOutParameters().get(i).visit(this);
        }

        // first visit modifies clauses to ensure old names exist
        for (ASTElement e : node.getSpecification())
            if (e instanceof ModifiesClause)
                e.visit(this);

        for (ASTElement e : node.getSpecification())
            if (!(e instanceof ModifiesClause))
                e.visit(this);

        if (node.isImplemented())
            node.getBody().visit(this);
    }

    @Override
    public void visit(ProcedureImplementation node) throws ASTVisitException {

        // we wan in parameters as in0, in1, ... and out parameters in the same
        // form, so we can simply join contracts from declarations and
        // implementations

        for (int i = 0; i < node.getInParameters().size(); i++) {
            desiredName = "in_" + node.getName() + "_" + i;
            node.getInParameters().get(i).visit(this);
        }

        for (int i = 0; i < node.getOutParameters().size(); i++) {
            desiredName = "out_" + node.getName() + "_" + i;
            node.getOutParameters().get(i).visit(this);
        }

        node.getBody().visit(this);
    }
}
