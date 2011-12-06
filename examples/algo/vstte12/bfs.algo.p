include "$set.p"
include "$int.p"
include "$symbex.p"
include "$decproc.p"

sort vertex

function set(vertex) succ(vertex)

function bool connect(vertex, vertex, int)

rule connect_def
  find connect(%a, %b, %n)
  where freshVar %x, %a, %b, %n
  replace cond(%n=0,
        %a = %b,
        (\exists %x; connect(%a,%x,%n-1) & %b::succ(%x)))

rule oops
closegoal