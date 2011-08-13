procedure P0()
{
  // these labels don't exist at all
  goto X;  // error: undefined label
  goto Y;  // error: undefined label
}
