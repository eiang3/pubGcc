package gccBin.MidCode.Line;

import SymbolTableBin.APIIRSymTable;
import SymbolTableBin.Element.ElementTable;
import SymbolTableBin.TableSymbol;
import gccBin.MIPS.SubOp;
import gccBin.MIPS.tool.Reg;
import gccBin.MIPS.tool.TempRegPool;
import gccBin.MIPS.tool.MipsIns;
import gccBin.MidCode.Judge;
import gccBin.UnExpect;

import java.io.IOException;

/**
 * 数组指针。
 * exp : 123 | $tx | a | a[$tx] | a[a] | a[123]
 * <p>
 * a[0] = exp
 * j = exp  【3】
 * a = b (* | - | * | / | % | >> | << ) c 【5】
 * a = ( - | ! ) b 【4】
 * j = 3
 * i = RET
 * z = a[x]
 */
public class AssignLine extends Line {
    private String t1;
    private String t2;
    private String op;
    private String ans;

    private boolean t1IsUse; //代表着是非全局var,以后可以直接用
    private boolean t2IsUse;
    private final boolean ansIsGen;

    private boolean pureAssign; //x = y;
    private boolean oneOpr; // x = - | ! y
    private boolean twoOpr; // x = y (+-*/%>><<) w


    public AssignLine(String s, int line, TableSymbol tableSymbol, String[] ele) {
        super(s, line, tableSymbol);
        pureAssign = false;
        oneOpr = false;
        twoOpr = false;
        ans = ele[0];
        ansIsGen = super.addGen(ans);
        if (ele.length == 3) {
            pureAssign = true;
            t1 = ele[2];
            t1IsUse = super.addUse(t1);
        } else if (ele.length == 4) {
            oneOpr = true;
            t1 = ele[3];
            op = ele[2];
            t1IsUse = super.addUse(t1);
        } else if (ele.length == 5) {
            twoOpr = true;
            t1 = ele[2];
            op = ele[3];
            t2 = ele[4];
            t1IsUse = super.addUse(t1);
            t2IsUse = super.addUse(t2);
        }
    }

    @Override
    public void renameGen(String old, String name) {
        if (ansIsGen && ans.equals(old)) {
            ans = name;
            super.setGen(name);
        }
    }

    @Override
    public void renameUse(String old, String name) {
        if (t1IsUse && t1.equals(old)) {
            t1 = name;
            super.replaceOneUse(old, name);
        }
        if (t2IsUse && t2.equals(old)) {
            t2 = name;
            super.replaceOneUse(old, name);
        }
    }

    public String getT1() {
        return t1;
    }

    public String getT2() {
        return t2;
    }

    public String getOp() {
        return op;
    }

    public String getAns() {
        return ans;
    }

    public boolean isPureAssign() {
        return pureAssign;
    }

    public boolean isOneOpr() {
        return oneOpr;
    }

    public boolean isTwoOpr() {
        return twoOpr;
    }
}
