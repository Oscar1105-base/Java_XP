package layout.App;

import code.Compile;
import code.Config;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

//簡易Java 開發工具，目前只支援官方套件
public class JNotePadUI extends JFrame {

    private final FileChangeListener fileChangeListener;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final StringBuilder outputBuffer = new StringBuilder();
    private JMenuItem menuNew;
    private JMenuItem menuOpen;
    private JMenuItem menuSave;
    private JMenuItem menuSaveAs;
    private JMenuItem menuClose;
    private JMenu editMenu;
    private JMenuItem menuCut;
    private JMenuItem menuCopy;
    private JMenuItem menuPaste;
    private JMenuItem menuAbout;
    private JTextField inputField;
    private Font inputFont = new Font("Monospaced", Font.ITALIC, 12);
    private JButton sendButton;
    private JTextPane textPane;
    private JTextPane outputPane;
    private JPopupMenu popUpMenu;
    private JLabel stateBar;
    private JFileChooser fileChooser;
    private JButton runButton;
    private JButton stopButton;
    private Process runningProcess;
    private boolean isModified = false;
    private boolean isSaved = false; // 新增變量，用於記錄文件是否已保存
    private File currentFile;

    public JNotePadUI(FileChangeListener listener) {
        super("IDE for Simple Java");
        this.fileChangeListener = listener;
        this.currentFile = null;
        setUpUIComponent();
        setUpEventListener();
        setVisible(true);
        File defaultSaveDir = new File(Config.getTestPath());
        if (!defaultSaveDir.exists()) {
            defaultSaveDir.mkdirs();
        }
        fileChooser = new JFileChooser(defaultSaveDir);
        fileChooser.setAcceptAllFileFilterUsed(false);
        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("Java Files", "java"));
    }

    private void notifyFileCreated(String fileName) {
        if (fileChangeListener != null) {
            fileChangeListener.onFileCreated(fileName);
        }
    }

    private void setUpUIComponent() {
        setSize(640, 480);

        // 創建菜單欄
        JMenuBar menuBar = new JMenuBar();

        // 文件菜單
        JMenu fileMenu = new JMenu("檔案");
        menuNew = new JMenuItem("開啟新檔");
        menuNew.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_MASK));
        menuOpen = new JMenuItem("開啟舊檔");
        menuOpen.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_MASK));
        menuSave = new JMenuItem("儲存檔案");
        menuSave.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_MASK));
        menuSaveAs = new JMenuItem("另存新檔");
        menuClose = new JMenuItem("關閉");
        menuClose.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.CTRL_MASK));

        fileMenu.add(menuNew);
        fileMenu.add(menuOpen);
        fileMenu.addSeparator();
        fileMenu.add(menuSave);
        fileMenu.add(menuSaveAs);
        fileMenu.addSeparator();
        fileMenu.add(menuClose);

        // 編輯菜單
        editMenu = new JMenu("編輯");
        menuCut = new JMenuItem("剪下");
        menuCut.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, InputEvent.CTRL_MASK));
        menuCopy = new JMenuItem("複製");
        menuCopy.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_MASK));
        menuPaste = new JMenuItem("貼上");
        menuPaste.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, InputEvent.CTRL_MASK));

        editMenu.add(menuCut);
        editMenu.add(menuCopy);
        editMenu.add(menuPaste);

        // 關於菜單
        JMenu aboutMenu = new JMenu("關於");
        menuAbout = new JMenuItem("關於JNotePad");
        aboutMenu.add(menuAbout);

        menuBar.add(fileMenu);
        menuBar.add(editMenu);
        menuBar.add(aboutMenu);

        setJMenuBar(menuBar);

        // 主文本區域
        textPane = new JTextPane();
        textPane.setFont(new Font("細明體", Font.PLAIN, 16));
        JScrollPane textScrollPane = new JScrollPane(textPane, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        // 輸出區域
        outputPane = new JTextPane();
        outputPane.setEditable(false);
        outputPane.setFont(new Font("Monospaced", Font.PLAIN, 16));
        Dimension outputPaneSize = outputPane.getPreferredSize();
        outputPaneSize.height = outputPane.getFontMetrics(outputPane.getFont()).getHeight() * 5;
        outputPane.setPreferredSize(outputPaneSize);

        JScrollPane outputScrollPane = new JScrollPane(outputPane);
        outputScrollPane.setBorder(BorderFactory.createTitledBorder("執行輸出"));

        // 分割面板
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, textScrollPane, outputScrollPane);
        splitPane.setResizeWeight(0.7);

        // 按鈕面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        runButton = new JButton("執行");
        stopButton = new JButton("暫停");
        stopButton.setEnabled(false);
        buttonPanel.add(runButton);
        buttonPanel.add(stopButton);

        // 狀態欄
        stateBar = new JLabel("未修改");
        stateBar.setHorizontalAlignment(SwingConstants.LEFT);
        stateBar.setBorder(BorderFactory.createEtchedBorder());

        // 輸入區域（新增）
        inputField = new JTextField();
        sendButton = new JButton("發送");
        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.add(inputField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);
        inputPanel.setPreferredSize(new Dimension(outputPaneSize.width, inputPanel.getPreferredSize().height));
