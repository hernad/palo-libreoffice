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

package org.palooca.dialogs;

import org.palooca.dialogs.nodes.ElementTreeNode;
import com.sun.star.uno.XComponentContext;
import java.awt.event.ItemEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import com.jedox.palojlib.interfaces.*;
import com.jedox.palojlib.main.ConnectionManager;
import org.palooca.PalOOCaManager;
import org.palooca.network.ConnectionChangeListener;
import org.palooca.network.ConnectionHandler;
import org.palooca.network.ConnectionInfo;
import org.palooca.network.ConnectionState;

/**
 *
 * @author MichaelRaue
 */
public class PaloDialogUtilities extends javax.swing.JDialog implements ConnectionChangeListener {

    protected DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode();
    protected DefaultComboBoxModel databaseModel = new DefaultComboBoxModel();
    protected DefaultComboBoxModel cubeModel = new DefaultComboBoxModel();
    protected DefaultComboBoxModel dimensionComboModel = new DefaultComboBoxModel();
    protected DefaultTreeModel elementTreeModel = new DefaultTreeModel(rootNode);
    protected DefaultListModel elementModel = new DefaultListModel();
    protected XComponentContext context;
    protected PalOOCaManager manager;
    protected ConnectionHandler connectionHandler;
    protected IDatabase database = null;
    protected ICube cube = null;
    protected IDimension dimension = null;
    protected IAttribute attribute = null;

    public IAttribute getAttribute() {
        return attribute;
    }

    public void setAttribute(IAttribute attribute) {
        this.attribute = attribute;
    }
    protected ResourceBundle resourceBundle;
    protected ConnectionInfo connectionInfo = null;

    public PaloDialogUtilities(java.awt.Frame parent, boolean modal, XComponentContext context) {
        super(parent, modal);
        init(context);
    }

    public PaloDialogUtilities(java.awt.Dialog parent, boolean modal, XComponentContext context) {
        super(parent, modal);
        init(context);
    }

    private void init(XComponentContext context) {
        this.context = context;
        this.manager = PalOOCaManager.getInstance(context);
        this.connectionHandler = manager.getConnectionHandler();
        this.resourceBundle = PalOOCaManager.getInstance(context).getResourceBundle("org/palooca/dialogs/PalOOCaDialogs");
    }

    public void buildDatabaseList() {
        databaseModel.removeAllElements();

        ArrayList<ConnectionInfo> connections = connectionHandler.getConnections();
        Iterator<ConnectionInfo> iterator = connections.iterator();
        while (iterator.hasNext()) {
            ConnectionInfo connInfo = iterator.next();
            if (connInfo.getState() != ConnectionState.Connected) {
                if (connInfo.isAutoLogin()) {
                    connInfo.connect(this,2000);
                }
            }
            databaseModel.addElement(connInfo);

            IConnection connection =  connInfo.getConnection();
//            Connection connection =  connInfo.connect();
            if (connection != null) {
                IDatabase[] databases =  connection.getDatabases();
                for (int i = 0; i < databases.length; i++) {
                    if (databases[i].getType().equals(IDatabase.DatabaseType.DATABASE_NORMAL)) {
                        DatabaseInfo dbInfo = new DatabaseInfo(connInfo, databases[i]);
                        databaseModel.addElement(dbInfo);
                    }
                }
            }
        }
    }

    protected void selectDatabaseCombo(ConnectionInfo connInfo, IDatabase database, JComboBox combo) {

        if (connInfo == null || database == null)
            return;

        for (int i = 0; i < databaseModel.getSize(); i++) {
            Object obj = databaseModel.getElementAt(i);
            if (obj instanceof DatabaseInfo) {
                DatabaseInfo dbInfo = (DatabaseInfo)obj;
                if (dbInfo.getDatabase() == database) {
                    combo.setSelectedIndex(i);
                    break;
                }
            }
        }
    }

    protected void comboDatabaseChangeEvent(java.awt.event.ItemEvent evt, JComboBox cubeComboBox) {
        if (evt.getStateChange() == ItemEvent.SELECTED) {
            Object val  = evt.getItem();
            if (val instanceof DatabaseInfo) {
                DatabaseInfo dbInfo = (DatabaseInfo) evt.getItem();
                setDatabase(dbInfo.getConnInfo(), dbInfo.getDatabase(), cubeComboBox);
            }
            else
            {
                resetDatabase();
            }
        }
    }

