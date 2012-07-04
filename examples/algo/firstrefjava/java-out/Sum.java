class Sum {

    /*@ requires array != null;
      @ ensures true; 
      @ */
    int sum(int[] array) {
	int result = 0;
	int i = 0;
	while(i < array.length) {
	    de.uka.iti.ivil.jbc.Spec.special("/*@ mark 1; */");
	    result += array[i];
	    i++;
	    de.uka.iti.ivil.jbc.Spec.special("/*@ mark 2; */");
	}
	return result;
    }

}