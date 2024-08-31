package code;

import javax.swing.*;
import java.io.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class Compile {
    public static CompletableFuture<Object> compileAndRun(File currentFile, boolean isModified, Runnable saveFile) {
        return CompletableFuture.supplyAsync(() -> {
            if (currentFile == null || isModified) {
                SwingUtilities.invokeLater(saveFile);
                try {
                    Thread.sleep(100); // 給一點時間讓 saveFile 完成
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }

            if (currentFile == null) {
                return "無法找到文件路徑，請先保存文件";
            }

            String filePath = currentFile.getAbsolutePath();
            String classPath = currentFile.getParent();
            String className = currentFile.getName().replaceFirst("[.][^.]+$", "");

            try {
                // 將.java編譯成class，並且複製一份到code/test資料夾
                ProcessBuilder compileBuilder = new ProcessBuilder("javac", "-encoding", "UTF-8", "-cp", classPath, filePath);
                compileBuilder.redirectErrorStream(true);
                Process compileProcess = compileBuilder.start();

                StringBuilder compileOutput = new StringBuilder();
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(compileProcess.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        compileOutput.append(line).append("\n");
                    }
                }
                //設置超時10秒會中斷Jnote程式運行
                boolean compileFinished = compileProcess.waitFor(10, TimeUnit.SECONDS);

                if (!compileFinished) {
                    compileProcess.destroyForcibly();
                    return "編譯超時";
                }

                if (compileProcess.exitValue() != 0) {
                    return "編譯錯誤:\n" + compileOutput.toString();
                }

                // 執行.class
                ProcessBuilder runBuilder = new ProcessBuilder("java", "-Dfile.encoding=UTF-8", "-cp", classPath, className);
                runBuilder.redirectErrorStream(true);
                return runBuilder.start();
            } catch (Exception e) {
                return "執行錯誤: " + e.getMessage();
            }
        });
    }

    //處理輸出訊息
    public static void handleProcessOutput(Process process, Consumer<String> outputHandler) {
        new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    outputHandler.accept(line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }
    //處理輸入訊息
    public static void handleProcessInput(Process process) {
        new Thread(() -> {
            try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
                 BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in))) {
                String input;
                while ((input = consoleReader.readLine()) != null) {
                    writer.write(input);
                    writer.newLine();
                    writer.flush();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }
}