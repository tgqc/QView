// Console.java

/*
  This software is part of the JEX (Java Exemplarisch) Utility Library.
  It is Open Source Free Software, so you may
    - run the code for any purpose
    - study how the code works and adapt it to your needs
    - integrate all or parts of the code in your own programs
    - redistribute copies of the code
    - improve the code and release your improvements to the public
  However the use of the code is entirely your responsibility.
 */

package org.qview.gui.console;

//import ch.aplu.util.Size;
import java.io.*;
import java.awt.*;
import java.util.*;
import java.awt.event.*;
import javax.swing.*;
import java.awt.print.*;
import javax.swing.text.*;
import javax.swing.JOptionPane;
import org.qview.gui.*;

/**
 * Console window for line oriented input and output.<br><br>
 * Once the console window is instantiated all output to stdout and stderr is
 * redirected to this window. Only one console instance is allowed.<br>
 * To avoid creating an object, the factory method init() may be used.<br>
 * Only 7-bit-ASCII characters are supported.<br>
 *
 * Part of code from  Rjhm van den Bergh (rvdb@comweb.nl)
 * with thanks for the permission to use and distribute
 */
public class Console implements WindowListener, ActionListener, KeyListener, Printable, FocusListener
{
  private PrintStream _ps;
  private Console _console;    // Needed because of several methods
  private static int _instanceCount = 0;
  private JFrame _frame;
  private JTextArea _textArea;
  private JScrollPane _scrollPane;
  private Caret _caret;
  private int _caretPosition = 0;
  private Thread _reader1;
  private Thread _reader2;
  private boolean _quit;
  private boolean _gotKey = false;
  private char _keyChar;
  private int _keyCode;
  private int _modifiers;
  private String _modifiersText;
  private int _charCount = 0;
//  private PipedInputStream _pin1 = new PipedInputStream();
//  private PipedInputStream _pin2 = new PipedInputStream();
  private double _scale;
  private boolean _isVisible = true;
  private boolean _isRedirectToFile = false;
  private String _filename = "";  

  /**
   * Create a new <code>Console</code> with default attributes<br>
   * and returns a reference to it.
   * Default size: half the screen dimensions<br>
   * Default position: centered to screen<br>
   * Default font: Font( "Courier New", Font.PLAIN, 16 )
   */
  public Console init()
  {
    _console = new Console(null, null, null);
    return _console;
  }

  /**
   * Create a <code>Console</code> with specified size and position
   * and returns a reference to it.
   * @param position   a ref to a Position object
   * @see ch.aplu.util.Position
   * @param size   a ref to a Size object
   * @see ch.aplu.util.Size
   */
  public Console init(Position position, Size size)
  {
    _console = new Console(position, size, null);
    return _console;
  }

  /**
   * Create a <code>Console</code> with specified font
   * and returns a reference to it.
   * @param font   a ref to a Font object
   * @see java.awt.Font
   */
  public Console init(Font font)
  {
    _console = new Console(null, null, font);
    return _console;
  }

  /**
   * Create a new <code>Console</code> with specified size, position and font
   * and returns a reference to it.
   * @param position   a ref to a Position object
   * @see ch.aplu.util.Position
   * @param size   a ref to a Size object
   * @see ch.aplu.util.Size
   * @param font   a ref to a Font object
   * @see java.awt.Font
   */
  public Console init(Position position, Size size, Font font)
  {
    _console = new Console(position, size, font);
    return _console;
  }

  /**
   * Redirect all output to stdout and stderr to a text file
   * with the given filename.
   * If the file exists the text is appended, otherwise the
   * file is created.
   * (No console window is shown, so no input is possible.)
   * If null is given, all output to stdout and stderr is
   * ignored. This may be used to hide output from library functions.
   */
  public Console init(String filename)
  {
    _filename = filename;
    if (filename != null)
      _isRedirectToFile = true;
    _isVisible = false;
    _console = new Console();
    return _console;
  }

  /**
   * Close the console instance.
   */
  public void end()
  {
    if (_instanceCount == 0)
      return;
    _console._instanceCount = 0;
    _console._frame.setVisible(false);
    // _console._frame.dispose();
  }

  /**
   * Erase all text in console window
   */
  public void clear()
  {
    if (_instanceCount == 0)
      return;
    _console._textArea.setText("");
    setCaretPosition(0);
    _charCount = 0;
    _gotKey = false;
  }

