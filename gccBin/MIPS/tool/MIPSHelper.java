package gccBin.MIPS.tool;

import GramTree.Element.FuncFParam;
import SymbolTableBin.*;
import SymbolTableBin.Element.ElementFParam;
import SymbolTableBin.Element.ElementTable;
import gccBin.MIPS.MIPS;
import gccBin.MIPS.SubOp;
import gccBin.MidCode.Judge;
import gccBin.UnExpect;

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

    /**
     * 并将应该写入mips的信息传出,将取出相应的数到特定的寄存器，但是对于数字的话，不需要存到
     * 寄存器里，因为数字可以直接计算
     * 如果值是保存在寄存器里的，就输出寄存器，如果值是保存在
     * 内存里的，就先把值取到临时寄存器
     *
     * @num:是右侧表达式的第几个操作数
     */
    public Reg getValue(Reg ans, TableSymbol tableSymbol, String s) throws IOException {
        ElementTable elementTable = APIIRSymTable.
                getInstance().findElementRecur(tableSymbol, s);
        if (Judge.isAddr(elementTable, s)) {
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


    /**
     * 操作数是数组地址，说明只能是右操作数
     *
     * @param ans 是返回的寄存器（一般为rightOne或rightTwo）
     * @return
     */
    private void arrAddrGlobal(Reg ans, String arr) throws IOException {
        mipsIns.la_label(ans, arr);
    }

    private void arrAddrPara(Reg ans, String arr, int index) throws IOException {
        if (index <= 4) {
            Reg reg = Reg.getFParamReg(index);
            mipsIns.move(ans, reg);
        } else {
            int off = FuncFParam.getOff(index);
            mipsIns.lw_number_reg(ans, off, Reg.$fp);
        }
    }

    private void arrAddrNormal(Reg ans, int off) throws IOException {
        mipsIns.add_reg_o(ans, Reg.$fp, off);
    }


    /**
     * @param ans
     * @param name
     * @param tableSymbol
     */
    public void assignSomeToTemp(String ans, String name, TableSymbol tableSymbol) throws IOException {
        if (Judge.isRET(name)) {
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
                mipsIns.lw_number_reg(Reg.r1, off, Reg.$fp);
                allocTempAndInit(ans, Reg.r1);
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
        if (Judge.isTemp(sub)) {
            Reg reg = TempRegPool.getInstance().getTempInReg(Reg.r1, sub);
            mipsIns.sll(Reg.r1, reg, 2);
            mipsIns.lw_label_reg(Reg.r1, arr, Reg.r1);
        } else {  //assert number
            int number = Integer.parseInt(sub);
            number = number * 4;
            mipsIns.lw_label_number(Reg.r1, arr, number);
        }
        allocTempAndInit(ans, Reg.r1);
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
            mipsIns.lw_number_reg(Reg.r1, off, Reg.$fp);
            regAddr = Reg.r1;
        }
        lwArrValueInRegTwo(sub, regAddr);
        allocTempAndInit(ans, Reg.r2);
    }

    /**
     * 当右值是数组时，说明只有一个操作数，就是数组
     * 所以右侧的寄存器可以随意用，
     * 按照中间代码的设计，左侧只能是临时寄存器
     * 可以临时申请
     */
    private void lwArrNormal(String ans, String sub, int off) throws IOException {
        mipsIns.add_reg_o(Reg.r1, Reg.$fp, off);
        lwArrValueInRegTwo(sub, Reg.r1);
        allocTempAndInit(ans, Reg.r2);
    }

    /**
     * pre：temp是临时变量或者数字
     *
     * @param var         数组名（包括下标）
     * @param temp        number 或者 temp
     * @param tableSymbol 符号表
     * @throws IOException
     */
    public void assignExpToVar(String var, String temp, TableSymbol tableSymbol) throws IOException {
        ElementTable elementTable = APIIRSymTable.getInstance().findElementRecur(tableSymbol, var);
        Reg value = null;
        if (Judge.isTemp(temp)) {
            value = TempRegPool.getInstance().getTempInReg(Reg.r1, temp);
        }
        if (elementTable.getDimension() != 0) {
            if (value == null) { //说明要先把数字存到一个寄存器
                mipsIns.li(Reg.r1, Integer.parseInt(temp));
                value = Reg.r1;
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
            if (elementTable.isGlobal()) {
                storeVarGlobal(var, value, temp);
            } else if (elementTable instanceof ElementFParam) {
                ElementFParam elementFParam = (ElementFParam) elementTable;
                storeVarPara(elementFParam.getIndex(), value, temp);
            } else {
                storeVar(elementTable, value, temp);
            }
        }
    }

    /**
     * 将值存到参数里
     *
     * @param index 参数索引 1 - 4+
     * @param value 如果是temp是临时寄存器的话，保存临时寄存器的值，否则是null(数字)
     * @throws IOException
     */
    private void storeVarPara(int index, Reg value, String temp) throws IOException {
        if (index <= 4) {
            Reg reg = Reg.getFParamReg(index);
            if (value == null) mipsIns.li(reg, Integer.parseInt(temp));
            else mipsIns.move(reg, value);
        } else {
            int off = FuncFParam.getOff(index);
            if (value == null) {
                mipsIns.li(Reg.r1, Integer.parseInt(temp));
                mipsIns.sw_number_reg(Reg.r1, off, Reg.$fp);
            } else {
                mipsIns.sw_number_reg(value, off, Reg.$fp);
            }
        }
    }

    /**
     * 把数据存到一个数中
     *
     * @param elementTable
     * @param value
     * @param temp
     * @throws IOException
     */
    public void storeVar(ElementTable elementTable, Reg value, String temp) throws IOException {
        if (elementTable.isHasReg()) {
            Reg reg = elementTable.getReg();
            if (value != null) mipsIns.move(reg, value);
            else mipsIns.li(reg, Integer.parseInt(temp));
        } else {
            int off = elementTable.getMemOff();
            if (value == null) { //说明要先把数字存到一个寄存器
                mipsIns.li(Reg.r1, Integer.parseInt(temp));
                value = Reg.r1;
            }
            mipsIns.sw_number_reg(value, off, Reg.$fp);
        }
    }

    /**
     * 对全局变量存值
     *
     * @param var
     * @param value
     * @throws IOException
     */
    public void storeVarGlobal(String var, Reg value, String temp) throws IOException {
        if (value == null) { //说明要先把数字存到一个寄存器
            mipsIns.li(Reg.r1, Integer.parseInt(temp));
            value = Reg.r1;
        }
        mipsIns.sw_label_number(value, var, 0);
    }

    /**
     * 当右值是数组时，说明只有一个操作数，就是数组
     * 所以右侧的寄存器可以随意用，
     * 按照中间代码的设计，左侧只能是临时寄存器
     * 可以临时申请
     */
    private void swArrGlobal(String arr, String sub, Reg regValue) throws IOException {
        if (Judge.isTemp(sub)) {
            Reg reg = TempRegPool.getInstance().getTempInReg(Reg.l2, sub);
            mipsIns.sll(Reg.l2, reg, 2);
            mipsIns.sw_label_reg(regValue, arr, Reg.l2);
        } else {  //assert number
            int number = Integer.parseInt(sub);
            number = number * 4;
            mipsIns.sw_label_number(regValue, arr, number);
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
            mipsIns.lw_number_reg(Reg.l1, off, Reg.$fp);
            regAddr = Reg.l1;
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
        mipsIns.add_reg_o(Reg.l1, Reg.$fp, off);
        swArrValue(value, Reg.l1, sub);
    }

    /**
     * 会改变regTwo的值
     *
     * @param sub     数组的下标
     * @param regAddr 数组的基地址
     * @throws IOException
     */
    public void lwArrValueInRegTwo(String sub, Reg regAddr) throws IOException {
        if (Judge.isTemp(sub)) {
            Reg temp = TempRegPool.getInstance().getTempInReg(Reg.r2, sub);
            mipsIns.sll(Reg.r2, temp, 2);
            mipsIns.add_reg_o(Reg.r2, Reg.r2, regAddr);
            mipsIns.lw_reg(Reg.r2, Reg.r2);
        } else { // assert number
            int number = Integer.parseInt(sub);
            number = number * 4;
            mipsIns.lw_number_reg(Reg.r2, number, regAddr);
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
        if (Judge.isTemp(sub)) {
            Reg temp = TempRegPool.getInstance().getTempInReg(Reg.l2, sub);
            mipsIns.sll(Reg.l2, temp, 2);
            mipsIns.add_reg_o(Reg.l2, Reg.l2, regAddr);
            mipsIns.sw_reg(regValue, Reg.l2);
        } else { // assert number
            int number = Integer.parseInt(sub);
            number = number * 4;
            mipsIns.sw_number_reg(regValue, number, regAddr);
        }
    }

    /**
     * 当temp出现在等式左边时，说明这是temp的第一次定义/使用，
     * 需要分配新的temp空间（地址或者是t寄存器）
     *
     * @param tempName
     * @param regValue
     * @throws IOException
     */
    public void allocTempAndInit(String tempName, Reg regValue) throws IOException {
        Reg reg = TempRegPool.getInstance().addToPool(tempName);
        if (reg == null) {
            TempRegPool.getInstance().storeToMem(regValue, tempName);
        } else {
            mipsIns.move(reg, regValue);
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
