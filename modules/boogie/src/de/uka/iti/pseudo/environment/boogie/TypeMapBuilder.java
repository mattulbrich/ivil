package de.uka.iti.pseudo.environment.boogie;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.environment.EnvironmentException;
import de.uka.iti.pseudo.environment.Function;
import de.uka.iti.pseudo.environment.Sort;
import de.uka.iti.pseudo.parser.boogie.ASTElement;
import de.uka.iti.pseudo.parser.boogie.ASTVisitException;
import de.uka.iti.pseudo.parser.boogie.ast.AxiomDeclaration;
import de.uka.iti.pseudo.parser.boogie.ast.CallForallStatement;
import de.uka.iti.pseudo.parser.boogie.ast.CallStatement;
import de.uka.iti.pseudo.parser.boogie.ast.CodeBlock;
import de.uka.iti.pseudo.parser.boogie.ast.CodeExpressionReturn;
import de.uka.iti.pseudo.parser.boogie.ast.FunctionDeclaration;
import de.uka.iti.pseudo.parser.boogie.ast.LoopInvariant;
import de.uka.iti.pseudo.parser.boogie.ast.ProcedureDeclaration;
import de.uka.iti.pseudo.parser.boogie.ast.ProcedureImplementation;
import de.uka.iti.pseudo.parser.boogie.ast.SimpleAssignment;
import de.uka.iti.pseudo.parser.boogie.ast.Trigger;
import de.uka.iti.pseudo.parser.boogie.ast.VariableDeclaration;
import de.uka.iti.pseudo.parser.boogie.ast.expression.AndExpression;
import de.uka.iti.pseudo.parser.boogie.ast.expression.BinaryIntegerExpression;
import de.uka.iti.pseudo.parser.boogie.ast.expression.BitvectorAccessSelectionExpression;
import de.uka.iti.pseudo.parser.boogie.ast.expression.BitvectorLiteralExpression;
import de.uka.iti.pseudo.parser.boogie.ast.expression.BitvectorSelectExpression;
import de.uka.iti.pseudo.parser.boogie.ast.expression.CodeExpression;
import de.uka.iti.pseudo.parser.boogie.ast.expression.CoercionExpression;
import de.uka.iti.pseudo.parser.boogie.ast.expression.ConcatenationExpression;
import de.uka.iti.pseudo.parser.boogie.ast.expression.EqualsExpression;
import de.uka.iti.pseudo.parser.boogie.ast.expression.EquivalenceExpression;
import de.uka.iti.pseudo.parser.boogie.ast.expression.ExistsExpression;
import de.uka.iti.pseudo.parser.boogie.ast.expression.ExtendsExpression;
import de.uka.iti.pseudo.parser.boogie.ast.expression.FalseExpression;
import de.uka.iti.pseudo.parser.boogie.ast.expression.ForallExpression;
import de.uka.iti.pseudo.parser.boogie.ast.expression.FunctionCallExpression;
import de.uka.iti.pseudo.parser.boogie.ast.expression.IfThenElseExpression;
import de.uka.iti.pseudo.parser.boogie.ast.expression.ImpliesExpression;
import de.uka.iti.pseudo.parser.boogie.ast.expression.IntegerExpression;
import de.uka.iti.pseudo.parser.boogie.ast.expression.LambdaExpression;
import de.uka.iti.pseudo.parser.boogie.ast.expression.MapAccessExpression;
import de.uka.iti.pseudo.parser.boogie.ast.expression.MapUpdateExpression;
import de.uka.iti.pseudo.parser.boogie.ast.expression.NegationExpression;
import de.uka.iti.pseudo.parser.boogie.ast.expression.OldExpression;
import de.uka.iti.pseudo.parser.boogie.ast.expression.OrExpression;
import de.uka.iti.pseudo.parser.boogie.ast.expression.QuantifierBody;
import de.uka.iti.pseudo.parser.boogie.ast.expression.RelationExpression;
import de.uka.iti.pseudo.parser.boogie.ast.expression.TrueExpression;
import de.uka.iti.pseudo.parser.boogie.ast.expression.UnaryMinusExpression;
import de.uka.iti.pseudo.parser.boogie.ast.expression.VariableUsageExpression;
import de.uka.iti.pseudo.parser.boogie.ast.expression.WildcardExpression;
import de.uka.iti.pseudo.parser.boogie.ast.type.ASTTypeApplication;
import de.uka.iti.pseudo.parser.boogie.ast.type.ASTTypeParameter;
import de.uka.iti.pseudo.parser.boogie.ast.type.BuiltInType;
import de.uka.iti.pseudo.parser.boogie.ast.type.MapType;
import de.uka.iti.pseudo.parser.boogie.ast.type.UserTypeDefinition;
import de.uka.iti.pseudo.parser.boogie.util.DefaultASTVisitor;
import de.uka.iti.pseudo.term.SchemaType;
import de.uka.iti.pseudo.term.TermException;
import de.uka.iti.pseudo.term.Type;
import de.uka.iti.pseudo.term.TypeApplication;
import de.uka.iti.pseudo.term.TypeVariable;
import de.uka.iti.pseudo.term.TypeVisitor;
import de.uka.iti.pseudo.term.UnificationException;
import de.uka.iti.pseudo.term.creation.RebuildingTypeVisitor;
import de.uka.iti.pseudo.term.creation.TypeUnification;
import de.uka.iti.pseudo.term.creation.TypingContext;

