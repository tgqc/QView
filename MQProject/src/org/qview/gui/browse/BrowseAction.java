package org.qview.gui.browse;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.windows.TopComponent;

/**
 * Action which shows Browse component.
 */
public class BrowseAction extends AbstractAction {
    
    public BrowseAction() {
        super(NbBundle.getMessage(BrowseAction.class, "CTL_BrowseAction"));
        putValue(SMALL_ICON, new ImageIcon(Utilities.loadImage(BrowseTopComponent.ICON_PATH, true)));
    }
    
    public void actionPerformed(ActionEvent evt) {
        TopComponent win = BrowseTopComponent.findInstance();
        win.open();
        win.requestActive();
    }
    
}
