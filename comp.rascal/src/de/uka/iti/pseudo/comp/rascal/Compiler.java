package de.uka.iti.pseudo.comp.rascal;

import java.io.PrintWriter;


public class Compiler {

    /**
     * @param args
     * @throws Exception 
     */
    public static void main(String[] args) throws Exception {
        
        System.out.println("1. Parameter: .ras Datei");
        System.out.println("2. Paramerte: Name der Prozedur");
        
        new RascalParser(new java.io.FileInputStream(args[0]));
        
        Node ast = RascalParser.Program();
        
        EnvironmentExtractor ext = new EnvironmentExtractor(args[1]);
        ast.jjtAccept(ext, null);
        Environment env = ext.getEnv();
        
        Translator translator = new Translator(env);
        ext.getProcedureUnderInspection().jjtAccept(translator, null);
        
        PrintWriter out = new PrintWriter(System.out);

        env.exportPseudo(out);
        
        translator.getRegisterBank().dump(out);
        
        out.println("(*** Contracts");
        env.dumpContracts(out);
        out.println("***)");
        
        out.println("program P source \"" + args[0] + "\"");
        for (String line : translator.getStatements()) {
            out.println(line);
        }
        
        out.println("problem [P]");

        out.flush();
    }

}
