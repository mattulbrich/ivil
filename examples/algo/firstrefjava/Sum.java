@spec.SourceFile("./Sum.jspec") class Sum {

    /*@ contract
      @   requires "!_array = null"
      @   ensures "true" 
      @ */
    int sum(int[] array) {
	int result = 0;
	int i = 0;
	while(i < array.length) {
	    spec.Spec.mark("1");
	    result += array[i];
	    i++;
	    spec.Spec.mark("2");
	}
	return result;
    }

}