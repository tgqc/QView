package org.qview.gui;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.windows.TopComponent;

/**
 * Action which shows Output component.
 */
public class OutputAction extends AbstractAction {
    
    public OutputAction() {
        super(NbBundle.getMessage(OutputAction.class, "CTL_OutputAction"));
        putValue(SMALL_ICON, new ImageIcon(Utilities.loadImage(OutputTopComponent.ICON_PATH, true)));
    }
    
    public void actionPerformed(ActionEvent evt) {
        TopComponent win = OutputTopComponent.findInstance();
        win.open();
        win.requestActive();
    }
    
}
