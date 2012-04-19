/*
 * WMQCluster.java
 *
 * Created on March 30, 2006, 5:32 PM
 *
 */

package org.qview.data.mqmodel;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import org.qview.data.ObjectRepository;

/**
 *
 */
public class WMQCluster extends WMQObject implements Serializable {
//    private ArrayList fullRepositories = new ArrayList();
    private ArrayList reposNames = new ArrayList();
    private ArrayList qmgrs= new ArrayList();
    private ArrayList qmgrNames = new ArrayList();
                
    /**
     * Creates a new instance of WMQCluster
     */
    public WMQCluster(String c, String networkName) {
        caption = c;
        uniqueName = c;
        this.setClusterName(c);
        typeStr = "Cluster";
        this.setAttribute("Cluster Name", c);
        //this.setAttribute("Repositories", "");
        this.network = networkName;
//        repository = ObjectRepository.findInstance(networkName);
//        repository.addToRepository(this);
    }
    
    public synchronized void addQMgr(WMQQMgr newQMgr){
        if (!qmgrs.contains(newQMgr.getUniqueName())){
            qmgrs.add(newQMgr.getUniqueName());
            qmgrNames.add(newQMgr.getCaption());
        }
        // create a sorted, comma separated string list to display qmgr names
        String qmgrList = null;
        Collections.sort(qmgrNames);
        Iterator i = qmgrNames.iterator();
        while (i.hasNext()){
            if (qmgrList != null){
                qmgrList = qmgrList + ",";
            } else {
                qmgrList = "";
            }
            qmgrList = qmgrList + i.next();
        }
        this.setAttribute("Members", qmgrList);

    } //addQMgr
    
    public ArrayList getQMgrs(){
        System.out.println("getQMgrs() " + qmgrs.toString());
        ArrayList qmgrList = (ArrayList) qmgrs.clone();
        if ((qmgrList != null) && !(qmgrList.isEmpty())) {
            Collections.sort(qmgrList);
        }        
        return qmgrList;        
    } //getQMgrs
    
    public synchronized void addFullRepository(WMQQMgr fullRepository){

        if (!reposNames.contains(fullRepository.getCaption())){
            reposNames.add(fullRepository.getCaption());
        }
        // create a sorted, comma separated string list to display qmgr names
        String reposList = null;
        Collections.sort(reposNames);
        Iterator i = reposNames.iterator();
        while (i.hasNext()){
            if (reposList != null){
                reposList = reposList + ",";
            } else {
                reposList = "";
            }
            reposList = reposList + i.next();
        }
        this.setAttribute("Repositories", reposList);
    } //getFullRepositories
    public ArrayList getFullRepositories(){        
        return reposNames;
    } //getFullRepositories    
    
    public void orphan(){
        this.setUpdated("false");
//        fullRepositories = new ArrayList();
//        qmgrs= new ArrayList();
    }
    
}
