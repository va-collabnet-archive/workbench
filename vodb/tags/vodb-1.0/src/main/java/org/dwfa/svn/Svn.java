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



import org.dwfa.ace.log.AceLog;
import org.tigris.subversion.javahl.ClientException;
import org.tigris.subversion.javahl.NodeKind;
import org.tigris.subversion.javahl.Notify2;
import org.tigris.subversion.javahl.NotifyAction;
import org.tigris.subversion.javahl.NotifyInformation;
import org.tigris.subversion.javahl.NotifyStatus;
import org.tigris.subversion.javahl.Revision;
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
					SvnLog.info("svn onNotify: " + " path: " + info.getPath()
							+ "\n" + " kind: "
							+ NodeKind.getNodeKindName(info.getKind()) + " "
							+ " content state: "
							+ NotifyStatus.statusNames[info.getContentState()]
							+ " " + " prop state: "
							+ NotifyStatus.statusNames[info.getPropState()]
							+ " \n" + " err msg: " + info.getErrMsg() + " "
							+ " mime : " + info.getMimeType() + " "
							+ " revision: " + info.getRevision() + " \n"
							+ " lock: " + info.getLock() + " "
							+ " lock state: " + info.getLockState() + " "
							+ " action: "
							+ NotifyAction.actionNames[info.getAction()]);
				}
			});
		}
		return client;
	}

	/**
	 * @param args
	 * @throws ClientException
	 */
	public static void main(String[] args) throws ClientException {

		String localRepo = "target/maven-javadoc-plugin";
		AceLog.getAppLog().info("checkout");
		getSvnClient()
				.checkout(
						"http://svn.apache.org/repos/asf/maven/plugins/trunk/maven-javadoc-plugin",
						localRepo, Revision.HEAD, true);
		AceLog.getAppLog().info("cleanup");
		getSvnClient().cleanup(localRepo);
		AceLog.getAppLog().info(
				"client version: " + getSvnClient().info(localRepo));
	}
}
