package org.dwfa.ace.table.refset;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import org.dwfa.ace.api.I_HoldRefsetPreferences;
import org.dwfa.ace.refset.I_RefsetDefaultsBoolean;
import org.dwfa.ace.refset.I_RefsetDefaultsConcept;
import org.dwfa.ace.refset.I_RefsetDefaultsInteger;
import org.dwfa.ace.refset.I_RefsetDefaultsLanguage;
import org.dwfa.ace.refset.I_RefsetDefaultsLanguageScoped;
import org.dwfa.ace.refset.I_RefsetDefaultsMeasurement;
import org.dwfa.tapi.TerminologyException;

public class RefsetPreferences implements I_HoldRefsetPreferences, Serializable {

   /**
    * 
    */
   private static final long serialVersionUID = 1L;

   private static final int dataVersion = 1;
   
   private I_RefsetDefaultsBoolean booleanPreferences = new RefsetDefaultsBoolean();
   private I_RefsetDefaultsConcept conceptPreferences = new RefsetDefaultsConcept();
   private I_RefsetDefaultsInteger integerPreferences = new RefsetDefaultsInteger();
   private I_RefsetDefaultsLanguage languagePreferences = new RefsetDefaultsLanguage();
   private I_RefsetDefaultsLanguageScoped languageScopedPreferences = new RefsetDefaultsLanguageScoped();
   private I_RefsetDefaultsMeasurement measurementPreferences = new RefsetDefaultsMeasurement();

   private void writeObject(ObjectOutputStream out) throws IOException {
      out.writeInt(dataVersion);
      out.writeObject(booleanPreferences);
      out.writeObject(conceptPreferences);
      out.writeObject(integerPreferences);
      out.writeObject(languagePreferences);
      out.writeObject(languageScopedPreferences);
      out.writeObject(measurementPreferences);
   }

   @SuppressWarnings("unchecked")
   private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
      int objDataVersion = in.readInt();
      if (objDataVersion == dataVersion) {
         booleanPreferences = (I_RefsetDefaultsBoolean) in.readObject();
         conceptPreferences = (I_RefsetDefaultsConcept) in.readObject();
         integerPreferences = (I_RefsetDefaultsInteger) in.readObject();
         languagePreferences = (I_RefsetDefaultsLanguage) in.readObject();
         languageScopedPreferences = (I_RefsetDefaultsLanguageScoped) in.readObject();
         measurementPreferences = (I_RefsetDefaultsMeasurement) in.readObject();
      } else {
         throw new IOException("Can't handle dataversion: " + objDataVersion);
      }

   }
   
   public RefsetPreferences() throws TerminologyException, IOException {
      super();
   }
   public I_RefsetDefaultsBoolean getBooleanPreferences() {
      return booleanPreferences;
   }

   public I_RefsetDefaultsConcept getConceptPreferences() {
      return conceptPreferences;
   }

   public I_RefsetDefaultsInteger getIntegerPreferences() {
      return integerPreferences;
   }

   public I_RefsetDefaultsLanguage getLanguagePreferences() {
      return languagePreferences;
   }

   public I_RefsetDefaultsMeasurement getMeasurementPreferences() {
      return measurementPreferences;
   }

   public I_RefsetDefaultsLanguageScoped getLanguageScopedPreferences() {
      return languageScopedPreferences;
   }

}
