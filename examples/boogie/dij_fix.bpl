
/* Nested comments /* are good */ */

// ---------------------------------------------------------------
// -- Axiomatization of sets -------------------------------------
// ---------------------------------------------------------------

type Set T = [T]bool;

function Set_Empty<T>(x:T) returns (Set T);
axiom (forall<T> o: T :: { Set_Empty(o)[o] } !Set_Empty(o)[o]);

function Set_Singleton<T>(T) returns (Set T);
axiom (forall<T> r: T :: { Set_Singleton(r) } Set_Singleton(r)[r]);
axiom (forall<T> r: T, o: T :: { Set_Singleton(r)[o] } Set_Singleton(r)[o] <==> r == o);

function Set_UnionOne<T>(Set T, T) returns (Set T);
axiom (forall<T> a: Set T, x: T, o: T :: { Set_UnionOne(a,x)[o] }
  Set_UnionOne(a,x)[o] <==> o == x || a[o]);
axiom (forall<T> a: Set T, x: T :: { Set_UnionOne(a, x) }
  Set_UnionOne(a, x)[x]);
axiom (forall<T> a: Set T, x: T, y: T :: { Set_UnionOne(a, x), a[y] }
  a[y] ==> Set_UnionOne(a, x)[y]);

function Set_Union<T>(Set T, Set T) returns (Set T);
axiom (forall<T> a: Set T, b: Set T, o: T :: { Set_Union(a,b)[o] }
  Set_Union(a,b)[o] <==> a[o] || b[o]);
axiom (forall<T> a, b: Set T, y: T :: { Set_Union(a, b), a[y] }
  a[y] ==> Set_Union(a, b)[y]);
axiom (forall<T> a, b: Set T, y: T :: { Set_Union(a, b), b[y] }
  b[y] ==> Set_Union(a, b)[y]);
axiom (forall<T> a, b: Set T :: { Set_Union(a, b) }
  Set_Disjoint(a, b) ==>
    Set_Difference(Set_Union(a, b), a) == b &&
    Set_Difference(Set_Union(a, b), b) == a);

function Set_Intersection<T>(Set T, Set T) returns (Set T);
axiom (forall<T> a: Set T, b: Set T, o: T :: { Set_Intersection(a,b)[o] }
  Set_Intersection(a,b)[o] <==> a[o] && b[o]);

function Set_Difference<T>(Set T, Set T) returns (Set T);
axiom (forall<T> a: Set T, b: Set T, o: T :: { Set_Difference(a,b)[o] }
  Set_Difference(a,b)[o] <==> a[o] && !b[o]);
axiom (forall<T> a, b: Set T, y: T :: { Set_Difference(a, b), b[y] }
  b[y] ==> !Set_Difference(a, b)[y] );

function Set_Subset<T>(Set T, Set T) returns (bool);
axiom(forall<T> a: Set T, b: Set T :: { Set_Subset(a,b) }
  Set_Subset(a,b) <==> (forall o: T :: {a[o]} {b[o]} a[o] ==> b[o]));

function Set_Equal<T>(Set T, Set T) returns (bool);
axiom(forall<T> a: Set T, b: Set T :: { Set_Equal(a,b) }
  Set_Equal(a,b) <==> (forall o: T :: {a[o]} {b[o]} a[o] <==> b[o]));
axiom(forall<T> a: Set T, b: Set T :: { Set_Equal(a,b) }  // extensionality axiom for sets
  Set_Equal(a,b) ==> a == b);

function Set_Disjoint<T>(Set T, Set T) returns (bool);
axiom (forall<T> a: Set T, b: Set T :: { Set_Disjoint(a,b) }
  Set_Disjoint(a,b) <==> (forall o: T :: {a[o]} {b[o]} !a[o] || !b[o]));
    
// MATT

function Set_Complement<T>(Set T) returns (Set T);
axiom(forall<T> a: Set T, o: T :: {Set_Complement(a)[o]}
  Set_Complement(a)[o] <==> !a[o]);
  
function Set_isEmpty<T>(a: Set T) returns (bool)
  { !(exists x:T :: a[x]) }

// axiom (forall<T> :: Set_isEmpty(Set_Empty()));
  
type Rel T U = [T,U]bool;

