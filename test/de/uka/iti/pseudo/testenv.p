include
        "$base.p"
        "$int.p"
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
        'a id('a)
	poly('a, 'b) P('a, 'b)
	'a Q(poly('a,'a))

(* for tests with programs *)

program P
    assume b1 ; "first statement"
    assert b2
    skip
    goto 5, 0
    skip (* havov i1 *)
    i1 := i2 + i3
    end true

program Q
    assume b1 & b2
    sourceline 3
  label: label2:
    skip
    goto label
