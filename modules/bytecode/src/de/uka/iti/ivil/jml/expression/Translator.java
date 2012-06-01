package de.uka.iti.ivil.jml.expression;

import org.gjt.jclasslib.structures.ClassFile;
import org.gjt.jclasslib.structures.MethodInfo;

import de.uka.iti.ivil.jbc.environment.ConcreteProofObligation;
import de.uka.iti.ivil.jbc.environment.NameResolver;
import de.uka.iti.ivil.jbc.util.ObjectType;
import de.uka.iti.ivil.jml.parser.ast.expr.Expression;

final public class Translator {

    final ClassFile thisClass;
    final NameResolver resolver;
    final MethodInfo method;
    final String resultName, thisName;
    final String[] argumentNames, argumentTranslatedNames;

    final ConcreteProofObligation<?> po;

    /**
     * This constructor is most useful if translating expressions that appear
     * inside a class body but not inside a method.
     */
    public Translator(ClassFile cls, ConcreteProofObligation<?> po, String thisName) {
        this(cls, null, po, null, null, null, thisName);
    }

    /**
     * This constructor is most useful if translating expressions that appear
     * inside the context of a method.
     */
    public Translator(ClassFile cls, MethodInfo method, ConcreteProofObligation<?> po, String[] localVariable,
            String[] localVariableTranslatedNames, String resultName, String thisName) {
        this.thisClass = cls;
        this.method = method;
        this.resolver = po.resolver;
        this.argumentNames = localVariable;
        this.argumentTranslatedNames = localVariableTranslatedNames;
        this.resultName = resultName;
        this.thisName = thisName;
        this.po = po;
    }

    /**
     * get only the class type. this is slightly faster, then a full translation
     */
    public ObjectType type(Expression expr) {
        TypeVisitor v = new TypeVisitor(this);
        expr.accept(v, null);
        return v.types.get(expr);
    }

    public String translate(Expression expr) {
        TypeVisitor v = new TypeVisitor(this);
        expr.accept(v, null);

        TermVisitor t = new TermVisitor(this, v.types);
        StringBuilder term = new StringBuilder();
        expr.accept(t, term);

        return term.toString();
    }

}
