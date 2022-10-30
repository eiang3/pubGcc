package gccBin.MidCode;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class PrintfFormatStringStore {
    private static PrintfFormatStringStore instance;

    private final ArrayList<String> formatsStrings;

    private PrintfFormatStringStore() {
        this.formatsStrings = new ArrayList<>();
    }

    public static PrintfFormatStringStore getInstance() {
        if (instance == null) {
            instance = new PrintfFormatStringStore();
        }
        return instance;
    }

    /**
     * 将语法分析中遇见的字符串按顺序存储(%d \n 分割 不包含空串)
     */
    public void parse(String str) {
        str = str.substring(1, str.length() - 1);
        ArrayList<String> s = mySplit(str);
        for (String a : s) {
            if (!a.equals("%d") && !a.equals("\n") && !a.equals("")) {
                this.formatsStrings.add(a);
            }
        }
    }

    public ArrayList<String> getFormatsStrings() {
        return formatsStrings;
    }

    //
    private static int i = 0;

    public void midCodePrintf(FileWriter fileWriter, String str) throws IOException {
        if (str.equals("\n")) {
            fileWriter.write("printf str_" + "\n");
        } else if (str.equals("%d")) {
            fileWriter.write("printf " + str + "\n");
        } else {
            fileWriter.write("printf str_" + i + "\n");
            i++;
        }
    }


    public ArrayList<String> mySplit(String str) { //以\n和%d分隔字符，并且保存\n和%d
        ArrayList<String> ans = new ArrayList<>();
        int index = 0;
        int before = 0;
        while (index < str.length()) {
            int iNext = str.indexOf("\\n", index);
            int iNum = str.indexOf("%d", index);
            if (iNext < iNum && iNext != -1 || iNext != -1 && iNum == -1) {
                //下一个是 \n
                index = iNext;
                if (before <= index - 1) {
                    ans.add(str.substring(before, index));
                }
                ans.add("\n");
                before = index + 2;
                index = index + 2;
            } else if (iNum < iNext && iNum != -1 || iNum != -1 && iNext == -1) {
                index = iNum;
                if (before <= index - 1) {
                    ans.add(str.substring(before, index));
                }
                ans.add("%d");
                before = index + 2;
                index += 2;
            } else {
                ans.add(str.substring(index)); //
                break;
            }
        }
        return ans;
    }
}
