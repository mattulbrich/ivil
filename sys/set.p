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
 * This files contains the definitions for the set datatype
 *
 * You will find optimised rules in .... .
 *)

include "$fol.p"

sort
  set('a)

function
  bool      mem('a, set('a))
  set('a)   union(set('a), set('a))
  set('a)   diff(set('a), set('a))
  bool      subset(set('a), set('a))
  set('a)   emptyset