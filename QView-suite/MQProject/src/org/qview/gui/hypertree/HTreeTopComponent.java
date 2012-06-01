package org.qview.gui.hypertree;

import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGImageEncoder;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.print.Book;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import javax.swing.ActionMap;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import org.openide.util.Exceptions;
import org.qview.control.EntryPoint;
import org.qview.control.HyperTree;
import org.openide.ErrorManager;
import org.openide.explorer.ExplorerManager;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.lookup.ProxyLookup;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * Top component which displays something.
 */
public class HTreeTopComponent extends TopComponent {    
    private static HTreeTopComponent instance;
    private static HashMap namedInstance = new HashMap();
    private HTNodeBase root = null; // the root of the tree
    private final ExplorerManager manager = new ExplorerManager();
    private ActionMap map = null;

   
    /** path to the icon used by the component and its open action */
    static final String ICON_PATH = "org/qview/gui/connection.gif";
    private static final String MODE = "editor"; //open mode
    private static final String PREFERRED_ID = "HTreeTopComponent";

    private String instName;
    private HTView view;

    private Lookup[] lookupArray = null;
    private ProxyLookup proxylookup = null;
    
    private HTreeTopComponent(String newName) {
        initComponents();
        this.instName = newName;
//        this.jLabel1.setText(newName);
        namedInstance.put(newName, this);
        setName(newName);      
        setToolTipText(NbBundle.getMessage(HTreeTopComponent.class, "HINT_HTreeTopComponent") + " : " + newName);
        setIcon(Utilities.loadImage(ICON_PATH, true));
        setBackground(java.awt.Color.white);
        setOpaque(true);
    }

    public JPanel getHypertreePanel() {
        return hypertreePanel;
    } //getHyperPanel
    public JPanel getView() {
        return view;
    } //getHyperPanel
    public void setPanel(HTView vw, HTNodeBase rootNode){         
//        if (this.instName != RootNode.getTopName()){
            root = rootNode;
            if (namedInstance.containsKey("MQ Environment")){
                ((HTreeTopComponent)namedInstance.remove("MQ Environment")).close();
            }
            view = vw;
            if (vw != null){            
                this.getHypertreePanel().removeAll();
                this.getHypertreePanel().add(view, BorderLayout.CENTER);
                view.setVisible(true);
                this.getHypertreePanel().setVisible(true);            
            }      
    }

    // Method returns value of user-defined queue filter
    public String getQueueFilter(){
         return this.jTextQueueFilter.getText();
    }

    // Method returns value of user-defined channel filter
    public String getChannelFilter(){
         return this.jTextChannelFilter.getText();
    }

    // Method checks whether local queues are to be shown in the tree
    public boolean getLocalQueuesCheck(){
         return this.jCheckQueueLocal.isSelected();
    }
    // Method checks whether remote queues are to be shown in the tree
    public boolean getRemoteQueuesCheck(){
         return this.jCheckQueueRemote.isSelected();
    }
    // Method checks whether alias queues are to be shown in the tree
    public boolean getAliasQueuesCheck(){
         return this.jCheckQueueAlias.isSelected();
    }

    // Method checks whether non-local queues are to be shown in the tree
    public int getMinQueueDepth() {
         Integer minQueueDepth;
         try {
             minQueueDepth = Integer.valueOf(this.jTextQueueDepth.getText());
         } catch (NumberFormatException ex) {
             minQueueDepth = 10000000;  // No qs is a 'prettier' default than all qs
         }
         int depth = minQueueDepth.intValue();
         return depth;
    }

    // Method checks whether inactive channels are to be shown in the tree
    public boolean getInactiveChannelsCheck(){
        return this.jCheckInactive.isSelected();
    }
    // Method checks whether running channels are to be shown in the tree
    public boolean getRunningChannelsCheck(){
        return this.jCheckRunning.isSelected();
    }
    // Method checks whether running channels are to be shown in the tree
    public boolean getConnChannelsCheck(){
        return this.jCheckConn.isSelected();
    }

