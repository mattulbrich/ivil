include
        "$base.p"
        "$int.p"

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

(* some booleans *)	
	bool b1 assignable
	bool b2

(* alternate arb *)
        'a other
	
	int f('a)
        'a id('a)
	poly('a, 'b) P('a, 'b)
	'a Q(poly('a,'a))

(* for tests with programs *)

program
    assume b1
    assert b2
    skip
    goto 5, 0
    skip (* havov i1 *)
    i1 := i2 + i3
    end true