/*
 * BaseConfig.java
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
 * Created on 29.08.2007, 15:01:26
 * 
 */

package org.palooca.config;

import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;
import com.sun.star.util.XStringSubstitution;
import java.io.File;
import java.io.FileWriter;
import java.net.URI;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

/**
 *
 * @author Andreas Schneider
 */
public class BaseConfig {
    private static BaseConfig instance;
    
    public static BaseConfig getInstance(XComponentContext context) {
        if (instance == null || instance.context != context) {//I assume that it will not happen to be called from another context again ...
            instance = new BaseConfig(context);
        }
        return instance;
    }
    
    private XComponentContext context;
    private String basePath;

    public String getBasePath() {
        return basePath;
    }
    
    private BaseConfig(XComponentContext context) {
        try {
            this.context = context;
            
            XMultiComponentFactory xMCF = context.getServiceManager();
            XStringSubstitution pathSubstitution = (XStringSubstitution)UnoRuntime.queryInterface(XStringSubstitution.class, xMCF.createInstanceWithContext("com.sun.star.util.PathSubstitution", context));
            basePath = pathSubstitution.substituteVariables("$(user)/PalOOCa/", false);
            new File(URI.create(basePath)).mkdirs();
               
        } catch (Exception e) {
            
        }
    }
    
    public Document openConfig(String configName) {
        Document result = null;
        try {
            Document document = new SAXReader().read(new File(URI.create(basePath + configName + ".xml")));
            Element root = document.getRootElement();
            if (root != null && root.getName().equals(configName))
                result = document;
        } catch (Exception e) {
            
        }
        
        if (result == null) {
            result = createConfig(configName);
        }
        
        return result;
    }
    
    public Document createConfig(String configName) {
        Document document = DocumentHelper.createDocument();
        document.addElement(configName);
        return document;
    }
    
    public void saveConfig(Document document) {
        try {
            Element root = document.getRootElement();
            if (root != null) {
                OutputFormat format = OutputFormat.createPrettyPrint();
                XMLWriter writer = new XMLWriter(new FileWriter(new File(URI.create(basePath + root.getName() + ".xml"))), format);
                writer.write(document);
                writer.close();
            }
        } catch (Exception e) {
            
        }
    }
}