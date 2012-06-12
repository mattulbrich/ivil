include "$int.p"

function 
  int $a assignable
  int $c assignable

  int x assignable
  int y assignable
  int readonly assignable

(* *** testRefinementMod *)

program A
  x := 2
  y := 2
  $a := 1
  skip_mark x = 1
  skip (* spurious skip to check that *)
  x := 3
  $a := 2
  skip_mark x = 2
  y := 4

program C
  x := 2
  y := 2
  $c := 2
  skip
  x := 3
  $c := 1
  skip
  y := 4

program A_expected
  x := 2
  y := 2
  $a := 1
  end
  skip
  x := 3
  $a := 2
  end
  y := 4


program C_expected
  x := 2
  y := 2
  $c := 2
  end
  x := 3
  $c := 1
  end
  y := 4


(* *** testUsingTheMarks:
 * reading from the marks
 *)

program CUsing
  x := $a
  y := $c

program AUsing
  skip


(* *** testUsingTheMarks2:
 * modifying wrong mark
 *)

program CUsing2
  $a := 1

program AUsing2
  skip


(* *** testAsymmetricMarks:
 * only marks in 1 program
 *)

program CAsymm
  $c := 1
  skip

program AAsymm
  $a := 2
  skip
  

problem refineMod:
  [0;C][<1;A>](x=0)

problem using:
  [0;CUsing][<0;AUsing>]true

problem using2:
  [0;CUsing][<0;AUsing>]true

problem asymm:
  [0;CAsymm][<0;AAsymm>]true