    protected void setDatabase(ConnectionInfo connectionInfoNew, IDatabase databaseNew, JComboBox comboBoxCube) {

        resetDatabase();

        if (connectionInfoNew != null && databaseNew != null) {
            setConnectionInfo(connectionInfoNew);
            database = databaseNew;
            if (comboBoxCube != null)
                buildCubeList(ICube.CubeType.CUBE_NORMAL, comboBoxCube);
        }
    }

    protected IDatabase getDatabase() {
        return database;
    }

    protected void setConnectionInfo(ConnectionInfo connectionInfo) {
        if (this.connectionInfo != connectionInfo) {
            if (this.connectionInfo != null)
                this.connectionInfo.removeConnectionChangeListener(this);
            if (connectionInfo != null)
                connectionInfo.addConnectionChangeListener(this);

            this.connectionInfo = connectionInfo;
        }
    }

    protected ConnectionInfo getConnectionInfo() {
        return connectionInfo != null ? connectionInfo : connectionHandler.getLastConnectionInfo();
    }

    public void connectionChanged(ConnectionInfo connectionInfo) {
        if (connectionInfo.getState() != ConnectionState.Connected) {
            setConnectionInfo(null);
            database = null;
            dimension = null;
            cube = null;
            cubeModel.removeAllElements();
            rootNode.removeAllChildren();
        }
    }


    protected void resetDatabase() {
        setConnectionInfo(null);
        database = null;
        cubeModel.removeAllElements();

    }

    protected void buildCubeList(ICube.CubeType cubeType, JComboBox cubeComboBox) {
        cubeModel.removeAllElements();
        ICube[] cubes = database.getCubes();
        for (int i = 0; i < cubes.length; i++) {
            if (cubes[i].getType() == cubeType) {
                IDimension[] dims = cubes[i].getDimensions();
                int j;
                for (j = 0; j < dims.length; j++) {
                    if (dims[j].getElements(false).length == 0)
                        break;
                }
                if (j == cubes[i].getDimensions().length)
                    cubeModel.addElement(cubes[i]);
            }
        }

        if (cubeModel.getSize() > 0) {
            cubeComboBox.setSelectedIndex(0);
            cubeComboBox.setEnabled(true);
        }
    }

    protected void buildFlatElementList(IDimension dimension, final IAttribute attribute) {
        if (dimension == null)
            return;

        rootNode.removeAllChildren();

        IElement[] elems = dimension.getElements(false);

        for (int i = 0; i < elems.length; i++) {
            rootNode.add(new ElementTreeNode(elems[i],dimension));
        }

        elementTreeModel.reload();
    }

    protected void visitElement(DefaultMutableTreeNode parentNode, IElement element, IDimension dimension) {
        DefaultMutableTreeNode childNode = new ElementTreeNode(element,dimension);
        parentNode.add(childNode);
        for (IElement c : element.getChildren()) {
            visitElement(childNode,c,dimension);
        }
    }

    protected void buildElementList(IDimension dimension, final IAttribute attribute) {
        if (dimension != null) {
            rootNode.removeAllChildren();
            for (IElement root : dimension.getRootElements(false)) {
                visitElement(rootNode,root,dimension);
            }
            elementTreeModel.reload();
        }
    }

    protected static boolean searchForNode(JTree tree, String name, IAttribute attribute, TreeNode parent, boolean shift, boolean singleSelect) {
        boolean success = false;
        String currentName;

        if (parent instanceof ElementTreeNode) {
            if (attribute == null) {
                currentName = ((ElementTreeNode)parent).getElement().getName();
            } else {
                ElementTreeNode elementTreeNode = (ElementTreeNode)parent;
                IElement attributedElement = elementTreeNode.getDimension().getElementByName(elementTreeNode.getElement().getName(), true);
                currentName = attributedElement.getAttributeValue(attribute.getName()).toString();
                if (currentName.length() == 0)
                    currentName = ((ElementTreeNode)parent).getElement().getName();

            }

            Pattern searchPattern = Pattern.compile(name);
            Matcher m = searchPattern.matcher(currentName);
            if (m.matches()) {
                TreePath selection = new TreePath(((DefaultMutableTreeNode) parent).getPath());
                tree.addSelectionPath(selection);
                success = true;
                if (singleSelect)
                    return success;
            }
        }

        Enumeration<TreeNode> e = parent.children();
        while (e.hasMoreElements()) {
            if (searchForNode(tree, name, attribute, e.nextElement(), shift, singleSelect) == true) {
                success = true;
                if (singleSelect)
                    return success;
            }
        }

        return success;
    }

