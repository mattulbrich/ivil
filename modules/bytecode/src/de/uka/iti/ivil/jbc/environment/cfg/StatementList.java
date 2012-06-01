package de.uka.iti.ivil.jbc.environment.cfg;

import java.util.ArrayList;
import java.util.List;

import de.uka.iti.ivil.jbc.environment.BytecodeCompilerException;

public class StatementList {
    private StringBuilder sb = new StringBuilder();
    private List<String> statements = new ArrayList<String>(32);

    /**
     * Statements is backupped in special statements while translating special
     * statements. This is because loop invariants need to be put at the
     * beginning of the loop.
     */
    private String moveableStatement = null;

    /**
     * starts a new statement by printing its sourceline annotation
     * 
     * @param pc
     *            the pc of the current instruction
     * 
     * @param moveableTargetInstruction
     *            true iff the instruction can be the target of moveable
     *            instructions
     */
    StatementList begin(int lineNumber, int pc, boolean moveableTargetInstruction) {
        sb.append("\nsourceline ").append(lineNumber).append("  PC").append(pc).append(":\n  ");
        if (moveableTargetInstruction) {
            // assume that loops maintain wellformedness
            end("assume $wellformed($heap)");
            if (null != moveableStatement) {
                // write moveable statement at the beginning of the translation
                // of
                // that opcode
                end(moveableStatement);
                moveableStatement = null;
                end("assume $wellformed($heap)");
            }
        }
        return this;
    }

    /**
     * append s to the current line
     */
    public StatementList add(String s) {
        sb.append(s);
        return this;
    }

    /**
     * append s to the current line
     */
    public StatementList add(int s) {
        sb.append(s);
        return this;
    }

    /**
     * append s to the current line and end the statement
     */
    public StatementList end(String s) {
        sb.append(s).append("\n");
        statements.add(sb.toString());
        sb = new StringBuilder("  ");
        return this;
    }

    /**
     * append s to the current line and end the statement
     */
    public StatementList end(int s) {
        sb.append(s).append("\n");
        statements.add(sb.toString());
        sb = new StringBuilder("  ");
        return this;
    }

    public void endMoveable(String s) throws BytecodeCompilerException {
        if (null != moveableStatement)
            throw new BytecodeCompilerException(
                    "there is already a moveable statement. only one is allowed. do not create multiple invariants for the same loop!");
        moveableStatement = s;
    }

    public int getStatementCount() {
        // @note: moveable statements do not count, because they do not yet
        // exist. if this mechanism becomes a problem, remove it completely and
        // replace it by more labels inside opcodes, goto #,# is bad style
        // anyway
        return statements.size();
    }

    @Override
    public String toString() {
        StringBuilder o = new StringBuilder();
        for (String s : statements)
            o.append(s);
        return o + "\n--|===>\n" + sb;
    }

    /**
     * Build a String from the statement list, thus finishing the translation of
     * a method.
     */
    public String build() throws BytecodeCompilerException {
        if (moveableStatement != null)
            throw new BytecodeCompilerException("there is a dangling moveable statement: " + moveableStatement);

        for (String s : statements)
            sb.append(s);
        return sb.toString();
    }

    /**
     * creates labels and goto statements such that the control flow will
     * continue at these labels. Label names are created using the prefix and an
     * appended number, thus the caller has to guarantee absence of name
     * clashes.
     * 
     * @param prefix
     *            common label prefix
     * 
     * @param count
     *            the number of labels the caller wants to jump to; count musst
     *            be > 0
     * 
     * @return an array ob labels that can be used by the caller to mark the
     *         jump targets
     */
    public String[] gotoLabels(String prefix, int count) {
        assert count > 0 : "you need to supply at least one jump target";
        if (1 == count) {
            add("goto ").add(prefix).add("0\n  ");
            return new String[] { prefix + "0" };
        }
        String[] result = new String[count];
        int i = 0;
        while(count > 2) {
            count--;
            result[count] = prefix + (i++);
            add("goto ").add(result[count]).add(", ").end(prefix + (i++));
            add(prefix + (i - 1)).add(": ");
        }
        result[1] = prefix + (i++);
        result[0] = prefix + (i++);
        add("goto ").add(result[1]).add(", ").end(result[0]);

        return result;
    }
}
