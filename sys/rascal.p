#
# This file is part of PSEUDO
# Copyright (C) 2009 Universitaet Karlsruhe, Germany
#    written by Mattias Ulbrich
#
# The system is protected by the GNU General Public License.
# See LICENSE.TXT for details.
#

(*
 * This file contains the definitions needed to parse 
 * compiled rascal files
 *)

include 
  "$base.p"
  "$fol.p"
  "$int.p"
  "$heap.p"

(* function that are always present *)
function 
  heap h assignable
  bool cnd assignable