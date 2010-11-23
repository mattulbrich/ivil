package de.uka.iti.pseudo.parser.boogie.environment;

import de.uka.iti.pseudo.parser.boogie.ASTElement;
import de.uka.iti.pseudo.parser.boogie.ASTVisitException;
import de.uka.iti.pseudo.parser.boogie.ast.Attribute;
import de.uka.iti.pseudo.parser.boogie.ast.BuiltInType;
import de.uka.iti.pseudo.parser.boogie.ast.FunctionDeclaration;
import de.uka.iti.pseudo.parser.boogie.ast.LabelStatement;
import de.uka.iti.pseudo.parser.boogie.ast.MapType;
import de.uka.iti.pseudo.parser.boogie.ast.ProcedureBody;
import de.uka.iti.pseudo.parser.boogie.ast.ProcedureDeclaration;
import de.uka.iti.pseudo.parser.boogie.ast.ProcedureImplementation;
import de.uka.iti.pseudo.parser.boogie.ast.QuantifierBody;
import de.uka.iti.pseudo.parser.boogie.ast.UserTypeDefinition;
import de.uka.iti.pseudo.parser.boogie.ast.Variable;
import de.uka.iti.pseudo.parser.boogie.util.DefaultASTVisitor;

/**
 * This visitor fills all *Space fields of state with information.
 * 
 * @author timm.felden@felden.com
 */
public class NamespaceBuilder extends DefaultASTVisitor {

    private final EnvironmentCreationState state;

    // used to create labelSpace
    private ProcedureBody currentBody = null;

    public NamespaceBuilder(EnvironmentCreationState environmentCreationState) throws ASTVisitException {
        state = environmentCreationState;

        visit(state.root);
    }

    /**
     * Pushes a type parameter and checks for duplicates.
     * 
     * @throws ASTVisitException
     *             thrown if a type parameter with name is already defined
     */
    private void addTypeParameter(ASTElement node, String name) throws ASTVisitException {
        Scope scope = state.scopeMap.get(node);

        for (Scope s = scope; s != null; s = s.parent)
            if (state.typeParameterSpace.containsKey(new Pair<String, Scope>(name, s)))
                throw new ASTVisitException("Tried to add type parameter " + name + " allready defined @"
                        + state.typeParameterSpace.get(new Pair<String, Scope>(name, s)).getLocation());

        state.typeParameterSpace.put(new Pair<String, Scope>(name, state.scopeMap.get(node)), node);
    }

    @Override
    protected void defaultAction(ASTElement node) throws ASTVisitException {
        for (ASTElement e : node.getChildren())
            e.visit(this);
    }

    @Override
    public void visit(Variable node) throws ASTVisitException {
        Pair<String, Scope> key = new Pair<String, Scope>(node.getName(), state.scopeMap.get(node));

        if (state.variableSpace.containsKey(key))
            throw new ASTVisitException("Tried to add key " + key + " allready defined @"
                    + state.variableSpace.get(key).getLocation());

        state.variableSpace.put(key, node);

        for (ASTElement e : node.getChildren())
            e.visit(this);
    }

    @Override
    public void visit(FunctionDeclaration node) throws ASTVisitException {
        final String key = node.getName();

        if (state.functionSpace.containsKey(key))
            throw new ASTVisitException("Tried to add key " + key + " allready defined @"
                    + state.functionSpace.get(key).getLocation());

        state.functionSpace.put(key, node);

        for (String s : node.getTypeParameters())
            addTypeParameter(node, s);

        for (ASTElement e : node.getChildren())
            e.visit(this);
    }

    @Override
    public void visit(BuiltInType node) throws ASTVisitException {
        // this is used to register all actually used built in types; if we wont
        // do this, we could not insert bv-Types into the typeSpace as they
        // would create an infinite amount of entries

        state.typeSpace.put(node.getPrettyName(), node);

        // no check needed here, grammar does not allow to create harmful
        // duplicates

        for (ASTElement e : node.getChildren())
            e.visit(this);
    }

    @Override
    public void visit(UserTypeDefinition node) throws ASTVisitException {
        final String key = node.getName();

        if (state.typeSpace.containsKey(key))
            throw new ASTVisitException("Tried to add key " + key + " allready defined @"
                    + state.typeSpace.get(key).getLocation());

        state.typeSpace.put(key, node);

        if (node.getDefinition() != null) {
            // we have to add type arguments as they will be used like type
            // parameters
            for (String name : node.getTypeParameters())
                addTypeParameter(node, name);
        }

        for (ASTElement e : node.getChildren())
            e.visit(this);
    }

    @Override
    public void visit(MapType node) throws ASTVisitException {
        for (String name : node.getTypeParameters())
            addTypeParameter(node, name);

        for (ASTElement e : node.getChildren())
            e.visit(this);
    }

    @Override
    public void visit(ProcedureDeclaration node) throws ASTVisitException {
        final String key = node.getName();

        if (state.procedureSpace.containsKey(key))
            throw new ASTVisitException("Tried to add key " + key + " allready defined @"
                    + state.procedureSpace.get(key).getLocation());

        state.procedureSpace.put(key, node);

        for (String s : node.getTypeParameters())
            addTypeParameter(node, s);

        for (ASTElement e : node.getChildren())
            e.visit(this);
    }

    @Override
    public void visit(ProcedureImplementation node) throws ASTVisitException {
        // dont add name, as the name has to be declared elsewhere

        for (String s : node.getTypeParameters())
            addTypeParameter(node, s);

        for (ASTElement e : node.getChildren())
            e.visit(this);
    }

    @Override
    public void visit(QuantifierBody node) throws ASTVisitException {
        // dont add name, as the name has to be declared elsewhere

        for (String s : node.getTypeParameters())
            addTypeParameter(node, s);

        for (ASTElement e : node.getChildren())
            e.visit(this);
    }

    @Override
    public void visit(ProcedureBody node) throws ASTVisitException {
        currentBody = node;

        for (ASTElement e : node.getChildren())
            e.visit(this);

        currentBody = null;
    }

    @Override
    public void visit(LabelStatement node) throws ASTVisitException {
        assert currentBody != null;

        Pair<String, ProcedureBody> key = new Pair<String, ProcedureBody>(node.getName(), currentBody);

        if (state.variableSpace.containsKey(key))
            throw new ASTVisitException("Tried to add key " + key + " allready defined @"
                    + state.variableSpace.get(key).getLocation());

        state.labelSpace.put(key, node);

        for (ASTElement e : node.getChildren())
            e.visit(this);
    }

    @Override
    public void visit(Attribute node) throws ASTVisitException {
        final String key = node.getName();

        state.attributeSpace.put(key, node);

        for (ASTElement e : node.getChildren())
            e.visit(this);
    }
}
