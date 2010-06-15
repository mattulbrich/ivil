#
# This file is part of This file is part of
#    ivil - Interactive Verification on Intermediate Language
#
# Copyright (C) 2009 Universitaet Karlsruhe, Germany
#    written by Mattias Ulbrich
#
# The system is protected by the GNU General Public License.
# See LICENSE.TXT for details.
#


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
    
  tags expectedTranslation "(((_replace as skolem) = _find -> (!add_left | add_right))
                &((_find as skolem) = _find -> !add_left2))
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
  
  tags expectedTranslation "( (!add_left | add_right | !_replace) 
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
    
  tags expectedTranslation "( (!add_left | add_right | _replace) 
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

  tags expectedTranslation "  (!add_left | add_right) 
                & (!add_left2)
               -> (!assume_left | assume_right)"

rule extract_findless_assume_less
  samegoal
    add add_left |-
  samegoal
    add |- add_right

  tags expectedTranslation "  (!add_left) 
                & (add_right)
               -> false"
               
rule rename_schemas
  find (\forall x; (\forall %x; x=%x))
  replace true
  tags expectedTranslation 
        "true = (\forall x; (\forall x1 as skolem; x=x1)) -> false -> false"


rule rename_schemas2
  find (\forall x; x=%x)
  replace true
  tags expectedTranslation 
        "true = (\forall x as skolem; x=x1(x)) -> false -> false"
  
  
rule rename_schema3
  find (\forall c; c=c) | %c=%c
  replace true
  tags expectedTranslation 
        "true = ((\forall c as skolem; c=c) | c1=c1)-> false -> false"
  
  
# from a problem
rule rename_schema4
  find (\forall c; %phi) & %c
  replace true
  tags expectedTranslation 
        "true = ((\forall c as skolem; phi(c)) & c1)-> false -> false"  
  

# from a problem
rule rename_schema5
  find (\forall %x; true)
  replace true
  tags expectedTranslation 
        "true=(\forall x as skolem; true) -> false -> false"  

  
function bool f(int)
#see TestSchemaVariableUseVisitor
rule skolemize
  find (\forall c; (\forall %x; f(%x) & %a & %b & %d) & %a) & %c & %d | (\exists %e; true)
  replace true
  tags expectedTranslation 
        "true = ((\forall c as skolem; (\forall x; f(x) & a(c) & b(x, c) & d) & a(c)) & c1 & d |
         (\exists e as skolem1; true)) -> false -> false"  
