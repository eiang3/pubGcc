package gccBin.MidCode.original;

import GramTree.Element.Exp;
import GramTree.Element.InitVal;
import GramTree.InheritProp;
import GramTree.Param;
import GramTree.TreeElement;
import GramTree.Word;
import SymbolTableBin.*;
import SymbolTableBin.Element.ElementFunc;
import SymbolTableBin.Element.ElementTable;
import gccBin.Lex.Symbol;
import gccBin.MidCode.Judge;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class IRGenerate {
    private static IRGenerate instance;
    private FileWriter fileWriter;

    private TreeElement root;

    private IRGenerate() {
    }

    public static IRGenerate getInstance() {
        if (instance == null) {
            instance = new IRGenerate();
        }
        return instance;
    }

    public void open() throws IOException {
        File file = new File("midCode.txt");
        this.fileWriter = new FileWriter(file.getName());
    }

    public void close() throws IOException {
        this.fileWriter.close();
    }

    public void setRoot(TreeElement root) {
        this.root = root;
    }

    public void begin() throws IOException {
        Param param = new Param();
        this.root.midCodeGen(this.fileWriter, param);
    }

    public void constDefArray(String name, int len, ArrayList<ArrayList<Integer>> nums, TableSymbol tableSymbol, int falseRow) throws IOException {
        name = APIIRSymTable.getInstance().findElementIRGen(tableSymbol, name, falseRow).getIRName();
        write("&arr int " + name + "[" + len + "]\n");
        int index = 0;
        for (ArrayList<Integer> integers : nums) {
            for (Integer i : integers) {
                write(name + "[" + (index++) + "]" + " = " + i + "\n");
            }
        }
    }

    public void varDef(TableSymbol tableSymbol, String name, int len, InitVal initVal, int falseRow) throws IOException {
        name = APIIRSymTable.getInstance().findElementIRGen(tableSymbol, name, falseRow).getIRName();
        Param param = new Param();
        if (len <= 0) {
            write("&var int " + name + "\n");
            if (initVal != null) {
                ArrayList<Exp> exps = initVal.getExps();
                exps.get(0).midCodeGen(fileWriter, param);
                write(name + " = " + initVal.getExps().get(0).getMidCode() + "\n");
            }
            return;
        }

        write("&arr int " + name + "[" + len + "]\n");
        int index = 0;
        if (initVal != null) {
            ArrayList<Exp> exps = initVal.getExps();
            for (Exp exp : exps) {
                exp.midCodeGen(fileWriter, param);
                write(name + "[" + (index++) + "]" + " = " + exp.getMidCode() + "\n");
            }
        }
    }

    public String lValRParam(TableSymbol tableSymbol, String name, int falseRow) throws IOException {
        ElementTable elementTable = APIIRSymTable.getInstance()
                .findElementIRGen(tableSymbol, name, falseRow);
        String oldName = name;
        name = elementTable.getIRName();
        if (elementTable.getDimension() == 0) {  //保存的是值
            return lValNormal(tableSymbol, oldName, new Param(), falseRow); //实参传值
        } else {
            String ans = IRTagManage.getInstance().newVar();
            write(ans + " = " + name + " >> 2\n");  // 除4
            return ans;
        }
    }

    public String lValRParam(TableSymbol tableSymbol, String name, String one, int falseRow) throws IOException {
        ElementTable elementTable =
                APIIRSymTable.getInstance().findElementIRGen(tableSymbol, name, falseRow);
        String oldName = name;
        name = elementTable.getIRName();
        if (elementTable.getDimension() == 1) {  //保存的是值
            return lValNormal(oldName, one, new Param(), tableSymbol, falseRow);
        } else if (elementTable.getDimension() == 2) {
            int len = APIIRSymTable.getInstance().findTwoDimArrayLen(
                    tableSymbol, oldName, falseRow);
            String t1 = IRTagManage.getInstance().newVar();
            write(t1 + " = " + one + " * " + len + "\n");
            String t2 = IRTagManage.getInstance().newVar();
            String ans = IRTagManage.getInstance().newVar();
            write(t2 + " = " + name + " >> 2" + "\n"); // 除4
            write(ans + " = " + t1 + " + " + t2 + "\n");
            return ans;
        } else return null;
    }

    //2维一定是数值
    public String lValRParam(TableSymbol tableSymbol, String name, String one, String two, int falseRow) throws IOException {
        int len = APIIRSymTable.getInstance().findTwoDimArrayLen(
                tableSymbol, name, falseRow);
        return lValNormal(name, one, two, len, new Param(), tableSymbol, falseRow);
    }

    public String lValNormal(TableSymbol tableSymbol, String name, Param param, int falseRow) throws IOException {
        name = APIIRSymTable.getInstance()
                .findElementIRGen(tableSymbol, name, falseRow).getIRName();
        if (param.getExpKind() == InheritProp.LValAssign) {
            return name;
        } else {
            String t1 = IRTagManage.getInstance().newVar();
            write(t1 + " = " + name + "\n");
            return t1;
        }
    }


    public String lValNormal(String name, String one, String two, int len, Param param, TableSymbol tableSymbol, int falseRow) throws IOException {
        name = APIIRSymTable.getInstance().findElementIRGen(tableSymbol, name, falseRow).getIRName();
        String t1 = IRTagManage.getInstance().newVar();
        write(t1 + " = " + one + " * " + len + "\n");
        String t2 = IRTagManage.getInstance().newVar();
        write(t2 + " = " + two + " + " + t1 + "\n");
        if (param.getExpKind() == InheritProp.LValAssign) {
            return name + "[" + t2 + "]";
        } else {
            String ans = IRTagManage.getInstance().newVar();
            write(ans + " = " + name + "[" + t2 + "]\n");
            return ans;
        }
    }

    public String lValNormal(String name, String one, Param param, TableSymbol tableSymbol, int falseRow) throws IOException {
        name = APIIRSymTable.getInstance()
                .findElementIRGen(tableSymbol, name, falseRow).getIRName();
        if (param.getExpKind() == InheritProp.LValAssign) {
            return name + "[" + one + "]";
        } else {
            String ans = IRTagManage.getInstance().newVar();
            write(ans + " = " + name + "[" + one + "]\n");
            return ans;
        }
    }

    public String mulExpUnary(String mid, int coe, int negative) throws IOException {
        if (coe == 1 && negative == 1) return mid;
        if (Judge.isNumber(mid) && negative == 1) {
            return "-" + mid;
        }
        String ans = IRTagManage.getInstance().newVar();
        if (negative == -1) {  //如果有！的话，是不是1没什么关系了
            write(ans + " = ! " + mid + "\n");
        } else if (coe == -1) {
            write(ans + " = - " + mid + "\n");
        }
        return ans;
    }

    public String mulExpTwo(String mul, String una, int coe, int negative, Word op) throws IOException {
        String t2 = mulExpUnary(una, coe, negative);
        String ans = IRTagManage.getInstance().newVar();
        generateTwoExp(ans, mul, op.getToken(), t2);
        return ans;
    }

    public String addExpTwo(String add, String mul, Word op) throws IOException {
        String ans = IRTagManage.getInstance().newVar();
        generateTwoExp(ans, add, op.getToken(), mul);
        return ans;
    }

    public void inBlock() throws IOException {
        write("{\n");
    }

    public void leaveBlock() throws IOException {
        write("}\n");
    }

    public void b_label(String label) throws IOException {
        write("b " + label + "\n");
    }

    public String condJump(String op1, Word op, String op2) throws IOException {
        String subAns = IRTagManage.getInstance().newVar();
        if (op.getSym() == Symbol.LSS || op.getSym() == Symbol.LEQ) {
            myWrite(subAns + " = " + op1 + " - " + op2);
        } else {
            myWrite(subAns + " = " + op2 + " - " + op1);
        }

        String ret = IRTagManage.getInstance().newVar();
        // a - b < 0 置1
        if (op.getSym() == Symbol.LSS || op.getSym() == Symbol.GRE) {
            myWrite(ret + " = " + subAns + " >> 31");
        } //a - b <= 0 置一
        else if (op.getSym() == Symbol.LEQ || op.getSym() == Symbol.GEQ) {
            String label1 = IRTagManage.getInstance().newLabel();
            String label2 = IRTagManage.getInstance().newLabel();
            myWrite("&cmp " + subAns + " 0");
            myWrite("ble " + label1);
            myWrite(ret + " = 0");
            b_label(label2);
            localLabel(label1);
            myWrite(ret + " = 1");
            localLabel(label2);
        } //a - b == 0 置1
        else if (op.getSym() == Symbol.EQL) {
            String label1 = IRTagManage.getInstance().newLabel();
            String label2 = IRTagManage.getInstance().newLabel();
            myWrite("&cmp " + subAns + " 0");
            myWrite("beq " + label1);
            myWrite(ret + " = 0");
            b_label(label2);
            localLabel(label1);
            myWrite(ret + " = 1");
            localLabel(label2);
        } // a - b != 0 置1
        else if (op.getSym() == Symbol.NEQ) {
            String label1 = IRTagManage.getInstance().newLabel();
            String label2 = IRTagManage.getInstance().newLabel();
            myWrite("&cmp " + subAns + " 0");
            myWrite("bne " + label1);
            myWrite(ret + " = 0");
            b_label(label2);
            localLabel(label1);
            myWrite(ret + " = 1");
            localLabel(label2);
        }
        /*write("&cmp " + op1 + " " + op2 + "\n");
        if (op.getSym() == Symbol.LSS) { // <
            write("bge " + label + "\n");
        } else if (op.getSym() == Symbol.GRE) { // >
            write("ble " + label + "\n");
        } else if (op.getSym() == Symbol.LEQ) { // <=
            write("bgt " + label + "\n");
        } else if (op.getSym() == Symbol.GEQ) { //>=
            write("blt " + label + "\n");
        } else if (op.getSym() == Symbol.EQL) { //==
            write("bne " + label + "\n");
        } else if (op.getSym() == Symbol.NEQ) { // !=
            write("beq " + label + "\n");
        }*/
        return ret;
    }

    public void b_false(String ir, String label) throws IOException {
        myWrite("&cmp " + ir + " 0");
        myWrite("beq " + label);
    }

    public void localLabel(String label) throws IOException {
        write(label + ":\n");
    }

    private void generateTwoExp(String ans, String t1, String op, String t2) throws IOException {
        if (!Judge.isNumber(t1) && Judge.isNumber(t2) && op.equals("/")) {
            generateTwo_div_number(ans, t1, t2);
            return;
        } else if (!Judge.isNumber(t1) && Judge.isNumber(t2) && op.equals("*")) {
            int twoTimes = Judge.isTimesTwo(Integer.parseInt(t2));
            if (twoTimes != -1) {
                myWrite(ans + " = " + t1 + " << " + twoTimes);
                return;
            }
        } else if (!Judge.isNumber(t1) && Judge.isNumber(t2) && op.equals("%")) {
            String div = IRTagManage.getInstance().newVar();
            String result = IRTagManage.getInstance().newVar();
            String t1_copy = IRTagManage.getInstance().newVar();
            String number = t2;
            // 因为表达式的结果不是数字，就是temp，所以这里一定是temp
            myWrite(t1_copy + " = " + t1);
            generateTwo_div_number(div, t1, number);
            myWrite(result + " = " + div + " * " + number);
            myWrite(ans + " = " + t1_copy + " - " + result);
            return;
        }
        myWrite(ans + " = " + t1 + " " + op + " " + t2);
    }

    private void generateTwo_div_number(String ans, String t1, String t2) throws IOException {
        int div = Integer.parseInt(t2);
        int twoTimes = Judge.isTimesTwo(div);
        if (twoTimes != -1) {
            myWrite(ans + " = " + t1 + " >> " + twoTimes);
            return;
        }
        long M = ((long) (Math.pow(2, 33)) + 3) / div;
        String temp = IRTagManage.getInstance().newVar();
        myWrite(temp + " = " + t1 + " * " + M);
        myWrite(ans + " = " + temp + " >> 33");
    }

    public void funcDef(TypeTable returnType, String name) throws IOException {
        write(returnType.toString().toLowerCase() + " " + name + " ()\n");
    }

    public void funcFParam(TypeTable type, String name, int dim, TableSymbol tableSymbol, int falseRow) throws IOException {
        name = APIIRSymTable.getInstance().findElementIRGen(tableSymbol, name, falseRow).getIRName();
        if (dim == 0) {
            write("&para " + type.toString().toLowerCase() + " " + name + "\n");
        } else {
            write("&para " + type.toString().toLowerCase() + " " + name + " []\n");
        }
    }

    public String funcCall(String funcName, ArrayList<Exp> exps) throws IOException {
        for (Exp exp : exps) {
            write("&push " + exp.getMidCode() + "\n");
        }
        return funcCall(funcName);
    }

    public String funcCall(String funcName) throws IOException {
        write("&call " + funcName + "\n");
        ElementFunc elementFunc = APIIRSymTable.getInstance().getFuncElement(funcName);
        if (elementFunc.getReturnType() == TypeTable.INT) {
            String t1 = IRTagManage.getInstance().newVar();
            write(t1 + " = $RET\n");
            return t1;
        }
        return null;
    }


    public void returnStmt(String exp) throws IOException {
        write("&ret " + exp + "\n");
    }

    public void returnStmt() throws IOException {
        write("&ret" + "\n");
    }

    public void mainRetStmt() throws IOException {
        write("&ret main\n");
    }

    public void assignStmtScanf(String lVal) throws IOException {
        String t1 = IRTagManage.getInstance().newVar();
        write("&scanf " + t1 + "\n");
        assignStmtExp(lVal, t1);
    }

    public void assignStmtExp(String left, String right) throws IOException {
        write(left + " = " + right + "\n");
    }

    public void write(String str) throws IOException {
        fileWriter.write(str);
        //System.out.print(str);
    }

    public void myWrite(String str) throws IOException {
        fileWriter.write(str + "\n");
        //System.out.print(str);
    }

    public void LAndExpSignalExp(String t0, String endLabel) throws IOException {
        write("&cmp " + t0 + " 0\n");
        write("beq " + endLabel + "\n");
    }

    //对于数组指针引用，最后的结果还需要乘4
    public String mulFour(String s) throws IOException {
        String ans = IRTagManage.getInstance().newVar();
        write(ans + " = " + s + " << 2\n");
        return ans;
    }

    public void annotate(String str) throws IOException {
        write("##" + str + "##\n");
    }
}
