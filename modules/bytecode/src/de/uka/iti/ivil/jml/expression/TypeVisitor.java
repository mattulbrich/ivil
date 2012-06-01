package de.uka.iti.ivil.jml.expression;

import java.util.HashMap;
import java.util.Map;

import org.gjt.jclasslib.structures.ClassFile;
import org.gjt.jclasslib.structures.FieldInfo;
import org.gjt.jclasslib.structures.attributes.CodeAttribute;
import org.gjt.jclasslib.structures.attributes.LocalVariableCommonEntry;
import org.gjt.jclasslib.structures.attributes.LocalVariableTableAttribute;

import de.uka.iti.ivil.jbc.environment.BytecodeCompilerError;
import de.uka.iti.ivil.jbc.util.MethodName;
import de.uka.iti.ivil.jbc.util.ObjectType;
import de.uka.iti.ivil.jml.parser.ast.expr.ArrayAccessExpr;
import de.uka.iti.ivil.jml.parser.ast.expr.DoubleLiteralExpr;
import de.uka.iti.ivil.jml.parser.ast.expr.Expression;
import de.uka.iti.ivil.jml.parser.ast.expr.FieldAccessExpr;
import de.uka.iti.ivil.jml.parser.ast.expr.IntegerLiteralExpr;
import de.uka.iti.ivil.jml.parser.ast.expr.MethodCallExpr;
import de.uka.iti.ivil.jml.parser.ast.expr.NameExpr;
import de.uka.iti.ivil.jml.parser.ast.expr.SuperExpr;
import de.uka.iti.ivil.jml.parser.ast.expr.ThisExpr;
import de.uka.iti.ivil.jml.parser.ast.expr.TypeRelationExpression;
import de.uka.iti.ivil.jml.parser.ast.visitor.VoidVisitorAdapter;

/**
 * handles the typeing of an expression
 * 
 * @author timm.felden@felden.com
 * 
 */
final class TypeVisitor extends VoidVisitorAdapter<Void> {

    private final Translator trans;
    final Map<Expression, ObjectType> types = new HashMap<Expression, ObjectType>();

    TypeVisitor(Translator trans) {
        this.trans = trans;
    }

    @Override
    public void visit(ArrayAccessExpr n, Void arg) {
        n.getName().accept(this, arg);
        ObjectType t = types.get(n.getName());
        types.put(n, ObjectType.createTypeFromSingleTypeDescriptor(t.getJVMType().substring(1)));

        n.getIndex().accept(this, arg);
    }

    @Override
    public void visit(FieldAccessExpr n, Void arg) {
        n.getScope().accept(this, arg);

        ObjectType t = types.get(n.getScope());
        if (t.arrayDepth() > 0) {
            // length field has been accessed
            types.put(n, ObjectType.createTypeFromSingleTypeDescriptor("I"));
        } else {
            try {
                ClassFile cls = trans.resolver.requestClass(t.getBytecodeClassName());
                if (null != cls)
                    for (FieldInfo field : cls.getFields()) {
                        if (field.getName().equals(n.getField())) {
                            types.put(n, ObjectType.createTypeFromSingleTypeDescriptor(field.getDescriptor()));

                            return;
                        }
                    }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void visit(MethodCallExpr n, Void arg) {
        if (null == n.getScope()) {
            if (n.getName().equals("\\old")) {
                n.getArgs().get(0).accept(this, arg);
                types.put(n, types.get(n.getArgs().get(0)));
            } else if (n.getName().equals("\\fresh")) {
                n.getArgs().get(0).accept(this, arg);
                types.put(n, ObjectType.createTypeFromSingleTypeDescriptor("Z"));
            }
        }
    }

    @Override
    public void visit(NameExpr n, Void arg) {

        // check for result
        if (n.getName().equals("\\result")) {
            try {
                MethodName name = MethodName.createFromClassFile(trans.method.getClassFile(), trans.method);

                types.put(n, name.getSignature(trans.method.getClassFile()).getResultType());
            } catch (Exception e) {
                e.printStackTrace();
                // this is bad!
            }
            return;
        }

        // have a look at the local variables
        if (null != trans.method)
            try {
                CodeAttribute code = ((CodeAttribute) trans.method.findAttribute(CodeAttribute.class));
                LocalVariableTableAttribute lvars = (LocalVariableTableAttribute) code
                        .findAttribute(LocalVariableTableAttribute.class);
                for (LocalVariableCommonEntry entry : lvars.getLocalVariableEntries()) {
                    // TODO correct pc: if (entry.getStartPc() == 0)
                    if (n.getName().equals(trans.thisClass.getConstantPoolEntryName(entry.getNameIndex()))) {

                        types.put(
                                n,
                                ObjectType.createTypeFromSingleTypeDescriptor(trans.thisClass.getConstantPoolUtf8Entry(
                                        entry.getDescriptorOrSignatureIndex()).getString()));

                        return;
                    }
                }
            } catch (Exception e) {
                // not a local variable
            }

        // have a look at the fields of this
        try {
            String tmp = trans.resolver.resolveFieldName(trans.thisClass, n.getName());
            if (null != tmp) {
                // note: this can only happen if this. is omitted
                for (FieldInfo field : trans.thisClass.getFields()) {
                    if (field.getName().equals(n.getName())) {
                        types.put(n, ObjectType.createTypeFromSingleTypeDescriptor(field.getDescriptor()));

                        return;
                    }
                }
            }
        } catch (Exception e) {
            // not important, if something happened, its just not a field
        }

        // we dont know
    }

    @Override
    public void visit(SuperExpr n, Void arg) {
        try {
            types.put(n, ObjectType.createTypeFromBytecodeClass(trans.resolver.requestClass(trans.thisClass
                    .getSuperClassName())));
        } catch (Exception e) {
            throw new BytecodeCompilerError(e);
        }
    }

    @Override
    public void visit(ThisExpr n, Void arg) {
        try {
            types.put(n, ObjectType.createTypeFromBytecodeClass(trans.resolver.requestClass(trans.thisClass
                    .getThisClassName())));
        } catch (Exception e) {
            throw new BytecodeCompilerError(e);
        }
    }

    // literals

    @Override
    public void visit(IntegerLiteralExpr n, Void arg) {
        types.put(n, ObjectType.createTypeFromSingleTypeDescriptor("I"));
    }

    @Override
    public void visit(DoubleLiteralExpr n, Void arg) {
        types.put(n, ObjectType.createTypeFromSingleTypeDescriptor("D"));
    }

    @Override
    public void visit(TypeRelationExpression n, Void arg) {
        types.put(n, ObjectType.createTypeFromSingleTypeDescriptor("Z"));
    }
}
