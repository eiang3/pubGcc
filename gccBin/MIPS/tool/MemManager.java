package gccBin.MIPS.tool;

import SymbolTableBin.APIIRSymTable;
import SymbolTableBin.ElementTable;
import SymbolTableBin.TableSymbol;

public class MemManager {
    private static MemManager memManager;

    private int fpOff;

    private MemManager() {
        fpOff = 0x0;
    }

    public static MemManager getInstance() {
        if (memManager == null) {
            memManager = new MemManager();
        }
        return memManager;
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
            addFpOff(size * 4);
        }
    }

    public void allocationVarMem(String name, TableSymbol tableSymbol) {
        ElementTable elementTable = APIIRSymTable.getInstance().findElementRecur(tableSymbol, name);
        if (elementTable != null && !elementTable.isHasReg()) {
            elementTable.setMemOff(fpOff);
            addFpOff(4);
        }
    }

    public int allocationTempMem() {
        int ret = fpOff;
        addFpOff(4);
        return ret;
    }

    public void addFpOff(int x) {
        fpOff = fpOff + x;
    }
}
