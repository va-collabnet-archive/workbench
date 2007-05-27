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
		return Float.compare(this.score, o.score);
	}
}
