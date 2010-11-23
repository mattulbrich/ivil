package de.uka.iti.pseudo.parser.boogie.environment;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import com.sun.org.apache.bcel.internal.generic.NEW;

import de.uka.iti.pseudo.parser.boogie.ASTElement;
import de.uka.iti.pseudo.parser.boogie.ASTVisitException;
import de.uka.iti.pseudo.parser.boogie.ast.AdditionExpression;
import de.uka.iti.pseudo.parser.boogie.ast.AndExpression;
import de.uka.iti.pseudo.parser.boogie.ast.BitvectorAccessSelectionExpression;
import de.uka.iti.pseudo.parser.boogie.ast.BitvectorLiteralExpression;
import de.uka.iti.pseudo.parser.boogie.ast.BitvectorSelectExpression;
import de.uka.iti.pseudo.parser.boogie.ast.BuiltInType;
import de.uka.iti.pseudo.parser.boogie.ast.CoercionExpression;
import de.uka.iti.pseudo.parser.boogie.ast.ConcatenationExpression;
import de.uka.iti.pseudo.parser.boogie.ast.DivisionExpression;
import de.uka.iti.pseudo.parser.boogie.ast.EqualsExpression;
import de.uka.iti.pseudo.parser.boogie.ast.EqualsNotExpression;
import de.uka.iti.pseudo.parser.boogie.ast.EquivalenceExpression;
import de.uka.iti.pseudo.parser.boogie.ast.ExistsExpression;
import de.uka.iti.pseudo.parser.boogie.ast.FalseExpression;
import de.uka.iti.pseudo.parser.boogie.ast.ForallExpression;
import de.uka.iti.pseudo.parser.boogie.ast.FunctionCallExpression;
import de.uka.iti.pseudo.parser.boogie.ast.FunctionDeclaration;
import de.uka.iti.pseudo.parser.boogie.ast.GreaterExpression;
import de.uka.iti.pseudo.parser.boogie.ast.GreaterThenExpression;
import de.uka.iti.pseudo.parser.boogie.ast.IfThenElseExpression;
import de.uka.iti.pseudo.parser.boogie.ast.ImpliesExpression;
import de.uka.iti.pseudo.parser.boogie.ast.IntegerExpression;
import de.uka.iti.pseudo.parser.boogie.ast.LambdaExpression;
import de.uka.iti.pseudo.parser.boogie.ast.LessExpression;
import de.uka.iti.pseudo.parser.boogie.ast.LessThenExpression;
import de.uka.iti.pseudo.parser.boogie.ast.LoopInvariant;
import de.uka.iti.pseudo.parser.boogie.ast.MapAccessExpression;
import de.uka.iti.pseudo.parser.boogie.ast.MapType;
import de.uka.iti.pseudo.parser.boogie.ast.ModuloExpression;
import de.uka.iti.pseudo.parser.boogie.ast.MultiplicationExpression;
import de.uka.iti.pseudo.parser.boogie.ast.NegationExpression;
import de.uka.iti.pseudo.parser.boogie.ast.OldExpression;
import de.uka.iti.pseudo.parser.boogie.ast.OrExpression;
import de.uka.iti.pseudo.parser.boogie.ast.PartialLessExpression;
import de.uka.iti.pseudo.parser.boogie.ast.ProcedureDeclaration;
import de.uka.iti.pseudo.parser.boogie.ast.ProcedureImplementation;
import de.uka.iti.pseudo.parser.boogie.ast.QuantifierBody;
import de.uka.iti.pseudo.parser.boogie.ast.SimpleAssignment;
import de.uka.iti.pseudo.parser.boogie.ast.SubtractionExpression;
import de.uka.iti.pseudo.parser.boogie.ast.TemplateType;
import de.uka.iti.pseudo.parser.boogie.ast.TrueExpression;
import de.uka.iti.pseudo.parser.boogie.ast.Type;
import de.uka.iti.pseudo.parser.boogie.ast.UnaryMinusExpression;
import de.uka.iti.pseudo.parser.boogie.ast.UserTypeDefinition;
import de.uka.iti.pseudo.parser.boogie.ast.Variable;
import de.uka.iti.pseudo.parser.boogie.ast.VariableUsageExpression;
import de.uka.iti.pseudo.parser.boogie.ast.WildcardExpression;
import de.uka.iti.pseudo.parser.boogie.util.DefaultASTVisitor;

