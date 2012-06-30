#
# This file is part of This file is part of
#    ivil - Interactive Verification on Intermediate Language
#
# Copyright (C) 2009-2012 Karlsruhe Institute of Technology
#
# The system is protected by the GNU General Public License.
# See LICENSE.TXT for details.
#

(*
 * This files contains the definitions for the sequence datatype
 *
 * You find optimised rules in seq.p.
 *)

include "$int.p"
        
sort
  seq('a)

function
  'a seqGet(seq('a), int)
  int seqLen(seq('a))
  int seqIndexOf(seq('a), 'a)
  'a seqError
  
binder
  (* primary constructor *)
  seq('a) (\seqDef int; int; int; 'a)

function
  (* secondary constructors *)
  seq('a) seqEmpty
  seq('a) seqSingleton('a)
  seq('a) seqConcat(seq('a), seq('a))
  seq('a) seqSub(seq('a), int, int)
  seq('a) seqReverse(seq('a))


(*
 * Axioms
 *)

rule seqGetDef
  find seqGet((\seqDef %i; %a; %b; %t), %j)
  replace cond(0 <= %j & %j < %b-%a, 
           $$subst(%i, %j+%a, %t),
           seqError)
  tags
    rewrite "fol simp"

rule seqLenDef
  find seqLen((\seqDef %i; %a; %b; %t))
  replace cond(%a <= %b, %b-%a, 0)
  tags
    rewrite "fol simp"
    
axiom seqLenNonNeg
  (\T_all 'a; (\forall s as seq('a); seqLen(s) >= 0))

rule seqExtensionality
  find %s1 = %s2
  where freshVar %i, %s1, %s2
  replace seqLen(%s1) = seqLen(%s2) &
    (\forall %i; 0<=%i & %i < seqLen(%s1) 
        -> seqGet(%s1,%i) = seqGet(%s2,%i))

(*
 * derived constructors,
 * definitorial extensions
 *)
 
rule seqEmptyDef
  find seqEmpty as seq(%'a)
  replace (\seqDef x; 0; 0; seqError as %'a)

rule seqSingletonDef
  find seqSingleton(%val)
  where freshVar %i, %val
  replace (\seqDef %i; 0; 1; %val)

rule seqSubDef
  find seqSub(%a, %from, %to)
  where freshVar %x, %from, %to, %a
  replace (\seqDef %x; %from; %to; seqGet(%a, %x))

rule seqConcatDef
  find seqConcat(%a, %b)
  where freshVar %x, %a, %b
  replace (\seqDef %x; 0; seqLen(%a) + seqLen(%b); 
            cond(%x < seqLen(%a), seqGet(%a, %x), 
                                  seqGet(%b, %x-seqLen(%a))))

rule seqReverseDef
  find seqReverse(%a)
  where freshVar %x, %a
  replace (\seqDef %x; 0; seqLen(%a); seqGet(%a, seqLen(%a) - 1 - %x))
