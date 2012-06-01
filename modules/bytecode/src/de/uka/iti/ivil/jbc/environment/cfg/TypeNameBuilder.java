//package de.uka.iti.ivil.jbc.environment.cfg;
//
//import java.util.HashMap;
//import java.util.Map;
//
//import org.gjt.jclasslib.structures.ClassFile;
//import org.gjt.jclasslib.structures.InvalidByteCodeException;
//
//import sun.reflect.generics.tree.ArrayTypeSignature;
//import sun.reflect.generics.tree.BooleanSignature;
//import sun.reflect.generics.tree.BottomSignature;
//import sun.reflect.generics.tree.ByteSignature;
//import sun.reflect.generics.tree.CharSignature;
//import sun.reflect.generics.tree.ClassTypeSignature;
//import sun.reflect.generics.tree.DoubleSignature;
//import sun.reflect.generics.tree.FloatSignature;
//import sun.reflect.generics.tree.FormalTypeParameter;
//import sun.reflect.generics.tree.IntSignature;
//import sun.reflect.generics.tree.LongSignature;
//import sun.reflect.generics.tree.ShortSignature;
//import sun.reflect.generics.tree.SimpleClassTypeSignature;
//import sun.reflect.generics.tree.TypeVariableSignature;
//import sun.reflect.generics.tree.VoidDescriptor;
//import sun.reflect.generics.tree.Wildcard;
//import sun.reflect.generics.visitor.TypeTreeVisitor;
//import de.uka.iti.ivil.jbc.environment.ByteCodeCompilerException;
//import de.uka.iti.ivil.jbc.environment.NameResolver;
//
///**
// * Can visit a generic type tree and will return a type term.
// * 
// * @author timm.felden@felden.com
// * 
// * @deprecated currently out of sync to the rest of the project; might need
// *             rewrite
// */
//@Deprecated
//public final class TypeNameBuilder implements TypeTreeVisitor<String> {
//    private final StringBuilder rval = new StringBuilder();
//    private final NameResolver resolver;
//    private final Map<String, String> typeArgumentInstantiations;
//    private final ClassFile thisClass;
//
//    public TypeNameBuilder(NameResolver resolver) {
//        this.resolver = resolver;
//        this.typeArgumentInstantiations = new HashMap<String, String>();
//        thisClass = null;
//    }
//
//    /**
//     * In case of fields and methods, some instantiations of type variables come
//     * from outside. Note that this method will modify the given map.
//     */
//    public TypeNameBuilder(NameResolver resolver, Map<String, String> typeArgumentInstantiations, ClassFile thisClass) {
//        this.resolver = resolver;
//        this.typeArgumentInstantiations = typeArgumentInstantiations;
//        this.thisClass = thisClass;
//    }
//
//    @Override
//    public String getResult() {
//        return rval.toString();
//    }
//
//    @Override
//    public void visitFormalTypeParameter(FormalTypeParameter paramFormalTypeParameter) {
//        // TODO Auto-generated method stub
//
//    }
//
//    @Override
//    public void visitClassTypeSignature(ClassTypeSignature param) {
//        StringBuilder name = new StringBuilder();
//        for (int i = 0; i < param.getPath().size(); i++) {
//            name.append(param.getPath().get(i).getName());
//            if (i != param.getPath().size() - 1) {
//                name.append("/");
//            } else {
//                final SimpleClassTypeSignature node = param.getPath().get(i);
//                try {
//                    rval.append(resolver.getTypeNameFromHierarchy(name.toString()));
//                } catch (ByteCodeCompilerException e) {
//                    // we are in deep trouble if
//                    // that happens
//                    e.printStackTrace();
//                }
//
//                if (node.getTypeArguments().length > 0) {
//                    rval.append("(");
//                    for (int j = 0; j < node.getTypeArguments().length; j++) {
//                        if (j != 0)
//                            rval.append(", ");
//                        node.getTypeArguments()[j].accept(this);
//                    }
//                    rval.append(")");
//                }
//            }
//        }
//    }
//
//    @Override
//    public void visitArrayTypeSignature(ArrayTypeSignature paramArrayTypeSignature) {
//        // TODO Auto-generated method stub
//
//    }
//
//    @Override
//    public void visitTypeVariableSignature(TypeVariableSignature param) {
//        if (typeArgumentInstantiations.containsKey(param.getIdentifier()))
//            rval.append(typeArgumentInstantiations.get(param.getIdentifier()));
//        else if (null != thisClass) {
//            // TODO
//            // try {
//            // rval.append("TA_" + param.getIdentifier() + "__" +
//            // EscapeType.classNameToTypeName(thisClass.getThisClassName()));
//            // } catch (InvalidByteCodeException e) {
//            // // wont happen
//            // e.printStackTrace();
//            // }
//        } else
//            rval.append("☢ tvar " + param.getIdentifier() + " not found ☢");
//    }
//
//    @Override
//    public void visitWildcard(Wildcard paramWildcard) {
//        // TODO Auto-generated method stub
//
//    }
//
//    @Override
//    public void visitSimpleClassTypeSignature(SimpleClassTypeSignature param) {
//
//        try {
//            rval.append(resolver.getTypeNameFromHierarchy(param.getName()));
//        } catch (ByteCodeCompilerException e) {
//            // we are in deep trouble if that
//            // happens
//            e.printStackTrace();
//        }
//
//        if (param.getTypeArguments().length > 0) {
//            rval.append("(");
//            for (int i = 0; i < param.getTypeArguments().length; i++) {
//                if (i != 0)
//                    rval.append(", ");
//                param.getTypeArguments()[i].accept(this);
//            }
//            rval.append(")");
//        }
//    }
//
//    @Override
//    public void visitBottomSignature(BottomSignature paramBottomSignature) {
//        // TODO Auto-generated method stub
//
//    }
//
//    @Override
//    public void visitByteSignature(ByteSignature paramByteSignature) {
//        // TODO Auto-generated method stub
//
//    }
//
//    @Override
//    public void visitBooleanSignature(BooleanSignature paramBooleanSignature) {
//        // TODO Auto-generated method stub
//
//    }
//
//    @Override
//    public void visitShortSignature(ShortSignature paramShortSignature) {
//        // TODO Auto-generated method stub
//
//    }
//
//    @Override
//    public void visitCharSignature(CharSignature paramCharSignature) {
//        // TODO Auto-generated method stub
//
//    }
//
//    @Override
//    public void visitIntSignature(IntSignature paramIntSignature) {
//        // TODO Auto-generated method stub
//
//    }
//
//    @Override
//    public void visitLongSignature(LongSignature paramLongSignature) {
//        // TODO Auto-generated method stub
//
//    }
//
//    @Override
//    public void visitFloatSignature(FloatSignature paramFloatSignature) {
//        // TODO Auto-generated method stub
//
//    }
//
//    @Override
//    public void visitDoubleSignature(DoubleSignature paramDoubleSignature) {
//        // TODO Auto-generated method stub
//
//    }
//
//    @Override
//    public void visitVoidDescriptor(VoidDescriptor paramVoidDescriptor) {
//        // TODO Auto-generated method stub
//
//    }
// }