  /**
   * Return a Position ref with specified upperleft x and y coordinates.
   * May be used in init() to avoid the keyword <code>new</code>
   */
  public Position position(int ulx, int uly)
  {
    Position p = new Position(ulx, uly);
    return p;
  }

  /**
   * Return a Size ref with specified width and height.
   * May be used in init() to avoid the keyword new
   */
  public Size size(int width, int height)
  {
    Size s = new Size(width, height);
    return s;
  }

  /**
   * Construct a Console with attributes.
   * @param position   a ref to a Position object
   * @see ch.aplu.util.Position
   * @param size   a ref to a Size object
   * @see ch.aplu.util.Size
   * @param font   a ref to a Font object
   * @see java.awt.Font
   */
  public Console(Position position, Size size, Font font)     // Ctor
  {
//    if (_instanceCount == 0)
//      _instanceCount = 1; // _instanceCount = 1;
//    else
//    {
//      JOptionPane.showMessageDialog(null,
//                                    "Only one instance of Console allowed.");
//      System.exit(1);
//    }
    _console = this;  
    _instanceCount = 1;
    // Create all components and add them
    _frame = new JFrame("Java Input/Output Console");
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    Dimension frameSize = null;
    int ulx = 0;
    int uly = 0;

    if (size == null)
    {
      frameSize = new Dimension((int)(screenSize.width / 2),
                                (int)(screenSize.height / 2));
      ulx = (int)(frameSize.width / 2);
      uly = (int)(frameSize.height / 2);
    }
    else
    {
      frameSize = new Dimension(size.getWidth(), size.getHeight());
      ulx = position.getUlx();
      uly = position.getUly();
    }

    _frame.setBounds(ulx, uly, frameSize.width, frameSize.height);

    _textArea = new JTextArea();
    _textArea.setEditable(false);
    _caret = _textArea.getCaret();
    _caret.setVisible(false);

    if (font == null)
      _textArea.setFont(new Font("Courier New", Font.PLAIN, 12));
    else
      _textArea.setFont(font);
    JButton button = new JButton("Clear");

    _frame.getContentPane().setLayout(new BorderLayout());
    _scrollPane = new JScrollPane(_textArea,
                      JScrollPane.
                      VERTICAL_SCROLLBAR_AS_NEEDED,
                      JScrollPane.
                      HORIZONTAL_SCROLLBAR_AS_NEEDED);
    _frame.getContentPane().add(_scrollPane,BorderLayout.CENTER);
    _frame.getContentPane().add(button, BorderLayout.SOUTH);

    _frame.addWindowListener(this);
    _textArea.addFocusListener(this);
    _textArea.addKeyListener(this);
    button.addActionListener(this);

    PrintStream ps;

    _quit = false;    // signals the Threads that they should exit

    _frame.setVisible(_isVisible);
  }

  /**
   * Construct a Console with default attributes ( see init() ).
   */
  public Console()
  {
    this(null, null, null);    
  }

  /**
   * For internal use only.
  */
  public synchronized void windowClosed(WindowEvent evt)
  {
//    Toolkit.getDefaultToolkit().beep();
    _quit = true;
    this.notifyAll();     // stop all threads

    System.exit(0);
  }

  /**
   * For internal use only.
   */
  public synchronized void windowClosing(WindowEvent evt)
  {
    _frame.setVisible(false);     // default behaviour of JFrame
    _frame.dispose();
  }

  /**
   * For internal use only.
   */
  public void windowActivated(WindowEvent e){}

  /**
   * For internal use only.
   */
  public void windowDeactivated(WindowEvent e){}

  /**
   * For internal use only.
   */
  public void windowDeiconified(WindowEvent e){}

  /**
   * For internal use only.
   */
  public void windowIconified(WindowEvent e){}

  /**
   * For internal use only.
   */
  public void windowOpened(WindowEvent e){}

  /**
   * For internal use only
   */
  public synchronized void actionPerformed(ActionEvent evt)
  {
    clear();
    _textArea.requestFocus();
}

  public synchronized void writeText(String input){      
     input = input + "\n";
     _textArea.insert(input, _charCount);
     _charCount += input.length();
     setCaretPosition(_charCount);
  }

