class First {

    /*@ 
      @ ensures true;
      @ diverges true;
      @*/
    int first(int n) {
        int sum = 0;

        de.uka.iti.ivil.jbc.Spec.special("/*@ loop_invariant true;\n          @*/");

        for (int i = 1; i <= n; i++) {
	    de.uka.iti.ivil.jbc.Spec.special("/*@ mark 1; */");
            sum += i;
        }

        return sum;
    }
}
