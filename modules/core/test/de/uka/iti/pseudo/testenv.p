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

# This is the test bed environment used to test the system

include
        "$base.p"
        "$int.p"
        "$fol.p"
        "$symbex.p"

sort
	poly('a, 'b)
	set('a)

binder
        'b (\bind 'a; 'b)

function

(* some integers *)
	int i1  assignable
	int i2
	int i3
	int i4
	
(* some unique functions *)
        int uniq1 unique
        bool uniq2 unique
        bool uniq3(int, int) unique
        bool uniq4(int, int) unique

(* some booleans *)	
	bool b1 assignable
	bool b2

(* alternate arb *)
        'a other
	
	int f('a)
	int g('a, 'b)
        bool bf('a) 
        'a id('a)

	poly('a, 'b) P('a, 'b)
	'a Q(poly('a,'a))
	
(* a remove rule to test rule application *)
rule remove_right
    find |- %a
    remove

(* for tests with programs *)

program P (* tested in TerstTermUnification *)
    assume b1 ; "first statement"
    assert b2
    skip
    goto 5, 0
    havoc i1
    i1 := i2 + i3
    end
    end
    skip LOOPINV, i1 > 0, i2
    i1:=1 || b1 := true

program Q
    assume b1 & b2
    sourceline 3
  label: label2:
    skip
    goto label

(*
program test_meta_functions_subst
    assert \var b

program test_meta_functions_subst2
    b1 := \var b
    
program test_meta_functions_subst3
    assert [0; test_meta_functions_subst](true)   
*)