  private void setCaretPosition(int pos)
  {
    _caret.setVisible(false);
    _textArea.setCaretPosition(pos);
    _caretPosition = pos;
    _caret.setVisible(true);
  }


  /**
   * Return true if a key was hit since the last time the one character buffer
   * was read with getKeyChar() oder getKeyCharWait().
   * (Put the current thread to sleep for 1 ms).
*/
  public boolean kbhit()
  {
    checkInstance("kbhit()");
    delay(1);
    return _gotKey;
  }

  /**
   * Return the unicode character associated with last key pressed.
   * Return KeyEvent.CHAR_UNDEFINED if the one character buffer is empty.
   * (No echo in the console window.)
   */
  public char getKey()
  {
    checkInstance("getKey()");

    if (_gotKey)
    {
      _gotKey = false;
      return _keyChar;
    }
    else
      return KeyEvent.CHAR_UNDEFINED;
  }

  /**
   * Return the keycode associated with last key pressed.
   * Return KeyEvent.CHAR_UNDEFINED if the one character buffer is empty.
   * (No echo in the console window.)
   */
  public  int getKeyCode()
  {
    checkInstance("getKeyCode()");

    if (_gotKey)
    {
      _gotKey = false;
      return _keyCode;
    }
    else
      return KeyEvent.CHAR_UNDEFINED;
  }

  /**
   * Wait until a key is typed and
   * return the unicode character associated with last key pressed.
   * (No echo in the console window.)
   */
  public char getKeyWait()
  {
    checkInstance("getKeyWait()");

    while (!_gotKey)
      delay(1);
    return getKey();
  }

  /**
   * Wait until a key is typed and
   * return the keycode associated with last key pressed.
   * (No echo in the console window.)
   */
  public int getKeyCodeWait()
  {
    checkInstance("getKeyCodeWait()");

    while (!_gotKey)
      delay(1);
    return getKeyCode();
  }

  /**
   * Wait until a key is typed and
   * return the unicode character associated it.
   * (Echo the character in the console window.)
   */
  public char readChar()
  {
    checkInstance("readChar()");

    while (!_gotKey)
      delay(1);
    char ch = getKey();
    _console._textArea.insert(Character.toString(ch), _charCount);
    _charCount++;
    return ch;
  }

  /**
   * Wait until a sequence of characters with trailing newline is typed.
   * Return the string (without the trailing newline)
   * (Echo the characters in the console wiindow.)
   */
  public String readLine()
  {    
    checkInstance("readLine()");

//    JTextArea ta = _console._textArea;
    JTextArea ta = this._textArea;
    char ch = ' ';
    String s = "";

    while (ch != '\n')
    {
      ch = getKeyWait();

      if (_keyCode == '\b')    // Delete Key
      {
        if (s.length() > 0)
        {
          String tmp = ta.getText();
          ta.setText(tmp.substring(0, tmp.length()-1));
          _charCount--;
          setCaretPosition(_charCount);
          s = s.substring(0, s.length() - 1);
        }
      }
      else
      {
        if (Character.isDefined(ch) &&
            getLastModifiers() < 2 &&
            !(getLastModifiers() == 1 && getLastKeyCode() == 16)) // Shift key alone

        {
          ta.insert(Character.toString(ch), _charCount);
          _charCount++;
          setCaretPosition(_charCount);
          if (ch != '\n')
            s = s + Character.toString(ch);
        }
      }
    }
    System.out.print("readLine() " + s);
    return s;    
  }  
 
  /**
   *  Return the key code associated with last key pressed.
   */
  public int getLastKeyCode()
  {
    return _keyCode;
  }

  /**
   *  Return the key character associated with last key pressed.
   */
  public char getLastKeyChar()
  {
    return _keyChar;
  }

  /**
   *  Return the modifiers associated with last key pressed.
   */
  public int getLastModifiers()
  {
    return _modifiers;
  }

  /**
   *  Return the modifiers text associated with last key pressed.
   */
  public String getLastModifiersText()
  {
    return _modifiersText;
  }

  /**
   * Wait until a sequence of numbers with trailing newline is typed.
   * Return the converted integer (if possible)
   * (Echo the character in the console window.)
   */
  public int readInt()
  {
    checkInstance("readInt()");
    return Integer.parseInt(readLine());
  }

