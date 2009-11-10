/*
 * This file is part of PSEUDO
 * Copyright (C) 2009 Universitaet Karlsruhe, Germany
 *    written by Mattias Ulbrich
 * 
 * The system is protected by the GNU General Public License. 
 * See LICENSE.TXT for details.
 */

package de.uka.iti.pseudo.term.creation;

import java.util.List;

import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.term.Application;
import de.uka.iti.pseudo.term.Binding;
import de.uka.iti.pseudo.term.LiteralProgramTerm;
import de.uka.iti.pseudo.term.SchemaProgramTerm;
import de.uka.iti.pseudo.term.SchemaUpdateTerm;
import de.uka.iti.pseudo.term.SchemaVariable;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.TermException;
import de.uka.iti.pseudo.term.TypeVariable;
import de.uka.iti.pseudo.term.UnificationException;
import de.uka.iti.pseudo.term.Update;
import de.uka.iti.pseudo.term.UpdateTerm;
import de.uka.iti.pseudo.term.Variable;
import de.uka.iti.pseudo.term.statement.Statement;

/**
 * The Class TermMatcher implements the term visitor which is used to unify
 * terms.
 * 
 * Only left unification is supported, i.e. schema entities may appear only on
 * the left hand side and will be matched accordingly.
 */
class TermMatcher extends DefaultTermVisitor {

    /**
     * The unification objects records the instantiations
     */
    private TermUnification termUnification;
    
    /**
     * The subterm to compare with
     */
    private Term compareTerm;
    
    /**
     * use this environment to resolve program statements.
     */
    private Environment env;
    
    /**
     * Instantiates a new term matcher with a given instantiation object
     * 
     * @param termUnification
     *            the object to record instantiations to.
     * @param env
     *            the environment, needed to extract information about
     *            statements of progs.
     */
    public TermMatcher(TermUnification termUnification, Environment env) {
        this.termUnification = termUnification;
        this.env = env;
    }

    /**
     * Compare two terms.
     * 
     * First, type unification is performed. Then if t1 is a schema variable,
     * the variable is expanded. If there is no term for it, yet, the schema
     * variable is correspondingly instantiated.
     * 
     * Schema programs are matched in 
     * {@link #matchSchemaProgram(SchemaProgram, LiteralProgramTerm)}
     * 
     * All other cases are subject to a visit.
     * 
     * However, only if the types (=class) of the terms coincide, comparison
     * is performed, otherwise a fail message is thrown
     * 
     * @param t1
     *            the left hand term
     * @param t2
     *            the right hand term
     * 
     * @throws TermException
     *             if unification fails
     */
    public void compare(Term t1, Term t2) throws TermException {
        termUnification.getTypeUnification().leftUnify(t1.getType(), t2.getType());
        if (t1 instanceof SchemaVariable) {
            SchemaVariable sv = (SchemaVariable) t1;
            Term inst = termUnification.getTermFor(sv);
            if(inst != null) {
                compare(inst, t2);
            } else {
                termUnification.addInstantiation(sv, t2);
                termUnification.getTypeUnification().leftUnify(new TypeVariable(sv.getName()), t2.getType());
            }
            
        } else if(t1 instanceof SchemaProgramTerm && t2 instanceof LiteralProgramTerm) {
            SchemaProgramTerm sp = (SchemaProgramTerm) t1;
            LiteralProgramTerm litPrg = (LiteralProgramTerm) t2;
            matchSchemaProgram(sp, litPrg);
            
        } else if(t1 instanceof SchemaUpdateTerm && t2 instanceof UpdateTerm) {
            SchemaUpdateTerm su = (SchemaUpdateTerm) t1;
            UpdateTerm upt = (UpdateTerm) t2; 
            matchSchemaUpdate(su, upt);
            
        } else if(t1.getClass() == t2.getClass()) {
            compareTerm = t2;
            t1.visit(this);
            
        } else 
            throw new UnificationException("Incomparable types of terms", t1, t2);
    }
    
    private void matchSchemaUpdate(SchemaUpdateTerm su, UpdateTerm upt) throws TermException {
        String schemaIdentifier = su.getSchemaIdentifier();
        Update inst = termUnification.getUpdateFor(schemaIdentifier);
        if(inst == null){
            termUnification.addUpdateInstantiation(schemaIdentifier, upt.getUpdate());
        } else {
            if(!inst.equals(upt.getUpdate()))
                throw new UnificationException("Incomparable updates", su, upt);
        }
        compare(su.getSubterm(0), upt.getSubterm(0));
    }

