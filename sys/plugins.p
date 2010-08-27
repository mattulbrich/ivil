#
# This file is part of This file is part of
#    ivil - Interactive Verification on Intermediate Language
#
# Copyright (C) 2009 Universitaet Karlsruhe, Germany
#    written by Mattias Ulbrich
#
# The system is protected by the GNU General Public License.
# See LICENSE.TXT for details.
#

(*
 * This file contains the basic plugins for ivil:
 *  - where conditions for propositional parts
 *  - where conditions for first order parts
 *  - basic meta functions 
 *)

plugin
    (*
     * propsitional and general plugins
     *)
    # mark a schema variable as interactive
    whereCondition : "de.uka.iti.pseudo.rule.where.Interactive"

    # check whether the find selector does not coincide
    # with an assumption selector
    whereCondition : "de.uka.iti.pseudo.rule.where.DistinctAssumeAndFind"

    (* 
     * First order plugins 
     *)
    # general substitution
    metaFunction : "de.uka.iti.pseudo.rule.meta.SubstMetaFunction"

    # general substitution
    metaFunction : "de.uka.iti.pseudo.rule.meta.SpecialiseMetaFunction"

    # skolem symbol generation
    metaFunction : "de.uka.iti.pseudo.rule.meta.SkolemMetaFunction"

    # translate an equality of unique terms
    metaFunction : "de.uka.iti.pseudo.rule.meta.ResolveUniqueMetaFunction"

    # check whether a variable appears free in a term
    whereCondition : "de.uka.iti.pseudo.rule.where.NotFreeIn"

    # check whether a variable appears free in a term
    whereCondition : "de.uka.iti.pseudo.rule.where.NoFreeVars"

    # check whether two terms have different types
    whereCondition : "de.uka.iti.pseudo.rule.where.DifferentTypes"

    # check whether a term has a unique top level function symbol
    whereCondition : "de.uka.iti.pseudo.rule.where.IsUnique"

    #check whether a term using meta functions can be evaluated
    whereCondition : "de.uka.iti.pseudo.rule.where.CanEvaluateMeta"

    # check whether two terms have different types
    whereCondition : "de.uka.iti.pseudo.rule.where.DifferentTypes"

    # check whether the find selector is not whitin a modality
    # or in the range of a modality term
    whereCondition : "de.uka.iti.pseudo.rule.where.TopLevel"

    # check whether the formula to be added is indeed an axiom
    whereCondition : "de.uka.iti.pseudo.rule.where.AxiomCondition"


(*
 * This rule can -- in combination with the where condition axiom
 * be used to add axioms from the environment to the sequent.
 * When this rule is to be applied, the property "axiomName" has
 * to be set on the RuleApplication to the name of the axiom to
 * add. %b is then instantiated accordingly by the where condition.
 *)
rule `axiom`
    where `axiom` %b
    add %b |-
    tags autoonly
         display "Insert axiom {property axiomName}"
