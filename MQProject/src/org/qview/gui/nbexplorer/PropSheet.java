/*
 * PropSheet.java
 *
 * Created on 3 June 2006, 20:37
 */

package org.qview.gui.nbexplorer;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.ResourceBundle;
import java.util.Set;
import org.qview.control.GuiDataAdapter;
import org.qview.data.ObjectRepository;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;

/**
 *
 * @author T.Goodwill
 */
public class PropSheet {
    private ObjectRepository repository;
    private static ResourceBundle bundle = NbBundle.getBundle(LeafNode.class);
    private Node node;
    private String key;
    private String parent;
    private String networkName;
    private String propSetName;
    private Sheet propSheet;
    private HashMap propsMap = new HashMap();
    private HashMap newMap = new HashMap();
//    private HashMap editable = new HashMap();
//    private ArrayList booleanAttrs = new ArrayList();
    
    /** Creates a new instance of PropSheet */
    public PropSheet(Sheet sheet, Node thisNode) {
        networkName = (String)thisNode.getValue("networkName");
        repository = ObjectRepository.findInstance(networkName);
//        editable = ModelAdapter.findInstance().getEditableProps();
        this.node = thisNode;
        this.key = thisNode.getName();
        if ( node.getParentNode() != null){
            this.parent = node.getParentNode().getName();
        } 
        
        propsMap = GuiDataAdapter.findInstance().getProperties(networkName, key, parent);
        Set propsList = propsMap.keySet();
        Iterator r = propsList.iterator();
        while (r.hasNext()) {
            propSetName = (String) r.next();
            Sheet.Set props = sheet.remove(propSetName);            
            props = Sheet.createPropertiesSet();            
            props.setName(propSetName);
            props.setDisplayName(propSetName);
            props.setExpert(true);                            

            /** returns read-only property to attribute sheet */
            class Attribute extends PropertySupport.ReadOnly {
                private String attribute;
                private Object value;
                public Attribute(Object attr, Object val) {
                    super((String)attr, val.getClass(),
                            (String)attr, bundle.getString("HINT_value"));
                    attribute = (String)attr;
                    value = val;
                }
                public Object getValue() {
                    return value;
                }
            }

            /** returns read-write property to attribute sheet */
            class EditableAttribute extends PropertySupport.ReadWrite {
                private String attribute;
                private Object value;
                private String propSet;
                public EditableAttribute(Object attr, Object val) {
                    super((String)attr, val.getClass(),
                            (String)attr, bundle.getString("HINT_value"));
                    attribute = (String)attr;
                    value = val;
                    propSet = propSetName;
                }
                public Object getValue() {
//                    return ModelAdapter.findInstance().getProperty(networkName, propSetName, key, attribute, parent);
                    return value;
                }
                public void setValue(Object nue) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
                    // if set property is successful, change value                    
                    if (GuiDataAdapter.findInstance().setProperty(networkName, propSet, key, attribute, nue)){
                        value = nue;
                    } 
                    PropertiesNotifier.changed();
//                    NodeExplorerTopComponent.findInstance().requestActive();
                }
            }      

            //extrapolate out keys from property lists
            ArrayList list = new ArrayList();
            Object value = new Object(); 
            
            newMap = (HashMap) propsMap.get(propSetName);

            Collection attrs = (Collection) newMap.keySet();
            list.addAll(attrs);
            Collections.sort(list);
            Iterator e = list.iterator();
            while (e.hasNext()) {
                String item = (String) e.next();                
                value = newMap.get(item);
                if (GuiDataAdapter.findInstance().isEditable(item, propSetName)){                   
                    props.put(new EditableAttribute(item, value));
                } else {                    
                    props.put(new Attribute(item, value));
                }    
            }
            sheet.put(props);  
        }
        propSheet = sheet;
    }
    public Sheet getPropSheet(){
        return this.propSheet;
    }
    
}
