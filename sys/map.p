#
# This file is part of This file is part of
#    ivil - Interactive Verification on Intermediate Language
#
# Copyright (C) 2009-2011 Universitaet Karlsruhe, Germany
#    written by Timm Felden, Mattias Ulbrich
#
# The system is protected by the GNU General Public License.
# See LICENSE.TXT for details.
#

(*
 * This file contains an implementation of one dimensional maps and lambdas,
 * that can be chained to mimic maps of higher dimension.
 *)
 
include 
  "$base.p"

include "$fol.p"

plugin
  prettyPrinter : "test.ArrayPrettyPrinter"

sort
  map('d, 'r)
  

function
  map('d, 'r) $store(map('d, 'r), 'd, 'r)
  'r $load(map('d, 'r), 'd)

#this rule can cause a split...
rule map_load_store
  find $load($store(%m, %d1, %v), %d2)
  replace cond(%d1 = %d2, %v, $load(%m, %d2))
  tags rewrite "split"
  
# ...so rules were added for cases where no split occurs
rule map_load_store_same
  find $load($store(%m, %d, %v), %d)
  replace %v
  tags rewrite "concrete"
  
rule map_load_store_same_assume
  find $load($store(%m, %d, %v), %d2)
  assume %d = %d2 |-
  replace %v
  tags rewrite "concrete"

rule map_load_store_other
  find $load($store(%m, %d1, %v), %d2)
  assume |- %d1 = %d2
  replace $load(%m, %d2)
  tags rewrite "concrete"
  
rule map_load_store_other_reverse
  find $load($store(%m, %d1, %v), %d2)
  assume |- %d2 = %d1
  replace $load(%m, %d2)
  tags rewrite "concrete"
  

binder
  map('d, 'r) (\lambda 'd; 'r)

rule map_lambda
  find $load((\lambda %x; %e), %y)
  replace $$subst(%x, %y, %e)
  tags rewrite "concrete"
  
rule map_lambda_assume
  find $load(%m, %y)
  assume %m = (\lambda %x; %e) |-
  replace $$subst(%x, %y, %e)
  tags rewrite "concrete"
