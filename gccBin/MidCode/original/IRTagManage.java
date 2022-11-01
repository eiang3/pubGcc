package gccBin.MidCode.original;

public class IRTagManage {
    private static IRTagManage instance;
    private int count;
    private int ret;
    private int label;

    private IRTagManage() {
        this.count = 0;
        this.ret = 0;
        this.label = 0;
    }

    public static IRTagManage getInstance() {
        if (instance == null) {
            instance = new IRTagManage();
        }
        return instance;
    }

    public String newVar() {
        return "$t" + (count++);
    }

    public String  newRet(){
        return "RET";
    }

    public String newLabel(){
        return "label" + (label++);
    }
}
