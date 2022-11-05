package gccBin.MidCode.Line;

import SymbolTableBin.TableSymbol;
import gccBin.MIPS.SubOp;
import gccBin.MidCode.Judge;
import gccBin.UnExpect;

import java.util.Objects;

/**
 * arr int a[4]
 * 先未分析类型
 */

public class ArrayDefLine extends Line {
    private String name;
    private int len;

    public ArrayDefLine(String s, int line, TableSymbol tableSymbol, String[] elements) {
        super(s, line, tableSymbol);
        parse(elements[2]);
    }

    private void parse(String arr) {
        this.name = SubOp.getArrName(arr);
        String l = Objects.requireNonNull(SubOp.getArrSubscript(arr));
        if (Judge.isNumber(l)) {
            this.len = Integer.parseInt(l);
        } else UnExpect.notNum(l);
    }

    public String getName() {
        return name;
    }

    public int getLen() {
        return len;
    }
}
