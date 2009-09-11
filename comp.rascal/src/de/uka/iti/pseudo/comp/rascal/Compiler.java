package de.uka.iti.pseudo.comp.rascal;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;


public class Compiler {

    /**
     * @param args
     * @throws Exception 
     */
    public static void main(String[] args) throws Exception {
        
        System.out.println("1. Parameter: .ras Datei");
        System.out.println("2. Paramerte: Name der Prozedur");
        
        String procName = args[1];
        
        new RascalParser(new java.io.FileInputStream(args[0]));
        
        Node ast = RascalParser.Program();
        
        EnvironmentExtractor ext = new EnvironmentExtractor(procName);
        ast.jjtAccept(ext, null);
        Environment env = ext.getEnv();
        
        Translator translator = new Translator(env);
        
        ASTProcDecl procedureUnderInspection = ext.getProcedureUnderInspection();
        if(procedureUnderInspection == null)
            throw new Exception("Procedure not found!");
        
        // that triggers actual translation
        procedureUnderInspection.jjtAccept(translator, null);
        
        // rest is output only
        PrintWriter out = new PrintWriter(System.out);
        
        dumpPreamble(out);

        env.exportPseudo(out);
        
        Procedure proc = env.contractMap.get(procName);
        proc.dumpParameters(out);
        
        translator.getRegisterBank().dump(out);
        
        out.println("(*** Contracts");
        env.dumpContracts(out);
        out.println("***)");
        out.println();
        
        out.println("program P source \"" + args[0] + "\"");
        for (String line : translator.getStatements()) {
            out.println("  " + line);
        }
        out.println();
        
        out.println("problem [0; P]");
        out.flush();
    }

    private static void dumpPreamble(PrintWriter out) throws IOException {
        InputStream preamble = Compiler.class.getResourceAsStream("preamble.txt");
        byte[] buffer = new byte[1024];
        int read = preamble.read(buffer);
        while(read > 0) {
            out.write(new String(buffer, 0, read));
            read = preamble.read(buffer);
        }
        preamble.close();
    }

}
