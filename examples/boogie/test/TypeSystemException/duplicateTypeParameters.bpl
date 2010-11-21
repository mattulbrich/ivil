/*
  This should raise a TypeException because of duplicate typeparameter 'a'.
*/

type S = <a>[<a>[a]a, a]a;
