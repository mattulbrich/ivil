

type List _;

function NIL<a>() returns (List a);
function Cons<a>(a, List a) returns (List a);

function car<a>(List a) returns (a);
function cdr<a>(List a) returns (List a);

axiom (forall<a> x:a, l:List a :: car(Cons(x, l)) == x);
axiom (forall<a> x:a, l:List a :: cdr(Cons(x, l)) == l);

axiom (forall<a> x:a, l:List a :: Cons(x, l) != NIL());

var l:List bool;

var m:List int;
var mar:[int](List int);

procedure Q() returns () {
  assert Cons(NIL(), NIL()) != NIL();  // warning, but provable
}
