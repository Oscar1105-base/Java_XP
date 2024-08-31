package layout;

import code.DesktopAction;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

// 模擬桌面右鍵選單
public class DesktopContextMenu extends JPopupMenu {
    private JMenuItem newFolderItem, cutItem, copyItem, pasteItem, deleteItem;
    private JComponent selectedComponent;
    private ActionPanel actionPanel;

    public DesktopContextMenu(ActionPanel actionPanel) {
        this.actionPanel = actionPanel;

        newFolderItem = new JMenuItem("新增資料夾");
        cutItem = new JMenuItem("剪下");
        copyItem = new JMenuItem("複製");
        pasteItem = new JMenuItem("貼上");
        deleteItem = new JMenuItem("刪除");

        add(newFolderItem);
        add(cutItem);
        add(copyItem);
        add(pasteItem);
        add(deleteItem);

        pasteItem.setEnabled(false);

        ActionListener menuListener = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JMenuItem source = (JMenuItem) (e.getSource());

                if (source == newFolderItem) {
                    DesktopAction.createNewFolder(actionPanel);
                } else if (source == cutItem && selectedComponent instanceof JPanel) {
                    DesktopAction.cutComponent(selectedComponent, actionPanel);
                } else if (source == copyItem && selectedComponent instanceof JPanel) {
                    DesktopAction.copyComponent(selectedComponent, actionPanel);
                } else if (source == pasteItem) {
                    DesktopAction.pasteComponent(actionPanel);
                } else if (source == deleteItem && selectedComponent instanceof JPanel) {
                    DesktopAction.deleteComponent(selectedComponent, actionPanel);
                }
                updateMenuItems();
            }
        };

        newFolderItem.addActionListener(menuListener);
        cutItem.addActionListener(menuListener);
        copyItem.addActionListener(menuListener);
        pasteItem.addActionListener(menuListener);
        deleteItem.addActionListener(menuListener);
    }

    public void showMenu(JComponent invoker, int x, int y, JComponent clickedComponent) {
        this.selectedComponent = clickedComponent;
        updateMenuItems();
        super.show(invoker, x, y);
    }

    public void updateMenuItems() {
        boolean hasClipboardContent = actionPanel.hasClipboardContent();
        boolean isOnIcon = selectedComponent != null && !(selectedComponent instanceof JLayeredPane);
        newFolderItem.setEnabled(!isOnIcon);
        cutItem.setEnabled(isOnIcon);
        copyItem.setEnabled(isOnIcon);
        pasteItem.setEnabled(hasClipboardContent);
        deleteItem.setEnabled(isOnIcon);
    }

    public void attachToComponent(JComponent component) {
        component.addMouseListener(new PopupListener());
    }

    private class PopupListener extends MouseAdapter {
        public void mousePressed(MouseEvent e) {
            maybeShowPopup(e);
        }

        public void mouseReleased(MouseEvent e) {
            maybeShowPopup(e);
        }

        private void maybeShowPopup(MouseEvent e) {
            if (e.isPopupTrigger()) {
                selectedComponent = (JComponent) e.getComponent();
                updateMenuItems();
                show(e.getComponent(), e.getX(), e.getY());
            }
        }
    }
}