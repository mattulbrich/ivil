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
import java.util.Map;

import nonnull.NonNull;
import de.uka.iti.pseudo.prettyprint.PrettyPrint;
import de.uka.iti.pseudo.term.Binding;
import de.uka.iti.pseudo.term.LiteralProgramTerm;
import de.uka.iti.pseudo.term.SchemaProgramTerm;
import de.uka.iti.pseudo.term.SchemaUpdateTerm;
import de.uka.iti.pseudo.term.SchemaVariable;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.TermException;
import de.uka.iti.pseudo.term.Type;
import de.uka.iti.pseudo.term.TypeVariable;
import de.uka.iti.pseudo.term.TypeVisitor;
import de.uka.iti.pseudo.term.UnificationException;
import de.uka.iti.pseudo.term.Update;
import de.uka.iti.pseudo.term.UpdateTerm;
import de.uka.iti.pseudo.term.statement.AssignmentStatement;
import de.uka.iti.pseudo.util.Util;
// TODO DOC
public class TermInstantiator extends RebuildingTermVisitor {

    private Map<String, Term> termMap;
    private Map<String, Type> typeMap;
    private Map<String, Update> updateMap;

    public TermInstantiator(Map<String, Term> termMap, Map<String, Type> typeMap, Map<String, Update> updateMap) {
        super();
        this.termMap = termMap;
        this.typeMap = typeMap;
        this.updateMap = updateMap;
    }

    public Term instantiate(Term toInst) throws TermException {
        toInst.visit(this);
        if(resultingTerm != null)
            return resultingTerm;
        else
            return toInst;
    }

    /**
     * Replace schema variables in a string.
     * 
     * <p>
     * For instance <code>Assume {%c} in {%a}</code> might become
     * <code>Assume $eq(x,3) in [4; P]</code>
     * 
     * <p>
     * Term{@link Term#toString())} is used to render the term.
     * 
     * @param string
     *            the string to instantiate
     * 
     * @return the string with schema variables replaced
     */
    public @NonNull String replaceInString(@NonNull String string) {
        return replaceInString(string, null);
    }
    
    /**
     * Replace schema variables in a string.
     * 
     * <p>
     * For instance <code>Assume {%c} in {%a}</code> might become
     * <code>Assume x = 3 in [4; P]</code>
     * 
     * <p>
     * The provided pretty printer is used to render the term. If it is
     * <code>null</code>, defaults to {@link #replaceInString(String)}.
     * 
     * @param pp
     *            pretty printer to render the instantiations
     * @param string
     *            the string to instantiate
     * 
     * @return the string with schema variables replaced
     */
    public @NonNull String replaceInString(@NonNull String string, PrettyPrint pp) {
        
        StringBuilder retval = new StringBuilder();
        StringBuilder curley = new StringBuilder();
        
        int len = string.length();
        
        boolean inCurley = false;
        for (int i = 0; i < len; i++) {
            char c = string.charAt(i);
            switch(c) {
            case '{':
                inCurley = true;
                break;
                
            case '}':
                String lookup = curley.toString();
                String display = "??";
                if(lookup.charAt(0) == '%') {
                    Term t = termMap.get(lookup);
                    if(t != null) {
                        if(pp == null) {
                            display = t.toString();
                        } else {
                            display = pp.print(t).toString();
                        }
                    }
                } else if(lookup.startsWith("explain %")) {
                    // retrieve explanation, overread "explain "
                    display = extractExplanation(termMap.get(lookup.substring(8)));
                }
                
                retval.append(display);
                
                inCurley = false;
                curley.setLength(0);
                break;
                
            default:
                if(inCurley)
                    curley.append(c);
                else
                    retval.append(c);
            }
        }
        
        return retval.toString();
    }
    
    private String extractExplanation(Term term) {
        String ret = null;
        if (term instanceof LiteralProgramTerm) {
            LiteralProgramTerm prog = (LiteralProgramTerm) term;
            int index = prog.getProgramIndex();
            ret = prog.getProgram().getTextAnnotation(index);
        }
        return ret == null ? "" : ret;
    }

    private boolean typesInstantiated;
    
    private TypeVisitor typeInstantiator = new DefaultTypeVisitor() {
        public Type visit(TypeVariable typeVariable) throws TermException {
            Type t = typeMap.get(typeVariable.getVariableName());
            if(t == null)
                return typeVariable;
            else {
                typesInstantiated = true;
                return t;
            }
        }
    };
    
