package gccBin.MIPS.tool;

import GramTree.Element.FuncFParam;
import SymbolTableBin.*;
import SymbolTableBin.Element.ElementFParam;
import SymbolTableBin.Element.ElementTable;
import gccBin.MIPS.MIPS;
import gccBin.MidCode.JudgeExpElement;

import java.io.IOException;

public class MIPSHelper {
    private static MIPSHelper mipsHelper;

    private MIPSHelper() {
    }

    public static MIPSHelper get() {
        if (mipsHelper == null) {
            mipsHelper = new MIPSHelper();
        }
        return mipsHelper;
    }

    private IRType irType;

    public IRType getIrType() {
        return irType;
    }


    public void assignTwo(String ans, String t1, String op, String t2,TableSymbol tableSymbol) throws IOException {
        Reg reg1 = getValue(Reg.rightOne,tableSymbol,t1);
        IRType irType1 = irType;
        Reg reg2 = getValue(Reg.rightTwo,tableSymbol,t2);
        IRType irType2 = irType;
        if(irType1 == IRType.Number && irType2 == IRType.Number){
            int num1 = Integer.parseInt(t1);
            int num2 = Integer.parseInt(t2);
            if(JudgeExpElement.isPlus(op)){
                MIPSIns.li(Reg.rightOne,num1+num2);
            } else if (JudgeExpElement.isMinus(op)) {
                MIPSIns.li(Reg.rightOne,num1-num2);
            }else if (JudgeExpElement.isMul(op)) {
                MIPSIns.li(Reg.rightOne,num1*num2);
            } else if (JudgeExpElement.isDiv(op)) {
                MIPSIns.li(Reg.rightOne,num1/num2);
            }else if (JudgeExpElement.isMod(op)) {
                MIPSIns.li(Reg.rightOne,num1%num2);
            }
        } else {
            if(JudgeExpElement.isPlus(op)){
                MIPSIns.add_reg_o(Reg.rightOne,reg1,reg2);
            } else if (JudgeExpElement.isMinus(op)) {
                MIPSIns.sub_reg_o(Reg.rightOne,reg1,reg2);
            }else if (JudgeExpElement.isMul(op)) {
                MIPSIns.mult_reg_reg(Reg.rightOne,reg1,reg2);
            } else if (JudgeExpElement.isDiv(op)) {
                MIPSIns.div_reg_reg(Reg.rightOne,reg1,reg2);
            }else if (JudgeExpElement.isMod(op)) {
                MIPSIns.mod_reg_reg(Reg.rightOne,reg1,reg2);
            } else if (JudgeExpElement.isSll(op)) {
                MIPSIns.sll(Reg.rightOne,reg1,2);
            } else if (JudgeExpElement.isSrl(op)) {
                MIPSIns.srl(Reg.rightOne,reg1,2);
            }
        }
    }

    /**
     * 并将应该写入mips的信息传出,将取出相应的数到特定的寄存器，但是对于数字的话，不需要存到
     * 寄存器里，因为数字可以直接计算
     * 如果值是保存在寄存器里的，就输出寄存器，如果值是保存在
     * 内存里的，就先把值取到临时寄存器
     *
     * @num:是右侧表达式的第几个操作数
     */
    public Reg getValue(Reg ans, TableSymbol tableSymbol, String s) throws IOException {
        if (JudgeExpElement.isTemp(s)) {
            irType = IRType.Temp;
            return TempRegPool.getInstance().getTempInReg(ans, s);
        } else if (JudgeExpElement.isNumber(s)) {
            irType = IRType.Number;
            MIPSIns.li(ans,Integer.parseInt(s));
            return null;
        } else {
            ElementTable elementTable =  APIIRSymTable.
                    getInstance().findElementRecur(tableSymbol,s);
            if (JudgeExpElement.isAddr(elementTable, s)) {
                irType = IRType.ADDR;
                if (elementTable.isGlobal()) {
                    arrAddrGlobal(ans, s);
                } else if (elementTable instanceof ElementFParam) {
                    ElementFParam elementFParam = (ElementFParam) elementTable;
                    arrAddrPara(ans, s, elementFParam.getIndex());
                } else {
                    arrAddrNormal(ans, elementTable.getMemOff());
                }
            }
            return ans;
        }
    }


