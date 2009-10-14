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
  "$int.p"
  "$symbex.p"
  "$reftypes.p"

function
  heap h assignable
  heap h_pre assignable
  heap anon assignable
  stack st assignable

  field(int) length
  field(bool) created

  int intRet assignable
  bool boolRet assignable
  ref refRet assignable

  ref exc assignable
  ref new assignable