package nl.chrishofstede.bulktransform;

import java.io.File;
import java.io.FileFilter;
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
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.xerces.util.XMLCatalogResolver;
import org.w3c.dom.Document;
import org.xml.sax.SAXParseException;

import nl.chrishofstede.bulktransform.utils.Parameters;

/**
 * Bulk transform app
 *
 */
public class App {

    public static void main(String[] args) {
        try {
            // Set commandline options
            Options options = new Options();
            Option inOption = new Option("in", "input", true, "input file name (wildcards allowed): -in in\\*.xml");
            inOption.setArgs(Option.UNLIMITED_VALUES);
            options.addOption(inOption);
            Option xslOption = new Option("xsl", "xslt", true, "XSLT stylesheet input file name: -xsl html.xsl");
            options.addOption(xslOption);
            Option outOption = new Option("out", "output", true, "output folder: -out out");
            options.addOption(outOption);
            Option catOption = new Option("cat", "catalog", true, "XML catalog file name: -cat catalog.xml");
            options.addOption(catOption);

            // Option values
            String[] in = null;
            String xsl = null;
            String out = null;
            String cat = null;

            // Create the parser
            CommandLineParser parser = new DefaultParser();
            try {
                // Parse the command line arguments
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
                    if (line.hasOption(catOption)) {
                        cat = line.getOptionValue(catOption);
                        System.out.println("cat: " + cat);
                    }
                }
                if (in == null || xsl == null || out == null) {
                    showHelp(options);
                } else {
                    transform(in, xsl, out, cat);
                }
            } catch (ParseException exp) {
                // oops, something went wrong
                System.err.println("Commandline parsing failed.  Reason: " + exp.getMessage());
            }
        }
        // Catch unhandled exceptions and report them in the log
        catch (final Exception e) {
            System.err.println("Error: " + getExceptionMessage(e));
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

    /**
     * Gets the XML catalog resolver to be used with the XML parser.
     * 
     * @param catalog Path to the XML catalog file.
     * @throws Exception
     *                   Signals that a non user recoverable error has occurred.
     */
    static XMLCatalogResolver getCatalogResolver(final String catalog) throws Exception {
        String[] catalogs = { catalog };

        // Create catalog resolver and set a catalog list.
        return new XMLCatalogResolver(catalogs, false);
    }

    static void transform(String[] in, String xsl, String out, String catalog) throws Exception {

        // Check output directory and create one if it doesn't exist
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

        // Check the catalog
        XMLCatalogResolver catalogResolver = null;
        if (catalog != null) {
            System.out.println("Checking: " + catalog);
            File catalogFile = new File(catalog);
            if (catalogFile.exists()) {
                if (catalogFile.isDirectory()) {
                    System.out.println("catalog is a directory");
                    return;
                }
                catalogResolver = getCatalogResolver(catalogFile.getAbsolutePath());
                System.out.println("Using catalog: " + catalogFile.getAbsolutePath());
            }
        }

        // Check the stylesheet
        System.out.println("Checking: " + xsl);
        File xslFile = new File(xsl);
        if (xslFile.exists()) {
            if (xslFile.isDirectory()) {
                System.out.println("xsl is a directory");
                return;
            }

            // Create translet from the stylesheet
            Stylesheet stylesheet = new Stylesheet(xslFile);
            Parameters parameters = new Parameters(); // Future expansion

            // Find the input files
            WildcardFileFilter.Builder wildcardBuilder = WildcardFileFilter.builder();
            wildcardBuilder.setIoCase(IOCase.SYSTEM);
            System.out.println("Processing input files...");
            for (String inPathString : in) {
                System.out.println("In: " + inPathString);
                File inPath = new File(FileUtils.current(), inPathString);
                File inDirectory = inPath.getParentFile();
                if (inDirectory != null) {

                    // Process the wildcard matches if used
                    FileFilter fileFilter = wildcardBuilder.setWildcards(inPath.getName()).get();
                    File[] inFiles = inDirectory.listFiles(fileFilter);
                    for (File inFile : inFiles) {
                        if (inFile.isFile()) {

                            // Parse the input file
                            System.out.println("Parsing: " + inFile.getAbsolutePath());
                            Document document = DOMBuilder.parseDocumentAtPath(inFile, catalogResolver);

                            // Set the transformed output file
                            File outFile = new File(outDirectory, inFile.getName());
                            System.out.println("Transforming to: " + outFile.getAbsolutePath());
                            try (OutputStream outputXML = new FileOutputStream(outFile)) {

                                // Transform the input document
                                stylesheet.transformNodeToStream(document, parameters, outputXML, null);
                            }
                        }
                    }
                }
            }
        }
    }
}
