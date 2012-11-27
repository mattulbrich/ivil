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
  "$int.p"
  "$symbex.p"
  "$decproc.p"
  "$reftypes.p"
  "$refinement.p"  (* needed for skip MARK constant *)
  

function
  heap h assignable
  heap ho assignable
  heap pre_h assignable
  heap anon assignable
  ref _this assignable

  int resInt assignable
  bool resBool assignable
  ref resRef assignable

  int intRet assignable
  bool boolRet assignable
  ref refRet assignable

  ref exc assignable
  ref new assignable

  # todo find a way to do this somewhat more flexible
  int decrBase assignable

  set(ref) freshObjects(heap)

rule freshObjects_def
  find %x :: freshObjects(%h)
  replace !%h[%x, created]
  tags
    asAxiom
    rewrite "fol simp"
