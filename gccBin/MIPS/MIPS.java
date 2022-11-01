package gccBin.MIPS;

import SymbolTableBin.*;
import gccBin.MIPS.tool.*;
import gccBin.MidCode.JudgeExpElement;
import gccBin.MidCode.Line.*;
import gccBin.MidCode.LineManager;
import gccBin.MidCode.original.PrintfFormatStringStore;

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

    public void openMipsWriter() throws IOException {
        File file = new File("mips.txt");
        this.fileWriter = new FileWriter(file.getName());
    }

    public void closeMIPSWriter() throws IOException {
        this.fileWriter.close();
    }

    public void beginTransLate() throws IOException {
        write(".data");
        LineManager.getInstance().beginErgodic();
        Line line = LineManager.getInstance().nextLine();
        TableSymbol tableSymbol = line.getTableSymbol();


    }


    public void cmpLineTrans(TableSymbol tableSymbol, CmpLine cmpLine) throws IOException {
        Reg reg1 = MIPSHelper.get().getValue(Reg.rightOne, tableSymbol, cmpLine.getT1());
        Reg reg2 = MIPSHelper.get().getValue(Reg.rightTwo, tableSymbol, cmpLine.getT2());
        BLine bLine = (BLine) LineManager.getInstance().nextLine();
        MIPSIns.bCond(bLine.getB(), reg1, reg2, bLine.getLabel());
    }

    public void callFuncLineTrans(TableSymbol tableSymbol, CallFuncLine callFuncLine) {

    }

    public void labelLineTrans(TableSymbol tableSymbol, LabelLine labelLine) throws IOException {
        write(labelLine.getLabel() + ":");
    }

    public void printfLineTrans(TableSymbol tableSymbol, PrintfLine printfLine) throws IOException {
        String s = printfLine.getT();
        if (JudgeExpElement.isTemp(s)) {
            Reg reg = TempRegPool.getInstance().getTempInReg(Reg.rightOne, s);
            MIPSIns.printfExp(reg);
        } else if (JudgeExpElement.isNumber(s)) {
            int num = Integer.parseInt(s);
            MIPSIns.printfInt(num);
        } else {
            MIPSIns.printfStr(s);
        }
    }

    public void scanfLineTrans(ScanfLine scanfLine) throws IOException {
        String t = scanfLine.getT();
        MIPSIns.scanfInt();
        MIPSHelper.get().allocTempAndInit(t, Reg.$a0);
    }

    public void retLineTrans(RetLine retLine) throws IOException {
        String exp = retLine.getExp();
        if (!retLine.isGotoExit() && exp != null) {
            if (JudgeExpElement.isTemp(exp)){
                Reg t = TempRegPool.getInstance().getTempInReg(Reg.rightOne,exp);
                MIPSIns.move(Reg.$v0,t);
            } else if(JudgeExpElement.isNumber(exp)){
                MIPSIns.li(Reg.$v0,Integer.parseInt(exp));
            }
        }
    }

    public void arrayLineTranslate(TableSymbol tableSymbol, ArrayDefLine arrayDefLine) throws IOException {
        int len = arrayDefLine.getLen();
        String arrName = arrayDefLine.getName();
        if (tableSymbol.getFather() == null) { //全局数组
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
            } else if (elementTable instanceof ElementVarArray) {
                write(".space " + len * 4); //先分配地址 等着
            }
        } else {
            //在fp上为数组分配合适的空间
            MemManager.getInstance().allocationArrMem(arrName, len, tableSymbol);
        }
    }

    private void varDeclLineTrans(TableSymbol tableSymbol, VarDeclLine varDeclLine) throws IOException {
        String name = varDeclLine.getName();
        if (tableSymbol.getFather() == null) {
            writeNotNext("  " + name + ":");
            write(".space 4");
        } else {
            MemManager.getInstance().allocationVarMem(name, tableSymbol);
        }
    }

    //不涉及数组指针操作
    private void assignLineTrans(TableSymbol tableSymbol, AssignLine assignLine) throws IOException {
        String ans = assignLine.getAns();
        String t1 = assignLine.getT1();
        String t2 = assignLine.getT2();
        String op = assignLine.getOp();
        if (assignLine.isPureAssign()) {
            if (JudgeExpElement.isTemp(t1) ||
                    JudgeExpElement.isNumber(t1)) {
                MIPSHelper.get().storeTempToVar(ans, t1, tableSymbol);
            } else {
                MIPSHelper.get().assignVarToTemp(ans, t1, tableSymbol);
            }
        } else if (assignLine.isOneOpr()) {
            MIPSHelper.get().oneAssign(ans, op, t1);
        } else if (assignLine.isTwoOpr()) {
            MIPSHelper.get().assignTwo(ans, t1, op, t2, tableSymbol);
        }
    }


    public void pre() throws IOException { //先打出来
        //fileWriter.write(".data\n");
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
        fileWriter.write(s + "\n");
    }

    public void writeNotNext(String s) throws IOException {
        fileWriter.write(s + "\n");
    }

}
