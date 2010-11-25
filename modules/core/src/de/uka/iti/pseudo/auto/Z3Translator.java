/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2010 Universitaet Karlsruhe, Germany
 *    written by Mattias Ulbrich
 * 
 * The system is protected by the GNU General Public License. 
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.auto;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import de.uka.iti.pseudo.environment.Binder;
import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.environment.Function;
import de.uka.iti.pseudo.environment.NumberLiteral;
import de.uka.iti.pseudo.term.Application;
import de.uka.iti.pseudo.term.Binding;
import de.uka.iti.pseudo.term.Sequent;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.TermException;
import de.uka.iti.pseudo.term.Type;
import de.uka.iti.pseudo.term.Variable;
import de.uka.iti.pseudo.term.creation.DefaultTermVisitor;
import de.uka.iti.pseudo.util.Util;

public class Z3Translator extends DefaultTermVisitor {
    
    private static final String[] BUILTIN_FUNCTIONS = {
        "false", "false",
        "true", "true",
        "$not", "not",
        "$and", "and",
        "$or", "or",
        "$impl", "implies",
        "$equiv", "iff",
        "\\forall", "FORALL",
        "\\exists", "EXISTS",
        "$gt", ">",
        "$lt", "<",
        "$gte", ">=",
        "$lte", "<=",
        "$eq", "=",
        "$plus", "+",
        "$minus", "-",
        "$mult", "*",
        "$div", "/"
    };
    
    private Map<String,String> translationMap = new HashMap<String, String>();
    
    /**
     * these storages can be read by test cases
     */
    Set<String> types = new HashSet<String>();
    Set<String> functions = new HashSet<String>();
    List<String> translation = new ArrayList<String>();
    
    private int registerNo = 0;
    private Stack<Variable> boundVariables = new Stack<Variable>();

    public Z3Translator(Environment env) {
        for (int i = 0; i < BUILTIN_FUNCTIONS.length; i += 2) {
            translationMap.put(BUILTIN_FUNCTIONS[i], BUILTIN_FUNCTIONS[i+1]);
        }
    }
    
    protected void defaultVisitTerm(Term term) throws TermException {
        registerNo++;
        String name = "c" + registerNo;
        String type = makeSort(term.getType());
        translation.add("Const " + name + " " + name + " " + type);
    }
    
    public void visit(Application application) throws TermException {
        Function function = application.getFunction();
        String name = function.getName();
        
        if(function instanceof NumberLiteral) {
            registerNo ++;
            translation.add("Num c" + registerNo + " " + name + " int");
            return;
        }
        
        String trans = translationMap.get(name);
        
        if(trans == null && application.countSubterms() == 0) {
            registerNo ++;
            String ty = makeSort(application.getType());
            translation.add("Const c" + registerNo + " " + maskName(name) + "." + ty + " " + ty);
            return;
        }
        
        StringBuffer result = new StringBuffer();
        for (Term subterm : application.getSubterms()) {
            subterm.visit(this);
            result.append(" c").append(registerNo);
        }

        registerNo ++;
        
        if(trans == null) {
            String decl = makeFuncDecl(application);
            result.insert(0, "Fun c" + registerNo + " " + decl);
        } else {
            result.insert(0, "App c" + registerNo + " " + trans);
        }
        
        translation.add(result.toString());
    }
    
    /*
 * quantifier  :=  
      (FORALL | EXISTS) 
      weight-num  
      skolem-id
      quant-id 
      decls-num
      (name-id type-id)*
      pattern-num
      pattern-id*
      ast-id
 */    
    public void visit(Binding binding) throws TermException {
        Binder binder = binding.getBinder();
        String name = binder.getName();
        String trans = translationMap.get(name);
        
        if(translation == null) {
            defaultVisitTerm(binding);
            return;
        }

        // we assume that this holds ...
        Variable variable = (Variable) binding.getVariable();
        
        boundVariables.push(variable);
        binding.getSubterm(0).visit(this);
        boundVariables.pop();
        
        int innerIndex = registerNo;
        
        
        String bound = variable.toString(false);
        String boundType = makeSort(variable.getType());
        
        registerNo++;
        translation.add("Var c" + registerNo + " 0 " + boundType);
        registerNo++;
        translation.add("Pat c" + registerNo + " c" + (registerNo - 1));
        int pattern = registerNo;
        registerNo++;
        translation.add("Qua c" + registerNo + 
                " " + trans + " 0 sk." + bound + 
                " q." + bound + " 1 " + bound + " " + boundType + 
                " 1 c" + pattern + " c" + innerIndex);
        
    }
    
    public void visit(Variable variable) throws TermException {
        int index = boundVariables.indexOf(variable);
        if(index == -1)
            throw new TermException("Unbound variable " + variable);
        
        registerNo ++;
        translation.add("Var c" + registerNo + " " + index + " " +
                makeSort(variable.getType()));
    }

    private String makeFuncDecl(Application application) {
        String name = "x." + application.getFunction().getName();
        name = name.replace('$', '_');
        
        String resultType = makeSort(application.getType());
        
        List<Term> subterms = application.getSubterms();
        String[] argTypes = new String[subterms.size()];
        for (int i = 0; i < argTypes.length; i++) {
            argTypes[i] = makeSort(subterms.get(i).getType());
        }
        
        String result = name + "." + Util.join(argTypes, ".") + "." + resultType;

        functions.add("Dec " + result + " " + result + " " +
                Util.join(argTypes, " ") + " " + resultType);
        
        return result;
    }
    
    private String maskName(String name) {
        name = "x." + name;
        name = name.replace('$', '_');
        return name;
    }

    
    private String makeSort(Type type) {
        String typeString = toString(type);
        types.add("Type " + typeString + " " + typeString);
        return typeString;
    }

    private String toString(Type t) {
        return t.toString().replaceAll("[\\(\\),]", "_");
    }
    
    public void export(Sequent sequent, Writer stream) throws TermException, IOException {
        
        translation.clear();
        types.clear();
        functions.clear();
        registerNo = 0;
        
        translate(sequent);
        
        PrintWriter pw = new PrintWriter(stream);
        pw.println("; created by ivil " + new Date());
        
        for (String type : types) {
            pw.println(type);
        }
        for (String fct : functions) {
            pw.println(fct);
        }
        for (String line : translation) {
            pw.println(line);
        }
        pw.println("Check");
        
        pw.flush();
    }

    private void translate(Sequent sequent) throws TermException {
        for (Term t : sequent.getAntecedent()) {
            t.visit(this);
            translation.add("Assert c" + registerNo);
        }
        for (Term t : sequent.getSuccedent()) {
            t.visit(this);
            translation.add("App c" + (registerNo+1) + " not c" + registerNo);
            registerNo ++;
            translation.add("Assert c" + registerNo);
        }
    }

}
