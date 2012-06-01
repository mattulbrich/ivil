package de.uka.iti.ivil.jml.expression;

import java.util.HashMap;
import java.util.Map;

import de.uka.iti.ivil.jbc.environment.BytecodeCompilerException;
import de.uka.iti.ivil.jbc.util.ObjectType;
import de.uka.iti.ivil.jml.parser.ast.body.Parameter;
import de.uka.iti.ivil.jml.parser.ast.expr.ArrayAccessExpr;
import de.uka.iti.ivil.jml.parser.ast.expr.AssignExpr;
import de.uka.iti.ivil.jml.parser.ast.expr.BinaryExpr;
import de.uka.iti.ivil.jml.parser.ast.expr.BooleanLiteralExpr;
import de.uka.iti.ivil.jml.parser.ast.expr.ConditionalExpr;
import de.uka.iti.ivil.jml.parser.ast.expr.DoubleLiteralExpr;
import de.uka.iti.ivil.jml.parser.ast.expr.Expression;
import de.uka.iti.ivil.jml.parser.ast.expr.FieldAccessExpr;
import de.uka.iti.ivil.jml.parser.ast.expr.InstanceOfExpr;
import de.uka.iti.ivil.jml.parser.ast.expr.IntegerLiteralExpr;
import de.uka.iti.ivil.jml.parser.ast.expr.LongLiteralExpr;
import de.uka.iti.ivil.jml.parser.ast.expr.MethodCallExpr;
import de.uka.iti.ivil.jml.parser.ast.expr.NameExpr;
import de.uka.iti.ivil.jml.parser.ast.expr.NullLiteralExpr;
import de.uka.iti.ivil.jml.parser.ast.expr.QuantificationExpression;
import de.uka.iti.ivil.jml.parser.ast.expr.SuperExpr;
import de.uka.iti.ivil.jml.parser.ast.expr.ThisExpr;
import de.uka.iti.ivil.jml.parser.ast.expr.TypeExpression;
import de.uka.iti.ivil.jml.parser.ast.expr.TypeRelationExpression;
import de.uka.iti.ivil.jml.parser.ast.expr.UnaryExpr;
import de.uka.iti.ivil.jml.parser.ast.visitor.VoidVisitorAdapter;

/**
 * creates a term out of an typed expression
 *
 * @author timm.felden@felden.com
 *
 */
final class TermVisitor extends VoidVisitorAdapter<StringBuilder> {

    private final Translator trans;
    private final Map<Expression, ObjectType> types;
    private boolean insideOld = false;
    private final Map<String, String> boundVars = new HashMap<String, String>();

    public TermVisitor(Translator trans, Map<Expression, ObjectType> types) {
        this.trans = trans;
        this.types = types;
    }

    @Override
    public void visit(ArrayAccessExpr n, StringBuilder sb) {
        appendHeap(sb);
        n.getName().accept(this, sb);
        sb.append(", $array_index(");
        n.getIndex().accept(this, sb);
        sb.append(")] as ");
        sb.append(types.get(n).getBaseType());
    }

    @Override
    public void visit(AssignExpr n, StringBuilder sb) {
        throw new IllegalArgumentException("Assignments are not allowed in specifications!");
    }

    @Override
    public void visit(BinaryExpr n, StringBuilder arg) {

        switch (n.getOperator()) {
        case or:
            arg.append("$or");
            break;
        case and:
            arg.append("$and");
            break;
        case binOr:
            arg.append("$ior");
            break;
        case binAnd:
            arg.append("$iand");
            break;
        case xor:
            arg.append("$ixor");
            break;
        case equals:
            arg.append("$eq");
            break;
        case notEquals:
            arg.append("!$eq");
            break;
        case less:
            arg.append("$order_less");
            break;
        case lessEquals:
            arg.append("$order_less_equal");
            break;
        case greater:
            arg.append("$order_greater");
            break;
        case greaterEquals:
            arg.append("$order_greater_equal");
            break;
        case lShift:
            arg.append("$ishl");
            break;
        case rSignedShift:
            arg.append("$ishr");
            break;
        case rUnsignedShift:
            arg.append("$iushr");
            break;
        case plus:
            arg.append("$" + types.get(n.getLeft()).getBaseType().substring(0, 1) + "add");
            break;
        case minus:
            arg.append("$" + types.get(n.getLeft()).getBaseType().substring(0, 1) + "sub");
            break;
        case times:
            arg.append("$" + types.get(n.getLeft()).getBaseType().substring(0, 1) + "mul");
            break;
        case divide:
            arg.append("$" + types.get(n.getLeft()).getBaseType().substring(0, 1) + "div");
            break;
        case remainder:
            arg.append("$" + types.get(n.getLeft()).getBaseType().substring(0, 1) + "rem");
            break;
        case equivalent:
            arg.append("$equiv");
            break;
        case notEquivalent:
            arg.append("!$equiv");
            break;
        case implies:
            arg.append("$impl");
            break;
        case explies:
            arg.append("$explies");
            break;
        default:
            throw new RuntimeException("unsupported operation");
        }

        arg.append("(");
        n.getLeft().accept(this, arg);
        arg.append(", ");
        n.getRight().accept(this, arg);
        arg.append(")");
    }