    /**
     * 操作数是数组地址，说明只能是右操作数
     *
     * @param ans 是返回的寄存器（一般为rightOne或rightTwo）
     * @return
     */
    private void arrAddrGlobal(Reg ans, String arr) throws IOException {
        MIPSIns.la_label(ans, arr);
    }

    private void arrAddrPara(Reg ans, String arr, int index) throws IOException {
        if (index <= 4) {
            Reg reg = Reg.getFParamReg(index);
            MIPSIns.move(ans, reg);
        } else {
            int off = FuncFParam.getOff(index);
            MIPSIns.lw_number_reg(ans, off, Reg.$fp);
        }
    }

    private void arrAddrNormal(Reg ans, int off) throws IOException {
        MIPSIns.add_reg_o(ans, Reg.$fp, off);
    }


    /**
     * @param ans
     * @param name
     * @param tableSymbol
     */
    public void assignVarToTemp(String ans, String name, TableSymbol tableSymbol) throws IOException {
        if (JudgeExpElement.isRET(name)) {
            allocTempAndInit(ans, Reg.$v0);
            return;
        }
        ElementTable elementTable = APIIRSymTable.getInstance().findElementRecur(tableSymbol, name);
        if (elementTable.getDimension() != 0) {
            String arrSub = getArrSubscript(name);
            String arrName = getArrName(name);
            if (elementTable.isGlobal()) {
                lwArrGlobal(ans, arrName, arrSub);
            } else if (elementTable instanceof ElementFParam) {
                ElementFParam elementFParam = (ElementFParam) elementTable;
                lwArrPara(ans, arrSub, elementFParam.getIndex());
            } else {
                int off = elementTable.getMemOff();
                lwArrNormal(ans, arrSub, off);
            }
        } else {
            if (elementTable.isHasReg()) {
                Reg reg = elementTable.getReg();
                allocTempAndInit(ans, reg);
            } else {
                int off = elementTable.getMemOff();
                MIPSIns.lw_number_reg(Reg.rightOne, off, Reg.$fp);
                allocTempAndInit(ans, Reg.rightOne);
            }
        }
    }

    /**
     * 当右值是数组时，说明只有一个操作数，就是数组
     * 所以右侧的寄存器可以随意用，
     * 按照中间代码的设计，左侧只能是临时寄存器
     * 可以临时申请
     */
    private void lwArrGlobal(String ans, String arr, String sub) throws IOException {
        if (JudgeExpElement.isTemp(sub)) {
            Reg reg = TempRegPool.getInstance().getTempInReg(Reg.rightOne, sub);
            MIPSIns.sll(Reg.rightOne, reg, 2);
            MIPSIns.lw_label_reg(Reg.rightOne, arr, Reg.rightOne);
        } else {  //assert number
            int number = Integer.parseInt(sub);
            number = number * 4;
            MIPSIns.lw_label_number(Reg.rightOne, arr, number);
        }
        allocTempAndInit(ans, Reg.rightOne);
    }

    /**
     * 当右值是数组时，说明只有一个操作数，就是数组
     * 所以右侧的寄存器可以随意用，
     * 按照中间代码的设计，左侧只能是临时寄存器
     * 可以临时申请
     */
    private void lwArrPara(String ans, String sub, int index) throws IOException {
        Reg regAddr;
        if (index <= 4) {
            regAddr = Reg.getFParamReg(index);
        } else {
            int off = FuncFParam.getOff(index);
            MIPSIns.lw_number_reg(Reg.rightOne, off, Reg.$fp);
            regAddr = Reg.rightOne;
        }
        lwArrValueInRegTwo(sub, regAddr);
        allocTempAndInit(ans, Reg.rightTwo);
    }

