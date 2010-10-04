package org.ihtsdo.tk.api;


public interface NidSetBI {

    public boolean contains(int nid);

    public int[] getSetValues();

    public void add(int nid);

    public void remove(int nid);

    public NidSetBI addAll(int[] nids);

    public void removeAll(int[] nids);

    public void clear();

	public int size();

	int getMax();

	int getMin();

	boolean contiguous();

}
