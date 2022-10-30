package gccBin.MidCode;

import SymbolTableBin.TableSymbol;
import gccBin.MidCode.Line.*;
import gccBin.MidCode.firstProcess.MidCodeFirst;

import java.util.ArrayList;

/**
 * 参数会和关键字重合吗？
 */
public class MidCodeLines {
    private static MidCodeLines midCodeLines;

    private final ArrayList<Line> lines; //第一次扫描之后的中间代码。
    private int index ;

    private MidCodeLines(){
        lines = new ArrayList<>();
        index = 0;
    }

    public static MidCodeLines getInstance(){
        if(midCodeLines == null){
            midCodeLines = new MidCodeLines();
        }
        return midCodeLines;
    }

    public Line addLines(String line, TableSymbol tableSymbol){
        String[] elements = line.split(" ");
        if(equ(3,elements,0,"arr")){
            ArrayDefLine a =  new ArrayDefLine(line,index++,tableSymbol,elements);
            this.lines.add(a);
            return a;
        } else if (equ(3,elements,0,"var")) {
            VarDeclLine a = new VarDeclLine(line,index++,tableSymbol,elements);
            this.lines.add(a);
            return a;
        } else if(lines.equals("{")){
            MidCodeFirst.getInstance().inTableSymbol();
            return null;
        } else if (lines.equals("}")) {
            MidCodeFirst.getInstance().leaveTableSymbol();
            return null;
        } else if (equ(2,elements, 0, "b", "bge", "ble", "bgt", "blt", "bne", "beq")) {
            BLine a = new BLine(line,index++,tableSymbol,elements);
            this.lines.add(a);
            return a;
        } else if (equ(3,elements,0,"cmp")) {
            CmpLine a = new CmpLine(line,index++,tableSymbol,elements);
            this.lines.add(a);
            return a;
        } else if (elements.length == 1 && isLabel(line)) {
            LabelLine a = new LabelLine(line,index++,tableSymbol,elements);
            this.lines.add(a);
            return a;
        } else if (ifFuncDef(elements)) {
            FuncDefLine a = new FuncDefLine(line,index++,tableSymbol,elements);
            this.lines.add(a);
            return a;
        } else if (isFParam(elements)) {
            FParamDefLine a = new FParamDefLine(line,index++,tableSymbol,elements);
            this.lines.add(a);
            return a;
        } else if (equ(2,elements,0,"push")) {
            PushLine a = new PushLine(line,index++,tableSymbol,elements);
            this.lines.add(a);
            return a;
        } else if (equ(2,elements,0,"call")) {
            CallFunc a = new CallFunc(line,index++,tableSymbol,elements);
            this.lines.add(a);
            return a;
        } else if (isRetLine(elements)) {
            RetLine a = new RetLine(line,index++,tableSymbol,elements);
            this.lines.add(a);
            return a;
        } else if (equ(2,elements,0,"scanf")) {
            ScanfLine a = new ScanfLine(line,index++,tableSymbol,elements);
            this.lines.add(a);
            return a;
        } else if (equ(2,elements,0,"printf")) {
            PrintfLine a = new PrintfLine(line,index++,tableSymbol,elements);
            this.lines.add(a);
            return a;
        } else if (equ(elements,1,"=")) {
            AssignLine a = new AssignLine(line,index++,tableSymbol,elements);
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

    public boolean equ(int len ,String[] arr, int pos, String... str) {
        if(arr.length!=len) return false;
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

    private boolean isLabel(String s){
        return s.charAt(s.length()-1) == ':';
    }

    private boolean ifFuncDef(String[] element){
        if(element.length!=3) return false;
        return element[2].equals("()");
    }

    private boolean isFParam(String[] ele){
        return equ(3,ele,0,"para") ||
                equ(4,ele,0,"para");
    }

    private boolean isRetLine(String[] ele){
        return equ(1,ele,0,"ret") ||
                equ(2,ele,0,"ret");
    }

}