    @Override
    public void visit(BooleanLiteralExpr n, StringBuilder sb) {
        if (n.getValue()) {
            sb.append("true");
        } else {
            sb.append("false");
        }
    }

    @Override
    public void visit(ConditionalExpr n, StringBuilder arg) {

        arg.append("cond(");
        n.getCondition().accept(this, arg);
        arg.append(", ");
        n.getThenExpr().accept(this, arg);
        arg.append(", ");
        n.getElseExpr().accept(this, arg);
        arg.append(")");
    }

    @Override
    public void visit(DoubleLiteralExpr n, StringBuilder sb) {
        sb.append(trans.po.getFloatLiteral(Double.parseDouble(n.getValue())));
    }

    @Override
    public void visit(FieldAccessExpr n, StringBuilder sb) {
        appendHeap(sb);
        n.getScope().accept(this, sb);

        ObjectType t = types.get(n.getScope());

        if (0 != t.arrayDepth()) {
            assert n.getField().equals("length");
            // there is only the length field!
            sb.append(", $array_length]");
        } else {
            try {
                sb.append(", ")
                        .append(trans.resolver.resolveFieldName(trans.resolver.requestClass(t.getBytecodeClassName()),
                                n.getField())).append("]");
            } catch (BytecodeCompilerException e) {
                e.printStackTrace();
            }
        }
        sb.append(" as ").append(types.get(n).getBaseType());
    }

    @Override
    public void visit(InstanceOfExpr n, StringBuilder sb) {
        String type = ObjectType.createTypeFromJavaTypeName(n.getType().toString()).getIvilTypeTerm();

        sb.append("instanceof(");
        n.getExpr().accept(this, sb);
        sb.append(", ").append(type).append(")");
    }

    @Override
    public void visit(IntegerLiteralExpr n, StringBuilder sb) {
        sb.append(n.getValue());
    }

    @Override
    public void visit(LongLiteralExpr n, StringBuilder sb) {
        sb.append(n.getValue());
    }

    @Override
    public void visit(MethodCallExpr n, StringBuilder sb) {
        if (null == n.getScope()) {
            if (n.getName().equals("\\old")) {
                if (insideOld) {
                    for (Expression e : n.getArgs()) {
                        e.accept(this, sb);
                    }
                } else {
                    insideOld = true;
                    for (Expression e : n.getArgs()) {
                        e.accept(this, sb);
                    }
                    insideOld = false;
                }
                return;
            } else if (n.getName().equals("\\fresh")) {
                // \forall x : x = <arg> -> ($heap[x, $created] &&
                // !$old_heap[x, $created])
                sb.append("(\\forall x; x = ");
                n.getArgs().get(0).accept(this, sb);
                sb.append(" -> ( $heap[x, $created] & !$old_heap[x, $created]))");

            } else {

                // the method is called on some sort of object
            }
        } else {
            // the method is called on this object
        }
    }

    @Override
    public void visit(NameExpr n, StringBuilder sb) {

        // check for result
        if (n.getName().equals("\\result")) {
            sb.append(trans.resultName);
            sb.append(" as ").append(types.get(n).getBaseType());
            return;
        }

        // have a look at the local variables
        if (null != trans.argumentNames) {
            for (int i = 0; i < trans.argumentNames.length && i < trans.argumentTranslatedNames.length; i++) {
                if (n.getName().equals(trans.argumentNames[i])) {
                    sb.append(trans.argumentTranslatedNames[i]);
                    return;
                }
            }
        }

        // have a look at the fields of this
        try {
            String tmp = trans.resolver.resolveFieldName(trans.thisClass, n.getName());
            if (null != tmp) {
                // note: this can only happen if this. is omitted
                appendHeap(sb);
                sb.append(trans.thisName).append(", ").append(tmp).append("]");
                return;
            }
        } catch (Exception e) {
            // not important, if something happened, its just not a field
        }

        // have a look at bound variables
        if (boundVars.containsKey(n.getName())) {
            sb.append(boundVars.get(n.getName()));
            return;
        }

        // what the hell should that be?
        sb.append("☢unkonwn name: " + n.getName() + "☢");
    }

