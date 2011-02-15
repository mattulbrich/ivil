package de.uka.iti.pseudo.environment.boogie;

import java.util.LinkedList;
import java.util.List;

import de.uka.iti.pseudo.parser.boogie.ASTElement;
import de.uka.iti.pseudo.parser.boogie.ASTVisitException;
import de.uka.iti.pseudo.parser.boogie.ast.Attribute;
import de.uka.iti.pseudo.parser.boogie.ast.BuiltInType;
import de.uka.iti.pseudo.parser.boogie.ast.ConstantDeclaration;
import de.uka.iti.pseudo.parser.boogie.ast.ExtendsParent;
import de.uka.iti.pseudo.parser.boogie.ast.FunctionDeclaration;
import de.uka.iti.pseudo.parser.boogie.ast.LabelStatement;
import de.uka.iti.pseudo.parser.boogie.ast.MapType;
import de.uka.iti.pseudo.parser.boogie.ast.ProcedureDeclaration;
import de.uka.iti.pseudo.parser.boogie.ast.ProcedureImplementation;
import de.uka.iti.pseudo.parser.boogie.ast.QuantifierBody;
import de.uka.iti.pseudo.parser.boogie.ast.UserTypeDefinition;
import de.uka.iti.pseudo.parser.boogie.ast.VariableDeclaration;
import de.uka.iti.pseudo.parser.boogie.util.DefaultASTVisitor;

/**
 * This visitor fills all *Space fields of state with information.
 * 
 * @author timm.felden@felden.com
 */
public class NamespaceBuilder extends DefaultASTVisitor {

    private final EnvironmentCreationState state;

    public NamespaceBuilder(EnvironmentCreationState environmentCreationState) throws ASTVisitException {
        state = environmentCreationState;

        visit(state.root);
    }

    /**
     * Pushes a type parameter and checks for duplicates.
     * 
     * @param name
     *            must match "[a-zA-Z_][a-zA-Z0-9_]*"
     * 
     * @throws ASTVisitException
     *             thrown if a type parameter with name is already defined
     * 
     */
    private void addTypeParameter(ASTElement node, String name) throws ASTVisitException {
        assert name.matches("[a-zA-Z_][a-zA-Z0-9_]*") : name + " has not been escaped propperly";

        Scope scope = state.scopeMap.get(node);

        for (Scope s = scope; s != null; s = s.parent)
            if (state.names.typeParameterSpace.containsKey(new Pair<String, Scope>(name, s)))
                throw new ASTVisitException("Tried to add type parameter " + name + " allready defined @"
                        + state.names.typeParameterSpace.get(new Pair<String, Scope>(name, s)).getLocation());

        state.names.typeParameterSpace.put(new Pair<String, Scope>(name, state.scopeMap.get(node)), node);
    }

    @Override
    protected void defaultAction(ASTElement node) throws ASTVisitException {
        for (ASTElement e : node.getChildren())
            e.visit(this);
    }

    @Override
    public void visit(VariableDeclaration node) throws ASTVisitException {
        final String name = node.getName();
        Pair<String, Scope> key = new Pair<String, Scope>(name, state.scopeMap.get(node));

        assert name.matches("[a-zA-Z_][a-zA-Z0-9_]*") : name + " has not been escaped propperly";

        if (state.names.variableSpace.containsKey(key))
            throw new ASTVisitException("Tried to add key " + key + " allready defined @"
                    + state.names.variableSpace.get(key).getLocation());

        state.names.variableSpace.put(key, node);

        for (ASTElement e : node.getChildren())
            e.visit(this);
    }

    @Override
    public void visit(ConstantDeclaration node) throws ASTVisitException {
        for (VariableDeclaration v : node.getNames())
            v.visit(this);

        if (node.hasExtends())
            for (ExtendsParent p : node.getParents()) {
                List<VariableDeclaration> usage = state.names.constantUsage.get(p.getName());
                if (null == usage)
                    state.names.constantUsage.put(p.getName(), usage = new LinkedList<VariableDeclaration>());

                usage.addAll(node.getNames());
            }
    }

    @Override
    public void visit(FunctionDeclaration node) throws ASTVisitException {
        final String key = node.getName();
        assert key.matches("[a-zA-Z_][a-zA-Z0-9_]*") : key + " has not been escaped propperly";

        if (state.names.functionSpace.containsKey(key))
            throw new ASTVisitException("Tried to add key " + key + " allready defined @"
                    + state.names.functionSpace.get(key).getLocation());

        state.names.functionSpace.put(key, node);

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

        state.names.typeSpace.put(node.getPrettyName(), node);

        // no check needed here, grammar does not allow to create harmful
        // duplicates

        for (ASTElement e : node.getChildren())
            e.visit(this);
    }

    @Override
    public void visit(UserTypeDefinition node) throws ASTVisitException {
        final String key = node.getName();
        assert key.matches("[a-zA-Z_][a-zA-Z0-9_]*") : key + " has not been escaped propperly";

        if (state.names.typeSpace.containsKey(key))
            throw new ASTVisitException("Tried to add key " + key + " allready defined @"
                    + state.names.typeSpace.get(key).getLocation());

        state.names.typeSpace.put(key, node);

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
        assert key.matches("[a-zA-Z_][a-zA-Z0-9_]*") : key + " has not been escaped propperly";

        if (state.names.procedureSpace.containsKey(key))
            throw new ASTVisitException("Tried to add key " + key + " allready defined @"
                    + state.names.procedureSpace.get(key).getLocation());

        state.names.procedureSpace.put(key, node);

        for (String s : node.getTypeParameters())
            addTypeParameter(node, s);

        for (ASTElement e : node.getChildren())
            e.visit(this);
    }

    @Override
    public void visit(ProcedureImplementation node) throws ASTVisitException {
        // dont add name, as the name has to be declared elsewhere

        // add quantified type parameters
        for (String s : node.getTypeParameters())
            addTypeParameter(node, s);

        for (ASTElement e : node.getChildren())
            e.visit(this);
    }

    @Override
    public void visit(QuantifierBody node) throws ASTVisitException {

        // add quantified type parameters
        for (String s : node.getTypeParameters())
            addTypeParameter(node, s);

        for (ASTElement e : node.getChildren())
            e.visit(this);
    }

    @Override
    public void visit(LabelStatement node) throws ASTVisitException {
        Pair<String, Scope> key = new Pair<String, Scope>(node.getName(), state.scopeMap.get(node));

        if (state.names.labelSpace.containsKey(key))
            throw new ASTVisitException("Tried to add key " + key + " allready defined @"
                    + state.names.labelSpace.get(key).getLocation());

        state.names.labelSpace.put(key, node);

        for (ASTElement e : node.getChildren())
            e.visit(this);
    }

    @Override
    public void visit(Attribute node) throws ASTVisitException {
        final String key = node.getName();

        state.names.attributeSpace.put(key, node);

        for (ASTElement e : node.getChildren())
            e.visit(this);
    }
}