    /**
     * 当右值是数组时，说明只有一个操作数，就是数组
     * 所以右侧的寄存器可以随意用，
     * 按照中间代码的设计，左侧只能是临时寄存器
     * 可以临时申请
     */
    private void lwArrNormal(String ans, String sub, int off) throws IOException {
        MIPSIns.add_reg_o(Reg.rightOne, Reg.$fp, off);
        lwArrValueInRegTwo(sub, Reg.rightOne);
        allocTempAndInit(ans, Reg.rightTwo);
    }

    /**
     * pre：temp是临时变量或者数字
     * @param var 数组名（包括下标）
     * @param temp number 或者 temp
     * @param tableSymbol 符号表
     * @throws IOException
     */
    public void storeTempOrNumberToVar(String var, String temp, TableSymbol tableSymbol) throws IOException {
        ElementTable elementTable = APIIRSymTable.getInstance().findElementRecur(tableSymbol, var);
        Reg value = null;
        if(JudgeExpElement.isTemp(temp)) {
            value = TempRegPool.getInstance().getTempInReg(Reg.rightOne, temp);
        }
        if (elementTable.getDimension() != 0) {
            if(value == null){ //说明要先把数字存到一个寄存器
                MIPSIns.li(Reg.rightOne,Integer.parseInt(temp));
                value = Reg.rightOne;
            }
            String arrSub = getArrSubscript(temp);
            String arrName = getArrName(temp);
            if (elementTable.isGlobal()) {
                swArrGlobal(arrName, arrSub, value);
            } else if (elementTable instanceof ElementFParam) {
                ElementFParam elementFParam = (ElementFParam) elementTable;
                swArrPara(arrSub, elementFParam.getIndex(), value);
            } else {
                int off = elementTable.getMemOff();
                swArrNormal(arrSub, off, value);
            }
        } else {
            if (elementTable.isHasReg()) {
                Reg reg = elementTable.getReg();
                if(value != null) MIPSIns.move(reg, value);
                else MIPSIns.li(reg,Integer.parseInt(temp));
            } else {
                int off = elementTable.getMemOff();
                if(value == null){ //说明要先把数字存到一个寄存器
                    MIPSIns.li(Reg.rightOne,Integer.parseInt(temp));
                    value = Reg.rightOne;
                }
                MIPSIns.sw_number_reg(value, off, Reg.$fp);
            }
        }
    }

    /**
     * 当右值是数组时，说明只有一个操作数，就是数组
     * 所以右侧的寄存器可以随意用，
     * 按照中间代码的设计，左侧只能是临时寄存器
     * 可以临时申请
     */
    private void swArrGlobal(String arr, String sub, Reg regValue) throws IOException {
        if (JudgeExpElement.isTemp(sub)) {
            Reg reg = TempRegPool.getInstance().getTempInReg(Reg.leftTwo, sub);
            MIPSIns.sll(Reg.leftTwo, reg, 2);
            MIPSIns.sw_label_reg(regValue, arr, Reg.leftTwo);
        } else {  //assert number
            int number = Integer.parseInt(sub);
            number = number * 4;
            MIPSIns.sw_label_number(regValue, arr, number);
        }
    }

    /**
     * 当右值是数组时，说明只有一个操作数，就是数组
     * 所以右侧的寄存器可以随意用，
     * 按照中间代码的设计，左侧只能是临时寄存器
     * 可以临时申请
     */
    private void swArrPara(String sub, int index, Reg value) throws IOException {
        Reg regAddr;
        if (index <= 4) {
            regAddr = Reg.getFParamReg(index);
        } else {
            int off = FuncFParam.getOff(index);
            MIPSIns.lw_number_reg(Reg.leftOne, off, Reg.$fp);
            regAddr = Reg.leftOne;
        }
        swArrValue(value, regAddr, sub);
    }

