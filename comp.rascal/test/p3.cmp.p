include "$rascal.p"
  "$symbex.p"
  "$decproc.p"
  
(* Record fields *)
function
   field(int) R_value unique
   field(bool) R_valid unique

(* Function paramters *)
function
  ref a assignable
  ref b assignable
  ref result assignable

(*** Contracts
Invariants:

Contract for sort
   Pre:      line 31: : !arr = nil
   Post:     line 31: : !arr = nil
   Modifies: ---

Contract for cmp
   Pre:      line 10:  !a=nil & !b=nil
   Post:     line 10:  !a=nil & !b=nil
   Modifies: ---
***)

program P source "p3.ras"
  sourceline 10
    assume  !a=null & !b=null
  sourceline 13
    assert !null = a
    cnd := sel(h, loc(a, R_valid))
    goto then0, else1
  then0:
    assume cnd
  sourceline 15
    assert !null = b
    cnd := sel(h, loc(b, R_valid))
    goto then3, else4
  then3:
    assume cnd
  sourceline 17
    assert !null = a
    assert !null = b
    cnd := (sel(h, loc(a, R_value)) > sel(h, loc(b, R_value)))
    goto then6, else7
  then6:
    assume cnd
  sourceline 18
    result := a
    goto procEnd
    goto after8
  sourceline 17
  else7:
    assume !cnd
  sourceline 19
    result := b
    goto procEnd
  after8:
    goto after5
  sourceline 14
  else4:
    assume !cnd
  sourceline 20
    result := a
    goto procEnd
  after5:
    goto after2
  sourceline 13
  else1:
    assume !cnd
  sourceline 25
    result := b
    goto procEnd
  after2:
  procEnd:
  sourceline 10
    assert result = a | result = b
  sourceline 0
    skip

problem [0; P]
