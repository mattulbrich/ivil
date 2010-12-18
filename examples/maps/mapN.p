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
 * This file contains an implementation and rules for polymorphic maps.
 * Only one dimensional maps are defined here, but you can chain them
 * to get any dimension you want.
 *)
 
include 
  "$base.p"

  
sort
  map('d1, 'd2, 'r)
  

function
  map('d1, 'd2, 'r) store(map('d1, 'd2, 'r), 'd1, 'd2, 'r)
  'r load(map('d1, 'd2, 'r), 'd1, 'd2)


rule map_load_store_same
  find load(store(%m, %d, %d2, %v), %d, %d2)
  replace %v
  tags rewrite "concrete"

rule map_load_store_other
  find load(store(%m, %d1, %d2, %v), %d3, %d4)
  replace load(%m, %d3, %d4)
  tags rewrite "concrete"


