package de.uka.iti.ivil.jbc.util.parsers.ast.type;

/**
 * A visitor to visit types. Stolen shamelessly from the JML parser.
 * 
 * @author timm.felden@felden.com
 * 
 */
public interface TypeVisitor<A> {

    void visit(ClassOrInterfaceType type, A arg);

    void visit(PrimitiveType type, A arg);

    void visit(ReferenceType type, A arg);

    void visit(VoidType type, A arg);

    void visit(WildcardType type, A arg);
}
