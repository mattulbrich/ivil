procedure P1(y: int)
{
  goto X;  // error: label out of reach
  while (y < 100)
  {
    X:
  }

  Q:
  if (y == 102) {
    A:
    goto Q;
  } else if (y == 104) {
    B:
  } else {
    C:
    goto K;  // error: label out of reach
  }

  while (y < 1000)
  {
    K:
    goto A;  // error: label out of reach
    if (y % 2 == 0) {
      goto L;
      M:
    }
    goto K, L;
    L:
    if (*) {
      goto M;  // error: label out of reach
    }
  }
  goto B;  // error: label out of reach
}
