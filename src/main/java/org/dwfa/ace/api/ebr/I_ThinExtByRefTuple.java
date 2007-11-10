package org.dwfa.ace.api.ebr;

import java.util.List;

public interface I_ThinExtByRefTuple extends I_ThinExtByRefPart {

   public int getPathId();

   public int getStatus();

   public int getVersion();

   public void setPathId(int pathId);

   public void setStatus(int idStatus);

   public void setVersion(int version);

   public void addVersion(I_ThinExtByRefPart part);

   public int getComponentId();

   public int getMemberId();

   public int getRefsetId();

   public int getTypeId();

   public List<? extends I_ThinExtByRefPart> getVersions();

   public I_ThinExtByRefVersioned getCore();

   public I_ThinExtByRefPart getPart();

}