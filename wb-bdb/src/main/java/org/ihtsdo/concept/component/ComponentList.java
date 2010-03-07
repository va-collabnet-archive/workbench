package org.ihtsdo.concept.component;

import java.util.Collection;
import java.util.concurrent.CopyOnWriteArrayList;

public class ComponentList<E> extends CopyOnWriteArrayList<E> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ComponentList(Collection<? extends E> c) {
		super(c);
	}

	public final boolean addDirect(E e) {
		return super.add(e);
	}


	@Override
	public final boolean addAll(Collection<? extends E> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public final void clear() {
		throw new UnsupportedOperationException();
	}
}
