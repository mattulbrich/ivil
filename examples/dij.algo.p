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
