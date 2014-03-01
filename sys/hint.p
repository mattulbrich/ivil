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
 * This file contains the proof hint plugins for ivil:
 * It also contains the rules which go along them.
 *
 * Other rules for hints are defined in plugins.p
 *)

(*
 * You propably need:
 * properties
 * CompoundStrategy.strategies 
 *   "HintStrategy,SimplificationStrategy,BreakpointStrategy,SMTStrategy"
 *)

plugins
 
    # proof hint where a rule application is given like §(rule ruleName)
    proofHint "de.uka.iti.pseudo.auto.strategy.hint.RuleProofHint"

    # proof hint where an axiom is introduced like §(axiom axiomName)
    proofHint "de.uka.iti.pseudo.auto.strategy.hint.AxiomProofHint"


    # proof hint to perform a case distinction like §(cut 'a > 0')
    proofHint "de.uka.iti.pseudo.auto.strategy.hint.CutProofHint"

    # proof hint to perform pick formulas like §(pick A.0 A.1 S.2)
    proofHint "de.uka.iti.pseudo.auto.strategy.hint.PickProofHint"

    # proof hint to perform focus on last formula like §(focus)
    proofHint "de.uka.iti.pseudo.auto.strategy.hint.FocusProofHint"

    # proof hint to perform focus on last formula like §(expand funct_def 2)
    proofHint "de.uka.iti.pseudo.auto.strategy.hint.ExpandProofHint"

    # proof hint to perform instantiation on a gamma formula like
    # §(inst j with 2) or §(inst '(\forall x.x*x>=0)' with 2)
    proofHint "de.uka.iti.pseudo.auto.strategy.hint.InstantiateProofHint"
   
    # proof hint to perform instantiation on last formula like §(witness 'x1 + 1')
    # performs update simplification
    proofHint "de.uka.iti.pseudo.auto.strategy.hint.WitnessProofHint"
 (*

 * "Focus" reduces the sequent to one single formula,
 * usually used to remove context knowledge to concentrate
 * on one aspect. There is an according hint.
 *
 * One of the few newgoal rules.
 *)
rule focus_right
  find |- %b
  newgoal add |- %b

rule focus_left
  find %b |-
  newgoal add %b |-
