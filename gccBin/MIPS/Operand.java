package gccBin.MIPS;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

//可能是什么？:变量【普通变量
// 数组】，  :形式 t1 = a[t2]  或者a[123]//初始化的时候。
// 返回值RET，:形式 t2 = RET；
// 数字
// 自定义变量
public class Operand {
    private String name;

    private String arrName;
    private String subscript;//常数的时候

    public Operand(String name) {
        this.name = name;
    }

    public Operand() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.subscript = " ";
        if (name.contains("[")) {
            int l = name.indexOf("[");
            int r = name.indexOf("]");
            this.arrName = name.substring(0, l);
            if (isNum(name.substring(l + 1, r))) {
                this.subscript =
                        name.substring(l + 1, r);
            }
        }
        this.name = name;
    }

    public boolean isNum(String name) { //bug
        if(name == null || name.length() == 0) return false;
        int len = name.length();
        int i = 0;
        if (name.charAt(0) == '-') {
            i++;
            if (i == len) return false;
        }
        for (; i < len; i++) {
            if (name.charAt(i) < '0' || name.charAt(i) > '9') {
                return false;
            }
        }
        return true;
    }

    public boolean isNum() {
        return  isNum(this.name);
    }

    public boolean isTempVar() {
        String regex = "^t(\\d)*$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(name);
        return matcher.find();
    }

    public boolean isRet() {
        return name.equals("RET");
    }

    public boolean isArray() {
        return name.contains("[");
    }

    public boolean isVar() {
        return !isNum() && !isTempVar() && !isRet() && !isArray();
    }

    public String getArrName() {
        return arrName;
    }

    public boolean isPrintfD() {
        return this.name.equals("%d");
    }

    public String getSubscript() {
        return subscript;
    }
}
