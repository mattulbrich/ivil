#
# This file is part of PSEUDO
# Copyright (C) 2009 Universitaet Karlsruhe, Germany
#    written by Mattias Ulbrich
#
# The system is protected by the GNU General Public License.
# See LICENSE.TXT for details.
#

(*
 * This file contains infrastructure for bytecode treatment.
 *)

include
   "$heap.p"
   "$stack.p"

function
  heap h assignable
  stack st assignable

  field(int) length
  field(bool) created

  ref exc
  ref new