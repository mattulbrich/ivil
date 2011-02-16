include "$set.p"
include "$map.p"
include "$pair.p"
include "$int.p"
include "$symbex.p"
include "$decproc.p"

sort node

function 
  map(node, int) distance assignable
  set(node) dom_distance assignable
  set(node) visited assignable

  node n assignable
  node o assignable
  node u assignable

  set(prod(node, node)) S assignable
  set(prod(node, node)) S0 assignable
  
  int weight(node, node) 
  set(prod(node, node)) dom_weight
  node start
  int d assignable
  
program Dij
  dom_distance := emptyset

  distance := write(distance, start, 0)
  dom_distance := singleton(start)

  visited := emptyset

  (* while exists node not in visited *)
  
 loop: 
  skip_loopinv visited <: dom_distance &
    (\forall x; x::visited -> ((\forall y; pair(x,y)::dom_weight -> read(distance, y) <= read(distance, x) + weight(x,y))
                             & (\exists y; pair(x,y)::dom_weight &  read(distance, y)  = read(distance, x) + weight(x,y))))

  
  goto body, after
 body:
  assume !emptyset = (dom_distance \ visited)
  assert (\exists t; ! (t :: visited) &
    t :: dom_distance &
    (\forall m; m::dom_distance -> read(distance,t) <= read(distance,m))) ; "assert before choose"

  havoc n ; "havoc for choice"
  assume !n :: visited &
    n :: dom_distance &
    (\forall m; m::dom_distance -> read(distance,n) <= read(distance,m)) ; "assume in choice"
    
  (* visited := visited union {n} *)
  visited := visited \/ singleton(n)

  (* foreach node m with (n,m) in edges which is not visited 
   * iterate {n} <| weight |>> visited using S and s *)
  S := singleton(n) <| dom_weight |> $complement(visited)
  S0 := S
 loop2:
  skip_loopinv S <: S0
  goto body2, after2
 body2:
  assume !emptyset = S
  havoc u
  havoc o
  assert (\exists x; (\exists y; pair(x,y) :: S))
  assume pair(o,u) :: S
  S := S \ singleton(pair(o,u))

  (* just to make sure *)
  assert n = o ; "make sure fst comp correct"

  (* d := distance[n] + weigth(n,m); *)
  assert pair(o,u) :: dom_weight ; "assert pair in weight domain"
  d := read(distance, n) + weight(o,u)

  (* if d < distance[m] *)
  goto then, else
 then:
  assume d < read(distance, u)
  distance := write(distance, u, d)
  goto after3
 else:
  assume !d < read(distance, u)
 after3:
  goto loop2

 after2:
  goto loop

 after:
  assume emptyset = (dom_distance \ visited)

problem
  [0; Dij]
(*

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


procedure Dijkstra(start : Node)
  modifies domain_distance, distance;
{
	var n : Node;
	var visited : Set Node;
	var S,S0 : Rel Node Node;
	var o, u : Node;
	var d : int;
   
	/* distance := {} */
	domain_distance := Set_Empty();
   
	/* distance := { start |-> 0 } */
	distance[start] := 0;
	domain_distance := Set_Singleton(start);
   
	/* visited := {} */
	visited := Set_Empty();
   
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


*)
