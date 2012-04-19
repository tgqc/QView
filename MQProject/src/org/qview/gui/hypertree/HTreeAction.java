package org.qview.gui.hypertree;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Iterator;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import org.qview.control.EntryPoint;
import org.qview.gui.nbexplorer.RootNode;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.windows.TopComponent;

/**
 * Action which shows HTree component.
 */
public class HTreeAction extends AbstractAction {
    
    public HTreeAction() {
        super(NbBundle.getMessage(HTreeAction.class, "CTL_HTreeAction"));
        putValue(SMALL_ICON, new ImageIcon(Utilities.loadImage(HTreeTopComponent.ICON_PATH, true)));
    }
    
    public void actionPerformed(ActionEvent evt) {
        
        if (EntryPoint.getEntryPoints().isEmpty()) {
//            EntryPoint.reloadEntryPoints();
//            if (EntryPoint.getEntryPoints().isEmpty()) {
                EntryPoint.findInstance(EntryPoint.GetDefaultEPName());
//            }
        }
        ArrayList entryPoints = EntryPoint.getEntryPoints();
        Iterator e = entryPoints.iterator();
        while (e.hasNext()){
            String instanceName = (String) e.next();
            TopComponent win = HTreeTopComponent.findInstance(instanceName);
            win.open();
            HTreeTopComponent.refreshInstance(instanceName);
//            win.requestActive();
        }
        
    }
    
}
