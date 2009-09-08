include "$int.p"
  "$fol.p"
  "$symbex.p"
  "$decproc.p"

sort
   heap
   ref

(* Variables and function parameters *)
function
   heap $h assignable
   ref nil
   
(* Record fields *)
function
   field(int) R_value unique
   field(ref) R_valid unique


(* temp heap model *)
function
   'a sel(heap, 

(* Temporary variables *)
function
   bool $cnd assignable
   
program P source "test/p3.ras"
sourceline 13
  assert !nil = a
  $cnd := sel($h, a, valid)
  goto then0 else1
then0:
  assume $cnd
sourceline 15
  assert !nil = b
  $cnd := sel($h, b, valid)
  goto then3 else4
then3:
  assume $cnd
sourceline 17
  assert !nil = a
  assert !nil = b
  $cnd := (sel($h, a, value) > sel($h, b, value))
  goto then6 else7
then6:
  assume $cnd
sourceline 18
  $result := a
  goto procEnd
  goto after8
sourceline 17
else7:
  assume !$cnd
sourceline 19
  $result := b
  goto procEnd
after8:
  goto after5
sourceline 15
else4:
  assume !$cnd
sourceline 22
  $result := a
  goto procEnd
after5:
  goto after2
sourceline 13
else1:
  assume !$cnd
sourceline 25
  $result := b
  goto procEnd
after2:
procEnd:
  skip
problem [P]

