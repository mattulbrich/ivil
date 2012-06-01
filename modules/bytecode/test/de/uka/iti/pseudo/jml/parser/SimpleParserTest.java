package de.uka.iti.pseudo.jml.parser;

import java.io.File;
import java.io.IOException;

import de.uka.iti.ivil.jml.parser.JMLParser;
import de.uka.iti.ivil.jml.parser.ParseException;

public class SimpleParserTest {

    /**
     * @param args
     */
    public static void main(String[] args) {
        try {
            System.out.println(JMLParser.parse(new File("Simple.java")).toString());
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
