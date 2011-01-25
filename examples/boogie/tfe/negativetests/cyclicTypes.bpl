/*
  This should raise a TypeException on all typedifinitions;
*/

type S = T;
type T = S;

type A = B;
type B = C;
type C = D;
type D = S;