/**
 * This objects of this class have the only purpose of decorating ASTElements
 * with types. No typechecking is done. After construction of a TypeMapBuilder
 * it is safe to assume that typedefinitions are acyclic.
 * 
 * @note if this class consumes a lot of time, try to use a better datastructure
 *       for todo
 * 
 * @author timm.felden@felden.com
 * 
 */
public final class TypeMapBuilder extends DefaultASTVisitor {
    private final EnvironmentCreationState state;

    private final Set<ASTElement> todo = new HashSet<ASTElement>();

    private final Map<ASTElement, List<UniversalType>> typeParameterMap = new HashMap<ASTElement, List<UniversalType>>();

    /**
     * Searches for declaration of typevariable name.
     * 
     * @param name
     *            name of the typevariable to be searched
     * @param scope
     *            scope to start search from
     * @return null if the typevariable is not defined, the defining node else;
     */
    private ASTElement getParameterDeclaration(String name, Scope scope) {
        Pair<String, Scope> key;
        ASTElement rval;

        while (scope != null) {
            key = new Pair<String, Scope>(name, scope);
            rval = state.typeParameterSpace.get(key);
            if (null != rval)
                return rval;

            scope = scope.parent;
        }
        return null;
    }

    /**
     * Get the translated type parameter for name defined in decl.
     * 
     * @param decl
     *            defining node, this is needed to locate the definition
     * 
     * @param name
     *            name of the typeparameter
     * 
     * @return the universal type equivalent to name
     */
    private UniversalType getParameter(ASTElement decl, String name) {
        if (null == typeParameterMap.get(decl))
            return null;

        for (UniversalType t : typeParameterMap.get(decl))
            if (t.name.equals(name))
                return t;

        return null;
    }

    private List<UniversalType> addParameters(List<String> names, ASTElement node) throws ASTVisitException {
        List<UniversalType> rval = new LinkedList<UniversalType>();

        for (String s : names)
            rval.add(UniversalType.newTypeParameter(s));

        typeParameterMap.put(node, rval);
        return rval;
    }

    /**
     * this function will set the type of this node to the same type as child
     * node and will enqueue node, if typeNode has no type decoration
     * 
     * @param node
     *            the node that will receive type information
     * @param typeNode
     *            node to be queried for type information
     * 
     * @throws ASTVisitException
     *             thrown in case of duplicate call with the same argument for
     *             node, as the node will be decorated twice
     */
    private void setTypeSameAs(ASTElement node, ASTElement typeNode) throws ASTVisitException {
        if (state.typeMap.has(node))
            return;

        if (state.typeMap.has(typeNode))
            state.typeMap.add(node, state.typeMap.get(typeNode));
        else
            todo.add(node);
    }

    /**
     * Fills the typemap decoration and detects errors of type cyclic or
     * undefinded type declaration.
     * 
     * @param environmentCreationState
     *            the state to be updated
     * 
     * @throws TypeSystemException
     *             raised if there are would be cyclic or undefined universal
     *             types, which is not allowed
     * 
     * @throws ASTVisitException
     *             thrown in case of bugs in this class
     */
    public TypeMapBuilder(EnvironmentCreationState environmentCreationState) throws TypeSystemException,
            ASTVisitException {
        state = environmentCreationState;

        todo.add(state.root);

        // try to translate all types, that could not be translated yet because
        // of missing dependencies
        while (todo.size() != 0) {
            ASTElement[] next = todo.toArray(new ASTElement[todo.size()]);
            todo.clear();

            for (int i = 0; i < next.length; i++) {
                next[i].visit(this);
            }
            if (next.length == todo.size()) {
                String problems = "\n";
                for (int i = 0; i < next.length; i++)
                    problems += "\tline " + next[i].getLocation() + "  " + next[i].toString() + "\n";

                throw new TypeSystemException(
                        "types of the following nodes contain cycles or are used without definition:" + problems);
            }
        }
    }

    /**
     * Default action setting a static type, no matter where this node occurs or
     * what its arguments are.
     */
    protected void defaultAction(ASTElement node, UniversalType type) throws ASTVisitException {
        if (!state.typeMap.has(node))
            state.typeMap.add(node, type);
        else
            return;

        for (ASTElement n : node.getChildren()) {
            n.visit(this);
        }
    }

