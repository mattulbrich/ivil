package de.uka.iti.pseudo.environment.boogie;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.environment.EnvironmentException;
import de.uka.iti.pseudo.environment.Sort;
import de.uka.iti.pseudo.parser.boogie.ASTElement;
import de.uka.iti.pseudo.parser.boogie.ASTVisitException;
import de.uka.iti.pseudo.parser.boogie.ast.AdditionExpression;
import de.uka.iti.pseudo.parser.boogie.ast.AndExpression;
import de.uka.iti.pseudo.parser.boogie.ast.BitvectorAccessSelectionExpression;
import de.uka.iti.pseudo.parser.boogie.ast.BitvectorLiteralExpression;
import de.uka.iti.pseudo.parser.boogie.ast.BitvectorSelectExpression;
import de.uka.iti.pseudo.parser.boogie.ast.BuiltInType;
import de.uka.iti.pseudo.parser.boogie.ast.CallForallStatement;
import de.uka.iti.pseudo.parser.boogie.ast.CallStatement;
import de.uka.iti.pseudo.parser.boogie.ast.CodeBlock;
import de.uka.iti.pseudo.parser.boogie.ast.CodeExpression;
import de.uka.iti.pseudo.parser.boogie.ast.CodeExpressionReturn;
import de.uka.iti.pseudo.parser.boogie.ast.CoercionExpression;
import de.uka.iti.pseudo.parser.boogie.ast.ConcatenationExpression;
import de.uka.iti.pseudo.parser.boogie.ast.DivisionExpression;
import de.uka.iti.pseudo.parser.boogie.ast.EqualsExpression;
import de.uka.iti.pseudo.parser.boogie.ast.EqualsNotExpression;
import de.uka.iti.pseudo.parser.boogie.ast.EquivalenceExpression;
import de.uka.iti.pseudo.parser.boogie.ast.ExistsExpression;
import de.uka.iti.pseudo.parser.boogie.ast.ExtendsExpression;
import de.uka.iti.pseudo.parser.boogie.ast.FalseExpression;
import de.uka.iti.pseudo.parser.boogie.ast.ForallExpression;
import de.uka.iti.pseudo.parser.boogie.ast.FunctionCallExpression;
import de.uka.iti.pseudo.parser.boogie.ast.FunctionDeclaration;
import de.uka.iti.pseudo.parser.boogie.ast.GreaterEqualExpression;
import de.uka.iti.pseudo.parser.boogie.ast.GreaterExpression;
import de.uka.iti.pseudo.parser.boogie.ast.IfThenElseExpression;
import de.uka.iti.pseudo.parser.boogie.ast.ImpliesExpression;
import de.uka.iti.pseudo.parser.boogie.ast.IntegerExpression;
import de.uka.iti.pseudo.parser.boogie.ast.LambdaExpression;
import de.uka.iti.pseudo.parser.boogie.ast.LessEqualExpression;
import de.uka.iti.pseudo.parser.boogie.ast.LessExpression;
import de.uka.iti.pseudo.parser.boogie.ast.LoopInvariant;
import de.uka.iti.pseudo.parser.boogie.ast.MapAccessExpression;
import de.uka.iti.pseudo.parser.boogie.ast.MapType;
import de.uka.iti.pseudo.parser.boogie.ast.MapUpdateExpression;
import de.uka.iti.pseudo.parser.boogie.ast.ModuloExpression;
import de.uka.iti.pseudo.parser.boogie.ast.MultiplicationExpression;
import de.uka.iti.pseudo.parser.boogie.ast.NegationExpression;
import de.uka.iti.pseudo.parser.boogie.ast.OldExpression;
import de.uka.iti.pseudo.parser.boogie.ast.OrExpression;
import de.uka.iti.pseudo.parser.boogie.ast.ProcedureDeclaration;
import de.uka.iti.pseudo.parser.boogie.ast.ProcedureImplementation;
import de.uka.iti.pseudo.parser.boogie.ast.QuantifierBody;
import de.uka.iti.pseudo.parser.boogie.ast.SimpleAssignment;
import de.uka.iti.pseudo.parser.boogie.ast.SubtractionExpression;
import de.uka.iti.pseudo.parser.boogie.ast.ASTTypeApplication;
import de.uka.iti.pseudo.parser.boogie.ast.TrueExpression;
import de.uka.iti.pseudo.parser.boogie.ast.UnaryMinusExpression;
import de.uka.iti.pseudo.parser.boogie.ast.UserTypeDefinition;
import de.uka.iti.pseudo.parser.boogie.ast.VariableDeclaration;
import de.uka.iti.pseudo.parser.boogie.ast.VariableUsageExpression;
import de.uka.iti.pseudo.parser.boogie.ast.WildcardExpression;
import de.uka.iti.pseudo.parser.boogie.util.DefaultASTVisitor;
import de.uka.iti.pseudo.term.SchemaType;
import de.uka.iti.pseudo.term.TermException;
import de.uka.iti.pseudo.term.Type;
import de.uka.iti.pseudo.term.TypeApplication;
import de.uka.iti.pseudo.term.TypeVariable;
import de.uka.iti.pseudo.term.UnificationException;
import de.uka.iti.pseudo.term.creation.TypingContext;

