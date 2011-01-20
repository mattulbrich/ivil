type T;
const zero: T;

function IsProperIndex(i: int, size: int): bool;

procedure D(a: [int]T, n: int)
{
  Start:
    assume (forall i : int :: IsProperIndex(i, n) ==> |{ B: return a[i] == zero; }|);
    goto Next;
  Next:
    assert (forall i : int :: IsProperIndex(i, n) ==> a[i] == zero);
}