    @Override
    protected Type modifyType(Type type) throws TermException {
        if(typeMap != null) {
            typesInstantiated = false;
            Type newType = type.visit(typeInstantiator);
            if(typesInstantiated)
                type = newType;
        }

        return type;
    }
    
    @Override
    public void visit(SchemaVariable schemaVariable) throws TermException {
        // the schema variable might have to be retyped
        super.visit(schemaVariable);
        
        if(termMap != null) {
            Term t  = termMap.get(schemaVariable.getName());
            
            if(t != null) {
                Type t1 = modifyType(t.getType());
                Type t2 = modifyType(schemaVariable.getType());
                if(!t1.equals(t2))
                    throw new UnificationException("Instantiation failed! Incompatible types", t1, t2);
                resultingTerm = t;
            }
            
        } else {
            resultingTerm = null;
        }
    }
    
    public void visit(SchemaUpdateTerm schemaUpdateTerm)
            throws TermException {
        
        String schemaIdentifier = schemaUpdateTerm.getSchemaIdentifier();
        
        schemaUpdateTerm.getSubterm(0).visit(this);
        
        if(resultingTerm == null) 
        {
            Update resultingUpdate = updateMap.get(schemaIdentifier);
            if(resultingUpdate != null) {
                resultingTerm = new UpdateTerm(resultingUpdate, schemaUpdateTerm.getSubterm(0));
            }
        } else {
            Update resultingUpdate = updateMap.get(schemaIdentifier);
            if(resultingUpdate == null) {
                resultingTerm = new SchemaUpdateTerm(schemaIdentifier, resultingTerm);
            } else {
                resultingTerm = new UpdateTerm(resultingUpdate, resultingTerm);
            }
        }
    }
    
    public void visit(SchemaProgramTerm schemaProgramTerm) throws TermException {
        // see above
        super.visit(schemaProgramTerm);
        
        if(termMap != null) {
            SchemaVariable schemaVariable = schemaProgramTerm.getSchemaVariable();
            Term t  = termMap.get(schemaVariable.getName());
            LiteralProgramTerm progTerm = (LiteralProgramTerm) t;
            
            checkSchemaProgramInstantiation(schemaProgramTerm, progTerm);
            resultingTerm = progTerm;
        }

    }

    /**
     * @param schema
     * @param prog
     * @throws TermException 
     */
    protected void checkSchemaProgramInstantiation(
            SchemaProgramTerm schema, LiteralProgramTerm prog)
            throws TermException {
        if(prog.isTerminating() != schema.isTerminating())
            throw new UnificationException("Instantiation failed! Termination incompatible", 
                    schema, prog);
    }
    
    /*
     * we need to handle this separately since the bound variable may be instantiated. 
     */
    protected void visitBindingVariable(Binding binding)
            throws TermException {
        binding.getVariable().visit(this);
    }
    
    /*
     * we need to handle this separately since the element to which sth is assigned 
     * may be instantiated too. 
     */
    public void visit(UpdateTerm updateTerm) throws TermException {

        updateTerm.getSubterm(0).visit(this);
        Term innerResult = resultingTerm != null ? 
                resultingTerm : updateTerm.getSubterm(0);
        
        AssignmentStatement newAssignments[] = null;
        List<AssignmentStatement> assignments = updateTerm.getAssignments();
        
        for(int i = 0; i < assignments.size(); i++) {
            
            assignments.get(i).getTarget().visit(this);
            Term tgt = resultingTerm;
            
            assignments.get(i).getValue().visit(this);
            Term val = resultingTerm;
            
            if(tgt != null || val != null) {
                if(newAssignments == null) {
                    newAssignments = Util.listToArray(assignments, AssignmentStatement.class);
                }
                newAssignments[i] = new AssignmentStatement(tgt, val);
            }
        }
        
        if(newAssignments != null) {
            resultingTerm = new UpdateTerm(new Update(newAssignments), innerResult);
        } else if(innerResult != updateTerm.getSubterm(0)) {
            newAssignments = Util.listToArray(assignments, AssignmentStatement.class);
            resultingTerm = new UpdateTerm(new Update(newAssignments), innerResult);
        } else {
            resultingTerm = null;
        }
    }
}
