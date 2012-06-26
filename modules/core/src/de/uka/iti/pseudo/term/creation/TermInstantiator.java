/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2012 Karlsruhe Institute of Technology
 *
 * The system is protected by the GNU General Public License.
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.term.creation;

import java.util.List;
import java.util.Map;

import nonnull.NonNull;
import de.uka.iti.pseudo.environment.Program;
import de.uka.iti.pseudo.term.Binding;
import de.uka.iti.pseudo.term.LiteralProgramTerm;
import de.uka.iti.pseudo.term.Modality;
import de.uka.iti.pseudo.term.SchemaProgramTerm;
import de.uka.iti.pseudo.term.SchemaType;
import de.uka.iti.pseudo.term.SchemaUpdateTerm;
import de.uka.iti.pseudo.term.SchemaVariable;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.TermException;
import de.uka.iti.pseudo.term.Type;
import de.uka.iti.pseudo.term.TypeVisitor;
import de.uka.iti.pseudo.term.UnificationException;
import de.uka.iti.pseudo.term.Update;
import de.uka.iti.pseudo.term.UpdateTerm;
import de.uka.iti.pseudo.term.statement.Assignment;
import de.uka.iti.pseudo.util.Util;

// Instantiation is not applied on the instantiated terms or types.
/**
 * The TermInstantiator replaces schematic entities in terms by concrete
 * counterparts.
 *
 * The assignments are stored in three Maps.
 *
 * <h4>Usage</h4>
 *
 * You should always use {@link #instantiate(Term)} and
 * {@link #instantiate(Type)} instead of the visiting types and terms with this
 * object.
 */
@SuppressWarnings({"nullness"})
public class TermInstantiator extends RebuildingTermVisitor {

    /**
     * The map from schema vars to terms.
     */
    private final Map<String, Term> termMap;

    /**
     * The map from schema vars to types.
     */
    private final Map<String, Type> typeMap;

    /**
     * The map from schema updates to updates.
     */
    private final Map<String, Update> updateMap;

    /**
     * Marker used by {@link #typeInstantiator} to indicate that schematic types
     * have been instantiated.
     */
    private boolean typesInstantiated;

    /**
     * Instantiator for schematic types. If the resulting type is modified, the
     * marker {@link #typesInstantiated} is set to <code>true</code>.
     */
    private final TypeVisitor<Type, Void> typeInstantiator = new RebuildingTypeVisitor<Void>() {
        @Override
        public Type visit(SchemaType stv, Void arg) throws TermException {
            Type t = typeMap.get(stv.getVariableName());
            if(t == null) {
                return stv;
            } else {
                typesInstantiated = true;
                return t;
            }
        }
    };

    /**
     * Instantiates a new term instantiator from three maps.
     *
     * @param termMap
     *            the map from schema to terms
     * @param typeMap
     *            the map from schema to type
     * @param updateMap
     *            the map from schema to update
     */
    public TermInstantiator(
            @NonNull Map<String, Term> termMap,
            @NonNull Map<String, Type> typeMap,
            @NonNull Map<String, Update> updateMap) {
        this.termMap = termMap;
        this.typeMap = typeMap;
        this.updateMap = updateMap;
    }

    /**
     * Instantiate schematic entities in a term.
     *
     * @param toInst
     *            the term to instantiate
     * @return the instantiated version of the argument term
     * @throws TermException
     *             if visitation fails (for typing issuses e.g.)
     */
    public @NonNull Term instantiate(@NonNull Term toInst) throws TermException {
        toInst.visit(this);
        if (resultingTerm != null) {
            return resultingTerm;
        } else {
            return toInst;
        }
    }

    /**
     * Instantiate schematic types in a type.
     *
     * @param type
     *            the type to instantiate schema types in.
     * @return the instantiated type
     * @throws TermException
     *             never thrown
     */
    public Type instantiate(Type type) throws TermException {
        return modifyType(type);
    }

    @Override
    protected Type modifyType(Type type) throws TermException {
        if(!typeMap.isEmpty()) {
            typesInstantiated = false;
            Type newType = type.accept(typeInstantiator, null);
            if(typesInstantiated) {
                type = newType;
            }
        }

        return type;
    }

    @Override
    public void visit(SchemaVariable schemaVariable) throws TermException {
        // the schema variable might have to be retyped
        // resultingTerm holds retyped variable then
        super.visit(schemaVariable);

        Term t  = termMap.get(schemaVariable.getName());

        if(t != null) {
            Type t1 = modifyType(t.getType());
            Type t2 = modifyType(schemaVariable.getType());
            if(!t1.equals(t2)) {
                throw new UnificationException("Instantiation failed! Incompatible types", t1, t2);
            }
            resultingTerm = t;
        }
    }

