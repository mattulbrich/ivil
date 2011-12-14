# Automatically created on Wed Dec 14 15:18:26 CET 2011
include "$int.p"
include "$symbex.p"
include "$decproc.p"
  function int array(int)

function int len
function int max assignable
function int i assignable

program arrayMin source "arrayMin.algo"
 sourceline 29
  max := array ( 0 ) 
 sourceline 30
  i := 1 
 loop0:
 sourceline 33
  skip_loopinv ( \forall j ; 0 <= j & j < i -> array ( j ) <= max ) & ( \exists j ; 0 <= j & j < i & array ( j ) = max ) & 0 <= i & i <= len , len - i 
 sourceline 32
  goto body0, after0
 body0:
  assume i < len ; "assume condition "
 sourceline 39
  goto then0, else0
 then0:
  assume array ( i ) > max ; "then"
 sourceline 41
  max := array ( i ) 
  goto after1
 else0:
  assume $not(array ( i ) > max ); "else"
 sourceline 42
 after1:
 sourceline 44
  i := i + 1 
  goto loop0
 sourceline 32
 after0:
  assume $not(i < len )


problem 
len > 0  |- [[0;arrayMin]]((( \forall i ; 0 <= i & i < len -> array ( i ) <= max ) ) & (( \exists i ; 0 <= i & i < len & array ( i ) = max ) ))
