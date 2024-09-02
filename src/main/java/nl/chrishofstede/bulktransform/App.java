package nl.chrishofstede.bulktransform;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.StringWriter;

import javax.xml.transform.TransformerException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.w3c.dom.Document;
import org.xml.sax.SAXParseException;

import nl.chrishofstede.bulktransform.utils.Parameters;

/**
 * Hello world!
 *
 */
public class App {

    public static void main(String[] args) {
        // Set commandline options
        Options options = new Options();
        Option inOption = new Option("in", "input", true, "input file name (wildcards allowed)");
        inOption.setArgs(Option.UNLIMITED_VALUES);
        options.addOption(inOption);
        Option xslOption = new Option("xsl", "xslt", true, "XSLT stylesheet input file name");
        options.addOption(xslOption);
        Option outOption = new Option("out", "output", true, "output folder");
        options.addOption(outOption);

        String[] in = null;
        String xsl = null;
        String out = null;

        // create the parser
        CommandLineParser parser = new DefaultParser();
        try {
            // parse the command line arguments
            CommandLine line = parser.parse(options, args);
            if (line.hasOption(inOption)) {
                in = line.getOptionValues(inOption);
                boolean bFirst = true;
                for (String inFile : in) {
                    if (bFirst) {
                        System.out.println("in : " + inFile);
                        bFirst = false;
                    } else {
                        System.out.println("   : " + inFile);
                    }
                }
                if (line.hasOption(xslOption)) {
                    xsl = line.getOptionValue(xslOption);
                    System.out.println("xsl: " + xsl);
                }
                if (line.hasOption(outOption)) {
                    out = line.getOptionValue(outOption);
                    System.out.println("out: " + out);
                }
            }
            if (in == null || xsl == null || out == null) {
                showHelp(options);
            } else {
                transform(in, xsl, out);
            }
        } catch (ParseException exp) {
            // oops, something went wrong
            System.err.println("Commandline parsing failed.  Reason: " + exp.getMessage());
        }
        // Catch unhandled exceptions and report them in the log
        catch (final Exception e) {
            System.err.println("Exception: " + getExceptionMessage(e));
        }
    }

    static void showHelp(Options options) {
        HelpFormatter formatter = HelpFormatter.builder().get();
        formatter.printHelp("Command line syntax:", options);
    }

    public static String getExceptionMessage(Exception exception) {
        final StringWriter msg = new StringWriter();
        msg.write(exception.getClass().getName() + ": ");

        if (exception instanceof SAXParseException) {

            // Derived from SAXException
            msg.write(exception.getLocalizedMessage() + " System id: " + ((SAXParseException) exception).getSystemId()
                    + " Line: " + ((SAXParseException) exception).getLineNumber() + " Column: "
                    + ((SAXParseException) exception).getColumnNumber());
        } else if (exception instanceof TransformerException) {

            // Derived from Exception
            msg.write(((TransformerException) exception).getMessageAndLocation());
        } else {

            // Fall through exception
            msg.write(exception.getLocalizedMessage());
        }

        return msg.toString();
    }

    static void transform(String[] in, String xsl, String out) throws Exception {
        System.out.println("Checking: " + out);
        File outDirectory = new File(out);
        if (outDirectory.exists()) {
            if (outDirectory.isFile()) {
                System.out.println("out directory is a file");
                return;
            }
        } else {
            if (!outDirectory.mkdirs()) {
                System.out.println("Couldn't create out directory");
                return;
            }
        }
        System.out.println("Checking: " + xsl);
        File xslFile = new File(xsl);
        if (xslFile.exists()) {
            if (xslFile.isDirectory()) {
                System.out.println("xsl is a directory");
                return;
            }
            Stylesheet stylesheet = new Stylesheet(xslFile);
            Parameters parameters = new Parameters();
            System.out.println("Processing input files...");
            for (String inFile : in) {
                System.out.println("in: " + inFile);
                File inputXML = new File(inFile);
                Document document = DOMBuilder.parseDocumentAtPath(inputXML);
                File outFile = new File(outDirectory, inputXML.getName());
                System.out.println("Transforming to: " + outFile.getAbsolutePath());
                try (OutputStream outputXML = new FileOutputStream(outFile)) {
                    stylesheet.transformNodeToStream(document, parameters, outputXML, null);
			}
            }
        }
    }
}
