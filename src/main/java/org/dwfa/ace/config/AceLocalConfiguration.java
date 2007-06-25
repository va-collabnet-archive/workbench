package org.dwfa.ace.config;

import org.dwfa.config.Configuration;

public class AceLocalConfiguration extends Configuration {
	public AceLocalConfiguration() {
		super(new AceLocalServices(), false, true);
	}

}

