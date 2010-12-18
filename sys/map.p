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
 * This file contains an implementation and rules for maps.
 * Maps of higher dimensionality can be created using map_pair.
 *)

# TODO make a distinct %a %b where clause, to improve automated handling of maps
 
include 
  "$base.p"

  
sort
  map('d, 'r)
  map_pair('a, 'b)


function
  map('d, 'r) map_store(map('d, 'r), 'd, 'r)
  'r map_load(map('d, 'r), 'd)
  map_pair('a, 'b) map_pair('a, 'b)


rule map_load_store_same
  find map_load(map_store(%m, %d, %v), %d)
  replace %v
  tags rewrite "concrete"

rule map_load_store_other
  assume |- %d1 = %d2
  find map_load(map_store(%m, %d1, %v), %d2)
  replace map_load(%m, %d2)
  tags rewrite "concrete"


rule map_load_store_other_cut
  find map_load(map_store(%m, %d1, %v), %d2)
  samegoal "Ignore store at {%d1}"
  	replace map_load(%m, %d2)
  	
  samegoal "Ensure {%d1} != {%d2}"
     add |- %d1 = %d2
