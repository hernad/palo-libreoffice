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

import com.jedox.palojlib.interfaces.IDatabase;
import com.sun.star.uno.XComponentContext;
import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragGestureRecognizer;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.*;
import java.io.IOException;
import java.util.*;
import org.palooca.subsets.Subset2;
import com.jedox.palojlib.interfaces.IDimension;
import java.util.List;
import org.palooca.network.ConnectionInfo;

public class DimensionSubsetList extends JPanel
                         implements MouseListener, DragSourceListener,
                         DropTargetListener, DragGestureListener/*, /*Autoscroll*/ {

    private class FilterElementActionListener implements ActionListener {
        private ElementSingleSelectDialog browser;
        DimensionSubsetListItem dimItem;

        public FilterElementActionListener(ElementSingleSelectDialog browser, DimensionSubsetListItem dimItem) {
            this.browser = browser;
            this.dimItem = dimItem;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
               dimItem.setSelectedFilterPath(browser.getSelectionPath());
               dimItem.setSelectedFilterElementObject(browser.getSelectionElement());
               dimItem.setAttribute(browser.getAttribute());
        }
    }

    private XComponentContext context;

    public void setContext(XComponentContext context) {
        this.context = context;
    }
    private JPanel drawingPane;
    private Vector<DimensionSubsetListItem>    dimensions = new Vector<DimensionSubsetListItem>();;
    private int selectedIndex = -1;
    private boolean filter = false;
    private DragSource dragSource;
    private DropTarget dropTarget;
    private DimensionSubsetListItem draggedNode = null;

    static DataFlavor localObjectFlavor;

    static {
        try {
            localObjectFlavor = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType);
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace(System.out);
        }
    }
    static DataFlavor[] supportedFlavors = {localObjectFlavor};

    public DimensionSubsetList() {
        super(new BorderLayout());

        //Set up the drawing area.
        drawingPane = new DrawingPane();
        drawingPane.setBackground(Color.white);
        drawingPane.addMouseListener(this);

        //Put the drawing area in a scroll pane.
        JScrollPane scroller = new JScrollPane(drawingPane);
        scroller.setPreferredSize(new java.awt.Dimension(200,200));
        scroller.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        //Lay out this demo.
        add(scroller, BorderLayout.CENTER);

        dragSource = new DragSource();
        DragGestureRecognizer dgr = dragSource.createDefaultDragGestureRecognizer(drawingPane, DnDConstants.ACTION_MOVE, this);
        dropTarget = new DropTarget(drawingPane, this);
        setDropTarget(dropTarget);
    }


    public void addDimension(DimensionSubsetListItem dimItem) {
        dimensions.add(dimItem);
    }

    public void addDimension(ConnectionInfo connectionInfo, IDatabase database, IDimension dim) {
        Rectangle client = drawingPane.getVisibleRect();
        dimensions.add(new DimensionSubsetListItem(connectionInfo, database, dim, true, isFilter()));

        Graphics g = this.getGraphics();

        drawingPane.setPreferredSize(new Dimension(client.width, dimensions.size() * getItemHeight(g)));
        drawingPane.revalidate();
    }

    public DimensionSubsetListItem getDimension(int index) {
        return dimensions.get(index);
    }

    public Collection<DimensionSubsetListItem> getDimensions() {
        return dimensions;
    }

    public void setDimensions(List<DimensionSubsetListItem> dimensions) {
        clearDimensions();
        for (DimensionSubsetListItem d : dimensions) {
            addDimension(d);
        }
        Rectangle client = drawingPane.getVisibleRect();
        Graphics g = this.getGraphics();
        drawingPane.setPreferredSize(new Dimension(client.width, dimensions.size() * getItemHeight(g)));
        drawingPane.revalidate();
    }

    public int getDimensionCount() {
        return dimensions.size();
    }

    public void clearDimensions() {
        dimensions.clear();
        drawingPane.setPreferredSize(new Dimension(10, 10));
        drawingPane.revalidate();
        selectedIndex = -1;
    }

    /** The component inside the scroll pane. */
    public class DrawingPane extends JPanel {

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            int y = 0;
            
            Graphics2D g2d = (Graphics2D)g;
            FontMetrics  fm = g.getFontMetrics(g.getFont());
            int itemHeight = getItemHeight(g);
            int yTxtOffset = itemHeight / 2 - fm.getMaxDescent() / 2 + fm.getMaxAscent() / 2;

            for (int i = 0; i < dimensions.size(); i++) {
                DimensionSubsetListItem dimItem = dimensions.elementAt(i);
                IDimension dim = dimItem.getDimension();

                g.drawRect(0, y - 1, this.getWidth(), itemHeight);

                g.setColor(new Color(192, 192, 192));

                GradientPaint gradient;

                if (i == selectedIndex) {
                    gradient = new GradientPaint(0, y + itemHeight, new Color(206, 203, 195),
                                                 0, y, new Color(183, 179, 166), true);
                } else {
                    gradient = new GradientPaint(0, y + itemHeight, new Color(251, 251, 248),
                                                 0, y, new Color(231, 239, 226), true);
                }
                g2d.setPaint(gradient);
                
                g2d.fillRect(0, y, this.getWidth(), itemHeight - 1);

                g.setColor(new Color(0, 0, 0));
                g.drawString(dim.getName(), 3, y + yTxtOffset);

                Image img;
                if (dimItem.isOpen()) {
                    img = new javax.swing.ImageIcon(getClass().getResource("/images/panelClose.PNG")).getImage();
                } else {
                    img = new javax.swing.ImageIcon(getClass().getResource("/images/panelOpen.PNG")).getImage();
                }

                g.drawImage(img, this.getWidth() - 18, y + (itemHeight - 13) / 2, null);

                y += itemHeight;
                if (selectedIndex == i && dimItem.isOpen()) {
                    Subset2[] subsets = dimItem.getAvailableSubsets();


                    if (dimItem.getSubset() == null) {
                        img = new javax.swing.ImageIcon(getClass().getResource("/images/radioChecked.PNG")).getImage();
                    } else {
                        img = new javax.swing.ImageIcon(getClass().getResource("/images/radioUnchecked.PNG")).getImage();
                    }
                    g.drawImage(img, 4, y + (itemHeight - 13) / 2, this);
                    g.drawString(java.util.ResourceBundle.getBundle("org/palooca/dialogs/PalOOCaDialogs").getString("SelectElements"), 20, y + yTxtOffset);

                    y += itemHeight;

                    for (int j = 0; j < subsets.length; j++) {
                        if (dimItem.getSubset() == subsets[j]) {
                            img = new javax.swing.ImageIcon(getClass().getResource("/images/radioChecked.PNG")).getImage();
                        } else {
                            img = new javax.swing.ImageIcon(getClass().getResource("/images/radioUnchecked.PNG")).getImage();
                        }
                        g.drawImage(img, 4, y + (itemHeight - 13) / 2, this);

                        g.drawString(subsets[j].getName(), 20, y + yTxtOffset);

                        y += itemHeight;
                    }
                }
            }
        }
    }

    private void mouseClicked(Point p, int clickcount) {
        boolean changed = true;

        Graphics g = this.getGraphics();
        int itemHeight = getItemHeight(g);

        Rectangle r = new Rectangle(0, 0, getWidth(), itemHeight);

        for (int i = 0; i < dimensions.size(); i++) {
            DimensionSubsetListItem dimItem = dimensions.elementAt(i);

            boolean open = dimItem.isOpen();
            if (r.contains(p)) {
                if (dimItem.isOpen()) {
                    dimItem.setOpen(false);
                } else {
                    for (int j = 0; j < dimensions.size(); j++) {
                        DimensionSubsetListItem item = dimensions.elementAt(j);
                        item.setOpen(false);
                    }
                    dimItem.setOpen(true);
                }

                selectedIndex = i;
                break;
            }

            r.y += itemHeight;

            if (open) {
                if (r.contains(p)) {
                    dimItem.setSubset(null);
                    if (clickcount == 2) {
                        if (isFilter()) {
                            ElementSingleSelectDialog browser = new ElementSingleSelectDialog(
                                                        null, true, context,
                                                        dimItem.getDimension(),
                                                        dimItem.getSelectedFilter(),
                                                        dimItem.getAttribute());
                            browser.setVisible(true);
                            if (browser.getModalResult() == JOptionPane.OK_OPTION) {
                                dimItem.setSelectedFilterPath(browser.getSelectionPath());
                                dimItem.setSelectedFilterElementObject(browser.getSelectionElement());
                                dimItem.setAttribute(browser.getAttribute());
                            }
                        } else {
                            ElementBrowserDialog browser = new ElementBrowserDialog(
                                                        null, true, context,
                                                        dimItem.getDimension(),
                                                        dimItem.getSelectedElements(),
                                                        dimItem.getAttribute());
                            browser.setVisible(true);
                            if (browser.getModalResult() == JOptionPane.OK_OPTION) {
                                browser.getSelection(dimItem);
                                dimItem.setAttribute(browser.getAttribute());
                            }

                        }
                    }
                    break;
                }

                r.y += itemHeight;

                IDimension dim = dimItem.getDimension();
                int j;
                Subset2[] subsets = dimItem.getAvailableSubsets();
                for (j = 0; j < subsets.length; j++) {
                    if (r.contains(p)) {
                        dimItem.setSubset(subsets[j]);
                        break;
                    }
                    r.y += itemHeight;
                }
                if (subsets.length > 0 && j != subsets.length)
                    break;
            }
        }


//        Rectangle rect = new Rectangle(x, y, W, H);
//        drawingPane.scrollRectToVisible(rect);

        if (changed) {
            Rectangle client = drawingPane.getVisibleRect();
            int height = dimensions.size() * itemHeight;
            //Update client's preferred size because
            //the area taken up by the graphics has
            //gotten larger or smaller (if cleared).

            if (selectedIndex != -1) {
                DimensionSubsetListItem dimItem = dimensions.get(selectedIndex);
                if (dimItem.isOpen()) {
                    IDimension dim = dimItem.getDimension();
                    Subset2[] subsets = dimItem.getAvailableSubsets();
                    height += (subsets.length + 1) * itemHeight;
                }
            }

            drawingPane.setPreferredSize(new Dimension(client.width, height));

            //Let the scroll pane know to update itself
            //and its scrollbars.
            drawingPane.revalidate();
        }
        drawingPane.repaint();

    }

    //Handle mouse events.
    public void mouseClicked(MouseEvent e) {
        
        Point   p = null;
        if (e != null) {
          p = new Point(e.getX(), e.getY());
        }
        else {
          p = new Point(-1, -1);
        }
        mouseClicked(p, e.getClickCount());
        
    }

    private int getDimensionItemByLocation(Point p) {
        Graphics g = this.getGraphics();
        int itemHeight = getItemHeight(g);

        Rectangle r = new Rectangle(0, 0, getWidth(), itemHeight);

        for (int i = 0; i < dimensions.size(); i++) {
            DimensionSubsetListItem dimItem = dimensions.elementAt(i);

            boolean open = dimItem.isOpen();
            if (r.contains(p)) {
                return i;
            }

            r.y += itemHeight;

            if (open) {
                if (r.contains(p)) {
                    return -1;
                }

                r.y += itemHeight;

                IDimension dim = dimItem.getDimension();
                Subset2[] subsets = dimItem.getAvailableSubsets();
                for (int j = 0; j < subsets.length; j++) {
                    if (r.contains(p)) {
                        return -1;
                    }
                    r.y += itemHeight;
                }
            }
        }

        return -1;
    }

    public void mouseReleased(MouseEvent e){}
    public void mouseEntered(MouseEvent e){}
    public void mouseExited(MouseEvent e){}
    public void mousePressed(MouseEvent e){

    }

    private void closeAllDimensionItems() {
        for (int i = 0; i < dimensions.size(); i++) {
            DimensionSubsetListItem dimItem = dimensions.elementAt(i);
            dimItem.setOpen(false);
        }
    }

    private int getItemHeight(Graphics g) {
        FontMetrics  fm = g.getFontMetrics(g.getFont());

        return fm.getHeight() * 7 / 4;
    }

    public boolean isFilter() {
        return filter;
    }

    public void setFilter(boolean filter) {
        this.filter = filter;
    }

    public void dragGestureRecognized(DragGestureEvent dge) {
        System.out.println("dragGestureRecognized");
        Point clickPoint = dge.getDragOrigin();

        int index = getDimensionItemByLocation(clickPoint);
        if (index == -1)
            return;

        draggedNode = dimensions.get(index);
        Transferable trans = new RJLTransferable(draggedNode);
        dragSource.startDrag(dge, DragSource.DefaultMoveDrop, trans, this);
    }

    public void dragEnter(DragSourceDragEvent dsde) {
    }

    public void dragOver(DragSourceDragEvent dsde) {
    }

    public void dropActionChanged(DragSourceDragEvent dsde) {
    }

    public void dragDropEnd(DragSourceDropEvent dsde) {
        System.out.println("dragDropEnd");

        if (dsde.getDropAction() == DnDConstants.ACTION_MOVE) {
            dimensions.remove(draggedNode);
            selectedIndex = -1;
        }

        draggedNode = null;
        mouseClicked(new Point(-1,-1),0);
        //the follwing should not be necessary but tries to fix a rerendering problem on dropping while moving with the mouse outside of component. TODO find better solution
        Container container = this;
        while (container.getParent() != null) {
            container = container.getParent();
            container.validate();
            container.repaint();
        }
        //mouseClicked(new MouseEvent(this,0,new Date().getTime(),0,dsde.getLocation().x,dsde.getLocation().y,1,false));
    }

    public void dragExit(DragSourceEvent dse) {
        revalidate();
        repaint();
    }

    public void dragEnter(DropTargetDragEvent dtde) {
        dtde.acceptDrag(DnDConstants.ACTION_MOVE);
    }

    public void dragOver(DropTargetDragEvent dtde) {
        dtde.acceptDrag(DnDConstants.ACTION_MOVE);
    }

    public void dropActionChanged(DropTargetDragEvent dtde) {
    }

    public void drop(DropTargetDropEvent dtde) {
        int index = getDimensionItemByLocation(dtde.getLocation());
        boolean dropped = false;

        try {
            dtde.acceptDrop(DnDConstants.ACTION_MOVE);
            System.out.println("accepted");
            Object droppedObject = dtde.getTransferable().getTransferData(localObjectFlavor);

            if (droppedObject instanceof DimensionSubsetListItem) {
                DimensionSubsetListItem item = new DimensionSubsetListItem((DimensionSubsetListItem)droppedObject);

                if (index == -1) {          // dropped at end
                    dimensions.add(item);
                    selectedIndex = dimensions.size() - 1;
                } else {                    // insert
                    dimensions.insertElementAt(item, index);
                    selectedIndex = index;
                }
                closeAllDimensionItems();
            } else {
                dtde.rejectDrop();
                return;
            }

            dropped = true;
        } catch (Exception ex) {
            dtde.rejectDrop();
            ex.printStackTrace(System.out);
        }

        dtde.dropComplete(dropped);
        mouseClicked(new Point(-1,-1),0);
        revalidate();
        repaint();
        //mouseClicked(new MouseEvent(this,0,new Date().getTime(),0,dtde.getLocation().x,dtde.getLocation().y,1,false));
    }

    public void dragExit(DropTargetEvent dte) {
    }

    public void autoscroll(Point cursor) {
//        Insets insets = getAutoscrollInsets();
//        Rectangle bounds = getBounds();
//        final Rectangle view = getViewport(this).getViewRect();
//
//        if (cursor.x < insets.left) {
//            view.x -= getScrollableUnitIncrement(view, SwingConstants.HORIZONTAL, -1);
//        } else if (cursor.x > bounds.width - insets.right) {
//            view.x += getScrollableUnitIncrement(view, SwingConstants.HORIZONTAL, 1);
//        }
//        if (cursor.y < insets.top) {
//            view.y -= getScrollableUnitIncrement(view, SwingConstants.VERTICAL, -1);
//        } else if (cursor.y > bounds.height - insets.bottom) {
//            view.y += getScrollableUnitIncrement(view, SwingConstants.VERTICAL, 1);
//        }
//        scrollRectToVisible(view);
    }

//    public Insets getAutoscrollInsets() {
//        JViewport viewport = getViewport(this);
//        if (viewport != null) {
//            java.awt.Dimension size = getSize();
//            Rectangle view = viewport.getViewRect();
//            Insets insets = new Insets(view.y + margin,
//                    view.x - margin,
//                    margin + size.height - (view.y + view.height),
//                    margin - size.width - (view.x - view.width));
//            return insets;
//        }
//        return new Insets(margin, margin, margin, margin);
//    }

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