    protected TreeNode findNode(String name, TreeNode parent, boolean exactMatch) {
        if (parent instanceof ElementTreeNode) {
            String currentName = ((ElementTreeNode)parent).getElement().getName();
            if ((exactMatch && currentName.equals(name)) || (!exactMatch && currentName.compareToIgnoreCase(name) >= 0))
                return parent;
        }

        Enumeration<TreeNode> e = parent.children();
        while (e.hasMoreElements()) {
            TreeNode result = findNode(name, e.nextElement(), exactMatch);
            if (result != null)
                return result;
        }

        return null;
    }

    public static TreePath findByName(JTree tree, String[] names) {
        TreeNode root = (TreeNode) tree.getModel().getRoot();
        return find2(tree, new TreePath(root), names, 0, true);
    }

    private static TreePath find2(JTree tree, TreePath parent, Object[] nodes,
            int depth, boolean byName) {
        TreeNode node = (TreeNode) parent.getLastPathComponent();
        Object o = node;

        if (o == null)
            return null;

        // If by name, convert node to a string
        if (byName) {
            o = o.toString();
        }

//        if (o == null)
//            return null;

        System.err.println(o+" : "+nodes[depth]);

        // If equal, go down the branch
        if ((depth == 0) || o.equals(nodes[depth])) {
            // If at end, return match
            if (depth == nodes.length - 1) {
                return parent;
            }

            // Traverse children
            if (node.getChildCount() >= 0) {
                for (Enumeration e = node.children();
                        e.hasMoreElements();) {
                    TreeNode n = (TreeNode) e.nextElement();
                    TreePath path = parent.pathByAddingChild(n);
                    TreePath result = find2(tree, path, nodes, depth + 1,
                            byName);
                    // Found a match
                    if (result != null) {
                        return result;
                    }
                }
            }
        }

        // No match at this branch
        return null;
    }

    protected void sortList(boolean ascending) {
        ListElement tmp[] = new ListElement[elementModel.getSize()];

        for (int i = 0; i < elementModel.getSize(); i++) {
            tmp[i] = (ListElement) elementModel.get(i);
        }

        Arrays.sort(tmp, new ElementComparator());

        elementModel.clear();

        if (ascending) {
            for (int i = 0; i < tmp.length; i++) {
                elementModel.addElement(tmp[i]);
            }
        } else {
            for (int i = tmp.length - 1; i >= 0; i--) {
                elementModel.addElement(tmp[i]);
            }
        }
    }

    protected class ElementComparator implements Comparator {

        public int compare(Object obj1, Object obj2) {
            ListElement el1, el2;

            el1 = (ListElement) obj1;
            el2 = (ListElement) obj2;

            return el1.getNode().getElement().getName().compareTo(el2.getNode().getElement().getName());
        }

        public boolean equals(Object obj) {
            return false;
        }
    }

    // find out the lowest level that is collapsed (hidden root is zero)
    protected int getCollapsedLevel(JTree tree) {
        int level = 0;
        int cnt = tree.getRowCount();

        for (int i = 0; i < cnt; i++) {
            TreePath path = tree.getPathForRow(i);
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
            level = Math.max(level, node.getLevel());
        }

        return level;
    }

    // find out the lowest level that is collapsed (hidden root is zero)
    protected int getExpandedLevel(JTree tree) {
        int level = Integer.MAX_VALUE;
        int cnt = tree.getRowCount();

        if (cnt == 0)
            return 0;

        for (int i = 0; i < cnt; i++) {
            TreePath path = tree.getPathForRow(i);
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
            if (node.getChildCount() > 0 && tree.isCollapsed(path)) {
                level = Math.min(level, node.getLevel());
            }
        }

        return level;
    }

    protected void expandAll(JTree tree, boolean expand) {
        TreeNode root = (TreeNode) tree.getModel().getRoot();

        // Traverse tree from root
        expandAll(tree, new TreePath(root), expand);
    }

