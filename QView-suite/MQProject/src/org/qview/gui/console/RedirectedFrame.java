/*
 * RedirectedFrame.java
 *
 * Created on 10 September 2008, 09:02
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.qview.gui.console;

import java.awt.*;
import javax.swing.*;
import java.io.*;


/**
 * http://tanksoftware.com/juk/developer/src/com/
 *     tanksoftware/util/RedirectedFrame.java
 * A Java Swing class that captures output to the command line 
 ** (eg, System.out.println)
 * RedirectedFrame
 * <p>
 * This class was downloaded from:
 * Java CodeGuru (http://codeguru.earthweb.com/java/articles/382.shtml) <br>
 * The origional author was Real Gagnon (real.gagnon@tactika.com);
 * William Denniss has edited the code, improving its customizability
 *
 * In breif, this class captures all output to the system and prints it in
 * a frame. You can choose weither or not you want to catch errors, log 
 * them to a file and more.
 * For more details, read the constructor method description
 */


public class RedirectedFrame extends JFrame {

    // Class information
    public static final String PROGRAM_NAME = "Redirect Frame";
    public static final String VERSION_NUMBER = "1.1";
    public static final String DATE_UPDATED = "13 April 2001";
    public static final String AUTHOR = 
       "Real Gagnon - edited by William Denniss";


    private boolean catchErrors;
    private boolean logFile;
    private String fileName;
    private int width;
    private int height;
    private int closeOperation;


    TextArea aTextArea = new TextArea();
    PrintStream aPrintStream  =
       new PrintStream(
         new FilteredStream(
           new ByteArrayOutputStream()));

    /** Creates a new RedirectFrame.
     *  From the moment it is created,
     *  all System.out messages and error messages (if requested)
     *  are diverted to this frame and appended to the log file 
     *  (if requested)
     *
     * for example:
     *  RedirectedFrame outputFrame =
     *       new RedirectedFrame
                (false, false, null, 700, 600, JFrame.DO_NOTHING_ON_CLOSE);
     * this will create a new RedirectedFrame that doesn't catch errors,
     * nor logs to the file, with the dimentions 700x600 and it doesn't 
     * close this frame can be toggled to visible, hidden by a controlling 
     * class by(using the example) outputFrame.setVisible(true|false)
     *  @param catchErrors set this to true if you want the errors to 
     *         also be caught
     *  @param logFile set this to true if you want the output logged
     *  @param fileName the name of the file it is to be logged to
     *  @param width the width of the frame
     *  @param height the height of the frame
     *  @param closeOperation the default close operation
     *        (this must be one of the WindowConstants)
     */
    public RedirectedFrame
       (boolean catchErrors, boolean logFile, String fileName, int width,
         int height, int closeOperation) {

        this.catchErrors = catchErrors;
        this.logFile = logFile;
        this.fileName = fileName;
        this.width = width;
        this.height = height;
        this.closeOperation = closeOperation;

        Container c = getContentPane();

        setTitle("Output Frame");
        setSize(width,height);
        c.setLayout(new BorderLayout());
        c.add("Center" , aTextArea);
        displayLog();

        this.logFile = logFile;

        System.setOut(aPrintStream); // catches System.out messages
        if (catchErrors)
            System.setErr(aPrintStream); // catches error messages

        // set the default closing operation to the one given
        setDefaultCloseOperation(closeOperation);

        Toolkit tk = Toolkit.getDefaultToolkit();
        Image im = tk.getImage("myicon.gif");
        setIconImage(im);
    }



    class FilteredStream extends FilterOutputStream {
        public FilteredStream(OutputStream aStream) {
            super(aStream);
          }

        public void write(byte b[]) throws IOException {
            String aString = new String(b);
            aTextArea.append(aString);
        }

        public void write(byte b[], int off, int len) throws IOException {
            String aString = new String(b , off , len);
            aTextArea.append(aString);
            if (logFile) {
                FileWriter aWriter = new FileWriter(fileName, true);
                aWriter.write(aString);
                aWriter.close();
            }
        }
    }

    private void displayLog() {
        Dimension dim = getToolkit().getScreenSize();
        Rectangle abounds = getBounds();
        Dimension dd = getSize();
        setLocation((dim.width - abounds.width) / 2,
                    (dim.height - abounds.height) / 2);
        setVisible(true);
        requestFocus();
    }

}

