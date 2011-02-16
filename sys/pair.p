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

include "$fol.p"
include "$set.p"

plugin
  prettyPrinter : "test.PairPrettyPrinter"

sort
  prod('a, 'b)

function
  prod('a,'b) pair('a,'b) unique
  'a fst(prod('a,'b))
  'b snd(prod('a,'b))

function
  set(prod('a, 'b)) $dom_restrict(set('a), set(prod('a,'b))) infix <| 80
  set(prod('a, 'b)) $rng_restrict(set(prod('a,'b)), set('b)) infix |> 80

axiom pair_constructor
  (\T_all 'a; (\T_all 'b; (\forall x as prod('a,'b); 
    (\exists y; (\exists z; x = pair(y,z))))))

axiom dom_restrict_definition
  (\T_all 'a; (\T_all 'b; (\forall r as set(prod('a,'b)); 
    (\forall s; (\forall x; (\forall y; 
      pair(x,y) :: s<|r <-> pair(x,y)::r & x :: s))))))


axiom rng_restrict_definition
  (\T_all 'a; (\T_all 'b; (\forall r as set(prod('a,'b)); 
    (\forall s; (\forall x; (\forall y; 
      pair(x,y) :: r|>s <-> pair(x,y)::r & y :: s))))))
    

# TODO new bound variables
rule every_pair_constructed
  find %p
  add (\exists y; (\exists z; %p = pair(y,z))) |-

rule dom_restrict_expand
  find %x :: %s <| %r
  replace %x :: %r & fst(%x) :: %s

rule rng_restrict_expand
  find %x :: %r |> %s
  replace %x :: %r & snd(%x) :: %s

rule fst_concrete
  find fst(pair(%a,%b))
  replace %a
  tags rewrite "concrete"

rule snd_concrete
  find snd(pair(%a,%b))
  replace %b
  tags rewrite "concrete"