/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.palooca;

/**
 *
 * @author chris
 */
public class OSUtil {

     public static boolean isWindows() {
        String os = System.getProperty("os.name");
        if (os != null && os.startsWith("Windows"))
            return true;
        return false;
    }

    public static boolean isOSX() {
        String os = System.getProperty("os.name");
        // System.out.println(os);
        if (os != null && os.startsWith("Mac OS X"))
            return true;
        return false;
    }

    public static void invokeRunnable(Runnable runnable) {
        if (isOSX()) {
            new Thread(runnable).start();
        }
        else {
            runnable.run();
        }
    }

    public static void invokeUI(Runnable worker) {
         /*
         if (OSUtil.isOSX()) {
              javax.swing.SwingUtilities.invokeLater(worker);
           }
           else {
               worker.run();
           }
          *
          */
        worker.run();
    }

}
