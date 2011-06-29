package org.dwfa.ace.config;

import java.awt.Container;

import org.dwfa.ace.task.svn.SvnPrompter;
/**
 * A shell around SVNPrompter to allow for changes 
 * @author adfl
 *
 */
public class UIAuthenticator {

	//Store UN etc in the SVN Prompter
	public SvnPrompter prompt = new SvnPrompter();
	
	private Container parentContainer = null;
	
	public String authenticate(){
		String result = testCredentials();
		
		
		return result;
	}
	
	public String testCredentials(){
		String result = "";
		
		
		return result;
	}

	public SvnPrompter getPrompt() {
		return prompt;
	}

	public void setPrompt(SvnPrompter prompt) {
		this.prompt = prompt;
	}

	public Container getParentContainer() {
		return parentContainer;
	}

	public void setParentContainer(Container parentContainer) {
		this.parentContainer = parentContainer;
	}
	

}
