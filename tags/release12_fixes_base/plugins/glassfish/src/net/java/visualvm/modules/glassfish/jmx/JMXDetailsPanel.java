/*
 * JMXDetailsPanel.java
 *
 * Created on March 14, 2008, 9:17 AM
 */

package net.java.visualvm.modules.glassfish.jmx;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;

/**
 *
 * @author  Jaroslav Bachorik
 */
public class JMXDetailsPanel extends javax.swing.JPanel {    
    public static final String VALIDITY_PROPERTY = JMXDetailsPanel.class.getName() + "#isValid";
    
    private boolean portValid = true;
    private boolean usernameValid = false;
    private boolean passwordValid = false;
    
    /** Creates new form JMXDetailsPanel */
    public JMXDetailsPanel() {
        initComponents();
        postInit();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        userName = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        password = new javax.swing.JPasswordField();
        jLabel3 = new javax.swing.JLabel();
        serverPort = new javax.swing.JSpinner();
        jSeparator1 = new javax.swing.JSeparator();

        jLabel1.setLabelFor(userName);
        jLabel1.setText(org.openide.util.NbBundle.getMessage(JMXDetailsPanel.class, "JMXDetailsPanel.jLabel1.text_1")); // NOI18N

        userName.setText(org.openide.util.NbBundle.getMessage(JMXDetailsPanel.class, "JMXDetailsPanel.userName.text_1")); // NOI18N

        jLabel2.setLabelFor(password);
        jLabel2.setText(org.openide.util.NbBundle.getMessage(JMXDetailsPanel.class, "JMXDetailsPanel.jLabel2.text_1")); // NOI18N

        password.setText(org.openide.util.NbBundle.getMessage(JMXDetailsPanel.class, "JMXDetailsPanel.password.text")); // NOI18N

        jLabel3.setText(org.openide.util.NbBundle.getMessage(JMXDetailsPanel.class, "JMXDetailsPanel.jLabel3.text_1")); // NOI18N

        serverPort.setModel(new javax.swing.SpinnerNumberModel(8686, 1024, 65535, 1));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jSeparator1, javax.swing.GroupLayout.DEFAULT_SIZE, 253, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1)
                            .addComponent(jLabel2))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(password, javax.swing.GroupLayout.DEFAULT_SIZE, 164, Short.MAX_VALUE)
                            .addComponent(userName, javax.swing.GroupLayout.DEFAULT_SIZE, 164, Short.MAX_VALUE)))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(serverPort, javax.swing.GroupLayout.DEFAULT_SIZE, 117, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(userName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(password, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(16, 16, 16)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(serverPort, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JPasswordField password;
    private javax.swing.JSpinner serverPort;
    private javax.swing.JTextField userName;
    // End of variables declaration//GEN-END:variables

    private void postInit() {
        serverPort.getModel().addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent e) {
                if (serverPort.getModel().getValue() != null && serverPort.getModel().getValue().toString().length() > 0) {
                    portValid = true;
                } else {
                    portValid = false;
                }
                firePropertyChange(VALIDITY_PROPERTY, null, null);
            }
        });
        userName.getDocument().addDocumentListener(new DocumentListener() {

            public void insertUpdate(DocumentEvent e) {
                checkValidity(e.getDocument());
            }

            public void removeUpdate(DocumentEvent e) {
                checkValidity(e.getDocument());
            }

            public void changedUpdate(DocumentEvent e) {
                checkValidity(e.getDocument());
            }
            
            private void checkValidity(Document document) {
                if (document.getLength() > 0) {
                    usernameValid = true;
                } else {
                    usernameValid = false;
                }
                firePropertyChange(VALIDITY_PROPERTY, null, null);
            }
        });
        password.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                checkValidity(e.getDocument());
            }

            public void removeUpdate(DocumentEvent e) {
                checkValidity(e.getDocument());
            }

            public void changedUpdate(DocumentEvent e) {
                checkValidity(e.getDocument());
            }
            
            private void checkValidity(Document document) {
                if (document.getLength() > 0) {
                    passwordValid = true;
                } else {
                    passwordValid = false;
                }
                firePropertyChange(VALIDITY_PROPERTY, null, null);
            }
        });
    }
    
    public int getServerPort() {
        return (Integer)serverPort.getModel().getValue();
    }

    public void setServerPort(int value) {
        this.serverPort.getModel().setValue(value);
    }

    public String getUserName() {
        return userName.getText();
    }
    
    public void setUserName(String value) {
        userName.setText(value);
    }
    
    public String getPassword() {
        return new String(password.getPassword());
    }
    
    public void setPassword(String value) {
        password.setText(value);
    }
    
    public boolean hasValidCredentials() {
        return portValid && usernameValid && passwordValid;
    }
}
