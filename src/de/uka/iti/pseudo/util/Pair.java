package de.uka.iti.pseudo.util;

public class Pair<E,F> {
	
	
	
	private E fstComponent;
	private F sndComponent;

	public E fst() {
		return fstComponent;
	}
	
	public F snd() {
		return sndComponent;
	}

	public boolean equals(Object obj) {
		if (obj instanceof Pair<?,?>) {
			Pair<?,?> pair = (Pair<?,?>) obj;
			return (fst() == null ? pair.fst() == null : fst().equals(pair.fst())) &&
				(snd() == null ? pair.snd() == null : snd().equals(pair.snd()));
		} else {
			return false;
		}
	}

	public Pair(E fst, F snd) {
		super();
		this.fstComponent = fst;
		this.sndComponent = snd;
	}
}
