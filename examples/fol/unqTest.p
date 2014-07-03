include "$fol.p"

function
  int u1 unique
  int u2 unique
  int i1 assignable
  int i2

  bool u3(int) unique
  bool u4(int) unique

lemma problem
  !u1=u2 & !u3(3)=u4(3) & !u4(3)=u4(4) & u3(3)=u3(3)
