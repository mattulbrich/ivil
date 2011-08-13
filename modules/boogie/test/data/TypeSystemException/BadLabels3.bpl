procedure Break(n: int)
{
  break;  // error: break not inside a loop
  if (*) {
    break;  // error: label-less break not inside a loop
  }
  
  A:
  if (*) {
    break A;  // this is fine, since the break statement uses a label
  }

  B:
  assert 2 <= n;
  while (*) {
    break B;  // error: B does not label a loop
    break;
    C: while (*) { assert n < 100; }
    break A;     // error: A does not label a loop
    break C;     // error: A does not label an enclosing loop
    F: break F;  // error: F does not label an enclosing loop
  }

  D:
  while (*) {
    E:
    while (*) {
      if (*) {
        break;
      } else if (*) {
        if (*) { break E; }
      } else {
        break D;
      }
    }
  }
}