//        inputPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));


        // 布局
        Container contentPane = getContentPane();
        contentPane.add(splitPane, BorderLayout.CENTER);
        contentPane.add(buttonPanel, BorderLayout.NORTH);
        contentPane.add(stateBar, BorderLayout.SOUTH);
        contentPane.add(inputPanel, BorderLayout.SOUTH);

        // 設置最小尺寸
        int minWidth = Math.max(menuBar.getPreferredSize().width, 300);
        int minHeight = menuBar.getPreferredSize().height
                + buttonPanel.getPreferredSize().height
                + outputPaneSize.height
                + stateBar.getPreferredSize().height;
        setMinimumSize(new Dimension(minWidth, minHeight));
        splitPane.setMinimumSize(new Dimension(minWidth, minHeight - menuBar.getPreferredSize().height - stateBar.getPreferredSize().height));

        // 文件選擇器
        fileChooser = new JFileChooser();
        fileChooser.setAcceptAllFileFilterUsed(false);
        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("Java Files", "java"));

        // 彈出菜單
        popUpMenu = editMenu.getPopupMenu();
    }
    //  https://github.com/JustinSDK/JavaSE6Tutorial 良葛格Java SE6 github記事本實作
    private void setUpEventListener() {
        // 窗口關閉事件
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                closeFile();
            }
        });

        // 菜單項事件
        menuNew.addActionListener(e -> newFile());
        menuOpen.addActionListener(e -> openFile());
        menuSave.addActionListener(e -> saveFile());
        menuSaveAs.addActionListener(e -> saveFileAs());
        menuClose.addActionListener(e -> closeFile());
        menuCut.addActionListener(e -> cut());
        menuCopy.addActionListener(e -> copy());
        menuPaste.addActionListener(e -> paste());
        menuAbout.addActionListener(e -> {
            JOptionPane.showOptionDialog(null,
                    "第一個專題項目\n" +
                            "發想來自良葛格的記事本實作\n" +
                            "後將特化成為Java設計的編輯器\n" +
                            "作為Java專題的主要目標\n" ,
                    "關於JNotePad",
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.INFORMATION_MESSAGE,
                    null, null, null);
        });

        // 運行和停止按鈕事件
        runButton.addActionListener(e -> runCurrentFile());
        stopButton.addActionListener(e -> stopRunningProcess());

        // 文本區域事件
        textPane.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) {
                setModified(true);
            }

            public void removeUpdate(DocumentEvent e) {
                setModified(true);
            }

            public void insertUpdate(DocumentEvent e) {
                setModified(true);
            }
        });

        textPane.addKeyListener(new KeyAdapter() {
            public void keyTyped(KeyEvent e) {
                processTextArea();
            }
        });

        //輸入訊息提前顯示事件

        inputField.addActionListener(e -> sendInput());

        inputField.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) {
                updateOutputWithInput();
            }

            public void removeUpdate(DocumentEvent e) {
                updateOutputWithInput();
            }

            public void insertUpdate(DocumentEvent e) {
                updateOutputWithInput();
            }

        });
        // 右鍵菜單事件
        textPane.addMouseListener(new MouseAdapter() {
            public void mouseReleased(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON3)
                    popUpMenu.show(editMenu, e.getX(), e.getY());
            }

            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1)
                    popUpMenu.setVisible(false);
            }
        });

        // 發送按鈕事件（新增）
        sendButton.addActionListener(e -> sendInput());
    }

    private void updateOutputWithInput() {
        SwingUtilities.invokeLater(() -> {
            StyledDocument doc = outputPane.getStyledDocument();
            try {
                // 清除之前的输入预览
                String text = doc.getText(0, doc.getLength());
                int inputPreviewStart = text.lastIndexOf("\n") + 1;
                doc.remove(inputPreviewStart, doc.getLength() - inputPreviewStart);

                // 添加新的输入预览
                Style style = outputPane.addStyle("InputPreviewStyle", null);
                StyleConstants.setFontFamily(style, inputFont.getFamily());
                StyleConstants.setFontSize(style, inputFont.getSize());
                StyleConstants.setItalic(style, true);
                StyleConstants.setForeground(style, Color.GRAY);  // 使用灰色来区分预览
                doc.insertString(doc.getLength(), inputField.getText(), style);
            } catch (BadLocationException e) {
                e.printStackTrace();
            }
        });
    }