  /**
   * Same as readInt() but returns an Integer object.
   * Return null, if entered character sequence cannot converted to an integer.
   */
  public Integer getInt()
  {
    checkInstance("getInt()");
    Integer value;
    try
    {
      value = new Integer(readLine());
    }
    catch (NumberFormatException e)
    {
      return null;
    }
    return value;
  }

  /**
   * Wait until a sequence of numbers with trailing newline is typed.
   * Return the converted double (if possible)
   * (Echo the character in the console window.)
   */
  public double readDouble()
  {
    checkInstance("getDouble()");
    return Double.parseDouble(readLine());
  }

  /**
   * Same as readDouble() but returns a Double object.
   * Return null, if entered character sequence cannot converted to a double.
   */
  public Double getDouble()
  {
    checkInstance("getDouble()");
    Double value;
    try
    {
      value = new Double(readLine());
    }
    catch (NumberFormatException e)
    {
      return null;
    }
    return value;
  }

  /**
   * Terminate application.
   */
  public void terminate()
  {
    checkInstance("terminate()");
    System.exit(0);
  }

  /**
   * For internal use only.
   */
  public void keyPressed(KeyEvent evt)
  {
    _gotKey = true;
    _keyCode = evt.getKeyCode();
    _keyChar = evt.getKeyChar();
    _modifiers = evt.getModifiers();
    _modifiersText = KeyEvent.getKeyModifiersText(_modifiers);
  }

  /**
   * For internal use only.
   */
  public void keyReleased(KeyEvent evt) {}

  /**
   * For internal use only.
   */
  public void keyTyped(KeyEvent evt){}
  // This event is not called when a normal (ASCII) key
  // is typed on a Mac, only keyPressed is called, so
  // we work with keyPressed-event only


  private void checkInstance(String methodName)
  {
    if (_instanceCount == 0)
    {
      JOptionPane.showMessageDialog(null,
                                    "Error when calling " + methodName
                                    + "\nNo instance of Console found");
      System.exit(1);
    }
  }

  /**
   * For internal use only.
   */
   public void focusGained(FocusEvent evt)
  {
    setCaretPosition(_caretPosition);
  }

  /**
   * For internal use only.
   */
  public void focusLost(FocusEvent evt)
  {
    _caretPosition = _textArea.getCaretPosition();
  }

  /**
   * Show all available fonts.
   */
  public void showFonts()
  {
    System.out.println("All fonts available to Graphic2D:\n");
    GraphicsEnvironment ge =
      GraphicsEnvironment.getLocalGraphicsEnvironment();
    String[] fontNames = ge.getAvailableFontFamilyNames();
    for (int n = 0; n < fontNames.length; n++)
      System.out.println(fontNames[n]);
  }

  /**
 * Delay execution for the given amount of time ( in ms ).
 */
  public void delay(int time)
  {
    try
    {
      Thread.currentThread().sleep(time);
    }
    catch (Exception e) {}
  }

  /**
   * Right justify the given number in a field with the given field width
   * (pad the field with leading spaces).
   * Return the padded string (if possible)
   */
  public String pad(String num, int fieldWidth)
  {
    if (fieldWidth <= 0) // Error
      return num;

    int leadingSpaces;
    leadingSpaces = fieldWidth - num.length();

    if (leadingSpaces < 0) // Error
      return num;

    StringBuffer buf = new StringBuffer();
    for (int i = 0; i < leadingSpaces; i++)
      buf.append(' ');
    buf.append(num);
    return new String(buf);
  }

  /**
   * Pad given number with trailing spaces to optain decimal width and
   * right justify in a field with the given width
   * (pad the the field with leading spaces).
   * Return the padded string (if possible)
   */
  public String pad(String num, int fieldWidth, int decimalWidth)
  {
    if (fieldWidth <= 0) // Error
      return num;

    int leadingDecimals = num.indexOf('.');
    int trailingDecimals;
    int trailingSpaces;
    if (leadingDecimals == -1)  // No decimal point
    {
      trailingDecimals = 0;
      trailingSpaces = decimalWidth + 1;
    }
    else
    {
      trailingDecimals = num.length() - leadingDecimals - 1;
      trailingSpaces = decimalWidth - trailingDecimals;
      if (trailingSpaces < 0)  // Error
        return num;
    }

    StringBuffer buf = new StringBuffer(num);
    for (int i = 0; i < trailingSpaces; i++)
      buf.append(' ');

    return pad(buf.toString(), fieldWidth);
  }

