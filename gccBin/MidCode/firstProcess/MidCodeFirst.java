package gccBin.MidCode.firstProcess;

import SymbolTableBin.TableSymbol;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.regex.Pattern;

/**
 * 主要是进行冲突图的构建工作
 */
public class MidCodeFirst {
    private static MidCodeFirst midCodeFirst;

    private BufferedReader bufferedReader;

    private final HashMap<String, VarNode> name2Node;

    private TableSymbol nowTable; //

    private String line;

    private MidCodeFirst(){
        name2Node = new HashMap<>();
    }

    public void beginFirstScan() throws IOException {
        readLine();
        while(line != null){
            if(!isAnnotate()){

            }
            readLine();
        }
    }

    public void inTableSymbol(){
        nowTable = nowTable.getNextChild();
    }

    public void leaveTableSymbol(){
        nowTable = nowTable.getFather();
    }

    public void setNowTable(TableSymbol nowTable) {
        this.nowTable = nowTable;
    }

    private boolean isAnnotate(){
        if(line.length() < 2) return false;
        return line.charAt(0) == '#' && line.charAt(1) == '#';
    }

    public static MidCodeFirst getInstance(){
        if(midCodeFirst == null){
            midCodeFirst = new MidCodeFirst();
        }
        return midCodeFirst;
    }

    public void open() throws IOException {
        File inputFile = new File("midCode.txt");
        bufferedReader = new BufferedReader(
                new InputStreamReader(Files.newInputStream(inputFile.toPath()), "UTF-8"));
    }

    public void close() throws IOException {
        this.bufferedReader.close();
    }

    public void readLine() throws IOException {
         line = this.bufferedReader.readLine();
    }
}
