package de.uka.iti.pseudo.auto;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
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
    
    private static final String[] BUILTIN_FUNCTIONS = {
        "false", "false",
        "true", "true",
        "$not", "not",
        "$and", "and",
        "$or", "or",
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
    
    private static final List<String> PREDICATES = Util.readOnlyArrayList(new String[] {
        "true", "false", "and", "or", "implies", "iff", "not", "<", ">", "<=", ">=", "="
    });


    
//         private Map<Term, String> unknownMap = new HashMap<Term, String>();
    private Map<String,String> translationMap = new HashMap<String, String>();
    
    private int unknownCounter = 0;
    
    /**
     * these storages can be read by test cases
     */
    Set<String> extrasorts = new HashSet<String>();
    Set<String> extrafuncs = new HashSet<String>();
    String result;
    
    boolean resultType;

    /**
     * the "cond" function from the environment must be
     * treated separately. This may be null
     */
    private Function condFunction;
    
    private static boolean FORMULA = true;
    private static boolean TERM = false;
    
    public SMTLibTranslator(Environment env) {
        for (int i = 0; i < BUILTIN_FUNCTIONS.length; i += 2) {
            translationMap.put(BUILTIN_FUNCTIONS[i], BUILTIN_FUNCTIONS[i+1]);
        }
        
        condFunction = env.getFunction("cond");
    }
    
    public String translate(Term term) throws TermException {
        term.visit(this);
        return result;
    }
    
    public String translate(Sequent sequent) throws TermException {
        StringBuilder sb = new StringBuilder();
        sb.append("(and true "); 
        for (Term term : sequent.getAntecedent()) {
            term.visit(this);
            sb.append(resultAs(FORMULA));
        }
        for (Term term : sequent.getSuccedent()) {
            sb.append("(not ");
            term.visit(this);
            sb.append(resultAs(FORMULA));
            sb.append(")");
        }
            
        sb.append(")");
        String result = sb.toString();
        return result;
    }

    // TODO variables
    protected void defaultVisitTerm(Term term) throws TermException {
        String name = "unknown" + unknownCounter;
        unknownCounter++;
        result = name;
        resultType = TERM;
        
        extrafuncs.add("(" + name + " " + makeSort(term.getType()) + ")");
    }
    
    public void visit(Application application) throws TermException {
        Function function = application.getFunction();
        String name = function.getName();
        String translation = translationMap.get(name);
        
        if(function == condFunction) {
            StringBuilder sb = new StringBuilder();
            sb.append("(ite ");
            application.getSubterm(0).visit(this);
            sb.append(resultAs(FORMULA)).append(" ");
            application.getSubterm(1).visit(this);
            sb.append(resultAs(TERM)).append(" ");
            application.getSubterm(2).visit(this);
            sb.append(resultAs(TERM)).append(")");
            result = sb.toString();
            return;
        }
        
        if(translation == null && function instanceof NumberLiteral) {
            translation = function.getName();
            translationMap.put(translation, translation);
        }

        if(translation == null) {
            translation = makeExtraFunc(application);
        }

        boolean hasArgs = application.countSubterms() > 0;

        if(hasArgs) {
            StringBuilder sb = new StringBuilder();
            sb.append("(").append(translation);
            for (Term subterm : application.getSubterms()) {
                sb.append(" ");
                subterm.visit(this);
                boolean expectedType = PREDICATES.contains(translation) 
                    && subterm.getType().equals(Environment.getBoolType()) ? FORMULA : TERM;
                sb.append(resultAs(expectedType));
            }
            sb.append(")");
            result = sb.toString();
        } else {
            result = translation;
        }
        resultType = PREDICATES.contains(translation) ? FORMULA : TERM;
    }
    
    public void visit(Binding binding) throws TermException {
        Binder binder = binding.getBinder();
        String name = binder.getName();
        String translation = translationMap.get(name);
        
        if(translation == null) {
            defaultVisitTerm(binding);
            return;
        }
        
        StringBuilder retval = new StringBuilder("(" + translation);
        BindableIdentifier variable = binding.getVariable();
        
        assert variable instanceof Variable;
        
        String bound = variable.toString(false);
        String boundType = makeSort(variable.getType());

        retval.append(" (?").append(bound).append(" ").
                append(boundType).append(") ");
        
        binding.getSubterm(0).visit(this);
        retval.append(resultAs(FORMULA)).append(")");
        
        result = retval.toString();
        resultType = FORMULA;
    }
    
    private String resultAs(boolean type) {
        if(type == resultType)
            return result;
        
        if(type == FORMULA) {
            return "(= " + result + " termTrue" + ")";
        } else {
            return "(ite " + result + " termTrue termFalse)";
        }
    }

    public void visit(Variable variable) throws TermException {
        result = "?" + variable.getName();
        resultType = TERM;
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
        
        extrafuncs.add("(" + result + " " + Util.join(argTypes, " ") + " " + resultType + ")");
        
        return result;
    }
    
    private String makeSort(Type type) {
        String typeString = toString(type);
        if(typeString.equals("int"))  {
            return "Int";
        } else if(typeString.equals("bool"))  {
            return "Bool";
        } else {
            extrasorts.add(typeString);
            return typeString;
        }
    }

    private String toString(Type t) {
        return t.toString().replaceAll("[\\(\\),]", "_");
    }
        
    
    public static String indent(String string) {

        StringBuilder sb = new StringBuilder();
        int indention = 0;
        
        for(int i = 0; i < string.length(); i++) {
            char c = string.charAt(i);
            switch(c) {
            case '(':
                sb.append("\n");
                for(int j = 0; j < indention; j++)
                    sb.append(" ");
                sb.append("(");
                indention++;
                break;
            case ')':
                indention--;
                // fall through
            default:
                sb.append(c);
            }
        }
        
        return sb.toString();
    }
    
    public void export(Sequent sequent, Appendable builder) throws TermException, IOException {
        
        extrafuncs.clear();
        extrasorts.clear();
        
        String translation = translate(sequent);
        
        builder.append("; created by pseudo " + new Date());
        builder.append("\n(benchmark pseudo_verification\n");
        builder.append(":logic AUFLIA\n\n");
        includePreamble(builder);
        builder.append(":extrasorts (" + Util.join(extrasorts, "\n   ") + ")\n\n");
        builder.append(":extrafuns (" + Util.join(extrafuncs, "\n   ") + ")\n\n");
        builder.append(":assumption (" + indent(translation) + ")\n");
        
    }

    private void includePreamble(Appendable pw) throws IOException {
        InputStream stream = getClass().getResourceAsStream("preamble.smt");
        if(stream == null)
            throw new IOException("Resource preamble.smt not found");
        Reader r = new InputStreamReader(stream);
        char[] buffer = new char[1024];
        int read = r.read(buffer);
        while(read != -1) {
            pw.append(new String(buffer, 0, read));
            read = r.read(buffer);
        }
    }

}