/**
 * This visitor decorates ASTElements with types. Typechecking is done using the
 * core type checking mechanism(unification).
 * 
 * @author timm.felden@felden.com
 * 
 */
public final class TypeMapBuilder extends DefaultASTVisitor {

    private static final Type BOOL_T = Environment.getBoolType();

    private final EnvironmentCreationState state;

    private final Map<ASTElement, List<? extends Type>> typeParameterMap = new HashMap<ASTElement, List<? extends Type>>();

    // shortcut to state.schemaTypes
    private final Decoration<SchemaType> schemaTypes;
    // shortcut to state.context
    private final TypingContext context;

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
            if (t.toString().equals('\'' + name))
                return t;

        return null;
    }

    /**
     * Converts a list of ASTTypeParameter to TypeVariables and adds them to the
     * typeParameterMap.
     */
    private List<TypeVariable> addParameters(List<ASTTypeParameter> names, ASTElement node) throws ASTVisitException {
        List<TypeVariable> rval = new LinkedList<TypeVariable>();

        for (ASTTypeParameter t : names) {
            TypeVariable p;
            SchemaType q;

            rval.add(p = TypeVariable.getInst(t.getName()));
            schemaTypes.add(t, q = context.newSchemaType());
            try {
                context.unify(p, q);
            } catch (UnificationException e) {
                e.printStackTrace();
                assert false : "internal error";
            }
        }

        typeParameterMap.put(node, rval);
        return rval;
    }

    /**
     * Adds a constraint for node.
     */
    private void unify(ASTElement node, Type type) throws ASTVisitException {
        try {
            context.unify(type, schemaTypes.get(node));
        } catch (UnificationException e) {
            throw new ASTVisitException("Type inferrence failed @ " + node.getLocation(), e);
        }
    }

    /**
     * Adds a constraint for node.
     */
    private void unify(ASTElement node, ASTElement other) throws ASTVisitException {
        try {
            context.unify(schemaTypes.get(other), schemaTypes.get(node));
        } catch (UnificationException e) {
            throw new ASTVisitException("Type inferrence failed @ " + node.getLocation(), e);
        }
    }

    /**
     * if node has not yet been processed, add shemaType and restricts the type
     * of node to be the same type as the type of typeNode
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
        if (schemaTypes.has(node))
            return;
        schemaTypes.add(node, context.newSchemaType());

        // this is ok, if the dependency is somewhere else in the tree
        if (!schemaTypes.has(typeNode))
            typeNode.visit(this);

        // this is not ok, as it meanst, that the dependency is somehow cyclic
        if (!schemaTypes.has(typeNode))
            throw new ASTVisitException(node.getLocation() + ":  The node " + node
                    + " could not be typed because it depends on the untyped node " + typeNode);

        unify(node, schemaTypes.get(typeNode));
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
        schemaTypes = state.schemaTypes;
        context = state.context;


        // add types from the context
        visit(state.root);
    }

    /**
     * Default action setting a static type, no matter where this node occurs or
     * what its arguments are.
     */
    protected void defaultTyping(ASTElement node, Type type) throws ASTVisitException {
        assert null != type : "the node needs a type";
        
        if (schemaTypes.has(node))
            return;
        schemaTypes.add(node, context.newSchemaType());

        try {
            context.unify(schemaTypes.get(node), type);
        } catch (UnificationException e) {
            throw new ASTVisitException("Type inferrence failed @ " + node.getLocation(), e);
        }

        for (ASTElement n : node.getChildren())
            n.visit(this);
    }

    @Override
    protected void defaultAction(ASTElement node) throws ASTVisitException {
        schemaTypes.add(node, context.newSchemaType());
        state.typeMap.add(node, null);
        for (ASTElement n : node.getChildren())
            n.visit(this);
    }

    @Override
    public void visit(FunctionDeclaration node) throws ASTVisitException {
        if (schemaTypes.has(node))
            return;
        schemaTypes.add(node, context.newSchemaType());

        List<TypeVariable> paramList = this.addParameters(node.getTypeParameters(), node);

        TypeVariable[] parameters = paramList.toArray(new TypeVariable[paramList.size()]);

        for (ASTElement n : node.getChildren())
            if (n != node.getExpression())
                n.visit(this);

        Type[] domain = new Type[node.getInParameters().size()];
        for (int i = 0; i < domain.length; i++)
            domain[i] = context.instantiate(schemaTypes.get(node.getInParameters().get(i)));

        try {
            unify(node, state.mapDB.getType(domain, context.instantiate(schemaTypes.get(node.getOutParemeter())),
                    parameters, node, state));
        } catch (TypeSystemException e) {
            throw new ASTVisitException(node.getLocation() + ":: " + e.getMessage(), e);
        }
        
        // the expression musst have the return type
        if (null != node.getExpression()) {
            node.getExpression().visit(this);
            unify(node.getExpression(), schemaTypes.get(node.getOutParemeter()));
        }
    }

    @Override
    public void visit(BuiltInType node) throws ASTVisitException {
        defaultTyping(node, node.newBasicType(state.env));
    }

    @Override
    public void visit(UserTypeDefinition node) throws ASTVisitException {
        if (schemaTypes.has(node))
            return;
        schemaTypes.add(node, context.newSchemaType());

        if (null == node.getDefinition()) {
            // simple case, a new type (corresponds to sort in ivil) with
            // arguments is defined
            String name;
            try {
                state.env.addSort(new Sort(name = ("utt_" + node.getName()), node.getTypeParameters().size(), node));

                Type[] args = new Type[node.getTypeParameters().size()];

                for (int i = 0; i < args.length; i++) {
                    args[i] = TypeVariable.getInst("arg" + i);
                    // arguments to type constructors are not relevant, if no
                    // definition was supplied
                    defaultAction(node.getTypeParameters().get(i));
                }

                Type result = state.env.mkType(name, args);

                unify(node, result);

            } catch (EnvironmentException e) {
                e.printStackTrace();
            } catch (TermException e) {
                e.printStackTrace();
            }
        } else {
            // more complex case, we have to push type parameters and then
            // put them into the template type field

            List<TypeVariable> param = addParameters(node.getTypeParameters(), node);

            for (ASTElement n : node.getChildren())
                n.visit(this);

            Type[] params = param.toArray(new Type[param.size()]);

            // Type Aliases use fake types, so they can not be unified
            state.typeMap.add(node, new TypeAlias(params, context.instantiate(schemaTypes.get(node.getDefinition())),
                    state));
        }
    }

    @Override
    public void visit(ASTTypeApplication node) throws ASTVisitException {
        if (schemaTypes.has(node))
            return;
        schemaTypes.add(node, context.newSchemaType());

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

            if (!schemaTypes.has(declaration))
                declaration.visit(this);

            // get type
            if (((UserTypeDefinition) declaration).getDefinition() != null) {
                if (!state.typeMap.has(declaration))
                    throw new ASTVisitException(node.getLocation() + " :: The type declared @"
                            + declaration.getLocation() + " appears to be cyclic.");

                type = state.typeMap.get(declaration);
            } else
                type = context.instantiate(schemaTypes.get(declaration));

            if (type instanceof SchemaType)
                throw new ASTVisitException(node.getLocation() + " :: The type declared @" + declaration.getLocation()
                        + " is ill-formed.");

            // get type arguments
            Type[] arg_t = new Type[node.getArguments().size()];

            try {
                if (type instanceof TypeAlias) {
                    for (int i = 0; i < node.getArguments().size(); i++) {
                        final ASTElement child = node.getArguments().get(i);
                        child.visit(this);
                        arg_t[i] = context.instantiate(schemaTypes.get(child));
                    }

                    type = ((TypeAlias) type).constructFrom(arg_t);
                } else {
                    for (int i = 0; i < node.getArguments().size(); i++) {
                        final ASTElement child = node.getArguments().get(i);
                        child.visit(this);
                        arg_t[i] = schemaTypes.get(child);
                    }

                    type = state.env.mkType(((TypeApplication) type).getSort().getName(), arg_t);
                }

            } catch (EnvironmentException e) {
                e.printStackTrace();
            } catch (TermException e) {
                e.printStackTrace();
            }
        }

        if (null == type)
            throw new ASTVisitException(node.getLocation() + ":  undeclared type " + node.getName());

        try {
            context.unify(schemaTypes.get(node), type);
        } catch (UnificationException e) {
            throw new ASTVisitException("Type inferrence failed @ " + node.getLocation(), e);
        }
    }

    @Override
    public void visit(ASTTypeParameter node) throws ASTVisitException {
        defaultTyping(node, TypeVariable.getInst(node.getName()));
    }

    @Override
    public void visit(MapType node) throws ASTVisitException {
        if (schemaTypes.has(node))
            return;
        schemaTypes.add(node, context.newSchemaType());

        List<TypeVariable> paramList = this.addParameters(node.getTypeParameters(), node);

        TypeVariable[] parameters = paramList.toArray(new TypeVariable[paramList.size()]);

        for (ASTElement n : node.getChildren())
            n.visit(this);

        Type[] domain = new Type[node.getDomain().size()];
        for (int i = 0; i < domain.length; i++)
            domain[i] = context.instantiate(schemaTypes.get(node.getDomain().get(i)));

        try {
            unify(node, state.mapDB.getType(domain, context.instantiate(schemaTypes.get(node.getRange())), parameters,
                    node, state));

        } catch (TypeSystemException e) {
            throw new ASTVisitException(node.getLocation() + ":: " + e.getMessage(), e);
        }
    }

    @Override
    public void visit(ProcedureDeclaration node) throws ASTVisitException {
        if (schemaTypes.has(node))
            return;
        schemaTypes.add(node, context.newSchemaType());

        // type of procedures is [IN][OUT]bool
        // therefore procedure declarations behave a lot like map type declarations

        // procedures can have polymorphic types, so push typeargs
        List<TypeVariable> paramList = this.addParameters(node.getTypeParameters(), node);

        TypeVariable[] parameters = paramList.toArray(new TypeVariable[paramList.size()]);

        for (ASTElement n : node.getInParameters())
                n.visit(this);

        for (ASTElement n : node.getOutParameters())
            n.visit(this);


        Type[] in = new Type[node.getInParameters().size()];
        for (int i = 0; i < in.length; i++)
            in[i] = context.instantiate(schemaTypes.get(node.getInParameters().get(i)));

        Type[] out = new Type[node.getOutParameters().size()];
        for (int i = 0; i < out.length; i++)
            out[i] = context.instantiate(schemaTypes.get(node.getOutParameters().get(i)));

        try {
            unify(node, state.mapDB.getType(in,
 state.mapDB.getType(out, BOOL_T, new TypeVariable[0], node, state),
                    parameters,
                    node, state));

        } catch (TypeSystemException e) {
            throw new ASTVisitException(node.getLocation() + ":: " + e.getMessage(), e);
        }

        for (ASTElement n : node.getSpecification())
            n.visit(this);

        if (node.isImplemented())
            node.getBody().visit(this);

        for (ASTElement n : node.getAttributes())
            n.visit(this);
    }

    @Override
    public void visit(ProcedureImplementation node) throws ASTVisitException {
        if (schemaTypes.has(node))
            return;
        schemaTypes.add(node, context.newSchemaType());

        // type of procedures is [IN][OUT]bool
        // therefore procedure declarations behave a lot like map type
        // declarations

        // procedures can have polymorphic types, so push typeargs
        List<TypeVariable> paramList = this.addParameters(node.getTypeParameters(), node);

        TypeVariable[] parameters = paramList.toArray(new TypeVariable[paramList.size()]);

        for (ASTElement n : node.getChildren())
            n.visit(this);

        Type[] in = new Type[node.getInParameters().size()];
        for (int i = 0; i < in.length; i++)
            in[i] = context.instantiate(schemaTypes.get(node.getInParameters().get(i)));

        Type[] out = new Type[node.getOutParameters().size()];
        for (int i = 0; i < out.length; i++)
            out[i] = context.instantiate(schemaTypes.get(node.getOutParameters().get(i)));

        try {
            unify(node, state.mapDB.getType(in,
 state.mapDB.getType(out, BOOL_T, new TypeVariable[0], node, state),
                    parameters,
                    node, state));

        } catch (TypeSystemException e) {
            throw new ASTVisitException(node.getLocation() + ":: " + e.getMessage(), e);
        }

        // implementations are required to have the same type as the declaration
        // with the same name
        try {
            ProcedureDeclaration decl = state.names.procedureSpace.get(node.getName());
            if (null == decl)
                throw new ASTVisitException("The procedure " + node.getName() + " was implemented but never declared.");

            // ensure that decl has been visited
            decl.visit(this);

            unify(node, context.instantiate(schemaTypes.get(decl)));
        } catch (ASTVisitException e) {
            // create a more usable error message
            throw new ASTVisitException("The implementation defined @" + node.getLocation()
                    + " does not match the type of its declaration:\n" + e.getCause().getMessage());
        }
    }

    @Override
    public void visit(VariableDeclaration node) throws ASTVisitException {
        node.getType().visit(this);
        setTypeSameAs(node, node.getType());

        node.getWhereClause().visit(this);
        unify(node.getWhereClause(), BOOL_T);
    }

    @Override
    public void visit(SimpleAssignment node) throws ASTVisitException {
        // the node itself is untyped
        defaultAction(node);

        // operands need to have the same type
        try {
            context.unify(context.instantiate(schemaTypes.get(node.getTarget())),
                    context.instantiate(schemaTypes.get(node.getNewValue())));
        } catch (UnificationException e) {
            throw new ASTVisitException("assignment illtyped @" + node.getLocation(), e);
        }
        setTypeSameAs(node, node.getTarget());
    }

    @Override
    public void visit(ExtendsExpression node) throws ASTVisitException {
        defaultTyping(node, BOOL_T);
    }

    /**
     * @note: type checking for concatenation expressions is weak, i.e. no
     *        dimension checking is done
     */
    @Override
    public void visit(ConcatenationExpression node) throws ASTVisitException {
        defaultTyping(node, getBitvectorType());
    }

    @Override
    public void visit(BitvectorSelectExpression node) throws ASTVisitException {
        defaultTyping(node, getBitvectorType());
    }

    @Override
    public void visit(BitvectorAccessSelectionExpression node) throws ASTVisitException {
        defaultAction(node);
        setTypeSameAs(node, node.getTarget());
    }

    @Override
    public void visit(MapAccessExpression node) throws ASTVisitException {
        if (schemaTypes.has(node))
            return;
        schemaTypes.add(node, context.newSchemaType());

        for (ASTElement n : node.getChildren())
            n.visit(this);

        // @note: this is a type application, if the object is a map
        Type t = context.instantiate(schemaTypes.get(node.getName()));
        if(!(t instanceof TypeApplication))
            throw new ASTVisitException(node.getLocation() + " the used map object has no map type!");
        
        Type[] signature;
        try {
            signature = makeMapSignature((TypeApplication) t);
        } catch (TermException e) {
            e.printStackTrace();
            throw new ASTVisitException(node.getLocation() + ": failed to create map signature", e);
        }
        
        // signature contains result, map
        if (node.getOperands().size() != signature.length - 1)
            throw new ASTVisitException(node.getLocation()
                    + ": mismatching number of operands to load expression. expected: " + (signature.length - 1)
                    + " got: " + node.getOperands().size());

        for (int i = 0; i < node.getOperands().size(); i++)
            unify(node.getOperands().get(i), signature[i + 1]);
        unify(node, signature[0]);
    }

    /**
     * Create a map signature as [range, domain0, ..., domainN]
     */
    private Type[] makeMapSignature(TypeApplication t) throws TermException {
        Function $load = state.env.getFunction("$load_" + ((TypeApplication) t).getSort().getName());
        final Type[] rval = new Type[$load.getArity()];
        
        Map<TypeVariable, Type> mapping = new HashMap<TypeVariable, Type>();
        for(int i = 0; i < t.getArguments().size(); i++)
            mapping.put(TypeVariable.getInst("_" + i), t.getArguments().get(i));
        
        RebuildingTypeVisitor<Map<TypeVariable, Type>> visitor = new RebuildingTypeVisitor<Map<TypeVariable, Type>>() {
            @Override
            public Type visit(TypeVariable typeVariable, Map<TypeVariable, Type> parameter) throws TermException {
                if (!parameter.containsKey(typeVariable)) {
                    // we encountered a locally bound type variable, which has
                    // to be replaced by a new schema type
                    parameter.put(typeVariable, context.newSchemaType());
                }
                return parameter.get(typeVariable);
            }
        };
        
        rval[0] = $load.getResultType().accept(visitor, mapping);
        for (int i = 1; i < rval.length; i++)
            rval[i] = $load.getArgumentTypes()[i].accept(visitor, mapping);

        return rval;
    }

    @Override
    public void visit(MapUpdateExpression node) throws ASTVisitException {
        // visit children
        for (ASTElement n : node.getChildren())
            n.visit(this);

        // set type for this node
        setTypeSameAs(node, node.getName());


        // add constraints for update arguments
                
        // @note: this is a type application, if the object is a map
        Type t = context.instantiate(schemaTypes.get(node.getName()));
        if (!(t instanceof TypeApplication))
            throw new ASTVisitException(node.getLocation() + " the used map object has no map type!");

        Type[] signature;
        try {
            signature = makeMapSignature((TypeApplication) t);
        } catch (TermException e) {
            e.printStackTrace();
            throw new ASTVisitException(node.getLocation() + ": failed to create map signature", e);
        }

        // signature contains result, map
        if (node.getOperands().size() != signature.length - 1)
            throw new ASTVisitException(node.getLocation()
                    + ": mismatching number of operands to load expression. expected: " + (signature.length - 1)
                    + " got: " + node.getOperands().size());

        for (int i = 0; i < node.getOperands().size(); i++)
            unify(node.getOperands().get(i), signature[i + 1]);
        unify(node.getUpdate(), signature[0]);
    }

    @Override
    public void visit(BitvectorLiteralExpression node) throws ASTVisitException {
        defaultTyping(node, getBitvectorType());
    }

    @Override
    public void visit(FunctionCallExpression node) throws ASTVisitException {
        // abuse map type of function declarations to infer types
        
        if (schemaTypes.has(node))
            return;
        schemaTypes.add(node, context.newSchemaType());

        for (ASTElement n : node.getChildren())
            n.visit(this);

        ASTElement decl = state.names.functionSpace.get(node.getName());
        if (null == decl)
            throw new ASTVisitException("Function " + node.getName() + " is used but never declared anywhere.");

        if (!schemaTypes.has(decl))
            decl.visit(this);

        // @note: this is a type application, if the object is a map
        Type t = context.instantiate(schemaTypes.get(decl));
        if (!(t instanceof TypeApplication))
            throw new ASTVisitException(node.getLocation() + " the used map object has no map type!");

        Type[] signature;
        try {
            signature = makeMapSignature((TypeApplication) t);
        } catch (TermException e) {
            e.printStackTrace();
            throw new ASTVisitException(node.getLocation() + ": failed to create map signature", e);
        }

        // signature contains result, map
        if (node.getOperands().size() != signature.length - 1)
            throw new ASTVisitException(node.getLocation()
                    + ": mismatching number of operands to function call expression. expected: "
                    + (signature.length - 1)
                    + " got: " + node.getOperands().size());

        for (int i = 0; i < node.getOperands().size(); i++)
            unify(node.getOperands().get(i), signature[i + 1]);
        unify(node, signature[0]);
    }

    @Override
    public void visit(VariableUsageExpression node) throws ASTVisitException {
        for (ASTElement n : node.getChildren())
            n.visit(this);

        if (node.getName().equals("*")) {
            // anonymous variable that will not be used anywhere, the type will
            // be as defined by the parent, which is a procedure implementation
            // or declaration

            // we do not do anything here, because we can not restrict the type
            // in any way

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
         // the quantifier body has a map type, that maps the quantified
         // variables to the result of the expression
        if (schemaTypes.has(node))
            return;
        schemaTypes.add(node, context.newSchemaType());

        List<TypeVariable> paramList = this.addParameters(node.getTypeParameters(), node);

        TypeVariable[] parameters = paramList.toArray(new TypeVariable[paramList.size()]);

        for (ASTElement n : node.getChildren())
            n.visit(this);

        Type[] domain = new Type[node.getQuantifiedVariables().size()];
        for (int i = 0; i < domain.length; i++)
            domain[i] = context.instantiate(schemaTypes.get(node.getQuantifiedVariables().get(i)));

        try {
            unify(node, state.mapDB.getType(domain, context.instantiate(schemaTypes.get(node.getBody())), parameters,
                    node, state));

        } catch (TypeSystemException e) {
            throw new ASTVisitException(node.getLocation() + ":: " + e.getMessage(), e);
        }
    }

    @Override
    public void visit(ForallExpression node) throws ASTVisitException {
        defaultTyping(node, BOOL_T);
        // we know as well, that the quantifier body has a bool range type
        unify(node.getBody().getBody(), BOOL_T);
    }

    @Override
    public void visit(ExistsExpression node) throws ASTVisitException {
        defaultTyping(node, BOOL_T);
        // we know as well, that the quantifier body has a bool range type
        unify(node.getBody().getBody(), BOOL_T);
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
        if (schemaTypes.has(node))
            return;
        schemaTypes.add(node, context.newSchemaType());

        // a coercion says that the operand has the same type as the type node,
        // which both will have the same type as this node, as this node will be
        // used as argument to other nodes
        for (ASTElement n : node.getChildren()) {
            n.visit(this);
            unify(node, schemaTypes.get(n));
        }
    }

    @Override
    public void visit(LoopInvariant node) throws ASTVisitException {
        defaultTyping(node, BOOL_T);
    }

    @Override
    public void visit(WildcardExpression node) throws ASTVisitException {
        // note: this method will only be called on boolean wildcards, if
        // wildcard occurs in call statements, the parent already typed this
        defaultTyping(node, BOOL_T);
    }

    @Override
    public void visit(BinaryIntegerExpression node) throws ASTVisitException {
        defaultTyping(node, Environment.getIntType());
        unify(node.getOperands().get(0), Environment.getIntType());
        unify(node.getOperands().get(1), Environment.getIntType());
    }

    @Override
    public void visit(EquivalenceExpression node) throws ASTVisitException {
        defaultTyping(node, BOOL_T);
        unify(node.getOperands().get(0), schemaTypes.get(node.getOperands().get(1)));
    }

    @Override
    public void visit(ImpliesExpression node) throws ASTVisitException {
        defaultTyping(node, BOOL_T);
    }

    @Override
    public void visit(AndExpression node) throws ASTVisitException {
        defaultTyping(node, BOOL_T);
    }

    @Override
    public void visit(OrExpression node) throws ASTVisitException {
        defaultTyping(node, BOOL_T);
    }

    @Override
    public void visit(EqualsExpression node) throws ASTVisitException {
        defaultTyping(node, BOOL_T);

        final Type t0 = context.instantiate(schemaTypes.get(node.getOperands().get(0))), t1 = context
                .instantiate(schemaTypes.get(node.getOperands().get(1)));

        // operands need to have the same type
        try {
            context.unify(t0, t1);
        } catch (UnificationException e) {
            // the equality might contain quantified type variables. if so, we
            // need only to ensure, that both operands can be unified in general
            try {
                new TypeUnification().unify(TypeUnification.makeSchemaVariant(t0),
                        TypeUnification.makeSchemaVariant(t1));
            } catch (UnificationException e1) {
                throw new ASTVisitException("equality illtyped @" + node.getLocation() + ": " + t0 + " vs. " + t1, e1);
            }
        }
    }

    @Override
    public void visit(RelationExpression node) throws ASTVisitException {
        defaultTyping(node, BOOL_T);
    }

    @Override
    public void visit(UnaryMinusExpression node) throws ASTVisitException {
        defaultTyping(node, Environment.getIntType());
    }

    @Override
    public void visit(NegationExpression node) throws ASTVisitException {
        defaultTyping(node, BOOL_T);
    }

    @Override
    public void visit(IntegerExpression node) throws ASTVisitException {
        defaultTyping(node, Environment.getIntType());
    }

    @Override
    public void visit(TrueExpression node) throws ASTVisitException {
        defaultTyping(node, BOOL_T);
    }

    @Override
    public void visit(FalseExpression node) throws ASTVisitException {
        defaultTyping(node, BOOL_T);
    }

    @Override
    public void visit(CodeExpression node) throws ASTVisitException {
        if (schemaTypes.has(node))
            return;
        schemaTypes.add(node, context.newSchemaType());
        
         for (ASTElement n : node.getChildren())
         n.visit(this);
                 
        boolean returns = false;
        for (CodeBlock b : node.getCode()) {
            if (null != b.getReturnStatement()) {
                returns = true;
                unify(node, b.getReturnStatement());
            }
        }
        if (!returns)
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
        
        if (schemaTypes.has(node))
            return;
        schemaTypes.add(node, context.newSchemaType());
        state.typeMap.add(node, null);

        for (ASTElement n : node.getChildren())
            if (n instanceof WildcardExpression)
                schemaTypes.add(n, context.newSchemaType());
            else
                n.visit(this);

        // @note: this is a type application, if the object is a map
        ASTElement decl = state.names.procedureSpace.get(node.getName());
        decl.visit(this);
        Type t = context.instantiate(schemaTypes.get(decl));
        if(!(t instanceof TypeApplication))
            throw new ASTVisitException(node.getLocation() + " the used map object has no map type!");
        
        final Type[] signature;
        try {
            signature = makeMapSignature((TypeApplication) t);
        } catch (TermException e) {
            e.printStackTrace();
            throw new ASTVisitException(node.getLocation() + ": failed to create map signature", e);
        }
        
        // signature contains result, map
        if (node.getArguments().size() != signature.length - 1)
            throw new ASTVisitException(node.getLocation()
                    + ": mismatching number of operands to load expression. expected: " + (signature.length - 1)
                    + " got: " + node.getArguments().size());

        for (int i = 0; i < node.getArguments().size(); i++)
            unify(node.getArguments().get(i), signature[i + 1]);

        final Type[] sigOut;
        try {
            sigOut = makeMapSignature((TypeApplication) signature[0]);
        } catch (TermException e) {
            e.printStackTrace();
            throw new ASTVisitException(node.getLocation() + ": failed to create map signature", e);
        }

        // signature contains result, map
        if (node.getOutParam().size() != sigOut.length - 1)
            throw new ASTVisitException(node.getLocation()
                    + ": mismatching number of operands to load expression. expected: " + (sigOut.length - 1)
                    + " got: " + node.getOutParam().size());

        for (int i = 0; i < node.getOutParam().size(); i++)
            unify(node.getOutParam().get(i), sigOut[i + 1]);
    }

    @Override
    public void visit(CallForallStatement node) throws ASTVisitException {

        // TODO push parameters of the called function in order to allow for
        // code such as:
        // procedure P<a>(y:a)returns(x:a){ call forall P(*); }

        if (schemaTypes.has(node))
            return;
        schemaTypes.add(node, context.newSchemaType());
        state.typeMap.add(node, null);

        for (ASTElement n : node.getChildren())
            if(n instanceof WildcardExpression)
                schemaTypes.add(n, context.newSchemaType());
            else
                n.visit(this);

        // @note: this is a type application, if the object is a map
        ASTElement decl = state.names.procedureSpace.get(node.getName());
        decl.visit(this);
        Type t = context.instantiate(schemaTypes.get(decl));
        if (!(t instanceof TypeApplication))
            throw new ASTVisitException(node.getLocation() + " the used map object has no map type!");

        final Type[] signature;
        try {
            signature = makeMapSignature((TypeApplication) t);
        } catch (TermException e) {
            e.printStackTrace();
            throw new ASTVisitException(node.getLocation() + ": failed to create map signature", e);
        }

        // signature contains result, map
        if (node.getArguments().size() != signature.length - 1)
            throw new ASTVisitException(node.getLocation()
                    + ": mismatching number of operands to load expression. expected: " + (signature.length - 1)
                    + " got: " + node.getArguments().size());

        for (int i = 0; i < node.getArguments().size(); i++)
            unify(node.getArguments().get(i), signature[i + 1]);
    }

    @Override
    public void visit(AxiomDeclaration node) throws ASTVisitException {
        defaultAction(node);
        unify(node.getAxiom(), BOOL_T);
    }

    @Override
    public void visit(Trigger node) throws ASTVisitException {
        defaultAction(node);
        // TODO add typing for triggers
        // note: the following code is valid:
        // axiom (forall<a> x:Box :: {unbox(x):a} box(unbox(x):a) == x);
        // therefore it would be necessary to attach the trigger to one of the
        // generated quantifiers; currently there is no working trigger support
    }
}