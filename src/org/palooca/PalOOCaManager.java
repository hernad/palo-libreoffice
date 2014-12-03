/*
 * PalOOCa.java
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
 * Created on 3. September 2007, 13:01
 *
 */

package org.palooca;

// <editor-fold defaultstate="collapsed" desc="Imports">
import com.jedox.palojlib.exceptions.PaloException;
import com.jedox.palojlib.exceptions.PaloJException;
import com.sun.star.awt.MouseEvent;
import com.sun.star.awt.Rectangle;
import com.sun.star.awt.WindowEvent;
import com.sun.star.awt.XUserInputInterception;
import com.sun.star.beans.PropertyValue;
import com.sun.star.beans.PropertyVetoException;
import com.sun.star.beans.UnknownPropertyException;
import com.sun.star.beans.XPropertySet;
import com.sun.star.container.ElementExistException;
import com.sun.star.container.NoSuchElementException;
import com.sun.star.container.XIndexContainer;
import com.sun.star.container.XNameAccess;
import com.sun.star.container.XNameContainer;
import com.sun.star.document.XEventBroadcaster;
import com.sun.star.drawing.XDrawPage;
import com.sun.star.drawing.XDrawPages;
import com.sun.star.drawing.XDrawPagesSupplier;
import com.sun.star.form.XFormsSupplier;
import com.sun.star.form.binding.XBindableValue;
import com.sun.star.form.binding.XListEntrySink;
import com.sun.star.frame.XComponentLoader;
import com.sun.star.frame.XController;
import com.sun.star.frame.XDesktop;
import com.sun.star.frame.XDispatch;
import com.sun.star.frame.XDispatchProvider;
import com.sun.star.frame.XFrame;
import com.sun.star.frame.XModel;
import com.sun.star.i18n.XLocaleData;
import com.sun.star.lang.EventObject;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.lang.XComponent;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.lang.XMultiServiceFactory;
import com.sun.star.lang.XTypeProvider;
import com.sun.star.sheet.ActivationEvent;
import com.sun.star.sheet.XCalculatable;
import com.sun.star.sheet.XCellRangeAddressable;
import com.sun.star.sheet.XFunctionAccess;
import com.sun.star.sheet.XSheetCellRange;
import com.sun.star.sheet.XSpreadsheet;
import com.sun.star.sheet.XSpreadsheetDocument;
import com.sun.star.sheet.XSpreadsheetView;
import com.sun.star.sheet.XSpreadsheets;
import com.sun.star.style.XStyleFamiliesSupplier;
import com.sun.star.table.CellContentType;
import com.sun.star.table.CellRangeAddress;
import com.sun.star.table.XCell;
import com.sun.star.table.XCellRange;
import com.sun.star.uno.AnyConverter;
import com.sun.star.uno.Type;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;
import com.sun.star.util.MalformedNumberFormatException;
import com.sun.star.util.URL;
import com.sun.star.util.XModifyBroadcaster;
import com.sun.star.util.XNumberFormats;
import com.sun.star.util.XNumberFormatsSupplier;
import com.sun.star.util.XURLTransformer;
import com.sun.star.view.XSelectionSupplier;
import com.sun.star.xml.AttributeData;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import com.jedox.palojlib.interfaces.*;
import com.sun.star.container.XIndexAccess;
import com.sun.star.sheet.XActivationBroadcaster;
import java.awt.Toolkit;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JDialog;
import org.palooca.config.BaseConfig;
import org.palooca.dialogs.AboutDialog;
import org.palooca.dialogs.DimensionSubsetListItem;
import org.palooca.dialogs.ElementBrowserDialog;
import org.palooca.dialogs.ElementSingleSelectDialog;
import org.palooca.dialogs.PaloDialogUtilities;
import org.palooca.dialogs.TextEditorDialog;
import org.palooca.dialogs.ViewDialog;
import org.palooca.formula.FormulaParser;
import org.palooca.formula.FunctionToken;
import org.palooca.formula.Token;
import org.palooca.network.ConnectionHandler;
import org.palooca.network.ConnectionState;
// </editor-fold>

/**
 *
 * @author Andreas Schneider
 */