    /*
     * match a schema program [ %a : stmt_with %schemavars ] to a literal
     * program term [ n ; P ] with n a number literal.
     * 
     * The statements are extracted and compared by class. If the classes are
     * identical, then subterms are compared and matched pairwise.
     * 
     * Only an identical number of subterms can be matched.
     */
    private void matchSchemaProgram(SchemaProgramTerm sp, LiteralProgramTerm litPrg) throws TermException {
        SchemaVariable sv = sp.getSchemaVariable();
        Term inst = termUnification.getTermFor(sv);
        
        if(inst != null) {
            compare(inst, litPrg);
        } else {
            if(sp.isTerminating() != litPrg.isTerminating())
                throw new UnificationException("Incomparable termination", sp, litPrg);
            
            termUnification.addInstantiation(sv, litPrg);
            termUnification.getTypeUnification().leftUnify(new TypeVariable(sv.getName()), Environment.getBoolType());
            if(sp.hasMatchingStatement()) {
                Statement matchingSt = sp.getMatchingStatement();
                Statement statement = litPrg.getStatement();
                
                if(matchingSt.getClass() != statement.getClass())
                    throw new UnificationException("Incomparable types of statements", matchingSt, statement);
                
                if(matchingSt.countSubterms() != statement.countSubterms())
                    throw new UnificationException("Incomparable count of subterms in statements", matchingSt, statement);
                
                List<Term> matchingSubterms = matchingSt.getSubterms();
                List<Term> subterms = statement.getSubterms();
                for (int i = 0; i < matchingSubterms.size(); i++) {
                    compare(matchingSubterms.get(i), subterms.get(i));
                }
            }
        }
    }

    /* 
     * the default behaviour is to compare all subterms pairwise
     */
    @Override
    protected void defaultVisitTerm(Term term) throws TermException {
        List<Term> sub1 = term.getSubterms();
        List<Term> sub2 = compareTerm.getSubterms();
        
        for (int i = 0; i < sub1.size(); i++) {
            compare(sub1.get(i), sub2.get(i));
        }
    }
    
    @Override
    public void visit(Variable v1) throws TermException {
        Variable v2 = (Variable) compareTerm;
        
        if(!v1.getName().equals(v2.getName()))
            throw new UnificationException(v1, v2);
        
        termUnification.getTypeUnification().leftUnify(v1.getType(), v2.getType());
    }

    @Override 
    public void visit(Binding b1) throws TermException {
        Binding b2 = (Binding) compareTerm;
        
        // binders are identical, not only equal
        if(b1.getBinder() != b2.getBinder())
            throw new UnificationException(b1, b2);
        
        termUnification.getTypeUnification().leftUnify(b1.getVariableType(), b2.getVariableType());
        
        if(b1.hasSchemaVariable()) {
            // rhs may not contain schema stuff
            assert !b2.hasSchemaVariable();
            termUnification.addInstantiation(
                    new SchemaVariable(b1.getVariableName(), b1.getVariableType()), 
                    new Variable(b2.getVariableName(), b2.getVariableType()));
        } else if(!b1.getVariableName().equals(b2.getVariableName())) {
            throw new UnificationException("Different variable", b1, b2);
        }
        
        defaultVisitTerm(b1);
    }

    @Override 
    public void visit(Application a1) throws TermException {
        Application a2 = (Application) compareTerm;
        
        // there is only one function symbols, check via ==
        if(a1.getFunction() != a2.getFunction())
            throw new UnificationException(a1, a2);
        
        termUnification.getTypeUnification().leftUnify(a1.getType(), a2.getType());
        
        defaultVisitTerm(a1);
    }

    @Override 
    public void visit(SchemaVariable schemaVariable) throws TermException {
        throw new Error("cannot be called");
    }
    
    @Override 
    public void visit(SchemaProgramTerm schemaProgramTerm) throws TermException {
        throw new Error("unification of 2 schema program terms is not implemented / intended");
    }
    
    @Override 
    public void visit(SchemaUpdateTerm schemaUpdateTerm) throws TermException {
        throw new Error("unification of 2 schema update terms is not implemented / intended");
    }
    
    @Override 
    public void visit(UpdateTerm u1) throws TermException {
        
        UpdateTerm u2 = (UpdateTerm) compareTerm;
        
        if(!u2.getAssignments().equals(u1.getAssignments())) {
            throw new UnificationException(u1, u2);
        }
        
        super.visit(u1);
    }
    
    @Override 
    public void visit(LiteralProgramTerm p) throws TermException {
        LiteralProgramTerm p2 = (LiteralProgramTerm) compareTerm;
        
        if(p.getProgram() != p2.getProgram())
            throw new UnificationException("Incompatible programs", p, p2);
        
        if(p.getProgramIndex() != p2.getProgramIndex())
            throw new UnificationException("Incompatible indices", p, p2);
        
        if(p.isTerminating() != p2.isTerminating())
            throw new UnificationException("Incompatible termination", p, p2);
    }
}