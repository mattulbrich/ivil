/*
 * This is an example for ivil
 *
 */

class Test {

    private int size;
    private Test next;

    /*@ assumes (\forall Test t; t.size >= 0);
      @ ensures \result >= 0;
      @ modifies \nothing;
      @*/
    public int sum() {
        int sum = 0;
        Test n = this;
        while(n != null) {
            sum += n.size;
            n = n.next;
        }
        return sum;
    }
}