  /**
   * Return a reference to the JTextArea of the console window
   */
  public JTextArea getTextArea()
  {
    checkInstance("getTextArea()");
    return _textArea;
  }

  /**
   * Print a boolean value.
   * Return a reference to Console to concatenate print methods.
   */
  public Console print(boolean b)
  {
    if (_instanceCount == 0)
      init();
    _ps.print(b);
    return _console;
  }

  /**
   * Print a character.
   * Return a reference to Console to concatenate print methods.
   */
  public Console print(char c)
  {
    if (_instanceCount == 0)
      init();
    _ps.print(c);
    return _console;
  }

  /**
   * Print an array of characters.
   * Return a reference to Console to concatenate print methods.
   */
  public Console print(char[] s)
  {
    if (_instanceCount == 0)
      init();
    _ps.print(s);
    return _console;
  }

  /**
   * Print a double-precision floating-point number.
   * Return a reference to Console to concatenate print methods.
   */
  public Console print(double d)
  {
    if (_instanceCount == 0)
      init();
    _ps.print(d);
    return _console;
  }

  /**
   * Print a floating-point number.
   * Return a reference to Console to concatenate print methods.
   */
  public Console print(float f)
  {
    if (_instanceCount == 0)
      init();
    _ps.print(f);
    return _console;
  }

  /**
   * Print an integer.
   * Return a reference to Console to concatenate print methods.
   */
  public Console print(int i)
  {
    if (_instanceCount == 0)
      init();
    _ps.print(i);
    return _console;
  }

  /**
   * Print a long integer.
   * Return a reference to Console to concatenate print methods.
   */
  public Console print(long l)
  {
    if (_instanceCount == 0)
      init();
    _ps.print(l);
    return _console;
  }

  /**
   * Print an object.
   * Return a reference to Console to concatenate print methods.
   */
  public Console print(Object obj)
  {
    if (_instanceCount == 0)
      init();
    _ps.print(obj);
    return _console;
  }

  /**
   * Print a string.
   * Return a reference to Console to concatenate print methods.
   */
  public Console print(String s)
  {
    if (_instanceCount == 0)
      init();
    _ps.print(s);
    return _console;
  }

  /**
   *  Terminate the current line by writing the line separator string.
   * Return a reference to Console to concatenate print methods.
   */
  public Console println()
  {
    if (_instanceCount == 0)
      init();
    _ps.println();
    return _console;
  }

  /**
   * Print a boolean and then terminate the line.
   * Return a reference to Console to concatenate print methods.
   */
  public Console println(boolean b)
  {
    if (_instanceCount == 0)
      init();
    _ps.println(b);
    return _console;
  }

  /**
   * Print a character and then terminate the line.
   * Return a reference to Console to concatenate print methods.
   */
  public Console println(char c)
  {
    if (_instanceCount == 0)
      init();
    _ps.println(c);
    return _console;
  }

  /**
   * Print an array of characters and then terminate the line.
   * Return a reference to Console to concatenate print methods.
   */
  public Console println(char[] s)
  {
    if (_instanceCount == 0)
      init();
    _ps.println(s);
    return _console;
  }

  /**
   * Print a double and then terminate the line.
   * Return a reference to Console to concatenate print methods.
   */
  public Console println(double d)
  {
    if (_instanceCount == 0)
      init();
    _ps.println(d);
    return _console;
  }

  /**
   * Print a float and then terminate the line.
   * Return a reference to Console to concatenate print methods.
   */
  public Console println(float f)
  {
    if (_instanceCount == 0)
      init();
    _ps.println(f);
    return _console;
  }

  /**
   * Print an integer and then terminate the line.
   * Return a reference to Console to concatenate print methods.
   */
  public Console println(int i)
  {
    if (_instanceCount == 0)
      init();
    _ps.println(i);
    return _console;
  }

  /**
   * Print a long and then terminate the line.
   * Return a reference to Console to concatenate print methods.
   */
  public Console println(long l)
  {
    if (_instanceCount == 0)
      init();
    _ps.println(l);
    return _console;
  }

  /**
   * Print an Object and then terminate the line.
   * Return a reference to Console to concatenate print methods.
   */

  public Console println(Object obj)
  {
    if (_instanceCount == 0)
      init();
    _ps.println(obj);
    return _console;
  }

