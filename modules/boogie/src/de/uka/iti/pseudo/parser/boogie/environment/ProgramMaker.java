package de.uka.iti.pseudo.parser.boogie.environment;

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
import de.uka.iti.pseudo.parser.boogie.ast.BitvectorSelectExpression;
import de.uka.iti.pseudo.parser.boogie.ast.BreakStatement;
import de.uka.iti.pseudo.parser.boogie.ast.CallForallStatement;
import de.uka.iti.pseudo.parser.boogie.ast.CallStatement;
import de.uka.iti.pseudo.parser.boogie.ast.CodeExpression;
import de.uka.iti.pseudo.parser.boogie.ast.CoercionExpression;
import de.uka.iti.pseudo.parser.boogie.ast.ConcatenationExpression;
import de.uka.iti.pseudo.parser.boogie.ast.ConstantDeclaration;
import de.uka.iti.pseudo.parser.boogie.ast.DivisionExpression;
import de.uka.iti.pseudo.parser.boogie.ast.EqualsExpression;
import de.uka.iti.pseudo.parser.boogie.ast.EqualsNotExpression;
import de.uka.iti.pseudo.parser.boogie.ast.EquivalenceExpression;
import de.uka.iti.pseudo.parser.boogie.ast.ExistsExpression;
import de.uka.iti.pseudo.parser.boogie.ast.FalseExpression;
import de.uka.iti.pseudo.parser.boogie.ast.ForallExpression;
import de.uka.iti.pseudo.parser.boogie.ast.FunctionCallExpression;
import de.uka.iti.pseudo.parser.boogie.ast.FunctionDeclaration;
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
import de.uka.iti.pseudo.parser.boogie.ast.LoopInvariant;
import de.uka.iti.pseudo.parser.boogie.ast.MapAccessExpression;
import de.uka.iti.pseudo.parser.boogie.ast.MapUpdateExpression;
import de.uka.iti.pseudo.parser.boogie.ast.ModifiesClause;
import de.uka.iti.pseudo.parser.boogie.ast.ModuloExpression;
import de.uka.iti.pseudo.parser.boogie.ast.MultiplicationExpression;
import de.uka.iti.pseudo.parser.boogie.ast.NegationExpression;
import de.uka.iti.pseudo.parser.boogie.ast.OldExpression;
import de.uka.iti.pseudo.parser.boogie.ast.OrExpression;
import de.uka.iti.pseudo.parser.boogie.ast.OrderSpecParent;
import de.uka.iti.pseudo.parser.boogie.ast.OrderSpecification;
import de.uka.iti.pseudo.parser.boogie.ast.PartialLessExpression;
import de.uka.iti.pseudo.parser.boogie.ast.Postcondition;
import de.uka.iti.pseudo.parser.boogie.ast.Precondition;
import de.uka.iti.pseudo.parser.boogie.ast.ProcedureDeclaration;
import de.uka.iti.pseudo.parser.boogie.ast.ProcedureImplementation;
import de.uka.iti.pseudo.parser.boogie.ast.QuantifierBody;
import de.uka.iti.pseudo.parser.boogie.ast.ReturnStatement;
import de.uka.iti.pseudo.parser.boogie.ast.SimpleAssignment;
import de.uka.iti.pseudo.parser.boogie.ast.SpecBlock;
import de.uka.iti.pseudo.parser.boogie.ast.SpecReturnStatement;
import de.uka.iti.pseudo.parser.boogie.ast.SubtractionExpression;
import de.uka.iti.pseudo.parser.boogie.ast.Trigger;
import de.uka.iti.pseudo.parser.boogie.ast.TrueExpression;
import de.uka.iti.pseudo.parser.boogie.ast.UnaryMinusExpression;
import de.uka.iti.pseudo.parser.boogie.ast.Variable;
import de.uka.iti.pseudo.parser.boogie.ast.VariableUsageExpression;
import de.uka.iti.pseudo.parser.boogie.ast.WhileStatement;
import de.uka.iti.pseudo.parser.boogie.ast.WildcardExpression;
import de.uka.iti.pseudo.parser.boogie.util.DefaultASTVisitor;
import de.uka.iti.pseudo.term.Application;
import de.uka.iti.pseudo.term.Binding;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.TermException;
import de.uka.iti.pseudo.term.Type;
import de.uka.iti.pseudo.term.statement.AssertStatement;
import de.uka.iti.pseudo.term.statement.AssumeStatement;
import de.uka.iti.pseudo.term.statement.Statement;

public final class ProgramMaker extends DefaultASTVisitor {

    private final EnvironmentCreationState state;

    private List<de.uka.iti.pseudo.term.statement.Statement> statements = null;
    private List<String> statementAnnotations = null;

    private List<de.uka.iti.pseudo.term.statement.Statement> preStatements = null;
    private List<String> preAnnotations = null;

    private List<de.uka.iti.pseudo.term.statement.Statement> postStatements = null;
    private List<String> postAnnotations = null;

