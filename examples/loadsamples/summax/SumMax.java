@spec.SourceFile("./SumMax.jspec") class SumMax {
      
    int max;
    int sum;

    // int N; declare an int constant

    /*@ contract normal
      @  requires "!_a = null & arrlen(_a) = N"
      @  requires "(\forall k; 0 <= k & k < N -> 0 <= h[_a, idxInt(k)])"
      @  ensures "(\forall k; 0<=k & k<N ->
      @                  h[_a, idxInt(k)] <= h[_this, F_SumMax_max])"
      @  ensures "N > 0 -> 
      @             (\exists k; 0<=k & k < N & 
      @                  h[_a, idxInt(k)] = h[_this, F_SumMax_max])"
      @  ensures "h[_this, F_SumMax_sum] <= N * h[_this, F_SumMax_max]"
      @  ensures "h[_this, F_SumMax_sum] = (\sum k; 0; N; h[_a, idxInt(k)])"
      @  modifies "singleton(_this)"
      @*/
    @spec.Include("aha.p")
    public void m(int[] a) {
	
	int i = 0;
	sum = 0;
	max = 0;

	spec.Spec.loopinv("((0 <= _i & _i<=N & \n	     (\\forall k; 0 <= k & k < _i -> h[_a, idxInt(k)] <= h[_this, F_SumMax_max]) &\n	     (_i = 0 -> h[_this, F_SumMax_max] = 0) &\n	     (_i > 0 -> (\\exists k; 0<=k & k < _i & \n	                 h[_a, idxInt(k)] = h[_this, F_SumMax_max])) &\n	     h[_this, F_SumMax_sum] <= _i * h[_this, F_SumMax_max] &\n	     h[_this, F_SumMax_sum] = (\\sum k; 0; _i; h[_a, idxInt(k)])) & modHeap(h, ho, {h:=ho}(singleton(_this)))), arrlen(_a) - _i");











	while (i < a.length) {
	    if (max < a[i]) {
		max = a[i];
	    }
	    sum = sum + a[i];
	    i++;
	}
    }     
}
