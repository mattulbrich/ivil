#
# This file is part of PSEUDO
# Copyright (C) 2009 Universitaet Karlsruhe, Germany
#    written by Mattias Ulbrich
#
# The system is protected by the GNU General Public License.
# See LICENSE.TXT for details.
#

(*
 * This file contains rules to contact decision procedures.
 *)

plugin 
  # ask a decision procedure.
  whereCondition : "de.uka.iti.pseudo.rule.where.AskDecisionProcedure"

(*
 * The rule auto_smt_close is special since it is the one used
 * for the background smt solver
 * The solver and the timeout are read from this rule
 *)
rule auto_smt_close
    where askDecisionProcedure
  closegoal
  tags 
    decisionProcedure "de.uka.iti.pseudo.auto.Z3SMT"
    timeout "1000"
  
