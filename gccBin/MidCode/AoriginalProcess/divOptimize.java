package gccBin.MidCode.AoriginalProcess;

public class divOptimize {
    public static int N = 31; //正整数可以，那负数是需要特判还是没那种可能？
    public static long M;
    public static int l;

    public static void get(int d) {
        // 2^(N+l) <= m * d <= 2^(N+l) + 2^l
        int d_abs = Math.abs(d);
        int temp_l = (int) Math.ceil(Math.log(d_abs) / Math.log(2));
        while (true) {
            long M_min = floor(temp_l, d_abs);
            long M_max = ceil(temp_l, d_abs);
            if (M_min <= M_max) {
                M = M_min;
                l = temp_l;
                temp_l--;
            } else {
                break;
            }
        }
    }

    public static long floor(int l, long d) {
        return (long) Math.ceil(Math.pow(2, N + l) / d);
    }

    public static long ceil(int l, long d) {
        return (long) Math.floor((Math.pow(2, N + l) + Math.pow(2, l)) / d);
    }

}