//    輸出框文字顯示
    private void sendInput() {
        if (runningProcess != null) {
            String input = inputField.getText();
            if (!input.isEmpty()) {
                try {
                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(runningProcess.getOutputStream()));
                    writer.write(input);
                    writer.newLine();
                    writer.flush();

                    // 将预览转换为正式输出
                    StyledDocument doc = outputPane.getStyledDocument();
                    Style style = outputPane.addStyle("InputStyle", null);
                    StyleConstants.setFontFamily(style, inputFont.getFamily());
                    StyleConstants.setFontSize(style, inputFont.getSize());
                    StyleConstants.setItalic(style, false);
                    StyleConstants.setForeground(style, Color.BLACK);  // 使用黑色表示正式输入

                    // 移除预览文本
                    String text = doc.getText(0, doc.getLength());
                    int inputPreviewStart = text.lastIndexOf("\n") + 1;
                    doc.remove(inputPreviewStart, doc.getLength() - inputPreviewStart);

                    // 添加正式输入文本
                    doc.insertString(doc.getLength(), input + "\n", style);

                    // 清空输入框
                    inputField.setText("");

                    // 滚动到最底部
                    outputPane.setCaretPosition(doc.getLength());
                } catch (IOException | BadLocationException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    private void appendToOutput(String text, Font font) {
        SwingUtilities.invokeLater(() -> {
            StyledDocument doc = outputPane.getStyledDocument();
            Style style = outputPane.addStyle("TempStyle", null);
            StyleConstants.setFontFamily(style, font.getFamily());
            StyleConstants.setFontSize(style, font.getSize());
            StyleConstants.setItalic(style, font.isItalic());
            StyleConstants.setBold(style, font.isBold());

            try {
                doc.insertString(doc.getLength(), text, style);
            } catch (BadLocationException e) {
                e.printStackTrace();
            }

            outputPane.setCaretPosition(doc.getLength());
        });
    }

    private void setModified(boolean modified) {
        isModified = modified;
        updateTitle();
        if (modified) {
            stateBar.setText("已修改");
        } else {
            stateBar.setText("未修改");
        }
    }

    private void updateTitle() {
        String title = getTitle();
        if (title.endsWith("*")) {
            title = title.substring(0, title.length() - 1);
        }
        if (isModified) {
            title += "*";
        }
        setTitle(title);
    }

    private void newFile() {
        if (isModified) {
            int option = JOptionPane.showConfirmDialog(
                    this,
                    "當前文件已被修改，是否保存？",
                    "保存文件",
                    JOptionPane.YES_NO_CANCEL_OPTION,
                    JOptionPane.WARNING_MESSAGE
            );

            if (option == JOptionPane.YES_OPTION) {
                if (!saveFileAs()) {
                    return; // 如果保存失敗或取消，則不繼續創建新文件
                }
            } else if (option == JOptionPane.CANCEL_OPTION) {
                return; // 用戶選擇取消，不創建新文件
            }
        }

        String fileName = JOptionPane.showInputDialog(this, "请输入新文件名称：", "新建文件", JOptionPane.PLAIN_MESSAGE);

        if (fileName == null) {
            return;

        } else if (fileName.isEmpty() || fileName.equalsIgnoreCase("main.Main")) {
            JOptionPane.showMessageDialog(this, "不允許使用空名稱或 'main.Main' 作為文件名", "無效的文件名", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // 確保文件名以 .java 結尾
        if (!fileName.toLowerCase().endsWith(".java")) {
            fileName += ".java";
        }

        String defaultContent =
                "public class " + fileName.substring(0, fileName.length() - 5) + " {\n" +
                        "    public static void main(String[] args) {\n" +
                        "        System.out.println(\"Hello World\");\n" +
                        "    }\n" +
                        "}\n";

        textPane.setText(defaultContent);
        setTitle(fileName);
        setModified(false);
        stateBar.setText("未修改");
        currentFile = null;  // 重置 currentFile
        isSaved = false;     // 設置為未保存狀態

        if (saveFileAs()) {
            setModified(false);
            stateBar.setText("已保存");
        } else {
            JOptionPane.showMessageDialog(this, "新文件未保存，某些功能可能受限。", "警告", JOptionPane.WARNING_MESSAGE);
        }
    }


    private void openFile() {
        if (isModified) {
            int option = JOptionPane.showConfirmDialog(
                    this,
                    "當前文件已被修改，是否保存？",
                    "保存文件",
                    JOptionPane.YES_NO_CANCEL_OPTION,
                    JOptionPane.WARNING_MESSAGE
            );

            if (option == JOptionPane.YES_OPTION) {
                if (!saveFileAs()) {
                    return; // 如果保存失敗或取消，則不繼續打開新文件
                }
            } else if (option == JOptionPane.CANCEL_OPTION) {
                return; // 用戶選擇取消，不打開新文件
            }
        }

        int option = fileChooser.showOpenDialog(this);

        if (option == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            System.out.println("selectedFile=" + selectedFile);
            //確保該java檔不會因為路徑而被IDE擋住
            boolean ignorePackageError = selectedFile.getAbsolutePath().contains(File.separator + "code" + File.separator + "test" + File.separator);
            openFile(selectedFile, ignorePackageError);

        }
    }

    public void openFile(File file, boolean ignorePackageError) {
        try {
            String content = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
            textPane.setText(content);
            currentFile = file;
            System.out.println(currentFile);
            setTitle(file.getName());
            setModified(false);
            isSaved = true;

            if (!ignorePackageError) {
                checkPackageStatement(content, file);
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error opening file: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void checkPackageStatement(String content, File file) {
        String expectedPackage = getExpectedPackage(file);
        String actualPackage = extractPackage(content);

        if (!expectedPackage.equals(actualPackage)) {
            int option = JOptionPane.showConfirmDialog(
                    this,
                    "檔案的 package 聲明可能不正確。\n" +
                            "預期的 package: " + expectedPackage + "\n" +
                            "實際的 package: " + actualPackage + "\n" +
                            "是否要自動修正？",
                    "Package 聲明不正確",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE
            );

            if (option == JOptionPane.YES_OPTION) {
                String correctedContent = content.replaceFirst(
                        "package\\s+[\\w.]+;",
                        "package " + expectedPackage + ";"
                );
                textPane.setText(correctedContent);
                setModified(true);
            }
        }
    }

    private String getExpectedPackage(File file) {
        String path = file.getAbsolutePath();
        int srcIndex = path.indexOf("src");
        if (srcIndex != -1) {
            String packagePath = path.substring(srcIndex + 4, path.lastIndexOf(File.separator));
            return packagePath.replace(File.separator, ".");
        }
        return "";
    }

    private String extractPackage(String content) {
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("package\\s+([\\w.]+);");
        java.util.regex.Matcher matcher = pattern.matcher(content);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return "";
    }


    private boolean saveFile() {
        if (currentFile == null) {
            return saveFileAs();
        }
        try {
            Files.write(currentFile.toPath(), textPane.getText().getBytes(StandardCharsets.UTF_8));
            stateBar.setText("已保存");
            setModified(false);
            isSaved = true;
            return true;
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, e.toString(),
                    "寫入文件失敗", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    private boolean saveFileAs() {
        File defaultSaveDir = new File(Config.getTestPath());
        fileChooser.setCurrentDirectory(defaultSaveDir);
        FileNameExtensionFilter javaFilter = new FileNameExtensionFilter("Java Files (*.java)", "java");
        fileChooser.setFileFilter(javaFilter);

        File suggestedFile = new File(getTitle());
        if (!suggestedFile.getName().equals("Main.java")) {
            fileChooser.setSelectedFile(suggestedFile);
        } else {
            fileChooser.setSelectedFile(new File("Main.java"));
        }

        int option = fileChooser.showSaveDialog(this);

        if (option == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            // 确保文件保存在 test 目录下
            if (!file.getParentFile().equals(defaultSaveDir)) {
                file = new File(defaultSaveDir, file.getName());
            }
            if (!file.getName().toLowerCase().endsWith(".java")) {
                file = new File(file.getParentFile(), file.getName() + ".java");
            }

            try {
                file.createNewFile();
                try (BufferedWriter buf = new BufferedWriter(new FileWriter(file))) {
                    buf.write(textPane.getText());
                }
                currentFile = file;  // 更新 currentFile
                setTitle(file.getName());
                setModified(false);
                stateBar.setText("已保存");
                notifyFileCreated(file.getName());
                isSaved = true;
                return true;
            } catch (IOException e) {
                JOptionPane.showMessageDialog(null, e.toString(),
                        "无法建立或储存档案", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }
        return false;
    }

    private void closeFile() {
        String content = textPane.getText().trim();
        if (isModified && !content.isEmpty()) {
            int option = JOptionPane.showConfirmDialog(
                    this,
                    "檔案已修改，是否儲存？",
                    "儲存檔案？",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE
            );

            switch (option) {
                case JOptionPane.YES_OPTION:
                    if (getTitle().equals("新增文字檔案")) {
                        if (!saveFileAs()) {
                            // 用戶取消了儲存操作，不關閉視窗
                            return;
                        }
                    } else {
                        saveFile();
                    }
                    dispose();
                    break;
                case JOptionPane.NO_OPTION:
                    dispose();
                    break;
                case JOptionPane.CLOSED_OPTION:
                    // 用戶取消或關閉對話框，不執行任何操作
                    return;
            }
        } else {
            // 文件未修改或為空，直接關閉
            dispose();
        }
    }

    private void cut() {
        textPane.cut();
        stateBar.setText("已修改");
        popUpMenu.setVisible(false);
    }

    private void copy() {
        textPane.copy();
        popUpMenu.setVisible(false);
    }

    private void paste() {
        textPane.paste();
        stateBar.setText("已修改");
        popUpMenu.setVisible(false);
    }

    private void processTextArea() {
        stateBar.setText("已修改");
    }

    //20240812 final edit EDT 確保線程處於安全狀態
    private void runCurrentFile() {
        SwingUtilities.invokeLater(() -> {
            outputPane.setText("");
            inputField.setText("");
            inputField.setEnabled(true);
            sendButton.setEnabled(true);
        });
        //緩衝區清空
        outputBuffer.setLength(0);

        Compile.compileAndRun(currentFile, isModified, this::saveFile).thenAccept(result -> {
            if (result instanceof Process) {
                runningProcess = (Process) result;

                Compile.handleProcessOutput(runningProcess, line ->
                        appendToOutput(line + "\n", outputPane.getFont()));

                Compile.handleProcessInput(runningProcess);

                try {
                    runningProcess.waitFor();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else {
                SwingUtilities.invokeLater(() ->
                        JOptionPane.showMessageDialog(this, result.toString(), "錯誤", JOptionPane.ERROR_MESSAGE));
            }
            SwingUtilities.invokeLater(() -> {
                runButton.setEnabled(true);
                stopButton.setEnabled(false);
            });
        });
    }

    public void dispose() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(800, TimeUnit.MILLISECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
        }
        super.dispose();
    }

    private void stopRunningProcess() {
        if (runningProcess != null) {
            runningProcess.destroyForcibly();
            runningProcess = null;
            SwingUtilities.invokeLater(() -> {
                runButton.setEnabled(true);
                stopButton.setEnabled(false);
            });
        }
    }

    public interface FileChangeListener {
        void onFileCreated(String fileName);
    }
}