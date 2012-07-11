package org.ihtsdo.tk.api;

//~--- non-JDK imports --------------------------------------------------------

import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.id.IdBI;
import org.ihtsdo.tk.api.refex.RefexChronicleBI;
import org.ihtsdo.tk.api.refex.RefexVersionBI;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface ComponentBI {
   boolean addAnnotation(RefexChronicleBI<?> annotation) throws IOException;

   String toUserString();

   //~--- get methods ---------------------------------------------------------

   Collection<? extends IdBI> getAdditionalIds() throws IOException;

   Collection<? extends IdBI> getAllIds() throws IOException;

   Collection<? extends RefexChronicleBI<?>> getAnnotations() throws IOException;

   int getConceptNid();
   
   /**
    * Returns the annotations on the component
    * @param viewCoordinate
    * @return
    * @throws IOException 
    */
   Collection<? extends RefexVersionBI<?>> getAnnotationsActive(ViewCoordinate viewCoordinate) throws IOException;

   Collection<? extends RefexVersionBI<?>> getAnnotationMembersActive(ViewCoordinate viewCoordinate, int refexNid)
           throws IOException;

   /**
    *
    * @param viewCoordinate
    * @return
    * @throws IOException
    * @deprecated use getAnnotationsActive
    */
   @Deprecated
   Collection<? extends RefexVersionBI<?>> getActiveAnnotations(ViewCoordinate viewCoordinate) throws IOException;

   /**
    *
    * @param viewCoordinate
    * @param refexNid
    * @return
    * @throws IOException
    * @deprecated use getAnnotationsActive
    */
   @Deprecated
   Collection<? extends RefexVersionBI<?>> getActiveAnnotations(ViewCoordinate viewCoordinate, int refexNid)
           throws IOException;
   /**
    * Returns any annotations on the component, or any members that are a "referenced component".
    * Refsets can only be on a concept not on a component.
    * @param viewCoordinate
    * @param refexNid
    * @return
    * @throws IOException 
    */
   Collection<? extends RefexVersionBI<?>> getRefexMembersActive(ViewCoordinate viewCoordinate, int refexNid)
           throws IOException;

   Collection<? extends RefexVersionBI<?>> getRefexesActive(ViewCoordinate viewCoordinate) throws IOException;

   /**
    *
    * @param viewCoordinate
    * @param refexNid
    * @return
    * @throws IOException
    * @deprecated use getRefexMembersActive
    */
   @Deprecated
   Collection<? extends RefexVersionBI<?>> getActiveRefexes(ViewCoordinate viewCoordinate, int refexNid)
           throws IOException;

   Collection<? extends RefexVersionBI<?>> getRefexesInactive(ViewCoordinate viewCoordinate) throws IOException;

   int getNid();

   /**
    *
    * @return the primordial if known. The IUnknown UUID (00000000-0000-0000-C000-000000000046) if not known.
    */
   UUID getPrimUuid();

   Collection<? extends RefexChronicleBI<?>> getRefexMembers(int refexNid) throws IOException;

   Collection<? extends RefexChronicleBI<?>> getRefexes() throws IOException;

   /**
    *
    * @param refexNid
    * @return
    * @throws IOException
    * @deprecated use getRefexMembers
    */
   @Deprecated
   Collection<? extends RefexChronicleBI<?>> getRefexes(int refexNid) throws IOException;

   List<UUID> getUUIDs();

   boolean hasAnnotationMemberActive(ViewCoordinate viewCoordinate, int refexNid) throws IOException;

   boolean hasRefexMemberActive(ViewCoordinate viewCoordinate, int refexNid) throws IOException;
}
