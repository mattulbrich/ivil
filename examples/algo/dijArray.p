include 
  "$base.p"
  "$int.p"
  "$symbex.p"
  "$decproc.p"

sort 
  map('from, 'to)

plugin
  prettyPrinter : "test.ArrayPrettyPrinter"

function
  map('a,'a) id

  map('a,'b) write(map('a,'b), 'a, 'b)
  'b read(map('a,'b), 'a)

(*
 * definitions ... rules are axioms
 *)
 
rule read_write
  find read(write(%a, %i, %v), %j)
  replace cond(%i=%j, %v, read(%a, %j))
  tags asAxiom
