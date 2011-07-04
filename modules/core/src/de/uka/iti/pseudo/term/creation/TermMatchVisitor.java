/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2010 Universitaet Karlsruhe, Germany
 * 
 * The system is protected by the GNU General Public License. 
 * See LICENSE.TXT (distributed with this file) for details.
 */

package de.uka.iti.pseudo.term.creation;

import java.util.List;

import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.term.Application;
import de.uka.iti.pseudo.term.Binding;
import de.uka.iti.pseudo.term.LiteralProgramTerm;
import de.uka.iti.pseudo.term.SchemaProgramTerm;
import de.uka.iti.pseudo.term.SchemaType;
import de.uka.iti.pseudo.term.SchemaUpdateTerm;
import de.uka.iti.pseudo.term.SchemaVariable;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.TermException;
import de.uka.iti.pseudo.term.Type;
import de.uka.iti.pseudo.term.TypeVariableBinding;
import de.uka.iti.pseudo.term.UnificationException;
import de.uka.iti.pseudo.term.Update;
import de.uka.iti.pseudo.term.UpdateTerm;
import de.uka.iti.pseudo.term.Variable;
import de.uka.iti.pseudo.term.statement.AssignmentStatement;
import de.uka.iti.pseudo.term.statement.Statement;

/**
 * The Class TermMatchVisitor implements the term visitor which is used to match
 * terms.
 * 
 * Only left unification is supported, i.e. schema entities may appear only on
 * the left hand side and will be matched accordingly.
 * 
 * @see TermMatcher
 * @see TypeMatchVisitor
 */
class TermMatchVisitor extends DefaultTermVisitor {

    /**
     * The matcher object records the instantiations
     */
    private TermMatcher termUnification;
    
    /**
     * the type visitor makes the type matching
     */
    private TypeMatchVisitor typeMatchVisitor;
    
    /**
     * The subterm to compare with
     * TODO have this as parameter when the TermVisitor supports parameters.
     */
    private Term compareTerm;
    
    /**
     * Instantiates a new term matcher with a given instantiation object
     * 
     * @param termMatcher
     *            the object to record instantiations to.
     */
    public TermMatchVisitor(TermMatcher termMatcher) {
        this.termUnification = termMatcher;
        this.typeMatchVisitor = new TypeMatchVisitor(termMatcher);
        // compareTerm is a non-null element
        try {
            this.compareTerm = SchemaVariable.getInst("%a", SchemaType.getInst("a"));
        } catch (TermException e) {
            e.printStackTrace();
            assert false : "schema code broken or specs changed";
        }
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
        t1.getType().accept(typeMatchVisitor, t2.getType());
        if (t1 instanceof SchemaVariable) {
            SchemaVariable sv = (SchemaVariable) t1;
            Term inst = termUnification.getTermFor(sv);
            if(inst != null) {
                compare(inst, t2);
            } else {
                termUnification.addInstantiation(sv, t2);
                // is this still desired? If %a <- 3 then %'a <- int automatically
                typeMatchVisitor.tryInstantiation(sv.getName().substring(1), t2.getType());
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
     * A special case is [ %a : U ] for a schematic parallel assignment.
     * 
     * The statements are extracted and compared by class. If the classes are
     * identical, then subterms are compared and matched pairwise.
     * 
     * Only an identical number of subterms can be matched.
     */
    private void matchSchemaProgram(SchemaProgramTerm sp, LiteralProgramTerm litPrg) throws TermException {
        SchemaVariable sv = sp.getSchemaVariable();
        Term inst = termUnification.getTermFor(sv);
        
        if(sp.isTerminating() != litPrg.isTerminating())
            throw new UnificationException("Incomparable termination", sp, litPrg);
        
        if(inst != null) {
            if(!inst.equals(litPrg)) {
                UnificationException ex = new UnificationException("Schema program already matched against other program term", sp, litPrg);
                ex.addDetail("Previous match: " + inst);
                throw ex;
            }
        } else {
            termUnification.addInstantiation(sv, litPrg);
            typeMatchVisitor.tryInstantiation(sv.getName().substring(1), Environment.getBoolType());
        }
        
        if(sp.hasMatchingStatement()) {
            Statement matchingSt = sp.getMatchingStatement();
            Statement statement = litPrg.getStatement();

            if(matchingSt.getClass() != statement.getClass())
                throw new UnificationException("Incomparable types of statements", matchingSt, statement);

            // special case AssignmentStatements: if there is an "Update" identifier 
            if(matchingSt instanceof AssignmentStatement) {
                AssignmentStatement assignmentSt = (AssignmentStatement) matchingSt;
                if(assignmentSt.isSchematic()) {
                    // type safe because of above getClass -check
                    AssignmentStatement otherAss = (AssignmentStatement)statement;
                    Update upd = new Update(otherAss.getAssignments()); 
                    termUnification.addUpdateInstantiation(assignmentSt.getSchemaIdentifier(), upd);
                    return;
                }
            } 

            // other cases
            if(matchingSt.countSubterms() != statement.countSubterms())
                throw new UnificationException("Incomparable count of subterms in statements", matchingSt, statement);

            List<Term> matchingSubterms = matchingSt.getSubterms();
            List<Term> subterms = statement.getSubterms();
            for (int i = 0; i < matchingSubterms.size(); i++) {
                compare(matchingSubterms.get(i), subterms.get(i));
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
    }

    @Override 
    public void visit(Binding b1) throws TermException {
        Binding b2 = (Binding) compareTerm;
        
        // binders are identical, not only equal
        if(b1.getBinder() != b2.getBinder())
            throw new UnificationException(b1, b2);
        
        b1.getVariableType().accept(typeMatchVisitor, b2.getVariableType());
        
        if(b1.hasSchemaVariable()) {
            // rhs may not contain schema stuff
            assert !b2.hasSchemaVariable();
            termUnification.addInstantiation(
                    SchemaVariable.getInst(b1.getVariableName(), b1.getVariableType()), 
                    Variable.getInst(b2.getVariableName(), b2.getVariableType()));
        } else if(!b1.getVariableName().equals(b2.getVariableName())) {
            throw new UnificationException("Different variable", b1, b2);
        }
        
        defaultVisitTerm(b1);
    }
    
    @Override
    public void visit(TypeVariableBinding tyvarBinding)
            throws TermException {

        TypeVariableBinding otherBinding = (TypeVariableBinding) compareTerm;
        
        if(tyvarBinding.getKind() != otherBinding.getKind()) {
            throw new UnificationException(tyvarBinding, otherBinding);
        }
        
        Type boundType = tyvarBinding.getBoundType();
        boundType.accept(typeMatchVisitor, otherBinding.getBoundType());
        
        if (boundType instanceof SchemaType) {
            SchemaType schemaType = (SchemaType) boundType;
            termUnification.addBoundSchemaType(schemaType);            
        }
        
        defaultVisitTerm(tyvarBinding);
    }

    @Override 
    public void visit(Application a1) throws TermException {
        Application a2 = (Application) compareTerm;
        
        // there is only one function symbols, check via ==
        if(a1.getFunction() != a2.getFunction())
            throw new UnificationException(a1, a2);
        
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
