package de.uka.iti.pseudo.algo;

public class Translation {

    public static void main(String[] args) throws ParseException {
        AlgoParser p = new AlgoParser(System.in);
        
        ASTStart result = AlgoParser.Start();
        TranslationVisitor visitor = new TranslationVisitor();
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
