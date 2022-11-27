package gccBin.MidCode.AfirstProcess;

import java.util.BitSet;
import java.util.HashSet;

public class SetOp {

    /**
     * 求两个集合的并集
     *
     * @param A *
     * @param B *
     * @return A并B
     */
    public static BitSet union(BitSet A, BitSet B) {
        BitSet ret = (BitSet) A.clone();
        ret.or(B);
        return ret;
    }

    /**
     * 求两个集合的差集
     *
     * @param A *
     * @param B *
     * @return A - B
     */
    public static BitSet differenceSet(BitSet A, BitSet B) {
        BitSet ret = (BitSet) A.clone();
        ret.and(B);
        ret.xor(A);
        return ret;
    }

    /**
     * 在数据流分析和活跃变量分析的时候需要用到的计算
     *
     * @param A *
     * @param B *
     * @param C *
     * @return A ∪ ( B - C )
     */
    public static BitSet streamSet(BitSet A, BitSet B, BitSet C) {
        BitSet ret = differenceSet(B, C);
        ret = union(A, ret);
        return ret;
    }


    /**
     * 求两个集合的并集
     *
     * @param A *
     * @param B *
     * @return A并B
     */
    public static HashSet<String> union(HashSet<String> A, HashSet<String> B) {
        HashSet<String> ret = new HashSet<>(A);
        ret.addAll(B);
        return ret;
    }

    /**
     * 求两个集合的差集
     *
     * @param A *
     * @param B *
     * @return A - B
     */
    public static HashSet<String> differenceSet(HashSet<String> A, HashSet<String> B) {
        HashSet<String> ret = new HashSet<>(A);
        ret.removeAll(B);
        return ret;
    }

    /**
     * 在数据流分析和活跃变量分析的时候需要用到的计算
     *
     * @param A *
     * @param B *
     * @param C *
     * @return A ∪ ( B - C )
     */
    public static HashSet<String> streamSet(HashSet<String> A, HashSet<String> B, HashSet<String> C) {
        HashSet<String> ret = differenceSet(B,C);
        ret = union(ret,A);
        return ret;
    }

}
