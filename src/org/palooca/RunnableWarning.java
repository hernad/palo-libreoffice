/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.palooca;

import javax.swing.JOptionPane;

/**
 *
 * @author chris
 */
public class RunnableWarning implements Runnable {

        private String message;
        private String title;

        public RunnableWarning(String message, String title) {
            this.message = message;
            this.title = title;
        }



        @Override
        public void run() {
            Runnable worker = new Runnable() {
                @Override
                public void run() {
                        JOptionPane.showMessageDialog(null,
                                    message,
                                    title, JOptionPane.WARNING_MESSAGE);
                }
            };
            javax.swing.SwingUtilities.invokeLater(worker);
            //OSUtil.invokeUI(worker);
        }
    }
