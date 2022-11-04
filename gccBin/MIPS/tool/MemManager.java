package gccBin.MIPS.tool;

import SymbolTableBin.APIIRSymTable;
import SymbolTableBin.Element.ElementTable;
import SymbolTableBin.TableSymbol;
import gccBin.UnExpect;

import java.io.IOException;
import java.util.ArrayList;

public class MemManager {
    private static MemManager memManager;

    private int fpOff;
    private ArrayList<Reg> regNeedToStore;

    private MemManager() {
        fpOff = 0x0;
        regNeedToStore = new ArrayList<>();
    }

    public static MemManager getInstance() {
        if (memManager == null) {
            memManager = new MemManager();
        }
        return memManager;
    }

    public void pushSReg() throws IOException {
        int number = regNeedToStore.size();
        int count = 0;
        for (Reg reg : regNeedToStore) {
            MipsIns.sw_value_num_baseReg(reg, count * 4, Reg.$sp);
            count++;
        }
        MipsIns.sw_value_num_baseReg(Reg.$ra, count * 4, Reg.$sp);
        MipsIns.add_ans_reg_regOrNum(Reg.$sp, Reg.$sp, (number + 1) * 4);
    }

    public void popSReg() throws IOException {
        int number = regNeedToStore.size();
        MipsIns.sub_ans_reg_regOrNum(Reg.$sp, Reg.$sp, (number + 1) * 4);
        int count = 0;
        for (Reg reg : regNeedToStore) {
            MipsIns.lw_ans_num_baseReg(reg, count * 4, Reg.$sp);
            count++;
        }
        MipsIns.lw_ans_num_baseReg(Reg.$ra, count * 4, Reg.$sp);
    }

    public void enterANewFunc() {
        this.fpOff = 0;
        regNeedToStore = new ArrayList<>();
    }

    public int getFpOff() {
        return fpOff;
    }

    /**
     * 对于一个变量，设置其对应的符号表项的偏移，并更新off(传入大小是字长)
     *
     * @param name
     * @param size
     * @param tableSymbol
     */
    public void allocationArrMem(String name, int size, TableSymbol tableSymbol) {
        ElementTable elementTable = APIIRSymTable.getInstance().findElementRecur(tableSymbol, name);
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
        ElementTable elementTable = APIIRSymTable.getInstance().findElementRecur(tableSymbol, name);

        if (elementTable == null) {
            UnExpect.error();
        }

        if (elementTable.isUseless()) {
            return;
        }

        if (!elementTable.isHasReg()) {
            elementTable.setMemOff(fpOff);
            addFpOff(1);
        } else if (elementTable.isHasReg()) {
            Reg reg = elementTable.getReg();
            this.regNeedToStore.add(reg);
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
}
