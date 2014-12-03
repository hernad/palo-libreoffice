/*
 * PalOOCaUI.java
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
 * Created on 1. September 2007, 18:35
 *
 */

package org.palooca;

import com.sun.star.beans.PropertyValue;
import com.sun.star.frame.XDispatch;
import com.sun.star.frame.XStatusListener;
import com.sun.star.uno.XComponentContext;
import com.sun.star.lib.uno.helper.Factory;
import com.sun.star.lang.XSingleComponentFactory;
import com.sun.star.registry.XRegistryKey;
import com.sun.star.lib.uno.helper.WeakBase;
import com.sun.star.util.URL;
import org.palooca.PalOOCaManager.KeepAlive;
import org.palooca.dataimport.ImportResult;
import org.palooca.dialogs.ExternalDataDialog;
import org.palooca.dialogs.ImportFunctionsDialog;
import org.palooca.dialogs.InsertDataDialog;
import org.palooca.dialogs.InsertElementDialog;
import org.palooca.dialogs.Modeller;
import org.palooca.dialogs.SubsetModeller;
import org.palooca.dialogs.ViewDialog;

/**
 *
 * @author Andreas Schneider
 */

public final class PalOOCaUI extends WeakBase
   implements com.sun.star.lang.XServiceInfo,
              com.sun.star.frame.XDispatchProvider,
              com.sun.star.lang.XInitialization,
              com.sun.star.frame.XDispatch
{

    private class RunnableModal implements Runnable {

        private URL url;

        public RunnableModal(URL url) {
            this.url = url;
        }

        @Override
        public void run() {
            System.err.println("Handling UI Event...");
            //manager.new KeepAlive().run();
            Runnable worker = new Runnable() {
               public void run() {
                        if (url.Protocol.equals("org.palooca:") && manager.UIEnabled()) {
                            if (url.Path.equals("ShowAbout")) {
                                manager.getAboutDialog().setVisible(true);
                            } else if (url.Path.equals("ShowConnectionManager")) {
                //                ConnectionManagerDialog connectionManagerDialog = manager.getConnectionManagerDialog();
                //                connectionManagerDialog.setVisible(true);
                //                connectionManagerDialog.toFront();
                            } else if (url.Path.equals("InsertData")) {
                                InsertDataDialog insertDataDialog = new InsertDataDialog(null, true,
                                        xContext, manager.getSelectedRange());
                                manager.showModal(insertDataDialog);
                            } else if (url.Path.equals("InsertElements")) {
                                InsertElementDialog insertElementDialog = new InsertElementDialog(null, true, xContext, manager.getSelectedRange());
                                manager.showModal(insertElementDialog);
                            } else if (url.Path.equals("InsertSubset")) {
                                SubsetModeller subsetModeller = new SubsetModeller(null, false, xContext);
                                subsetModeller.setVisible(true);
                                subsetModeller.setAlwaysOnTop(true);
                            } else if (url.Path.equals("CreateView")) {
                                ViewDialog createViewDialog = new ViewDialog(null, true,
                                        xContext);
                                manager.showModal(createViewDialog);
                            } else if (url.Path.equals("ImportFunctions")) {
                                ImportFunctionsDialog importFunctionsDialog = new ImportFunctionsDialog(null, true,
                                        xContext);
                                ImportResult.beginImport(importFunctionsDialog);
                                manager.recalculateDocument(); //do the actual import
                                ImportResult.endImport();
                                importFunctionsDialog.dispose();
                                manager.recalculateDocument(); //update all other fields
                            } else if (url.Path.equals("ExternalDataSource")) {
                                ImportResult.beginExternalImport();
                                ExternalDataDialog externalData = new ExternalDataDialog(null,true,xContext);
                                if (externalData.SetupImport()) {
                                    manager.showModal(externalData);
                                }
                                ImportResult.endExternalImport();
                                manager.recalculateDocument(); // Refresh the screen
                            }
                            else if (url.Path.equals("ShowModeler")) {
                                Modeller modeler = new Modeller(null, true, xContext);
                                manager.showModal(modeler);
                            } else if (url.Path.equals("CreateSnapshot")) {
                                PalOOCaSheetReplicator.freezeDocument(xContext);
                            }
                         }
               }
           };
           OSUtil.invokeUI(worker);
        }
    }
    
    private static final String implementationName = PalOOCaUI.class.getName();
    private static final String[] serviceNames = {
        "com.sun.star.frame.ProtocolHandler" };
 
    private final XComponentContext xContext;
    private final PalOOCaManager manager;

    public PalOOCaUI( XComponentContext context )
    {
        xContext = context;
        manager = PalOOCaManager.getInstance(context);
    };

    public static XSingleComponentFactory __getComponentFactory( String sImplementationName ) {
        XSingleComponentFactory xFactory = null;

        if ( sImplementationName.equals( implementationName ) )
            xFactory = Factory.createComponentFactory(PalOOCaUI.class, serviceNames);
        return xFactory;
    }

    public static boolean __writeRegistryServiceInfo( XRegistryKey xRegistryKey ) {
        return Factory.writeRegistryServiceInfo(implementationName,
                                                serviceNames,
                                                xRegistryKey);
    }

    // com.sun.star.lang.XServiceInfo:
    public String getImplementationName() {
         return implementationName;
    }

    public boolean supportsService( String sService ) {
        int len = serviceNames.length;

        for( int i = 0; i < len; i++) {
            if (sService.equals(serviceNames[i]))
                return true;
        }
        return false;
    }

    public String[] getSupportedServiceNames() {
        return serviceNames;
    }

    // com.sun.star.frame.XDispatchProvider:
    public XDispatch queryDispatch( URL url,
                                    String targetFrameName,
                                    int searchFlags )
    {
        if (url.Protocol.equals("org.palooca:") ) {
            if ( url.Path.equals("ShowAbout") ||
//                 url.Path.equals("ShowConnectionManager") ||
                 url.Path.equals("InsertData") ||
                 url.Path.equals("InsertElements") ||
                 url.Path.equals("InsertSubset") ||
                 url.Path.equals("CreateView") ||
                 url.Path.equals("ImportFunctions") ||
                 url.Path.equals("ExternalDataSource") ||
                 url.Path.equals("ShowModeler") ||
                 //url.Path.equals("WikiPalo") ||
                 url.Path.equals("CreateSnapshot") )
                return this;
        }
        return null;
    }

    // com.sun.star.frame.XDispatchProvider:
    public com.sun.star.frame.XDispatch[] queryDispatches(
         com.sun.star.frame.DispatchDescriptor[] seqDescriptors )
    {
        int nCount = seqDescriptors.length;
        com.sun.star.frame.XDispatch[] seqDispatcher =
            new com.sun.star.frame.XDispatch[seqDescriptors.length];

        for( int i=0; i < nCount; ++i )
        {
            seqDispatcher[i] = queryDispatch(seqDescriptors[i].FeatureURL,
                                             seqDescriptors[i].FrameName,
                                             seqDescriptors[i].SearchFlags );
        }
        return seqDispatcher;
    }

    // com.sun.star.lang.XInitialization:
    public void initialize( Object[] object )
        throws com.sun.star.uno.Exception
    {
        
    }

    // com.sun.star.frame.XDispatch:
    public void dispatch( URL url,
                          PropertyValue[] arguments )
    {
      //if (javax.swing.SwingUtilities.isEventDispatchThread()) {
      //  new RunnableModal(url).run();
      //} else {
        System.err.println("Dispatching UI request...");
        OSUtil.invokeRunnable(new RunnableModal(url));
      //}
    }

    public void addStatusListener( XStatusListener xControl, URL aURL ) {
    }

    public void removeStatusListener( XStatusListener xControl, URL aURL ) {
    }

}
