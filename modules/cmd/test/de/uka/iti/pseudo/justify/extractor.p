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

  tags expectedTranslation "((_find = (_replace as 'alpha)  -> (!add_left | add_right))
                & (!add_left2))
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

  tags expectedTranslation "  !(!add_left
                & add_right)"

rule rename_schemas
  find (\forall x; (\forall %x; x=%x))
  replace true
  tags expectedTranslation
        "(\\forall x as 'x; (\\forall x1 as 'x; x=x1))"


rule rename_schemas2
  find (\forall x; x=%x)
  replace true
  tags expectedTranslation
        "(\\forall x as 'x; x=x1(x))"

rule free_numeric_schema_type
  find (\forall x; true)
  replace true
  tags expectedTranslation
        "(\\forall x as 'v2; true)"

rule rename_schema3
  find (\forall c; c=c) | %c=%c
  replace true
  tags expectedTranslation
        "((\\forall c as 'v3; c=c) | c1 as 'c=c1)"


# from a problem
rule rename_schema4
  find (\forall c; %phi) & %c
  replace true
  tags expectedTranslation
        "((\\forall c as 'v2; phi(c)) & c1)"


# from a problem
rule rename_schema5
  find (\forall %x; true)
  replace true
  tags expectedTranslation
        "(\\forall x as 'x; true)"


rule schema_type1
  find %x = %x
  replace true
  tags expectedTranslation
        "(x as 'x = x)"


rule schema_type2
  find (%x as 't = %x) | (%t = %t)
  replace true
  tags expectedTranslation
        "((x as 't = x) | t as 't1 = t)"


rule skolemize
  find (\forall %x; %b)
  replace true
  tags expectedTranslation
       "(\\forall x as 'x; b(x))"
# ensure "bool b('x)" is available.


rule skolemize2
  find (\forall %x as int; %b)
  replace true
  tags expectedTranslation
       "(\\forall x; b(x))"
# ensure "bool b(int)" is available.

function bool f(int)
#see TestSchemaVariableUseVisitor
rule skolemize3
  find (\forall c; (\forall %x; f(%x) & %a & %b & %d) & %a) & %c & %d | (\exists %e; true)
  replace true
  tags expectedTranslation
        "((\\forall c as 'v3; (\\forall x as int; f(x) & a(c) & b(x, \\var c as 'v3) & d) & a(c)) & c1 & d |
         (\\exists e as 'e; true))"

rule type_quant_fails
  find (\T_all 'a; %c)
  replace true

# from a strange case with sets:
rule assume_no_add
  assume assume_left |-
  find _find as bool
  replace _replace
  tags expectedTranslation
        "!(_find as bool = _replace) -> !assume_left"