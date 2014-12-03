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

import com.sun.star.uno.XComponentContext;
import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
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
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import com.jedox.palojlib.interfaces.IDimension;
import org.palooca.subsets.Subset2;

public class SubsetList extends JPanel
                         implements MouseListener, DragSourceListener,
                         DropTargetListener, DragGestureListener/*, /*Autoscroll*/ {

    private XComponentContext context;
    private final int sections = 3;
    private IDimension   dimension = null;
    private JPanel drawingPane;
    private int selectedIndex = -1;
    private Subset2 selectedSubset = null;
    private Subset2[] availableSubsets = null;

    public Subset2 getSelectedSubset() {
        return selectedSubset;
    }

    public void setSelectedSubset(Subset2 selectedSubset) {
        this.selectedSubset = selectedSubset;
    }
    private SubsetModeller modeller = null;
    private int openIndex = -1;
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

    public SubsetList() {
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
        dragSource.createDefaultDragGestureRecognizer(drawingPane, DnDConstants.ACTION_MOVE, this);
        dropTarget = new DropTarget(drawingPane, this);
        setDropTarget(dropTarget);
    }

    /** The component inside the scroll pane. */
    public class DrawingPane extends JPanel {

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            int y = 0;
            
            y = drawSection(g, y, 0,
                        java.util.ResourceBundle.getBundle("org/palooca/dialogs/PalOOCaDialogs").getString("FormulaSubset"));
            y = drawSection(g, y, 1,
                        java.util.ResourceBundle.getBundle("org/palooca/dialogs/PalOOCaDialogs").getString("LocalSubset"));
            y = drawSection(g, y, 2,
                        java.util.ResourceBundle.getBundle("org/palooca/dialogs/PalOOCaDialogs").getString("GlobalSubset"));
        }

        private int drawSection(Graphics g, int y, int sectionIndex, String label) {
            Graphics2D g2d = (Graphics2D)g;
            FontMetrics  fm = g.getFontMetrics(g.getFont());
            int itemHeight = getItemHeight(g);
            int yTxtOffset = itemHeight / 2 - fm.getMaxDescent() / 2 + fm.getMaxAscent() / 2;

            g.drawRect(0, y - 1, this.getWidth(), itemHeight);

            g.setColor(new Color(192, 192, 192));

            GradientPaint gradient;

            if (selectedIndex == sectionIndex) {
                gradient = new GradientPaint(0, y + itemHeight, new Color(206, 203, 195),
                                             0, y, new Color(183, 179, 166), true);
            } else {
                gradient = new GradientPaint(0, y + itemHeight, new Color(251, 251, 248),
                                             0, y, new Color(231, 239, 226), true);
            }

            g2d.setPaint(gradient);
            g2d.fillRect(0, y, getWidth(), itemHeight - 1);

            g.setColor(new Color(0, 0, 0));
            g.drawString(label, 3, y + yTxtOffset);

            Image img;
            if (openIndex == sectionIndex) {
                img = new javax.swing.ImageIcon(getClass().getResource("/images/panelClose.PNG")).getImage();
            } else {
                img = new javax.swing.ImageIcon(getClass().getResource("/images/panelOpen.PNG")).getImage();
            }

            g.drawImage(img, getWidth() - 18, y + (itemHeight - 16) / 2, null);
            y += itemHeight;

            if (selectedIndex == sectionIndex && openIndex == sectionIndex && dimension != null) {
                Subset2[] subsets = getSubsets(dimension);

                for (int j = 0; j < subsets.length; j++) {
                    if ((sectionIndex == 1 && subsets[j].getType() == Subset2.TYPE_LOCAL) ||
                        (sectionIndex == 2 && subsets[j].getType() == Subset2.TYPE_GLOBAL)) {

                        img = new javax.swing.ImageIcon(getClass().getResource("/images/subset.png")).getImage();
                        g.drawImage(img, 3, y + (itemHeight - 16) / 2, this);

                        if (subsets[j] == selectedSubset) {
                            Rectangle2D rtext = fm.getStringBounds(subsets[j].getName(), g);
                            g.setColor(new Color(49, 106, 197));
                            g.fillRect(23, y + 2, (int)rtext.getWidth() + 4, itemHeight - 4);
                            g.setColor(new Color(255, 255, 255));
                        } else {
                            g.setColor(new Color(0, 0, 0));
                        }
                        g.drawString(subsets[j].getName(), 25, y + yTxtOffset);

                        y += itemHeight;
                    }
                }
            }

            return y;
        }
    }
    public IDimension getDimension() {
        return dimension;
    }

    public void setDimension(IDimension dimension) {
        this.dimension = dimension;
        openIndex = -1;
        selectedIndex = -1;
        selectedSubset = null;
        availableSubsets = null;
    }

    public void setContext(XComponentContext context) {
        this.context = context;
    }

    public SubsetModeller getModeller() {
        return modeller;
    }

    public void setModeller(SubsetModeller modeller) {
        this.modeller = modeller;
    }

    private Subset2[] getSubsets(IDimension dimension) {
        /*
        if (availableSubsets == null) {
            availableSubsets = SubsetHandler.loadSubsets(modeller.getDatabase(), dimension, modeller.getConnectionInfo().getUsername());
         * return availableSubsets;
        }
         * */
        return new Subset2[0];
    }

    //Handle mouse events.
    public void mouseClicked(MouseEvent e) {
        
        boolean changed = true;
        Point   p = new Point(e.getX(), e.getY());
        Graphics g = this.getGraphics();
        int itemHeight = getItemHeight(g);

        Rectangle r = new Rectangle(0, 0, getWidth(), itemHeight);
        selectedSubset = null;

        for (int i = 0; i < sections; i++) {

            boolean open = (i == openIndex);
            if (r.contains(p)) {
                if (openIndex == i) {
                    openIndex = -1;
                } else {
                    openIndex = i;
                }

                selectedIndex = i;
                break;
            }

            r.y += itemHeight;

            if (open && dimension != null) {

                int j;
                Subset2[] subsets = getSubsets(dimension);
                for (j = 0; j < subsets.length; j++) {
                    if ((selectedIndex == 1 && subsets[j].getType() == Subset2.TYPE_LOCAL) ||
                        (selectedIndex == 2 && subsets[j].getType() == Subset2.TYPE_GLOBAL)) {
                        if (r.contains(p)) {
                            selectedSubset = subsets[j];
                            modeller.setSubset(selectedSubset);
                            break;
                        }
                        r.y += itemHeight;
                    }
                }
                if (subsets.length > 0 && j != subsets.length)
                    break;
            }
        }


        if (changed) {
            Rectangle client = drawingPane.getVisibleRect();
            int height = sections * itemHeight;
            //Update client's preferred size because
            //the area taken up by the graphics has
            //gotten larger or smaller (if cleared).

            if (selectedIndex != -1) {
//                DimensionSubsetListItem dimItem = dimensions.get(selectedIndex);
//                if (dimItem.isOpen()) {
//                    org.palo.api.Dimension dim = dimItem.getDimension();
//                    Subset2[] subsets = dim.getSubsetHandler().getSubsets();
//                    height += (subsets.length + 1) * itemHeight;
//                }
            }

            drawingPane.setPreferredSize(new Dimension(client.width, height));

            //Let the scroll pane know to update itself
            //and its scrollbars.
            drawingPane.revalidate();
        }
        drawingPane.repaint();
    }

    private int getDimensionItemByLocation(Point p) {
//        Graphics g = this.getGraphics();
//        int itemHeight = getItemHeight(g);
//
//        Rectangle r = new Rectangle(0, 0, getWidth(), itemHeight);

//        for (int i = 0; i < dimensions.size(); i++) {
//            DimensionSubsetListItem dimItem = dimensions.elementAt(i);
//
//            boolean open = dimItem.isOpen();
//            if (r.contains(p)) {
//                return i;
//            }
//
//            r.y += itemHeight;
//
//            if (open) {
//                if (r.contains(p)) {
//                    return -1;
//                }
//
//                r.y += itemHeight;
//
//                org.palo.api.Dimension dim = dimItem.getDimension();
//                Subset2[] subsets = dim.getSubsetHandler().getSubsets();
//                for (int j = 0; j < subsets.length; j++) {
//                    if (r.contains(p)) {
//                        return -1;
//                    }
//                    r.y += itemHeight;
//                }
//            }
//        }

        return -1;
    }

    public void mouseReleased(MouseEvent e){}
    public void mouseEntered(MouseEvent e){}
    public void mouseExited(MouseEvent e){}
    public void mousePressed(MouseEvent e){

    }

    private void closeAllDimensionItems() {
//        for (int i = 0; i < dimensions.size(); i++) {
//            DimensionSubsetListItem dimItem = dimensions.elementAt(i);
//            dimItem.setOpen(false);
//        }
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

//        draggedNode = dimensions.get(index);
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
//            dimensions.remove(draggedNode);
            selectedIndex = -1;
        }

        draggedNode = null;
        repaint();
    }

    public void dragExit(DragSourceEvent dse) {
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

//                if (index == -1) {          // dropped at end
//                    dimensions.add(item);
//                    selectedIndex = dimensions.size() - 1;
//                } else {                    // insert
//                    dimensions.insertElementAt(item, index);
//                    selectedIndex = index;
//                }
                closeAllDimensionItems();
                repaint();
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