    protected void expandAll(JTree tree, TreePath parent, boolean expand) {
        // Traverse children
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) parent.getLastPathComponent();

        if (node.getChildCount() >= 0) {
            for (Enumeration e = node.children(); e.hasMoreElements();) {
                TreeNode n = (TreeNode) e.nextElement();
                TreePath path = parent.pathByAddingChild(n);
                expandAll(tree, path, expand);
            }
        }

        // Expansion or collapse must be done bottom-up
        if (expand) {
            tree.expandPath(parent);
        } else {
            TreeNode root = (TreeNode) tree.getModel().getRoot();
            if (root != node) {
                tree.collapsePath(parent);
            }
        }
    }

    protected void collapse(JTree tree)
    {
        int level = getCollapsedLevel(tree);
        collapseToLevel(tree, level - 1);
    }

    protected void collapseToLevel(JTree tree, int level)
    {
        int cnt = tree.getRowCount();

        for (int i = cnt - 1; i >= 0; i--) {
            TreePath path = tree.getPathForRow(i);
            ElementTreeNode node = (ElementTreeNode) path.getLastPathComponent();
            if (level == node.getLevel()) {
                tree.collapsePath(path);
                cnt = tree.getRowCount();
            }
        }
    }

    protected void expand(JTree tree) {
        int level = getExpandedLevel(tree);
        expandToLevel(tree, level);
        collapseToLevel(tree, level + 1);
    }

    protected void expandToLevel(JTree tree, int level) {
        int cnt = tree.getRowCount();

        for (int i = 0; i < cnt; i++) {
            TreePath path = tree.getPathForRow(i);
            ElementTreeNode node = (ElementTreeNode) path.getLastPathComponent();
            int lv = node.getLevel();
            if (level >= lv) {
                tree.expandPath(path);
                cnt = tree.getRowCount();
            }
        }
    }

    protected void updateMoveButtons(JTree tree, JList list, JButton btnLeft, JButton btnRight, JButton btnUp, JButton btnDown) {
        int selRows[] = list.getSelectedIndices();
        int selTree = tree.getSelectionCount();

        if (selTree  == 0) {
            btnRight.setEnabled(false);
        } else {
            btnRight.setEnabled(true);
        }

        if (selRows.length == 0) {
            btnDown.setEnabled(false);
            btnLeft.setEnabled(false);
            btnUp.setEnabled(false);
        } else {
            if (selRows[0] == 0) {
                btnUp.setEnabled(false);
            } else {
                btnUp.setEnabled(true);
            }
            if (selRows[selRows.length - 1] == elementModel.getSize() - 1) {
                btnDown.setEnabled(false);
            } else {
                btnDown.setEnabled(true);
            }

            btnLeft.setEnabled(true);
        }
    }

    public String stringArrayToStringPath(String[] path) {
        String  result = new String("");

        for (int i = 0; i < path.length; i++) {
            result += path[i];
            if (i < path.length - 1)
                result += "\\";
        }
        
        return result;
    }

    public String pathToStringPath(TreePath path) {
        String[] stringPath = pathToStringArray(path);

        if (path == null)
            return "";

        String result = new String("");

        for (int i = 1; i < stringPath.length; i++) {
            result += stringPath[i];
            if (i < stringPath.length - 1)
                result += "\\";
        }

        return result;
    }

    public String[] pathToStringArray(TreePath path) {
        String[] result;

        if (path == null || path.getPathCount() == 0) {
//            result = new String[1];
//            result[0] = new String("");
            return null;
        }

        result = new String[path.getPathCount()];

        for (int j = 0; j < path.getPathCount(); j++) {
            if (path.getPathComponent(j) instanceof ElementTreeNode)
                result[j] = new String(((ElementTreeNode) path.getPathComponent(j)).getElement().getName());
        }

        return result;
    }

    static public String[] stringPathToStringArray(String path) {
        String[] result = null;

        String rep = path.replace('\\', ':');
        String[] tmpResult = rep.split(new String(":"));
        
        result = new String[tmpResult.length + 1];

        result[0] = null;
        for (int i = 0; i < tmpResult.length; i++) {
            result[i + 1] = tmpResult[i];
        }

        return result;
    }
}
