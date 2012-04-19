package org.qview.gui.nbexplorer;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.windows.TopComponent;

/**
 * Action which shows NodeExplorer component.
 */
public class NodeExplorerAction extends AbstractAction {
    
    public NodeExplorerAction() {
        super(NbBundle.getMessage(NodeExplorerAction.class, "CTL_NodeExplorerAction"));
        putValue(SMALL_ICON, new ImageIcon(Utilities.loadImage(NodeExplorerTopComponent.ICON_PATH, true)));
    }
    
    public void actionPerformed(ActionEvent evt) {
        TopComponent win = NodeExplorerTopComponent.findInstance();
        win.open();
        win.requestActive();
    }
    
}
