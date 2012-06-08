class First {

    /*@ 
      @ ensures true;
      @ diverges true;
      @*/
    int first(int n) {
        int sum = 0;

        /*@ loop_invariant true;
          @*/
        for (int i = 1; i <= n; i++) {
	    /*@ mark 1; */
            sum += i;
        }

        return sum;
    }
}
