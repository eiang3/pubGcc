package gccBin.MIPS.tool;

import SymbolTableBin.APIIRSymTable;
import SymbolTableBin.Element.ElementTable;
import SymbolTableBin.Element.ElementVar;
import SymbolTableBin.TableSymbol;
import gccBin.UnExpect;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;

public class MemManager {
    private static MemManager memManager;

    private int fpOff;
    private HashSet<Reg> regNeedToStore;

    private HashSet<Reg> regFParamNeedToStore;

    private MemManager() {
        fpOff = 0x0;
        regNeedToStore = new HashSet<>();
        regFParamNeedToStore = new HashSet<>();
    }

    public static MemManager getInstance() {
        if (memManager == null) {
            memManager = new MemManager();
        }
        return memManager;
    }

    public void pushS_TReg() throws IOException {
        int number = regNeedToStore.size();
        ArrayList<Reg> tempRegInUse = TempRegPool.getInstance().getTempRegInUse();
        number = number + tempRegInUse.size();

        MipsIns.sub_ans_reg_regOrNum(Reg.$sp, Reg.$sp, (number + 1) * 4);
        int count = 0;
        for (Reg reg : regNeedToStore) {
            MipsIns.sw_value_num_baseReg(reg, count * 4, Reg.$sp);
            count++;
        }
        for (Reg reg : tempRegInUse) {
            MipsIns.sw_value_num_baseReg(reg, count * 4, Reg.$sp);
            count++;
        }
        MipsIns.sw_value_num_baseReg(Reg.$ra, count * 4, Reg.$sp);
    }

    public void popS_TReg() throws IOException {
        int number = regNeedToStore.size();
        ArrayList<Reg> tempRegInUse = TempRegPool.getInstance().getTempRegInUse();
        number = number + tempRegInUse.size();

        int count = 0;
        for (Reg reg : regNeedToStore) {
            MipsIns.lw_ans_num_baseReg(reg, count * 4, Reg.$sp);
            count++;
        }
        for (Reg reg : tempRegInUse) {
            MipsIns.lw_ans_num_baseReg(reg, count * 4, Reg.$sp);
            count++;
        }
        MipsIns.lw_ans_num_baseReg(Reg.$ra, count * 4, Reg.$sp);
        MipsIns.add_ans_reg_regOrNum(Reg.$sp, Reg.$sp, (number + 1) * 4);
    }

    public void enterANewFunc() {
        this.fpOff = 0;
        regNeedToStore = new HashSet<>();
        regFParamNeedToStore = new HashSet<>();
    }

    public int getFpOff() {
        return fpOff;
    }

    /**
     * 对于一个变量，设置其对应的符号表项的偏移，并更新off(传入大小是字长)
     *
     * @param name        *
     * @param size        *
     * @param tableSymbol *
     */
    public void allocationArrMem(String name, int size, TableSymbol tableSymbol) {
        //ElementTable elementTable = APIIRSymTable.getInstance().findElementRecur(tableSymbol, name);
        ElementTable elementTable = APIIRSymTable.getInstance().findElementInSumTable(name);
        if (elementTable != null) {
            elementTable.setMemOff(fpOff);
            addFpOff(size);
        }
    }

    /**
     * 1.将没有分配到寄存器的变量分配活动记录空间
     * 2.分配到寄存器的变量加入调用函数时要保存的寄存器
     * 3.无用的变量抛弃
     *
     * @param name        变量的名字
     * @param tableSymbol 变量对应的符号表项
     */
    public void handleVar(String name, TableSymbol tableSymbol) {
        ArrayList<ElementVar> elementVars = tableSymbol.findReNamesVar(name);

        for (ElementVar element : elementVars) {
            if (element == null) {
                UnExpect.error();
            }
            if (element.isUseless()) {
                return;
            }
            if (!element.isHasReg()) {
                element.setMemOff(fpOff);
                addFpOff(1);
            } else if (element.isHasReg()) {
                Reg reg = element.getReg();
                this.regNeedToStore.add(reg);
            }
        }
    }

    public int allocation_A_Temp_Mem() {
        int ret = fpOff;
        addFpOff(1);
        return ret;
    }

    public void addFpOff(int x) {
        fpOff = fpOff + x * 4;
    }

    public void addTo_AF_StoreReg(Reg reg) {
        this.regFParamNeedToStore.add(reg);
    }

    public void pushAReg() throws IOException {
        int number = regFParamNeedToStore.size();
        if (number != 0) {
            MipsIns.sub_ans_reg_regOrNum(Reg.$sp, Reg.$sp, number * 4);
        }
        int count = 0;
        for (Reg reg : regFParamNeedToStore) {
            MipsIns.sw_value_num_baseReg(reg, count * 4, Reg.$sp);
            count++;
        }
    }

    public void popAReg() throws IOException {
        int number = regFParamNeedToStore.size();
        int count = 0;
        for (Reg reg : regFParamNeedToStore) {
            MipsIns.lw_ans_num_baseReg(reg, count * 4, Reg.$sp);
            count++;
        }
        if (number != 0) {
            MipsIns.add_ans_reg_regOrNum(Reg.$sp, Reg.$sp, number * 4);
        }
    }

    public boolean inRegToStore(Reg reg) {
        return regFParamNeedToStore.contains(reg) || regNeedToStore.contains(reg);
    }
}
