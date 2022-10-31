package gccBin.MidCode.firstProcess;

import gccBin.MIPS.Reg;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class RegAllocation {
    private static RegAllocation regAllocation;
    /***
     * 加入未划分的冲突图，ok更新符号表表项，ok对相应lines和进行重命名。
     */
    private HashMap<String, VarWeb> newName2Web; //最后保存下来的全部冲突变量z

    private ArrayList<VarWeb> orderUse = new ArrayList<>();
    private HashSet<VarWeb> orderSearch = new HashSet<>();

    private ArrayList<VarWeb> leave;

    private int k = 8;

    private RegAllocation() {
    }

    public static RegAllocation getInstance() {
        if (regAllocation == null) {
            regAllocation = new RegAllocation();
        }
        return regAllocation;
    }

    public void add(VarWeb varWeb) {
        this.orderUse.add(varWeb);
        this.orderSearch.add(varWeb);
    }

    public void finishRegAllocation() {
        do {
            VarWeb varWeb = getOneNode();
            if (varWeb != null) {
                add(varWeb);
            } else {
                selectOneNodeInMem();
            }
        }while (leave.size()!=1);

        VarWeb lastOne = leave.get(0);
        lastOne.setReg(Reg.S0);

        for(int i = orderUse.size()-1;i>=0;i--){
            VarWeb varWeb = orderUse.get(i);
            varWeb.setReg(leave);
            this.leave.add(varWeb);
        }
    }

    /**
     * Step one : find a node link less than
     */
    public VarWeb getOneNode() {
        for (VarWeb varWeb : leave) {
            if (varWeb.getClashSub().size() < k) {
                return varWeb;
            }
        }
        return null;
    }

    /**
     *
     */
    public void selectOneNodeInMem() {
        VarWeb varWeb = leave.get(0);
        leave.remove(varWeb);
        varWeb.removeFromGraph();
    }

    public void setNewName2Web(HashMap<String, VarWeb> newName2Web) {
        this.newName2Web = newName2Web;
        // bug ?
        this.leave = new ArrayList<>(newName2Web.values());
    }

}
