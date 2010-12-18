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
  map('D, 'r)
  pair('a, 'b)
  

function
  map('D, 'r) store(map('D, 'r), 'D, 'r)
  'r load(map('D, 'r), 'D)
  pair('a, 'b) pair('a, 'b)


rule map_load_store_same
  find load(store(%m, %d, %v), %d)
  replace %v
  tags rewrite "concrete"

rule map_load_store_other
  find load(store(%m, %d1, %v), %d2)
  replace load(%m, %d2)
  tags rewrite "concrete"


