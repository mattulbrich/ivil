include "$base.p"
# Used for testing rule -> problem

sort s

function
  bool assume_left
  bool assume_right
  bool add_left
  bool add_right
  bool add_left2
  
  'a _find
  'a _replace

rule extract_both
  assume assume_left |-
  assume |- assume_right
  find _find as 'alpha
  samegoal
    replace _replace as 'alpha
    add add_left |-
    add |- add_right
    
  samegoal
    # no change
    add add_left2 |-
    
  tags extract "(((_replace as alpha) = _find -> (!add_left | add_right))
                &((_find as alpha) = _find -> !add_left2))
                -> (!assume_left | assume_right)"
    
rule extract_left
  assume assume_left |-
  assume |- assume_right
  find _find |-
  samegoal
    replace _replace
    add add_left |-
    add |- add_right
  samegoal
    # no change
    add add_left2 |-
  samegoal
    remove
    add |- add_right
  samegoal
    remove
  samegoal
    replace _replace
  
  tags extract "( (!add_left | add_right | !_replace) 
                & (!add_left2 | !_find)
                & (add_right)
                & (false)
                & (!_replace) )
               -> (!assume_left | assume_right | !_find)"
    
    
rule extract_right
  assume assume_left |-
  assume |- assume_right
  find |- _find
  samegoal
    replace _replace
    add add_left |-
    add |- add_right
  samegoal
    # no change
    add add_left2 |-
  samegoal
    remove
    add |- add_right
  samegoal
    remove
  samegoal
    replace _replace
    
  tags extract "( (!add_left | add_right | _replace) 
                & (!add_left2 | _find)
                & (add_right)
                & (false)
                & (_replace) )
               -> (!assume_left | assume_right | _find)"


rule extract_findless
  assume assume_left |-
  assume |- assume_right
  samegoal
    add add_left |-
    add |- add_right
  samegoal
    add add_left2 |-

  tags extract "  (!add_left | add_right) 
                & (!add_left2)
               -> (!assume_left | assume_right)"

rule extract_findless_assume_less
  samegoal
    add add_left |-
  samegoal
    add |- add_right

  tags extract "  (!add_left) 
                & (add_right)
               -> false"
               
rule rename_schemas
  find (\forall x; (\forall %x; x=%x))
  replace true
  tags extract "true = (\forall x; (\forall x1 as sk; x=x1)) -> false -> false"


rule rename_schemas2
  find (\forall x; x=%x)
  replace true
  tags extract "true = (\forall x as sk; x=x1(x))) -> false -> false"

(*
function bool f(int)
#see TestSchemaVariableUseVisitor
rule skolemize
  find (\forall c; (\forall %x; f(%x) & %a & %b & %d) & %a) & %c & %d | (\exists %e; true)
  replace true
  tags extract "true = (\forall c; (\forall x; f(x) & a(c) & b(c,x) & d) & a(c)) & c & d | (\exists e; true) -> false -> false"  
*)
         
# TODO    
rule no_newgoal
  find true
  newgoal
    add true |-
