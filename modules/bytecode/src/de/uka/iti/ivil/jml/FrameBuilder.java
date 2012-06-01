package de.uka.iti.ivil.jml;

import java.util.LinkedList;
import java.util.List;

import org.gjt.jclasslib.structures.ClassFile;

import de.uka.iti.ivil.jbc.environment.BytecodeCompilerException;
import de.uka.iti.ivil.jbc.environment.NameResolver;
import de.uka.iti.ivil.jml.expression.Translator;
import de.uka.iti.ivil.jml.parser.ast.expr.NameExpr;
import de.uka.iti.ivil.jml.parser.ast.spec.AnyFieldExpression;
import de.uka.iti.ivil.jml.parser.ast.spec.ArrayRangeExpression;
import de.uka.iti.ivil.jml.parser.ast.spec.MethodContract;
import de.uka.iti.ivil.jml.parser.ast.spec.MethodContract.Line;
import de.uka.iti.ivil.jml.parser.ast.spec.MethodContract.LineType;
import de.uka.iti.ivil.jml.parser.ast.spec.StoreEverythingExpression;
import de.uka.iti.ivil.jml.parser.ast.spec.StoreRefExpression;

/**
 * small util class that adds pairs to a frame based on a set of store ref
 * expressions.
 * 
 * @author timm.felden@felden.com
 * 
 */
final class FrameBuilder {

    static StringBuilder makeFrame(String name, String thisName, MethodContract c, NameResolver resolver,
            Translator trans, ClassFile cls) throws BytecodeCompilerException {

        List<StoreRefExpression> storeRefs = new LinkedList<StoreRefExpression>();
        for (Line line : c.get(LineType.assignable)) {
            storeRefs.addAll(line.storeRefList);
        }
        return makeFrame(name, thisName, storeRefs, resolver, trans, cls);
    }

    /**
     * Creates a term that fills the frame <i>name</i> with the locations
     * defined in the assignable part of the contract.
     * 
     * @param name
     *            name of the frame. the caller has to ensure the validity
     * @param c
     *            the method contract to be used
     * @return a StringBuilder containing a term of the form ( <o,f> :: name &
     *         <o2, f2> :: name &...)
     * 
     * @note the result does not have a trailing & !
     */
    static StringBuilder makeFrame(String name, String thisName, List<StoreRefExpression> storeRefs,
            NameResolver resolver, Translator trans, ClassFile cls) throws BytecodeCompilerException {

        StringBuilder sb = new StringBuilder();


        boolean isEmpty = true;

        sb.append("(\\forall o; (\\forall f; (pair(o, f) :: ").append(name).append(") = (");
        for (StoreRefExpression expr : storeRefs) {
            if (!isEmpty)
                sb.append(" | ");
            else
                isEmpty = false;

            if (expr instanceof StoreEverythingExpression) {
                sb.append("true");
            } else {
                final String ref = expr.ref() == null ? thisName : trans.translate(expr.ref());

                if (expr.field() instanceof NameExpr) {
                    // pair(ref, field)
                    sb.append("(o=").append(ref).append(" & f=");

                    String fieldName = ((NameExpr) expr.field()).getName();

                    if (null == expr.ref())
                        sb.append(resolver.resolveFieldName(cls, fieldName));
                    else {
                        if (fieldName.equals("\\array_length"))
                            sb.append("$array_length");
                        else
                            sb.append(resolver.resolveFieldName(
                                    resolver.requestClass(trans.type(expr.ref()).getBytecodeClassName()), fieldName));
                    }

                    sb.append(")");

                } else if (expr.field() instanceof AnyFieldExpression) {
                    // ∀ i. pair(ref, $index(i))
                    if (expr.isArray()) {
                        sb.append("(o = ").append(ref).append(" & (\\exists i; f = $array_index(i)))");

                    } else {
                        // ref = r
                        sb.append("(").append(ref).append(" = o)");
                    }
                } else if (expr.field() instanceof ArrayRangeExpression) {
                    // o = ref & ∃ i; ( begin <= i && i < end) & f =
                    // index(i)

                    final String begin, end;
                    begin = trans.translate(((ArrayRangeExpression) expr.field()).begin());
                    end = trans.translate(((ArrayRangeExpression) expr.field()).end());

                    sb.append("(o = ").append(ref).append(" & (\\exists i; ").append(begin).append(" <= i & i < ")
                            .append(end).append(" & (f = $array_index(i))))");
                } else {
                    if (!expr.isArray()) {
                        sb.append("(o = ").append(trans.translate(expr.ref())).append(" & f = ")
                                .append(trans.translate(expr.field())).append(")");
                    } else {
                        final String field = trans.translate(expr.field());
                        sb.append("(o = ").append(ref).append(" & (f = $array_index(").append(field).append(")))");
                    }
                }
            }
        }
        if (isEmpty)
            sb.append("false");

        sb.append(")))");

        return sb;
    }
}
