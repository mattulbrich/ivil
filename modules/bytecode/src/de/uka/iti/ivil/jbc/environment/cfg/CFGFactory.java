package de.uka.iti.ivil.jbc.environment.cfg;

import java.util.LinkedList;

import org.gjt.jclasslib.structures.MethodInfo;

import de.uka.iti.ivil.jbc.environment.BytecodeCompilerException;
import de.uka.iti.ivil.jbc.environment.ConcreteProofObligation;
import de.uka.iti.ivil.jbc.environment.cfg.CFGTypeConstraintFactory.Constraint;

/**
 * Builds a control flow graph solving all typing constraints.
 * 
 * @author timm.felden@felden.com
 * 
 */
public final class CFGFactory {
    static class Pair {
        final Type l, r;

        Pair(Type left, Type right) {
            l = left;
            r = right;
        }
    }

    private final ControlFlowGraph cfg;
    private final TypeFrame[] frames;
    private LinkedList<Constraint> typeConstraints;

    private CFGFactory(ControlFlowGraph controlFlowGraph) {
        cfg = controlFlowGraph;
        frames = new TypeFrame[cfg.instructions.length];
    }

    public static ControlFlowGraph build(MethodInfo method, ConcreteProofObligation<?> po)
            throws BytecodeCompilerException {
        CFGFactory factory = new CFGFactory(new ControlFlowGraph(method, po));

        factory.build();

        return factory.cfg;
    }

    /**
     * Builds the control flow graph out of the code of the supplied method and
     * solves the typeing equations.
     */
    private void build() throws BytecodeCompilerException {

        // build control flow graph
        new CFGBlocksFactory(cfg);
        
        // add opcode based type constraints
        typeConstraints = CFGTypeConstraintFactory.build(cfg, frames);

        // simple but horribly incorrect solution:
        for (Constraint c : typeConstraints) {
            c.target = c.target.mergeWith(c.source);
            if (c.isSameType)
                c.source.copy(c.target);
        }

        // // add constraints for nodes with multiple parents
        // for (int i = 0; i < cfg.instructions.length; i++) {
        // if (!cfg.blocks.getBlock(i).isExceptionHandler &&
        // cfg.blocks.getBlock(i).parents.size() > 1) {
        // // constraint all parents to have the same type as the first one
        // ArrayList<Block> parents = cfg.blocks.getBlock(i).parents;
        // Type[] master, other;
        // master = cfg.stack.getLayout(parents.get(0).index);
        // for (int j = 1; j < parents.size(); j++) {
        // other = cfg.stack.getLayout(parents.get(j).index);
        //
        // for (int k = 0; k < master.length; k++)
        // addConstraint(other[k], master[k]);
        // }
        // }
        // }
        //
        // // solve type constraints; constraint solving has to converge after
        // // size*size tries
        // final int size = typeConstraints.size();
        // for (int i = 0; !typeConstraints.isEmpty() && i < size * size; i++) {
        // Pair p = typeConstraints.removeFirst();
        // Type l = p.l, r = p.r;
        //
        // if (l.type() == IntermediateType.intOrBool && r.type() !=
        // IntermediateType.intOrBool) {
        // l.setType(r.type());
        // } else if (l.type() != IntermediateType.intOrBool && r.type() ==
        // IntermediateType.intOrBool) {
        // r.setType(l.type());
        // } else if (l.type() != IntermediateType.intOrBool && r.type() !=
        // IntermediateType.intOrBool
        // && l.type() != r.type()) {
        // throw new ByteCodeCompilerException("Illegal type constraint: " + l +
        // ", " + r);
        // } else {
        // if (p.l.type() == IntermediateType.intOrBool)
        // typeConstraints.addLast(p);
        // }
        // }
        // // if constraints did not converge, we have types, that may be both,
        // // bool or int, so we chose bool
        // for (Pair p : typeConstraints) {
        // if (p.l.type() != p.r.type() && p.l.type() !=
        // IntermediateType.intOrBool)
        // throw new
        // ByteCodeCompilerException("Illegal unresolved type constraint: " +
        // p.l + ", " + p.r);
        //
        // p.l.setType(IntermediateType.bool);
        // p.r.setType(IntermediateType.bool);
        // }

        // create stack and local variables
        cfg.stack = new StackLayout(cfg, frames);
        cfg.registers = new RegisterLayout(cfg, frames);
    }


    /**
     * Add a constraint and return one argument to allow for direct adding of
     * other constraints.
     */
    // private Type addConstraint(Type left, Type right) {
    // typeConstraints.add(new Pair(left, right));
    // return left;
    // }
}