    @Override
    public void visit(SchemaUpdateTerm schemaUpdateTerm) throws TermException {

        String schemaIdentifier = schemaUpdateTerm.getSchemaIdentifier();

        schemaUpdateTerm.getSubterm(0).visit(this);

        if(resultingTerm == null) {
            Update resultingUpdate = updateMap.get(schemaIdentifier);
            if(resultingUpdate == null) {
                // remains null: resultingTerm = null;
            } else if(resultingUpdate == Update.EMPTY_UPDATE) {
                resultingTerm = schemaUpdateTerm.getSubterm(0);
            } else {
                resultingTerm = UpdateTerm.getInst(resultingUpdate, schemaUpdateTerm.getSubterm(0));
            }
        } else {
            Update resultingUpdate = updateMap.get(schemaIdentifier);
            if(resultingUpdate == null) {
                boolean optional = schemaUpdateTerm.isOptional();
                resultingTerm = SchemaUpdateTerm.getInst(schemaIdentifier, optional, resultingTerm);
            } else if(resultingUpdate == Update.EMPTY_UPDATE) {
                // resultingTerm remains the result of the subterm
            } else {
                resultingTerm = UpdateTerm.getInst(resultingUpdate, resultingTerm);
            }
        }
    }

    @Override
    public void visit(SchemaProgramTerm schemaProgramTerm) throws TermException {
        super.visit(schemaProgramTerm);
        if(resultingTerm != null) {
            // this is guaranteed to be a schema program term here.
            schemaProgramTerm = (SchemaProgramTerm) resultingTerm;
        }

        if(termMap != null) {
            SchemaVariable schemaVariable = schemaProgramTerm.getSchemaVariable();
            Term t  = termMap.get(schemaVariable.getName());
            if (!(t instanceof LiteralProgramTerm)) {
                throw new TermException("Tried to instantiate a schema program term " +
                        "with a non-program term: " + t);
            }

            LiteralProgramTerm litProgTerm = (LiteralProgramTerm) t;

            checkSchemaProgramInstantiation(schemaProgramTerm, litProgTerm);

            // take index and program from literal prog term,
            Program program = litProgTerm.getProgram();
            int index = litProgTerm.getProgramIndex();
            // take suffix from schema program term
            Term suffixTerm = schemaProgramTerm.getSuffixTerm();
            // take modality from schema program or from instantiation if ANY
            Modality modality = schemaProgramTerm.getModality();
            if(modality == Modality.ANY) {
                modality = litProgTerm.getModality();
            }

            resultingTerm = LiteralProgramTerm.getInst(index, modality,
                    program, suffixTerm);
        }

    }

    /**
     * Check the instantiation of a schema program term.
     *
     * If the method returns without exception, the schema and the literal
     * program term are compatible.
     *
     * In this class, matching statements are not allowed. Subclasses may choose
     * to override this behaviour.
     *
     * @param schemaProgramTerm
     *            a schematic program term to match with
     * @param litProgTerm
     *            a literal program term to match against.
     *
     * @throws TermException
     *             indicates that the matching term is inappropriate.
     */
    protected void checkSchemaProgramInstantiation(
            SchemaProgramTerm schemaProgramTerm, LiteralProgramTerm litProgTerm)
                    throws TermException {

        if(schemaProgramTerm.hasMatchingStatement()) {
            throw new TermException("Tried to instantiate a schema program term " +
                    "with matching statement: " + schemaProgramTerm);
        }
    }

    /*
     * we need to handle this separately since the bound variable may be instantiated.
     */
    @Override
    protected void visitBindingVariable(Binding binding) throws TermException {
        binding.getVariable().visit(this);
    }

    /*
     * we need to handle this separately since the element to which sth is assigned
     * may be instantiated too.
     */
    @Override
    public void visit(UpdateTerm updateTerm) throws TermException {

        updateTerm.getSubterm(0).visit(this);
        Term innerResult = resultingTerm != null ?
                resultingTerm : updateTerm.getSubterm(0);

        Assignment[] newAssignments = null;
        List<Assignment> assignments = updateTerm.getAssignments();

        for(int i = 0; i < assignments.size(); i++) {
            Assignment assignment = assignments.get(i);

            assignment.getTarget().visit(this);
            Term tgt = resultingTerm;

            assignment.getValue().visit(this);
            Term val = resultingTerm;

            if(tgt != null || val != null) {
                // restore target if visitation returned null
                if(tgt == null) {
                    tgt = assignment.getTarget();
                }

                // restore value if visitation returned null
                if(val == null) {
                    val = assignment.getValue();
                }

                if(newAssignments == null) {
                    newAssignments = Util.listToArray(assignments, Assignment.class);
                }
                newAssignments[i] = new Assignment(tgt, val);
            }
        }

        if(newAssignments != null) {
            resultingTerm = UpdateTerm.getInst(new Update(newAssignments), innerResult);
        } else if(innerResult != updateTerm.getSubterm(0)) {
            newAssignments = Util.listToArray(assignments, Assignment.class);
            resultingTerm = UpdateTerm.getInst(new Update(newAssignments), innerResult);
        } else {
            resultingTerm = null;
        }
    }

    @Override
    public String toString() {
        return "TermInstantiator[terms=" + termMap + "; types=" + typeMap
                + "; updates=" + updateMap + "]";
    }
}
