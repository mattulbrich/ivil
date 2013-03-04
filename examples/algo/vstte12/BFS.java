@spec.SourceFile("./BFS.jspec") class BFS {

    int size;
    boolean[][] adjacency;


    /*@ contract
      @   requires "!array = null"
      @   ensures "-1 <= resInt & resInt < arrlen(array)"
      @   ensures "resInt >= 0 -> h[array, idxBool(resInt)] &
      @               (\forall i; 0<=i & i<resInt -> !h[array, idxBool(i)])"
      @   ensures "resInt = -1 -> (\forall i; 0<=i & i<arrlen(array) -> 
      @                            !h[array,idxBool(i)])"
      @   modifies "emptyset"
      @*/
    int first(boolean[] array) {
	spec.Spec.loopinv("((0 <= _i & _i <= arrlen(_array) &\n	     (\\forall j; 0<=j&j<_i -> !h[_array, idxBool(j)])) & modHeap(h, ho, {h:=ho}(emptyset))), arrlen(_array) - _i");





        for(int i = 0; i < array.length; i++) {
            if(array[i]) {
                return i;
            }
        }
        return -1;
    }

    /*@ contract
      @   requires "!array = null"
      @   ensures "resBool = !(\exists i; 0 <= i & i < arrlen(array) & 
      @                            h[array, idxBool(i)])"
      @   modifies "emptyset"
      @*/
    boolean isEmpty(boolean[] array) {
	spec.Spec.loopinv("((0 <= _i & _i <= arrlen(_array) &\n	     (\\forall j; 0<=j&j<_i -> !h[_array, idxBool(j)])) & modHeap(h, ho, {h:=ho}(emptyset))), arrlen(_array) - _i");





	for(int i = 0; i < array.length; i++) {
	    if(array[i]) {
		return false;
	    }
	}
	return true;
    }

    /*@ contract
      @   requires "!array = null"
      @   ensures "(\forall i; 0 <= i & i < arrlen(array) -> 
      @                            !h[array, idxBool(i)])"
      @   modifies "singleton(array)"
      @*/
    void clear(boolean[] array) {
	spec.Spec.loopinv("((0 <= _i & _i <= arrlen(_array) &\n	     (\\forall j; 0<=j&j<_i -> !h[_array, idxBool(j)])) & modHeap(h, ho, {h:=ho}(singleton(_array)))), arrlen(_array) - _i");





	for(int i = 0; i < array.length; i++) {
	    array[i] = false;
	}
    }

    /*@ contract
      @   requires "!target = null"
      @   requires "!source = null"
      @   requires "arrlen(source) = arrlen(target)"
      @   ensures "(\forall i; 0 <= i & i < arrlen(target) -> 
      @     h[target, idxBool(i)] = h[source, idxBool(i)])"
      @   modifies "singleton(target)"
      @*/
    void copy(boolean[] target, boolean[] source) {
	spec.Spec.loopinv("((0 <= _i & _i <= arrlen(_source) &\n	      (\\forall j; 0 <= j & j < _i ->\n	         h[_target, idxBool(j)] = h[_source, idxBool(j)])) & modHeap(h, ho, {h:=ho}(singleton(_target)))), arrlen(_source) - _i");






	for(int i = 0; i < source.length; i++) {
	    target[i] = source[i];
	}
    }

    /*@ contract
      @   requires "!h[this, F_BFS_adjacency] = null"
      @   requires "h[this, F_BFS_size] > 0"
      @   requires "arrlen(h[this, F_BFS_adjacency]) = h[this, F_BFS_size]"
      @   requires "(\forall i; 0<=i & i < h[this, F_BFS_size] ->
      @              !h[h[this, F_BFS_adjacency], idxRef(i)] = null
      @            &   arrlen(h[h[this, F_BFS_adjacency], idxRef(i)]) 
      @              = h[this, F_BFS_size])"
      @   requires "0 <= src & src < h[this, F_BFS_size]"
      @   requires "0 <= dest & dest < h[this, F_BFS_size]"
      @   ensures "-1 <= resInt"
      @   modifies "freshObjects(h)"
      @   decreases "1"
      @*/
    int minDistance(int src, int dest) {

	boolean[] V = new boolean[size];
	boolean[] C = new boolean[size];
	boolean[] N = new boolean[size];
	
	spec.Spec.inline("assert !_V = _C\nassume !_V = _C");
	spec.Spec.inline("assert !_V = _N\nassume !_V = _N");
	spec.Spec.inline("assert !_N = _C\nassume !_N = _C");

	V[src] = true;
	C[src] = true;
	int d = 0;

	spec.Spec.loopinv("((_d >= 0) & modHeap(h, ho, {h:=ho}(singleton(_V) \\/ singleton(_C) \\/ singleton(_N)))), 2");



	while(!isEmpty(C)) {
	    spec.Spec.mark("1");
	    int v = first(C);

	    // C is not empty:
	    spec.Spec.inline("assert 0 <= _v & _v < h[_this, F_BFS_size]\nassume 0 <= _v & _v < h[_this, F_BFS_size]");
	    C[v] = false;
	    if(v == dest) {
		return d;
	    }
	    
	    spec.Spec.loopinv("((0 <= _w (* & _w <= h[_this, F_BFS_size] *)) & modHeap(h, ho, {h:=ho}(singleton(_V) \\/ singleton(_N)))), 2");



	    for(int w = 0; w < size; w++) {
		// ignore this if not a successor ...
		spec.Spec.loopinv("((0 <= _w & _w <= h[_this, F_BFS_size]) & modHeap(h, ho, {h:=ho}(singleton(_V) \\/ singleton(_N)))), 2");



		while(w < size && !adjacency[v][w]) {
		    w++;
                }

		spec.Spec.mark("2");

		if(w < size && !V[w]) {
		    V[w] = true;
		    N[w] = true;
		}
	    }

	    if(isEmpty(C)) {
		copy(C, N);
		clear(N);
		d ++;
	    }    
	}
	
	return -1;
    }
    
}