public class PalOOCaManager
        implements com.sun.star.document.XEventListener,
        com.sun.star.sheet.XActivationEventListener,
        com.sun.star.awt.XMouseClickHandler,
        com.sun.star.view.XSelectionChangeListener,
        com.sun.star.util.XModifyListener,
        com.sun.star.awt.XWindowListener
{

    private class TextInputActionListener implements ActionListener {

        private TextEditorDialog editor;
        private ICube cube;
        private String[] stringCoords;

        public TextInputActionListener(TextEditorDialog editor, ICube cube, String[] stringCoords) {
            this.editor = editor;
            this.cube = cube;
            this.stringCoords = stringCoords;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
           try {
                PaloLibUtil.setData(cube, stringCoords, editor.txtMain.getText());
            } catch (Exception ex) {
                if (ex.toString().contains("splash disabled")) { /*WARNING JPalo version compatibility?! TODO */
                    JOptionPane.showMessageDialog(null, coreResourceBundle.getString("Cumulated_Cell_Write"), coreResourceBundle.getString("Write_Error"), JOptionPane.WARNING_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(null, ex.getLocalizedMessage());
                }
            }
            activeCell.setFormula(activeCell.getFormula());
        }
    }

    private class MatrixElementActionListener implements ActionListener {

        private ElementBrowserDialog browser;
        private PalOOCaView view;
        private DimensionSubsetListItem dimItem;

        public MatrixElementActionListener(ElementBrowserDialog browser, PalOOCaView view, DimensionSubsetListItem dimItem) {
            this.browser = browser;
            this.view = view;
            this.dimItem = dimItem;
        }


        @Override
        public void actionPerformed(ActionEvent e) {
             browser.getSelection(dimItem);
             view.generate(getActiveSpreadSheet());
        }
    }

    private class FilterElementActionListener implements ActionListener {
        private ElementSingleSelectDialog browser;

        public FilterElementActionListener(ElementSingleSelectDialog browser) {
            this.browser = browser;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            //redefine async used variables
            XSpreadsheets sheets = activeDocument.getSheets();
            XSpreadsheet sheet = getActiveSpreadSheet();
           
            PalOOCaView view = PalOOCaView.parseSpreadsheet(context, getActiveSpreadSheet(),true);
            boolean fullUpdate = false;
            for (int i = 0; i < view.getFilterDimensions().size(); i++) {
                if (view.getFilterDimensions().get(i).getDimension() == browser.getDimension()) {
                    DimensionSubsetListItem dimItem = view.getFilterDimensions().get(i);
                    dimItem.setSelectedFilterPath(browser.getSelectionPath());
                    dimItem.setSelectedFilterElementObject(browser.getSelectionElement());
                    IAttribute dimAttribute = dimItem.getAttribute();
                    if (dimAttribute == null && browser.getAttribute() != null) fullUpdate = true;
                    if (dimAttribute != null && browser.getAttribute() == null) fullUpdate = true;
                    if (dimAttribute != null && browser.getAttribute() != null && !dimAttribute.getName().equals(browser.getAttribute().getName())) fullUpdate = true;
                    if (fullUpdate) dimItem.setAttribute(browser.getAttribute());
                    break;
                }
            }
            view.generate(sheet,fullUpdate,fullUpdate);
       }     
    }


    private class RunnableModal implements Runnable {

        private MouseEvent e;
        private boolean result;

        public RunnableModal(MouseEvent e) {
            this.e = e;
        }

        private boolean processMouseEvent(MouseEvent e) {
            System.err.println("Handling Mouse Event.");
            //new KeepAlive().run();
            if (e.ClickCount == 2) {
                if (activeCell == null || activeCell.getError() != 0)
                    return false;
                if (getData(activeCell, "IsPaloView", "").equals("True")) {
                    long time = System.currentTimeMillis();
                    PalOOCaView view = PalOOCaView.parseSpreadsheet(context, getActiveSpreadSheet(),true);
                    if (view != null) {
                        ViewDialog viewDialog = new ViewDialog(null, false, context, view);
                        showModal(viewDialog);
                    }
                    System.err.println("View Dialog display in "+(System.currentTimeMillis()-time));
                    return true;
                } else if (formula != null && formula.size() > 0) {
                    Token token = formula.get(0);
                    if (token instanceof FunctionToken) {
                        ArrayList<Token> arguments = token;
                        XSpreadsheets sheets = activeDocument.getSheets();
                        XSpreadsheet sheet = getActiveSpreadSheet();
                        if (token.getContent().equals("org.palooca.PalOOCa.PALO_ENAME")) {
                            if (arguments.size() > 3) {
                                Double type = (Double) arguments.get(3).calculate(functionAccess, sheets, sheet);
                                if (type != null) {
                                    switch (type.intValue()) {
                                        case 0:                 // show element selection dialog
                                            if (isShowElementOnDblClk()) {
                                                PalOOCaView view = PalOOCaView.parseSpreadsheet(context, getActiveSpreadSheet(),true);
                                                if (view != null) {
                                                    String elementPath = arguments.get(4).calculate(functionAccess, sheets, sheet).toString();
                                                    if (showElementSelection(view, elementPath)) {
                                                        return true;
                                                    }
                                                }
                                            }
                                            break;
                                        case 1: // Show element selection upon double click on filter dimension
                                            String  path = null;
                                            String  attribute = null;
                                            if (arguments.size() > 4) {
                                                path = arguments.get(4).calculate(functionAccess, sheets, sheet).toString();
                                            }
                                            if (arguments.size() > 5) {
                                                attribute = arguments.get(5).calculate(functionAccess, sheets, sheet).toString();
                                            }
                                            ElementSingleSelectDialog browser = new ElementSingleSelectDialog(null, false, context,
                                                   arguments.get(0).calculate(functionAccess, sheets, sheet).toString(),
                                                   arguments.get(1).calculate(functionAccess, sheets, sheet).toString(),
                                                   arguments.get(2).calculate(functionAccess, sheets, sheet).toString(),
                                                   path,
                                                   attribute);
                                            browser.getOKButton().addActionListener(new FilterElementActionListener(browser));
                                            showModal(browser);
                                            break;
                                        case 2: // Collapse or expand an element
                                        case 3:
                                            if (arguments.size() < 5)
                                                break;
                                            PalOOCaView view = PalOOCaView.parseSpreadsheet(context, getActiveSpreadSheet(),true);
                                            if (view != null) {
                                                if (isShowElementOnDblClk()) {
                                                    String elementPath = arguments.get(4).calculate(functionAccess, sheets, sheet).toString();
                                                    if (showElementSelection(view, elementPath))
                                                        return true;
                                                }
                                                String databaseName = arguments.get(0).calculate(functionAccess, sheets, sheet).toString();
                                                String dimensionName = arguments.get(1).calculate(functionAccess, sheets, sheet).toString();
                                                String elementName = arguments.get(2).calculate(functionAccess, sheets, sheet).toString();
                                                String elementPath = arguments.get(4).calculate(functionAccess, sheets, sheet).toString();
                                                view.handleDoubleClick(getActiveSpreadSheet(), databaseName, dimensionName, elementName, elementPath);
                                            }
                                            break;
                                    }
                                    return true;
                                }
                            }
                        } else if (token.getContent().startsWith("org.palooca.PalOOCa.PALO_DATA")) {
                            if (arguments.size() > 2) {
                                String servdb = arguments.get(0).calculate(functionAccess, sheets, sheet).toString();
                                String cubeName = arguments.get(1).calculate(functionAccess, sheets, sheet).toString();


                                Object[] coords = new Object[arguments.size() - 2];
                                for (int i = 2; i < arguments.size(); i++)
                                    coords[i - 2] = arguments.get(i).calculate(functionAccess, sheets, sheet);
                                String[] stringCoords = PalOOCaImpl.processCoordinates(coords);

                                IDatabase database = connectionHandler.getDatabase(servdb);
                                ICube cube = null;
                                if (database != null)
                                    cube = database.getCubeByName(cubeName);

                                Object oldData = null;
                                if (cube != null)
                                    oldData = PaloLibUtil.getData(cube, stringCoords);

                                boolean isString = false;
                                IDimension[] dims = cube.getDimensions();
                                IElement[] elems = new IElement[dims.length];
                                for (int i = 0; i < dims.length; i++) {
                                    elems[i] = dims[i].getElementByName(stringCoords[i],false);
                                    if (elems[i].getType() == IElement.ElementType.ELEMENT_STRING)
                                        isString = true;
                                }

                                if (isString) {
                                    TextEditorDialog editor = new TextEditorDialog(null, false, context);
                                    editor.txtMain.setText((String)oldData);
                                    editor.getOKButton().addActionListener(new TextInputActionListener(editor, cube, stringCoords));
                                    showModal(editor);
                                    return true;
                                }
                            }
                        }
                    }
                }
            }
            return false;
        }

        @Override
        public void run() {
            Runnable worker =  new Runnable() {
                @Override
                public void run() {
                    try {
                        if (getOpenDialogs().isEmpty()) {
                            result = processMouseEvent(e);
                        }
                    } catch (Exception e) {
                        Logger.getLogger(PalOOCaManager.class.getName()).log(Level.SEVERE, e.getMessage(),e);
                    }
                }
            };
            OSUtil.invokeUI(worker);
        }

        public boolean getResult() {
            return result;
        }
    }

    //TODO purpose is to avoid, that sometimes windows do not pop up after a certain time of inactivty. still have to further investigate the reason
    public class KeepAlive {
        public void run() {
            try {
                if (getConnectionHandler().getLastConnectionInfo() != null && getConnectionHandler().getLastConnectionInfo().getState().equals(ConnectionState.Connected)) {
                    if (!getConnectionHandler().getLastConnectionInfo().getConnection().isConnected()) getConnectionHandler().getLastConnectionInfo().getConnection().open();
                }
                Toolkit.getDefaultToolkit().sync();

            }
            catch (Exception e) {}
         }
    }


    private static final int VERSION_UNCHECKED = 0;
    private static final int VERSION_OK = 1;
    private static final int VERSION_INVALID = 2;
    
    private static PalOOCaManager instance;
    private static int javaVersionState = VERSION_UNCHECKED;

    // settings for insert view
    private double columnWidth = 2.9;
    private boolean columnCustomSize = true;
    private boolean columnLineBreak = true;
    private boolean rowInset = true;
    private boolean showElementOnDblClk = false;
    private boolean hideEmptyData = false;
    private boolean countDistinct = false;
    private boolean hideIdsOnAttributeUse = true;
    private int functionType = 0;
    private String importFilePath = "";
    private Integer maxSheetColumns = null;
    private Integer maxSheetRows = null;
    private List<JDialog> openDialogs = Collections.synchronizedList(new ArrayList<JDialog>());
    private XSpreadsheet lastActiveSheet;

    private Map<String,IDimension> dimensionLookUp = new HashMap<String,IDimension>();
    private Map<String,IDatabase> databaseLookUp = new HashMap<String,IDatabase>();

    private String concatName(String database, String dimension) {
        return database+":"+dimension;
    }

    public void clearDimensionCache() {
        dimensionLookUp.clear();
        databaseLookUp.clear();
    }

    public List<JDialog> getOpenDialogs() {
        return openDialogs;
    }

    public int getMaxSheetColumns() {
        if (maxSheetColumns == null) { //TODO find a more elegant solution for this
            maxSheetColumns = 65535;
            XCellRange targetRange = (XCellRange) UnoRuntime.queryInterface(XCellRange.class,getActiveSpreadSheet());
            for (int i=255; i<65535; i=i+64) {
                try {
                    targetRange.getCellByPosition(i, 0);
                }
                catch (com.sun.star.lang.IndexOutOfBoundsException e) {
                    maxSheetColumns = i-64;
                    System.err.println("Maximum columns used "+maxSheetColumns);
                    break;
                }
            }
        }
        return maxSheetColumns;
    }

     public int getMaxSheetRows() {
        if (maxSheetRows == null) { //TODO find a more elegant solution for this
            maxSheetRows = 1048576;
            XCellRange targetRange = (XCellRange) UnoRuntime.queryInterface(XCellRange.class,getActiveSpreadSheet());
            for (int i=65535; i<1048576; i=i+1024) {
                try {
                    targetRange.getCellByPosition(0,i);
                }
                catch (com.sun.star.lang.IndexOutOfBoundsException e) {
                    maxSheetRows = i-1024;
                    System.err.println("Maximum rows used "+maxSheetRows);
                    break;
                }
            }
        }
        return maxSheetRows;
    }

    public IDatabase getDatabase(String servdb) {
        IDatabase result = databaseLookUp.get(servdb);
        if (result == null) {
            result = connectionHandler.getDatabase(servdb);
            if (result != null ) {
                databaseLookUp.put(servdb, result);
            }
        }
        return result;
    }

    public IDimension getDimension(String servdb, String dimensionName) {
        IDimension result = dimensionLookUp.get(concatName(servdb,dimensionName));
        if (result == null) {
            IDatabase database = getDatabase(servdb);
            if (database != null) {
                result = database.getDimensionByName(dimensionName);
                if (result != null) {
                    dimensionLookUp.put(concatName(servdb,dimensionName), result);
                }
            }
        }
        return result;
    }

    public String getImportFilePath() {
        return importFilePath;
    }

    public void setImportFilePath(String importFilePath) {
        this.importFilePath = importFilePath;
    }

    public int getFunctionType() {
        return functionType;
    }

    public void setFunctionType(int functionType) {
        this.functionType = functionType;
    }

    public boolean isHideEmptyData() {
        return hideEmptyData;
    }

    public void setHideEmptyData(boolean hideEmptyData) {
        this.hideEmptyData = hideEmptyData;
    }

     public boolean isCountDistinct() {
        return countDistinct;
    }

    public void setCountDistinct(boolean countDistinct) {
        this.countDistinct = countDistinct;
    }

    public boolean isShowElementOnDblClk() {
        return showElementOnDblClk;
    }

    public void setShowElementOnDblClk(boolean showElementOnDblClk) {
        this.showElementOnDblClk = showElementOnDblClk;
    }

    public boolean isColumnLineBreak() {
        return columnLineBreak;
    }

    public void setColumnLineBreak(boolean columnLineBreak) {
        this.columnLineBreak = columnLineBreak;
    }

    public boolean isRowInset() {
        return rowInset;
    }

    public void setRowInset(boolean rowInset) {
        this.rowInset = rowInset;
    }

    public boolean isColumnCustomSize() {
        return columnCustomSize;
    }

    public void setColumnCustomSize(boolean columnCustomSize) {
        this.columnCustomSize = columnCustomSize;
    }

    public double getColumnWidth() {
        return columnWidth;
    }

    public void setColumnWidth(double columnWidth) {
        this.columnWidth = columnWidth;
    }

    public static PalOOCaManager getInstance(XComponentContext context) {
        if (instance == null || instance.context != context) {//I assume that it will not happen to be called from another context again ...
            instance = new PalOOCaManager(context);
            instance.initListeners();
        }
        return instance;
    }
    
    /**
     * Checks if the user interface can be or has been initialized.
     * @return <code>true</code>, if the UI is ready to be used and <code>false</code>, if not.
     */
    public static boolean UIEnabled() {
        if (javaVersionState == VERSION_UNCHECKED) {
            try {
                try {
                    //Since only the Windows Look & Feel has been tested successfully,
                    //we only switch to the system default on these platforms.
                    if (System.getProperty("os.name").contains("Windows")) {
                        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                    } else {
                        UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
                    }
                } catch (UnsupportedLookAndFeelException e) {
                    UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
                }
            } catch ( Exception e ) {
                
            }
            
            ResourceBundle resourceBundle = (instance == null ? ResourceBundle.getBundle("org/palooca/PalOOCaCore") :
                instance.coreResourceBundle);
            
            javaVersionState = VERSION_OK;

            String javaVersion = System.getProperty("java.version");
            String[] versionParts = javaVersion.split("\\.");

            try {
                Integer[] version = new Integer[2];
                version[0] = Integer.parseInt(versionParts[0]);
                version[1] = Integer.parseInt(versionParts[1]);

                if (version[0] < 1 || (version[0] == 1 && version[1] < 6))
                    javaVersionState = VERSION_INVALID;
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, e.toString(), resourceBundle.getString("Java_Version_Parser_Error_Caption"), JOptionPane.ERROR_MESSAGE);
                javaVersionState = VERSION_INVALID;
            }

            if (javaVersionState != VERSION_OK) {
                JOptionPane.showMessageDialog(null, resourceBundle.getString("Java_Version_Error_Message"), resourceBundle.getString("Java_Version_Invalid"), JOptionPane.WARNING_MESSAGE);
            }
                
        }
        return javaVersionState == VERSION_OK;
    }
    
    private XComponentContext context;
    private XMultiComponentFactory multiComponentFactory;
    private BaseConfig baseConfig;
    private ConnectionHandler connectionHandler;
    private XFunctionAccess functionAccess;
    private com.sun.star.lang.Locale officeLocale = new com.sun.star.lang.Locale(); //empty = default
    private Locale locale;
    private AboutDialog aboutDialog = null; //create on request
//    private ConnectionManagerDialog connectionManagerDialog = null; //create on request
    private ResourceBundle coreResourceBundle;
    private String decimalSeparator = ",";
    private String thousandSeparator = ".";
    
    /** Creates a new instance of PalOOCa */
    private PalOOCaManager(XComponentContext context) {
        this.context = context;
        this.multiComponentFactory = context.getServiceManager();
        
        try {
            XMultiComponentFactory xMCF = context.getServiceManager();
            Object oFunctionAccess = xMCF.createInstanceWithContext("com.sun.star.sheet.FunctionAccess", context);
            functionAccess = (XFunctionAccess) UnoRuntime.queryInterface(XFunctionAccess.class, oFunctionAccess);
            
            XMultiServiceFactory xConfigurationProvider = (XMultiServiceFactory) UnoRuntime.queryInterface(XMultiServiceFactory.class, xMCF.createInstanceWithContext("com.sun.star.configuration.ConfigurationProvider", context));
            PropertyValue[] params = new PropertyValue[1];
            params[0] = new PropertyValue();
            params[0].Name = "nodepath";
            params[0].Value = "org.openoffice.Setup/L10N";
            Object oPropertyAccess = xConfigurationProvider.createInstanceWithArguments("com.sun.star.configuration.ConfigurationAccess", params);
            XNameAccess xPropertyAccess = (XNameAccess) UnoRuntime.queryInterface(XNameAccess.class, oPropertyAccess);
            String ooLocale = (String) xPropertyAccess.getByName("ooLocale");
            if (ooLocale.length() >= 5) {
                locale = new Locale(ooLocale.substring(0, 2), ooLocale.substring(3, 5));
                officeLocale.Country = ooLocale.substring(0, 2);
                officeLocale.Language = ooLocale.substring(3, 5);
            } else {
                locale = new Locale(ooLocale.substring(0, 2));
                officeLocale.Country = ooLocale.substring(0, 2).toUpperCase();
                officeLocale.Language = ooLocale.substring(0, 2);
            }

            officeLocale = getOOoLocale(context);

            Object oLocaleData = multiComponentFactory.createInstanceWithContext("com.sun.star.i18n.LocaleData", context);
            XLocaleData xLocaleData = (XLocaleData) UnoRuntime.queryInterface(XLocaleData.class, oLocaleData);

            decimalSeparator = xLocaleData.getLocaleItem(officeLocale).decimalSeparator;
            thousandSeparator = xLocaleData.getLocaleItem(officeLocale).thousandSeparator;

        } catch (Exception e) {
            functionAccess = null;
            locale = null;
        }
        
        coreResourceBundle = getResourceBundle("org/palooca/PalOOCaCore");
        baseConfig = BaseConfig.getInstance(context);
        connectionHandler = ConnectionHandler.getInstance(context, baseConfig,
                coreResourceBundle);

        //Thread thread = new KeepAlive();
        //thread.start();
//        connectionHandler.autoConnect();
    }

    private com.sun.star.lang.Locale getOOoLocale( XComponentContext xContext ) {
        com.sun.star.lang.Locale aLocale = new  com.sun.star.lang.Locale();
        try {
            Object oConfigurationProvider =
                    xContext.getServiceManager().createInstanceWithContext(
                    "com.sun.star.configuration.ConfigurationProvider", xContext);
            XMultiServiceFactory xConfigurationProvider =
                    (XMultiServiceFactory) UnoRuntime.queryInterface(
                    XMultiServiceFactory.class,
                    oConfigurationProvider);

            PropertyValue aPropValue = new PropertyValue();
            aPropValue.Name = "nodepath";
            aPropValue.Value = "/org.openoffice.Setup/L10N";

            XNameAccess xNameAccess = (XNameAccess) UnoRuntime.queryInterface(
                    XNameAccess.class, xConfigurationProvider.createInstanceWithArguments(
                    "com.sun.star.configuration.ConfigurationAccess",
                    new PropertyValue[]{aPropValue}));

            String sLocale = AnyConverter.toString(
                    xNameAccess.getByName("ooLocale"));

            if ( sLocale.length() > 0) {
                if (sLocale.contains("-")){
                    String[] sLocaleInfo = sLocale.split("-");
                    aLocale.Language = sLocaleInfo[0];
                    if (sLocaleInfo.length > 1) {
                        aLocale.Country = sLocaleInfo[1];
                    }
                } else {
                    aLocale.Language = sLocale;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            return aLocale;
        }
    }

    public BaseConfig getBaseConfig() {
        return baseConfig;
    }
    
    public ConnectionHandler getConnectionHandler() {
        return connectionHandler;
    }
    
    public AboutDialog getAboutDialog() {
        if (aboutDialog == null) {
            aboutDialog = AboutDialog.getInstance();
            aboutDialog.setLocationRelativeTo(null);
        }
        return aboutDialog;
    }

    public boolean isHideIdsOnAttributeUse() {
        return hideIdsOnAttributeUse;
    }

    public void setHideIdsOnAttributeUse(boolean hideIdsOnAttributeUse) {
        this.hideIdsOnAttributeUse = hideIdsOnAttributeUse;
    }
    

    // <editor-fold defaultstate="collapsed" desc="Listeners">
    // <editor-fold defaultstate="collapsed" desc="com.sun.star.document.XEventListener">
    @Override
    public void notifyEvent(com.sun.star.document.EventObject e) {
        if (e.EventName.equals("OnFocus") || e.EventName.equals("OnNew") || e.EventName.equals("OnLoad") || e.EventName.equals("OnViewCreated")) {
            XSpreadsheetDocument doc = (XSpreadsheetDocument) UnoRuntime.queryInterface(XSpreadsheetDocument.class, e.Source);
            if (doc != null) {
                setActiveDocument(doc);
                if (e.EventName.equals("OnLoad")) {
                    PalOOCaView view = PalOOCaView.parseSpreadsheet(context, getActiveSpreadSheet(),false);
                    if (view != null) {
                        view.generate(getActiveSpreadSheet());
                    }
                }
            }
        }
        /* Done now in view#generate
        if (e.EventName.startsWith("OnMode")) {
            XComponent component = getCurrentComponent();
            XCalculatable calc = (XCalculatable) UnoRuntime.queryInterface(XCalculatable.class, component);
            if (calc != null) calc.enableAutomaticCalculation(false);
        }
         *
         */
    }

     @Override
    public void activeSpreadsheetChanged(ActivationEvent e) {
       lastActiveSheet = e.ActiveSheet;
    }

    @Override
    public void disposing(EventObject e) {
        setActiveDocument(null);
    }
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc="com.sun.star.awt.XMouseClickHandler">
    @Override
    public boolean mousePressed(MouseEvent e) {
        RunnableModal modal = new RunnableModal(e);
        OSUtil.invokeRunnable(modal);
        return ((e.ClickCount == 2 && activeCell != null && activeCell.getError() == 0 && getData(activeCell, "IsPaloView", "").equals("True")) || (e.ClickCount == 2 && activeCell != null && activeCell.getError() == 0 && formula != null && formula.size() > 0));
    }

    private boolean showElementSelection(PalOOCaView view, String elementPath)
    { 
        //First we check if it is a column element

        for (int i = 0; i < view.getColumnDimensions().size(); i++) {
            String[] stringPath = PaloDialogUtilities.stringPathToStringArray(elementPath);
            DimensionSubsetListItem dimItem = view.getColumnDimensions().get(i);
            String[] firstPath = dimItem.getSelectedElements().get(0);
            if (Arrays.equals(firstPath,stringPath )) {
                if (getSelectedColumnIndex() == view.getRowDimensions().size()) {      // only show dialog for first element in column
                    ElementBrowserDialog browser = new ElementBrowserDialog(null, false, context,
                           dimItem.getDimension(),
                           dimItem.getSelectedElements(), null);
                    browser.getOKButton().addActionListener(new MatrixElementActionListener(browser, view, dimItem));
                    showModal(browser);
                    return true;
                }
            }
        }

        for (int i = 0; i < view.getRowDimensions().size(); i++) {
            String[] stringPath = PaloDialogUtilities.stringPathToStringArray(elementPath);
            DimensionSubsetListItem dimItem = view.getRowDimensions().get(i);
            String[] firstPath = dimItem.getSelectedElements().get(0);
            if (Arrays.equals(firstPath,stringPath )) {
                if (getSelectedRowIndex() == view.getFilterDimensions().size() + view.getColumnDimensions().size() + 3) {      // only show dialog for first element in column
                    ElementBrowserDialog browser = new ElementBrowserDialog(null, false, context,
                           dimItem.getDimension(),
                           dimItem.getSelectedElements(), null);
                    showModal(browser);
                    browser.getOKButton().addActionListener(new MatrixElementActionListener(browser, view, dimItem));
                    return true;
                }
            }
        }

        return false;
    }

    public boolean mouseReleased(MouseEvent e) {
        return false;
    }
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc="com.sun.star.view.XSelectionChangeListener">
    public void selectionChanged(EventObject e) {
        XCell cell = null;
        XSelectionSupplier selectionSupplier = (XSelectionSupplier) UnoRuntime.queryInterface(XSelectionSupplier.class, e.Source);
        if (selectionSupplier != null) {
            cell = (XCell) UnoRuntime.queryInterface(XCell.class, selectionSupplier.getSelection());
        }
        setActiveCell(cell);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="com.sun.star.util.XModifyListener">
    public void modified(EventObject evt) {
        CellContentType contentType = activeCell.getType();
        XCell cell = getActiveCell();
        String content = cell.getFormula();
        boolean success = false;

        if (content.length() != 0 && contentType != CellContentType.FORMULA) {
            try {
                ArrayList<Token> arguments = formula.get(0);
                if (arguments.size() > 2) {
                    XSpreadsheets sheets = activeDocument.getSheets();
                    XSpreadsheet sheet = getActiveSpreadSheet();
                    String servdb = arguments.get(0).calculate(functionAccess, sheets, sheet).toString();
                    String cubeName = arguments.get(1).calculate(functionAccess, sheets, sheet).toString();
                    Object[] coords = new Object[arguments.size() - 2];
                    for (int i = 2; i < arguments.size(); i++)
                        coords[i - 2] = arguments.get(i).calculate(functionAccess, sheets, sheet);
                    String[] stringCoords = PalOOCaImpl.processCoordinates(coords);
                    
                    IDatabase database = connectionHandler.getDatabase(servdb);
                    ICube cube = null;
                    if (database != null)
                        cube = database.getCubeByName(cubeName);

                    if (cube != null) {
                        boolean splash = false;
                        String newContent = activeCell.getFormula();
                        if (newContent.length() != 0) {
                            if (newContent.charAt(0) == '#' || newContent.charAt(0) == '!' ) {
                                splash = true;
                            }
                        }

                        boolean isString = false;
                        boolean isConsolidated = false;
                        IDimension[] dims = cube.getDimensions();
                        IElement[] elems = new IElement[dims.length];
                        for (int i = 0; i < dims.length; i++) {
                            elems[i] = dims[i].getElementByName(stringCoords[i],false);
                            if (elems[i].getType() == IElement.ElementType.ELEMENT_STRING)
                                isString = true;
//                            if (elems[i].getType() == Element.ELEMENTTYPE_CONSOLIDATED)
//                                isConsolidated = true;
                        }
                        
                        if (splash) {
                            PaloLibUtil.setInput(cube, elems, newContent, thousandSeparator.charAt(0),decimalSeparator.charAt(0));
                        } else {
                            PaloLibUtil.setInput(cube, elems, newContent, ',', '.');
                        }

//                        if (isString) {
//                            cube.setData(elems, newContent);
//                        } else {
//                            if (splash) {
//                                cube.setDataSplashed(elems, newContent);
//                            } else {
//                                if (isConsolidated) {
//                                    JOptionPane.showMessageDialog(null, coreResourceBundle.getString("Cumulated_Cell_Write"), coreResourceBundle.getString("Write_Error"), JOptionPane.WARNING_MESSAGE);
//                                } else {
//                                    cube.setData(elems, newContent);
//                                }
//                            }
//                        }
                        success = true;
                    } else {
                        RunnableWarning modal = new RunnableWarning(ResourceBundle.getBundle("org/palooca/dialogs/PalOOCaDialogs").getString("WriteImpossible"),coreResourceBundle.getString("Write_Error"));
                        OSUtil.invokeUI(modal);
                    }
                }
            } catch (PaloException pe) {
                 RunnableWarning modal = new RunnableWarning(pe.getLocalizedMessage(),coreResourceBundle.getString("Write_Error"));
                 OSUtil.invokeUI(modal);
                //JOptionPane.showMessageDialog(null, pe.getLocalizedMessage());
            } catch (PaloJException e) {
                if (e.toString().contains("splash disabled")) {
                    RunnableWarning modal = new RunnableWarning(coreResourceBundle.getString("Cumulated_Cell_Write"),coreResourceBundle.getString("Write_Error"));
                    OSUtil.invokeUI(modal);
                    //JOptionPane.showMessageDialog(null, coreResourceBundle.getString("Cumulated_Cell_Write"), coreResourceBundle.getString("Write_Error"), JOptionPane.WARNING_MESSAGE);
                } else {
                    RunnableWarning modal = new RunnableWarning(e.getLocalizedMessage(),coreResourceBundle.getString("Write_Error"));
                    OSUtil.invokeUI(modal);
                    //JOptionPane.showMessageDialog(null, e.getLocalizedMessage());
                }
            } catch (Throwable t) {
                System.err.println("Exception caught: " + t.getMessage());
                t.printStackTrace();
            }
             cell.setFormula(oldContent);
            if (success) {
                 PalOOCaView view = PalOOCaView.parseSpreadsheet(context, getActiveSpreadSheet(),false);
                 if (view != null) {
                       view.generate(getActiveSpreadSheet(), false, false);
                 } else {
                    recalculateDocument();
                 }
            } else { //recalculate old value based on formula
                  XCalculatable calc = (XCalculatable) UnoRuntime.queryInterface(XCalculatable.class,getCurrentComponent());
                  if (calc != null) calc.calculate();
            }

            //cell.setFormula(oldContent);
        } else {
            oldContent = activeCell.getFormula();
        }
    }
    // </editor-fold>
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc="Listener handling">
    /**
     * Activates the global listeners to keep track of active spreadsheet document changes.
     * @see #setActiveDocument(com.sun.star.sheet.XSpreadsheetDocument)
     */
    public void initListeners() {
        try {
            Object oGlobalEventBroadcaster = multiComponentFactory.createInstanceWithContext("com.sun.star.frame.GlobalEventBroadcaster", context);
            XEventBroadcaster broadcaster = (XEventBroadcaster) UnoRuntime.queryInterface(XEventBroadcaster.class, oGlobalEventBroadcaster);
            if (broadcaster != null) {
                broadcaster.addEventListener(this);
            }
           
            
            //add window listener
            /*
            Object oDesktop = multiComponentFactory.createInstanceWithContext("com.sun.star.frame.Desktop", context);
            XDesktop xDesktop = (XDesktop) UnoRuntime.queryInterface(XDesktop.class, oDesktop);
            xDesktop.getCurrentFrame().getContainerWindow().addWindowListener(this);
             */
            
            
            //check if there is already a spreadsheet available
            XComponent currentComponent = getCurrentComponent();

            if (currentComponent != null)
                setActiveDocument((XSpreadsheetDocument) UnoRuntime.queryInterface(XSpreadsheetDocument.class, currentComponent));
        } catch (com.sun.star.uno.Exception ex) {
            ex.printStackTrace();
        }
    }
    
    private XSpreadsheetDocument activeDocument = null;
    private XSelectionSupplier selectionSupplier = null;
    private XSpreadsheetView spreadsheetView = null;
    
    /**
     * Helper function to get the currently active (tracked) spreadsheet document.
     * @return The active spreadsheet document.
     */
    public XSpreadsheetDocument getActiveDocument() {
        return activeDocument;
    }
    
    /**
     * Helper function to get the currently active (tracked) spreadsheet view.
     * @return The active spreadsheet view.
     */
    public XSpreadsheetView getActiveView() {
        return spreadsheetView;
    }

    public XSpreadsheet getActiveSpreadSheet() {
        
        if (spreadsheetView != null && spreadsheetView.getActiveSheet() != null) {
            lastActiveSheet = spreadsheetView.getActiveSheet();
            return lastActiveSheet;
        }
        /*
        if (spreadsheetView != null && spreadsheetView.getActiveSheet() == null && lastActiveSheet != null) {
            XIndexAccess xSheetIndexAccess = (XIndexAccess)UnoRuntime.queryInterface(XIndexAccess.class, getActiveDocument().getSheets());
            try {
                spreadsheetView.setActiveSheet(lastActiveSheet);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            return lastActiveSheet;
        }
        return null;
         *
         */
        //set by listener activeSpreadsheetChanged
        return lastActiveSheet;
    }
    
    /**
     * Helper function to set the currently active (tracked) spreadsheet document. It also removes
     * active listeners from the former active spreadsheet document and adds them to the new one.
     * These listeners keep track of cell changes and mouse events.
     * @param document The new active spreadsheet document.
     */
    public void setActiveDocument(XSpreadsheetDocument document) {
            /*
            if (selectionSupplier != null) {
                selectionSupplier.removeSelectionChangeListener(this);
                XUserInputInterception inputInterception = (XUserInputInterception) UnoRuntime.queryInterface(XUserInputInterception.class, selectionSupplier);
                inputInterception.removeMouseClickHandler(this);
                setActiveCell(null);
                spreadsheetView = null;
                selectionSupplier = null;
            }
             *
             */
            
            if (document != null) {
                XModel model = (XModel) UnoRuntime.queryInterface(XModel.class, document);
                selectionSupplier = (XSelectionSupplier) UnoRuntime.queryInterface(XSelectionSupplier.class, model.getCurrentController());
                if (selectionSupplier != null) {
                    spreadsheetView = (XSpreadsheetView) UnoRuntime.queryInterface(XSpreadsheetView.class, selectionSupplier);
                    selectionSupplier.addSelectionChangeListener(this);
                    XUserInputInterception inputInterception = (XUserInputInterception) UnoRuntime.queryInterface(XUserInputInterception.class, selectionSupplier);
                    inputInterception.removeMouseClickHandler(this);
                    inputInterception.addMouseClickHandler(this);
                    setActiveCell((XCell) UnoRuntime.queryInterface(XCell.class, selectionSupplier.getSelection()));
                    XComponent xComponent = getCurrentComponent();
                    XModel xModel = (XModel) UnoRuntime.queryInterface(XModel.class, xComponent);
                    XController xController = xModel.getCurrentController();
                    XSpreadsheetView view = (XSpreadsheetView)UnoRuntime.queryInterface(XSpreadsheetView.class, xController );
                    XActivationBroadcaster activationBroadcaster = (XActivationBroadcaster) UnoRuntime.queryInterface(XActivationBroadcaster.class, view);
                    if (activationBroadcaster != null) {
                        activationBroadcaster.removeActivationEventListener(this);
                        activationBroadcaster.addActivationEventListener(this);
                    }
                }
                activeDocument = document;
            }
    }
    
    private XCell activeCell = null;
    private Token formula = null;
    private String oldContent = null;
    
    /**
     * Helper function to query the currently (tracked) active cell.
     * @return The active cell.
     */
    public XCell getActiveCell() {
        return activeCell;
    }
    
    /**
     * Helper function to set the (tracked) active cell. It also frees up active listeners
     * of the former active cell and sets these listeners on the new one.
     * The listeners keep track of content changes for cells, which use PALO.DATA.
     * @param cell The (now) active cell.
     */
    public void setActiveCell(XCell cell) {
        if (activeCell != cell) {
            if (activeCell != null) {
                formula = null;
                if (oldContent != null) {
                    oldContent = null;
                    removeModify(activeCell);
                }
            }

            if (cell != null) {
                formula = FormulaParser.parseFormula(cell.getFormula());
                if (formula.size() > 0) {
                    Token token = formula.get(0);
                    if (token instanceof FunctionToken && token.getContent().indexOf("org.palooca.PalOOCa.PALO_DATA") == 0) {
                        oldContent = cell.getFormula();
                        addModify(cell);
                    }
                }
            }

            activeCell = cell;
        }
    }
    // </editor-fold>

    private void removeModify(XCell cell) {
        XModifyBroadcaster modifyBroadcaster = (XModifyBroadcaster) UnoRuntime.queryInterface(XModifyBroadcaster.class, cell);
        if (modifyBroadcaster != null) {
            modifyBroadcaster.removeModifyListener(this);
        }
    }

    private void addModify(XCell cell) {
        XModifyBroadcaster modifyBroadcaster = (XModifyBroadcaster) UnoRuntime.queryInterface(XModifyBroadcaster.class, cell);
        if (modifyBroadcaster != null) {
            modifyBroadcaster.addModifyListener(this);
        }
    }

    // <editor-fold defaultstate="collapsed" desc="Helper functions">
    /**
     * Retrieves the currently active OpenOffice.org Component (sheet, text, etc.)
     * @return The currently active OOo Component.
     */
    public XComponent getCurrentComponent() {
        try {
            Object oDesktop = multiComponentFactory.createInstanceWithContext("com.sun.star.frame.Desktop", context);
            XDesktop xDesktop = (XDesktop) UnoRuntime.queryInterface(XDesktop.class, oDesktop);
            return xDesktop.getCurrentComponent();
        } catch (Exception e) {
            return null;
        }
    }
    
    public Rectangle getWorkspaceDimensions() {
        try {
            XComponent xComponent = getCurrentComponent();
            XModel xModel = (XModel) UnoRuntime.queryInterface(XModel.class, xComponent);
            XController xController = xModel.getCurrentController();
            XFrame xFrame = xController.getFrame();
            return xFrame.getComponentWindow().getPosSize();
        } catch (Exception e) {
            return null;
        }
    }
    
    public XSpreadsheetDocument createNewSheetComponent() {
        try {
            Object oDesktop = multiComponentFactory.createInstanceWithContext("com.sun.star.frame.Desktop", context);
            XComponentLoader xComponentLoader = (XComponentLoader) UnoRuntime.queryInterface(XComponentLoader.class, oDesktop);
            PropertyValue[] args = new PropertyValue[0];
            XComponent xComponent = xComponentLoader.loadComponentFromURL("private:factory/scalc", "_blank", 0, args);
            return (XSpreadsheetDocument) UnoRuntime.queryInterface(XSpreadsheetDocument.class, xComponent);
        } catch (Exception e) {
            return null;
        }
    }
    
    /** Method for completing selection commands with UNO, as copy, paste, undo, redo, etc.
     * @param operation String that define the UNO Operation, such as: ".uno:Undo", ".uno:Redo", ".uno:Paste", ".uno:Copy"
     */

    protected void unoOperation(String operation) throws
                java.lang.Exception {

        XComponent xComponent = getCurrentComponent();
        XModel xModel = (XModel) UnoRuntime.queryInterface(XModel.class, xComponent);
        XController xController = xModel.getCurrentController();
        XDispatchProvider xDispProv = (XDispatchProvider)UnoRuntime.queryInterface(XDispatchProvider.class, xController);
        XURLTransformer xURLTransformer = queryForUrlTransformer();

        // Because it's an in/out parameter
        // we must use an array of URL objects.
        URL[] aParseURL = new URL[1];
        aParseURL[0] = new URL();
        aParseURL[0].Complete = operation;
        xURLTransformer.parseStrict(aParseURL);
        URL aURL = aParseURL[0];

        XDispatch xDispatcher = xDispProv.queryDispatch( aURL, "", 0);

        if( xDispatcher != null ) {
            //xDispatcher.dispatch( aURL, null );
            xDispatcher.dispatch( aURL, new PropertyValue[0] );
        }
    }

    public XURLTransformer queryForUrlTransformer() {
        XURLTransformer xURLTransformer = null;
        try {
            Object transformer = multiComponentFactory.createInstanceWithContext(
                                "com.sun.star.util.URLTransformer", context);
            xURLTransformer = (com.sun.star.util.XURLTransformer)
            UnoRuntime.queryInterface( com.sun.star.util.XURLTransformer.class,  transformer);

        } catch (com.sun.star.uno.Exception e) {
            System.err.println("Errore: Impossibile ottenere l'oggetto XURLTransformer.");
            System.exit(1);
        }
        return (xURLTransformer);
    }


    
    /**
     * For debug purposes: prints all implemented interfaces of a given object.
     * @param object The object to query. (Should implement <code>XTypeProvider</code>)
     */
    public static void printInterfaces(Object object) {
        XTypeProvider typeProvider = (XTypeProvider) UnoRuntime.queryInterface(XTypeProvider.class, object);
        if (typeProvider != null)
        {
            String typesStr = "";
            Type[] types = typeProvider.getTypes();
            for (int i = 0; i < types.length; i++)
                typesStr = typesStr + types[i].getTypeName() + '\n';
            //JOptionPane.showMessageDialog(null, typesStr);
            System.out.println(typesStr);
        } else {
            JOptionPane.showMessageDialog(null, object.toString());
        }
    }

     /**
     * This method is a workaround for the problem, that Java windows can't be shown
     * with OOo as parent. Therefore it creates an OOo internal dialog which cannot be
     * closed and is as small as possible to get OOo into the state of being behind
     * a modal form.
     * @param dialog The dialog to be shown modal.
    
    public void showModal(final javax.swing.JDialog dialog) {
        try {

            Object tempDialogModel = multiComponentFactory.createInstanceWithContext("com.sun.star.awt.UnoControlDialogModel", context);
            XPropertySet properties = (XPropertySet) UnoRuntime.queryInterface(XPropertySet.class, tempDialogModel);
            properties.setPropertyValue("PositionX", new Integer(0));
            properties.setPropertyValue("PositionY", new Integer(0));
            properties.setPropertyValue("Width", new Integer(1));
            properties.setPropertyValue("Height", new Integer(1));
            properties.setPropertyValue("Closeable", Boolean.FALSE);

            final Object tempDialog = multiComponentFactory.createInstanceWithContext("com.sun.star.awt.UnoControlDialog", context);
            XControl xControl = (XControl) UnoRuntime.queryInterface(XControl.class, tempDialog);
            XControlModel xControlModel = (XControlModel) UnoRuntime.queryInterface(XControlModel.class, tempDialogModel);
            xControl.setModel(xControlModel);

            Object toolkit = multiComponentFactory.createInstanceWithContext("com.sun.star.awt.Toolkit", context);
            XToolkit xToolkit = (XToolkit) UnoRuntime.queryInterface(XToolkit.class, toolkit);
            final XWindow xWindow = (XWindow) UnoRuntime.queryInterface(XWindow.class, xControl);
            xWindow.setVisible(false);
            xControl.createPeer(xToolkit, null);

            final XDialog xTempDialog = (XDialog) UnoRuntime.queryInterface(XDialog.class, tempDialog);


            dialog.addWindowListener(new WindowAdapter() {

                @Override
                public void windowClosed(WindowEvent e) {
                    xTempDialog.endExecute();
                    dialog.removeWindowListener(this);
                }
            });


            //It worked as expected on Linux, but crashed on Windows ...
            //a small visible window is less disturbing than a crash


            dialog.setAlwaysOnTop(true);
            dialog.setVisible(true);

            xTempDialog.execute();
            XComponent component = (XComponent) UnoRuntime.queryInterface(XComponent.class, tempDialog);
            component.dispose();
        } catch (com.sun.star.uno.Exception ex) {
            ex.printStackTrace();
        }
    }
 */
    
    
    public void showModal(final javax.swing.JDialog dialog) {
        dialog.setAlwaysOnTop(true);
        dialog.setVisible(true);
    }
    
    public XCellRange getSelectedRange() {
        if (selectionSupplier != null) {
            Object selection = selectionSupplier.getSelection();
            XCellRange cellRange = (XCellRange) UnoRuntime.queryInterface(XCellRange.class, selection);
            return cellRange;
        } else {
            return null;
        }
    }

    public int getSelectedColumnIndex()
    {
        XCellRangeAddressable cellRangeAddressable = (XCellRangeAddressable) UnoRuntime.queryInterface(XCellRangeAddressable.class, getSelectedRange());
        CellRangeAddress address = cellRangeAddressable.getRangeAddress();
        return address.StartColumn;
    }

    public int getSelectedRowIndex()
    {
        XCellRangeAddressable cellRangeAddressable = (XCellRangeAddressable) UnoRuntime.queryInterface(XCellRangeAddressable.class, getSelectedRange());
        CellRangeAddress address = cellRangeAddressable.getRangeAddress();
        return address.StartRow;
    }

    public XCellRange getRange(int col, int row, int endCol, int endRow) {
        try {
            if (spreadsheetView != null) {
                XSheetCellRange sheetCellRange = (XSheetCellRange) UnoRuntime.queryInterface(XSheetCellRange.class, getActiveSpreadSheet());
                if (sheetCellRange != null) {
                    return sheetCellRange.getCellRangeByPosition(col, row, endCol, endRow);
                }
            }
        } catch (Exception e) {}
        
        return null;
    }

    private static String getSheetColumnName(int columnNumber) {
        int dividend = columnNumber;
        String columnName = "";
        int modulo;

        while (dividend > 0)
        {
            modulo = (dividend - 1) % 26;
            columnName = (char)(65 + modulo) + columnName;
            dividend = (int)((dividend - modulo) / 26);
        }

        return columnName;

    }
    
    public static String getCellIdentifier(int col, int row, boolean fixCol, boolean fixRow) {
        StringBuilder result = new StringBuilder();
        
        if (fixCol)
            result.append('$');
/*
        
        if (col >= 26)
            result.append((char)('A' + col / 26 - 1));
        result.append((char)('A' + col % 26));
*/
        result.append(getSheetColumnName(col+1));
        if (fixRow)
            result.append('$');
        
        result.append(row + 1);
        
        return result.toString();
    }
    
    /**
     * Iterates through all dimensions of the cube and returns that one, which contains a
     * certain element.
     * @param cube A reference to the cube.
     * @param elementName The name of the element to search for.
     * @return The dimension containing the searched element. <code>null</code> if none was found.
     */
    public static IDimension findDimensionByElementName(ICube cube, String elementName) {
        IDimension[] dimensions = cube.getDimensions();
        for (int i = 0; i < dimensions.length; i++) {
            if (dimensions[i].getElementByName(elementName,false) != null)
                return dimensions[i];
        }
        
        return null;
    }
    
    /**
     * Queries all existing cell styles and returns them as XNameContainer.
     * @return The container holding all cell styles.
     */
    public XNameContainer getCellStyles() {
        if (activeDocument != null) {
            try {
                XStyleFamiliesSupplier styleFamiliesSupplier = (XStyleFamiliesSupplier) UnoRuntime.queryInterface(XStyleFamiliesSupplier.class, activeDocument);
                XNameAccess styleFamilies = styleFamiliesSupplier.getStyleFamilies();
                return (XNameContainer) UnoRuntime.queryInterface(XNameContainer.class, styleFamilies.getByName("CellStyles"));
            } catch (NoSuchElementException ex) {

            } catch (WrappedTargetException ex) {

            }
        }
        
        return null;
    }
    
    /**
     * Initializes a new cell style and adds it to the documents styles.
     * @param styleFamily The style family to hold the new cell style.
     * @param name The name of the style.
     * @return The style information as XPropertySet.
     */
    public XPropertySet createCellStyle(XNameContainer styleFamily, String name) {
        try {
            XMultiServiceFactory serviceFactory = (XMultiServiceFactory) UnoRuntime.queryInterface(XMultiServiceFactory.class, activeDocument);
            Object cellStyle = serviceFactory.createInstance("com.sun.star.style.CellStyle");
            styleFamily.insertByName(name, cellStyle);
            return (XPropertySet) UnoRuntime.queryInterface(XPropertySet.class, cellStyle);
        } catch (com.sun.star.uno.Exception ex) {

        }
        return null;
    }
    
    /**
     * Sets the style of a given cell.
     * @param cell The reference to the target cell.
     * @param styleName The name of the style to use for the given cell.
     */
    public void setCellStyle(Object cell, String styleName) {
        try {
            XPropertySet propertySet = (XPropertySet) UnoRuntime.queryInterface(XPropertySet.class, cell);
            if (propertySet != null) {
                propertySet.setPropertyValue("CellStyle", styleName);
            }
        } catch (WrappedTargetException ex) {
            ex.printStackTrace();
        } catch (PropertyVetoException ex) {
            ex.printStackTrace();
        } catch (com.sun.star.lang.IllegalArgumentException ex) {
            ex.printStackTrace();
        } catch (UnknownPropertyException ex) {
            ex.printStackTrace();
        }
    }
    
    /**
     * Processes a Form or Formcomponent to iterate through their children and update ValueBindings and ListEntrySinks.
     * @param form The Form or FormComponent.
     */
    private void processForm(Object form) {
        try {
            //Child forms
            XIndexContainer forms = (XIndexContainer) UnoRuntime.queryInterface(XIndexContainer.class, form);
            if (forms != null)
                for (int i = 0; i < forms.getCount(); i++)
                    processForm(forms.getByIndex(i));
            
            //Values
            XBindableValue bindableValue = (XBindableValue) UnoRuntime.queryInterface(XBindableValue.class, form);
            if (bindableValue != null)
                bindableValue.setValueBinding(bindableValue.getValueBinding());
            
            //List entries
            XListEntrySink listEntrySink = (XListEntrySink) UnoRuntime.queryInterface(XListEntrySink.class, form);
            if (listEntrySink != null)
                listEntrySink.setListEntrySource(listEntrySink.getListEntrySource());
        } catch (Exception e) {}
    }

    /**
     * Recalculates all components and cells within the document. Due to a bug in some OOo versions,
     * form components are not automatically updated to cell changes which is done as a workaround
     * in this method too.
     */
    public void recalculateDocument() {
        this.recalculateDocument(false);
    }

    /**
     * Recalculates all or all dirty components and cells within the document. Due to a bug in some OOo versions,
     * form components are not automatically updated to cell changes which is done as a workaround
     * in this method too.
     */
    public void recalculateDocument(boolean dirtyCellsOnly) {
        XComponent component = getCurrentComponent();
        XCalculatable calc = (XCalculatable) UnoRuntime.queryInterface(XCalculatable.class, component);
        if (calc != null)
            if (dirtyCellsOnly)
                calc.calculate();
            else calc.calculateAll();
        XDrawPagesSupplier dps = (XDrawPagesSupplier) UnoRuntime.queryInterface(XDrawPagesSupplier.class, component);
        if (dps != null) {
            try {
                XDrawPages drawPages = dps.getDrawPages();
                for (int i = 0; i < drawPages.getCount(); i++) {
                    XDrawPage drawPage = (XDrawPage) UnoRuntime.queryInterface(XDrawPage.class, drawPages.getByIndex(i));
                    XFormsSupplier formsSupplier = (XFormsSupplier) UnoRuntime.queryInterface(XFormsSupplier.class, drawPage);
                    if (formsSupplier != null)
                        processForm(formsSupplier.getForms());
                }
            } catch (Exception e) { }
        }
    }

    /**
     * Returns the existing numberFormat with the given formatString or adds a new one
     * if it is missing.
     * @param formatString The format string.
     * @return The index of the (internal) number format.
     */
    public int getNumberFormat(String formatString) {
        int result = -1;
        XNumberFormatsSupplier numberFormatsSupplier = (XNumberFormatsSupplier) UnoRuntime.queryInterface(XNumberFormatsSupplier.class, activeDocument);
        if (numberFormatsSupplier != null) {
            XNumberFormats numberFormats = numberFormatsSupplier.getNumberFormats();
            result = numberFormats.queryKey(formatString, officeLocale, true);
            if (result < 0) {
                try {
                    result = numberFormats.addNew(formatString, officeLocale);
                } catch (MalformedNumberFormatException ex) {
                    ex.printStackTrace();
                }
                
            }
        }
        return result;
    }
    
    /**
     * Returns the existing numberFormat with the given formatString or adds a new one
     * if it is missing.
     * @param formatString The format string.
     * @return The index of the (internal) number format.
     */
    public int getNumberFormat(int precision) {
        int result = -1;
        XNumberFormatsSupplier numberFormatsSupplier = (XNumberFormatsSupplier) UnoRuntime.queryInterface(XNumberFormatsSupplier.class, activeDocument);
        if (numberFormatsSupplier != null) {
            XNumberFormats numberFormats = numberFormatsSupplier.getNumberFormats();
            String format = numberFormats.generateFormat(0, officeLocale, true, false, (short)precision, (short)1);
            result = numberFormats.queryKey(format, officeLocale, true);
        }
        return result;
    }

    /**
     * Reads hidden data from a cell.
     * @param cell The cell reference which contains the data to be read.
     * @param dataName The name of the data entry.
     * @param defaultValue The value to be used, when the data entry cannot be found.
     * @return The data.
     */
    public String getData(XCell cell, String dataName, String defaultValue) {
        try {
            XPropertySet propertySet = (XPropertySet) UnoRuntime.queryInterface(XPropertySet.class, cell);
            XNameContainer container = (XNameContainer) UnoRuntime.queryInterface(XNameContainer.class,
                    propertySet.getPropertyValue("UserDefinedAttributes"));
            if (container.hasByName(dataName)) {
                AttributeData a = (AttributeData) container.getByName(dataName);
                return a.Value;
            }
        } catch (UnknownPropertyException ex) {
            ex.printStackTrace();
        } catch (WrappedTargetException ex) {
            ex.printStackTrace();
        } catch (NoSuchElementException ex) {
            ex.printStackTrace();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        
        return defaultValue;
    }

    /**
     * Writes "hidden" data into a specified cell.
     * @param cell The cell reference to which the data should be written.
     * @param dataName The name of the data entry.
     * @param value The data itself.
     */
    public void setData(XCell cell, String dataName, String value) {
        try {
            XPropertySet propertySet = (XPropertySet) UnoRuntime.queryInterface(XPropertySet.class, cell);
            XNameContainer container = (XNameContainer) UnoRuntime.queryInterface(XNameContainer.class,
                    propertySet.getPropertyValue("UserDefinedAttributes"));

            if (container.hasByName(dataName)) {
                AttributeData a = (AttributeData) container.getByName(dataName);
                a.Value = value;
                container.replaceByName(dataName, a);
            } else {
                AttributeData a = new AttributeData();
                a.Type = "CDATA";
                a.Value = value;
                container.insertByName(dataName, a);
            }
            propertySet.setPropertyValue("UserDefinedAttributes", container);
        } catch (ElementExistException ex) {
            ex.printStackTrace();
        } catch (com.sun.star.lang.IllegalArgumentException ex) {
            ex.printStackTrace();
        } catch (PropertyVetoException ex) {
            ex.printStackTrace();
        } catch (UnknownPropertyException ex) {
            ex.printStackTrace();
        } catch (WrappedTargetException ex) {
            ex.printStackTrace();
        } catch (NoSuchElementException ex) {
            ex.printStackTrace();
        }
    }
    
    /**
     * Returns the resource bundle for the default (OOo) locale.
     * @param bundle The name of the bundle.
     * @return The resource bundle.
     */
    public ResourceBundle getResourceBundle(String bundle) {
        return ResourceBundle.getBundle(bundle, locale);
    }
    
    /**
     * Finds the named bundle and queries the named key to a localization entry.
     * @param bundle The name of the bundle.
     * @param key The name of the localized item.
     * @return The localized content.
     */
    public String getLocalization(String bundle, String key) {
        return getResourceBundle(bundle).getString(key);
    }

    /**
     * @return the Decimal Separator setup for the current locale
     */
    public String getDecimalSeparator() {
        return decimalSeparator;
    }

     @Override
    public void windowHidden(EventObject arg0) {
        for (JDialog d : openDialogs) d.setVisible(false);
    }

    @Override
    public void windowMoved(WindowEvent arg0) {
        //nothing to to
    }

    @Override
    public void windowResized(WindowEvent arg0) {
        //nothing to to
    }

    @Override
    public void windowShown(EventObject arg0) {
         for (JDialog d : openDialogs) d.setVisible(true);
    }

    // </editor-fold>

}
