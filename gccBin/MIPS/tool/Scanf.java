package gccBin.MIPS.tool;

import gccBin.MIPS.Reg;
import gccBin.MIPS.tool.Compute;

import java.io.FileWriter;
import java.io.IOException;

public class Scanf {
    public static void scanf(FileWriter fileWriter) throws IOException {
        fileWriter.write("li $v0,5\n");
        fileWriter.write("syscall\n");
        //Compute.get().push(Reg.V0);
    }
}