  /**
   * Print a String and then terminate the line.
   * Return a reference to Console to concatenate print methods.
   */
  public Console println(String s)
  {
    if (_instanceCount == 0)
      init();
    _ps.println(s);
    return _console;
  }

  /**
   * J2SE V5 only!
   * Print a formatted string using the specified format string and varargs.
   * (See PrintStream.printf() for more information)
   * Return a reference to Console to concatenate print methods.
   */
   public Console printf(String format, Object[] args)
   {return null;}  // dummy for documentation

  /**
   * J2SE V5 only!
   * Print a formatted string using the specified format string and varargs
   * and applying given locale during formatting.
   * (See PrintStream.printf() for more information)
   * Return a reference to Console to concatenate print methods.
   */
  public Console printf(Locale l, String format, Object[] args)
   {return null;} // dummy for documentation


  /**
   * For internal use only. Implements Printable.print().
   */
  public int print(Graphics g, PageFormat pf, int pageIndex)
  {
    if (pageIndex != 0)
      return NO_SUCH_PAGE;
    Graphics2D g2D = (Graphics2D)g;
    double printerWidth = pf.getImageableWidth();
    double printerHeight = pf.getImageableHeight();
    double printerSize =
      printerWidth > printerHeight ? printerWidth : printerHeight;
    // The 600 depends on the JPanel default size
    double scalex = 600 / printerSize * _scale;
    double scaley = scalex;

    double xZero = pf.getImageableX();
    double yZero = pf.getImageableY();

    g2D.translate(xZero, yZero);
    g2D.scale(scalex, scaley);

    _textArea.print(g);
    return PAGE_EXISTS;
  }

  /**
   * Print the current text area to an attached printer
   * with the given magnification scale factor.
   * A standard printer dialog is shown before printing is
   * started.<br>
   *
   * Return false, if printing is canceled in this dialog,
   * return true, when print data is sent to the printer spooler.<br>
   *
   * Only the text thats fits on one page is printed.
   *
   */
  public boolean printScreen(double scale)
  {
    _scale = scale;
    MessageDialog msg = new MessageDialog(_frame, "Printing in progress. Please wait...");

    PrinterJob pj = PrinterJob.getPrinterJob();
    pj.setPrintable(this);
    if (pj.printDialog())
    {
      try
      {
        msg.show();
        pj.print();
        msg.close();
      }
      catch(PrinterException ex)
      {
        System.out.println("Exception in Console.printScreen()\n" + ex);
      }
      return true;
    }
    else
      return false;
  }

  /**
   * Same as printScreen(scale) with scale = 1
   */
  public boolean printScreen()
  {
    return printScreen(1);
  }

  /**
   * Return a reference to the JFrame instance used by
   * the console
   */
  public JFrame getFrame()
  {
    if (_instanceCount == 0)
      init();
    return _frame;
  }

  /**
   * Insert/remove a vertical scroll bar.
   * Default scroll bar policy: scroll bar added as needed
   */
  public void showVerticalScrollBar(boolean b)
  {
    if (_instanceCount == 0)
      init();
    if (b)
      _scrollPane.setVerticalScrollBarPolicy(JScrollPane.
                        VERTICAL_SCROLLBAR_ALWAYS);
    else
      _scrollPane.setVerticalScrollBarPolicy(JScrollPane.
                        VERTICAL_SCROLLBAR_NEVER);
    _textArea.revalidate();
  }

  /**
   * Insert/remove a horizonal scroll bar.
   * Default scroll bar policy: scroll bar added as needed
   */
  public void showHorizontalScrollBar(boolean b)
  {
    if (_instanceCount == 0)
      init();
    if (b)
      _scrollPane.setHorizontalScrollBarPolicy(JScrollPane.
                        HORIZONTAL_SCROLLBAR_ALWAYS);
    else
      _scrollPane.setHorizontalScrollBarPolicy(JScrollPane.
                        HORIZONTAL_SCROLLBAR_NEVER);
    _textArea.revalidate();
  }


  /**
   * Set another title in the console's title bar.
   */
   public void setTitle(String title)
   {
     if (_instanceCount == 0)
       init();
     _frame.setTitle(title);
   }

  /**
   * Return version information
   */
  public String getVersion()
  {
    return "0.1";
  }

  /**
   * Return copywrite information
   */
  public String getAbout()
  {
    return "mqview";
  }
}



