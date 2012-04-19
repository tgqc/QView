// MessageDialog.java

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

package org.qview.gui;

import java.awt.*;
import javax.swing.*;
import javax.swing.JOptionPane;

/** A simple message dialog (with no prompt button).
 * The dialog is modeless, e.g. it "floats" while
 * the application continues.
 */
public class MessageDialog
{
  private JDialog _msgDialog;

  /**
   * Construct a dialog with given message, which is not
   * yet shown. show() will display
   * the dialog in the center of the given component.
   */
  public MessageDialog(Component parent, String message)
  {
    ImageIcon icon = null;
    java.net.URL imgURL = getClass().getResource("images/jman.gif");
    if (imgURL != null)
      icon = new ImageIcon(imgURL);
    Object[] options = {};
    JOptionPane pane = new JOptionPane(message,
                                       JOptionPane.DEFAULT_OPTION,
                                       JOptionPane.INFORMATION_MESSAGE,
                                       icon,
                                       options,
                                       null);
    _msgDialog = pane.createDialog(parent, "Message");
  }

  /**
   * Same as MessageDialog(parent, message ) with parent = null.
   */
  public MessageDialog(String message)
  {
    this(null, message);
  }


  /**
   * Show a modeless message dialog with given message.
   * Returns immediately.
   */
  public void show()
  {
    _msgDialog.setModal(false);
    _msgDialog.show();
  }

  /**
   * Hide the modeless message dialog previously shown.
   * May be reshown calling show().
   */
  public void close()
  {
    _msgDialog.dispose();
  }


}
