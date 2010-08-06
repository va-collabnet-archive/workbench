package org.ihtsdo.tk.api;

import java.util.Collection;

public interface PositionBI {

    public PathBI getPath();

    public int getVersion();

    public long getTime();

    public boolean isSubsequentOrEqualTo(int version, int pathId);

    public boolean isAntecedentOrEqualTo(int version, int pathId);

    public boolean isAntecedentOrEqualTo(PositionBI another);

    public boolean checkAntecedentOrEqualToOrigins(Collection<? extends PositionBI> origins);

    public boolean isSubsequentOrEqualTo(PositionBI another);

    public boolean equals(int version, int pathId);

    public Collection<? extends PositionBI> getAllOrigins();

}