/**
 * This visitor decorates ASTElements with types. Typechecking is done using the
 * core type checking mechanism(unification).
 * 
 * @author timm.felden@felden.com
 * 
 */
public final class TypeMapBuilder extends DefaultASTVisitor {

    private final EnvironmentCreationState state;

    private final TypingContext context = new TypingContext();

    private final Map<ASTElement, List<Type>> typeParameterMap = new HashMap<ASTElement, List<Type>>();

    /**
     * Searches for declaration of type variable name.
     * 
     * @param name
     *            name of the type variable to be searched
     * @param scope
     *            scope to start search from
     * @return null if the type variable is not defined, the defining node else;
     */
    private ASTElement getParameterDeclaration(String name, Scope scope) {
        Pair<String, Scope> key;
        ASTElement rval;

        while (scope != null) {
            key = new Pair<String, Scope>(name, scope);
            rval = state.names.typeParameterSpace.get(key);
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
    private Type getParameter(ASTElement decl, String name) {
        if (null == typeParameterMap.get(decl))
            return null;

        for (Type t : typeParameterMap.get(decl))
            if (t.toString().equals('\'' + name)) // TODO needs prefixing?
                return t;

        return null;
    }

    private List<Type> addParameters(List<String> names, ASTElement node) throws ASTVisitException {
        List<Type> rval = new LinkedList<Type>();

        for (String s : names)
            rval.add(new TypeVariable(s));

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

        // this is ok, if the dependency is somewhere else in the tree
        if (!state.typeMap.has(typeNode))
            typeNode.visit(this);

        // this is not ok, as it meanst, that the dependency is somehow cyclic
        if (!state.typeMap.has(typeNode))
            throw new ASTVisitException(node.getLocation() + ":  The node " + node
                    + " could not be typed because it depends on the untyped node " + typeNode);

        state.typeMap.add(node, state.typeMap.get(typeNode));
    }

    /**
     * @return a bitvector type
     */
    private Type getBitvectorType() {
        try {
            return state.env.mkType("bitvector");
        } catch (EnvironmentException e) {
            e.printStackTrace();
            assert false : "dont mess with bitvector.p!";
        } catch (TermException e) {
            e.printStackTrace();
            assert false : "dont mess with bitvector.p!";
        }
        return null;
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

        visit(state.root);
    }

    /**
     * Default action setting a static type, no matter where this node occurs or
     * what its arguments are.
     */
    protected void defaultAction(ASTElement node, Type type) throws ASTVisitException {
        if (!state.typeMap.has(node))
            state.typeMap.add(node, type);
        else
            return;

        for (ASTElement n : node.getChildren())
            n.visit(this);
    }

    @Override
    protected void defaultAction(ASTElement node) throws ASTVisitException {
        defaultAction(node, null);
    }

    @Override
    public void visit(FunctionDeclaration node) throws ASTVisitException {
        // TODO
        // // translate typeparameters first, as children depend on them
        // List<Type> param = this.addParameters(node.getTypeParameters(),
        // node);
        //
        // for (ASTElement n : node.getChildren())
        // n.visit(this);
        //
        // if (state.typeMap.has(node.getOutParemeter())) {
        // List<Type> domain = new LinkedList<Type>();
        //
        // for (ASTElement n : node.getInParameters()) {
        // if (state.typeMap.has(n))
        // domain.add(state.typeMap.get(n));
        // else {
        // todo.add(node);
        // return;
        // }
        //
        // }
        //
        // try {
        // state.typeMap.add(node,
        // ASTType.newMap(param, domain,
        // state.typeMap.get(node.getOutParemeter()), false));
        // } catch (IllegalArgumentException e) {
        // // can happen if the maptype is illformed like <a>[]int or
        // // <a>[int]a
        // throw new ASTVisitException("\nmap creation failed @ " +
        // node.getLocation(), e);
        // }
        // } else {
        // todo.add(node);
        // return;
        // }
    }

    @Override
    public void visit(BuiltInType node) throws ASTVisitException {
        if (state.typeMap.has(node))
            return;

        state.typeMap.add(node, node.newBasicType(state.env));
    }

    @Override
    public void visit(UserTypeDefinition node) throws ASTVisitException {
        if (state.typeMap.has(node))
            return;

        if (null == node.getDefinition()) {
            // simple case, a new type (corresponds to sort in ivil) with
            // arguments is defined
            String name;
            try {
                state.env.addSort(new Sort(name = ("utt_" + node.getName()), node.getTypeParameters().size(), node));
            
                Type[] args = new Type[node.getTypeParameters().size()];

                for (int i = 0; i < args.length; i++)
                    args[i] = new TypeVariable("arg" + i);

                Type result = state.env.mkType(name, args);

                state.typeMap.add(node, result);

            } catch (EnvironmentException e) {
                e.printStackTrace();
            } catch (TermException e) {
                e.printStackTrace();
            }
        } else {
            // TODO
        //
        // // more complex case, we have to push type parameters and then to
        // // put them into the template type field
        //
        // List<Type> param = addParameters(node.getTypeParameters(), node);
        //
        // for (ASTElement n : node.getChildren())
        // n.visit(this);
        //
        // if (state.typeMap.has(node.getDefinition())) {
        // state.typeMap.add(node, ASTType.newTypeSynonym(node.getName(), param,
        // state.typeMap.get(node.getDefinition()), state.names.typeSpace));
        // } else {
        // todo.add(node);
        // return;
            // }
        }
    }

    @Override
    public void visit(ASTTypeApplication node) throws ASTVisitException {

        if (state.typeMap.has(node))
            return;

        // resolve type name
        ASTElement declaration = getParameterDeclaration(node.getName(), state.scopeMap.get(node));
        Type type = null;
        if (null != declaration) {
            // this type refers to a typeparameter or typeargument
            type = getParameter(declaration, node.getName());
        } else {
            // this type refers to a regular type
            declaration = state.names.typeSpace.get(node.getName());
            if (null == declaration)
                throw new ASTVisitException(node.getLocation() + ":  undeclared Type " + node.getName());

            if (!state.typeMap.has(declaration))
                declaration.visit(this);

            // get type
            type = state.typeMap.get(declaration);

            // get type arguments
            Type[] arg_t = new Type[node.getArguments().size()];

            for (int i = 0; i < node.getArguments().size(); i++) {
                final ASTElement child = node.getArguments().get(i);
                child.visit(this);
                arg_t[i] = state.typeMap.get(child);
            }
            try {
                type = state.env.mkType(((TypeApplication) type).getSort().getName(), arg_t);

            } catch (EnvironmentException e) {
                e.printStackTrace();
            } catch (TermException e) {
                e.printStackTrace();
            }
        }

        if (null == type)
            throw new ASTVisitException(node.getLocation() + ":  undeclared type " + node.getName());

        state.typeMap.add(node, type);

        // TODO change behavior to allow for treatment of ASTTypeAlias

        // // if the type has arguments, create a new type, if not, return the
        // // already existing type
        // if (0 == type.templateArguments.length) {
        // state.typeMap.add(node, type);
        // } else {
        //
        // boolean failed = false;
        // for (ASTElement e : node.getChildren()) {
        // e.visit(this);
        // if (!state.typeMap.has(e))
        // failed = true;
        // }
        // if (failed) {
        // todo.add(node);
        // return;
        // }
        //
        // List<Type> arguments = new LinkedList<Type>();
        // for (ASTType t : node.getArguments()) {
        // arguments.add(state.typeMap.get(t));
        // }
        //
        // try {
        // state.typeMap.add(node, ASTType.newTemplateType(type, arguments));
        // } catch (IllegalArgumentException e) {
        // // can happen if the maptype is illformed like <a>[]int or
        // // <a>[int]a
        // throw new ASTVisitException("\nmap creation failed @ " +
        // node.getLocation(), e);
        // }
        // }
    }

    @Override
    public void visit(MapType node) throws ASTVisitException {
        if (state.typeMap.has(node))
            return;

        addParameters(node.getTypeParameters(), node);

        for (ASTElement n : node.getChildren())
            n.visit(this);

        state.typeMap.add(node, state.mapDB.getType(node, state));
    }

    @Override
    public void visit(ProcedureDeclaration node) throws ASTVisitException {
        for (ASTElement e : node.getChildren())
            e.visit(this);

        state.typeMap.add(node, null);
        // TODO
        // // functions can have polymorphic types, so push typeargs
        // List<Type> param = addParameters(node.getTypeParameters(), node),
        // empty = new LinkedList<Type>();
        //
        // for (ASTElement n : node.getChildren())
        // n.visit(this);
        //
        // List<Type> range = new LinkedList<Type>();
        // for (ASTElement n : node.getOutParameters()) {
        // if (state.typeMap.has(n))
        // range.add(state.typeMap.get(n));
        // else {
        // todo.add(node);
        // return;
        // }
        // }
        // List<Type> domain = new LinkedList<Type>();
        //
        // for (ASTElement n : node.getInParameters()) {
        // if (state.typeMap.has(n))
        // domain.add(state.typeMap.get(n));
        // else {
        // todo.add(node);
        // return;
        // }
        //
        // }
        //
        // // this is bit of a hack to represent types of procedures, but it
        // workes
        // state.typeMap.add(node,
        // ASTType.newMap(param, domain, ASTType.newMap(empty, range,
        // Environment.getBoolType(),
        // true), true));
    }

    @Override
    public void visit(ProcedureImplementation node) throws ASTVisitException {
        // TODO
        //
        // List<Type> param = addParameters(node.getTypeParameters(), node),
        // empty = new LinkedList<Type>();
        //
        // for (ASTElement n : node.getChildren())
        // n.visit(this);
        //
        // List<Type> range = new LinkedList<Type>();
        // for (ASTElement n : node.getOutParameters()) {
        // if (state.typeMap.has(n))
        // range.add(state.typeMap.get(n));
        // else {
        // todo.add(node);
        // return;
        // }
        // }
        //
        // List<Type> domain = new LinkedList<Type>();
        //
        // for (ASTElement n : node.getInParameters()) {
        // if (state.typeMap.has(n))
        // domain.add(state.typeMap.get(n));
        // else {
        // todo.add(node);
        // return;
        // }
        //
        // }
        //
        // // this is bit of a hack to represent types of procedures, but it
        // workes
        // state.typeMap.add(node,
        // ASTType.newMap(param, domain, ASTType.newMap(empty, range,
        // Environment.getBoolType(),
        // true), true));
    }

    @Override
    public void visit(VariableDeclaration node) throws ASTVisitException {
        for (ASTElement n : node.getChildren())
            n.visit(this);

        setTypeSameAs(node, node.getType());
    }

    @Override
    public void visit(SimpleAssignment node) throws ASTVisitException {
        if (state.typeMap.has(node))
            return;

        for (ASTElement n : node.getChildren())
            n.visit(this);

        Type t = state.typeMap.get(node.getTarget());

        // TODO state.typeMap.add(node, null == t.range ? t : t.range);
        state.typeMap.add(node, t);
    }

    @Override
    public void visit(ExtendsExpression node) throws ASTVisitException {
        defaultAction(node, Environment.getBoolType());
    }

    /**
     * @note: type checking for concatenation expressions is weak, i.e. no
     *        dimension checking is done
     */
    @Override
    public void visit(ConcatenationExpression node) throws ASTVisitException {
        defaultAction(node, getBitvectorType());
    }

    @Override
    public void visit(BitvectorSelectExpression node) throws ASTVisitException {
        defaultAction(node, getBitvectorType());
    }

    @Override
    public void visit(BitvectorAccessSelectionExpression node) throws ASTVisitException {
        if (state.typeMap.has(node))
            return;

        for (ASTElement n : node.getChildren())
            n.visit(this);

        setTypeSameAs(node, node.getOperands().get(0));
    }

    @Override
    public void visit(MapAccessExpression node) throws ASTVisitException {
        if (state.typeMap.has(node))
            return;

        for (ASTElement n : node.getChildren())
            n.visit(this);

        Type t = state.typeMap.get(node.getName());
        Type r = state.mapDB.getRange(t);
        if (r instanceof TypeVariable) {
            // infer type
            Type[] signature = state.mapDB.getSignature(context, t);

            try {
                for (int i = 0; i < node.getOperands().size(); i++)
                    context.solveConstraint(signature[i + 1], state.typeMap.get(node.getOperands().get(i)));
            } catch (UnificationException e) {
                throw new ASTVisitException(node.getLocation() + ":  illtyped mapaccess", e);
            }

            Type i = context.getInstantiation().get(((SchemaType) signature[0]).getVariableName());

            if (null == i && !state.printDebugInformation())
                throw new ASTVisitException(node.getLocation()
                        + ":  non local type inference is currently not supported");

            state.typeMap.add(node, i);
        } else {
            state.typeMap.add(node, r);
        }
    }

    @Override
    public void visit(MapUpdateExpression node) throws ASTVisitException {
        if (state.typeMap.has(node))
            return;

        for (ASTElement n : node.getChildren())
            n.visit(this);

        setTypeSameAs(node, node.getName());
    }

    @Override
    public void visit(BitvectorLiteralExpression node) throws ASTVisitException {
        defaultAction(node, getBitvectorType());
    }

    @Override
    public void visit(FunctionCallExpression node) throws ASTVisitException {
        if (state.typeMap.has(node))
            return;

        for (ASTElement n : node.getChildren())
            n.visit(this);

        ASTElement decl = state.names.functionSpace.get(node.getName());
        if (null == decl)
            throw new ASTVisitException("Function " + node.getName() + " is used but never declared anywhere.");

        if (!state.typeMap.has(decl)) {
            // TODO todo.add(node);
            return;
        }

        // try {
        // // TODO state.typeMap.add(node,
        // // ASTType.newInferedType(state.typeMap.get(decl), node,
        // // state).range);
        // } catch (TypeSystemException e) {
        // throw new ASTVisitException(node.getLocation() + e.getMessage());
        // }
    }

    @Override
    public void visit(VariableUsageExpression node) throws ASTVisitException {
        for (ASTElement n : node.getChildren())
            n.visit(this);

        if (node.getName().equals("*")) {
            // anonymous variable that will not be used anywhere, the type will
            // be as defined by the parent, which is a procedure implementation
            // or declaration

            // as its hard to get type from here, the type allready was set by
            // parent
            assert state.typeMap.has(node);

        } else {
            // find declaration of this variable to get the type of the
            // declaration
            Scope scope = state.scopeMap.get(node);
            ASTElement definition = null;
            for (; definition == null && scope != null; scope = scope.parent) {
                definition = state.names.variableSpace.get(new Pair<String, Scope>(node.getName(), scope));
            }

            if (null == definition)
                throw new ASTVisitException(node.getLocation() + ":  undefined Variable " + node.getName());

            setTypeSameAs(node, definition);
        }
    }

    @Override
    public void visit(OldExpression node) throws ASTVisitException {
        for (ASTElement n : node.getChildren())
            n.visit(this);

        setTypeSameAs(node, node.getOperands().get(0));
    }

    @Override
    public void visit(QuantifierBody node) throws ASTVisitException {
        // TODO
        // // the quantifier body has a maptype, that maps the quantified
        // variables
        // // to the result of the expression
        // if (state.typeMap.has(node))
        // return;
        //
        // List<Type> param = addParameters(node.getTypeParameters(), node);
        //
        // for (ASTElement n : node.getChildren())
        // n.visit(this);
        //
        // if (state.typeMap.has(node.getBody())) {
        // List<Type> domain = new LinkedList<Type>();
        //
        // for (ASTElement n : node.getQuantifiedVariables()) {
        // if (state.typeMap.has(n))
        // domain.add(state.typeMap.get(n));
        // else {
        // todo.add(node);
        // return;
        // }
        //
        // }
        //
        // try {
        // state.typeMap.add(node, ASTType.newMap(param, domain,
        // state.typeMap.get(node.getBody()),
        // node.getParent() instanceof LambdaExpression));
        // } catch (IllegalArgumentException e) {
        // // can happen if the maptype is illformed like <a>[]int or
        // // <a>[int]a
        // throw new ASTVisitException("\nmap creation failed @ " +
        // node.getLocation(), e);
        // }
        //
        // } else {
        // todo.add(node);
        // return;
        // }
    }

    @Override
    public void visit(ForallExpression node) throws ASTVisitException {
        defaultAction(node, Environment.getBoolType());
    }

    @Override
    public void visit(ExistsExpression node) throws ASTVisitException {
        defaultAction(node, Environment.getBoolType());
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
        defaultAction(node, Environment.getBoolType());
    }

    @Override
    public void visit(WildcardExpression node) throws ASTVisitException {
        // note: this method will only be called on boolean wildcards, if
        // wildcard occurs in call statements, the parent already typed this

        if (state.typeMap.has(node))
            return;

        defaultAction(node, Environment.getBoolType());
    }

    @Override
    public void visit(AdditionExpression node) throws ASTVisitException {
        defaultAction(node, Environment.getIntType());
    }

    @Override
    public void visit(SubtractionExpression node) throws ASTVisitException {
        defaultAction(node, Environment.getIntType());
    }

    @Override
    public void visit(EquivalenceExpression node) throws ASTVisitException {
        defaultAction(node, Environment.getBoolType());
    }

    @Override
    public void visit(ImpliesExpression node) throws ASTVisitException {
        defaultAction(node, Environment.getBoolType());
    }

    @Override
    public void visit(AndExpression node) throws ASTVisitException {
        defaultAction(node, Environment.getBoolType());
    }

    @Override
    public void visit(OrExpression node) throws ASTVisitException {
        defaultAction(node, Environment.getBoolType());
    }

    @Override
    public void visit(EqualsExpression node) throws ASTVisitException {
        defaultAction(node, Environment.getBoolType());
    }

    @Override
    public void visit(EqualsNotExpression node) throws ASTVisitException {
        defaultAction(node, Environment.getBoolType());
    }

    @Override
    public void visit(LessExpression node) throws ASTVisitException {
        defaultAction(node, Environment.getBoolType());
    }

    @Override
    public void visit(LessEqualExpression node) throws ASTVisitException {
        defaultAction(node, Environment.getBoolType());
    }

    @Override
    public void visit(GreaterExpression node) throws ASTVisitException {
        defaultAction(node, Environment.getBoolType());
    }

    @Override
    public void visit(GreaterEqualExpression node) throws ASTVisitException {
        defaultAction(node, Environment.getBoolType());
    }

    @Override
    public void visit(MultiplicationExpression node) throws ASTVisitException {
        defaultAction(node, Environment.getIntType());
    }

    @Override
    public void visit(DivisionExpression node) throws ASTVisitException {
        defaultAction(node, Environment.getIntType());
    }

    @Override
    public void visit(ModuloExpression node) throws ASTVisitException {
        defaultAction(node, Environment.getIntType());
    }

    @Override
    public void visit(UnaryMinusExpression node) throws ASTVisitException {
        defaultAction(node, Environment.getIntType());
    }

    @Override
    public void visit(NegationExpression node) throws ASTVisitException {
        defaultAction(node, Environment.getBoolType());
    }

    @Override
    public void visit(IntegerExpression node) throws ASTVisitException {
        defaultAction(node, Environment.getIntType());
    }

    @Override
    public void visit(TrueExpression node) throws ASTVisitException {
        defaultAction(node, Environment.getBoolType());
    }

    @Override
    public void visit(FalseExpression node) throws ASTVisitException {
        defaultAction(node, Environment.getBoolType());
    }

    @Override
    public void visit(CodeBlock node) throws ASTVisitException {
        for (ASTElement n : node.getChildren())
            n.visit(this);

        setTypeSameAs(node, node.getChildren().get(node.getChildren().size() - 1));
    }

    @Override
    public void visit(CodeExpression node) throws ASTVisitException {
        for (ASTElement n : node.getChildren())
            n.visit(this);

        for (ASTElement n : node.getChildren()) {
            if (state.typeMap.get(n) != null) {
                state.typeMap.add(node, state.typeMap.get(n));
                return;
            }
        }
        throw new ASTVisitException("CodeExpression @" + node.getLocation() + " has no return statement!");
    }

    @Override
    public void visit(CodeExpressionReturn node) throws ASTVisitException {
        for (ASTElement n : node.getChildren())
            n.visit(this);

        setTypeSameAs(node, node.getChildren().get(0));
    }

    @Override
    public void visit(CallStatement node) throws ASTVisitException {

        // type wildcards in arguments
        for (int i = 0; i < node.getArguments().size(); i++) {
            ASTElement n = node.getArguments().get(i);
            if (n instanceof WildcardExpression) {
                setTypeSameAs(n, state.names.procedureSpace.get(node.getName()).getInParameters().get(i));

                // TODO :
                // if
                // (state.typeMap.get(state.names.procedureSpace.get(node.getName()).getInParameters().get(i)).isTypeVariable)
                // throw new ASTVisitException("\n" + node.getLocation() +
                // "argument #" + (i + 1)
                // + " has unresolvable type");
            }
        }

        // type wildcards in results
        for (int i = 0; i < node.getOutParam().size(); i++) {
            VariableUsageExpression n = node.getOutParam().get(i);
            if (n.getName().equals("*")) {
                setTypeSameAs(n, state.names.procedureSpace.get(node.getName()).getOutParameters().get(i));
            }
        }

        defaultAction(node);
    }

    @Override
    public void visit(CallForallStatement node) throws ASTVisitException {

        // type wildcards in arguments
        for (int i = 0; i < node.getArguments().size(); i++) {
            ASTElement n = node.getArguments().get(i);
            if (n instanceof WildcardExpression) {
                setTypeSameAs(n, state.names.procedureSpace.get(node.getName()).getInParameters().get(i));
                // TODO
                // if
                // (state.typeMap.get(state.names.procedureSpace.get(node.getName()).getInParameters().get(i)).isTypeVariable)
                // throw new ASTVisitException("\n" + node.getLocation() +
                // "argument #" + (i + 1)
                // + " has unresolvable type");
            }
        }

        defaultAction(node);
    }
}