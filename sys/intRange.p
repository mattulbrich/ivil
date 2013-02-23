
#
# This belongs to the bytecode translation to ivil
#
include "$int.p"
include "$heap.p"

sort intrangetype

function
  intrangetype INT_RANGE unique
  intrangetype BYTE_RANGE unique
  intrangetype SHORT_RANGE unique
  intrangetype CHAR_RANGE unique
  intrangetype LONG_RANGE unique
  
  bool inRange(int, intrangetype)
  intrangetype rangeof(field(int))

rule intRange_def
  find inRange(%i, INT_RANGE)
  replace -2147483648 <= %i & %i < 2147483648
  tags
    asAxiom

rule byteRange_def
  find inRange(%i, BYTE_RANGE)
  replace -128 <= %i & %i < 128
  tags
    asAxiom

rule wellformedIntRange
  assume wellformed(%h) |-
  find %h[%o, %f as field(int)]
  add inRange(%h[%o, %f], rangeof(%f)) |-
  tags
    asAxiom

rule TEMP_all_is_int
  find rangeof(%f)
  replace INT_RANGE
  tags
    asAxiom