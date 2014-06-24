package org.ihtsdo.mojo.pbl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.logging.Logger;

import org.apache.maven.plugin.logging.Log;

public class Executor {
	public static int executeCommand(String[] command, Logger log)
			throws IOException, InterruptedException {
		ProcessBuilder pb = new ProcessBuilder(command);
		pb.redirectErrorStream(true);
		log.info(Arrays.asList(command).toString());

		Process p = pb.start();
		InputStream is = p.getInputStream();
		InputStreamReader isr = new InputStreamReader(is);
		BufferedReader br = new BufferedReader(isr);
		String line;

		log.info("Output:");

		while ((line = br.readLine()) != null) {
			log.info(line);
		}
		return p.waitFor();
	}

	public static int executeCommand(String[] command, Log log)
			throws IOException, InterruptedException {
		ProcessBuilder pb = new ProcessBuilder(command);
		pb.redirectErrorStream(true);
		log.info(Arrays.asList(command).toString());

		Process p = pb.start();
		InputStream is = p.getInputStream();
		InputStreamReader isr = new InputStreamReader(is);
		BufferedReader br = new BufferedReader(isr);
		String line;

		log.info("Output:");

		while ((line = br.readLine()) != null) {
			log.info(line);
		}
		return p.waitFor();
	}

}
