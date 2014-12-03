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

import javax.swing.*;
import javax.swing.tree.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragGestureRecognizer;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSourceListener;
import java.awt.dnd.DropTargetListener;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.Point;
import java.awt.datatransfer.Transferable;
import java.awt.Cursor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.awt.Container;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.dnd.Autoscroll;
import com.jedox.palojlib.interfaces.IConsolidation;
import com.jedox.palojlib.interfaces.IDimension;
import com.jedox.palojlib.interfaces.IElement;
import com.jedox.palojlib.exceptions.*;

public class DnDJTree
        extends JTree implements DragSourceListener, DropTargetListener, DragGestureListener, Autoscroll {

    static DataFlavor localObjectFlavor;

    static {
        try {
            localObjectFlavor = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType);
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace(System.out);
        }
    }
    static DataFlavor[] supportedFlavors = {localObjectFlavor};
    DragSource dragSource;
    DropTarget dropTarget;
    TreeNode dropTargetNode = null;
    private int margin = 12;

    public TreeNode getDropTargetNode() {
        return dropTargetNode;
    }

    public void setDropTargetNode(TreeNode dropTargetNode) {
        this.dropTargetNode = dropTargetNode;
    }
    TreeNode draggedNode = null;

    public DnDJTree() {
        super();
        _init();
    }

    private void _init() {
        setModel(new DefaultTreeModel(new DefaultMutableTreeNode("default")));
        dragSource = new DragSource();
        DragGestureRecognizer dgr = dragSource.createDefaultDragGestureRecognizer(this, DnDConstants.ACTION_MOVE, this);
        dropTarget = new DropTarget(this, this);
    }

    public void dragGestureRecognized(DragGestureEvent dge) {
        System.out.println("dragGestureRecognized");
        Point clickPoint = dge.getDragOrigin();
        TreePath path = getPathForLocation(clickPoint.x, clickPoint.y);
        if (path == null) {
            System.out.println("NOT A NODE");
            return;
        }
        draggedNode = (TreeNode) path.getLastPathComponent();
        Transferable trans = new RJLTransferable(draggedNode);
        dragSource.startDrag(dge, Cursor.getDefaultCursor(), trans, this);
    }

    public void dragEnter(DragSourceDragEvent dsde) {
    }

    public void dragOver(DragSourceDragEvent dsde) {
    }

    public void dropActionChanged(DragSourceDragEvent dsde) {
    }

    public void dragDropEnd(DragSourceDropEvent dsde) {
        System.out.println("dragDropEnd");
        dropTargetNode = null;
        draggedNode = null;
        repaint();
    }

    public void dragExit(DragSourceEvent dse) {
    }

    public void dragEnter(DropTargetDragEvent dtde) {
        System.out.println("dragEnter");
        dtde.acceptDrag(DnDConstants.ACTION_MOVE);
    }

    public void dragOver(DropTargetDragEvent dtde) {
        Point dragPoint = dtde.getLocation();
        TreePath path = getPathForLocation(dragPoint.x, dragPoint.y);
        if (path == null) {
            dropTargetNode = null;
            dtde.rejectDrag();
        } else {
            dropTargetNode = (TreeNode) path.getLastPathComponent();
            dtde.acceptDrag(DnDConstants.ACTION_MOVE);
        }
        repaint();
    }

    public void dropActionChanged(DropTargetDragEvent dtde) {
    }

    public void drop(DropTargetDropEvent dtde) {
        if (draggedNode == dropTargetNode) {
            dtde.rejectDrop();
            return;
        }

        System.out.println("drop");
        Point dropPoint = dtde.getLocation();
        TreePath path = getPathForLocation(dropPoint.x, dropPoint.y);
        System.out.println("drop path is: " + path);
        boolean dropped = false;
        try {
            dtde.acceptDrop(DnDConstants.ACTION_MOVE);
            System.out.println("accepted");
            Object droppedObject = dtde.getTransferable().getTransferData(localObjectFlavor);
            ElementTreeNode droppedNode = null;

            if (droppedObject instanceof ElementTreeNode) {
                droppedNode = (ElementTreeNode) droppedObject;
                ElementTreeNode oldParent = null;
                
                if (droppedNode.getParent() instanceof ElementTreeNode)
                    oldParent = (ElementTreeNode) droppedNode.getParent();

                ((DefaultTreeModel) getModel()).removeNodeFromParent(droppedNode);

                if (oldParent != null) {
                    int childCount = oldParent.getChildCount();
                    IConsolidation elemCons[] = new IConsolidation[childCount];
                    IDimension dim = oldParent.getDimension();

                    for (int i = 0; i < childCount; i++) {
                        ElementTreeNode node = (ElementTreeNode) oldParent.getChildAt(i);
                        elemCons[i] = dim.newConsolidation(oldParent.getElement(),node.getElement(), 1.0);
                    }
                    dim.updateConsolidations(elemCons);
                    if (childCount == 0) {
                        dim.updateElementsType(new IElement[]{oldParent.getElement()},IElement.ElementType.ELEMENT_NUMERIC);
                    }
                }
            } else {
                dtde.rejectDrop();
                return;
            }

            DefaultMutableTreeNode dropNode;

            if (path == null) {
                dropNode = (DefaultMutableTreeNode) getModel().getRoot();
            } else {
                dropNode = (DefaultMutableTreeNode) path.getLastPathComponent();
            }

            if (dropNode.getParent() instanceof ElementTreeNode) {                  // not on root ?

                ElementTreeNode parent = (ElementTreeNode) dropNode.getParent();
                int index = parent.getIndex(dropNode);
                ((DefaultTreeModel) getModel()).insertNodeInto(droppedNode, parent, index);

                int childCount = parent.getChildCount();
                IConsolidation elemCons[] = new IConsolidation[childCount];
                IDimension dim = parent.getDimension();

                for (int i = 0; i < childCount; i++) {
                    ElementTreeNode node = (ElementTreeNode) parent.getChildAt(i);
                    elemCons[i] = dim.newConsolidation(parent.getElement(),node.getElement(), 1.0);
                }

                dim.updateConsolidations(elemCons);

            } else {

                DefaultMutableTreeNode parent = (DefaultMutableTreeNode)getModel().getRoot();
                int index = parent.getIndex(dropNode);
                ((DefaultTreeModel) getModel()).insertNodeInto(droppedNode, parent, index);
                ((ElementTreeNode)droppedNode).getElement().move(index); //TODO!!
            }

            if (droppedNode != null) {
                //((ElementTreeNode)droppedNode).getElement().getDimension().reload(true);
            }

            dropped = true;
        } catch (PaloJException pe) {
            JOptionPane.showMessageDialog(this, pe.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        } catch (PaloException pe) {
            JOptionPane.showMessageDialog(this, pe.getDescription() + ", " + pe.getReason(), "Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            ex.printStackTrace(System.out);
        }

        dtde.dropComplete(dropped);
    }

    public void dragExit(DropTargetEvent dte) {
    }

    public void autoscroll(Point cursor) {
        Insets insets = getAutoscrollInsets();
        Rectangle bounds = getBounds();
        final Rectangle view = getViewport(this).getViewRect();

        if (cursor.x < insets.left) {
            view.x -= getScrollableUnitIncrement(view, SwingConstants.HORIZONTAL, -1);
        } else if (cursor.x > bounds.width - insets.right) {
            view.x += getScrollableUnitIncrement(view, SwingConstants.HORIZONTAL, 1);
        }
        if (cursor.y < insets.top) {
            view.y -= getScrollableUnitIncrement(view, SwingConstants.VERTICAL, -1);
        } else if (cursor.y > bounds.height - insets.bottom) {
            view.y += getScrollableUnitIncrement(view, SwingConstants.VERTICAL, 1);
        }
        scrollRectToVisible(view);
    }

    public Insets getAutoscrollInsets() {
        JViewport viewport = getViewport(this);
        if (viewport != null) {
            java.awt.Dimension size = getSize();
            Rectangle view = viewport.getViewRect();
            Insets insets = new Insets(view.y + margin,
                    view.x - margin,
                    margin + size.height - (view.y + view.height),
                    margin - size.width - (view.x - view.width));
            return insets;
        }
        return new Insets(margin, margin, margin, margin);
    }

    private JViewport getViewport(Container con) {
        while (con != null && !(con instanceof JViewport)) {
            con = con.getParent();
        }
        return (JViewport) con;
    }

    class RJLTransferable implements Transferable {

        Object object;

        public RJLTransferable(Object o) {
            this.object = o;
        }

        public DataFlavor[] getTransferDataFlavors() {
            return supportedFlavors;
        }

        public boolean isDataFlavorSupported(DataFlavor flavor) {
            return flavor.equals(localObjectFlavor);
        }

        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
            if (isDataFlavorSupported(flavor)) {
                return object;
            } else {
                throw new UnsupportedFlavorException(flavor);
            }
        }
    }
}