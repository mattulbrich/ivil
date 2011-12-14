(*
This header is intended for testing of hiding rule features. After hiding is stable, the rules will be moved to the appropriate existing files.
*)

include "$ivil.p"

rule forall_left_hide
  find  (\forall %x; %b)  |-
  where
    interact %inst
  replace $$subst(%x, %inst, %b)
  tags dragdrop "6"
       hiding "find"
       
#note: this rule does not hide %b, but it will be able to unhide %b. this is not a problem, as any term can be safely marked as hidden
rule obscure_forall_left_hide
  find  (\forall %x; %b)  |-
  assume %b |-
  where
    interact %inst
  replace $$subst(%x, %inst, %b)
  tags dragdrop "6"
       hiding "find,a0"
       
rule hide_term_left
  find  %a  |-
  where toplevel
  remove
  tags hiding "find"

rule hide_term_right
  find  |- %a
  where toplevel
  remove
  tags hiding "find"
  
# this program is intended to test the rules from above
program P
  assume (\forall x; x = 0)
  assume (\forall x as int; true)
  assume true
  assume true -> false
  assert false
  
problem [0;P]true
