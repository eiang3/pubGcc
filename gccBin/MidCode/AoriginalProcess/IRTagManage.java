package gccBin.MidCode.AoriginalProcess;

import gccBin.MidCode.Judge;

import java.util.HashMap;

public class IRTagManage {
    private static IRTagManage instance;
    private int count;
    private int label;

    private final HashMap<String, Integer> temp2useTimes;

    private IRTagManage() {
        this.count = 0;
        this.label = 0;
        temp2useTimes = new HashMap<>();
    }

    public static IRTagManage getInstance() {
        if (instance == null) {
            instance = new IRTagManage();
        }
        return instance;
    }

    public String newVar() {
        String ret = "$t" + (count++);
        this.temp2useTimes.put(ret, 1);
        return ret;
    }

    public String newVar(int useTimes) {
        String ret = "$t" + (count++);
        this.temp2useTimes.put(ret, useTimes);
        return ret;
    }

    public void addUse(String temp) {
        if (!Judge.isTemp(temp)) return;
        if (!this.temp2useTimes.containsKey(temp)) {
            this.temp2useTimes.put(temp, 1);
        }
        int a = this.temp2useTimes.get(temp);
        this.temp2useTimes.put(temp, a + 1);
    }

    public void addUse(String temp, int add) {
        if (!Judge.isTemp(temp)) return;
        if (!this.temp2useTimes.containsKey(temp)) {
            this.temp2useTimes.put(temp, add);
        }
        int a = this.temp2useTimes.get(temp);
        this.temp2useTimes.put(temp, a + add);
    }

    public String newLabel() {
        return "label" + (label++);
    }

    /**
     * 假设只使用一次，就是真的删除，返回true
     * 而如果使用多次，就是引用减1，返回false
     *
     * @param str *
     * @return *
     */
    public boolean delete(String str) {
        if (!Judge.isTemp(str)) return false;
        if (!temp2useTimes.containsKey(str)) {
            return false;
        }
        int indexes = temp2useTimes.get(str);
        if (indexes == 1) {
            temp2useTimes.remove(str);
            return true;
        } else {
            temp2useTimes.put(str, indexes - 1);
            return false;
        }
    }

}
