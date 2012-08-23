@spec.SourceFile("./SelSort.jspec") class SelSort {
    
    /*@ contract
      @  requires "!_array = null"
      @  requires "!_array = _this"
      @*/
    void sort(int[] array) {
	
	for(int i = 0; i < array.length - 1; i++) {
	    int t = i;
	    for(int j = i+1; j < array.length; j++) {
		if(array[j] < array[t]) {
		    t = j;
		}
		spec.Spec.mark("1");
	    }
	    int tmp = array[i];
	    array[i] = array[t];
	    array[t] = tmp;
	    spec.Spec.mark("2");
	}
    }
}
		