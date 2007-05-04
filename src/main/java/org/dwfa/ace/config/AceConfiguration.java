package org.dwfa.ace.config;

import org.dwfa.config.Configuration;

public class AceConfiguration extends Configuration {
	public AceConfiguration() {
		super(new AceServices(), false);
	}

}
