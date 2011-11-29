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
 * You find optimised rules in set.p .
 *)

include "$fol.p"

sort
  set('a)
  rel('a, 'b)

function
  bool      $mem('a, set('a))            infix ::  50
  set('a)   $union(set('a), set('a))     infix \/  53
  set('a)   $intersect(set('a), set('a)) infix /\  55
  set('a)   $diff(set('a), set('a))      infix \   55
  bool      $subset(set('a), set('a))    infix <:  50
  set('a)   $complement(set('a))         prefix ^  80
  set('a)   emptyset
  set('a)   singleton('a)
  
function    (* Finiteness and cardinality *)
  set('a)   fullset
  bool      finite(set('a))
  int       card(set('a))

binder
  set('a)   (\set 'a; bool)

axiom emptyset_definition
  (\T_all 'a; (\forall x as 'a; !x::emptyset))

axiom singleton_definition
  (\T_all 'a; (\forall x as 'a; (\forall y as 'a;
    x :: singleton(y) <-> x=y)))

axiom complement_definition
  (\T_all 'a; (\forall s as set('a); (\forall x as 'a;
    x :: ^s <-> !x :: s)))

axiom diff_definition
  (\T_all 'a; (\forall x as 'a; (\forall a as set('a); (\forall b as set('a);
    x :: a \ b <-> x::a & !x::b))))

axiom intersect_definition
  (\T_all 'a; (\forall x as 'a; (\forall a as set('a); (\forall b as set('a);
    x :: a /\ b <-> x::a & x::b))))

axiom union_definition
  (\T_all 'a; (\forall x as 'a; (\forall a as set('a); (\forall b as set('a);
    x :: a \/ b <-> x::a | x::b))))

axiom subset_definition
  (\T_all 'a; (\forall a as set('a); (\forall b as set('a);
    a <: b <-> (\forall x; x::a -> x::b))))

axiom set_equality_definition
  (\T_all 'a; (\forall a as set('a); (\forall b as set('a);
    a = b <-> (\forall x; x::a <-> x::b))))
    