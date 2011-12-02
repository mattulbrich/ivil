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

    # skolem symbol generation
    metaFunction : "de.uka.iti.pseudo.rule.meta.SkolemTypeMetaFunction"
    
    # translate an equality of unique terms
    metaFunction : "de.uka.iti.pseudo.rule.meta.ResolveUniqueMetaFunction"

    # check whether a variable appears free in a term
    # deprecated? and not implemented
    # whereCondition : "de.uka.iti.pseudo.rule.where.NotFreeIn"

    # check whether no free var appears in a term
    whereCondition : "de.uka.iti.pseudo.rule.where.NoFreeVars"

    # check whether a variable is fresh for a term (or more terms)
    whereCondition : "de.uka.iti.pseudo.rule.where.FreshVariable"

    # check whether two terms have different types
    whereCondition : "de.uka.iti.pseudo.rule.where.DifferentTypes"

    # check whether a term has a unique top level function symbol
    whereCondition : "de.uka.iti.pseudo.rule.where.IsUnique"

    #check whether a term using meta functions can be evaluated
    whereCondition : "de.uka.iti.pseudo.rule.where.CanEvaluateMeta"

    # check whether two terms have different types
    whereCondition : "de.uka.iti.pseudo.rule.where.DifferentTypes"
    
    # check whether two terms have different types, but returns false if
    #  typevariables are present
    whereCondition : "de.uka.iti.pseudo.rule.where.DifferentGroundTypes"
    
    # check whether the find selector is not whitin a modality
    #  or in the range of a modality term
    whereCondition : "de.uka.iti.pseudo.rule.where.TopLevel"

    # check whether the arguments are present in the sequent
    whereCondition : "de.uka.iti.pseudo.rule.where.PresentInAntecedent"
    whereCondition : "de.uka.iti.pseudo.rule.where.PresentInSuccedent"
    whereCondition : "de.uka.iti.pseudo.rule.where.PresentInSequent"
        
    # check whether the formula to be added is indeed an axiom
    whereCondition : "de.uka.iti.pseudo.rule.where.AxiomCondition"
    
    # check whether two terms are not yet in ordered form
    whereCondition : "de.uka.iti.pseudo.rule.where.UnorderedTerms"

    # check whether the formula to be added is indeed known and legal
    whereCondition : "de.uka.iti.pseudo.rule.where.KnownFormula"
    
    # pretty printing for maps
    prettyPrinter : "de.uka.iti.pseudo.prettyprint.plugin.MapPrettyPrinter"
    
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
         
         
(*
 * These rules are used to unhide hidden terms. To use these rules, the unhide
 * term action has to be used (accessible over f9 or rightclick on term).
 *)
rule unhide_left
  where knownFormula %b, %LEFT
  add %b |-
  tags autoonly
       display "Unhide formula from {property knownFormula}"
       
rule unhide_right
  where knownFormula %b, %RIGHT
  add |- %b
  tags autoonly
       display "Unhide formula from {property knownFormula}"
