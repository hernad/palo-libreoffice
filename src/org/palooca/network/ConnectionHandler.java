/*
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
 */

package org.palooca.network;

import com.sun.star.uno.XComponentContext;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.ResourceBundle;
import org.dom4j.Document;
import org.dom4j.Element;
import com.jedox.palojlib.interfaces.IDatabase;
import com.jedox.palojlib.interfaces.IConnection;
import org.palooca.config.*;

/**
 * The ConnectionHandler is a Thread which keeps connections alive for a certain time and handles the processing of connection strings.
 * @author Andreas Schneider
 */
public class ConnectionHandler
        implements ConnectionChangeListener
{
    
    private static ConnectionHandler instance;
    
    public static ConnectionHandler getInstance(XComponentContext context, BaseConfig baseConfig, ResourceBundle resourceBundle) {
        if (instance == null || instance.context != context) //I assume that it will not happen to be called from another context again ...
            instance = new ConnectionHandler(context, baseConfig, resourceBundle);
        return instance;
    }
    
    private XComponentContext context;
    private BaseConfig baseConfig;
    private ArrayList<ConnectionInfo> connections;
    private ArrayList<ConnectionsChangeListener> connectionsChangeListeners = new ArrayList<ConnectionsChangeListener>();
    private ConnectionInfo lastConnectionInfo;
    private IConnection lastConnection;
    private IDatabase lastDatabase;
    private ResourceBundle resourceBundle;

    public ResourceBundle getResourceBundle() {
        return resourceBundle;
    }
    
    public ArrayList<ConnectionInfo> getConnections() {
        return connections;
    }
    
    public ConnectionInfo getLastConnectionInfo() {
        return lastConnectionInfo;
    }
    
    public IDatabase getLastDatabase() {
        return lastDatabase;
    }
    
    public void updateLastServDB(ConnectionInfo connectionInfo, IDatabase database) {
        if (connectionInfo != null && database != null) {
            lastConnectionInfo = connectionInfo;
            lastDatabase = database;
        }
    }

    /**
     * Creates a new instance of ConnectionHandler.
     */
    private ConnectionHandler(XComponentContext context, BaseConfig baseConfig,
            ResourceBundle resourceBundle) {
        this.context = context;
        this.baseConfig = baseConfig;
        this.resourceBundle = resourceBundle;
        connections = new ArrayList<ConnectionInfo>();
        Element root = baseConfig.openConfig("Connections").getRootElement();
        Iterator elementIterator = root.elementIterator("Connection");
        while (elementIterator.hasNext()) {
            ConnectionInfo info = new ConnectionInfo(context, this, (Element)elementIterator.next());
            connections.add(info);
        }
        
        if (connections.size() == 0) { //add default demo entry
            connections.add(new ConnectionInfo(context, this, "proclos", "palo.proclos.com", "7777", "admin", "admin", ConnectionInfo.TYPE_HTTP, false));
            connections.add(new ConnectionInfo(context, this, "localhost", "localhost", "7777", "admin", "admin", ConnectionInfo.TYPE_HTTP, false));
        }
    }

    public void autoConnect() {
        for (int i  = 0; i < connections.size(); i++)
        {
            ConnectionInfo info = connections.get(i);
            if (info.isAutoLogin()) {
                info.connect(2000);
            }
        }       
    }

    public void saveConfig() {
        Document document = baseConfig.createConfig("Connections");
        Element root = document.getRootElement();
        synchronized(this) {
            for (int i = 0; i < connections.size(); i++) {
                Element child = root.addElement("Connection");
                connections.get(i).serialize(child);
            }
        }
        baseConfig.saveConfig(document);
    }
    
    
    /**
     * Finds the connection with the given ConnectionString. If it is not already available, a new connection is established.
     * @param name The name of the connection as specified in the connection manager.
     * @return The connection to the given ConnectionString.
     */
    public IConnection getConnection(String name) {
        synchronized(this) {
            ConnectionInfo info = null;
            
            for (int i = 0; i < connections.size() && info == null; i++) {
                info = connections.get(i);
                if (!info.getName().equalsIgnoreCase(name)) {
                    info = null;
                }
            }
            
            if (info != null) {
                lastConnectionInfo = info;
                if (info.getState() != ConnectionState.Connected) {
                    return info.connect(null);
                } else {
                    return info.getConnection();
                }
            } else {
                return null;
            }
        }
    }
    
    /**
     * Parses the servdb string, retrieves the connection (using {@link #getConnection(String)}) and returns the database upon success.
     * @param servdb The servdb string (connectionName/database)
     * @return The database object (<I>null</I> if not successfull).
     */
    public IDatabase getDatabase(String servdb) {
        //split "connectionName/Database"
        String[] connectionData = servdb.split("/", 2);
        IConnection connection = null;
        if ( connectionData.length == 2 )
            connection = getConnection(connectionData[0]);

        IDatabase database = null;
        if ( connection != null ) {
            if (connection == lastConnection && lastDatabase != null && lastDatabase.getName().equals(connectionData[1])) {
                database = lastDatabase;
            } else {
                database = connection.getDatabaseByName(connectionData[1]);
            }
        }
        
        if (database != null) {
            lastConnection = connection;
            lastDatabase = database;
        }
        
        return database;
    }
    
    public ConnectionInfo getConnectionInfo(IConnection connection) {
        ConnectionInfo result = null;
        synchronized(this) {
            for (int i = 0; i < connections.size() && result == null; i++)
                if (connections.get(i).getConnection() == connection)
                    result = connections.get(i);
        }

        if (result != null) {
            if (result.isAutoLogin()) {
                result.connect(null,2000);
            }
        }

        return result;
    }

    @Override
    public void connectionChanged(ConnectionInfo connectionInfo) {
        if (connectionInfo.getState() != ConnectionState.Connected &&
                connectionInfo.getConnection() == lastConnection) {
            lastConnection = null;
            lastDatabase = null;
        }
    }
    
    public void addConnectionsChangeListener(ConnectionsChangeListener listener) {
        connectionsChangeListeners.add(listener);
        //send all current connections to the new listener
        synchronized(this) {
            listener.addAllConnections(connections);
        }
    }
    
    public void removeConnectionsChangeListener(ConnectionsChangeListener listener) {
        connectionsChangeListeners.remove(listener);
    }
    
}