    private Map<String, de.uka.iti.pseudo.term.Variable> boundVars = new HashMap<String, de.uka.iti.pseudo.term.Variable>();

    public ProgramMaker(EnvironmentCreationState state) throws EnvironmentCreationException {
        this.state = state;

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
    }

    @Override
    public void visit(ConstantDeclaration node) throws ASTVisitException {
        // TODO move unique to variables
    }

    @Override
    public void visit(Variable node) throws ASTVisitException {

        // TODO where clause?

        Type[] arguments = new Type[0];

        // the prefix ends with __ so it is impossible to create duplicates of a
        // variable
        String name = "_" + node.getName();

        for (Scope scope = state.scopeMap.get(node); scope != state.globalScope; scope = scope.parent) {
            name = scope.creator.getName() + "_" + name;
        }

        name = "var_" + name;

        state.translation.variableNames.put(node, name);

        // quantified variables differ from ordinary variables, as they arent
        // functions
        if (node.getParent() instanceof QuantifierBody || node.getParent() instanceof FunctionDeclaration) {
            boundVars.put(name, new de.uka.iti.pseudo.term.Variable(name, state.ivilTypeMap.get(node)));

        } else {
            try {
                state.env.addFunction(new Function(name, state.ivilTypeMap.get(node), arguments, false, !node
                        .isConstant(), node));

            } catch (EnvironmentException e) {
                e.printStackTrace();
                throw new ASTVisitException("Variable creation failed because of:\n", e);
            }
        }
    }

