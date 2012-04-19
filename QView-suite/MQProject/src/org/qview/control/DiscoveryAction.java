package org.qview.control;

import java.util.ArrayList;
import java.util.Iterator;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CallableSystemAction;

public final class DiscoveryAction extends CallableSystemAction {
    
    public void performAction() {
        ArrayList entryPointList = EntryPoint.getEntryPoints();
        Iterator e = entryPointList.iterator();
        while (e.hasNext()) {
            String networkName = (String) e.next();
            Discovery disc = new Discovery(networkName);
            disc.start();        
        }//while        
    }    
    public void performAction(String conName) {        
        Discovery disc = new Discovery(conName);
        disc.start();
    }
    
    public String getName() {
        return NbBundle.getMessage(DiscoveryAction.class, "CTL_DiscoveryAction");
    }
    
    protected String iconResource() {
        return "org/qview/gui/discover.gif";
    }
    
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
    
    protected boolean asynchronous() {
        return false;
    }
    
}
