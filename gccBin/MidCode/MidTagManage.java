package gccBin.MidCode;

public class MidTagManage {
    private static MidTagManage instance;
    private int count;
    private int ret;
    private int label;

    private MidTagManage() {
        this.count = 0;
        this.ret = 0;
        this.label = 0;
    }

    public static MidTagManage getInstance() {
        if (instance == null) {
            instance = new MidTagManage();
        }
        return instance;
    }

    public String newVar() {
        return "t" + (count++);
    }

    public String  newRet(){
        return "RET";
    }

    public String newLabel(){
        return "label" + (label++);
    }
}
