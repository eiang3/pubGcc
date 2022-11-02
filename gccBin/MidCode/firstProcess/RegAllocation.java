package gccBin.MidCode.firstProcess;

import gccBin.MIPS.tool.Reg;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class RegAllocation {
    private static RegAllocation regAllocation;
    /***
     * 加入未划分的冲突图，ok更新符号表表项，ok对相应lines和进行重命名。
     */
    private HashMap<String, VarWeb> newName2Web; //最后保存下来的全部冲突变量z

    private final ArrayList<VarWeb> orderUse = new ArrayList<>();
    private final HashSet<VarWeb> orderSearch = new HashSet<>();

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
        if(leave.size() == 0) return; //没有网

        do {
            VarWeb varWeb = getOneNode();
            if (varWeb != null) {
                add(varWeb);
                this.leave.remove(varWeb);
            } else {
                selectOneNodeInMem();
            }
        }while (leave.size()!=1);

        VarWeb lastOne = leave.get(0);
        lastOne.setReg(Reg.$s0);
        for(int i = orderUse.size()-1;i>=0;i--){
            VarWeb varWeb = orderUse.get(i);
            varWeb.allocReg(leave);
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

    /**
     * 变量名和对应的变量
     * @param newName2Web
     */
    public void addNodeToLeaveSet(HashMap<String, VarWeb> newName2Web) {
        this.newName2Web = newName2Web;
        // bug ?
        this.leave = new ArrayList<>(newName2Web.values());
    }

}
