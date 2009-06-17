#
# This file is part of PSEUDO
# Copyright (C) 2009 Universitaet Karlsruhe, Germany
#    written by Mattias Ulbrich
#
# The system is protected by the GNU General Public License.
# See LICENSE.TXT for details.
#

(*
 * This file contains rules for first order logic
 *)

include
   "$proposition.p"

rule forall_right
  find  |-  (\forall %x as 'a; %b) 
  replace  $$subst(%x, $$skolem(%x), %b) 

rule exists_right
  find  |-  (\exists %x as 'a; %b) 
  where
    interact %inst as 'a
  replace  $$subst(%x, %inst, %b) 

rule forall_left
  find  (\forall %x as 'a; %b)  |-
  where
    interact %inst as 'a
  replace  $$subst(%x, %inst, %b) 

rule exists_left
  find   (\exists %x as 'a; %b)  |-
  replace  $$subst(%x, $$skolem(%x), %b) 

rule equality
  find  %t = %t 
  replace  true 

rule equality_comm
  find  %t = %u 
  replace  %u = %t 

rule eq_apply
  find  %t 
  assume  %t = %u  |-
  where
    toplevel
  replace   %u 

rule cond_true
  find  cond(true, %a, %b) 
  replace  %a 

rule cond_false
  find  cond(false, %a, %b) 
  replace  %b 
