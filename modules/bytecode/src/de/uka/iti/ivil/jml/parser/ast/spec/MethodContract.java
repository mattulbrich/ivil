package de.uka.iti.ivil.jml.parser.ast.spec;

import java.util.ArrayList;
import java.util.List;

import de.uka.iti.ivil.jml.parser.ast.Node;
import de.uka.iti.ivil.jml.parser.ast.expr.Expression;
import de.uka.iti.ivil.jml.parser.ast.type.ReferenceType;
import de.uka.iti.ivil.jml.parser.ast.visitor.GenericVisitor;
import de.uka.iti.ivil.jml.parser.ast.visitor.VoidVisitor;

public class MethodContract extends Node {

    /**
     * A single contract line. The fields are filled depending on the type.
     * 
     * @author timm.felden@felden.com
     * 
     */
    public static class Line {
        public final LineType type;
        public final Expression expr;
        public final ReferenceType refType;
        public final List<StoreRefExpression> storeRefList;

        public Line(LineType type, Expression expr) {
            this.type = type;
            this.expr = expr;

            refType = null;
            storeRefList = null;
        }

        public Line(LineType type, ReferenceType refType, Expression expr) {
            this.type = type;
            this.expr = expr;
            this.refType = refType;

            storeRefList = null;
        }

        public Line(LineType type, List<StoreRefExpression> storeRefList) {
            this.type = type;
            this.storeRefList = storeRefList;

            this.expr = null;
            refType = null;
        }
    }

    /**
     * The type of a contract line
     * 
     * @author timm.felden@felden.com
     * 
     */
    public enum LineType {
        requires, ensures, measured_by, signals, assignable, diverges
    }

    @SuppressWarnings("unchecked")
    private final List<Line> lines[] = new List[LineType.values().length];

    public MethodContract(List<Line> lines) {
        for (int i = 0; i < this.lines.length; i++)
            this.lines[i] = new ArrayList<MethodContract.Line>();
        for (Line l : lines) {
            this.lines[l.type.ordinal()].add(l);
        }
    }

    @Override
    public <R, A> R accept(GenericVisitor<R, A> v, A arg) {
        return v.visit(this, arg);
    }

    @Override
    public <A> void accept(VoidVisitor<A> v, A arg) {
        v.visit(this, arg);
    }

    public List<Line> get(LineType lt) {
        return lines[lt.ordinal()];
    }

}
