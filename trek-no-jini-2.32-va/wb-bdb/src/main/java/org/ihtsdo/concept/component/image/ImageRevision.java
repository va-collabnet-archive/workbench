package org.ihtsdo.concept.component.image;

//~--- non-JDK imports --------------------------------------------------------

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

import java.io.IOException;
import org.apache.commons.collections.primitives.ArrayIntList;

import org.dwfa.ace.api.I_ImagePart;
import org.dwfa.ace.api.I_MapNativeToNative;
import org.dwfa.ace.api.Terms;

import org.ihtsdo.concept.component.ConceptComponent;
import org.ihtsdo.concept.component.Revision;
import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.tk.api.ContradictionException;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.media.MediaAnalogBI;
import org.ihtsdo.tk.api.media.MediaVersionBI;
import org.ihtsdo.tk.dto.concept.component.media.TkMediaRevision;

//~--- JDK imports ------------------------------------------------------------

import java.util.Collection;
import java.util.Set;
import org.ihtsdo.tk.api.blueprint.InvalidCAB;
import org.ihtsdo.tk.api.blueprint.MediaCAB;
import org.ihtsdo.tk.dto.concept.component.TkRevision;

public class ImageRevision extends Revision<ImageRevision, Image>
        implements I_ImagePart<ImageRevision>, MediaAnalogBI<ImageRevision> {
   private String textDescription;
   private int    typeNid;

   //~--- constructors --------------------------------------------------------

   protected ImageRevision() {
      super();
   }

   ImageRevision(Image primoridalMember) {
      super(primoridalMember.primordialSapNid, primoridalMember);
      this.textDescription = primoridalMember.getTextDescription();
      this.typeNid         = primoridalMember.getTypeNid();
   }

   ImageRevision(ImageRevision another, Image primoridalMember) {
      super(another.sapNid, primoridalMember);
      this.textDescription = another.textDescription;
      this.typeNid         = another.typeNid;
   }

   public ImageRevision(TkMediaRevision eiv, Image primoridalMember) {
      super(Bdb.uuidToNid(eiv.getStatusUuid()), eiv.getTime(), Bdb.uuidToNid(eiv.getAuthorUuid()),
            Bdb.uuidToNid(eiv.getModuleUuid()), Bdb.uuidToNid(eiv.getPathUuid()), primoridalMember);
      this.textDescription = eiv.getTextDescription();
      this.typeNid         = Bdb.uuidToNid(eiv.getTypeUuid());
   }

   protected ImageRevision(TupleInput input, Image primoridalMember) {
      super(input.readInt(), primoridalMember);
      this.textDescription = input.readString();
      this.typeNid         = input.readInt();
   }
   
   protected ImageRevision(I_ImagePart another, int statusNid, long time, int authorNid,
           int moduleNid, int pathNid, Image primoridalMember) {
      super(statusNid, time, authorNid, moduleNid, pathNid, primoridalMember);
      this.textDescription = another.getTextDescription();
      this.typeNid         = another.getTypeNid();
   }

   //~--- methods -------------------------------------------------------------

   @Override
   protected void addComponentNids(Set<Integer> allNids) {
      allNids.add(typeNid);
   }

   @Override
   public void convertIds(I_MapNativeToNative jarToDbNativeMap) {
      throw new UnsupportedOperationException();
   }

   @Override
   public ImageRevision duplicate() {
      throw new UnsupportedOperationException();
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == null) {
         return false;
      }

      if (ImageRevision.class.isAssignableFrom(obj.getClass())) {
         ImageRevision another = (ImageRevision) obj;

         if (this.sapNid == another.sapNid) {
            return true;
         }
      }

      return false;
   }
   
   @Override
   public ImageRevision makeAnalog(int statusNid, long time, int authorNid, int moduleNid, int pathNid) {
      if ((this.getTime() == time) && (this.getPathNid() == pathNid)) {
         this.setStatusNid(statusNid);
         this.setAuthorNid(authorNid);
         this.setModuleNid(moduleNid);

         return this;
      }

      ImageRevision newR;

      newR = new ImageRevision(this, statusNid, time, authorNid,
              moduleNid, pathNid,this.primordialComponent);
      this.primordialComponent.addRevision(newR);

      return newR;
   }
   
   @Override
    public MediaCAB makeBlueprint(ViewCoordinate vc) throws IOException, ContradictionException, InvalidCAB{
        MediaCAB mediaBp = new MediaCAB(getConceptNid(),
                getTypeNid(),
                getFormat(),
                getTextDescription(),
                getMedia(),
                getVersion(vc),
                vc);
        return mediaBp;
    }

   @Override
   public boolean readyToWriteRevision() {
      assert textDescription != null : assertionString();
      assert typeNid != Integer.MAX_VALUE : assertionString();

      return true;
   }

   /*
    *  (non-Javadoc)
    * @see java.lang.Object#toString()
    */
   @Override
   public String toString() {
      StringBuilder buf = new StringBuilder();

      buf.append(this.getClass().getSimpleName()).append(":{");
      buf.append(" textDescription:" + "'").append(this.textDescription).append("'");
      buf.append(" typeNid:").append(this.typeNid);
      buf.append(super.toString());

      return buf.toString();
   }

   @Override
   public String toUserString() {
      StringBuffer buf = new StringBuffer();

      ConceptComponent.addTextToBuffer(buf, typeNid);
      buf.append("; ");
      buf.append(primordialComponent.getFormat());
      buf.append(": ");
      buf.append(textDescription);

      return buf.toString();
   }

   @Override
   protected void writeFieldsToBdb(TupleOutput output) {
      output.writeString(textDescription);
      output.writeInt(typeNid);
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public int getConceptNid() {
      return primordialComponent.enclosingConceptNid;
   }

   @Override
   public String getFormat() {
      return primordialComponent.getFormat();
   }

   @Override
   public byte[] getMedia() {
      return primordialComponent.getMedia();
   }

   @Override
   public Image getPrimordialVersion() {
      return primordialComponent;
   }

   /*
    *  (non-Javadoc)
    * @see org.dwfa.vodb.types.I_ImagePart#getTextDescription()
    */
   @Override
   public String getTextDescription() {
      return textDescription;
   }

   /*
    *  (non-Javadoc)
    * @see org.dwfa.vodb.types.I_ImagePart#setTextDescription(java.lang.String)
    */

   @Deprecated
   @Override
   public int getTypeId() {
      return typeNid;
   }

   /*
    *  (non-Javadoc)
    * @see org.dwfa.vodb.types.I_ImagePart#setTypeId(int)
    */

   @Override
   public int getTypeNid() {
      return typeNid;
   }

   /*
    *  (non-Javadoc)
    * @see org.dwfa.vodb.types.I_ImagePart#setTypeId(int)
    */

   @Override
   public ArrayIntList getVariableVersionNids() {
      ArrayIntList partComponentNids = new ArrayIntList(3);

      partComponentNids.add(typeNid);

      return partComponentNids;
   }

   @Override
   public Image.Version getVersion(ViewCoordinate c) throws ContradictionException {
      return primordialComponent.getVersion(c);
   }

   @Override
   public Collection<? extends MediaVersionBI> getVersions() {
      return ((Image) primordialComponent).getVersions();
   }

   @Override
   public Collection<Image.Version> getVersions(ViewCoordinate c) {
      return primordialComponent.getVersions(c);
   }

   /*
    *  (non-Javadoc)
    * @see org.dwfa.vodb.types.I_ImagePart#hasNewData(org.dwfa.vodb.types.ThinImagePart)
    */
   public boolean hasNewData(ImageRevision another) {
      return ((this.getPathNid() != another.getPathNid()) || (this.getStatusNid() != another.getStatusNid())
              || ((this.textDescription.equals(another.getTextDescription()) == false)
                  || (this.typeNid != another.getTypeNid())));
   }

   /*
    *  (non-Javadoc)
    * @see org.dwfa.vodb.types.I_ImagePart#convertIds(org.dwfa.vodb.jar.I_MapNativeToNative)
    */

   //~--- set methods ---------------------------------------------------------

   @Override
   public void setTextDescription(String name) {
      this.textDescription = name;
      modified();
   }

   /*
    *  (non-Javadoc)
    * @see org.dwfa.vodb.types.I_ImagePart#getTypeId()
    */

   @Deprecated
   @Override
   public void setTypeId(int type) {
      this.typeNid = type;
      modified();
   }

   @Override
   public void setTypeNid(int type) {
      this.typeNid = type;
      modified();
   }
}
