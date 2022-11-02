package gccBin.MIPS.tool;

import SymbolTableBin.APIIRSymTable;
import SymbolTableBin.Element.ElementTable;
import SymbolTableBin.TableSymbol;

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

    public void addRegToStore(Reg reg) {
        regNeedToStore.add(reg);
    }

    public void pushSReg() throws IOException {
        int number = regNeedToStore.size();
        int count = 0;
        for (Reg reg : regNeedToStore) {
            MIPSIns.sw_number_reg(reg, count * 4, Reg.$sp);
            count++;
        }
        MIPSIns.sw_number_reg(Reg.$ra, count * 4, Reg.$sp);
        MIPSIns.add_reg_o(Reg.$sp, Reg.$sp, (number + 1) * 4);
    }

    public void popSReg() throws IOException {
        int number = regNeedToStore.size();
        MIPSIns.sub_reg_o(Reg.$sp, Reg.$sp, (number + 1) * 4);
        int count = 0;
        for (Reg reg : regNeedToStore) {
            MIPSIns.lw_number_reg(reg, count * 4, Reg.$sp);
            count++;
        }
        MIPSIns.lw_number_reg(Reg.$ra, count * 4, Reg.$sp);
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

    public void allocationVarMem(String name, TableSymbol tableSymbol) {
        ElementTable elementTable = APIIRSymTable.getInstance().findElementRecur(tableSymbol, name);
        if (elementTable.isUseless()) {
            return;
        }
        if (elementTable != null && !elementTable.isHasReg()) {
            elementTable.setMemOff(fpOff);
            addFpOff(1);
        } else if (elementTable != null && elementTable.isHasReg()) {
            Reg reg = elementTable.getReg();
            this.regNeedToStore.add(reg);
        }
    }

    public int allocationTempMem() {
        int ret = fpOff;
        addFpOff(1);
        return ret;
    }

    public void addFpOff(int x) {
        fpOff = fpOff + x * 4;
    }
}
