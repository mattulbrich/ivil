//package de.uka.iti.pseudo.environment.boogie;
//
//import de.uka.iti.pseudo.environment.EnvironmentException;
//import de.uka.iti.pseudo.parser.boogie.ASTElement;
//import de.uka.iti.pseudo.parser.boogie.ASTVisitException;
//import de.uka.iti.pseudo.parser.boogie.util.DefaultASTVisitor;
//import de.uka.iti.pseudo.term.TermException;
//
///**
// * translates universal types to ivil types
// * 
// * @author timm.felden@felden.com
// * 
// */
//public final class TypeTranslator extends DefaultASTVisitor {
//    private final EnvironmentCreationState state;
//
//    public TypeTranslator(EnvironmentCreationState state) throws ASTVisitException {
//        this.state = state;
//
//        state.root.visit(this);
//    }
//
//    @Override
//    protected void defaultAction(ASTElement node) throws ASTVisitException {
//        if (state.ivilTypeMap.has(node))
//            return;
//
//        if (null != state.typeMap.get(node)) {
//            try {
//                state.ivilTypeMap.add(node, state.typeMap.get(node).toIvilType(state));
//            } catch (EnvironmentException e1) {
//                e1.printStackTrace();
//                throw new ASTVisitException(e1);
//            } catch (TermException e1) {
//                e1.printStackTrace();
//                throw new ASTVisitException(e1);
//            }
//        } else
//            state.ivilTypeMap.add(node, null);
//
//        for (ASTElement e : node.getChildren())
//            e.visit(this);
//    }
// }
