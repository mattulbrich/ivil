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

rule close_by_Z3
  find %b
    where askDecisionProcedure
  closegoal
  tags decisionProcedure "de.uka.iti.pseudo.auto.Z3SMT"
  