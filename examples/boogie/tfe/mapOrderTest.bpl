/*
  This tests ordering of map parameters in universal types.
*/

procedure P()
{
    var a :<a,b>[a, b]b;
    var b :<b,a>[a, b]b;
    var c :<c,d>[d, c]d;
    var d :<d,c>[d, c]d;
}
