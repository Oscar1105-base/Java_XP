package layout.App;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Stack;

// 0830 修復運算邏輯，並添加歷史紀錄
public class Calculator extends JFrame {
    private JTextArea result;
    private JTextArea history_result;
    private final int btn_nums = 20;
    private final JButton[] btn_sets = new JButton[btn_nums];
    private boolean done = true;

    Stack<Character> operatorStack = new Stack<>();
    Stack<Double> numberStack = new Stack<>();

    public Calculator() {
        super("計算機");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new GridLayout(1, 2, 2, 0));
        JPanel workSpace = new JPanel();
        workSpace.setBackground(Color.white);
        add(workSpace);
        JPanel historySpace = new JPanel();
        historySpace.setBackground(Color.white);
        add(historySpace);

        //左上顯示輸入字符
        workSpace.setLayout(new BorderLayout());
        result = new JTextArea(8, 10);
        result.setFont(new Font("微软雅黑", Font.BOLD, 25));
        result.setEditable(false);
        result.setLineWrap(true);
        workSpace.add(result, BorderLayout.NORTH);

        //下方按鈕 5*4 布局
        JPanel btnPanel = new JPanel();
        btnPanel.setLayout(new GridLayout(5, 4));
        for (int i = 0; i < btn_nums; i++) {
            String[] btn_text = {
                    "(", ")", "C", "÷",
                    "7", "8", "9", "x",
                    "4", "5", "6", "-",
                    "1", "2", "3", "+",
                    "±", "0", ".", "="
            };
            btn_sets[i] = new JButton(btn_text[i]);
            btn_sets[i].addActionListener(new ButtonMonitor());
            btn_sets[i].setFont(new Font("微软雅黑", Font.PLAIN, 20));
            btn_sets[i].setFocusable(false);

            btnPanel.add(btn_sets[i]);
        }
        workSpace.add(btnPanel);

        //右方歷史紀錄
        historySpace.setLayout(new BorderLayout());

        JLabel history_title = new JLabel("歷史紀錄");
        history_title.setFont(new Font("微软雅黑", Font.BOLD, 20));
        historySpace.add(history_title, BorderLayout.NORTH);

        JButton clear_all = new JButton("clear all");
        clear_all.setFont(new Font("微软雅黑", Font.BOLD, 15));
        clear_all.setFocusable(false);
        clear_all.addActionListener(e -> history_result.setText(""));

        history_result = new JTextArea();
        history_result.setFont(new Font("微软雅黑", Font.BOLD, 20));
        history_result.setEditable(false);
        history_result.setLineWrap(true);

        JScrollPane history_scroll = new JScrollPane(history_result);
        historySpace.add(history_scroll, BorderLayout.CENTER);

