import java.util.Arrays;

public class PrioQ {

    int[] h;
    int size;
    
    public PrioQ() {
        this(8);
    }
    
    public PrioQ(int initial) {
        h = new int[initial];
    }

    public int take() {
        int result = h[0];
        int last = h[size - 1];
        size --;
        
        if(size == 0)
            return result;
        
        h[0] = last;
        int i = 0;
        while(i < size/2) {
            
//            assert isValid(i) : "Failed in take at " + toString() + " for " + i;
            
            int min;
            if(2 * i + 2 == size) {
                min = 2 * i + 1;
            } else if(h[2*i+1] > h[2*i + 2]) {
                min = 2 * i + 2;
            } else {
                min = 2 * i + 1;
            }
            
            if(h[i] > h[min]) {
                int tmp = h[i];
                h[i] = h[min];
                h[min] = tmp;
                i = min;
            } else {
                i = size;
            }
            
//            assert isValid(i);
        }
        
        return result;
    }
    
    public void add(int v) {

        if(h.length == size) {
            h = Arrays.copyOf(h, 2*h.length);
        }
        
        size ++;
        h[size - 1] = v;
        int i = size -1;
        
        while(i > 0) {
            
            int p = (i-1)/2;
            assert isValid(p);
            
            if(h[p] > h[i]) {
                int tmp = h[i];
                h[i] = h[p];
                h[p] = tmp;
                i = p;
            } else {
                i = 0;
            }
            
            assert isValid(p);
        }
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < size; i++) {
            if(i > 0) {
                sb.append(", ");
            }
            sb.append(h[i]);
        }
        sb.append("]");
        return sb.toString();
    }
    
    boolean isValid(int except) {
        return isValid(h, size, except);
    }

    static boolean isValid(int[] array, int size, int except) {
        for (int i = 1; i < size; i++) {
            int x = (i-1)/2;
            if(x == except)
                continue;
            if(array[x] > array[i])
                return false;
        }
        return true;
    }

    public void set(int... values) {
        h = values;
        size = h.length;
        
        assert isValid(0);
    }
    
}