    // Method sets default view of non-zero qs and active chls to be shown in the tree
    public void setDefaultView(){
         this.jCheckQueueLocal.setSelected(true);
         this.jCheckQueueRemote.setSelected(false);
         this.jCheckQueueAlias.setSelected(false);
         this.jCheckInactive.setSelected(false);
         this.jCheckRunning.setSelected(false);
         this.jCheckConn.setSelected(false);
         this.jTextQueueFilter.setText("^(?!SYSTEM).*");
         this.jTextQueueDepth.setText("1");
         this.jTextChannelFilter.setText("^(?!SYSTEM).*");
         HyperTree.refreshNode(this.getName());
         this.revalidate();
    }

    
    public String getSelectedNodeName(){
        String selectedNodeName = null;
//        System.out.println("-------------------------" + HyperView.findInstance(instName).getHyperTree().toString());
        if ((HyperTree.findInstance(instName).getHyperTree() != null) && (HyperTree.findInstance(instName).getHyperTree().getSelectedNode() != null)) { 
            selectedNodeName = ((HTNodeBase)HyperTree.findInstance(instName).getHyperTree().getSelectedNode()).getUniqueName();
            System.out.println("---------------------------------------------------" + selectedNodeName);
        }
        return selectedNodeName;
    }
    
    public void printPanel() {
        // setting size would require a new HTView.
//        public static final Dimension A4_LANDSCAPE = new Dimension(842, 595);
//        public static final Dimension A3_LANDSCAPE = new Dimension(595 * 2, 842);
//        public static Dimension DEFAULT_SIZE = A4_LANDSCAPE;
//        view.setMaximumSize(DEFAULT_SIZE);
//        view.setMinimumSize(DEFAULT_SIZE);
//        view.setPreferredSize(DEFAULT_SIZE);
//        view.setSize(DEFAULT_SIZE);
        if (view != null) {
            PrinterJob printJob = PrinterJob.getPrinterJob();
            printJob.setJobName(instName);
            PageFormat pf = printJob.defaultPage();
            pf.setOrientation(PageFormat.LANDSCAPE);

            Book book = new Book();
            book.append(view, pf);
            printJob.setPageable(book);

            if (printJob.printDialog())
            {
                try
                {
                    printJob.print();
                } catch (Exception ex)
                {
                    ex.printStackTrace();
                }
            }
        }
    }

    public void saveImage() {
       if (view != null) {
           Dimension size = view.getSize();
           BufferedImage myImage =
             new BufferedImage(size.width, size.height,
             BufferedImage.TYPE_INT_RGB);
           Graphics2D g2 = myImage.createGraphics();
           view.paint(g2);

           // Show save dialog; this method does not return until the dialog is closed
           JFileChooser fc = new JFileChooser(new File(File.separator+instName+".jpg"));
           fc.setSelectedFile(new File(File.separator+instName+".jpg"));
           fc.setFileFilter(new JPGFilter());
           fc.showSaveDialog(hypertreePanel);
           File selFile = fc.getSelectedFile();

           String pathName = selFile.getPath();
           if (!(pathName.substring(pathName.length()-4).equalsIgnoreCase(".jpg")) && !(pathName.substring(pathName.length()-5).equalsIgnoreCase(".jpeg"))) {
               selFile = new File(pathName + ".jpg");
           }

           try {
             OutputStream out = new FileOutputStream(selFile);
             JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(out);
             encoder.encode(myImage);
             out.close();
           } catch (Exception e) {
             System.out.println(e);
           }
       }
     }