    @Override
    protected void defaultAction(ASTElement node) throws ASTVisitException {
        defaultAction(node, null);
    }

    @Override
    public void visit(FunctionDeclaration node) throws ASTVisitException {

        // translate typeparameters first, as children depend on them
        List<UniversalType> param = this.addParameters(node.getTypeParameters(), node);

        for (ASTElement n : node.getChildren())
            n.visit(this);

        if (state.typeMap.has(node.getOutParemeter())) {
            List<UniversalType> domain = new LinkedList<UniversalType>();

            for (ASTElement n : node.getInParameters()) {
                if (state.typeMap.has(n))
                    domain.add(state.typeMap.get(n));
                else {
                    todo.add(node);
                    return;
                }

            }

            state.typeMap.add(node, UniversalType.newMap(param, domain, state.typeMap.get(node.getOutParemeter()), 1));
        } else {
            todo.add(node);
            return;
        }
    }

    @Override
    public void visit(BuiltInType node) throws ASTVisitException {
        if (state.typeMap.has(node))
            return;

        state.typeMap.add(node, UniversalType.newBasicType(node));
    }

    @Override
    public void visit(UserTypeDefinition node) throws ASTVisitException {
        if (state.typeMap.has(node))
            return;

        if (null == node.getDefinition()) {
            // simple case, a new type with template arguments is defined

            state.typeMap.add(node, UniversalType.newTemplateType(node.getName(), node.getTypeParameters().size()));
        } else {

            // more complex case, we have to push type parameters and then to
            // put them into the template type field

            List<UniversalType> param = addParameters(node.getTypeParameters(), node);

            for (ASTElement n : node.getChildren())
                n.visit(this);

            if (state.typeMap.has(node.getDefinition())) {
                state.typeMap.add(node,
                        UniversalType.newTypeSynonym(node.getName(), param, state.typeMap.get(node.getDefinition())));
            } else {
                todo.add(node);
                return;
            }
        }
    }

    @Override
    public void visit(TemplateType node) throws ASTVisitException {
        if (state.typeMap.has(node))
            return;


        // resolve type name
        ASTElement declaration = getParameterDeclaration(node.getName(), state.scopeMap.get(node));
        UniversalType type = null;
        if (null != declaration) {
            // this type refers to a typeparameter or typeargument
            type = getParameter(declaration, node.getName());
        } else {
            // this type refers to a regular type
            declaration = state.typeSpace.get(node.getName());
            if (state.typeMap.has(declaration))
                type = state.typeMap.get(declaration);
        }

        if (null == type) {
            todo.add(node);
            return;
        }

        // if the type has arguments, create a new type, if not, return the
        // already existing type
        if (0 == type.templateArguments.length) {
            state.typeMap.add(node, type);
        } else {

            boolean failed = false;
            for (ASTElement e : node.getChildren()) {
                e.visit(this);
                if (!state.typeMap.has(e))
                    failed = true;
            }
            if (failed) {
                todo.add(node);
                return;
            }

            List<UniversalType> arguments = new LinkedList<UniversalType>();
            for (Type t : node.getArguments()) {
                arguments.add(state.typeMap.get(t));
            }

            state.typeMap.add(node, UniversalType.newTemplateType(type, arguments));
        }
    }

    @Override
    public void visit(MapType node) throws ASTVisitException {
        if (state.typeMap.has(node))
            return;

        List<UniversalType> param = addParameters(node.getTypeParameters(), node);

        for (ASTElement n : node.getChildren())
            n.visit(this);

        if (state.typeMap.has(node.getRange())) {
            List<UniversalType> domain = new LinkedList<UniversalType>();

            for (ASTElement n : node.getDomain()) {
                if (state.typeMap.has(n))
                    domain.add(state.typeMap.get(n));
                else {
                    todo.add(node);
                    return;
                }

            }

            state.typeMap.add(node, UniversalType.newMap(param, domain, state.typeMap.get(node.getRange()), 1));
        } else {
            todo.add(node);
            return;
        }
    }