function Rel_DomRestrict<T,U>(Rel T U, Set T) returns (Rel T U);
axiom (forall<T,U> a: Rel T U, b: Set T, o: T, u: U :: { Rel_DomRestrict(a,b)[o,u] }
  Rel_DomRestrict(a,b)[o,u] <==> a[o,u] && b[o]);
  
function Rel_RngRestrict<T,U>(Rel T U, Set U) returns (Rel T U);
axiom (forall<T,U> a: Rel T U, b: Set U, o: T, u: U :: { Rel_RngRestrict(a,b)[o,u] }  
  Rel_RngRestrict(a,b)[o,u] <==> a[o,u] && b[u]);
  
function Rel_Empty<T,U>(x:T, y:U) returns (Rel T U);
axiom (forall<T,U> o:T, u:U :: { Rel_Empty(o,u)[o,u] } !Rel_Empty(o,u)[o,u]);

function Rel_isEmpty<T,U>(a: Rel T U) returns (bool)
  { ! (exists o:T, u:U :: a[o,u]) }

function Rel_Subrel<T,U>(Rel T U, Rel T U) returns (bool);
axiom(forall<T,U> a: Rel T U, b: Rel T U :: { Rel_Subrel(a,b) }
  Rel_Subrel(a,b) <==> (forall o:T, u:U :: {a[o,u]} {b[o,u]} a[o,u] ==> b[o,u]));

function Rel_Take<T,U>(Rel T U, T, U) returns (Rel T U);
axiom (forall<T,U> a: Rel T U, b,o: T, c,u: U:: { Rel_Take(a,b,c)[o,u] }
  Rel_Take(a,b,c)[o,u] <==> a[o,u] && (o != b || u != c));
  
// -------------------

type Node;

var distance : [Node]int;
var domain_distance : Set Node;

var weight : [Node, Node]int;
var domain_weight : [Node,Node]bool;

procedure Dijkstra(start : Node)
  modifies domain_distance, distance;
{
	var n : Node;
	var visited : Set Node;
	var S,S0 : Rel Node Node;
	var o, u : Node;
	var d : int;
   
	/* distance := {} */
	domain_distance := Set_Empty(n);
   
	/* distance := { start |-> 0 } */
	distance[start] := 0;
	domain_distance := Set_Singleton(start);
   
	/* visited := {} */
	visited := Set_Empty(n);
   
	/* while exists node not in visited */
	while(!Set_isEmpty(Set_Difference(domain_distance, visited)))
		invariant Set_Subset(visited, domain_distance);
	{   
		
		/* n := node with smallest distance not in visited */
		assert (exists t:Node :: 
			!visited[t] && 
			domain_distance[t] &&
			(forall m:Node :: domain_distance[m] ==> (distance[t] <= distance[m])));
		havoc n;
		assume !visited[n] && 
			domain_distance[n] &&
			(forall m:Node :: domain_distance[m] ==> (distance[n] <= distance[m]));
		
		/* visited := visited union {n} */
		visited := Set_Union(visited, Set_Singleton(n));
		
		/* foreach node m with (n,m) in edges which is not visited */
		/* iterate {n} <| weight |>> visited using S and s*/
		S := Rel_RngRestrict(Rel_DomRestrict(domain_weight, Set_Singleton(n)),
			Set_Complement(visited));
		S0 := S;
		
		while(!Rel_isEmpty(S))
		  invariant Rel_Subrel(S, S0);
		{
			havoc u,o;
			assert (exists x,y:Node :: S[x,y]);
			assume S[o,u];
			S := Rel_Take(S, o, u);
		   
			/* just to make sure */
			assert n == o;
		   
			/* d := distance[n] + weigth(n,m); */
			assert domain_weight[o,u];
			d := distance[n] + weight[o,u];
		   
			/* if d < distance[m] */
			if(d < distance[u])
			{
				/* distance[m] := d */
				distance[u] := d;
			}
		}
   }
   assert Set_isEmpty(Set_Difference(domain_distance, visited));
   assert !(exists x:Node :: Set_Difference(domain_distance, visited)[x]);
   assert !(exists x:Node :: domain_distance[x] && !visited[x]);
   assert Set_Equal(domain_distance, visited);
}


