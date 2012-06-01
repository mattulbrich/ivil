package de.uka.iti.ivil.jbc.util;

import sun.reflect.generics.tree.ArrayTypeSignature;
import sun.reflect.generics.tree.BooleanSignature;
import sun.reflect.generics.tree.BottomSignature;
import sun.reflect.generics.tree.ByteSignature;
import sun.reflect.generics.tree.CharSignature;
import sun.reflect.generics.tree.ClassTypeSignature;
import sun.reflect.generics.tree.DoubleSignature;
import sun.reflect.generics.tree.FloatSignature;
import sun.reflect.generics.tree.FormalTypeParameter;
import sun.reflect.generics.tree.IntSignature;
import sun.reflect.generics.tree.LongSignature;
import sun.reflect.generics.tree.ShortSignature;
import sun.reflect.generics.tree.SimpleClassTypeSignature;
import sun.reflect.generics.tree.TypeVariableSignature;
import sun.reflect.generics.tree.VoidDescriptor;
import sun.reflect.generics.tree.Wildcard;
import sun.reflect.generics.visitor.TypeTreeVisitor;

/**
 * TypeTreeVisitor with empty method bodies as implementation. Useful if one is
 * to implement only few Signature Types.
 * 
 * @author timm.felden@felden.com
 */
public class EmptyTypeTreeVisitor<T> implements TypeTreeVisitor<T> {

    @Override
    public T getResult() {
        return null;
    }

    @Override
    public void visitFormalTypeParameter(FormalTypeParameter paramFormalTypeParameter) {
    }

    @Override
    public void visitClassTypeSignature(ClassTypeSignature paramClassTypeSignature) {
    }

    @Override
    public void visitArrayTypeSignature(ArrayTypeSignature paramArrayTypeSignature) {
    }

    @Override
    public void visitTypeVariableSignature(TypeVariableSignature paramTypeVariableSignature) {
    }

    @Override
    public void visitWildcard(Wildcard paramWildcard) {
    }

    @Override
    public void visitSimpleClassTypeSignature(SimpleClassTypeSignature paramSimpleClassTypeSignature) {
    }

    @Override
    public void visitBottomSignature(BottomSignature paramBottomSignature) {
    }

    @Override
    public void visitByteSignature(ByteSignature paramByteSignature) {
    }

    @Override
    public void visitBooleanSignature(BooleanSignature paramBooleanSignature) {
    }

    @Override
    public void visitShortSignature(ShortSignature paramShortSignature) {
    }

    @Override
    public void visitCharSignature(CharSignature paramCharSignature) {
    }

    @Override
    public void visitIntSignature(IntSignature paramIntSignature) {
    }

    @Override
    public void visitLongSignature(LongSignature paramLongSignature) {
    }

    @Override
    public void visitFloatSignature(FloatSignature paramFloatSignature) {
    }

    @Override
    public void visitDoubleSignature(DoubleSignature paramDoubleSignature) {
    }

    @Override
    public void visitVoidDescriptor(VoidDescriptor paramVoidDescriptor) {
    }
}
