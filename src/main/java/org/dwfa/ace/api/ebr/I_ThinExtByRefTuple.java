package org.dwfa.ace.api.ebr;

import java.util.List;

public interface I_ThinExtByRefTuple extends I_ThinExtByRefPart {

	/**
	 * @deprecated Use {@link #getStatusId()}
	 */
   @Deprecated	
   public int getStatus();

   /**
    * @deprecated Use {@link #setStatusId(int)}
    */
   @Deprecated
   public void setStatus(int idStatus);

   public void addVersion(I_ThinExtByRefPart part);

   public int getComponentId();

   public int getMemberId();

   public int getRefsetId();

   public int getTypeId();

   public List<? extends I_ThinExtByRefPart> getVersions();

   public I_ThinExtByRefVersioned getCore();

   public I_ThinExtByRefPart getPart();

}