    @Override
    public void visit(NullLiteralExpr n, StringBuilder sb) {
        sb.append("$null");
    }

    @Override
    public void visit(SuperExpr n, StringBuilder sb) {
        sb.append(trans.thisName);
    }

    @Override
    public void visit(ThisExpr n, StringBuilder sb) {
        sb.append(trans.thisName);
    }

    @Override
    public void visit(UnaryExpr n, StringBuilder sb) {
        switch (n.getOperator()) {
        case positive: // +
            n.getExpr().accept(this, sb);
            break;
        case negative: // -
            if ("int".equals(types.get(n.getExpr()).getBaseType())) {
                sb.append("$ineg(");
            } else {
                sb.append("$dneg(");
            }
            n.getExpr().accept(this, sb);
            sb.append(")");
            break;

        case preIncrement: // ++
            throw new RuntimeException("unsupported operation");
            // break;
        case preDecrement: // --
            throw new RuntimeException("unsupported operation");
            // break;
        case not: // !
            sb.append("$not(");
            n.getExpr().accept(this, sb);
            sb.append(")");
            break;
        case inverse: // ~
            sb.append("$iinverse(");
            n.getExpr().accept(this, sb);
            sb.append(")");
            break;
        case posIncrement: // ++
            throw new RuntimeException("unsupported operation");
            // break;
        case posDecrement: // --
            throw new RuntimeException("unsupported operation");
            // break;
        }
    }

    @Override
    public void visit(QuantificationExpression n, StringBuilder arg) {
        String quant = n.getQuantifier();
        for (Parameter p : n.getTargets()) {
            final String name = p.getId().getName();
            arg.append("(").append(quant).append(" SPEC_B_").append(name).append(";");
            boundVars.put(name, "SPEC_B_" + name);
        }

        if (null != n.getRestriction()) {
            // TODO was a java7 switch string
            if(quant.equals("\\forall")) {
                arg.append("(");
                n.getRestriction().accept(this, arg);
                arg.append(") -> ");

            } else if(quant.equals("\\exists")) {
                arg.append("(");
                n.getRestriction().accept(this, arg);
                arg.append(") & ");
            } else {
                throw new IllegalArgumentException("unknown restricted quantifier @" + n.getBeginLine() + ":"
                        + n.getBeginColumn() + " :: " + n.toString());
            }
        }

        arg.append("(");
        n.getExpression().accept(this, arg);
        arg.append(")");

        for (Parameter p : n.getTargets()) {
            arg.append(")");
            // TODO this is a wrong behavior if a bound var shadows another
            boundVars.remove(p.getId().getName());
        }
    }

    private void appendHeap(StringBuilder sb) {
        sb.append(insideOld ? "$old_heap[" : "$heap[");
    }

    @Override
    public void visit(TypeExpression n, StringBuilder sb) {
        String operator = n.getOperator();
        if(operator.equals("\\type")) {
            sb.append(ObjectType.createTypeFromJavaTypeName(n.getType().toString()).getIvilTypeTerm());
            return;
        }

        if(operator.equals( "\\elemtype")) {
            // some t; TF_array(t) = exactTypeOf(expr)
            sb.append("(\\some t; TF_array(t) = exactTypeOf(");
            n.getExpr().accept(this, sb);
            sb.append("))");
            return;
        }

        if(operator.equals( "\\typeof")) {
            // exactTypeOf(expr)
            sb.append("exactTypeOf(");
            n.getExpr().accept(this, sb);
            sb.append(")");
            return;
        }

        throw new IllegalArgumentException("illegal Typeexpression operator: " + operator);
    }

    @Override
    public void visit(TypeRelationExpression n, StringBuilder sb) {
        if (n.isEquality()) {
            sb.append("$eq(");
        } else {
            sb.append("superType(");
        }

        n.getLeft().accept(this, sb);
        sb.append(", ");
        n.getRight().accept(this, sb);
        sb.append(")");
    }
}
