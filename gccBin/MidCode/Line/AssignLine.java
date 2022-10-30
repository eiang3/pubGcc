package gccBin.MidCode.Line;

import SymbolTableBin.TableSymbol;

/**
 * 数组指针。
 * exp : 123 | $tx | a | a[$tx] | a[a] | a[123]
 *
 * a[0] = exp
 * j = exp  【3】
 * a = b (* | - | * | / | % | >> | << ) c 【5】
 * a = ( - | ! ) b 【4】
 * j = 3
 * i = RET
 * z = a[x]
 */
public class AssignLine extends Line{
    private String t1;
    private String t2;
    private String op;
    private String ans;

    public AssignLine(String s, int line,TableSymbol tableSymbol,String[] ele){
        super(s,line,tableSymbol);
        ans = ele[0];
        if(ele.length == 3){
            t1 = ele[2];
        } else if (ele.length == 4) {
            t1 = ele[3];
            op = ele[2];
        } else if(ele.length == 5){
            t1 = ele[2];
            op = ele[3];
            t2 = ele[4];
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
}
