package org.dwfa.maven.transform;

import java.io.IOException;

public class SimpleExportNullIsEmpty extends SimpleExport {
   public void writeRec() throws IOException {
      for (int i = 0; i < transformers.length; i++) {
         if (i != 0) {
            w.append(getOutputColumnDelimiter());
         }
         if (transformers[i].getLastTransform() == null) {
            //don't write anything
         } else if (transformers[i].getLastTransform().equals("null")) {
            //don't write anything
         } else {
            w.append(transformers[i].getLastTransform());
         }
      }
      w.append('\n');
   }


}
