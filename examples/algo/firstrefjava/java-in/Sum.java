class Sum {

    /*@ requires array != null;
      @ ensures true; 
      @ */
    int sum(int[] array) {
	int result = 0;
	int i = 0;
	while(i < array.length) {
	    /*@ mark 1; */
	    result += array[i];
	    i++;
	}
	return result;
    }

}