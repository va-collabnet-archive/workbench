package org.dwfa.ace.select;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * A generic class for selecting the preferred element from a collection. 
 * 
 * The implementing class will define both:<ul>
 * <li> 1. The type of object that it can handle
 * <li> 2. Preferences class (which implement {@link SelectionStrategy}) that influence its behaviour 
 * <br>
 * @param <E> The type of the objects handled by an implementation
 * @see DescriptionSelector 
 */
public class Selector<E> {

	private SelectionStrategy<?, E>[] preferences;
	
	public <V extends SelectionStrategy<?, E>> Selector(V ... preferences) {
		this.preferences = preferences;
	}
	
	/**
	 * Get the most preferred item from a collection
	 */
	public E getPreferred(List<E> items) {
		
		Collections.sort(items, new Comparator<E>() {
			public int compare(E o1, E o2) {
				for (SelectionStrategy<?, E> stategy : preferences) {
					int result = stategy.compare(o1, o2);
					if (result != 0) return result;
				}
				return 0;
			}
		});
		
		return items.get(0);
	}
	
	
	/**
	 * 
	 * @param <T> The type of configuration parameters that may be passed to the implementing class
	 * @param <O> The type of the objects to be compared
	 */
	protected static abstract class SelectionStrategy<T, O> implements Comparator<O> {
		
		protected T[] values;
		
		public SelectionStrategy(T ... values) {
			this.values = values;
		}
		
		public int compare(O o1, O o2) {
			for (T value : values) {
				if (value.equals(getComparableValue(o1)) && !value.equals(getComparableValue(o2))) {
					return -1;
				} else if (value.equals(getComparableValue(o2)) && !value.equals(getComparableValue(o1))) {
					return 1;
				}
			}
			return 0;
		}			
		
		/**
		 * Concrete implementations to override and obtain the appropriate value from a specific type of object.
		 */
		protected abstract T getComparableValue(O object);
	}

}
