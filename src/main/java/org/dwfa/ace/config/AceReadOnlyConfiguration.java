package org.dwfa.ace.config;

import org.dwfa.config.Configuration;

public class AceReadOnlyConfiguration extends Configuration {
   public AceReadOnlyConfiguration() {
      super(new AceReadOnlyServices(), false);
   }

}
