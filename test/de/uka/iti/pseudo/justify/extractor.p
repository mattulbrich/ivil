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
    
  tags expectedTranslation "(((_replace as 'alpha) = _find -> (!add_left | add_right))
                &((_find as 'alpha) = _find -> !add_left2))
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
        "true = (\forall x as 'x; (\forall x1 as 'x; x=x1)) -> false -> false"


rule rename_schemas2
  find (\forall x; x=%x)
  replace true
  tags expectedTranslation 
        "true = (\forall x as 'x; x=x1(x)) -> false -> false"
  
rule free_numeric_schema_type
  find (\forall x; true)
  replace true
  tags expectedTranslation 
        "true = (\forall x as 'v2; true) -> false -> false"
  
rule rename_schema3
  find (\forall c; c=c) | %c=%c
  replace true
  tags expectedTranslation 
        "true = ((\forall c as 'v3; c=c) | c1 as 'c=c1) -> false -> false"
  
  
# from a problem
rule rename_schema4
  find (\forall c; %phi) & %c
  replace true
  tags expectedTranslation 
        "true = ((\forall c as 'v2; phi(c)) & c1)-> false -> false"  
  

# from a problem
rule rename_schema5
  find (\forall %x; true)
  replace true
  tags expectedTranslation 
        "true=(\forall x as 'x; true) -> false -> false"  


rule schema_type1
  find %x = %x
  replace true
  tags expectedTranslation 
        "true = (x as 'x = x) -> false -> false"  
        
        
rule schema_type2
  find (%x as 't = %x) | (%t = %t)
  replace true
  tags expectedTranslation 
        "true = ((x as 't = x) | t as 't1 = t) -> false -> false"  
  
 
rule skolemize
  find (\forall %x; %b)
  replace true
  tags expectedTranslation 
       "true = (\forall x as 'x; b(x)) -> false -> false"
# ensure "bool b('x)" is available.  
   
   
rule skolemize2
  find (\forall %x as int; %b)
  replace true
  tags expectedTranslation 
       "true = (\forall x; b(x)) -> false -> false"
# ensure "bool b(int)" is available.  
  
function bool f(int)
#see TestSchemaVariableUseVisitor
rule skolemize3
  find (\forall c; (\forall %x; f(%x) & %a & %b & %d) & %a) & %c & %d | (\exists %e; true)
  replace true
  tags expectedTranslation 
        "true = ((\forall c as 'c; (\forall x as int; f(x) & a(c) & b(x, c) & d) & a(c)) & c1 & d |
         (\exists e as skolem1; true)) -> false -> false"  