    // return the state of the ShowLables togle switch.
    public boolean getShowLables(){
        return jToggleShowLables.isSelected();
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        hypertreePanel = new javax.swing.JPanel();
        jCheckQueueLocal = new javax.swing.JCheckBox();
        jCheckInactive = new javax.swing.JCheckBox();
        jToggleShowLables = new javax.swing.JToggleButton();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jCheckQueueRemote = new javax.swing.JCheckBox();
        jTextQueueFilter = new javax.swing.JTextField();
        jTextChannelFilter = new javax.swing.JTextField();
        jCheckRunning = new javax.swing.JCheckBox();
        jButtonReset = new javax.swing.JButton();
        jButtonSave = new javax.swing.JButton();
        jCheckQueueAlias = new javax.swing.JCheckBox();
        jTextQueueDepth = new javax.swing.JTextField();
        jCheckConn = new javax.swing.JCheckBox();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();

        setBackground(java.awt.Color.white);
        setForeground(java.awt.Color.black);
        setOpaque(true);
        addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                formFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                formFocusLost(evt);
            }
        });

        hypertreePanel.setBackground(new java.awt.Color(255, 255, 255));
        hypertreePanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        hypertreePanel.setFont(new java.awt.Font("Tahoma", 0, 10));
        hypertreePanel.setLayout(new java.awt.BorderLayout());

        jCheckQueueLocal.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(jCheckQueueLocal, "Local");
        jCheckQueueLocal.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        jCheckQueueLocal.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jCheckQueueLocal.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckQueueLocalActionPerformed(evt);
            }
        });
        jCheckQueueLocal.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jCheckQueueLocalStateChanged(evt);
            }
        });
        jCheckQueueLocal.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jCheckQueueLocalMouseClicked(evt);
            }
        });

        jCheckInactive.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(jCheckInactive, "Inactive");
        jCheckInactive.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        jCheckInactive.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jCheckInactive.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jCheckInactiveStateChanged(evt);
            }
        });
        jCheckInactive.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jCheckInactiveMouseClicked(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jToggleShowLables, "Expand Lables");
        jToggleShowLables.setName(""); // NOI18N
        jToggleShowLables.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jToggleShowLablesMouseClicked(evt);
            }
        });

        jLabel1.setFont(new java.awt.Font("Tahoma", 1, 12));
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, "Q Filter:");

        jLabel2.setFont(new java.awt.Font("Tahoma", 1, 12));
        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, "Chl Filter:");

        org.openide.awt.Mnemonics.setLocalizedText(jCheckQueueRemote, "Remote");
        jCheckQueueRemote.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        jCheckQueueRemote.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jCheckQueueRemote.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jCheckQueueRemoteStateChanged(evt);
            }
        });
        jCheckQueueRemote.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jCheckQueueRemoteMouseClicked(evt);
            }
        });

        jTextQueueFilter.setText("^(?!SYSTEM).*");
        jTextQueueFilter.setPreferredSize(new java.awt.Dimension(94, 19));
        jTextQueueFilter.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextQueueFilterActionPerformed(evt);
            }
        });

        jTextChannelFilter.setText("^(?!SYSTEM).*");
        jTextChannelFilter.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextChannelFilterActionPerformed(evt);
            }
        });

        jCheckRunning.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(jCheckRunning, "Running");
        jCheckRunning.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        jCheckRunning.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jCheckRunning.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jCheckRunningStateChanged(evt);
            }
        });
        jCheckRunning.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jCheckRunningMouseClicked(evt);
            }
        });

        jButtonReset.setForeground(java.awt.Color.darkGray);
        org.openide.awt.Mnemonics.setLocalizedText(jButtonReset, "Reset");
        jButtonReset.setToolTipText("Reset this explorer");
        jButtonReset.setBorder(null);
        jButtonReset.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonResetActionPerformed(evt);
            }
        });

        jButtonSave.setForeground(java.awt.Color.darkGray);
        org.openide.awt.Mnemonics.setLocalizedText(jButtonSave, "Save");
        jButtonSave.setToolTipText("Print this view");
        jButtonSave.setBorder(new javax.swing.border.LineBorder(java.awt.Color.gray, 1, true));
        jButtonSave.setBorderPainted(false);
        jButtonSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonSaveActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jCheckQueueAlias, "Alias");
        jCheckQueueAlias.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        jCheckQueueAlias.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jCheckQueueAlias.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jCheckQueueAliasStateChanged(evt);
            }
        });
        jCheckQueueAlias.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jCheckQueueAliasMouseClicked(evt);
            }
        });

        jTextQueueDepth.setText("1");
        jTextQueueDepth.setPreferredSize(new java.awt.Dimension(94, 19));
        jTextQueueDepth.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextQueueDepthActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jCheckConn, "Conn");
        jCheckConn.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        jCheckConn.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jCheckConn.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jCheckConnStateChanged(evt);
            }
        });
        jCheckConn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jCheckConnMouseClicked(evt);
            }
        });

        jLabel3.setFont(new java.awt.Font("Tahoma", 1, 11));
        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, ">=");

        org.openide.awt.Mnemonics.setLocalizedText(jLabel4, "Show:");

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(jLabel1)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jTextQueueFilter, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 85, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jLabel3)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jTextQueueDepth, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 26, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(layout.createSequentialGroup()
                        .add(jLabel4)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jCheckQueueLocal)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jCheckQueueAlias)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jCheckQueueRemote)))
                .add(22, 22, 22)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(jCheckInactive)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jCheckRunning)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jCheckConn))
                    .add(layout.createSequentialGroup()
                        .add(jLabel2)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jTextChannelFilter, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 85, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 90, Short.MAX_VALUE)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(jToggleShowLables, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 114, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(layout.createSequentialGroup()
                        .add(jButtonReset, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 54, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jButtonSave, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 54, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
            .add(hypertreePanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 603, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel1)
                    .add(jTextQueueFilter, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jToggleShowLables, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 17, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel3)
                    .add(jTextQueueDepth, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel2)
                    .add(jTextChannelFilter, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(1, 1, 1)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jButtonSave, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 17, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jButtonReset, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 17, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jCheckInactive, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 15, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel4)
                    .add(jCheckQueueLocal, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 17, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jCheckQueueAlias, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 17, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jCheckQueueRemote, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 17, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jCheckRunning, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 15, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jCheckConn, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 15, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(hypertreePanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 414, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void formFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_formFocusLost
//        ExplorerUtils.activateActions(manager, false);        
    }//GEN-LAST:event_formFocusLost

    private void formFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_formFocusGained
//        ExplorerUtils.activateActions(manager, true);
        refreshInstance(instName);
    }//GEN-LAST:event_formFocusGained

    private void jCheckQueueRemoteStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jCheckQueueRemoteStateChanged
        refreshInstance(this.instName);
}//GEN-LAST:event_jCheckQueueRemoteStateChanged

    private void jCheckQueueRemoteMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jCheckQueueRemoteMouseClicked

}//GEN-LAST:event_jCheckQueueRemoteMouseClicked

    private void jToggleShowLablesMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jToggleShowLablesMouseClicked
        if (view != null){
            view.changeKleinMode(jToggleShowLables.isSelected());
        }
    }//GEN-LAST:event_jToggleShowLablesMouseClicked

    private void jCheckQueueLocalMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jCheckQueueLocalMouseClicked
   
}//GEN-LAST:event_jCheckQueueLocalMouseClicked

    private void jCheckInactiveMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jCheckInactiveMouseClicked

}//GEN-LAST:event_jCheckInactiveMouseClicked

    private void jCheckInactiveStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jCheckInactiveStateChanged
        refreshInstance(this.instName);
        //        HyperView.refreshNode();
}//GEN-LAST:event_jCheckInactiveStateChanged

    private void jCheckQueueLocalStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jCheckQueueLocalStateChanged
        refreshInstance(this.instName);
        //        HyperView.refreshNode();
}//GEN-LAST:event_jCheckQueueLocalStateChanged

    private void jTextQueueFilterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextQueueFilterActionPerformed
        refreshInstance(this.instName);
}//GEN-LAST:event_jTextQueueFilterActionPerformed

    private void jTextChannelFilterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextChannelFilterActionPerformed
        refreshInstance(this.instName);
    }//GEN-LAST:event_jTextChannelFilterActionPerformed

    private void jCheckRunningStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jCheckRunningStateChanged
        refreshInstance(instName);
}//GEN-LAST:event_jCheckRunningStateChanged

    private void jCheckRunningMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jCheckRunningMouseClicked
        // TODO add your handling code here:
}//GEN-LAST:event_jCheckRunningMouseClicked

    private void jButtonResetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonResetActionPerformed
        // reset and refresh this view.
        this.setDefaultView();
}//GEN-LAST:event_jButtonResetActionPerformed

    private void jButtonSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonSaveActionPerformed
        // printPanel();
        saveImage();
}//GEN-LAST:event_jButtonSaveActionPerformed

    private void jCheckQueueAliasStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jCheckQueueAliasStateChanged
        refreshInstance(instName);
}//GEN-LAST:event_jCheckQueueAliasStateChanged

    private void jCheckQueueAliasMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jCheckQueueAliasMouseClicked
        // TODO add your handling code here:
}//GEN-LAST:event_jCheckQueueAliasMouseClicked

    private void jTextQueueDepthActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextQueueDepthActionPerformed
        refreshInstance(instName);
}//GEN-LAST:event_jTextQueueDepthActionPerformed

    private void jCheckQueueLocalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckQueueLocalActionPerformed
        // TODO add your handling code here:
}//GEN-LAST:event_jCheckQueueLocalActionPerformed

    private void jCheckConnStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jCheckConnStateChanged
        refreshInstance(instName);
}//GEN-LAST:event_jCheckConnStateChanged

    private void jCheckConnMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jCheckConnMouseClicked
        // TODO add your handling code here:
}//GEN-LAST:event_jCheckConnMouseClicked
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel hypertreePanel;
    private javax.swing.JButton jButtonReset;
    private javax.swing.JButton jButtonSave;
    private javax.swing.JCheckBox jCheckConn;
    private javax.swing.JCheckBox jCheckInactive;
    private javax.swing.JCheckBox jCheckQueueAlias;
    private javax.swing.JCheckBox jCheckQueueLocal;
    private javax.swing.JCheckBox jCheckQueueRemote;
    private javax.swing.JCheckBox jCheckRunning;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JTextField jTextChannelFilter;
    private javax.swing.JTextField jTextQueueDepth;
    private javax.swing.JTextField jTextQueueFilter;
    private javax.swing.JToggleButton jToggleShowLables;
    // End of variables declaration//GEN-END:variables
    
    /**
     * Gets default instance. Do not use directly: reserved for *.settings files only,
     * i.e. deserialization routines; otherwise you could get a non-deserialized instance.
     * To obtain the singleton instance, use {@link findInstance}.
     */
    public static synchronized HTreeTopComponent getDefault(String networkName) {
        if (instance == null) {
            instance = findInstance(networkName);
        }
        return instance;
    }    
    public static synchronized HTreeTopComponent getDefault() {
        if (instance == null) {
//            instance = new HTreeTopComponent(RootNode.getTopName());
            instance = findInstance(EntryPoint.GetDefaultEPName());
        }
        return instance;
    }
    
    /**
     * Obtain the HTreeTopComponent instance. Never call {@link #getDefault} directly!
     */
    public static synchronized HTreeTopComponent findInstance() {
        TopComponent win = WindowManager.getDefault().findTopComponent(PREFERRED_ID);
        if (win == null) {
            ErrorManager.getDefault().log(ErrorManager.WARNING, "Cannot find HTree component. It will not be located properly in the window system.");
            return getDefault();
        }
        if (win instanceof HTreeTopComponent) {
            return (HTreeTopComponent)win;
        }
        ErrorManager.getDefault().log(ErrorManager.WARNING, "There seem to be multiple components with the '" + PREFERRED_ID + "' ID. That is a potential source of errors and unexpected behavior.");
        return getDefault();
    }
    public static HTreeTopComponent findInstance(String thisName){
        HTreeTopComponent httc = null;
//        TopComponent win = (TopComponent) HTreeTopComponent.namedInstance.get(RootNode.getTopName());
        TopComponent win = (TopComponent) HTreeTopComponent.namedInstance.get(EntryPoint.GetDefaultEPName());
        if (namedInstance.containsKey(thisName)){
            httc = (HTreeTopComponent) namedInstance.get(thisName);
        } else {            
            httc = new HTreeTopComponent(thisName);       
        }
//        win.close();              
//        win = httc;
        if (!httc.isDisplayable()){
            setActive(httc);
        }
//        win.open();
//        win.requestActive();
        return httc;
    }
    public static void setActive(HTreeTopComponent top){
        TopComponent win = (TopComponent) top;
        win.open();
        win.requestActive();
    }
    
    public static void refreshAll(){        
        Iterator e = namedInstance.keySet().iterator();
        while (e.hasNext()){
            String instanceName = (String) e.next();
            refreshInstance(instanceName);            
        }        
    }    
    public static void refreshInstance(String instanceName){        
        HTreeTopComponent thisWindow = findInstance(instanceName);            
        String selectedNodeName = thisWindow.getSelectedNodeName();
                // find currently selected node, to be re-selected               
        HyperTree.refreshNode(instanceName);     // Build new node   
        if ((thisWindow.isShowing()) && (selectedNodeName != null)){  // select node "selectedNodeName" if selectedNodeName has a value       
            System.out.println("selectedNode : " + selectedNodeName);
            HTNode selectedNode = HyperTree.findInstance(instanceName).getHTNode(selectedNodeName);
            if (selectedNode != null){
                HyperTree.findInstance(instanceName).getHyperTree().changeSelectedNode(selectedNode);
            }   
        }        
        thisWindow.revalidate();
        thisWindow.jToggleShowLablesMouseClicked(null);   // to set "long lables" if required 
    }
    
    @Override
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_ALWAYS;
    }
    
    @Override
    public void componentOpened() {
        // TODO add custom code on component opening
    }
    
    @Override
    public void componentClosed() {
        // TODO add custom code on component closing
    }

    @Override
    /** replaces this in object stream */
    public Object writeReplace() {
        return new ResolvableHelper(this.instName);
    }
    
    @Override
    protected String preferredID() {
        return PREFERRED_ID;
    }
    
    final static class ResolvableHelper implements Serializable {
        private static final long serialVersionUID = 1L;
//        private static String networkName = RootNode.getTopName();
        private static String networkName = EntryPoint.GetDefaultEPName();
        public ResolvableHelper(String nwName){
            networkName = nwName;
        }
        public Object readResolve() {
            return HTreeTopComponent.getDefault(networkName);
        }
    }
    
}

class JPGFilter extends javax.swing.filechooser.FileFilter {
    public boolean accept(File f) {
        return f.isDirectory() || f.getName().toLowerCase().endsWith(".jpg");
    }

    public String getDescription() {
        return "JPEG files";
    }
}
