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
  map('a, 'b)

function
  'b read(map('a, 'b), 'a)
  map('a,'b) write(map('a,'b), 'a, 'b)

rule readwrite
  find read(write(%m, %a1, %b), %a2)
  replace cond(%a1 = %a2, %b, read(%m, %a2))