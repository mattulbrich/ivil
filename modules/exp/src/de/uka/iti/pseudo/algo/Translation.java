/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2012 Karlsruhe Institute of Technology
 *
 * The system is protected by the GNU General Public License.
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.algo;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.List;
import java.util.ListIterator;

import de.uka.iti.pseudo.util.Util;

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
        
        // parentheses around ensures
        List<String> guarantees = visitor.getGuarantees();
        if(guarantees.isEmpty()) {
            guarantees.add("true");
        }
        ListIterator<String> it = guarantees.listIterator();
        while(it.hasNext()) {
            it.set("(" + it.next() + ")");
        }

        System.out.println();
        System.out.println("problem ");
        String req = Util.commatize(visitor.getRequirements());
        String ens = Util.join(guarantees, " & ");  
        
        System.out.println(req + " |- [[0;" + visitor.getProgramName() + "]](" + ens + ")");
        
    }

}
