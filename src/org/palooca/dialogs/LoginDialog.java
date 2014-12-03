/*
 * LoginDialog.java
 *
 * Palo Open Office Calc AddIn
 * Copyright (C) 2008 PalOOCa Team,  Tensegrity Software GmbH, 2009

 * The software is licensed under an Open-Source License (GPL).
 * If you want to redistribute the software you must observe the regulations of
 * the GPL . If you want to redistribute the software without the
 * restrictions of the GPL, you have to contact Tensegrity Software GmbH
 * (Tensegrity) for written consent to do so.
 * Tensegrity may offer commercial licenses for redistribution (Dual Licensing)
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *
 * Created on 29. August 2007, 18:19
 *
 */

package org.palooca.dialogs;

import com.sun.star.uno.XComponentContext;
import java.util.ResourceBundle;
import javax.swing.JOptionPane;
import org.palooca.PalOOCaManager;

/**
 *
 * @author  Andreas Schneider
 */
public class LoginDialog extends javax.swing.JDialog {
    
    private int modalResult = JOptionPane.CANCEL_OPTION;
    private ResourceBundle resourceBundle;
    
    public int getModalResult() {
        return modalResult;
    }
    
    /** Creates new form LoginDialog */
    public LoginDialog(java.awt.Dialog parent, boolean modal, XComponentContext context) {
        super(parent, modal);
        this.resourceBundle = PalOOCaManager.getInstance(context).getResourceBundle("org/palooca/dialogs/PalOOCaDialogs");
        initComponents();
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        lblUsername = new javax.swing.JLabel();
        lblPassword = new javax.swing.JLabel();
        txtUsername = new javax.swing.JTextField();
        txtPassword = new javax.swing.JPasswordField();
        btnOK = new javax.swing.JButton();
        btnCancel = new javax.swing.JButton();
        lblDescription = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/palooca/dialogs/PalOOCaDialogs"); // NOI18N
        setTitle(bundle.getString("Login_Caption")); // NOI18N
        setName("LoginDialog"); // NOI18N
        setResizable(false);

        lblUsername.setLabelFor(txtUsername);
        lblUsername.setText(bundle.getString("Username")); // NOI18N

        lblPassword.setText(bundle.getString("Password")); // NOI18N

        btnOK.setText(bundle.getString("OK")); // NOI18N
        this.getRootPane().setDefaultButton(btnOK);
        btnOK.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnOKActionPerformed(evt);
            }
        });

        btnCancel.setText(bundle.getString("Cancel")); // NOI18N
        btnCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCancelActionPerformed(evt);
            }
        });

        lblDescription.setText(bundle.getString("Login_Server")); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(lblDescription)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                        .add(btnOK)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(btnCancel))
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(lblUsername)
                            .add(lblPassword))
                        .add(30, 30, 30)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(txtUsername, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 152, Short.MAX_VALUE)
                            .add(txtPassword, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 152, Short.MAX_VALUE))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(lblDescription)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(lblUsername)
                    .add(txtUsername, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(lblPassword)
                    .add(txtPassword, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(btnCancel)
                    .add(btnOK))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCancelActionPerformed
        modalResult = JOptionPane.CANCEL_OPTION;
        setVisible(false);
    }//GEN-LAST:event_btnCancelActionPerformed

    private void btnOKActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnOKActionPerformed
        modalResult = JOptionPane.OK_OPTION;
        setVisible(false);
    }//GEN-LAST:event_btnOKActionPerformed

    public javax.swing.JButton getOKButton() {
        return btnOK;
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCancel;
    private javax.swing.JButton btnOK;
    public javax.swing.JLabel lblDescription;
    private javax.swing.JLabel lblPassword;
    private javax.swing.JLabel lblUsername;
    public javax.swing.JPasswordField txtPassword;
    public javax.swing.JTextField txtUsername;
    // End of variables declaration//GEN-END:variables
    
}
