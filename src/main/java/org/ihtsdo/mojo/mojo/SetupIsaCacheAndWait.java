/**
 * Copyright (c) 2009 International Health Terminology Standards Development
 * Organisation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */



package org.ihtsdo.mojo.mojo;

//~--- non-JDK imports --------------------------------------------------------

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.ihtsdo.db.bdb.computer.kindof.KindOfComputer;

/**
 *
 * @goal vodb-setup-isa-cache
 *
 * @phase process-resources
 * @requiresDependencyResolution compile
 */
public class SetupIsaCacheAndWait extends AbstractMojo {
   public void execute() throws MojoExecutionException, MojoFailureException {
      try {
         I_ConfigAceFrame activeConfig = Terms.get().getActiveAceFrameConfig();
         I_TermFactory    tf           = Terms.get();

         getLog().info("Setting up Is-a cache...");

         if (Terms.get().getActiveAceFrameConfig().getViewCoordinate().getIsaCoordinates().size() != 1) {
            throw new MojoExecutionException(
                "Only one is-a coordinate allowed. Found: "
                + Terms.get().getActiveAceFrameConfig().getViewCoordinate().getIsaCoordinates());
         }
         
         KindOfComputer.persistIsaCache = false;

         Terms.get().setupIsaCacheAndWait(
             Terms.get().getActiveAceFrameConfig().getViewCoordinate().getIsaCoordinates().iterator().next());
         getLog().info("Is-a created OK...");
      } catch (Exception e) {
         throw new MojoExecutionException(e.getLocalizedMessage(), e);
      }
   }
}


//~ Formatted by Jindent --- http://www.jindent.com
