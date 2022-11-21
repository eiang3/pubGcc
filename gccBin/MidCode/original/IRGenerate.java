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
        myWrite("&arr int " + name + "[" + len + "]");
        int index = 0;
        for (ArrayList<Integer> integers : nums) {
            for (Integer i : integers) {
                myWrite(name + "[" + (index++) + "]" + " = " + i);
            }
        }
    }

    public void varDef(TableSymbol tableSymbol, String name, int len, InitVal initVal, int falseRow) throws IOException {
        name = APIIRSymTable.getInstance().findElementIRGen(tableSymbol, name, falseRow).getIRName();
        Param param = new Param();
        if (len <= 0) {
            myWrite("&var int " + name);
            if (initVal != null) {
                ArrayList<Exp> exps = initVal.getExps();
                exps.get(0).midCodeGen(fileWriter, param);
                myWrite(name + " = " + initVal.getExps().get(0).getMidCode());
            }
            return;
        }

        myWrite("&arr int " + name + "[" + len + "]");
        int index = 0;
        if (initVal != null) {
            ArrayList<Exp> exps = initVal.getExps();
            for (Exp exp : exps) {
                exp.midCodeGen(fileWriter, param);
                myWrite(name + "[" + (index++) + "]" + " = " + exp.getMidCode());
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
            myWrite(ans + " = " + name + " >> 2");  // 除4
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
            myWrite(t1 + " = " + one + " * " + len);
            String t2 = IRTagManage.getInstance().newVar();
            String ans = IRTagManage.getInstance().newVar();
            myWrite(t2 + " = " + name + " >> 2"); // 除4
            myWrite(ans + " = " + t1 + " + " + t2);
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
            myWrite(t1 + " = " + name);
            return t1;
        }
    }


    public String lValNormal(String name, String one, String two, int len, Param param, TableSymbol tableSymbol, int falseRow) throws IOException {
        name = APIIRSymTable.getInstance().findElementIRGen(tableSymbol, name, falseRow).getIRName();
        String t1 = IRTagManage.getInstance().newVar();
        myWrite(t1 + " = " + one + " * " + len);
        String t2 = IRTagManage.getInstance().newVar();
        myWrite(t2 + " = " + two + " + " + t1);
        if (param.getExpKind() == InheritProp.LValAssign) {
            return name + "[" + t2 + "]";
        } else {
            String ans = IRTagManage.getInstance().newVar();
            myWrite(ans + " = " + name + "[" + t2 + "]");
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
            myWrite(ans + " = " + name + "[" + one + "]");
            return ans;
        }
    }

    public String mulExpUnary(String mid, int coe, int negative) throws IOException {
        if (coe == 1 && negative == 1) return mid;
        String ans = IRTagManage.getInstance().newVar();
        if (negative == -1) {  //如果有！的话，是不是1没什么关系了
            myWrite(ans + " = ! " + mid);
        } else if (coe == -1) {
            myWrite(ans + " = - " + mid);
        }
        return ans;
    }

    public String mulExpTwo(String mul, String una, int coe, int negative, Word op) throws IOException {
        String t2 = mulExpUnary(una, coe, negative);
        String ans = IRTagManage.getInstance().newVar();
        myWrite(generateTwoExp(ans, mul, op.getToken(), t2));
        return ans;
    }

    public String addExpTwo(String add, String mul, Word op) throws IOException {
        String ans = IRTagManage.getInstance().newVar();
        myWrite(generateTwoExp(ans, add, op.getToken(), mul));
        return ans;
    }

    public void inBlock() throws IOException {
        myWrite("{");
    }

    public void leaveBlock() throws IOException {
        myWrite("}");
    }

    public void b_label(String label) throws IOException {
        myWrite("b " + label);
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
        return ret;
    }

    public void b_false(String ir, String label) throws IOException {
        myWrite("&cmp " + ir + " 0");
        myWrite("beq " + label);
    }

    public void localLabel(String label) throws IOException {
        myWrite(label + ":");
    }

    private String generateTwoExp(String ans, String t1, String op, String t2) {
        return ans + " = " + t1 + " " + op + " " + t2 + "\n";
    }

    public void funcDef(TypeTable returnType, String name) throws IOException {
        myWrite(returnType.toString().toLowerCase() + " " + name + " ()");
    }

    public void funcFParam(TypeTable type, String name, int dim, TableSymbol tableSymbol, int falseRow) throws IOException {
        name = APIIRSymTable.getInstance().findElementIRGen(tableSymbol, name, falseRow).getIRName();
        if (dim == 0) {
            myWrite("&para " + type.toString().toLowerCase() + " " + name);
        } else {
            myWrite("&para " + type.toString().toLowerCase() + " " + name + " []");
        }
    }

    public String funcCall(String funcName, ArrayList<Exp> exps) throws IOException {
        for (Exp exp : exps) {
            myWrite("&push " + exp.getMidCode());
        }
        return funcCall(funcName);
    }

    public String funcCall(String funcName) throws IOException {
        myWrite("&call " + funcName);
        ElementFunc elementFunc = APIIRSymTable.getInstance().getFuncElement(funcName);
        if (elementFunc.getReturnType() == TypeTable.INT) {
            String t1 = IRTagManage.getInstance().newVar();
            myWrite(t1 + " = $RET");
            return t1;
        }
        return null;
    }


    public void returnStmt(String exp) throws IOException {
        myWrite("&ret " + exp);
    }

    public void returnStmt() throws IOException {
        myWrite("&ret");
    }

    public void mainRetStmt() throws IOException {
        myWrite("&ret main");
    }

    public void assignStmtScanf(String lVal) throws IOException {
        String t1 = IRTagManage.getInstance().newVar();
        myWrite("&scanf " + t1);
        assignStmtExp(lVal, t1);
    }

    public void assignStmtExp(String left, String right) throws IOException {
        myWrite(left + " = " + right);
    }

    public void myWrite(String str) throws IOException {
        fileWriter.write(str + "\n");
        //System.out.print(str);
    }

    //对于数组指针引用，最后的结果还需要乘4
    public String mulFour(String s) throws IOException {
        String ans = IRTagManage.getInstance().newVar();
        myWrite(ans + " = " + s + " << 2");
        return ans;
    }

    public void annotate(String str) throws IOException {
        myWrite("##" + str + "##");
    }
}
