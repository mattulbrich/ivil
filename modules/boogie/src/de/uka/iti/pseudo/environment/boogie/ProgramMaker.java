package de.uka.iti.pseudo.environment.boogie;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import de.uka.iti.pseudo.environment.Axiom;
import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.environment.EnvironmentException;
import de.uka.iti.pseudo.environment.Function;
import de.uka.iti.pseudo.environment.Program;
import de.uka.iti.pseudo.parser.boogie.ASTElement;
import de.uka.iti.pseudo.parser.boogie.ASTVisitException;
import de.uka.iti.pseudo.parser.boogie.ast.AdditionExpression;
import de.uka.iti.pseudo.parser.boogie.ast.AndExpression;
import de.uka.iti.pseudo.parser.boogie.ast.AssertionStatement;
import de.uka.iti.pseudo.parser.boogie.ast.AssignmentStatement;
import de.uka.iti.pseudo.parser.boogie.ast.AssumptionStatement;
import de.uka.iti.pseudo.parser.boogie.ast.Attribute;
import de.uka.iti.pseudo.parser.boogie.ast.AttributeParameter;
import de.uka.iti.pseudo.parser.boogie.ast.AxiomDeclaration;
import de.uka.iti.pseudo.parser.boogie.ast.BitvectorAccessSelectionExpression;
import de.uka.iti.pseudo.parser.boogie.ast.BitvectorLiteralExpression;
import de.uka.iti.pseudo.parser.boogie.ast.BreakStatement;
import de.uka.iti.pseudo.parser.boogie.ast.CallForallStatement;
import de.uka.iti.pseudo.parser.boogie.ast.CallStatement;
import de.uka.iti.pseudo.parser.boogie.ast.CodeBlock;
import de.uka.iti.pseudo.parser.boogie.ast.CodeExpression;
import de.uka.iti.pseudo.parser.boogie.ast.CodeExpressionReturn;
import de.uka.iti.pseudo.parser.boogie.ast.CoercionExpression;
import de.uka.iti.pseudo.parser.boogie.ast.ConcatenationExpression;
import de.uka.iti.pseudo.parser.boogie.ast.ConstantDeclaration;
import de.uka.iti.pseudo.parser.boogie.ast.DivisionExpression;
import de.uka.iti.pseudo.parser.boogie.ast.EqualsExpression;
import de.uka.iti.pseudo.parser.boogie.ast.EqualsNotExpression;
import de.uka.iti.pseudo.parser.boogie.ast.EquivalenceExpression;
import de.uka.iti.pseudo.parser.boogie.ast.ExistsExpression;
import de.uka.iti.pseudo.parser.boogie.ast.Expression;
import de.uka.iti.pseudo.parser.boogie.ast.ExtendsExpression;
import de.uka.iti.pseudo.parser.boogie.ast.ExtendsParent;
import de.uka.iti.pseudo.parser.boogie.ast.FalseExpression;
import de.uka.iti.pseudo.parser.boogie.ast.ForallExpression;
import de.uka.iti.pseudo.parser.boogie.ast.FunctionCallExpression;
import de.uka.iti.pseudo.parser.boogie.ast.FunctionDeclaration;
import de.uka.iti.pseudo.parser.boogie.ast.GlobalVariableDeclaration;
import de.uka.iti.pseudo.parser.boogie.ast.GotoStatement;
import de.uka.iti.pseudo.parser.boogie.ast.GreaterEqualExpression;
import de.uka.iti.pseudo.parser.boogie.ast.GreaterExpression;
import de.uka.iti.pseudo.parser.boogie.ast.HavocStatement;
import de.uka.iti.pseudo.parser.boogie.ast.IfStatement;
import de.uka.iti.pseudo.parser.boogie.ast.IfThenElseExpression;
import de.uka.iti.pseudo.parser.boogie.ast.ImpliesExpression;
import de.uka.iti.pseudo.parser.boogie.ast.IntegerExpression;
import de.uka.iti.pseudo.parser.boogie.ast.LabelStatement;
import de.uka.iti.pseudo.parser.boogie.ast.LambdaExpression;
import de.uka.iti.pseudo.parser.boogie.ast.LessEqualExpression;
import de.uka.iti.pseudo.parser.boogie.ast.LessExpression;
import de.uka.iti.pseudo.parser.boogie.ast.LocalVariableDeclaration;
import de.uka.iti.pseudo.parser.boogie.ast.LoopInvariant;
import de.uka.iti.pseudo.parser.boogie.ast.MapAccessExpression;
import de.uka.iti.pseudo.parser.boogie.ast.MapUpdateExpression;
import de.uka.iti.pseudo.parser.boogie.ast.ModifiesClause;
import de.uka.iti.pseudo.parser.boogie.ast.ModuloExpression;
import de.uka.iti.pseudo.parser.boogie.ast.MultiplicationExpression;
import de.uka.iti.pseudo.parser.boogie.ast.NegationExpression;
import de.uka.iti.pseudo.parser.boogie.ast.OldExpression;
import de.uka.iti.pseudo.parser.boogie.ast.OrExpression;
import de.uka.iti.pseudo.parser.boogie.ast.Postcondition;
import de.uka.iti.pseudo.parser.boogie.ast.Precondition;
import de.uka.iti.pseudo.parser.boogie.ast.ProcedureDeclaration;
import de.uka.iti.pseudo.parser.boogie.ast.ProcedureImplementation;
import de.uka.iti.pseudo.parser.boogie.ast.QuantifierBody;
import de.uka.iti.pseudo.parser.boogie.ast.ReturnStatement;
import de.uka.iti.pseudo.parser.boogie.ast.SimpleAssignment;
import de.uka.iti.pseudo.parser.boogie.ast.Specification;
import de.uka.iti.pseudo.parser.boogie.ast.SubtractionExpression;
import de.uka.iti.pseudo.parser.boogie.ast.Trigger;
import de.uka.iti.pseudo.parser.boogie.ast.TrueExpression;
import de.uka.iti.pseudo.parser.boogie.ast.UnaryMinusExpression;
import de.uka.iti.pseudo.parser.boogie.ast.VariableDeclaration;
import de.uka.iti.pseudo.parser.boogie.ast.VariableUsageExpression;
import de.uka.iti.pseudo.parser.boogie.ast.WhileStatement;
import de.uka.iti.pseudo.parser.boogie.ast.WildcardExpression;
import de.uka.iti.pseudo.parser.boogie.util.DefaultASTVisitor;
import de.uka.iti.pseudo.term.Application;
import de.uka.iti.pseudo.term.Binding;
import de.uka.iti.pseudo.term.LiteralProgramTerm;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.TermException;
import de.uka.iti.pseudo.term.Type;
import de.uka.iti.pseudo.term.TypeVariableBinding;
import de.uka.iti.pseudo.term.Variable;
import de.uka.iti.pseudo.term.statement.AssertStatement;
import de.uka.iti.pseudo.term.statement.Assignment;
import de.uka.iti.pseudo.term.statement.AssumeStatement;
import de.uka.iti.pseudo.term.statement.EndStatement;
import de.uka.iti.pseudo.term.statement.SkipStatement;
import de.uka.iti.pseudo.term.statement.Statement;

/**
 * This is the heart of the loader. This Visitor transforms the gathered
 * informations into IVIL equivalents. As expressions can contain code and code
 * can contain expressions, there is no useful way to make this visitor smaller.
 * 
 * The treatment of most nodes is straight forward, the other nodes have
 * individual comments, which hopefully explain how the translation is done.
 * 
 * @author timm.felden@felden.com
 */
public final class ProgramMaker extends DefaultASTVisitor {

    static private final Type[] NO_TYPE = new Type[0];
    static private final Term[] NO_ARGS = new Term[0];

    /**
     * This triple consist of (preconditions, body, postconditions). It is
     * needed to append specifications of procedures to their implementation.
     * 
     * @author timm.felden@felden.com
     */
    static public final class StatementTripel {
        public List<de.uka.iti.pseudo.term.statement.Statement> bodyStatements = new LinkedList<Statement>();
        public List<String> bodyAnnotations = new LinkedList<String>();

        public List<de.uka.iti.pseudo.term.statement.Statement> preStatements = new LinkedList<Statement>();
        public List<String> preAnnotations = new LinkedList<String>();

        public List<de.uka.iti.pseudo.term.statement.Statement> postStatements = new LinkedList<Statement>();
        public List<String> postAnnotations = new LinkedList<String>();
    }

    private final EnvironmentCreationState state;

    private StatementTripel statements = null;

    // wheres for global variables
    final List<de.uka.iti.pseudo.term.statement.Statement> whereStatements = new LinkedList<Statement>();
    // wheres for local variables
    final List<String> whereAnnotations = new LinkedList<String>();

    // variables can get a desired name instead of the default ones
    private String desiredName = null;