    @Override
    public void visit(ProcedureDeclaration node) throws ASTVisitException {
        // functions can have polymorphic types, so push typeargs
        List<UniversalType> param = addParameters(node.getTypeParameters(), node);

        for (ASTElement n : node.getChildren())
            n.visit(this);

        for (ASTElement n : node.getOutParameters()) {
            if (!state.typeMap.has(n)) {
                todo.add(node);
                return;
            }

        }
        List<UniversalType> domain = new LinkedList<UniversalType>();

        for (ASTElement n : node.getInParameters()) {
            if (state.typeMap.has(n))
                domain.add(state.typeMap.get(n));
            else {
                todo.add(node);
                return;
            }

        }

        int outLength = node.getOutParameters().size();

        // procedures can have return type void. as there is no void type, 0
        // bools are returned, what will have the same effect
        state.typeMap.add(node, UniversalType.newMap(param, domain, 0 == outLength ? UniversalType.newBool()
                : state.typeMap.get(node.getOutParameters().get(0)), outLength));
    }

    @Override
    public void visit(ProcedureImplementation node) throws ASTVisitException {
        // FIXME bug revealed by
        // testBoogieParseexamples_boogie_test_closable_test20_ProcParamReordering

        List<UniversalType> param = addParameters(node.getTypeParameters(), node);

        for (ASTElement n : node.getChildren())
            n.visit(this);

        for (ASTElement n : node.getOutParameters()) {
            if (!state.typeMap.has(n)) {
                todo.add(node);
                return;
            }

        }
        List<UniversalType> domain = new LinkedList<UniversalType>();

        for (ASTElement n : node.getInParameters()) {
            if (state.typeMap.has(n))
                domain.add(state.typeMap.get(n));
            else {
                todo.add(node);
                return;
            }

        }

        int outLength = node.getOutParameters().size();

        // procedures can have return type void. as there is no void type, 0
        // bools are returned, what will have the same effect
        state.typeMap.add(node, UniversalType.newMap(param, domain, 0 == outLength ? UniversalType.newBool()
                : state.typeMap.get(node.getOutParameters().get(0)), outLength));

    }

    @Override
    public void visit(Variable node) throws ASTVisitException {
        for (ASTElement n : node.getChildren())
            n.visit(this);

        setTypeSameAs(node, node.getType());
    }

    @Override
    public void visit(SimpleAssignment node) throws ASTVisitException {
        // TODO das hier überarbeiten, vielleicht ist hier ein redesign
        // notwendig
    }

    @Override
    public void visit(PartialLessExpression node) throws ASTVisitException {
        defaultAction(node, UniversalType.newBool());
    }

    @Override
    public void visit(ConcatenationExpression node) throws ASTVisitException {
        // FIXME bitvector
    }

    @Override
    public void visit(BitvectorSelectExpression node) throws ASTVisitException {
        defaultAction(node, UniversalType.newBitvector(1 + node.getFirst() - node.getLast()));
    }

    @Override
    public void visit(BitvectorAccessSelectionExpression node) throws ASTVisitException {
        // FIXME bitvector

    }

    @Override
    public void visit(MapAccessExpression node) throws ASTVisitException {
        for (ASTElement n : node.getChildren())
            n.visit(this);

        // get base type from name expression

        // get replace type parameters in base type by infered types from
        // arguments

        // decorate new type

        // TODO implementation
    }

    @Override
    public void visit(BitvectorLiteralExpression node) throws ASTVisitException {
        defaultAction(node, UniversalType.newBitvector(node.getDimension()));
    }

