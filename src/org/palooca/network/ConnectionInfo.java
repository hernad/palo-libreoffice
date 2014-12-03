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

import com.jedox.palojlib.exceptions.PaloException;
import com.jedox.palojlib.exceptions.PaloJException;
import com.sun.star.uno.XComponentContext;
import java.awt.Component;

import java.util.Date;
import java.util.Vector;
import javax.swing.JOptionPane;
import org.dom4j.Element;
import com.jedox.palojlib.interfaces.IConnection;
import com.jedox.palojlib.interfaces.IConnectionConfiguration;
import com.jedox.palojlib.main.ClientInfo;
import com.jedox.palojlib.main.ConnectionConfiguration;
import com.jedox.palojlib.main.ConnectionManager;
import org.palooca.PalOOCaManager;
import org.palooca.RunnableWarning;
import org.palooca.config.XMLHelper;
import org.palooca.olap4j.Olap4jConnection;


/**
 *
 * @author Andreas Schneider
 */
public class ConnectionInfo {

    public static final int TYPE_HTTP = 2;
    public static final int TYPE_XMLA = 3;

    private int timeout = 10000; //10 s
    
    private XComponentContext context;
    private ConnectionHandler owner;
    private String name;
    private String host;
    private String port;
    private String username;
    private String password;
    private int type = TYPE_HTTP;
    private boolean autoLogin = false;
    private int retryInterval = 1000; //1s
    private long lastTry = 0;

    public boolean isAutoLogin() {
        return autoLogin;
    }

    public void setAutoLogin(boolean autoLogin) {
        this.autoLogin = autoLogin;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
    private IConnection connection;
    private Date lastActivity;
    private ConnectionState state;
    private Vector<ConnectionChangeListener> changeListener = new Vector<ConnectionChangeListener>();
    
    public IConnection getConnection() {
        return connection;
    }
    
    public void setConnection(IConnection connection) {
        if (this.connection != connection) {
            this.connection = connection;
        }
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }
    
    public String getHost() {
        return host;
    }
    
    public void setHost(String host) {
        this.host = host;
    }
    
    public Date getLastActivity() {
        return lastActivity;
    }
    
    public void setLastActivity(Date lastActivity) {
        this.lastActivity = lastActivity;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public String getPort() {
        return port;
    }
    
    public void setPort(String port) {
        this.port = port;
    }
    
    public ConnectionState getState() {
        return state;
    }
    
    public void setState(ConnectionState state) {
        if (this.state != state) {
            
            this.state = state;
            
            if (state != ConnectionState.Connected && connection != null) {
                if (connection.isConnected())
                    connection.close();
                setConnection(null);
            }
                        
            for (int i = 0; i < changeListener.size(); i++)
                changeListener.get(i).connectionChanged(this);
            
            PalOOCaManager.getInstance(context).recalculateDocument();
        }
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public ConnectionInfo(XComponentContext context, ConnectionHandler owner, String name, 
                    String host, String port, String username, String password, int type, boolean autoLogin) {
        this.context = context;
        this.owner = owner;
        this.name = name;
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
        this.connection = null;
        this.autoLogin = autoLogin;
        this.type = type;
        this.lastActivity = new Date(); //set current time
        this.state = ConnectionState.Uninitialized;
    }
    
    public ConnectionInfo(XComponentContext context, ConnectionHandler owner, Element element) {
        this.context = context;
        this.owner = owner;
        deserialize(element);
        this.connection = null;
        this.lastActivity = new Date();
        this.state = ConnectionState.Uninitialized;
    }
    
    public void serialize(Element element) {
        element.addAttribute("Name", name);
        XMLHelper.WriteString(element, "Host", host);
        XMLHelper.WriteString(element, "Port", port);
        XMLHelper.WriteString(element, "Username", username);
        XMLHelper.WriteString(element, "Password", password);
        XMLHelper.WriteString(element, "Type", Integer.toString(type));
        XMLHelper.WriteString(element, "AutoLogin", Boolean.toString(autoLogin));
    }
    
    public void deserialize(Element element) {
        this.name = element.attributeValue("Name");
        this.host = XMLHelper.ReadString(element, "Host", "127.0.0.1");
        this.port = XMLHelper.ReadString(element, "Port", "7777");
        this.username = XMLHelper.ReadString(element, "Username", "");
        this.password = XMLHelper.ReadString(element, "Password", "");
        this.type = Integer.parseInt(XMLHelper.ReadString(element, "Type", Integer.toString(TYPE_XMLA)));
        this.autoLogin = Boolean.parseBoolean((XMLHelper.ReadString(element, "AutoLogin", Boolean.toString(false))));
    }
    
    @Override
    public String toString() {
        return name;
    }
    
    public void addConnectionChangeListener(ConnectionChangeListener listener) {
        changeListener.add(listener);
    }
    
    public void removeConnectionChangeListener(ConnectionChangeListener listener) {
        changeListener.remove(listener);
    }

    protected IConnectionConfiguration getConnectionConfiguration(Integer timeout) {
        IConnectionConfiguration cc = new ConnectionConfiguration();
        cc.setHost(getHost());
        cc.setPort(getPort());
        cc.setUsername(getUsername());
        cc.setPassword(getPassword());
        if (timeout == null) {
            cc.setTimeout(getTimeout());
        } else {
            cc.setTimeout(timeout);
        }
        cc.setClientInfo(new ClientInfo("OpenOffice","connection " + getName()) );
        return cc;
    }

    public boolean connect() {
        return connect(getTimeout());
    }

    public boolean connect(int timeout) {
        boolean success = false;
        //decativate socket reuse, since this may cause crashes / OLAP server not beeing responsive any more.
       com.jedox.palojlib.http.SocketManager.getInstance().setActive(false);

                try {
//                    setConnection(ConnectionFactory.getInstance().newConnection(host, port, username, password));
                    IConnection connection = null;
                    switch (getType()) {
                        case TYPE_HTTP: connection = ConnectionManager.getInstance().getConnection(getConnectionConfiguration(timeout)); break;
                        case TYPE_XMLA: connection = new Olap4jConnection(getHost(),getPort(),getUsername(),getPassword(),null); break;
                    }
                    setConnection(connection);
                    connection.open();
                    connection.getDatabases();
                    success = true;
                } catch (PaloException e) {
                    e.printStackTrace();
                     
                } catch (PaloJException e) {
                    e.printStackTrace();

                }catch (Exception e) {
                    e.printStackTrace();
                }


            if (success) {
                setState(ConnectionState.Connected);
            } else {
                setState(ConnectionState.Disconnected); 
            }
        return success;
    }

    public IConnection connect(Component comp) {
        return connect(comp,getTimeout());
    }

    public IConnection connect(Component comp, int timeout) {
        if (state != ConnectionState.Connected) {
            ConnectionState oldState = state;
            if (System.currentTimeMillis() - lastTry > retryInterval) {
                lastTry = System.currentTimeMillis();
                if (!connect(timeout) && state != oldState) {
                    if (comp == null) {
                        RunnableWarning modal = new RunnableWarning(String.format(owner.getResourceBundle().getString("Connection_Failed_Text"), host, port),owner.getResourceBundle().getString("Connection_Failed_Caption"));
                        Thread thread = new Thread(modal);
                        thread.start();
                    } else {
                        JOptionPane.showMessageDialog(comp,
                        String.format(owner.getResourceBundle().getString("Connection_Failed_Text"), host, port),
                        owner.getResourceBundle().getString("Connection_Failed_Caption"), JOptionPane.WARNING_MESSAGE);
                    }
                }
            }
        }
        return connection;
    }  
}
