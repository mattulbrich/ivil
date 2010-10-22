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

include 
  "selectionSortDecl.p"
  "$unicode.p"

properties
  BreakpointStrategy.stopAtLoop "false"

function
  array(int) a assignable
  array(int) a_pre
  int n
  int i assignable
  int j assignable
  int t assignable

program P source "selectionSort.algo"
  sourceline 8
  assume n = length(a) & n >= 1
  assume a = a_pre

  sourceline 13
  i := 1

  sourceline 14
loop1:
  skip_loopinv
     (\forall k; (\forall l; 
        1<=k & k<=l & l<=i -> read(a, k) <= read(a, l)))
    & (\forall k; (\forall l;
        1<=k & k<i & i <= l & l<=n -> read(a, k) <= read(a,l)))
    & 1<=i & i<=n
    & isPerm(a, a_pre)
    & length(a) = n

  goto body1, final1
body1:
  assume i <= n - 1
  
  sourceline 17
  t := i

  sourceline 19
  j := i+1
  sourceline 20

loop2:
  skip_loopinv
     (\forall k; i<=k & k<j -> read(a, t) <= read(a, k))
    & (\forall k; (\forall l; 
        1<=k & k<=l & l<=i -> read(a, k) <= read(a, l)))
    & (\forall k; (\forall l;
        1<=k & k<i & i <= l & l<=n -> read(a, k) <= read(a,l)))
    & 1<=i & i<n
    & i+1<=j & j<=n+1
    & i<=t & t<=n
    & isPerm(a, a_pre)
    & length(a) = n

  goto body2, final2
body2:
  assume j <= n

  sourceline 23
  assert 1 <= j & j <= length(a) ; "Index j within bounds"
  assert 1 <= t & t <= length(a) ; "Index t within bounds"
  goto then1, else1
then1:
  assume read(a,j) < read(a,t)
  sourceline 24
  t := j
  goto afterIf1
else1:
  sourceline 23
  assume !read(a,j) < read(a,t)
afterIf1:

  sourceline 19
  j := j + 1
  goto loop2
final2:
  assume j > n

  sourceline 26
  assert 1 <= i & i <= length(a) ; "Index i within bounds"
  assert 1 <= t & t <= length(a) ; "Index t within bounds"
  a := swap(a, i, t)

  sourceline 13
  i := i + 1
  goto loop1

final1: 
  assume i = (n - 1) + 1

  sourceline 29
  assert (\forall i; 1<=i & i<=n & 1<=i+1 & i+1<=n -> read(a, i) <= read(a, i+1))
    ; "for all applicable i: a[i] <= a[i+1]"
  
  sourceline 30
  assert isPerm(a, a_pre) ; "a is a permutation of original a"

problem
  [ 0 ; P ]
