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

include
  "$int.p"


sort
  bitvector

function
# bitvectors are stored in $bv_new as (value, dimension)
  bitvector $bv_new(int, int)
  
  bitvector $bv_concat(bitvector, bitvector)
  bitvector $bv_select(bitvector, int, int)

  int $bv_toInt(bitvector)


rule bv_concat
  find $bv_concat($bv_new(%v1, %d1), $bv_new(%v2, %d2))
  replace $bv_new(%v1*2^%d2 + %v2, %d1 + %d2)
  tags rewrite "fol simp"

rule bv_select
  find $bv_select($bv_new(%v, %d), %u, %l)
  replace $bv_new($mod((%v >>> %l), %u-%l), %u-%l)
  tags rewrite "fol simp"

rule bv_toInt
  find $bv_toInt($bv_new(%v, %d))
  replace %v
