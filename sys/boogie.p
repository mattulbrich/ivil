#
# This file is part of This file is part of
#    ivil - Interactive Verification on Intermediate Language
#
# Copyright (C) 2010 Universitaet Karlsruhe, Germany
#    written by Timm Felden
#
# The system is protected by the GNU General Public License.
# See LICENSE.TXT for details.
#

(*
 * This file contains the very basic definitions needed for treatment of boogie
 * files.
 *)
 
include 
  "$base.p"
  "$int.p"
  "$symbex.p"
  "$decproc.p"
  "$unicode.p"
  "$fol.p"
  "$maps.p"


(*  Dont use prettyprinter.
 * plugin
 *   prettyPrinter : "de.uka.iti.pseudo.parser.boogie.environment.BoogiePrettyPrinter"
 *)
 
(*
 * Toplevel and right will be processed automatically.
 *)
    
rule toplevel_and_right
  find |-  %a & %b 
  where 
    toplevel
  samegoal "Conj1: {%a}"
    replace  %a 
  samegoal "Conj2: {%b}"
    replace  %b 
  tags rewrite "prop simp"