    @Override
    public void visit(FunctionDeclaration node) throws ASTVisitException {
        defaultAction(node);

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
        if (null != node.getExpression())
        {
            Term[] args = new Term[1];

            try {
                // add body expression to args
                args[0] = state.translation.terms.get(node.getExpression());

                Term[] inArgs = new Term[node.getInParameters().size()];
                for (int i = 0; i < inArgs.length; i++)
                    inArgs[i] = boundVars.get(state.translation.variableNames.get(node.getInParameters().get(i)));

                // add "expr == f(x)"
                args = new Term[] {
                        args[0],
                        new Application(state.env.getFunction("fun__" + node.getName()), state.ivilTypeMap.get(node
                                .getOutParemeter()), inArgs) };

                args = new Term[] { new Application(state.env.getFunction("$eq"), Environment.getBoolType(), args) };

                // add quantifiers before body
                for (Variable v : node.getInParameters()) {
                    args = new Term[] { new Binding(state.env.getBinder("\\forall"), Environment.getBoolType(),
                            boundVars.get(state.translation.variableNames.get(v)), args) };
                }

            } catch (TermException e) {
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
        if (node.getBody() != null) {

            statements = new LinkedList<Statement>();
            statementAnnotations = new LinkedList<String>();

            preStatements = new LinkedList<Statement>();
            preAnnotations = new LinkedList<String>();

            postStatements = new LinkedList<Statement>();
            postAnnotations = new LinkedList<String>();

            for (ASTElement e : node.getChildren())
                e.visit(this);

            // insert pre and postconditions
            preStatements.addAll(statements);
            preAnnotations.addAll(statementAnnotations);

            preStatements.addAll(postStatements);
            preAnnotations.addAll(postAnnotations);

            try {
                state.env.addProgram(new Program(node.getName(), state.root.getURL(), preStatements, preAnnotations,
                        node));
            } catch (EnvironmentException e) {
                e.printStackTrace();

                throw new ASTVisitException("Program creation failed:\n" + e.getMessage());
            }

        } else {
            // TODO maybe we have to do something with this declaration later
        }

    }

    @Override
    public void visit(Precondition node) throws ASTVisitException {
        defaultAction(node);

        try {
            preStatements.add(new AssumeStatement(node.getLocationToken().beginLine, state.translation.terms.get(node
                    .getCondition())));
            preAnnotations.add("");
        } catch (TermException e) {
            e.printStackTrace();
            throw new ASTVisitException(e);
        }
    }

    @Override
    public void visit(ModifiesClause node) throws ASTVisitException {

        // TODO modifies is currently completely ignored, there are also
        // compiletime checks missing -> SimpleAssignment

        // does this even create code? it should only be of interest in
        // implementation of call statements
    }

    @Override
    public void visit(Postcondition node) throws ASTVisitException {
        defaultAction(node);

        try {
            postStatements.add(new AssertStatement(node.getLocationToken().beginLine, state.translation.terms.get(node
                    .getCondition())));
            postAnnotations.add("");
        } catch (TermException e) {
            e.printStackTrace();
            throw new ASTVisitException(e);
        }
    }

    @Override
    public void visit(ProcedureImplementation node) throws ASTVisitException {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(GotoStatement node) throws ASTVisitException {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(ReturnStatement node) throws ASTVisitException {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(IfStatement node) throws ASTVisitException {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(LoopInvariant node) throws ASTVisitException {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(WhileStatement node) throws ASTVisitException {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(BreakStatement node) throws ASTVisitException {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(AssertionStatement node) throws ASTVisitException {
        defaultAction(node);

        try {
            statements.add(new AssertStatement(node.getLocationToken().beginLine, state.translation.terms.get(node
                    .getAssertion())));
            statementAnnotations.add("");
        } catch (TermException e) {
            e.printStackTrace();
            throw new ASTVisitException(e);
        }
    }

    @Override
    public void visit(AssumptionStatement node) throws ASTVisitException {
        defaultAction(node);

        try {
            statements.add(new AssumeStatement(node.getLocationToken().beginLine, state.translation.terms.get(node
                    .getAssertion())));
            statementAnnotations.add("");
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
                Variable decl = state.names.findVariable(name, node);

                statements.add(new de.uka.iti.pseudo.term.statement.HavocStatement(node.getLocationToken().beginLine,
                        new Application(state.env.getFunction(state.translation.variableNames.get(decl)),
                                state.ivilTypeMap.get(decl))));
                statementAnnotations.add("");
            } catch (TermException e) {
                e.printStackTrace();
                throw new ASTVisitException(e);
            }
        }
    }

    @Override
    public void visit(WildcardExpression node) throws ASTVisitException {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(CallForallStatement node) throws ASTVisitException {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(CallStatement node) throws ASTVisitException {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(LabelStatement node) throws ASTVisitException {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(SimpleAssignment node) throws ASTVisitException {
        defaultAction(node);

        try {
            statements.add(new de.uka.iti.pseudo.term.statement.AssignmentStatement(node.getLocationToken().beginLine,
                    state.translation.terms.get(node.getTarget()), state.translation.terms.get(node.getNewValue())));

            statementAnnotations.add("");

        } catch (TermException e) {
            e.printStackTrace();

            throw new ASTVisitException(e);
        }
    }

    @Override
    public void visit(AssignmentStatement node) throws ASTVisitException {
        // FIXME support for parallel assignment

        for (ASTElement e : node.getChildren())
            e.visit(this);
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
        defaultAction(node);

        Term[] args = new Term[2];

        for (int i = 0; i < 2; i++)
            args[i] = state.translation.terms.get(node.getOperands().get(i));

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
    public void visit(PartialLessExpression node) throws ASTVisitException {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(ConcatenationExpression node) throws ASTVisitException {
        // TODO Auto-generated method stub

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
        // TODO Auto-generated method stub

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
    public void visit(BitvectorSelectExpression node) throws ASTVisitException {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(BitvectorAccessSelectionExpression node) throws ASTVisitException {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(MapAccessExpression node) throws ASTVisitException {
        // TODO Auto-generated method stub

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
        // TODO Auto-generated method stub

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
        Variable decl = state.names.findVariable(node);

        try {
            // unbound variable
            if (null != state.env.getFunction(state.translation.variableNames.get(decl)))
                state.translation.terms.put(node,
                        new Application(state.env.getFunction(state.translation.variableNames.get(decl)),
                                state.ivilTypeMap.get(node)));
            // bound variable
            else
                state.translation.terms.put(node, boundVars.get(state.translation.variableNames.get(decl)));

        } catch (TermException e) {
            e.printStackTrace();

            throw new ASTVisitException(e);
        }
    }

    @Override
    public void visit(OldExpression node) throws ASTVisitException {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(ForallExpression node) throws ASTVisitException {
        defaultAction(node);

        Term[] args = new Term[1];

        // add body expression to args
        args[0] = state.translation.terms.get(node.getBody().getBody());

        // add quantifiers befor body
        for (Variable v : node.getBody().getQuantifiedVariables()) {
            try {
                args = new Term[] { new Binding(state.env.getBinder("\\forall"), Environment.getBoolType(),
                        boundVars.get(state.translation.variableNames.get(v)), args) };
            } catch (TermException e) {
                e.printStackTrace();
            }
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
        for (Variable v : node.getBody().getQuantifiedVariables()) {
            try {
                args = new Term[] { new Binding(state.env.getBinder("\\exists"), Environment.getBoolType(),
                        boundVars.get(state.translation.variableNames.get(v)), args) };
            } catch (TermException e) {
                e.printStackTrace();
            }
        }

        state.translation.terms.put(node, args[0]);
    }

    @Override
    public void visit(LambdaExpression node) throws ASTVisitException {
        // TODO Auto-generated method stub

        // note: translate to ∀args.rval[args]==λ-expr[args]; or maybe use
        // \\some?
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
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(Attribute node) throws ASTVisitException {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(Trigger node) throws ASTVisitException {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(CoercionExpression node) throws ASTVisitException {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(OrderSpecParent node) throws ASTVisitException {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(OrderSpecification node) throws ASTVisitException {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(MapUpdateExpression node) throws ASTVisitException {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(SpecBlock node) throws ASTVisitException {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(CodeExpression node) throws ASTVisitException {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(SpecReturnStatement node) throws ASTVisitException {
        // TODO Auto-generated method stub

    }
}
