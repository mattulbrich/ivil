include "$set.p"
include "$map.p"
include "$pair.p"
include "$int.p"
include "$symbex.p"
include "$decproc.p"

sort node

plugin
  prettyPrinter : "test.UnicodePrettyPrinter"

function 
  int weight(node, node) 
  set(prod(node, node)) dom_weight

binder
  'a (\argmin 'a; bool; int)

rule argmin_expand
  find (\argmin %x; %b; %e)
  samegoal "{%e} has a lower limited"
    # TODO create fresh variable here
    add |- (\exists limit; (\forall %x; %b -> %e >= limit))
  samegoal "{%b} has a witness"
    add |- (\exists %x; %b)
  samegoal "argmin is the minimum"
    add ($$subst(%x, (\argmin %x; %b; %e), %b)) |-
    add (\forall %x; %b -> 
      $$subst(%x, (\argmin %x; %b; %e), %e) <= %e) |-
