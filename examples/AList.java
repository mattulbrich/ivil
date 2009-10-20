class AList {

    private Object[] elements = new Object[0];

    public Object get(int i) {
	return elements[i];
    }

    public void set(int i, Object o) {
	elements[i] = o;
    }

    private void arraycopy(Object[] from, Object[] to) {
	int len = Math.min(from.length, to.length);
	for(int i = 0; i < len; i++) {
	    to[i] = from[i];
	}
    }
		
    public void setLength(int length) {
	if(elements.length == length)
	    return;

	Object[] newEl = new Object[length];
        arraycopy(elements, newEl);
    }

    public static boolean test() {
	AList a = new AList();
	a.setLength(100);
	a.set(0, a);
	for(int i = 1; i < 100; i++) {
	    a.set(i, new Object());
	}
	a.setLength(2);
	return a.get(0) == a;
    }
}
	
