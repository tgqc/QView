package org.qview.control;

import java.util.ArrayList;
import java.util.Iterator;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CallableSystemAction;

public final class StatusPollAction extends CallableSystemAction {
    
    public void performAction() {
        // TODO implement action body        
        ArrayList entryPointList = EntryPoint.getEntryPoints();
        Iterator e = entryPointList.iterator();
        while (e.hasNext()) {
            String networkName = (String) e.next();            
            StatusPoll nextPoll = new StatusPoll(networkName);
            nextPoll.start();
        }//while       
    }
    
    public String getName() {
        return NbBundle.getMessage(StatusPollAction.class, "CTL_StatusPollAction");
    }
    
    protected String iconResource() {
        return "org/qview/gui/gears.gif";
    }
    
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
    
    protected boolean asynchronous() {
        return false;
    }
    
}
