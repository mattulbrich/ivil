package de.uka.iti.pseudo.parser.boogie.environment;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import de.uka.iti.pseudo.environment.Axiom;
import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.environment.EnvironmentException;
import de.uka.iti.pseudo.environment.Function;
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
import de.uka.iti.pseudo.term.TypeVariableBinding;
import de.uka.iti.pseudo.term.statement.AssertStatement;
import de.uka.iti.pseudo.term.statement.AssumeStatement;
import de.uka.iti.pseudo.term.statement.SkipStatement;
import de.uka.iti.pseudo.term.statement.Statement;

public final class ProgramMaker extends DefaultASTVisitor {

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

    // variables can get a desired name instead of the default ones
    private String desiredName = null;

    private Map<String, de.uka.iti.pseudo.term.Variable> boundVars = new HashMap<String, de.uka.iti.pseudo.term.Variable>();

    /**
     * This String is nonnull iff breaks are meaningfull. In that case, they
     * will contain the annotation of the label, which has to be jumped to on
     * breaks.
     */
    private String breakLabel;

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
    public void visit(ConstantDeclaration node) throws ASTVisitException {
        // TODO move unique to variables
        // TODO implement extends
        defaultAction(node);
    }

    @Override
    public void visit(Variable node) throws ASTVisitException {

        // TODO where clause?

        Type[] arguments = new Type[0];

        // names are somesort of magic, if no other name is desired, the name
        // will be var_<line>_<colon>_name

        String name = null != desiredName ? desiredName : "var" + node.getLocation().replace(':', '_') + "__"
                + node.getName();
        desiredName = null;

        state.translation.variableNames.put(node, name);

        // quantified variables differ from ordinary variables, as they aren't
        // functions
        if (node.getParent() instanceof QuantifierBody || node.getParent() instanceof FunctionDeclaration) {
            boundVars.put(name, new de.uka.iti.pseudo.term.Variable(name, state.ivilTypeMap.get(node)));

        } else {
            try {
                if (null == state.env.getFunction(name))
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
                for (Variable v : node.getInParameters()) {
                    args = new Term[] { new Binding(state.env.getBinder("\\forall"), Environment.getBoolType(),
                            boundVars.get(state.translation.variableNames.get(v)), args) };
                }

                // FIXME polymorphic functions seem not to work as expected
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

        for (ASTElement e : node.getSpecification())
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
            statements.preAnnotations.add("");
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
            statements.bodyStatements.add(new SkipStatement(node.getLocationToken().beginLine, new Term[0]));

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
            statements.bodyStatements.add(new SkipStatement(node.getLocationToken().beginLine, new Term[0]));
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
            statements.bodyStatements.add(new SkipStatement(node.getLocationToken().beginLine, new Term[0]));
            statements.bodyAnnotations.add("$goto;" + labelThen + ";" + labelElse);

            // then assumes condition to be true
            {
                statements.bodyStatements.add(new SkipStatement(node.getLocationToken().beginLine, new Term[0]));
                statements.bodyAnnotations.add("$label:" + labelThen);

                statements.bodyStatements.add(new AssumeStatement(node.getLocationToken().beginLine,
                        state.translation.terms.get(node.getGuard())));
                statements.bodyAnnotations.add(null);

                for (de.uka.iti.pseudo.parser.boogie.ast.Statement s : node.getThenBlock()) {
                    s.visit(this);
                }
                statements.bodyStatements.add(new SkipStatement(node.getLocationToken().beginLine, new Term[0]));
                statements.bodyAnnotations.add("$goto;" + labelEnd);
            }

            // else assumes condition to be false
            {
                statements.bodyStatements.add(new SkipStatement(node.getLocationToken().beginLine, new Term[0]));
                statements.bodyAnnotations.add("$label:" + labelElse);

                statements.bodyStatements.add(new AssumeStatement(node.getLocationToken().beginLine, new Application(
                        state.env.getFunction("$not"), Environment.getBoolType(), new Term[] { state.translation.terms
                                .get(node.getGuard()) })));
                statements.bodyAnnotations.add(null);

                if (node.getElseBlock() != null) {

                    for (de.uka.iti.pseudo.parser.boogie.ast.Statement s : node.getElseBlock()) {
                        s.visit(this);
                    }
                    statements.bodyStatements.add(new SkipStatement(node.getLocationToken().beginLine, new Term[0]));
                    statements.bodyAnnotations.add("$goto;" + labelEnd);
                }
            }

            // add end label
            statements.bodyStatements.add(new SkipStatement(node.getLocationToken().beginLine, new Term[0]));
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

            statements.bodyStatements.add(new SkipStatement(node.getLocationToken().beginLine, new Term[0]));
            statements.bodyAnnotations.add("$label:" + labelBegin);

            // loopinvariant
            if (node.getInvariants().size() != 0) {
                Term[] args = new Term[node.getInvariants().size()];
                for (int i = 0; i < node.getInvariants().size(); i++) {
                    LoopInvariant inv = node.getInvariants().get(i);
                    inv.visit(this);
                    args[i] = state.translation.terms.get(inv.getExpression());

                }

                // TODO treatment of free
                statements.bodyStatements.add(new SkipStatement(node.getLocationToken().beginLine, args));
                statements.bodyAnnotations.add(null);
            }

            statements.bodyStatements.add(new SkipStatement(node.getLocationToken().beginLine, new Term[0]));
            statements.bodyAnnotations.add("$goto;" + labelBody + ";" + labelEnd);

            // loop body
            {
                statements.bodyStatements.add(new SkipStatement(node.getLocationToken().beginLine, new Term[0]));
                statements.bodyAnnotations.add("$label:" + labelBody);

                statements.bodyStatements.add(new AssumeStatement(node.getLocationToken().beginLine,
                        state.translation.terms.get(node.getGuard())));
                statements.bodyAnnotations.add(null);

                for (de.uka.iti.pseudo.parser.boogie.ast.Statement s : node.getBody()) {
                    s.visit(this);
                }
                statements.bodyStatements.add(new SkipStatement(node.getLocationToken().beginLine, new Term[0]));
                statements.bodyAnnotations.add("$goto;" + labelBegin);
            }

            // end of the loop
            statements.bodyStatements.add(new SkipStatement(node.getLocationToken().beginLine, new Term[0]));
            statements.bodyAnnotations.add("$label:" + labelEnd);

            statements.bodyStatements.add(new AssumeStatement(node.getLocationToken().beginLine, new Application(
                    state.env.getFunction("$not"), Environment.getBoolType(), new Term[] { state.translation.terms
                            .get(node.getGuard()) })));
            statements.bodyAnnotations.add(null);

            statements.bodyStatements.add(new SkipStatement(node.getLocationToken().beginLine, new Term[0]));
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
            statements.bodyStatements.add(new SkipStatement(node.getLocationToken().beginLine, new Term[0]));

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
                Variable decl = state.names.findVariable(name, node);

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
        try {
            statements.bodyStatements.add(new SkipStatement(node.getLocationToken().beginLine, new Term[0]));
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
            statements.bodyStatements.add(new de.uka.iti.pseudo.term.statement.AssignmentStatement(node
                    .getLocationToken().beginLine, state.translation.terms.get(node.getTarget()),
                    state.translation.terms.get(node.getNewValue())));

            statements.bodyAnnotations.add(null);

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
        defaultAction(node);

        Term[] args = new Term[2];

        for (int i = 0; i < 2; i++)
            args[i] = state.translation.terms.get(node.getOperands().get(i));

        try {
            state.translation.terms.put(node,
                    new Application(state.env.getFunction("$bv_concat"), state.env.mkType("bitvector"),
                    args));
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
                    new Application(state.env.getFunction("$bv_select"), state.env.mkType("bitvector"),
                            new Term[] {
                                    state.translation.terms.get(node.getTarget()),
                                    new Application(state.env.getNumberLiteral(node.getFirst()), Environment
                                            .getIntType()),
                                    new Application(state.env.getNumberLiteral(node.getLast()), Environment
                                            .getIntType()) }));

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
        // TODO Auto-generated method stub
        
        // FIXME maps werden automatisch unifiziert, man kann die also einfach
        // als map(wicket a, a) hinschreiben

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
        // create a new variable old_<location> insert an assignment statement
        // into the precondition code and return the variable as resulting term
        // here

        defaultAction(node);

        try {
            Function oldval = new Function("old_" + node.getLocation().replace(':', '_'), state.ivilTypeMap.get(node),
                    new Type[0], false, true, node);
            state.env.addFunction(oldval);

            Application old = new Application(oldval, state.ivilTypeMap.get(node));

            statements.preStatements.add(new de.uka.iti.pseudo.term.statement.AssignmentStatement(node
                    .getLocationToken().beginLine, old, state.translation.terms.get(node.getOperands().get(0))));

            statements.preAnnotations.add(null);

            state.translation.terms.put(node, old);

        } catch (EnvironmentException e) {
            e.printStackTrace();
            throw new ASTVisitException(e);
        } catch (TermException e) {
            e.printStackTrace();
            throw new ASTVisitException(e);
        }
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
        // anm.: die funktionsweise hiervon hängt sehr stark von der
        // implementierung der maps ab

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
