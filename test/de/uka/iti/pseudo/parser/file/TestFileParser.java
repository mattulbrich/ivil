package de.uka.iti.pseudo.parser.file;

import java.io.File;
import java.io.FileNotFoundException;


public class TestFileParser {
	
	public static void main(String[] args) throws FileNotFoundException, ParseException {
		FileParser fp = new FileParser();
		ASTFile f = fp.parseFile(new File(args[0]));
		f.dumpTree();
	}
	
}