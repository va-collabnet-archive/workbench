package org.ihtsdo.tk.api;

import java.util.Collection;

public interface PositionBI {

    public PathBI getPath();

    public int getVersion();

    public long getTime();

    public boolean isSubsequentOrEqualTo(int version, int pathNid);

    public boolean isAntecedentOrEqualTo(int version, int pathNid);

    public boolean isSubsequentOrEqualTo(long time, int pathNid);

    public boolean isAntecedentOrEqualTo(long time, int pathNid);

    public boolean isAntecedentOrEqualTo(PositionBI another);

    public boolean checkAntecedentOrEqualToOrigins(Collection<? extends PositionBI> origins);

    public boolean isSubsequentOrEqualTo(PositionBI another);

    public boolean equals(int version, int pathNid);

    public boolean equals(long time, int pathNid);

    public Collection<? extends PositionBI> getAllOrigins();

}
