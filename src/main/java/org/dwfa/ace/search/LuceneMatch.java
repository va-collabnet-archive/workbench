package org.dwfa.ace.search;

import org.dwfa.vodb.types.ThinDescVersioned;

public class LuceneMatch implements Comparable<LuceneMatch>{
	ThinDescVersioned desc;
	Float score;
	public LuceneMatch(ThinDescVersioned desc, Float score) {
		super();
		this.desc = desc;
		this.score = score;
	}
	public ThinDescVersioned getDesc() {
		return desc;
	}
	public Float getScore() {
		return score;
	}
	public int compareTo(LuceneMatch o) {
		if (this.score.equals(o.score) == false) {
			return Float.compare(this.score, o.score);
		}
		if (this.desc.getFirstTuple().getText().equals(o.desc.getFirstTuple().getText()) == false) {
			return this.desc.getFirstTuple().getText().compareTo(o.desc.getFirstTuple().getText());
		}
		return this.desc.toString().compareTo(o.desc.toString());
	}
	@Override
	public boolean equals(Object obj) {
		if (LuceneMatch.class.isAssignableFrom(obj.getClass())) {
			LuceneMatch another = (LuceneMatch) obj;
			return desc.getDescId() == another.desc.getDescId();
		}
		return false;
	}
	@Override
	public int hashCode() {
		return desc.getDescId();
	}
	
	
}
