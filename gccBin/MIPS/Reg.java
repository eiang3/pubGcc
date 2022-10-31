package gccBin.MIPS;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public enum Reg {
    RA("$ra"),
    ZERO("$zero"),
    A0("$a0"),
    V0("$v0"),
    S0("$s0"),
    S1("$s1"),
    S2("$s2"),
    S3("$s3"),
    S4("$s4"),
    S5("$s5"),
    S6("$s6"),
    S7("$s7"),
    T0("$t0"),
    T1("$t1"),
    T2("$t2"),
    T3("$t3"),
    T4("$t4"),
    T5("$t5"),
    T6("$t6"),
    T7("$t7"),
    T8("$t8"),
    T9("$t9");

    private final String name;

    private Reg(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }


    //***********************************************
    // 待续
    public static Reg getAStoreReg(ArrayList<Reg> arr) {
        if (!arr.contains(S0)) {
            return S0;
        } else if (!arr.contains(S1)) {

        }
        return S7;
    }

    //***********************************************
    // 待续
    public static Reg getATempReg(HashMap<Reg,String> arr) {
        if (!arr.containsKey(T0)) {
            return T0;
        } else if (!arr.containsKey(T1)) {

        }
        return S7;
    }
}
