package gccBin.MidCode;

import GramTree.Element.Exp;
import GramTree.Element.InitVal;
import GramTree.InheritProp;
import GramTree.Param;
import GramTree.TreeElement;
import GramTree.Word;
import SymbolTableBin.*;
import gccBin.Lex.Symbol;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class MidCode {
    private static MidCode instance;
    private FileWriter fileWriter;

    private TreeElement root;

    private MidCode() {
    }

    public static MidCode getInstance() {
        if (instance == null) {
            instance = new MidCode();
        }
        return instance;
    }

    public void openMidCodeWriter() throws IOException {
        File file = new File("midCode.txt");
        this.fileWriter = new FileWriter(file.getName());
    }

    public void closeMidCodeWriter() throws IOException {
        this.fileWriter.close();
    }

    public void setRoot(TreeElement root) {
        this.root = root;
    }

    public void beginMidCodeGen() throws IOException {
        Param param = new Param();
        this.root.midCodeGen(this.fileWriter, param);
    }

    public void constDefArray(String name, int len, ArrayList<ArrayList<Integer>> nums) throws IOException {
        write("arr int " + name + "[" + len + "]\n");
        int index = 0;
        for (ArrayList<Integer> integers : nums) {
            for (Integer i : integers) {
                write(name + "[" + (index++) + "]" + " = " + i + "\n");
            }
        }
    }

    public void varDef(String name, int len, InitVal initVal) throws IOException {
        Param param = new Param();
        if (len <= 0) {
            write("var int " + name + "\n");
            if (initVal != null) {
                ArrayList<Exp> exps = initVal.getExps();
                exps.get(0).midCodeGen(fileWriter, param);
                write(name + " = " + initVal.getExps().get(0).getMidCode() + "\n");
            }
            return;
        }
        write("arr int " + name + "[" + len + "]\n");
        int index = 0;
        if (initVal != null) {
            ArrayList<Exp> exps = initVal.getExps();
            for (Exp exp : exps) {
                exp.midCodeGen(fileWriter, param);
                write(name + "[" + (index++) + "]" + " = " + exp.getMidCode() + "\n");
            }
        }
    }

    public String lValRParam(TableSymbol tableSymbol, String name) throws IOException {
        ElementTable elementTable = APIMidCodeSymTable.getInstance()
                .findTableElementRecur(tableSymbol, name);
        if ( elementTable.getDimension() == 0 ) {  //保存的是值
            return lValNormal(name,new Param()); //实参传值
        } else {
            String ans = MidTagManage.getInstance().newVar();
            write(ans + " = " + name +" >> 2\n");  // 除4
            return name;
        }
    }

    public String lValRParam(TableSymbol tableSymbol, String name, String one) throws IOException {
        ElementTable elementTable = APIMidCodeSymTable.getInstance()
                .findTableElementRecur(tableSymbol, name);

        if (elementTable.getDimension() == 1) {  //保存的是值
            return lValNormal(name, one,new Param());
        } else if (elementTable.getDimension() == 2) {
            int len = APIMidCodeSymTable.getInstance().findTwoDimArrayLen(
                    tableSymbol, name);
            String t1 = MidTagManage.getInstance().newVar();
            write(t1 + " = " + one + " * " + len + "\n");
            String ans = MidTagManage.getInstance().newVar();
            write(ans + " = " + name +  " >> 2" + "\n"); // 除4
            write(ans + " = " + t1 + " + " + ans + "\n");
            return ans;
        } else return null;
    }

    //2维一定是数值
    public String lValRParam(TableSymbol tableSymbol, String name, String one, String two) throws IOException {
        int len = APIMidCodeSymTable.getInstance().findTwoDimArrayLen(
                tableSymbol, name);
        return lValNormal(name, one, two, len,new Param());
    }

    public String lValNormal(String name,Param param) throws IOException {
        if(param.getExpKind() == InheritProp.LValAssign){
            return  name;
        } else {
            String t1 = MidTagManage.getInstance().newVar();
            write(t1 +" = "+name+"\n");
            return t1;
        }
    }


    public String lValNormal(String name,String one, String two, int len,Param param) throws IOException {
        String t1 = MidTagManage.getInstance().newVar();
        write(t1 + " = " + one + " * " + len + "\n");
        String t2 = MidTagManage.getInstance().newVar();
        write(t2 + " = " + two + " + " + t1 + "\n");
        if(param.getExpKind() == InheritProp.LValAssign) {
            return name + "[" + t2 + "]";
        } else {
            String ans  = MidTagManage.getInstance().newVar();
            write(ans + " = "+name + "[" + t2 + "]\n");
            return ans;
        }
    }

    public String lValNormal(String name,String one,Param param) throws IOException {
        if(param.getExpKind() == InheritProp.LValAssign) {
            return name + "[" + one + "]";
        } else {
            String ans  = MidTagManage.getInstance().newVar();
            write(ans + " = "+name + "[" + one + "]\n");
            return ans;
        }
    }

    public String mulExpUnary(String mid, int coe, int negative) throws IOException {
        if (coe == 1 && negative == 1) return mid;
        String ans = MidTagManage.getInstance().newVar();
        if (negative == -1) {  //如果有！的话，是不是1没什么关系了
            write(ans + " = ! " + mid + "\n");
        } else if (coe == -1) {
            write(ans + " = - " + mid + "\n");
        }
        return ans;
    }

    public String mulExpTwo(String mul, String una, int coe, int negative, Word op) throws IOException {
        String t2 = mulExpUnary(una, coe, negative);
        String ans = MidTagManage.getInstance().newVar();
        write(generateTwoExp(ans, mul, op.getToken(), t2));
        return ans;
    }

    public String addExpTwo(String add, String mul, Word op) throws IOException {
        String ans = MidTagManage.getInstance().newVar();
        write(generateTwoExp(ans, add, op.getToken(), mul));
        return ans;
    }

    public void inBlock() throws IOException {
        write("{\n");
    }

    public void leaveBlock() throws IOException {
        write("}\n");
    }

    public void jump(String label) throws IOException {
        write("b " + label + "\n");
    }

    public void condJump(String t1, Word op, String t2, String label) throws IOException {
        write("cmp " + t1 + " " + t2 + "\n");
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
        }
    }

    public void localLabel(String label) throws IOException {
        write(label + ":\n");
    }

    private String generateTwoExp(String ans, String t1, String op, String t2) {
        return ans + " = " + t1 + " " + op + " " + t2 + "\n";
    }

    public void funcDef(TypeTable returnType, String name) throws IOException {
        write(returnType.toString().toLowerCase() + " " + name + " ()\n");
    }

    public void funcFParam(TypeTable type, String name, int dim) throws IOException {
        if (dim == 0) {
            write("para " + type.toString().toLowerCase() + " " + name + "\n");
        } else {
            write("para " + type.toString().toLowerCase() + " " + name + " []\n");
        }
    }

    public String funcCall(String funcName, ArrayList<Exp> exps) throws IOException {
        for (Exp exp : exps) {
            write("push " + exp.getMidCode() + "\n");
        }
        return funcCall(funcName);
    }

    public String funcCall(String funcName) throws IOException {
        write("call " + funcName + "\n");
        ElementFunc elementFunc = APIMidCodeSymTable.getInstance().getFuncElement(funcName);
        if(elementFunc.getReturnType() == TypeTable.INT) {
            String t1 = MidTagManage.getInstance().newVar();
            write(t1 + " = RET\n");
            return t1;
        }
        return null;
    }


    public void returnStmt(String exp) throws IOException {
        write("ret " + exp + "\n");
    }

    public void returnStmt() throws IOException {
        write("ret" + "\n");
    }

    public void assignStmtScanf(String lVal) throws IOException {
        String t1 = MidTagManage.getInstance().newVar();
        write("scanf " + t1 + "\n");
        assignStmtExp(lVal, t1);
    }

    public void assignStmtExp(String left, String right) throws IOException {
        write(left + " = " + right + "\n");
    }

    public void write(String str) throws IOException {
        fileWriter.write(str);
        System.out.print(str);
    }

    public void LAndExpSignalExp(String t0, String endLabel) throws IOException {
        write("cmp " + t0 + " 0\n");
        write("beq " + endLabel + "\n");
    }

    //对于数组指针引用，最后的结果还需要乘4
    public String mulFour(String s) throws IOException {
        String ans = MidTagManage.getInstance().newVar();
        write(ans + " = " + s + " << 2\n");
        return ans;
    }

    public void annotate(String str) throws IOException {
        write("# "+str+"\n");
    }
}
