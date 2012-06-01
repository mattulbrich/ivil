package de.uka.iti.ivil.jbc.util;

import java.io.ByteArrayInputStream;
import java.util.List;

import de.uka.iti.ivil.jbc.environment.BytecodeCompilerException;
import de.uka.iti.ivil.jbc.util.parsers.MethodDescriptorParser;
import de.uka.iti.ivil.jbc.util.parsers.ParseException;

/**
 * This class provides a method to unpack Java Bytecode Method Descriptors into
 * usable types.
 * 
 * @author timm.felden@felden.com
 */
public final class MethodDescriptorUnpacker {

    /**
     * Unpacks descriptor and returns an array of properly escaped typenames,
     * where the first element is the result of the function.
     */
    public static String[] parse(String descriptor) throws BytecodeCompilerException {
        
        MethodDescriptorParser p = new MethodDescriptorParser(new ByteArrayInputStream(descriptor.getBytes()));
        
        List<String> rval;
        try {
            rval = p.parse();
        } catch (ParseException e) {
            System.err.println(descriptor);
            throw new BytecodeCompilerException(e.getMessage());
        }
        
        for (int i = 0; i < rval.size(); i++)
            rval.set(i, rval.get(i));

        return rval.toArray(new String[rval.size()]);
    }
}
