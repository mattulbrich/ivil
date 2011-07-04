package de.uka.iti.pseudo.algo;

import java.io.FileNotFoundException;
import java.io.FileReader;

public class Translation {

    public static void main(String[] args) throws ParseException, FileNotFoundException {
        String source;
        if(args.length > 0) {
            source = args[0];
            new AlgoParser(new FileReader(source));
        } else {
            source = null; 
            new AlgoParser(System.in);   
        }
        
        ASTStart result = AlgoParser.Start();
        TranslationVisitor visitor = new TranslationVisitor(source);
        result.jjtAccept(visitor, null);
        
        for (String string : visitor.getHeader()) {
            System.out.println(string);
        }
        
        System.out.println();
        
        for (String string : visitor.getStatements()) {
            System.out.println(string);
        }
    }

}
