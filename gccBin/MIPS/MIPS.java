package gccBin.MIPS;

import SymbolTableBin.*;
import SymbolTableBin.Element.ElementConstArray;
import SymbolTableBin.Element.ElementTable;
import SymbolTableBin.Element.ElementVarArray;
import gccBin.MIPS.tool.*;
import gccBin.MidCode.Judge;
import gccBin.MidCode.Line.*;
import gccBin.MidCode.LineManager;
import gccBin.MidCode.AoriginalProcess.PrintfFormatStringStore;
import gccBin.UnExpect;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class MIPS {
    private static MIPS instance;

    private FileWriter fileWriter; //和MM绑定

    private MIPS() {
    }

    public static MIPS getInstance() {
        if (instance == null) {
            instance = new MIPS();
        }
        return instance;
    }

    public void open() throws IOException {
        File file = new File("mips.txt");
        this.fileWriter = new FileWriter(file.getName());
    }

    public void close() throws IOException {
        this.fileWriter.close();
    }

    public void begin() throws IOException {
        fileWriter.write("li $fp,0x10040000\n");
        LineManager.getInstance().beginErgodic();
        do {
            Line line = LineManager.getInstance().nextLine();
            TableSymbol tableSymbol = line.getTableSymbol();
            if (line instanceof ArrayDefLine) {
                arrayLineTranslate(tableSymbol, (ArrayDefLine) line);
            } else if (line instanceof AssignLine) {
                assignLineTrans(tableSymbol, (AssignLine) line);
            } else if (line instanceof CmpLine) {
                cmpLineTrans((CmpLine) line, tableSymbol);
            } else if (line instanceof FParamDefLine) {
                fParamDefLineTrans((FParamDefLine) line);
            } else if (line instanceof FuncDefLine) {
                funcDefLineTrans((FuncDefLine) line);
            } else if (line instanceof LabelLine) {
                labelLineTrans((LabelLine) line);
            } else if (line instanceof PrintfLine) {
                printfLineTrans((PrintfLine) line, tableSymbol);
            } else if (line instanceof PushLine) {
                pushLineLineTrnas((PushLine) line, tableSymbol);
            } else if (line instanceof RetLine) {
                retLineTrans((RetLine) line, tableSymbol);
            } else if (line instanceof ScanfLine) {
                scanfLineTrans((ScanfLine) line);
            } else if (line instanceof VarDeclLine) {
                varDeclLineTrans(tableSymbol, (VarDeclLine) line);
            } else if (line instanceof CallFuncLine) {
                callFuncLineTrans((CallFuncLine) line);
            } else if (line instanceof BLine) {
                bLineTrans((BLine) line);
            }
        } while (LineManager.getInstance().hasNext());
    }


    public void cmpLineTrans(CmpLine cmpLine, TableSymbol tableSymbol) throws IOException {
        Reg reg1 = MIPSHelper.getValueInReg_t_v_n(Reg.r1, cmpLine.getT1(), tableSymbol);
        Reg reg2 = MIPSHelper.getValueInReg_t_v_n(Reg.r2, cmpLine.getT2(), tableSymbol);

        BLine bLine = (BLine) LineManager.getInstance().nextLine();
        MipsIns.bCond_reg1_reg2_label(bLine.getB(), reg1, reg2, bLine.getLabel());
        TempRegPool.getInstance().delete(cmpLine.getT1());
        TempRegPool.getInstance().delete(cmpLine.getT2());
    }

    public void bLineTrans(BLine bLine) throws IOException {
        if (bLine.getB().equals("b")) {
            MipsIns.b_Label(bLine.getLabel());
        }
    }

    public void callFuncLineTrans(CallFuncLine callFuncLine) throws IOException {
        if (!push) MemManager.getInstance().pushAReg();
        if (callFuncLine.getFuncName().equals("main")) {
            MipsIns.b_Label("main");
            return;
        }
        MemManager.getInstance().pushS_TReg(); //保存现场
        int fpOff = MemManager.getInstance().getFpOff();
        if (fpOff != 0) {
            MipsIns.add_ans_reg_regOrNum(Reg.$fp, Reg.$fp, fpOff);
        }
        MipsIns.jal_label(callFuncLine.getFuncName());
        if (fpOff != 0) {
            MipsIns.sub_ans_reg_regOrNum(Reg.$fp, Reg.$fp, fpOff);
        }
        MemManager.getInstance().popS_TReg();
        MemManager.getInstance().popAReg();
        push = false;
    }

    public void fParamDefLineTrans(FParamDefLine fParamDefLine) {
        int index = 1;
        Line line;
        do {
            if (index > 4) {
                MemManager.getInstance().addFpOff(1);
            } else if (index >= 1) {
                MemManager.getInstance().addTo_AF_StoreReg(Reg.getFParamReg(index));
            }
            line = LineManager.getInstance().nextLine();
            index++;
        } while (line instanceof FParamDefLine);
        LineManager.getInstance().retract();
    }

    private boolean push = false;

    //若果要push,就先保存本函数的a-reg，否则就在调用函数的时候保存reg
    public void pushLineLineTrnas(PushLine push, TableSymbol tableSymbol) throws IOException {
        this.push = true;
        MemManager.getInstance().pushAReg();
        int index = 1;
        Line line = push;
        int off = MemManager.getInstance().getFpOff();

        while (line instanceof PushLine) {
            PushLine pushLine = (PushLine) line;
            String exp = pushLine.getExp();

            if (index <= 4 && index >= 1) {
                Reg paramReg = Reg.getFParamReg(index);
                MIPSHelper.getValueInSpecialReg_t_v_n(paramReg, exp, tableSymbol);
            } else if (index >= 5) {
                Reg value = Reg.r1;
                MIPSHelper.getValueInSpecialReg_t_v_n(value, exp, tableSymbol);
                MipsIns.sw_value_num_baseReg(value, off, Reg.$fp);
                off = off + 4;
            } else UnExpect.fParamIndexError(exp, index);
            line = LineManager.getInstance().nextLine();
            index++;
            TempRegPool.getInstance().delete(exp);
        }
        LineManager.getInstance().retract();
    }

    private boolean pre = true;

    public void funcDefLineTrans(FuncDefLine funcDefLine) throws IOException {
        if (pre) {
            pre();
            pre = false;
        }
        write(funcDefLine.getName() + ":");
        MemManager.getInstance().enterANewFunc();
    }

    public void labelLineTrans(LabelLine labelLine) throws IOException {
        write(labelLine.getLabel() + ":");
    }

    public void printfLineTrans(PrintfLine printfLine, TableSymbol tableSymbol) throws IOException {
        String s = printfLine.getExp();
        if (Judge.isTemp(s)) {
            Reg reg = MIPSHelper.getValueInReg_t_v(Reg.r1, s, tableSymbol);
            MipsIns.printfExp(reg);
            TempRegPool.getInstance().delete(s);
        } else if (Judge.isNumber(s)) {
            int num = Integer.parseInt(s);
            MipsIns.printfInt(num);
        } else {
            MipsIns.printfStr(s);
        }
    }

    public void scanfLineTrans(ScanfLine scanfLine) throws IOException {
        String t = scanfLine.getExp();
        MipsIns.scanfInt();
        TempRegPool.getInstance().addToPool(t);
        if (TempRegPool.getInstance().inReg(t)) {
            Reg reg = TempRegPool.getInstance().getReg(t);
            MipsIns.move_reg_reg(reg, Reg.$v0);
        } else if (TempRegPool.getInstance().inMem(t)) {
            TempRegPool.getInstance().storeToMem(Reg.$v0, t);
        } else UnExpect.tempNotInMemAndReg(t);
    }

    public void retLineTrans(RetLine retLine, TableSymbol tableSymbol) throws IOException {
        if (retLine.isGotoExit()) {
            MipsIns.b_Label("main_end");
            return;
        }
        String exp = retLine.getExp();
        if (exp != null) {
            MIPSHelper.getValueInSpecialReg_t_v_n(Reg.$v0, exp, tableSymbol);
        }
        TempRegPool.getInstance().delete(exp);
        MipsIns.jr_reg(Reg.$ra);
    }

    public void arrayLineTranslate(TableSymbol tableSymbol, ArrayDefLine arrayDefLine) throws IOException {
        int len = arrayDefLine.getLen();
        String arrName = arrayDefLine.getName();
        if (tableSymbol.getFather() == null) { //全局数组
            write(".data");
            ElementTable elementTable = APIIRSymTable.getInstance().findGlobalElement(
                    arrayDefLine.getName());
            writeNotNext("  " + arrName + ":");
            if (elementTable instanceof ElementConstArray) {
                writeNotNext(".word ");
                for (int i = 0; i < len; i++) {
                    AssignLine assignLine = (AssignLine) LineManager.
                            getInstance().nextLine();
                    writeNotNext(assignLine.getT1() + ",");
                }
                writeNotNext("\n");
                write(".text");
            } else if (elementTable instanceof ElementVarArray) {
                write(".space " + len * 4); //先分配地址 等着
                write(".text");
            }
        } else {
            MemManager.getInstance().allocationArrMem(arrName, len, tableSymbol);
        }
    }

    private void varDeclLineTrans(TableSymbol tableSymbol, VarDeclLine varDeclLine) throws IOException {
        String name = varDeclLine.getName();
        if (tableSymbol.getFather() == null) {
            write(".data");
            writeNotNext("  " + name + ":");
            write(".space 4");
            write(".text");
        } else {
            MemManager.getInstance().handleVar(name, tableSymbol);
        }
    }

    //不涉及数组指针操作
    private void assignLineTrans(TableSymbol tableSymbol, AssignLine assignLine) throws IOException {
        if (assignLine.toString().equals("m = 0")) {
            int a = 1;
        }
        String ans = assignLine.getAns();
        String t1 = assignLine.getT1();
        String t2 = assignLine.getT2();
        String op = assignLine.getOp();
        if (assignLine.isPureAssign()) {
            if (Judge.isRET(t1)) {
                if (Judge.isTemp(ans)) {
                    TempRegPool.getInstance().addV0ToTemp(ans);
                } else if (Judge.isArrayValue(ans)) {
                    MIPSHelper.assignVTNToArr(ans, "$RET", tableSymbol);
                } else if (Judge.isVar(ans)) {
                    MIPSHelper.assignVTNToVar(ans, "$RET", tableSymbol);
                }
            } else if (Judge.isVar(ans) && Judge.isVTN(t1)) { // v = t v n
                MIPSHelper.assignVTNToVar(ans, t1, tableSymbol);
            } else if (Judge.isTemp(ans) && (Judge.isVar(t1) || Judge.isNumber(t1))) { // t = v n
                MIPSHelper.assignVTN_ToTemp(ans, t1, tableSymbol);
            } else if (Judge.isArrayValue(ans) && Judge.isVTN(t1)) { //a[] = t v n
                MIPSHelper.assignVTNToArr(ans, t1, tableSymbol);
            } else if (Judge.isTemp(ans) && Judge.isArrayValue(t1)) { //t = a[]
                MIPSHelper.assignArrToTemp(ans, t1, tableSymbol);
            } else if (Judge.isTemp(ans) && Judge.isTemp(t1)) {
                TempRegPool.getInstance().justCopy(ans, t1);
            }
        } else if (assignLine.isOneAssign()) {
            if (Judge.isVarOrTemp(t1)) {
                MIPSHelper.assignOne(ans, op, t1, tableSymbol);
            } else if (Judge.isNumber(t1)) {
                MIPSHelper.assignOne(ans, op, Integer.parseInt(t1));
            } else UnExpect.printf("assignOne error");
        } else if (assignLine.isTwoAssign()) {
            MIPSHelper.assignTwo(ans, t1, op, t2, tableSymbol);
        }
    }


    public void pre() throws IOException { //先打出来
        fileWriter.write(".data\n");
        fileWriter.write("str_ : .asciiz \"\\n\"\n");
        int i = 0;
        for (String str : PrintfFormatStringStore.getInstance().getFormatsStrings()) {
            fileWriter.write("str_" + i + ": " + ".asciiz \"" + str + "\"\n");
            i++;
        }
        fileWriter.write(".text\n");
    }

    public String annotate(String s) {
        return "# " + s + " #";
    }

    public void write(String s) throws IOException {
        fileWriter.write(s);

        int len = s.length();
        int space = (len <= 30) ? 30 - len : 0;
        for (int i = 0; i < space; i++) {
            fileWriter.write(" ");
        }
        fileWriter.write("#" + LineManager.getInstance()
                .getNowLine().getMidCodeLine() + "\n");

        System.out.println(s);
    }

    public void writeNotNext(String s) throws IOException {
        fileWriter.write(s);
        System.out.print(s);
    }

}