    @Override
    public void visit(FunctionCallExpression node) throws ASTVisitException {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(VariableUsageExpression node) throws ASTVisitException {
        for (ASTElement n : node.getChildren())
            n.visit(this);

        // find declaration of this variable to get the type of the declaration
        Scope scope = state.scopeMap.get(node);
        ASTElement definition = null;
        for (; definition == null && scope != null; scope = scope.parent) {
            definition = state.variableSpace.get(new Pair<String, Scope>(node.getName(), scope));
        }

        setTypeSameAs(node, definition);
    }

    @Override
    public void visit(OldExpression node) throws ASTVisitException {
        for (ASTElement n : node.getChildren())
            n.visit(this);

        setTypeSameAs(node, node.getOperands().get(0));
    }

    @Override
    public void visit(QuantifierBody node) throws ASTVisitException {
        // the quantifier body has a maptype, that maps the quantified variables
        // to the result of the expression
        if (state.typeMap.has(node))
            return;

        List<UniversalType> param = addParameters(node.getTypeParameters(), node);

        for (ASTElement n : node.getChildren())
            n.visit(this);

        if (state.typeMap.has(node.getBody())) {
            List<UniversalType> domain = new LinkedList<UniversalType>();

            for (ASTElement n : node.getQuantifiedVariables()) {
                if (state.typeMap.has(n))
                    domain.add(state.typeMap.get(n));
                else {
                    todo.add(node);
                    return;
                }

            }

            state.typeMap.add(node, UniversalType.newMap(param, domain, state.typeMap.get(node.getBody()), 1));
        } else {
            todo.add(node);
            return;
        }
    }

    @Override
    public void visit(ForallExpression node) throws ASTVisitException {
        defaultAction(node, UniversalType.newBool());
    }

    @Override
    public void visit(ExistsExpression node) throws ASTVisitException {
        defaultAction(node, UniversalType.newBool());
    }

    @Override
    public void visit(LambdaExpression node) throws ASTVisitException {
        for (ASTElement n : node.getChildren())
            n.visit(this);

        setTypeSameAs(node, node.getBody());
    }

    @Override
    public void visit(IfThenElseExpression node) throws ASTVisitException {
        for (ASTElement n : node.getChildren())
            n.visit(this);

        setTypeSameAs(node, node.getThen());
    }

    @Override
    public void visit(CoercionExpression node) throws ASTVisitException {
        for (ASTElement n : node.getChildren())
            n.visit(this);

        setTypeSameAs(node, node.getType());
    }

    @Override
    public void visit(LoopInvariant node) throws ASTVisitException {
        defaultAction(node, UniversalType.newBool());
    }

    @Override
    public void visit(WildcardExpression node) throws ASTVisitException {
        defaultAction(node, UniversalType.newBool());
    }

    @Override
    public void visit(AdditionExpression node) throws ASTVisitException {
        defaultAction(node, UniversalType.newInt());
    }

    @Override
    public void visit(SubtractionExpression node) throws ASTVisitException {
        defaultAction(node, UniversalType.newInt());
    }

    @Override
    public void visit(EquivalenceExpression node) throws ASTVisitException {
        defaultAction(node, UniversalType.newBool());
    }

    @Override
    public void visit(ImpliesExpression node) throws ASTVisitException {
        defaultAction(node, UniversalType.newBool());
    }

    @Override
    public void visit(AndExpression node) throws ASTVisitException {
        defaultAction(node, UniversalType.newBool());
    }

    @Override
    public void visit(OrExpression node) throws ASTVisitException {
        defaultAction(node, UniversalType.newBool());
    }

    @Override
    public void visit(EqualsExpression node) throws ASTVisitException {
        defaultAction(node, UniversalType.newBool());
    }

    @Override
    public void visit(EqualsNotExpression node) throws ASTVisitException {
        defaultAction(node, UniversalType.newBool());
    }

    @Override
    public void visit(LessExpression node) throws ASTVisitException {
        defaultAction(node, UniversalType.newBool());
    }

    @Override
    public void visit(LessThenExpression node) throws ASTVisitException {
        defaultAction(node, UniversalType.newBool());
    }

    @Override
    public void visit(GreaterExpression node) throws ASTVisitException {
        defaultAction(node, UniversalType.newBool());
    }

    @Override
    public void visit(GreaterThenExpression node) throws ASTVisitException {
        defaultAction(node, UniversalType.newBool());
    }

    @Override
    public void visit(MultiplicationExpression node) throws ASTVisitException {
        defaultAction(node, UniversalType.newInt());
    }

    @Override
    public void visit(DivisionExpression node) throws ASTVisitException {
        defaultAction(node, UniversalType.newInt());
    }

    @Override
    public void visit(ModuloExpression node) throws ASTVisitException {
        defaultAction(node, UniversalType.newInt());
    }

    @Override
    public void visit(UnaryMinusExpression node) throws ASTVisitException {
        defaultAction(node, UniversalType.newInt());
    }

    @Override
    public void visit(NegationExpression node) throws ASTVisitException {
        defaultAction(node, UniversalType.newBool());
    }

    @Override
    public void visit(IntegerExpression node) throws ASTVisitException {
        defaultAction(node, UniversalType.newInt());
    }

    @Override
    public void visit(TrueExpression node) throws ASTVisitException {
        defaultAction(node, UniversalType.newBool());
    }

    @Override
    public void visit(FalseExpression node) throws ASTVisitException {
        defaultAction(node, UniversalType.newBool());
    }

}
