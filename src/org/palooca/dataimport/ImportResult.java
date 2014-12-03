/*
 * ImportResult.java
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
 * Created on 28. October 2007, 16:37
 *
 */

package org.palooca.dataimport;

import com.sun.star.sheet.XResultListener;
import com.sun.star.uno.XComponentContext;
import java.util.Vector;
import org.palooca.ExternalImportListener;
import org.palooca.ImportFunctionListener;
import org.palooca.PalOOCaManager;
import org.palooca.network.ConnectionHandler;

/**
 *
 * @author Andreas Schneider
 */
public abstract class ImportResult{
    
    // Static members
    protected static Vector<ImportFunctionListener> importFuncListeners = new Vector<ImportFunctionListener>();
    protected static Vector<ExternalImportListener> externalImportListeners = new Vector<ExternalImportListener>();

    public static void addImportFunctionListener(ImportFunctionListener listener){
        if (!importFuncListeners.contains(listener))
            importFuncListeners.add(listener);
    }

    public static void removeImportFunctionListener(ImportFunctionListener listener){
        if (importFuncListeners.contains(listener))
            importFuncListeners.remove(listener);
    }

    public static void addExternalImportListener(ExternalImportListener listener){
        if (!externalImportListeners.contains(listener))
            externalImportListeners.add(listener);
    }

    public static void removeExternalImportListener(ExternalImportListener listener){
        if (externalImportListeners.contains(listener))
            externalImportListeners.remove(listener);
    }

    //...therefore we use a state variable and do the import during a recalculate()
    
    protected static boolean importMode = false;
    protected static boolean externalImportMode = false;
    protected static ImportHandler importHandler = null;
    protected static Vector<String> importErrors = new Vector<String>();
    protected static final String DEFAULT_FILE_DELIMITER = "\t";
    protected static String fileDelimiter = DEFAULT_FILE_DELIMITER;
    private static boolean useResultCache = true;
    protected static Vector<ImportResult> resultCache = new Vector<ImportResult>();

    public static void setUseResultCache(boolean useResultCache){
        ImportResult.useResultCache = useResultCache;
    }

    public static boolean getUseResultCache(){
        return ImportResult.useResultCache;
    }

    public static void beginExternalImport(){
       externalImportMode = true;
       for (ExternalImportListener listener : externalImportListeners)
            listener.ExternalImportBegin();
    }

    public static void endExternalImport(){
        externalImportMode = false;
        ImportResult.setUseResultCache(true);
        
        for (ExternalImportListener listener : externalImportListeners)
            listener.ExternalImportEnd();
        resetFileDelimiter();
    }

    public static boolean getExternalImportMode(){
        return externalImportMode;
    }

    public static boolean getImportMode(){
        return importMode;
    }

    public static String getFileDelimiter(){
        return fileDelimiter;
    }

    public static void setFileDelimiter(String delimiter){
        fileDelimiter = delimiter;
    }

    public static void resetFileDelimiter(){
        fileDelimiter = DEFAULT_FILE_DELIMITER;
    }

    public static void beginImport(ImportHandler importHandler) {
        importMode = true;
        ImportResult.importHandler = importHandler;

        for (ImportFunctionListener listener : importFuncListeners)
            listener.ImportFunctionBegin();

    }

    public static boolean isImportError(String errorMessage){
        return importErrors.contains(errorMessage);
    }
    
    public static ImportResult doImport(ImportResult requestResult) {

        /* Retrieve the result object from cache
         * to get the previous value.  Important to get since we only
         * run the functions when in import mode.  When not in import
         * mode you want the previously set value
         */
        ImportResult result = null;
        if (getUseResultCache() == false){
            result = requestResult;
        }
        else{
            int index = resultCache.indexOf(requestResult);

            if (index >= 0)
                result = resultCache.get(index);

            if (result == null){
                result = requestResult;
                resultCache.add(result);
            }
        }
        
        if (importMode && importHandler.processImportResult(result)) {
            result.importSuccess = result.execute();
            if (!result.importSuccess && result.error.length() > 0)
                importErrors.add(result.error);

            result.importDone = true;
        }
        return result;
    }

    public static void endImport() {
        importMode = false;

        for (ImportFunctionListener listener : importFuncListeners)
            listener.ImportFunctionEnd();

        // All import errors should have been processed by now
        importErrors.clear();
    }

    public static void clearCache(){
        resultCache.clear();
    }
    
    // Instance members
    
    protected XComponentContext context;
    protected PalOOCaManager manager;
    protected ConnectionHandler connectionHandler;
    protected Vector<XResultListener> listeners = new Vector<XResultListener>();
    protected boolean importDone = false;
    protected boolean importSuccess = false;
    protected String  error = "";
    protected Object value;
    
    protected ImportResult(XComponentContext context) {
        this.context = context;
        this.manager = PalOOCaManager.getInstance(context);
        this.connectionHandler = manager.getConnectionHandler();
    }
    
    public final Object evaluate() {
        if (!importDone)
            return false;
        
        if (importDone && importSuccess)
            return value;//new Double(1);
        else
            return error; //new Double(0);
    }
    
    protected abstract boolean execute();
    
}
