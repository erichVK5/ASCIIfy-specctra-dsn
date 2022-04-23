import java.io.File;
import java.util.Scanner;
import java.io.PrintWriter;
import java.lang.StringBuilder;
import java.util.ArrayList;

// Class for ASCIIfying malformed dsn-files that contains non-ASCII.

public class ASCIIfy
{
   private static ArrayList<String> nonASCIItext = new ArrayList<String>();
   private static ArrayList<String> nonASCIIhash = new ArrayList<String>();  

// to check if a string consists of US-ASCII only
    private static boolean stringNotASCII(String input) {
        for (int i = 0; i < input.length(); i++) {
            int c = input.charAt(i);
            if (c > 0x7F) {
                return true;
            }
        }
        return false;
    }

    private static String ASCIIfyString(String input) {
        StringBuilder newString = new StringBuilder("");
        StringBuilder nonASCII = new StringBuilder("");
        boolean ascii = true;
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            int val = c;
            if (val > 0x7F) {
                if (ascii == true) {
                    ascii = false;
                    nonASCII = new StringBuilder(""); 
                }
                nonASCII.append(c);
            } else {
                if (ascii == false) {
                    ascii = true;
		    String hash = "hash" + nonASCII.toString().hashCode();
                    newString.append(hash);
                    nonASCIItext.add(nonASCII.toString());
                    nonASCIIhash.add(hash);
                    nonASCII = new StringBuilder("");
                }
                newString.append(c);
            }
        }
        if (ascii == false) {
            String hash = "hash" + nonASCII.toString().hashCode();
            newString.append(hash);
            nonASCIItext.add(nonASCII.toString());
            nonASCIIhash.add(hash);
        }
        return newString.toString();
    }

    private static void printUsage()
    {
        System.out.println("A utility to sanitise specctra DSN files with non ASCII content for autorouting");
        System.out.println("and simplify subsequent restoration of the non ASCII text in the autorouted .ses file.\n"); 
	System.out.println("Usage:\n   java ASCIIfy filename.dsn\n");
        System.out.println("Outputs:\n   filename-ASCII.dsn\n   filename-ASCII.ses-deASCIIfy.sh\n");
        System.out.println("Usage after autorouting:\n   /bin/sh filename-ASCII.ses-deASCIIfy.sh\n");
    }

    public static void main(String[] args)
    {
        if (args.length < 1) {
            printUsage();
            System.exit(0);
        }
        if (args[0].startsWith("-h") || args[0].startsWith("--h")) {
            printUsage();
            System.exit(0);
        } 
        String fileName = args[0];
	String fileStem = fileName.substring(0,fileName.length()-4);
        try {
            File dsnFile = new File(fileName);
            Scanner scanner = new Scanner(dsnFile);
	    File outputFile = new File(fileStem + "-ASCII.dsn");
	    PrintWriter pw1 = new PrintWriter(outputFile);
            scanner.useDelimiter(System.getProperty("line.separator"));
            while (scanner.hasNext()) {
                String currentLine = scanner.next();
                if (stringNotASCII(currentLine)) {
                    // System.out.println(currentLine); // to display visual diff
                    pw1.println(ASCIIfyString(currentLine));
                } else {
                    pw1.println(currentLine);
                }
            }
            scanner.close();
            pw1.close();


            // we now generate a script to de-ASCIIfy the final .ses file
            String sesFile = fileStem + "-ASCII.ses";
	    File script = new File(sesFile + "-deASCIIfy.sh");
	    PrintWriter pw2 = new PrintWriter(script);
            pw2.println("#!/bin/sh");
	    for (int i = 0; i < nonASCIItext.size(); i++) {
                pw2.println("sed -i 's/" + nonASCIIhash.get(i) + "/" + nonASCIItext.get(i) + "/g' " + sesFile);
            }
	    pw2.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
