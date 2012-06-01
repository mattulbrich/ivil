package de.uka.iti.ivil.jbc.environment.cfg;

import java.util.Stack;

/**
 * Frame layout has to refer to the post state of an instruction. Otherwise the
 * instruction translation will not work.
 * 
 * @author timm.felden@felden.com
 */
class TypeFrame {
    /**
     * may contain null to express emptyness.
     */
    Type[] localVariables;

    /**
     * may not contain null but vary in size. is itself null until created by
     * the opcode type inference steps.
     */
    Stack<Type> stack;

    TypeFrame(int maxLocals) {
        localVariables = new Type[maxLocals];
    }
}
