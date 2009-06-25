package de.uka.iti.pseudo.auto;

import java.io.PrintWriter;
import java.io.Writer;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.uka.iti.pseudo.environment.Binder;
import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.environment.Function;
import de.uka.iti.pseudo.environment.NumberLiteral;
import de.uka.iti.pseudo.term.Application;
import de.uka.iti.pseudo.term.BindableIdentifier;
import de.uka.iti.pseudo.term.Binding;
import de.uka.iti.pseudo.term.Sequent;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.TermException;
import de.uka.iti.pseudo.term.Type;
import de.uka.iti.pseudo.term.Variable;
import de.uka.iti.pseudo.term.creation.DefaultTermVisitor;
import de.uka.iti.pseudo.util.Util;

public class SMTLibTranslator extends DefaultTermVisitor {
    
    private static final String[] KNOWN_TRANSLATIONS = {
        "false", "false",
        "true", "true",
        "$not", "not",
        "$and", "and",
        "$or", "or",
        "$and", "and",
        "$impl", "implies",
        "$equiv", "iff",
        "\\forall", "forall",
        "\\exists", "exists",
        "$gt", ">",
        "$lt", "<",
        "$gte", ">=",
        "$lte", "<=",
        "$eq", "=",
        "$plus", "+",
        "$minus", "-",
        "$mult", "*"
    };
    
    private Map<Term, String> unknownMap = new HashMap<Term, String>();
    private Map<String,String> translationMap = new HashMap<String, String>();
    
    private Environment env;
    private int unknownCounter = 0;
    
    /**
     * these storages can be read by test cases
     */
    Set<String> extrasorts = new HashSet<String>();
    Set<String> extrafuncs = new HashSet<String>();
    Set<String> extrapreds = new HashSet<String>();
    
    private StringBuffer writer = new StringBuffer();
    /*package*/ int indention = 0;


    public SMTLibTranslator(Environment env) {
        this.env = env;
        for (int i = 0; i < KNOWN_TRANSLATIONS.length; i += 2) {
            translationMap.put(KNOWN_TRANSLATIONS[i], KNOWN_TRANSLATIONS[i+1]);
        }
    }
    
    public String translate(Term term) throws TermException {
        term.visit(this);
        String result = writer.toString();
        writer.setLength(0);
        return result;
    }
    
    public String translate(Sequent sequent) throws TermException { 
        append("(and true "); 
        for (Term term : sequent.getAntecedent()) {
            term.visit(this);
            append(" ");
        }
        for (Term term : sequent.getSuccedent()) {
            append("(not ");
            term.visit(this);
            append(") ");
        }
            
        append(")");
        String result = writer.toString();
        writer.setLength(0);
        return result;
    }

    protected void defaultVisitTerm(Term term) throws TermException {
        String name = "unknown" + unknownCounter;
        unknownCounter++;
        append(name);
        
        if(term.getType().equals(Environment.getBoolType())) {
            extrapreds.add("(" + name + ")");
        } else {
            extrafuncs.add("(" + name + " " + makeSort(term.getType()) + ")");
        }
    }
    
    public void visit(Application application) throws TermException {
        Function function = application.getFunction();
        String name = function.getName();
        String translation = translationMap.get(name);

        if(translation == null && function instanceof NumberLiteral) {
            translation = function.getName();
            translationMap.put(translation, translation);
        }

        if(translation == null) {
            translation = makeExtraFunc(application);
        }

        boolean hasArgs = application.countSubterms() > 0;

        if(hasArgs) {
            append("(" + translation);
            for (Term subterm : application.getSubterms()) {
                append(" ");
                subterm.visit(this);
            }
            append(")");
        } else {
            append(translation);
        }
    }
    
    public void visit(Binding binding) throws TermException {
        Binder binder = binding.getBinder();
        String name = binder.getName();
        String translation = translationMap.get(name);
        
        if(translation == null) {
            defaultVisitTerm(binding);
            return;
        }
        
        append("(" + translation);
        BindableIdentifier variable = binding.getVariable();
        
        assert variable instanceof Variable;
        
        String bound = variable.toString(false);
        String boundType = makeSort(variable.getType());

        append(" (?" + bound +" " + boundType + ") ");
        binding.getSubterm(0).visit(this);
        append(")");
    }
    
    public void visit(Variable variable) throws TermException {
        append("?" + variable.getName());
    }

    private String makeExtraFunc(Application application) {
        String name = "extra." + application.getFunction().getName();
        name = name.replace('$', '_');
        
        String resultType = makeSort(application.getType());
        
        List<Term> subterms = application.getSubterms();
        String[] argTypes = new String[subterms.size()];
        for (int i = 0; i < argTypes.length; i++) {
            argTypes[i] = makeSort(subterms.get(i).getType());
        }
        
        String result = name + "." + Util.join(argTypes, ".") + "." + resultType;
        
        if(application.getType().equals(Environment.getBoolType())) {
            extrapreds.add("(" + result + " " + Util.join(argTypes, " ") + ")");
        } else {
            extrafuncs.add("(" + result + " " + Util.join(argTypes, " ") + " " + resultType + ")");
        }
        
        return result;
    }
    
    private String makeSort(Type type) {
        String typeString = toString(type);
        if(typeString.equals("int"))
            return "Int";
        else {
            extrasorts.add(typeString);
            return typeString;
        }
    }

    private String toString(Type t) {
        return t.toString().replaceAll("[\\(\\),]", "_");
    }
        
    
    private void append(String string) {

        // we can switch off indenting ... in test cases for instance
        if(indention == -1) {
            writer.append(string);
            return;
        }
        
        for(int i = 0; i < string.length(); i++) {
            char c = string.charAt(i);
            switch(c) {
            case '(':
                writer.append("\n");
                for(int j = 0; j < indention; j++)
                    writer.append(" ");
                writer.append("(");
                indention++;
                break;
            case ')':
                indention--;
            default:
                writer.append(c);
            }
        }
    }
    
    public void export(Sequent sequent, Writer stream) throws TermException {
        
        extrapreds.clear();
        extrafuncs.clear();
        extrasorts.clear();
        
        String translation = translate(sequent);
        
        PrintWriter pw = new PrintWriter(stream);
        pw.println("; created by pseudo " + new Date());
        pw.println("(benchmark pseudo_verification");
        pw.println(":logic AUFLIA");
        pw.println();
        pw.println(":extrasorts (" + Util.join(extrasorts, "\n   ") + ")");
        pw.println();
        pw.println(":extrapreds (" + Util.join(extrapreds, "\n   ") + ")");
        pw.println();
        pw.println(":extrafuns (" + Util.join(extrafuncs, "\n   ") + ")");
        pw.println();
        pw.println(":assumption (" + translation + ")");
        
        pw.flush();
    }

}
