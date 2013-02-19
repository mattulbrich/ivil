@spec.SourceFile("./A.jspec") class A {

    /*@
      @ contract normal_behaviour
      @  requires "0 <= _x & _x < arrlen(a)"
      @  requires "0 <= _y & _y < arrlen(a)"
      @  requires "! _a = null"
      @  ensures "0 <= resInt"
      @  ensures "resInt <= arrlen(a) - _x"
      @  ensures "resInt <= arrlen(a) - _y"
      @  ensures "(\forall j; 0 <= j & j < resInt ->
      @                 h[_a, idxInt(_x + j)] = h[_a, idxInt(_y + j)])"
      @  ensures "resInt = arrlen(a) - _x |
      @           resInt = arrlen(a) - _y |
      @           !h[_a, idxInt(_x + resInt)] = h[_a, idxInt(_y + resInt)]"
      @  modifies "emptyset"
      @*/
    int m(int[] a, int x, int y) {
	int i = 0;

	spec.Spec.loopinv("((0<=_i & _i<=arrlen(_a)-_x & _i<=arrlen(_a)-_y &\n	    (\\forall j; 0<=j & j < _i -> \n	       h[_a, idxInt(_x + j)] = h[_a, idxInt(_y + j)])) & modHeap(h, ho, {h:=ho}(emptyset))), arrlen(_a) - _i");








	while(x+i < a.length && y+i < a.length &&
	      a[x+i] == a[y+i]) {
	    i++;
	}

	return i;
    }
}
