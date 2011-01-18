
var x : bool;

procedure T()
ensures x == old(x);
{
}

procedure P()
requires x;
modifies x;
ensures x;
{
    x := false;
    x := old(|{
      A:
      call T();
      return old(x);
    }|);
}
