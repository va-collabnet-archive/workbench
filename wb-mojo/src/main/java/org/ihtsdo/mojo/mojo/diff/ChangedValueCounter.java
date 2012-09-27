package org.ihtsdo.mojo.mojo.diff;

import java.util.ArrayList;
import java.util.Arrays;

public class ChangedValueCounter {

	String name;

	private ArrayList<Integer> values = new ArrayList<Integer>();

	private int[][] changes = new int[0][0];

	public ChangedValueCounter(String name) {
		super();
		this.name = name;
	}

	private void ensureValue(Integer v) {
		if (!values.contains(v)) {
			values.add(v);
			int[][] new_changes = new int[values.size()][];
			for (int i = 0; i < changes.length; i++) {
				new_changes[i] = Arrays.copyOf(changes[i], values.size());
			}
			int[] add = new int[values.size()];
			Arrays.fill(add, 0);
			new_changes[values.size() - 1] = add;
			changes = new_changes;
			System.out.println("Values increased to: " + values.size());
		}
	}

	public void changedValue(Integer v1, Integer v2) {
		ensureValue(v1);
		ensureValue(v2);
		changes[values.indexOf(v1)][values.indexOf(v2)]++;
	}

	public ArrayList<Integer> getValues() {
		return values;
	}

	public int[][] getChanges() {
		return changes;
	}

	public String getName() {
		return name;
	}

}
