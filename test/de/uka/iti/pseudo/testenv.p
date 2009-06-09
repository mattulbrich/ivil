include "$base.p"

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
	bool b1
	bool b2

(* alternate arb *)
        'a other
	
	int f('a)
        'a id('a)
	poly('a, 'b) P('a, 'b)
	'a Q(poly('a,'a))
