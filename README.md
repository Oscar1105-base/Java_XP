# Java_XP 簡介

用 Java swing 模擬 Windows XP 桌面的小型專案，目前仍在測試當中!

![GitHub 簡介](/sample.jpg)
> 2024/09/02 bata 1.0 edit

## 前情提要

使用 JBR-17 開發，支援 JDK 17 以上環境

- Windows 系統需先下載 VLC Player 64位元
- Linux 暫未開放

### 大綱

在這專案中，包含以下功能：

- Windows 系統預設工具
- 簡易 Java 編輯器
- 3 款小遊戲
- VLC、DJNative 第三方jar檔導入

### 如何知道主畫面有哪些功能?

點擊右下角⚙️設定，開啟🕶️開發模式，即可看到!!

![layout](/DEMO/layout.jpg)
> 目前功能顯示只有繪製綠線，文字顯示仍在修復中

---

## DEMO 展示

### 實現：簡易 Java 編輯器
> 可支援基本輸入輸出，目前僅用於JDK內建方法 & 計算不得超過10秒

![DEMO 1](/DEMO/Demo1.jpg)![DEMO 1-1](/DEMO/Demo1-1.jpg)

### 實現：便利貼牆
> 生成方塊採用InterFrame，隨內容自動縮放待實作

![DEMO 2-1](/DEMO/Demo2-1.jpg)![DEMO 2-2](/DEMO/Demo2-2.jpg)

### 實現：DJNative 靜態瀏覽器
> 僅支援非JS生成的靜態網站

![DEMO 3](/DEMO/Demo3.jpg)![DEMO 3-2](/DEMO/Demo3-2.jpg)

### 實現：VLC 撥放器(測試中)
> 基本撥放功能，沒有燒錄和另存新檔功能

![DEMO 4](/DEMO/Demo4.jpg)

## 第三方jar來源

- DJNative靜態瀏覽器 [sourceforge下載路徑](https://sourceforge.net/projects/djproject/files/DJ%20Native%20Swing/)
- VLCJ VLC撥放器  [JAR-download下載路徑](https://jar-download.com/?search_box=vlcj)
