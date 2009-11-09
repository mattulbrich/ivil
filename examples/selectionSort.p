include 
  "selectionSortDecl.p"

function
  array(int) a assignable
  int n
  int i assignable
  int j assignable
  int t assignable

program P
  assume n = length(a)
  
  sourceline 13
  i := 1
loop1:
  goto body1, final1
body1:
  assume i <= n - 1
  
  sourceline 14
  t := i

  sourceline 15
  j := i+1
loop2:
  goto body2, final2
body2:
  assume j <= n

  sourceline 16
  assert 1 <= j & j <= length(a) ; "Index j within bounds"
  assert 1 <= t & t <= length(a) ; "Index t within bounds"
  goto then1, else1
then1:
  sourceline 17
  assume read(a,j) < read(a,t)
  t := j
  goto afterIf1
else1:
  sourceline 16
  assume !read(a,j) < read(a,t)
afterIf1:

  sourceline 18
  assert 1 <= i & i <= length(a) ; "Index i within bounds"
  assert 1 <= t & t <= length(a) ; "Index t within bounds"
  a := swap(a, i, t)

  sourceline 15
  j := j + 1
  goto loop1
final2:
  assume j > n

  sourceline 13  
  i := i + 1
  goto loop1

final1: 
  assume i = (n - 1) + 1