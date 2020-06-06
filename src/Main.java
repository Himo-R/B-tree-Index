import java.io.IOException;
import java.io.RandomAccessFile;

public class Main {
    public static void main(String[] args) throws IOException {
        System.out.println("himo");
        B_Tree t = new B_Tree();
        t.M=4;
        String FileName="B-tree Index.bin";
        int numberOfRecords = 5;
        t.CreateIndexFileFile(FileName, numberOfRecords, 4);
        t.InsertNewRecordAtIndex(FileName, 5, 35);
        t.InsertNewRecordAtIndex(FileName, 3, 36);
        t.InsertNewRecordAtIndex(FileName, 21, 37);
        t.InsertNewRecordAtIndex(FileName, 9, 34);
        t.InsertNewRecordAtIndex(FileName, 1, 43);
        t.InsertNewRecordAtIndex(FileName, 13, 68);
        t.InsertNewRecordAtIndex(FileName, 2, 200);
        t.InsertNewRecordAtIndex(FileName, 7, 100);
        t.InsertNewRecordAtIndex(FileName, 10, 922);

        t.DeleteRecordFromIndex(FileName,10);
        t.DeleteRecordFromIndex(FileName,10);
        t.DeleteRecordFromIndex(FileName,21);



        t.DisplayIndexFileContent(FileName);
        int Reference= t.SearchARecord(FileName,10);
        if (Reference==-1)
            System.out.println("sorry not found the key you search in index ");
        else
            System.out.println("the Reference of key you search is: "+Reference);

    }
}