    private Map<String, Variable> boundVars = new HashMap<String, Variable>();

    /**
     * This String is nonnull iff breaks are meaningfull. In that case, they
     * will contain the annotation of the label, which has to be jumped to on
     * breaks.
     */
    private String breakLabel;

    /**
     * If this is true, we are inside an old statement, thus we need to try to
     * access old_ variables.
     */
    private boolean oldMode = false;

    /**
     * List of currently modifiable variables. This is needed to access
     * variables in oldMode correctly.
     */
    private List<VariableDeclaration> modifiable = null;

    /**
     * Needed to store result variables of code expressions.
     */
    private Variable codeexpressionResult = null;

    public ProgramMaker(EnvironmentCreationState state) throws EnvironmentCreationException {
        this.state = state;
        extd = state.env.getFunction("$extends_direct");
        extu = state.env.getFunction("$extends_unique");

        try {
            state.root.visit(this);

        } catch (ASTVisitException e) {
            e.printStackTrace();
            throw new EnvironmentCreationException(e.getMessage());
        }
    }

    @Override
    protected void defaultAction(ASTElement node) throws ASTVisitException {
        for (ASTElement e : node.getChildren())
            e.visit(this);
    }

    @Override
    public void visit(AxiomDeclaration node) throws ASTVisitException {
        defaultAction(node);

        // add axiom
        try {
            state.env.addAxiom(new Axiom("axiom_" + state.env.getAllAxioms().size(), state.translation.terms.get(node
                    .getAxiom()), new HashMap<String, String>(), node));
        } catch (EnvironmentException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void visit(VariableDeclaration node) throws ASTVisitException {

        // names are somesort of magic, if no other name is desired, the name
        // will be var_<line>_<colon>__name

        String name = null != desiredName ? desiredName : "var" + node.getLocation().replace(':', '_') + "__"
                + node.getName();
        desiredName = null;

        state.translation.variableNames.put(node, name);

        // quantified variables differ from ordinary variables, as they aren't
        // functions
        if (node.isQuantified() || node.getParent() instanceof FunctionDeclaration) {
            boundVars.put(name, new de.uka.iti.pseudo.term.Variable(name, state.ivilTypeMap.get(node)));

        } else {
            try {
                if (null == state.env.getFunction(name)) {
                    Function var = new Function(name, state.ivilTypeMap.get(node), NO_TYPE, node.isUnique(),
                            !node.isConstant(), node);
                    state.env.addFunction(var);
                }

                // if where is not true, we have to add an assumption, that
                // specifies the where clause
                try {
                    node.getWhereClause().visit(this);
                } catch (NullPointerException e) {
                    // this will be caused by statements like x,y where x == y
                    // when processing the first variable. one should find a
                    // nicer solution to this problem later
                    return;
                }

                Term where = state.translation.terms.get(node.getWhereClause());
                if (where.equals(Environment.getTrue()))
                    return; // Don't add assume true

                if (node.getParent() instanceof GlobalVariableDeclaration) {
                    whereStatements.add(new AssumeStatement(node.getLocationToken().beginLine, where));
                    whereAnnotations.add("where global");
                } else {
                    statements.preStatements.add(new AssumeStatement(node.getLocationToken().beginLine, where));
                    statements.preAnnotations.add("where");
                }

            } catch (EnvironmentException e) {
                e.printStackTrace();
                throw new ASTVisitException(node.getLocation(), e);

            } catch (TermException e) {
                e.printStackTrace();
                throw new ASTVisitException(node.getLocation(), e);
            }
        }
    }

    // shortcut to create $eq applications
    private Term extend(Term a, VariableDeclaration b) throws TermException {
        return new Application(state.env.getFunction("$extends"), bool_t, new Term[] { a,
                new Application(state.env.getFunction(state.translation.variableNames.get(b)), a.getType()) });
    }

    final private Function extd, extu;

    final private Type bool_t = Environment.getBoolType();

    @Override
    public void visit(ConstantDeclaration node) throws ASTVisitException {
        defaultAction(node);

        try {
            if (node.hasExtends()) {
                Type t = state.ivilTypeMap.get(node.getNames().get(0));

                for (VariableDeclaration v : node.getNames()) {
                    // add edges

                    for (ExtendsParent p : node.getParents()) {
                        Term ext = new Application(p.unique ? extu : extd, bool_t, new Term[] {
                                new Application(state.env.getFunction(state.translation.variableNames.get(v)), t),
                                new Application(state.env.getFunction(state.translation.variableNames.get(state.names
                                        .findVariable(p.getName(), node))), t) });

                        state.env.addAxiom(new Axiom(v.getName() + "<:" + p.getName(), ext,
                                new HashMap<String, String>(), node));
                    }

                    // add axiom that specify that all known parents were
                    // specified
                    // ∀ x : t :: v <: x ==> x == v || (|| ∀P <: x)

                    Variable x = new Variable("x", t);

                    Term axiom = new Application(state.env.getFunction("$eq"), bool_t, new Term[] { x,
                            new Application(state.env.getFunction(state.translation.variableNames.get(v)), t) });

                    // || ∀P <: x
                    for (ExtendsParent p : node.getParents()) {
                        axiom = new Application(state.env.getFunction("$or"), bool_t, new Term[] { axiom,
                                extend(x, state.names.findVariable(p.getName(), node)) });
                    }

                    axiom = new Application(state.env.getFunction("$impl"), bool_t, new Term[] { extend(x, v), axiom });

                    axiom = new Binding(state.env.getBinder("\\forall"), bool_t, x, new Term[] { axiom });

                    state.env.addAxiom(new Axiom("parents of " + v.getName(), axiom, new HashMap<String, String>(),
                            node));
                }
            }

            if (node.isComplete()) {
                // collect usage U of v
                // ∀ x : t :: x <: v ==> x == v || (|| ∀u:U . x <: u)

                // TODO implement complete as described above
            }

        } catch (EnvironmentException e) {
            e.printStackTrace();
            throw new ASTVisitException(node.getLocation(), e);

        } catch (TermException e) {
            e.printStackTrace();
            throw new ASTVisitException(node.getLocation(), e);
        }
    }

    public void visit(ExtendsExpression node) throws ASTVisitException {
        defaultAction(node);

        Term[] args = new Term[2];

        for (int i = 0; i < 2; i++)
            args[i] = state.translation.terms.get(node.getOperands().get(i));

        try {
            state.translation.terms.put(node,
                    new Application(state.env.getFunction("$extends"), Environment.getBoolType(), args));
        } catch (TermException e) {
            e.printStackTrace();
            throw new ASTVisitException(e);
        }
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
                arguments[i] = state.ivilTypeMap.get(node.getInParameters().get(i));

            try {
                state.env.addFunction(new Function("fun__" + node.getName(), state.ivilTypeMap.get(node
                        .getOutParemeter()), arguments, false, false, node));

            } catch (EnvironmentException e) {
                e.printStackTrace();
                throw new ASTVisitException("Function declaration failed because of:\n", e);
            }
        }

        // add definition
        if (null != node.getExpression()) {

            node.getExpression().visit(this);

            Term[] args = new Term[1];

            try {
                // add body expression to args
                args[0] = state.translation.terms.get(node.getExpression());

                Term[] inArgs = new Term[node.getInParameters().size()];
                for (int i = 0; i < inArgs.length; i++)
                    inArgs[i] = boundVars.get(state.translation.variableNames.get(node.getInParameters().get(i)));

                // add "f(x) == expr"
                args = new Term[] {
                        new Application(state.env.getFunction("fun__" + node.getName()), state.ivilTypeMap.get(node
                                .getOutParemeter()), inArgs), args[0] };

                args = new Term[] { new Application(state.env.getFunction("$eq"), Environment.getBoolType(), args) };

                // add quantifiers before body
                for (VariableDeclaration v : node.getInParameters()) {
                    args = new Term[] { new Binding(state.env.getBinder("\\forall"), Environment.getBoolType(),
                            boundVars.get(state.translation.variableNames.get(v)), args) };
                }

                // add type quantifiers before ordinary quantifiers
                {
                    UniversalType[] params = state.typeMap.get(node).parameters;
                    for (int i = 0; i < params.length; i++) {
                        args = new Term[] { new TypeVariableBinding(TypeVariableBinding.Kind.ALL,
                                params[i].toIvilType(state), args[0]) };
                    }
                }

            } catch (TermException e) {
                e.printStackTrace();
            } catch (EnvironmentException e) {
                e.printStackTrace();
            }

            // add axiom
            try {
                state.env.addAxiom(new Axiom("def_" + node.getName(), args[0], new HashMap<String, String>(), node));
            } catch (EnvironmentException e) {
                e.printStackTrace();
            }

        }

    }

    @Override
    public void visit(ProcedureDeclaration node) throws ASTVisitException {
        // ensure no illegal breaks can occur
        breakLabel = null;
        statements = new StatementTripel();
        modifiable = state.types.modifiable.get(node);

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

        state.translation.declarations.put(node, statements);
        statements = null;
    }

    @Override
    public void visit(Precondition node) throws ASTVisitException {
        defaultAction(node);

        try {
            statements.preStatements.add(new AssumeStatement(node.getLocationToken().beginLine, state.translation.terms
                    .get(node.getCondition())));
            statements.preAnnotations.add(null);
        } catch (TermException e) {
            e.printStackTrace();
            throw new ASTVisitException(e);
        }
    }

    @Override
    public void visit(ModifiesClause node) throws ASTVisitException {

        for (String name : node.getTargets()) {
            VariableDeclaration v = state.names.findVariable(name, node);

            String oldName = "old_" + state.translation.variableNames.get(v);

            try {
                Function old;
                if (null == state.env.getFunction(oldName)) {
                    old = new Function(oldName, state.ivilTypeMap.get(v), NO_TYPE, false, true, node);
                    state.env.addFunction(old);
                } else {
                    old = state.env.getFunction(oldName);
                }

                Function var = state.env.getFunction(state.translation.variableNames.get(v));

                if (null == var) {
                    var = new Function(state.translation.variableNames.get(v), state.ivilTypeMap.get(v), NO_TYPE,
                            false, true, node);
                    state.env.addFunction(var);
                }

                // add assignments in front of contracts, to make sure no error
                // occurs, if some idiot uses old in requires
                statements.preStatements.add(0,
                        new de.uka.iti.pseudo.term.statement.AssignmentStatement(node.getLocationToken().beginLine,
                                new Application(old, old.getResultType()), new Application(var, var.getResultType())));
                statements.preAnnotations.add(0, null);
            } catch (TermException e) {
                e.printStackTrace();
                throw new ASTVisitException(node.getLocation(), e);

            } catch (EnvironmentException e) {
                e.printStackTrace();
                throw new ASTVisitException(node.getLocation(), e);
            }
        }
    }

    @Override
    public void visit(Postcondition node) throws ASTVisitException {

        if (node.isFree())
            return;

        defaultAction(node);

        try {
            statements.postStatements.add(new AssertStatement(node.getLocationToken().beginLine,
                    state.translation.terms.get(node.getCondition())));
            statements.postAnnotations.add(null);
        } catch (TermException e) {
            e.printStackTrace();
            throw new ASTVisitException(e);
        }
    }

    @Override
    public void visit(ProcedureImplementation node) throws ASTVisitException {
        // ensure no illegal breaks can occur
        breakLabel = null;
        statements = new StatementTripel();

        modifiable = state.types.modifiable.get(state.names.procedureSpace.get(node.getName()));

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

        state.translation.implementations.put(node, statements);
        statements = null;
    }

    @Override
    public void visit(GotoStatement node) throws ASTVisitException {

        // push skip statement, that will be replaced later
        defaultAction(node);

        try {
            statements.bodyStatements.add(new SkipStatement(node.getLocationToken().beginLine, NO_ARGS));

            StringBuffer target = new StringBuffer();
            target.append("$goto");
            for (String d : node.getDestinations()) {

                target.append(";");
                target.append(d);
            }

            statements.bodyAnnotations.add(target.toString());
        } catch (TermException e) {
            e.printStackTrace();
            throw new ASTVisitException(e);
        }
    }

    @Override
    public void visit(ReturnStatement node) throws ASTVisitException {
        try {
            statements.bodyStatements.add(new SkipStatement(node.getLocationToken().beginLine, NO_ARGS));
            statements.bodyAnnotations.add("$return");
        } catch (TermException e) {
            e.printStackTrace();
            throw new ASTVisitException(e);
        }
    }

    @Override
    public void visit(IfStatement node) throws ASTVisitException {
        // evaluate guard
        node.getGuard().visit(this);

        // create label names for then/else/end
        String labelThen = "then" + node.getLocation();
        String labelElse = "else" + node.getLocation();
        String labelEnd = "end" + node.getLocation();

        // jump to then and else
        try {
            statements.bodyStatements.add(new SkipStatement(node.getLocationToken().beginLine, NO_ARGS));
            statements.bodyAnnotations.add("$goto;" + labelThen + ";" + labelElse);

            // then assumes condition to be true
            {
                statements.bodyStatements.add(new SkipStatement(node.getLocationToken().beginLine, NO_ARGS));
                statements.bodyAnnotations.add("$label:" + labelThen);

                statements.bodyStatements.add(new AssumeStatement(node.getLocationToken().beginLine,
                        state.translation.terms.get(node.getGuard())));
                statements.bodyAnnotations.add(null);

                for (de.uka.iti.pseudo.parser.boogie.ast.Statement s : node.getThenBlock()) {
                    s.visit(this);
                }
                statements.bodyStatements.add(new SkipStatement(node.getLocationToken().beginLine, NO_ARGS));
                statements.bodyAnnotations.add("$goto;" + labelEnd);
            }

            // else assumes condition to be false
            {
                statements.bodyStatements.add(new SkipStatement(node.getLocationToken().beginLine, NO_ARGS));
                statements.bodyAnnotations.add("$label:" + labelElse);

                statements.bodyStatements.add(new AssumeStatement(node.getLocationToken().beginLine, new Application(
                        state.env.getFunction("$not"), Environment.getBoolType(), new Term[] { state.translation.terms
                                .get(node.getGuard()) })));
                statements.bodyAnnotations.add(null);

                if (node.getElseBlock() != null) {

                    for (de.uka.iti.pseudo.parser.boogie.ast.Statement s : node.getElseBlock()) {
                        s.visit(this);
                    }
                    statements.bodyStatements.add(new SkipStatement(node.getLocationToken().beginLine, NO_ARGS));
                    statements.bodyAnnotations.add("$goto;" + labelEnd);
                }
            }

            // add end label
            statements.bodyStatements.add(new SkipStatement(node.getLocationToken().beginLine, NO_ARGS));
            statements.bodyAnnotations.add("$label:" + labelEnd);

        } catch (TermException e) {
            e.printStackTrace();
            throw new ASTVisitException(e);
        }
    }

    @Override
    public void visit(WhileStatement node) throws ASTVisitException {

        // evaluate guard
        node.getGuard().visit(this);

        String oldBreak = breakLabel;
        // create label names for then/else/end
        String labelBegin = "begin" + node.getLocation();
        String labelBody = "body" + node.getLocation();
        String labelEnd = "end" + node.getLocation();
        // break label is behind the assume !condition, as it can not be
        // assumed, that the condition wont hold any longer
        breakLabel = "break" + node.getLocation();

        // jump to then and else
        try {

            statements.bodyStatements.add(new SkipStatement(node.getLocationToken().beginLine, NO_ARGS));
            statements.bodyAnnotations.add("$label:" + labelBegin);

            // create invariant
            Term invariant = null;
            Term freeInvariant = null;
            if (node.getInvariants().size() != 0) {
                for (int i = 0; i < node.getInvariants().size(); i++) {
                    LoopInvariant inv = node.getInvariants().get(i);
                    inv.visit(this);

                    if (inv.isFree()) {
                        if (null == freeInvariant)
                            freeInvariant = state.translation.terms.get(inv.getExpression());
                        else
                            freeInvariant = new Application(state.env.getFunction("$and"), Environment.getBoolType(),
                                    new Term[] { state.translation.terms.get(inv.getExpression()), freeInvariant });
                    } else {
                        if (null == invariant)
                            invariant = state.translation.terms.get(inv.getExpression());
                        else
                            invariant = new Application(state.env.getFunction("$and"), Environment.getBoolType(),
                                    new Term[] { state.translation.terms.get(inv.getExpression()), invariant });
                    }
                }
            }

            if (null != invariant) {
                statements.bodyStatements.add(new SkipStatement(node.getLocationToken().beginLine,
                        new Term[] { invariant }));
                statements.bodyAnnotations.add(null);
            }

            statements.bodyStatements.add(new SkipStatement(node.getLocationToken().beginLine, NO_ARGS));
            statements.bodyAnnotations.add("$goto;" + labelBody + ";" + labelEnd);

            // loop body
            {
                statements.bodyStatements.add(new SkipStatement(node.getLocationToken().beginLine, NO_ARGS));
                statements.bodyAnnotations.add("$label:" + labelBody);

                // assume free invariant
                if (null != freeInvariant) {
                    statements.bodyStatements
                            .add(new AssumeStatement(node.getLocationToken().beginLine, freeInvariant));
                    statements.bodyAnnotations.add(null);
                }

                statements.bodyStatements.add(new AssumeStatement(node.getLocationToken().beginLine,
                        state.translation.terms.get(node.getGuard())));
                statements.bodyAnnotations.add(null);

                for (de.uka.iti.pseudo.parser.boogie.ast.Statement s : node.getBody()) {
                    s.visit(this);
                }
                statements.bodyStatements.add(new SkipStatement(node.getLocationToken().beginLine, NO_ARGS));
                statements.bodyAnnotations.add("$goto;" + labelBegin);
            }

            // end of the loop
            statements.bodyStatements.add(new SkipStatement(node.getLocationToken().beginLine, NO_ARGS));
            statements.bodyAnnotations.add("$label:" + labelEnd);

            // assume free invariant
            if (null != freeInvariant) {
                statements.bodyStatements.add(new AssumeStatement(node.getLocationToken().beginLine, freeInvariant));
                statements.bodyAnnotations.add(null);
            }

            statements.bodyStatements.add(new AssumeStatement(node.getLocationToken().beginLine, new Application(
                    state.env.getFunction("$not"), Environment.getBoolType(), new Term[] { state.translation.terms
                            .get(node.getGuard()) })));
            statements.bodyAnnotations.add(null);

            statements.bodyStatements.add(new SkipStatement(node.getLocationToken().beginLine, NO_ARGS));
            statements.bodyAnnotations.add("$label:" + breakLabel);

        } catch (TermException e) {
            e.printStackTrace();
            throw new ASTVisitException(e);

        } finally {
            // restore break label, to allow for nested loops
            breakLabel = oldBreak;
        }
    }

    @Override
    public void visit(BreakStatement node) throws ASTVisitException {
        // push skip statement, that will be replaced later
        defaultAction(node);

        try {
            statements.bodyStatements.add(new SkipStatement(node.getLocationToken().beginLine, NO_ARGS));

            String target = "$goto;" + (node.hasTarget() ? node.getTarget() : breakLabel);
            statements.bodyAnnotations.add(target);

        } catch (TermException e) {
            e.printStackTrace();
            throw new ASTVisitException(e);
        }
    }

    @Override
    public void visit(AssertionStatement node) throws ASTVisitException {
        defaultAction(node);

        try {
            statements.bodyStatements.add(new AssertStatement(node.getLocationToken().beginLine,
                    state.translation.terms.get(node.getAssertion())));
            statements.bodyAnnotations.add(null);
        } catch (TermException e) {
            e.printStackTrace();
            throw new ASTVisitException(e);
        }
    }

    @Override
    public void visit(AssumptionStatement node) throws ASTVisitException {
        defaultAction(node);

        try {
            statements.bodyStatements.add(new AssumeStatement(node.getLocationToken().beginLine,
                    state.translation.terms.get(node.getAssertion())));
            statements.bodyAnnotations.add(null);
        } catch (TermException e) {
            e.printStackTrace();
            throw new ASTVisitException(e);
        }
    }

    @Override
    public void visit(HavocStatement node) throws ASTVisitException {
        defaultAction(node);

        for (String name : node.getVarnames()) {
            try {
                VariableDeclaration decl = state.names.findVariable(name, node);

                statements.bodyStatements.add(new de.uka.iti.pseudo.term.statement.HavocStatement(node
                        .getLocationToken().beginLine, new Application(state.env
                        .getFunction(state.translation.variableNames.get(decl)), state.ivilTypeMap.get(decl))));
                statements.bodyAnnotations.add(null);
            } catch (TermException e) {
                e.printStackTrace();
                throw new ASTVisitException(e);
            }
        }
    }

    @Override
    public void visit(WildcardExpression node) throws ASTVisitException {
        defaultAction(node);

        try {
            String name = state.env.createNewFunctionName("wildcard");

            Function w = new Function(name, state.ivilTypeMap.get(node), NO_TYPE, false, false, node);
            state.env.addFunction(w);

            // call forall will quantify wildcards
            if (node.getParent() instanceof CallForallStatement) {
                state.translation.terms.put(node, new de.uka.iti.pseudo.term.Variable(w.getName(), w.getResultType()));
            } else {
                state.translation.terms.put(node, new Application(w, state.ivilTypeMap.get(node)));
            }

        } catch (TermException e) {
            e.printStackTrace();
            throw new ASTVisitException(node.getLocation(), e);

        } catch (EnvironmentException e) {
            e.printStackTrace();
            throw new ASTVisitException(node.getLocation(), e);
        }
    }

    @Override
    public void visit(CallForallStatement node) throws ASTVisitException {
        // assume ∀wildcard PRE => POST

        // ///// gather informations //////
        ProcedureDeclaration P = state.names.procedureSpace.get(node.getName());

        LinkedList<Precondition> assertions = new LinkedList<Precondition>();
        LinkedList<Postcondition> assumptions = new LinkedList<Postcondition>();

        for (Specification s : P.getSpecification()) {
            if (s instanceof Precondition)
                assertions.add((Precondition) s);
            else if (s instanceof Postcondition)
                assumptions.add((Postcondition) s);
        }

        String oldIns[] = new String[P.getInParameters().size()];
        for (int i = 0; i < oldIns.length; i++)
            oldIns[i] = state.translation.variableNames.get(P.getInParameters().get(i));

        String oldOuts[] = new String[P.getOutParameters().size()];
        for (int i = 0; i < oldOuts.length; i++)
            oldOuts[i] = state.translation.variableNames.get(P.getOutParameters().get(i));

        // create new variables for in and out parameters
        Function newIns[] = new Function[oldIns.length];
        Function newOuts[] = new Function[oldOuts.length];

        try {
            for (int i = 0; i < newIns.length; i++) {
                newIns[i] = new Function(state.env.createNewFunctionName("call_in" + i + "_"), state.ivilTypeMap.get(P
                        .getInParameters().get(i)), NO_TYPE, false, true, node);

                state.env.addFunction(newIns[i]);
            }

            for (int i = 0; i < newOuts.length; i++) {
                newOuts[i] = new Function(state.env.createNewFunctionName("call_out" + i + "_"),
                        state.ivilTypeMap.get(P.getOutParameters().get(i)), NO_TYPE, false, true, node);

                state.env.addFunction(newOuts[i]);
            }

        } catch (EnvironmentException e) {
            e.printStackTrace();
            throw new ASTVisitException(node.getLocation(), e);
        }

        // ///// create terms ////////

        List<de.uka.iti.pseudo.term.Variable> boundvars = new LinkedList<de.uka.iti.pseudo.term.Variable>();

        try {
            List<Assignment> assignments = new LinkedList<Assignment>();
            if (newIns.length > 0) {
                for (int i = 0; i < newIns.length; i++) {
                    Expression val = node.getArguments().get(i);

                    val.visit(this);

                    if (val instanceof WildcardExpression) {
                        de.uka.iti.pseudo.term.Variable var = (de.uka.iti.pseudo.term.Variable) state.translation.terms
                                .get(val);
                        boundvars.add(var);
                        boundVars.put(newIns[i].getName(), var);
                    } else {
                        assignments.add(new Assignment(new Application(newIns[i], newIns[i].getResultType()),
                                state.translation.terms.get(val)));
                    }
                }

                if (assignments.size() > 0) {
                    statements.bodyStatements.add(new de.uka.iti.pseudo.term.statement.AssignmentStatement(node
                            .getLocationToken().beginLine, assignments));

                    statements.bodyAnnotations.add(null);
                }
            }
        } catch (TermException e) {
            e.printStackTrace();
            throw new ASTVisitException(node.getLocation(), e);
        }

        // overwrite variable names for in and out parameters
        for (int i = 0; i < newIns.length; i++)
            state.translation.variableNames.put(P.getInParameters().get(i), newIns[i].getName());
        for (int i = 0; i < newOuts.length; i++)
            state.translation.variableNames.put(P.getOutParameters().get(i), newOuts[i].getName());

        Term PRE = Environment.getTrue();
        Term POST = Environment.getTrue();

        // gather precondition
        try {
            for (Precondition cond : assertions) {
                if (cond.isFree())
                    continue;

                cond.getCondition().visit(this);

                PRE = new Application(state.env.getFunction("$and"), Environment.getBoolType(), new Term[] { PRE,
                        state.translation.terms.get(cond.getCondition()) });
            }

        } catch (TermException e) {
            e.printStackTrace();
            throw new ASTVisitException(node.getLocation(), e);
        }

        // gather postcondition
        try {
            for (Postcondition cond : assumptions) {
                cond.getCondition().visit(this);

                POST = new Application(state.env.getFunction("$and"), Environment.getBoolType(), new Term[] { POST,
                        state.translation.terms.get(cond.getCondition()) });
            }

        } catch (TermException e) {
            e.printStackTrace();
            throw new ASTVisitException(node.getLocation(), e);
        }

        // restore variable names for in and out parameters
        for (int i = 0; i < newIns.length; i++) {
            state.translation.variableNames.put(P.getInParameters().get(i), oldIns[i]);
        }
        for (int i = 0; i < newOuts.length; i++)
            state.translation.variableNames.put(P.getOutParameters().get(i), oldOuts[i]);

        // add assumption
        Term assumption;
        try {
            assumption = new Application(state.env.getFunction("$impl"), Environment.getBoolType(), new Term[] { PRE,
                    POST });

            while (boundvars.size() > 0)
                assumption = new Binding(state.env.getBinder("\\forall"), Environment.getBoolType(),
                        boundvars.remove(0), new Term[] { assumption });

            // note: no type quantification is needed here, as polymorphic
            // arguments must have infered types

            statements.bodyStatements.add(new AssumeStatement(node.getLocationToken().beginLine, assumption));
            statements.bodyAnnotations.add(null);

        } catch (TermException e) {
            e.printStackTrace();
            throw new ASTVisitException(node.getLocation(), e);
        }
    }

    @Override
    public void visit(CallStatement node) throws ASTVisitException {

        // ///// gather informations //////
        ProcedureDeclaration P = state.names.procedureSpace.get(node.getName());

        LinkedList<Precondition> assertions = new LinkedList<Precondition>();
        LinkedList<ModifiesClause> modifies = new LinkedList<ModifiesClause>();
        LinkedList<Postcondition> assumptions = new LinkedList<Postcondition>();

        for (Specification s : P.getSpecification()) {
            if (s instanceof Precondition)
                assertions.add((Precondition) s);
            else if (s instanceof Postcondition)
                assumptions.add((Postcondition) s);
            else
                modifies.add((ModifiesClause) s);
        }

        String oldIns[] = new String[P.getInParameters().size()];
        for (int i = 0; i < oldIns.length; i++)
            oldIns[i] = state.translation.variableNames.get(P.getInParameters().get(i));

        String oldOuts[] = new String[P.getOutParameters().size()];
        for (int i = 0; i < oldOuts.length; i++)
            oldOuts[i] = state.translation.variableNames.get(P.getOutParameters().get(i));

        // create new variables for in and out parameters
        Function newIns[] = new Function[oldIns.length];
        Function newOuts[] = new Function[oldOuts.length];

        try {
            for (int i = 0; i < newIns.length; i++) {
                newIns[i] = new Function(state.env.createNewFunctionName("call_in" + i + "_"), state.ivilTypeMap.get(P
                        .getInParameters().get(i)), NO_TYPE, false, true, node);

                state.env.addFunction(newIns[i]);
            }

            for (int i = 0; i < newOuts.length; i++) {
                newOuts[i] = new Function(state.env.createNewFunctionName("call_out" + i + "_"),
                        state.ivilTypeMap.get(P.getOutParameters().get(i)), NO_TYPE, false, true, node);

                state.env.addFunction(newOuts[i]);
            }

        } catch (EnvironmentException e) {
            e.printStackTrace();
            throw new ASTVisitException(node.getLocation(), e);
        }

        // ///// create code ////////

        // TODO SAVE_OLD: save old_* variables somewhere and replace them with
        // current values

        // load parameters
        try {
            Assignment assignments[] = new Assignment[newIns.length];
            if (assignments.length > 0) {
                for (int i = 0; i < assignments.length; i++) {
                    Expression val = node.getArguments().get(i);

                    val.visit(this);

                    assignments[i] = new Assignment(new Application(newIns[i], newIns[i].getResultType()),
                            state.translation.terms.get(val));
                }

                statements.bodyStatements.add(new de.uka.iti.pseudo.term.statement.AssignmentStatement(node
                        .getLocationToken().beginLine, Arrays.asList(assignments)));

                statements.bodyAnnotations.add(null);
            }
        } catch (TermException e) {
            e.printStackTrace();
            throw new ASTVisitException(node.getLocation(), e);
        }

        // overwrite variable names for in and out parameters
        for (int i = 0; i < newIns.length; i++)
            state.translation.variableNames.put(P.getInParameters().get(i), newIns[i].getName());
        for (int i = 0; i < newOuts.length; i++)
            state.translation.variableNames.put(P.getOutParameters().get(i), newOuts[i].getName());

        // assert precondition
        try {
            for (Precondition cond : assertions) {
                if (cond.isFree())
                    continue;

                cond.getCondition().visit(this);

                statements.bodyStatements.add(new AssertStatement(node.getLocationToken().beginLine,
                        state.translation.terms.get(cond.getCondition())));
                statements.bodyAnnotations.add(null);
            }

        } catch (TermException e) {
            e.printStackTrace();
            throw new ASTVisitException(node.getLocation(), e);
        }

        // havoc modified values
        for (ModifiesClause clause : modifies) {
            for (String name : clause.getTargets())
                try {
                    VariableDeclaration decl = state.names.findVariable(name, node);

                    statements.bodyStatements.add(new de.uka.iti.pseudo.term.statement.HavocStatement(node
                            .getLocationToken().beginLine, new Application(state.env
                            .getFunction(state.translation.variableNames.get(decl)), state.ivilTypeMap.get(decl))));
                    statements.bodyAnnotations.add(null);
                } catch (TermException e) {
                    e.printStackTrace();
                    throw new ASTVisitException(e);
                }
        }

        // assume postcondition
        try {
            for (Postcondition cond : assumptions) {
                cond.getCondition().visit(this);

                statements.bodyStatements.add(new AssumeStatement(node.getLocationToken().beginLine,
                        state.translation.terms.get(cond.getCondition())));
                statements.bodyAnnotations.add(null);
            }

        } catch (TermException e) {
            e.printStackTrace();
            throw new ASTVisitException(node.getLocation(), e);
        }

        // restore variable names for in and out parameters
        for (int i = 0; i < newIns.length; i++)
            state.translation.variableNames.put(P.getInParameters().get(i), oldIns[i]);
        for (int i = 0; i < newOuts.length; i++)
            state.translation.variableNames.put(P.getOutParameters().get(i), oldOuts[i]);

        // safe results
        try {
            Assignment assignments[] = new Assignment[newOuts.length];
            if (assignments.length > 0) {
                for (int i = 0; i < assignments.length; i++) {
                    VariableUsageExpression target = node.getOutParam().get(i);

                    target.visit(this);

                    assignments[i] = new Assignment(state.translation.terms.get(target), new Application(newOuts[i],
                            newOuts[i].getResultType()));
                }

                statements.bodyStatements.add(new de.uka.iti.pseudo.term.statement.AssignmentStatement(node
                        .getLocationToken().beginLine, Arrays.asList(assignments)));

                statements.bodyAnnotations.add(null);
            }
        } catch (TermException e) {
            e.printStackTrace();
            throw new ASTVisitException(node.getLocation(), e);
        }

        // TODO RESTORE_OLD: revert changes done by SAVE_OLD
    }

    @Override
    public void visit(LabelStatement node) throws ASTVisitException {
        try {
            statements.bodyStatements.add(new SkipStatement(node.getLocationToken().beginLine, NO_ARGS));
            statements.bodyAnnotations.add("$label:" + node.getName());
        } catch (TermException e) {
            e.printStackTrace();
            throw new ASTVisitException(e);
        }
    }

    @Override
    public void visit(SimpleAssignment node) throws ASTVisitException {
        defaultAction(node);

        try {
            Term target = state.translation.terms.get(node.getTarget());
            Term nval = state.translation.terms.get(node.getNewValue());

            // we cannot assign map<D>_load statements, so create similar
            // map_store's

            // ! @note: this works, because we are transforming lvalues, thus no
            // other map operation can occur here
            while (target instanceof Application && ((Application) target).getFunction().getName().startsWith("map")
                    && !((Application) target).getFunction().isAssignable()) {
                // currently, terms[getTarget] is a map_load(name, domain),
                // but
                // we need:
                // name := store(name, domain, newValue)

                Term name = target.getSubterm(0);

                Term args[] = new Term[1 + target.getSubterms().size()];

                args[0] = name;
                for (int i = 1; i < target.getSubterms().size(); i++)
                    args[i] = target.getSubterm(i);

                args[args.length - 1] = nval;

                Function store = state.env.getFunction("map" + (args.length - 2) + "_store");

                if (name instanceof Application && ((Application) name).getFunction().getName().startsWith("map")
                        && !((Application) name).getFunction().isAssignable()) {

                    // unroll by creating new inner variables
                    Application tmp = new Application(new Function(state.env.createNewFunctionName("innerMap"),
                            name.getType(), NO_TYPE, false, true, node), name.getType());
                    state.env.addFunction(tmp.getFunction());

                    args[0] = tmp;

                    statements.bodyStatements.add(new de.uka.iti.pseudo.term.statement.AssignmentStatement(node
                            .getLocationToken().beginLine, tmp, new Application(store, tmp.getType(), args)));

                    statements.bodyAnnotations.add(null);

                    target = name;
                    nval = tmp;

                } else {

                    statements.bodyStatements.add(new de.uka.iti.pseudo.term.statement.AssignmentStatement(node
                            .getLocationToken().beginLine, name, new Application(store, name.getType(), args)));

                    statements.bodyAnnotations.add(null);

                    return;
                }
            }

            statements.bodyStatements.add(new de.uka.iti.pseudo.term.statement.AssignmentStatement(node
                    .getLocationToken().beginLine, target, nval));

            statements.bodyAnnotations.add(null);

        } catch (TermException e) {
            e.printStackTrace();
            throw new ASTVisitException(node.getLocation(), e);

        } catch (EnvironmentException e) {
            e.printStackTrace();
            throw new ASTVisitException(node.getLocation(), e);
        }
    }

    @Override
    public void visit(AssignmentStatement node) throws ASTVisitException {
        for (ASTElement e : node.getChildren())
            e.visit(this);

        int size = node.getAssignments().size();
        if (size > 1) {
            // the last #assignments Statements need to be merged into one
            // update statement
            List<Assignment> children = new ArrayList<Assignment>();
            int position = statements.bodyStatements.size() - 1;
            for (int i = 0; i < size; i++) {
                de.uka.iti.pseudo.term.statement.AssignmentStatement assStatement = (de.uka.iti.pseudo.term.statement.AssignmentStatement) statements.bodyStatements
                        .remove(position);
                children.addAll(assStatement.getAssignments());

                statements.bodyAnnotations.remove(position);

                position--;
            }

            try {
                statements.bodyStatements.add(new de.uka.iti.pseudo.term.statement.AssignmentStatement(node
                        .getLocationToken().beginLine, children));
            } catch (TermException e) {
                e.printStackTrace();
                throw new ASTVisitException(node.getLocation(), e);
            }
            statements.bodyAnnotations.add(null);
        }
    }

    @Override
    public void visit(AdditionExpression node) throws ASTVisitException {
        defaultAction(node);

        Term[] args = new Term[2];

        for (int i = 0; i < 2; i++)
            args[i] = state.translation.terms.get(node.getOperands().get(i));

        try {
            state.translation.terms.put(node, new Application(state.env.getFunction("$plus"), Environment.getIntType(),
                    args));
        } catch (TermException e) {
            e.printStackTrace();
            throw new ASTVisitException(e);
        }
    }

    @Override
    public void visit(SubtractionExpression node) throws ASTVisitException {
        defaultAction(node);

        Term[] args = new Term[2];

        for (int i = 0; i < 2; i++)
            args[i] = state.translation.terms.get(node.getOperands().get(i));

        try {
            state.translation.terms.put(node, new Application(state.env.getFunction("$minus"),
                    Environment.getIntType(), args));
        } catch (TermException e) {
            e.printStackTrace();
            throw new ASTVisitException(e);
        }
    }

    @Override
    public void visit(EquivalenceExpression node) throws ASTVisitException {
        defaultAction(node);

        Term[] args = new Term[2];

        for (int i = 0; i < 2; i++)
            args[i] = state.translation.terms.get(node.getOperands().get(i));

        try {
            state.translation.terms.put(node,
                    new Application(state.env.getFunction("$equiv"), Environment.getBoolType(), args));
        } catch (TermException e) {
            e.printStackTrace();
            throw new ASTVisitException(e);
        }
    }

    @Override
    public void visit(ImpliesExpression node) throws ASTVisitException {
        defaultAction(node);

        Term[] args = new Term[2];

        for (int i = 0; i < 2; i++)
            args[i] = state.translation.terms.get(node.getOperands().get(i));

        try {
            state.translation.terms.put(node, new Application(state.env.getFunction("$impl"),
                    Environment.getBoolType(), args));
        } catch (TermException e) {
            e.printStackTrace();
            throw new ASTVisitException(e);
        }
    }

    @Override
    public void visit(AndExpression node) throws ASTVisitException {
        defaultAction(node);

        Term[] args = new Term[2];

        for (int i = 0; i < 2; i++)
            args[i] = state.translation.terms.get(node.getOperands().get(i));

        try {
            state.translation.terms.put(node, new Application(state.env.getFunction("$and"), Environment.getBoolType(),
                    args));
        } catch (TermException e) {
            e.printStackTrace();
            throw new ASTVisitException(e);
        }
    }

    @Override
    public void visit(OrExpression node) throws ASTVisitException {
        defaultAction(node);

        Term[] args = new Term[2];

        for (int i = 0; i < 2; i++)
            args[i] = state.translation.terms.get(node.getOperands().get(i));

        try {
            state.translation.terms.put(node, new Application(state.env.getFunction("$or"), Environment.getBoolType(),
                    args));
        } catch (TermException e) {
            e.printStackTrace();
            throw new ASTVisitException(e);
        }
    }

    @Override
    public void visit(EqualsExpression node) throws ASTVisitException {
        Term[] args = new Term[2];

        for (int i = 0; i < 2; i++) {
            Expression operand = node.getOperands().get(i);
            operand.visit(this);
            args[i] = state.translation.terms.get(operand);
        }

        try {
            state.translation.terms.put(node, new Application(state.env.getFunction("$eq"), Environment.getBoolType(),
                    args));
        } catch (TermException e) {
            e.printStackTrace();
            throw new ASTVisitException(e);
        }
    }

    @Override
    public void visit(EqualsNotExpression node) throws ASTVisitException {
        defaultAction(node);

        Term[] args = new Term[2];

        for (int i = 0; i < 2; i++)
            args[i] = state.translation.terms.get(node.getOperands().get(i));

        try {

            state.translation.terms.put(node, new Application(state.env.getFunction("$not"), Environment.getBoolType(),
                    new Term[] { new Application(state.env.getFunction("$eq"), Environment.getBoolType(), args) }));
        } catch (TermException e) {
            e.printStackTrace();
            throw new ASTVisitException(e);
        }
    }

    @Override
    public void visit(LessExpression node) throws ASTVisitException {
        defaultAction(node);

        Term[] args = new Term[2];

        for (int i = 0; i < 2; i++)
            args[i] = state.translation.terms.get(node.getOperands().get(i));

        try {
            state.translation.terms.put(node, new Application(state.env.getFunction("$lt"), Environment.getBoolType(),
                    args));
        } catch (TermException e) {
            e.printStackTrace();
            throw new ASTVisitException(e);
        }
    }

    @Override
    public void visit(LessEqualExpression node) throws ASTVisitException {
        defaultAction(node);

        Term[] args = new Term[2];

        for (int i = 0; i < 2; i++)
            args[i] = state.translation.terms.get(node.getOperands().get(i));

        try {
            state.translation.terms.put(node, new Application(state.env.getFunction("$lte"), Environment.getBoolType(),
                    args));
        } catch (TermException e) {
            e.printStackTrace();
            throw new ASTVisitException(e);
        }
    }

    @Override
    public void visit(GreaterExpression node) throws ASTVisitException {
        defaultAction(node);

        Term[] args = new Term[2];

        for (int i = 0; i < 2; i++)
            args[i] = state.translation.terms.get(node.getOperands().get(i));

        try {
            state.translation.terms.put(node, new Application(state.env.getFunction("$gt"), Environment.getBoolType(),
                    args));
        } catch (TermException e) {
            e.printStackTrace();
            throw new ASTVisitException(e);
        }
    }

    @Override
    public void visit(GreaterEqualExpression node) throws ASTVisitException {
        defaultAction(node);

        Term[] args = new Term[2];

        for (int i = 0; i < 2; i++)
            args[i] = state.translation.terms.get(node.getOperands().get(i));

        try {
            state.translation.terms.put(node, new Application(state.env.getFunction("$gte"), Environment.getBoolType(),
                    args));
        } catch (TermException e) {
            e.printStackTrace();
            throw new ASTVisitException(e);
        }
    }

    @Override
    public void visit(ConcatenationExpression node) throws ASTVisitException {
        defaultAction(node);

        Term[] args = new Term[2];

        for (int i = 0; i < 2; i++)
            args[i] = state.translation.terms.get(node.getOperands().get(i));

        try {
            state.translation.terms.put(node,
                    new Application(state.env.getFunction("$bv_concat"), state.env.mkType("bitvector"), args));
        } catch (TermException e) {
            e.printStackTrace();
            throw new ASTVisitException(e);

        } catch (EnvironmentException e) {
            e.printStackTrace();
            throw new ASTVisitException(e);
        }
    }

    @Override
    public void visit(MultiplicationExpression node) throws ASTVisitException {
        defaultAction(node);

        Term[] args = new Term[2];

        for (int i = 0; i < 2; i++)
            args[i] = state.translation.terms.get(node.getOperands().get(i));

        try {
            state.translation.terms.put(node, new Application(state.env.getFunction("$mult"), Environment.getIntType(),
                    args));
        } catch (TermException e) {
            e.printStackTrace();
            throw new ASTVisitException(e);
        }
    }

    @Override
    public void visit(DivisionExpression node) throws ASTVisitException {
        defaultAction(node);

        Term[] args = new Term[2];

        for (int i = 0; i < 2; i++)
            args[i] = state.translation.terms.get(node.getOperands().get(i));

        try {
            state.translation.terms.put(node, new Application(state.env.getFunction("$div"), Environment.getIntType(),
                    args));
        } catch (TermException e) {
            e.printStackTrace();
            throw new ASTVisitException(e);
        }
    }

    @Override
    public void visit(ModuloExpression node) throws ASTVisitException {
        defaultAction(node);

        Term[] args = new Term[2];

        for (int i = 0; i < 2; i++)
            args[i] = state.translation.terms.get(node.getOperands().get(i));

        try {
            state.translation.terms.put(node, new Application(state.env.getFunction("$mod"), Environment.getIntType(),
                    args));
        } catch (TermException e) {
            e.printStackTrace();
            throw new ASTVisitException(e);
        }
    }

    @Override
    public void visit(UnaryMinusExpression node) throws ASTVisitException {
        defaultAction(node);

        try {
            state.translation.terms.put(node, new Application(state.env.getFunction("$neg"), Environment.getIntType(),
                    new Term[] { state.translation.terms.get(node.getOperands().get(0)) }));
        } catch (TermException e) {
            e.printStackTrace();
            throw new ASTVisitException(e);
        }
    }

    @Override
    public void visit(NegationExpression node) throws ASTVisitException {
        defaultAction(node);

        try {
            state.translation.terms.put(node, new Application(state.env.getFunction("$not"), Environment.getBoolType(),
                    new Term[] { state.translation.terms.get(node.getOperands().get(0)) }));
        } catch (TermException e) {
            e.printStackTrace();
            throw new ASTVisitException(e);
        }
    }

    @Override
    public void visit(IntegerExpression node) throws ASTVisitException {

        try {
            state.translation.terms.put(node,
                    new Application(state.env.getNumberLiteral(node.getValue()), Environment.getIntType()));
        } catch (TermException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void visit(BitvectorAccessSelectionExpression node) throws ASTVisitException {

        node.getTarget().visit(this);

        try {
            state.translation.terms.put(
                    node,
                    new Application(state.env.getFunction("$bv_select"), state.env.mkType("bitvector"), new Term[] {
                            state.translation.terms.get(node.getTarget()),
                            new Application(state.env.getNumberLiteral(node.getFirst()), Environment.getIntType()),
                            new Application(state.env.getNumberLiteral(node.getLast()), Environment.getIntType()) }));

        } catch (TermException e) {
            e.printStackTrace();
            throw new ASTVisitException(e);

        } catch (EnvironmentException e) {
            e.printStackTrace();
            throw new ASTVisitException(e);
        }
    }

    @Override
    public void visit(MapAccessExpression node) throws ASTVisitException {
        // translate to mapI_load(name, domain)
        defaultAction(node);

        List<de.uka.iti.pseudo.parser.boogie.ast.Expression> d = node.getOperands();

        try {
            Term args[] = new Term[d.size() + 1];
            args[0] = state.translation.terms.get(node.getName());
            for (int i = 0; i < d.size(); i++)
                args[i + 1] = state.translation.terms.get(d.get(i));

            Type range_t = state.ivilTypeMap.get(node);

            Term tmp = new Application(state.env.getFunction("map" + d.size() + "_load"), range_t, args);

            state.translation.terms.put(node, tmp);

        } catch (TermException e) {
            e.printStackTrace();
            throw new ASTVisitException(node.getLocation(), e);
        }
    }

    @Override
    public void visit(MapUpdateExpression node) throws ASTVisitException {
        // translate to mapI_store(name, domain, update)
        defaultAction(node);

        List<de.uka.iti.pseudo.parser.boogie.ast.Expression> d = node.getOperands();

        try {
            Term args[] = new Term[d.size() + 2];
            args[0] = state.translation.terms.get(node.getName());
            for (int i = 0; i < d.size(); i++)
                args[i + 1] = state.translation.terms.get(d.get(i));

            args[args.length - 1] = state.translation.terms.get(node.getUpdate());

            Type type = state.ivilTypeMap.get(node);

            state.translation.terms.put(node, new Application(state.env.getFunction("map" + d.size() + "_store"), type,
                    args));

        } catch (TermException e) {
            e.printStackTrace();
            throw new ASTVisitException(node.getLocation(), e);

        }
    }

    @Override
    public void visit(TrueExpression node) throws ASTVisitException {
        state.translation.terms.put(node, Environment.getTrue());
    }

    @Override
    public void visit(FalseExpression node) throws ASTVisitException {
        state.translation.terms.put(node, Environment.getFalse());
    }

    @Override
    public void visit(BitvectorLiteralExpression node) throws ASTVisitException {
        defaultAction(node);

        try {
            Term[] args = new Term[2];
            args[0] = new Application(state.env.getNumberLiteral(node.getValue()), Environment.getIntType());
            args[1] = new Application(state.env.getNumberLiteral(node.getDimension()), Environment.getIntType());

            state.translation.terms.put(node,
                    new Application(state.env.getFunction("$bv_new"), state.env.mkType("bitvector"), args));

        } catch (TermException e) {
            e.printStackTrace();
            throw new ASTVisitException(e);

        } catch (EnvironmentException e) {
            e.printStackTrace();
            throw new ASTVisitException(e);

        }
    }

    @Override
    public void visit(FunctionCallExpression node) throws ASTVisitException {
        defaultAction(node);

        Term[] args = new Term[node.getOperands().size()];
        for (int i = 0; i < args.length; i++)
            args[i] = state.translation.terms.get(node.getOperands().get(i));

        try {
            state.translation.terms.put(node, new Application(state.env.getFunction("fun__" + node.getName()),
                    state.ivilTypeMap.get(node), args));

        } catch (TermException e) {
            e.printStackTrace();

            throw new ASTVisitException(e);
        }
    }

    @Override
    public void visit(VariableUsageExpression node) throws ASTVisitException {
        VariableDeclaration decl = state.names.findVariable(node);
        String name = state.translation.variableNames.get(decl);
        // modifiable can be null, if old is used in an environment where it
        // would not be allowed to be used according to Boogie2Manual. As old
        // would have no effect in such environments, we simply ignore it
        if (oldMode && null != modifiable && modifiable.contains(decl))
            name = "old_" + name;

        try {
            Term bound = boundVars.get(name);

            if (null != bound)
                state.translation.terms.put(node, bound);
            else
                state.translation.terms.put(node,
                        new Application(state.env.getFunction(name), state.ivilTypeMap.get(node)));

        } catch (TermException e) {
            e.printStackTrace();

            throw new ASTVisitException(e);
        }
    }

    @Override
    public void visit(OldExpression node) throws ASTVisitException {
        // fix nested olds, just ignore this old
        if (oldMode) {
            defaultAction(node);
            state.translation.terms.put(node, state.translation.terms.get(node.getOperands().get(0)));

        } else {
            oldMode = true;
            defaultAction(node);
            state.translation.terms.put(node, state.translation.terms.get(node.getOperands().get(0)));

            oldMode = false;
        }
    }

    @Override
    public void visit(ForallExpression node) throws ASTVisitException {
        defaultAction(node);

        Term[] args = new Term[1];

        // add body expression to args
        args[0] = state.translation.terms.get(node.getBody().getBody());

        // add quantifiers befor body
        try {
            String name;
            for (VariableDeclaration v : node.getBody().getQuantifiedVariables()) {
                name = state.translation.variableNames.get(v);
                args = new Term[] { new Binding(state.env.getBinder("\\forall"), Environment.getBoolType(),
                        boundVars.get(name), args) };
                boundVars.remove(name);
            }

            // add type quantifiers before ordinary quantifiers
            {
                UniversalType[] params = state.typeMap.get(node.getBody()).parameters;
                for (int i = 0; i < params.length; i++) {
                    args = new Term[] { new TypeVariableBinding(TypeVariableBinding.Kind.ALL,
                            params[i].toIvilType(state), args[0]) };
                }
            }

        } catch (TermException e) {
            e.printStackTrace();
        } catch (EnvironmentException e) {
            e.printStackTrace();
        }

        state.translation.terms.put(node, args[0]);
    }

    @Override
    public void visit(ExistsExpression node) throws ASTVisitException {

        defaultAction(node);

        Term[] args = new Term[1];

        // add body expression to args
        args[0] = state.translation.terms.get(node.getBody().getBody());

        // add quantifiers befor body
        try {
            String name;
            for (VariableDeclaration v : node.getBody().getQuantifiedVariables()) {
                name = state.translation.variableNames.get(v);
                args = new Term[] { new Binding(state.env.getBinder("\\exists"), Environment.getBoolType(),
                        boundVars.get(name), args) };
                boundVars.remove(name);
            }

            // add type quantifiers before ordinary quantifiers
            {
                UniversalType[] params = state.typeMap.get(node.getBody()).parameters;
                for (int i = 0; i < params.length; i++) {
                    args = new Term[] { new TypeVariableBinding(TypeVariableBinding.Kind.ALL,
                            params[i].toIvilType(state), args[0]) };
                }
            }

        } catch (TermException e) {
            e.printStackTrace();
        } catch (EnvironmentException e) {
            e.printStackTrace();
        }

        state.translation.terms.put(node, args[0]);
    }

    @Override
    public void visit(LambdaExpression node) throws ASTVisitException {
        defaultAction(node);

        QuantifierBody b = node.getBody();

        // \some map ; ∀domain; map = store(map, domain, expr(domain))
        try {
            Type map_t = state.ivilTypeMap.get(node);
            de.uka.iti.pseudo.term.Variable map = new de.uka.iti.pseudo.term.Variable("map", map_t);

            Term[] args = new Term[1 + b.getQuantifiedVariables().size() + 1];

            args[0] = map;
            for (int i = 0; i < b.getQuantifiedVariables().size(); i++)
                args[1 + i] = boundVars.get(state.translation.variableNames.get(b.getQuantifiedVariables().get(i)));

            args[args.length - 1] = state.translation.terms.get(b.getBody());

            // {map, store}
            args = new Term[] { map,
                    new Application(state.env.getFunction("map" + (args.length - 2) + "_store"), map_t, args) };

            // eq
            args = new Term[] { new Application(state.env.getFunction("$eq"), Environment.getBoolType(), args) };

            // ∀ domain
            try {
                String name;
                for (VariableDeclaration v : b.getQuantifiedVariables()) {
                    name = state.translation.variableNames.get(v);
                    args = new Term[] { new Binding(state.env.getBinder("\\forall"), Environment.getBoolType(),
                            boundVars.get(name), args) };
                    boundVars.remove(name);
                }
            } catch (TermException e) {
                e.printStackTrace();
            }

            // \some
            state.translation.terms.put(node, new Binding(state.env.getBinder("\\some"), map_t, map, args));

        } catch (TermException e) {
            e.printStackTrace();
            throw new ASTVisitException(node.getLocation(), e);
        }
    }

    @Override
    public void visit(IfThenElseExpression node) throws ASTVisitException {
        defaultAction(node);

        Term[] args = new Term[3];

        args[0] = state.translation.terms.get(node.getCondition());
        args[1] = state.translation.terms.get(node.getThen());
        args[2] = state.translation.terms.get(node.getElse());

        try {
            state.translation.terms.put(node,
                    new Application(state.env.getFunction("cond"), state.ivilTypeMap.get(node.getThen()), args));
        } catch (TermException e) {
            e.printStackTrace();
            throw new ASTVisitException(e);
        }
    }

    @Override
    public void visit(AttributeParameter node) throws ASTVisitException {
        // attributes are currently ignored
    }

    @Override
    public void visit(Attribute node) throws ASTVisitException {
        // attributes are currently ignored
    }

    @Override
    public void visit(Trigger node) throws ASTVisitException {
        // triggers are currently ignored
    }

    @Override
    public void visit(CoercionExpression node) throws ASTVisitException {
        // tfe: to me, it looks like nothing has to be done here, as coercions
        // have been evaluated during type annotation
    }

    @Override
    public void visit(ExtendsParent node) throws ASTVisitException {
        // TODO maybe we have to create missing constants
    }

    /*
     * TODO constant declaration:
     * 
     * @Override public void visit(OrderSpecification node) throws
     * ASTVisitException { defaultAction(node);
     * 
     * ConstantDeclaration parent = (ConstantDeclaration) node.getParent();
     * 
     * try { for (Variable c : parent.getNames()) {
     * 
     * if (node.isComplete()) { // ( ∀ w : Wicket • c <: w ⇒ c == w ∨ a <: w ∨ b
     * <: w )
     * 
     * } else { // c <: b & ∀ x :: (c==x | b==x) <-> (c <: x & x <: b) for
     * (OrderSpecParent par : node.getParents()) { Variable b =
     * state.names.findVariable(par.getName(), node);
     * 
     * Term ac = new
     * Application(state.env.getFunction(state.translation.variableNames
     * .get(c)),
     * 
     * state.ivilTypeMap.get(c));
     * 
     * Term ab = new Application(
     * 
     * state.env.getFunction(state.translation.variableNames.get(b)),
     * state.ivilTypeMap.get(b));
     * 
     * de.uka.iti.pseudo.term.Variable x = new
     * de.uka.iti.pseudo.term.Variable("x", ac.getType());
     * 
     * Term tmp = new Application(state.env.getFunction("$or"),
     * Environment.getBoolType(), new Term[] { new
     * Application(state.env.getFunction("$eq"), Environment.getBoolType(), new
     * Term[] { x, ab }), new Application(state.env.getFunction("$eq"),
     * Environment.getBoolType(), new Term[] { x, ac }) });
     * 
     * tmp = new Application(state.env.getFunction("$equiv"),
     * Environment.getBoolType(), new Term[] { tmp, new
     * Application(state.env.getFunction("$and"), Environment.getBoolType(), new
     * Term[] { new Application(state.env.getFunction("$extends"),
     * Environment.getBoolType(), new Term[] { ac, x }), new
     * Application(state.env.getFunction("$extends"), Environment.getBoolType(),
     * new Term[] { x, ab }) }) });
     * 
     * tmp = new Binding(state.env.getBinder("\\forall"),
     * Environment.getBoolType(), x, new Term[] { tmp });
     * 
     * state.env.addAxiom(new Axiom("orderSpec_" +
     * state.env.getAllAxioms().size(), new Application(
     * state.env.getFunction("$and"), Environment.getBoolType(), new Term[] {
     * new Application(state.env.getFunction("$extends"),
     * Environment.getBoolType(), new Term[] { ac, ab }), tmp }), new
     * HashMap<String, String>(), node)); } } } } catch (EnvironmentException e)
     * { e.printStackTrace(); throw new ASTVisitException(node.getLocation(),
     * e);
     * 
     * } catch (TermException e) { e.printStackTrace(); throw new
     * ASTVisitException(node.getLocation(), e); } }
     */
    /*
     * Code expressions are transformed into programs which can be executed by
     * IVIL. As code expressions can occur in quantified contexts, these
     * Programs may contain quantified variables.
     */
    @Override
    public void visit(CodeExpression node) throws ASTVisitException {
        // save statements of the current function or code expression to allow
        // for creation of a new one
        StatementTripel savedTripel = statements;
        statements = new StatementTripel();

        Variable oldRval = codeexpressionResult;

        Type result_t = state.ivilTypeMap.get(node);

        // create result variable
        try {
            String name = state.env.createNewFunctionName("rval");
            Function rval;
            rval = new Function(name, result_t, NO_TYPE, false, false, node);

            state.env.addFunction(rval);
            codeexpressionResult = new Variable("rval", result_t);

        } catch (EnvironmentException e) {
            e.printStackTrace();
            throw new ASTVisitException(node.getLocation(), e);

        }

        // declare variables
        for (LocalVariableDeclaration var : node.getVars())
            var.visit(this);

        // create code
        for (CodeBlock b : node.getCode())
            b.visit(this);

        String name = state.env.createNewProgramName("codeexpression" + node.getLocation().replace(":", "_"));
        Program C = null;
        try {
            C = state.translation.registerProgram(state, statements.bodyStatements, statements.bodyAnnotations, name,
                    node, -1);
        } catch (EnvironmentCreationException e) {
            e.printStackTrace();
            throw new ASTVisitException(node.getLocation() + "program creation failed", e);
        }

        // create term '\some rval; [codeexpression;0]'
        try {
            state.translation.terms.put(node, new Binding(state.env.getBinder("\\some"), result_t,
                    codeexpressionResult, new Term[] { new LiteralProgramTerm(0, true, C) }));

        } catch (TermException e) {
            e.printStackTrace();
            throw new ASTVisitException(node.getLocation(), e);
        }

        // restore statements of the current function or code expression
        codeexpressionResult = oldRval;
        statements = savedTripel;
    }

    @Override
    public void visit(CodeExpressionReturn node) throws ASTVisitException {
        // 'end rval = expr'
        node.getRval().visit(this);

        try {
            statements.bodyStatements.add(new EndStatement(node.getLocationToken().beginLine, new Application(state.env
                    .getFunction("$eq"), Environment.getBoolType(), new Term[] { codeexpressionResult,
                    state.translation.terms.get(node.getRval()) })));

            statements.bodyAnnotations.add(null);

        } catch (TermException e) {
            e.printStackTrace();
            throw new ASTVisitException(node.getLocation(), e);
        }
    }
}
