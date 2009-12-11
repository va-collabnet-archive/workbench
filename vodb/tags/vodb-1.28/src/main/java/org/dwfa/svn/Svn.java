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
package org.dwfa.svn;



import java.util.logging.Level;

import org.dwfa.ace.log.AceLog;
import org.tigris.subversion.javahl.NodeKind;
import org.tigris.subversion.javahl.Notify2;
import org.tigris.subversion.javahl.NotifyAction;
import org.tigris.subversion.javahl.NotifyInformation;
import org.tigris.subversion.javahl.NotifyStatus;
import org.tigris.subversion.javahl.SVNClient;
import org.tigris.subversion.javahl.SVNClientInterface;
import org.tmatesoft.svn.core.javahl.SVNClientImpl;

public class Svn {
	
	enum SvnImpl {NATIVE, SVN_KIT};
	
	private static SvnImpl impl = SvnImpl.SVN_KIT;
	
	private static SVNClientInterface client;

	public static SVNClientInterface getSvnClient() {
		if (client == null) {
			switch (impl) {
			case NATIVE:
				client = new SVNClient();
				AceLog.getAppLog().info("Created native svn client: " + client);
				break;
				
			case SVN_KIT:
				client = SVNClientImpl.newInstance();
				AceLog.getAppLog().info("Created Svnkit pure java svn client: " + client);
				break;

			default:
				throw new RuntimeException("Can't handle svn impl type: " + impl);
			}
			client.setPrompt(new SvnPrompter());
			// The SVNClient needs an implementation of Notify before
			// successfully executing any other methods.
			client.notification2(new Notify2() {
				public void onNotify(NotifyInformation info) {
                    try {
                        if (AceLog.getAppLog().isLoggable(Level.FINE)) {
                            String path = info.getPath();
                            String nodeKindName = NodeKind.getNodeKindName(info.getKind());
                            String contentStateName = convertStatus(info.getContentState());
                            String propertyStateName = convertStatus(info.getPropState());
                            String errorMsg = info.getErrMsg();
                            String mimeType = info.getMimeType();
                            String revision = Long.toString(info.getRevision());
                            String lock = toString(info.getLock());
                            String lockState = Integer.toString(info.getLockState());
                            String action = convertAction(info.getAction());
                            
                            SvnLog.info("svn onNotify: " + " path: " + path
                                    + "\n" + " kind: "
                                    + nodeKindName + " "
                                    + " content state: "
                                    + contentStateName
                                    + " prop state: "
                                    + propertyStateName
                                    + " \n" + " err msg: " + errorMsg + " "
                                    + " mime : " + mimeType + " "
                                    + " revision: " + revision + " \n"
                                    + " lock: " + lock + " "
                                    + " lock state: " + lockState + " "
                                    + " action: "
                                    + action);
                        }
                    } catch (Throwable t) {
                        AceLog.getAppLog().alertAndLogException(t);
                    }
				}

                private String toString(Object obj) {
                    if (obj == null) {
                        return "null";
                    }
                    return obj.toString();
                }
                private String convertAction(int infoAction) {
                     if (infoAction >= 0 && infoAction < NotifyAction.actionNames.length) {
                        return NotifyAction.actionNames[infoAction];
                    }
                    return Integer.toString(infoAction);
                }
                private String convertStatus(int infoState) {
                    if (infoState >= 0 && infoState < NotifyStatus.statusNames.length) {
                        return NotifyStatus.statusNames[infoState];
                    }
                    return Integer.toString(infoState);
                }
			});
		}
		return client;
	}
}
