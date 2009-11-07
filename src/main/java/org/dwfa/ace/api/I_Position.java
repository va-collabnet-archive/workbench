package org.dwfa.ace.api;

import java.util.Collection;
import java.util.List;


public interface I_Position {

	public I_Path getPath();

	public int getVersion();
	
	public long getTime();

	public boolean isSubsequentOrEqualTo(int version, int pathId);

	public boolean isAntecedentOrEqualTo(int version, int pathId);

	public boolean isAntecedentOrEqualTo(I_Position another);

	public boolean checkAntecedentOrEqualToOrigins(List<I_Position> origins);

	public boolean isSubsequentOrEqualTo(I_Position another);

	public boolean equals(int version, int pathId);

	public int getPositionId();
	
	public Collection<I_Position> getAllOrigins();

}