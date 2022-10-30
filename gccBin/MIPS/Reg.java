package gccBin.MIPS;

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

    private Reg(String name){
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
