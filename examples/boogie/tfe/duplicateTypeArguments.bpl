/*
  This should raise a TypeException because of duplicate type argument '_' in synonym declaration.
*/

type S _ _ = [_]_;

