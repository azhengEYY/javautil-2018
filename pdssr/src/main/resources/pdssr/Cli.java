package com.pacificdataservices.pdssr;
import org.apache.commons.cli.CommandLineParser;

import java.util.Set;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;


public class Cli {
	
	private Set<String> commands = new Set<String>
	
	public Cli() {
		commands.add("createschema");
		commands.add("seed");
		commands.add("post");
		
	}

	public checkCommand(String command) {
		if (!commands.contains(command)) {
			System.err.println("command must be one of " + commands);
		}
	}
	
	
	
	public static void main(String [] args) {
		checkCommand(args);
		CommandLineParser parser = new DefaultParser();
		Options options = new Options();
		options.addOption(opt)
		CommandLine cmd = parser.parse( options, args);
	}
}