    /**
     * 当右值是数组时，说明只有一个操作数，就是数组
     * 所以右侧的寄存器可以随意用，
     * 按照中间代码的设计，左侧只能是临时寄存器
     * 可以临时申请
     */
    private void swArrNormal(String sub, int off, Reg value) throws IOException {
        MIPSIns.add_reg_o(Reg.leftOne, Reg.$fp, off);
        swArrValue(value, Reg.leftOne, sub);
    }

    /**
     * 会改变regTwo的值
     *
     * @param sub     数组的下标
     * @param regAddr 数组的基地址
     * @throws IOException
     */
    public void lwArrValueInRegTwo(String sub, Reg regAddr) throws IOException {
        if (JudgeExpElement.isTemp(sub)) {
            Reg temp = TempRegPool.getInstance().getTempInReg(Reg.rightTwo, sub);
            MIPSIns.sll(Reg.rightTwo, temp, 2);
            MIPSIns.add_reg_o(Reg.rightTwo, Reg.rightTwo, regAddr);
            MIPSIns.lw_reg(Reg.rightTwo, Reg.rightTwo);
        } else { // assert number
            int number = Integer.parseInt(sub);
            number = number * 4;
            MIPSIns.lw_number_reg(Reg.rightTwo, number, regAddr);
        }
    }

    /**
     * 会改变regTwo的值
     *
     * @param sub     数组的下标
     * @param regAddr 数组的基地址
     * @throws IOException
     */
    public void swArrValue(Reg regValue, Reg regAddr, String sub) throws IOException {
        if (JudgeExpElement.isTemp(sub)) {
            Reg temp = TempRegPool.getInstance().getTempInReg(Reg.leftTwo, sub);
            MIPSIns.sll(Reg.leftTwo, temp, 2);
            MIPSIns.add_reg_o(Reg.leftTwo, Reg.leftTwo, regAddr);
            MIPSIns.sw_reg(regValue, Reg.leftTwo);
        } else { // assert number
            int number = Integer.parseInt(sub);
            number = number * 4;
            MIPSIns.sw_number_reg(regValue, number, regAddr);
        }
    }


    public void oneAssign(String ans, String op, String oper) throws IOException {
        Reg temp = TempRegPool.getInstance().getTempInReg(Reg.rightOne, oper);
        if (op.equals("-")) {
            MIPSIns.not(Reg.rightOne, temp);
            MIPSIns.add_reg_o(Reg.rightOne, Reg.rightOne, 1);
            allocTempAndInit(ans, Reg.rightOne);
        } else {
            MIPSIns.logicalNot(temp);
            allocTempAndInit(ans, temp);
        }
    }

    /**
     * 只知道寄存器的名字，需要分配寄存器，并把计算所得的值算进去。
     */
    public void allocTempAndInit(String tempName, Reg regValue) throws IOException {
        TempRegPool.getInstance().addToPool(tempName);
        if (TempRegPool.getInstance().inReg(tempName)) {
            Reg reg = TempRegPool.getInstance().findTempReg(tempName);
            MIPSIns.move(reg, regValue);
        } else {
            TempRegPool.getInstance().storeToMem(regValue, tempName);
        }
    }


    public void write(String s) throws IOException {
        MIPS.getInstance().write(s);
    }

    public void writeNotNext(String s) throws IOException {
        MIPS.getInstance().writeNotNext(s);
    }

    public String getArrName(String s) {
        if (!s.contains("[")) return s;
        int l = s.indexOf('[');
        int r = s.indexOf(']');
        return s.substring(0, l);
    }

    public String getArrSubscript(String s) {
        if (!s.contains("[")) return null;
        int l = s.indexOf('[');
        int r = s.indexOf(']');
        return s.substring(l + 1, r);
    }
}
