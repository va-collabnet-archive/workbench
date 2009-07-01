package org.dwfa.ace.select;

import org.dwfa.ace.api.I_DescriptionTuple;

/**
 * Chooses the most appropriate description to be used.
 * 
 * Multiple preference strategies can be used; the strategies themselves being in a preferential order. 
 *  
 * This currently uses a predetermined preferential order however the intention is that this
 * will evolve to use the user's config preferences.
 * 
 * @see Selector#getPreferred(java.util.List)
 */
public class DescriptionSelector extends Selector<I_DescriptionTuple> {

	/**
	 * @param <T> A SelectionStrategy implementation
	 * @param preferences The preferential order of strategies to select a preferred description 
	 */
	public <T extends SelectionStrategy<?, I_DescriptionTuple>> DescriptionSelector(T ... preferences) {
		super(preferences);
	}

	/**
	 * Select a preferred description by ordering by the descriptions language ("en-GB", "en-US", etc)
	 */
	public static class LanguagePreference extends SelectionStrategy<String, I_DescriptionTuple> {
		
		/**
		 * @param languages The preferential order of languages (text)
		 */
		public LanguagePreference(String ... languages) {
			super(languages);
		}
		
		@Override
		protected String getComparableValue(I_DescriptionTuple tuple) {
			return tuple.getLang();
		}
	}
	
	/**
	 * Select a preferred description by ordering by the description's status (current, retired, etc)
	 */
	public static class StatusPreference extends SelectionStrategy<Integer, I_DescriptionTuple> {
		
		/**
		 * @param statuses The preferential order of statuses (native status id)
		 */
		public StatusPreference(Integer ... statuses) {
			super(statuses);
		}
		
		@Override
		protected Integer getComparableValue(I_DescriptionTuple tuple) {
			return tuple.getStatusId();
		}
	}	
	
	/**
	 * Select a preferred description by ordering by the description's type (PT, FSN, etc)
	 */
	public static class TypePreference extends SelectionStrategy<Integer, I_DescriptionTuple> {
		
		/**
		 * @param types The preferential order of description types
		 */
		public TypePreference(Integer ... types) {
			super(types);
		}
		
		@Override
		protected Integer getComparableValue(I_DescriptionTuple tuple) {
			return tuple.getTypeId();
		}
	}	
//	private SelectionStrategy[] preferences;
//	
//	public DescriptionSelector(SelectionStrategy ... preferences) {
//		this.preferences = preferences;
//	}
//	
//	public I_DescriptionTuple getPreferredDescription(List<I_DescriptionTuple> descriptionTuples) {
//		
//		Collections.sort(descriptionTuples, new Comparator<I_DescriptionTuple>() {
//			public int compare(I_DescriptionTuple o1, I_DescriptionTuple o2) {
//				for (SelectionStrategy stategy : preferences) {
//					int result = stategy.compare(o1, o2);
//					if (result != 0) return result;
//				}
//				return 0;
//			}
//		});
//		
//		return descriptionTuples.get(0);
//	}
//	
//	protected abstract static class SelectionStrategy<T> implements Comparator<I_DescriptionTuple> {
//		
//		protected T[] values;
//		
//		public SelectionStrategy(T ... values) {
//			this.values = values;
//		}
//		
//		public int compare(I_DescriptionTuple o1, I_DescriptionTuple o2) {
//			for (T value : values) {
//				if (value.equals(o1.getLang()) && !value.equals(o2.getLang())) {
//					return 1;
//				} else if (value.equals(o2.getLang()) && !value.equals(o1.getLang())) {
//					return -1;
//				}
//			}
//			return 0;
//		}			
//		
//		protected abstract T getValue(I_DescriptionTuple tuple);
//	}
//
//	public static class LangagePreference extends SelectionStrategy<String> {
//		@Override
//		protected String getValue(I_DescriptionTuple tuple) {
//			return tuple.getLang();
//		}
//	}
//	
//	public static class StatusPreference extends SelectionStrategy<Integer> {
//		@Override
//		protected Integer getValue(I_DescriptionTuple tuple) {
//			return tuple.getStatusId();
//		}
//	}	
//
//	public static class TypePreference extends SelectionStrategy<Integer> {
//		@Override
//		protected Integer getValue(I_DescriptionTuple tuple) {
//			return tuple.getTypeId();
//		}
//	}	
	
/*	
	public static class LangagePreference implements SelectionStrategy {

		private String[] languages;
		
		public LangagePreference(String ... languages) {
			this.languages = languages;
		}
		
		public int compare(I_DescriptionTuple o1, I_DescriptionTuple o2) {
			for (String language : languages) {
				if (language.equalsIgnoreCase(o1.getLang()) && !language.equalsIgnoreCase(o2.getLang())) {
					return 1;
				} else if (language.equalsIgnoreCase(o2.getLang()) && !language.equalsIgnoreCase(o1.getLang())) {
					return -1;
				}
			}
			return 0;
		}
		
	}
	
	public static class TypePreference implements SelectionStrategy {
		
		private Integer[] typeIds;
		
		public TypePreference(Integer ... typeIds) {
			this.typeIds = typeIds;
		}
		
		public int compare(I_DescriptionTuple o1, I_DescriptionTuple o2) {
			for (Integer typeId : typeIds) {
				if (o1.getTypeId() == typeId && o2.getTypeId() != typeId) {
					return 1;
				} else if (o2.getTypeId() == typeId && o1.getTypeId() != typeId) {
					return -1;
				}
			}
			return 0;
		}
	}
	
	public static class StatusPreference implements SelectionStrategy {
		
		private Integer[] statusIds;
		
		public StatusPreference(Integer ... statusIds) {
			this.statusIds = statusIds;
		}
		
		public int compare(I_DescriptionTuple o1, I_DescriptionTuple o2) {
			for (Integer typeId : statusIds) {
				if (o1.getStatusId() == typeId && o2.getStatusId() != typeId) {
					return 1;
				} else if (o2.getStatusId() == typeId && o1.getStatusId() != typeId) {
					return -1;
				}
			}
			return 0;
		}
	}
*/	
}
