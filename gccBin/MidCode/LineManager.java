package gccBin.MidCode;

import SymbolTableBin.TableSymbol;
import gccBin.MidCode.Line.*;
import gccBin.MidCode.firstProcess.IRFirst;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.BitSet;

/**
 * ok
 * ir ___ 中间代码
 * 参数会和关键字重合吗？
 */
public class LineManager {
    private static LineManager midCodeLines;


    /**
     * lines: 第一次扫描之后的中间代码
     * numLine:lines的个数
     * ergodicIndex:遍历lines时使用的下标
     */

    private final ArrayList<Line> lines;
    private int numLine;
    private int ergodicIndex;

    private LineManager() {
        lines = new ArrayList<>();
        numLine = 0;
    }

    public static LineManager getInstance() {
        if (midCodeLines == null) {
            midCodeLines = new LineManager();
        }
        return midCodeLines;
    }

    /**
     * 对line的一个遍历方法
     */
    public void beginErgodic() {
        ergodicIndex = 0;
    }

    /**
     * 下一条line
     *
     * @return 下一条line
     */
    public Line nextLine() {
        return lines.get(ergodicIndex++);
    }

    public Line getNowLine(){
        return lines.get(ergodicIndex-1);
    }
    /**
     * 回退一条line
     */
    public void retract() {
        ergodicIndex--;
    }

    /**
     * 判断是否遍历完毕
     *
     * @return 返回是否遍历完毕
     */
    public boolean hasNext() {
        return ergodicIndex < numLine;
    }

    public Line addLines(String line, TableSymbol tableSymbol) {
        String[] elements = line.split(" ");
        if (equ(3, elements, 0, "&arr")) {
            ArrayDefLine a = new ArrayDefLine(line, numLine++, tableSymbol, elements);
            this.lines.add(a);
            return a;
        } else if (equ(3, elements, 0, "&var")) {
            VarDeclLine a = new VarDeclLine(line, numLine++, tableSymbol, elements);
            this.lines.add(a);
            return a;
        } else if (line.equals("{")) {
            IRFirst.getInstance().inTableSymbol();
            return null;
        } else if (line.equals("}")) {
            IRFirst.getInstance().leaveTableSymbol();
            return null;
        } else if (equ(2, elements, 0, "b", "bge", "ble", "bgt", "blt", "bne", "beq")) {
            BLine a = new BLine(line, numLine++, tableSymbol, elements);
            this.lines.add(a);
            return a;
        } else if (equ(3, elements, 0, "&cmp")) {
            CmpLine a = new CmpLine(line, numLine++, tableSymbol, elements);
            this.lines.add(a);
            return a;
        } else if (elements.length == 1 && isLabel(line)) {
            LabelLine a = new LabelLine(line, numLine++, tableSymbol, elements);
            this.lines.add(a);
            return a;
        } else if (ifFuncDef(elements)) {
            FuncDefLine a = new FuncDefLine(line, numLine++, tableSymbol, elements);
            this.lines.add(a);
            return a;
        } else if (isFParam(elements)) {
            FParamDefLine a = new FParamDefLine(line, numLine++, tableSymbol, elements);
            this.lines.add(a);
            return a;
        } else if (equ(2, elements, 0, "&push")) {
            PushLine a = new PushLine(line, numLine++, tableSymbol, elements);
            this.lines.add(a);
            return a;
        } else if (equ(2, elements, 0, "&call")) {
            CallFuncLine a = new CallFuncLine(line, numLine++, tableSymbol, elements);
            this.lines.add(a);
            return a;
        } else if (isRetLine(elements)) {
            RetLine a = new RetLine(line, numLine++, tableSymbol, elements);
            this.lines.add(a);
            return a;
        } else if (equ(2, elements, 0, "&scanf")) {
            ScanfLine a = new ScanfLine(line, numLine++, tableSymbol, elements);
            this.lines.add(a);
            return a;
        } else if (equ(2, elements, 0, "&printf")) {
            PrintfLine a = new PrintfLine(line, numLine++, tableSymbol, elements);
            this.lines.add(a);
            return a;
        } else if (equ(elements, 1, "=")) {
            AssignLine a = new AssignLine(line, numLine++, tableSymbol, elements);
            this.lines.add(a);
            return a;
        } else {
            return null;
        }
    }

    public boolean equ(String[] arr, int pos, String... str) {
        if (arr.length - 1 < pos) {
            return false;
        }
        for (String s : str) {
            if (arr[pos].equals(s)) {
                return true;
            }
        }
        return false;
    }

    public boolean equ(int len, String[] arr, int pos, String... str) {
        if (arr.length != len) return false;
        return equ(arr, pos, str);
    }

    private boolean isLabel(String s) {
        if (s == null || s.length() == 0) return false;
        return s.charAt(s.length() - 1) == ':';
    }

    private boolean ifFuncDef(String[] element) {
        if (element.length != 3) return false;
        return element[2].equals("()");
    }

    private boolean isFParam(String[] ele) {
        return equ(3, ele, 0, "&para") ||
                equ(4, ele, 0, "&para");
    }

    private boolean isRetLine(String[] ele) {
        return equ(1, ele, 0, "&ret") ||
                equ(2, ele, 0, "&ret");
    }

    /**
     * 如果相应的bitset位为1，则更新该位对应的line的gen变量名字
     */
    public void reGenNameLine(BitSet a, String old, String name) {
        int start = a.nextSetBit(0);
        if (start < 0) return;
        for (int i = start; i < a.length() && i >= 0; i++) {
            if (a.get(i)) {
                lines.get(i).renameGen(old, name);
            }
        }
    }

    /**
     * 如果相应的bitset位为1，则更新该位对应的line的use变量名字
     */
    public void reUseNameLine(BitSet a, String old, String name) {
        int start = a.nextSetBit(0);
        for (int i = start; i < a.length() && i >= 0; i++) {
            if (a.get(i)) {
                lines.get(i).renameUse(old, name);
            }
        }
    }

    public Line getLine(int index) {
        return lines.get(index);
    }

    public void printfLines() throws IOException {
        FileWriter fileWriter = new FileWriter("midcodeLinesSet.txt");
        for (Line line : lines) {
            fileWriter.write(line.getMidCodeLine() + "\n");
        }
        fileWriter.close();
    }

    public int getErgodicIndex() {
        return ergodicIndex;
    }
}
