package nl.chrishofstede.bulktransform;

import org.apache.commons.cli.*;

/**
 * Hello world!
 *
 */
public class App {
    final static Options options = new Options();

    public static void main(String[] args) {
        System.out.println("Hello World!");
        options.addOption(new Option("in", "input", true, "input file name (wildcards allowed)"));
        options.addOption(new Option("xsl", "xslt", true, "XSLT stylesheet input file name"));
        options.addOption(new Option("out", "output", true, "output folder"));

        // create the parser
        CommandLineParser parser = new DefaultParser();
        try {
            // parse the command line arguments
            CommandLine line = parser.parse(options, args);
        } catch (ParseException exp) {
            // oops, something went wrong
            System.err.println("Parsing failed.  Reason: " + exp.getMessage());
        }
    }
}
