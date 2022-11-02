package gccBin.MIPS;

import SymbolTableBin.*;
import SymbolTableBin.Element.ElementConstArray;
import SymbolTableBin.Element.ElementTable;
import SymbolTableBin.Element.ElementVarArray;
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

    public void open() throws IOException {
        File file = new File("mips.txt");
        this.fileWriter = new FileWriter(file.getName());
    }

    public void close() throws IOException {
        this.fileWriter.close();
    }

    public void begin() throws IOException {
        LineManager.getInstance().beginErgodic();
        do {
            Line line = LineManager.getInstance().nextLine();
            TableSymbol tableSymbol = line.getTableSymbol();
            if (line instanceof ArrayDefLine) {
                arrayLineTranslate(tableSymbol, (ArrayDefLine) line);
            } else if (line instanceof AssignLine) {
                assignLineTrans(tableSymbol, (AssignLine) line);
            } else if (line instanceof CmpLine) {
                cmpLineTrans(tableSymbol, (CmpLine) line);
            } else if (line instanceof FParamDefLine) {
                fParamDefLineTrans((FParamDefLine) line);
            } else if (line instanceof FuncDefLine) {
                funcDefLineTrans((FuncDefLine) line);
            } else if (line instanceof LabelLine) {
                labelLineTrans((LabelLine) line);
            } else if (line instanceof PrintfLine) {
                printfLineTrans((PrintfLine) line);
            } else if (line instanceof PushLine) {
                pushLineLineTrnas((PushLine) line);
            } else if (line instanceof RetLine) {
                retLineTrans((RetLine) line);
            } else if (line instanceof ScanfLine) {
                scanfLineTrans((ScanfLine) line);
            } else if (line instanceof VarDeclLine) {
                varDeclLineTrans(tableSymbol, (VarDeclLine) line);
            } else if (line instanceof CallFuncLine) {
                callFuncLineTrans((CallFuncLine) line);
            }
        } while (LineManager.getInstance().hasNext());
    }


    public void cmpLineTrans(TableSymbol tableSymbol, CmpLine cmpLine) throws IOException {
        Reg reg1 = MIPSHelper.get().getValue(Reg.rightOne, tableSymbol, cmpLine.getT1());
        Reg reg2 = MIPSHelper.get().getValue(Reg.rightTwo, tableSymbol, cmpLine.getT2());
        BLine bLine = (BLine) LineManager.getInstance().nextLine();
        MIPSIns.bCond(bLine.getB(), reg1, reg2, bLine.getLabel());
    }

    /**
     * @param callFuncLine
     * @throws IOException
     */
    public void callFuncLineTrans(CallFuncLine callFuncLine) throws IOException {
        MemManager.getInstance().pushSReg();
        MIPSIns.sub_reg_o(Reg.$fp, Reg.$fp, MemManager.getInstance().getFpOff());
        MIPSIns.jalLabel(callFuncLine.getFuncName());
        MIPSIns.add_reg_o(Reg.$fp, Reg.$fp, MemManager.getInstance().getFpOff());
        MemManager.getInstance().popSReg();
    }

    public void fParamDefLineTrans(FParamDefLine fParamDefLine) {
        int index = 1;
        Line line;
        do {
            if (index > 4) {
                MemManager.getInstance().addFpOff(1);
            }
            line = LineManager.getInstance().nextLine();
            index++;
        } while (line instanceof FParamDefLine);
        LineManager.getInstance().retract();
    }

    public void pushLineLineTrnas(PushLine pushLine) throws IOException {
        int index = 1;
        Line line;
        int off = MemManager.getInstance().getFpOff();
        do {
            Reg temp = TempRegPool.getInstance().getTempInReg(Reg.rightOne, pushLine.getExp());
            if (index <= 4) {
                Reg reg = Reg.getFParamReg(index);
                MIPSIns.move(reg, temp);
            } else {
                MIPSIns.sw_number_reg(temp, off, Reg.$fp);
                off = off + 4;
            }
            line = LineManager.getInstance().nextLine();
            index++;
        } while (line instanceof PushLine);
        LineManager.getInstance().retract();
    }

    public void funcDefLineTrans(FuncDefLine funcDefLine) throws IOException {
        pre();
        write(funcDefLine.getName() + ":");
        MemManager.getInstance().enterANewFunc();
    }

    public void labelLineTrans(LabelLine labelLine) throws IOException {
        write(labelLine.getLabel() + ":");
    }

    public void printfLineTrans(PrintfLine printfLine) throws IOException {
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
        MIPSHelper.get().allocTempAndInit(t, Reg.$v0);
    }

    public void retLineTrans(RetLine retLine) throws IOException {
        if (retLine.isGotoExit()) return;
        String exp = retLine.getExp();
        if (!retLine.isGotoExit() && exp != null) {
            if (JudgeExpElement.isTemp(exp)) {
                Reg t = TempRegPool.getInstance().getTempInReg(Reg.rightOne, exp);
                MIPSIns.move(Reg.$v0, t);
            } else if (JudgeExpElement.isNumber(exp)) {
                MIPSIns.li(Reg.$v0, Integer.parseInt(exp));
            }
        }
        MIPSIns.jr(Reg.$ra);
    }

    public void arrayLineTranslate(TableSymbol tableSymbol, ArrayDefLine arrayDefLine) throws IOException {
        int len = arrayDefLine.getLen();
        String arrName = arrayDefLine.getName();
        if (tableSymbol.getFather() == null) { //全局数组
            ElementTable elementTable = APIIRSymTable.getInstance().findGlobalElement(
                    arrayDefLine.getName());
            writeNotNext("  " + arrName + ":");
            if (elementTable instanceof ElementConstArray) {
                write(".data");
                writeNotNext(".word ");
                for (int i = 0; i < len; i++) {
                    AssignLine assignLine = (AssignLine) LineManager.
                            getInstance().nextLine();
                    writeNotNext(assignLine.getT1() + ",");
                }
                writeNotNext("\n");
                write(".text");
            } else if (elementTable instanceof ElementVarArray) {
                write(".data");
                write(".space " + len * 4); //先分配地址 等着
                write(".text");
            }
        } else {
            //在fp上为数组分配合适的空间
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
            if (JudgeExpElement.isTemp(t1) || JudgeExpElement.isNumber(t1)) {
                MIPSHelper.get().storeTempOrNumberToVar(ans, t1, tableSymbol);
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

        fileWriter.write(".data\n");
        fileWriter.write("str_ : .asciiz \"\\n\"\n");
        int i = 0;
        for (String str : PrintfFormatStringStore.getInstance().getFormatsStrings()) {
            fileWriter.write("str_" + i + ": " + ".asciiz \"" + str + "\"\n");
            i++;
        }
        fileWriter.write(".text\n");
        fileWriter.write("li $fp,0x10040000\n");
    }

    public String annotate(String s) {
        return "# " + s + " #";
    }

    public void write(String s) throws IOException {
        fileWriter.write(s + "\n");
        System.out.println(s);
    }

    public void writeNotNext(String s) throws IOException {
        fileWriter.write(s);
        System.out.print(s);
    }

}
