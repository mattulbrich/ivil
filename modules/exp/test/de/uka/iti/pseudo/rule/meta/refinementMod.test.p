include
  "$int.p"
  "$refinement.p"

function 
  int x assignable
  int y assignable
  int readonly assignable

  int $markA assignable
  int $markC assignable

(* *** dummy program *)
program SKIP
  skip


(* *** testRefinementMod *)

program A
  x := 2
  y := 2
  skip MARK, 1, x = 1, 11
  skip ; "spurious skip to check that"
  x := 3
  skip MARK, 2, x = 2, 22
  y := 4
  goto 2
  goto 3

program C
  x := 2
  y := 2
  skip MARK, 2
  x := 3
  skip MARK, 1
  y := 4

program A_expected
  x := 2
  y := 2
  $markA := 1 ; "Marker for refinement"
  end ; "Marker for refinement"
  skip ; "spurious skip to check that"
  x := 3
  $markA := 2 ; "Marker for refinement"
  end ; "Marker for refinement"
  y := 4
  goto 2
  goto 4

program C_expected
  x := 2
  y := 2
  $markC := 2
  end
  x := 3
  $markC := 1
  end
  y := 4


(* *** testUsingTheMarks:
 * Missing literal
 *)

program C_0
  skip MARK

(* *** testUsingTheMarks2:
 * Missing invariant => true assumed
 * Tests also empty modification set
 *)

program A_1
  skip MARK, 1

program C_1
  skip MARK, 1

(* *** wrongly typed marks
 * Missing invariant
 *)

program A_2
  skip MARK, 1, 42

program C_2
  skip MARK, 1, 42

problem refineMod:
  [0;C][<1;A>](x=0)

problem using:
  [0;C_0][<0;SKIP>]true

problem using2:
  [0;C_1][<0;A_1>](x=0)

problem using3:
  [0;C_2][<0;A_2>]true
