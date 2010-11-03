#
# This file is part of This file is part of
#    ivil - Interactive Verification on Intermediate Language
#
# Copyright (C) 2009-2010 Universitaet Karlsruhe, Germany
#
# The system is protected by the GNU General Public License.
# See LICENSE.TXT for details.
#

include "selectionSortDecl.p"

properties  BreakpointStrategy.stopAtLoop "false"

function
  array(int) array assignable
  array(int) old_array assignable
  array(int) initial_array assignable
  int left assignable
  int right assignable
  int pivotIndex assignable
  int pivotValue assignable
  int storeIndex assignable
  int i assignable
  int result assignable


program partition source "quickSort.algo"
  old_array := array
  pivotValue := read(array, pivotIndex)
  array := swap(array, pivotIndex, right)
  storeIndex := left
  i := left 
 loop:skip_loopinv isPerm(array, old_array)
  goto body, after
  
 body:
  assume i < right 
  goto then, else
 then:
  assume read(array,i) <= pivotValue
  array := swap(array, i, storeIndex)
  storeIndex := storeIndex + 1
  goto afterIf
 else:
  assume !read(array,i) <= pivotValue
 afterIf:
  i := i + 1
  goto loop

 after:
  array := swap(array, storeIndex, right)
  result := storeIndex
  assert isPerm(array, old_array) & 
    (\forall i; left<=i & i<result -> read(array, i) <= read(array, result)) &
    (\forall i; result<i & i<right -> read(array, i) >= read(array, result)) &
    (\forall i; i<left | i >=right -> read(array, i) = read(old_array, i)) &
    left <= result & result < right


program quicksort source "quickSort.algo"

  assume left <= pivotIndex & pivotIndex <= right
  
  initial_array := array
  goto then, else

 then:
  assume right > left
  pivotIndex := left
  
  # pivotIndex := partition(array, left, right, pivotIndex)
  old_array := array
  havoc array
  havoc pivotIndex
  assume isPerm(array, old_array) & 
    (\forall i; left<=i & i<pivotIndex -> read(array, i) <= read(array, pivotIndex)) &
    (\forall i; pivotIndex<i & i<right -> read(array, i) >= read(array, pivotIndex)) &
    (\forall i; i<left | i >=right -> read(array, i) = read(old_array, i)) &
    left <= pivotIndex & pivotIndex < right

  old_array := array
  havoc array
  assume isPerm(array, old_array) &
   (\forall i; (\forall j; left <= i & i<=j & j < pivotIndex -> read(array, i) <= read(array, j))) &
   (\forall i; i < left | i >= pivotIndex -> read(array, i) = read(old_array,i))

  old_array := array
  havoc array
  assume isPerm(array, old_array) &
   (\forall i; (\forall j; pivotIndex + 1 <= i & i<=j & j < right -> read(array, i) <= read(array, j))) &
   (\forall i; i < pivotIndex +1  | i >= right -> read(array, i) = read(old_array,i))
    
  goto afterIf
 else:
  assume !right > left
  
 afterIf:
  assert isPerm(array, initial_array) &
   (\forall i; (\forall j; left <= i & i<=j & j < right -> read(array, i) <= read(array, j))) &
   (\forall i; i < left | i >= right -> read(array, i) = read(initial_array,i))


problem (*[0;partition] &*) [0;quicksort]