        setSize(800, 600);
        setMinimumSize(new Dimension(800, 600));
        setLocationRelativeTo(null);
        setFocusable(true);
        setVisible(true);
    }

    private void addNegative() {
        String exp = result.getText();

        //添加负号
        int ptr = exp.length() - 1;
        while (ptr >= 0 && exp.charAt(ptr) >= '0' && exp.charAt(ptr) <= '9') {
            ptr--;
        }
        String tmp = exp.substring(0, ptr + 1) + "(" + "-" + exp.substring(ptr + 1) + ")";
        result.setText(tmp);
    }

    private boolean checkLegality(char pre_char, char this_char) {
        // pre_char 為前一個位置的字符，this_char 為當前輸入的字符
        switch (this_char) {
            case '(': {
                return pre_char == ' ' || pre_char == '(' || pre_char == '+' || pre_char == '-' ||
                        pre_char == 'x' || pre_char == '÷';
            }
            case ')', '+', '-', 'x', '÷': {
                return pre_char == ')' || Character.isDigit(pre_char);
            }
            case '.':
            case '_': {
                return Character.isDigit(pre_char);
            }
            default: {
                return pre_char == ' ' || pre_char == '.' || pre_char == '(' || pre_char == '+' || pre_char == '-' ||
                        pre_char == 'x' || pre_char == '÷' || Character.isDigit(pre_char);
            }
        }
    }


    private boolean checkPriority(char top, char c) {
        //檢查前後運算子優先順序, top在前，c在後
        return top == '(' || (c == 'x' || c == '÷') && (top == '+' || top == '-');
    }

    private void getResult() {
        String exp = result.getText();
        ExpRes expRes = calculateExp(exp);

        int res_int = (int) expRes.res;
        String res_str = "";
        if (expRes.tag.equals("OK")) {
            if (expRes.res == 0) res_str += "0";
            else {
                if (expRes.res / res_int != 1) {
                    res_str += expRes.res;
                } else {
                    res_str += res_int;
                }
            }
        }

        if (expRes.tag.equals("OK")) {
            result.setText(res_str);
            String old_history = history_result.getText();

            // Get current system time
            LocalDateTime now = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String formattedDateTime = now.format(formatter);

            history_result.setText(old_history + exp + " = " + res_str + "\n-- " + formattedDateTime + " --\n\n");
        } else if (expRes.tag.equals("ERROR")) {
            result.setText(expRes.tag + " : " + expRes.msg);
        }

        done = true;
    }
    //運算子處理邏輯
    private ExpRes calculate() {
        char top = operatorStack.pop();
        double num1 = 0, num2 = 0, res = 0;
        num2 = numberStack.pop();

        if (!(top == '-' && numberStack.empty())) {
            num1 = numberStack.pop();
        }

        switch (top) {
            case '+':
                res = num1 + num2;
                break;
            case '-':
                res = num1 - num2;
                break;
            case 'x':
                res = num1 * num2;
                break;
            case '÷':
                if (num2 != 0) {
                    res = num1 / num2;
                } else {
                    return new ExpRes("ERROR", "Divided by 0", 0);
                }
                break;
            default:
                return new ExpRes("ERROR", "Unknown operator", 0);
        }

        numberStack.add(res);
        return new ExpRes("OK", "", res);
    }


    private ExpRes calculateExp(String exp) {
        operatorStack.clear();
        numberStack.clear();

        for (int i = 0; i < exp.length(); i++) {
            char cur = exp.charAt(i);
            if (cur == '(') {
                operatorStack.add(cur);
            } else if (cur == ')') {
                while (!operatorStack.empty() && operatorStack.peek() != '(') {
                    ExpRes res = calculate();
                    if (res.tag == "ERROR") {
                        return res;
                    }
                }
                operatorStack.pop();
            } else if (cur == '+' || cur == '-' || cur == 'x' || cur == '÷') {
                if (cur == '-' && exp.charAt(i - 1) == '(') { //給予負號
                    operatorStack.pop();
                    int j = i + 1;
                    while (j + 1 < exp.length() && ((exp.charAt(j + 1) >= '0' && exp.charAt(j + 1) <= '9') || exp.charAt(j + 1) == '.'))
                        j++;
                    String num = exp.substring(i + 1, j + 1);
                    numberStack.add(0 - Double.parseDouble(num));
                    i = j + 1;
                } else {
                    if (operatorStack.empty() || operatorStack.peek() == '(') operatorStack.add(cur);
                    else {
                        while (!operatorStack.empty() && !checkPriority(operatorStack.peek(), cur)) {
                            ExpRes res = calculate();
                            if (res.tag == "ERROR") {
                                return res;
                            }
                        }
                        operatorStack.add(cur);
                    }
                }
            } else if (cur >= '0' && cur <= '9') {
                //提取完整的数字
                int j = i;
                while (j + 1 < exp.length() && ((exp.charAt(j + 1) >= '0' && exp.charAt(j + 1) <= '9') || exp.charAt(j + 1) == '.'))
                    j++;
                String num = exp.substring(i, j + 1);
                numberStack.add(Double.parseDouble(num));
                i = j;
            } else continue;
        }

        while (!operatorStack.empty()) {
            ExpRes res = calculate();
            if (res.tag == "ERROR") {
                return res;
            }
        }
        return new ExpRes("OK", "", numberStack.pop());
    }

    class ButtonMonitor implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            JButton clickBtn = (JButton) e.getSource();
            for (int i = 0; i < btn_nums; i++) {
                if (clickBtn == btn_sets[i]) {
                    if (done) result.setText("");

                    if (i == 2) {//按下C(Clear)
                        result.setText("");
                        done = true;
                    } else if (i == 16) {//按下 ±
                        String exp = result.getText();
                        if (!exp.isEmpty() && checkLegality(exp.charAt(exp.length() - 1), '_')) {
                            addNegative();
                            done = false;
                        }
                    } else if (i == 19) {   //按下 '='
                        getResult();
                    } else {
                        String exp = result.getText();
                        char pre_char = ' ';
                        if (!exp.isEmpty()) {
                            pre_char = exp.charAt(exp.length() - 1);
                        }
                        if (checkLegality(pre_char, btn_sets[i].getText().charAt(0))) {
                            result.setText(exp + btn_sets[i].getText());
                            done = false;
                        }
                    }
                }
            }
        }
    }

    static class ExpRes {
        String tag;
        String msg;
        double res;

        public ExpRes(String tag, String msg, double res) {
            this.tag = tag;
            this.msg = msg;
            this.res = res;
        }

    }
}
