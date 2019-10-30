package com.cherokeelessons.syncorpus;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FileUtils;

/**
 * 
 * @author muksihs
 *
 */
public class Main {

	public static void main(String[] args) throws ParseException, IOException {
		Options options = new Options();
		options.addOption("h", "help", false, "Print out available CLI switches.");
		options.addOption("o", "output", true, "Destination file for the tab delimited output. If not specified, uses stdout.");
		//options.addOption("t", "tee", false, "Write to both the destination file specified by -o and stdout.");
		CommandLineParser parser = new DefaultParser();
		CommandLine cmd = parser.parse( options, args);
		if (cmd.hasOption("h")) {
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("CherokeeSyntheticCorpusGenerator.jar", options );
			return;
		}
		PrintStream out = System.out;
		if (cmd.hasOption("o")) {
			File outputFile = new File(cmd.getOptionValue("o"));
			if (!outputFile.getAbsoluteFile().isDirectory()) {
				throw new IllegalStateException("Directory "+outputFile.getAbsoluteFile().getParent()+" does not exist.");
			}
			if (outputFile.isDirectory()) {
				throw new IllegalStateException(outputFile.getAbsolutePath()+" is a directory.");
			}
			FileUtils.deleteQuietly(outputFile);
			FileUtils.touch(outputFile);
			out = new PrintStream(outputFile, StandardCharsets.UTF_8.name());
		}
		new App(out).run();
	}

}
