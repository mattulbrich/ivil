package de.uka.iti.ivil.jbc.environment.cfg;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import org.gjt.jclasslib.structures.AccessFlags;
import org.gjt.jclasslib.structures.ClassFile;
import org.gjt.jclasslib.structures.InvalidByteCodeException;
import org.gjt.jclasslib.structures.MethodInfo;
import org.gjt.jclasslib.structures.attributes.SourceFileAttribute;

import de.uka.iti.ivil.jbc.environment.BytecodeCompilerException;
import de.uka.iti.ivil.jbc.environment.ConcreteProofObligation;
import de.uka.iti.ivil.jbc.util.MethodName;
import de.uka.iti.ivil.jbc.util.ObjectType;
import de.uka.iti.ivil.jbc.util.MethodType;

/**
 * Translates a method using the cfg and the opcode translator.
 * 
 * @author timm.felden@felden.com
 */
public final class MethodTranslator {

    /**
     * writes a complete translation of cls.m; the po is the destination of the
     * generated code; path is needed in order to locate a source file and
     * ensure its existence
     * 
     * @return the name of the generated program
     */
    public static String translateProgram(ClassFile cls, MethodInfo method, ConcreteProofObligation<?> po, String path)
            throws BytecodeCompilerException {
        StringBuilder content = new StringBuilder();

        MethodName escapedName = MethodName.createFromClassFile(cls, method);
        content.append("\n\nprogram ").append(escapedName.getProgramName(cls));

        try {
            URL sourceFile = getSourceFile(cls, path);
            content.append("\n source \"").append(sourceFile).append("\"\n");
        } catch (BytecodeCompilerException e) {
            po.addWarning(e);
        }

        ControlFlowGraph cfg = CFGFactory.build(method, po);
        createProgramPreamble(cfg.statements, method, cfg.registers, cfg.methodName, cfg.proofObligation);

        OpcodeTranslator.translate(cfg);

        po.content.append(content);

        // cfg.dumpSymbolicState(System.out);

        cfg.writeStatements();

        return escapedName.getProgramName(cls);
    }

    /**
     * Creates initial state such as assuming that there is no exception and
     * R_this_ref is nonnull.
     * 
     * @param statements
     * @param method
     * @param registers
     * @param methodName
     * @param proofObligation
     */
    private static void createProgramPreamble(StatementList statements, MethodInfo method, RegisterLayout registers,
            MethodName methodName, ConcreteProofObligation<?> proofObligation) throws BytecodeCompilerException {
        boolean nonStatic = 0 == (method.getAccessFlags() & AccessFlags.ACC_STATIC);
        // the preamble is not part of the program and has to come before the
        // PC0 label, because the PC0 label can be a jump target

        // the heap is wellformed, there is no active exception and the old heap
        // refers to exactly this state
        statements.end("assume $wellformed($heap)");
        statements.end("$exception := $null");
        statements.end("$old_heap := $heap");

        // assume this to be nonnull for nonstatic methods and to have at
        // minimum the type of the current class
        if (nonStatic) {
            statements.end("assume !R_this_ref = $null & $heap[R_this_ref, $created]");

            statements
                    .add("assume instanceof(R_this_ref, ")
                    .add(ObjectType.createTypeFromBytecodeClass(
                            proofObligation.resolver.requestClass(methodName.getBytecodeClassName())).getIvilTypeTerm())
                    .end(")");

            // this might actually not be present, if it is never used in the
            // problem
            proofObligation.requestFunction("R_this_ref", "ref", "assignable");
        }

        // add instanceof constraints for arguments
        MethodType signature = methodName.getSignature(proofObligation.resolver);
        {
            int index = nonStatic ? 1 : 0;

            for (ObjectType type : signature.getArgumentTypes()) {
                if ("ref".equals(type.getBaseType())) {
                    final String varName = registers.get(index);
                    // ensure presence of that type
                    proofObligation.resolver.requestClass(type.getBytecodeClassName());

                    // any ref points to the created heap
                    statements.add("assume $heap[").add(varName).end(", $created]");

                    // any ref has at least the type stated in the signature
                    statements.add("assume instanceof(").add(varName).add(", ");
                    statements.add(type.getIvilTypeTerm());
                    statements.end(")");
                }
                if (type.isCategory2Type()) {
                    index += 2;
                } else {
                    index++;
                }
            }
        }
        // ensure preseence of return type
        {
            ObjectType type = signature.getResultType();
            if (type.getBaseType().equals("ref"))
                proofObligation.resolver.requestClass(type.getBytecodeClassName());
        }
    }

    /**
     * gets the URL of the source file and verifies its existence.
     */
    private static URL getSourceFile(ClassFile cls, String path) throws BytecodeCompilerException {
        URL sourceFile;

        try {
            final String s = cls.getConstantPoolEntryName(((SourceFileAttribute) cls
                    .findAttribute(SourceFileAttribute.class)).getSourcefileIndex());

            sourceFile = new URL(path.substring(0, path.lastIndexOf("/") + 1) + s);

            File source = new File(sourceFile.getFile());
            if (!source.isFile())
                throw new BytecodeCompilerException("The source file " + source.getAbsolutePath() + " does not exist.");

        } catch (InvalidByteCodeException e) {
            throw new BytecodeCompilerException("Class " + cls + " has not been compiled with attached source file.\n"
                    + e.getMessage());
        } catch (MalformedURLException e) {
            // why could this even happen?
            e.printStackTrace();
            throw new BytecodeCompilerException(e.getMessage());
        }

        return sourceFile;
    }
}
