import java.io.IOException;
import java.io.RandomAccessFile;

public class Main {
    public static void main(String[] args) throws IOException {
        System.out.println("himo");
        B_Tree t = new B_Tree();
        t.M=4;

        int numberOfRecords = 5;
        t.CreateIndexFileFile("B-tree Index.bin", 5, 4);
        t.InsertNewRecordAtIndex("B-tree Index.bin", 5, 35);
        t.InsertNewRecordAtIndex("B-tree Index.bin", 3, 36);
        t.InsertNewRecordAtIndex("B-tree Index.bin", 21, 37);
        t.InsertNewRecordAtIndex("B-tree Index.bin", 9, 34);
        t.InsertNewRecordAtIndex("B-tree Index.bin", 1, 43);
        t.InsertNewRecordAtIndex("B-tree Index.bin", 13, 68);
        t.InsertNewRecordAtIndex("B-tree Index.bin", 2, 200);
        t.InsertNewRecordAtIndex("B-tree Index.bin", 7, 100);
        t.InsertNewRecordAtIndex("B-tree Index.bin", 10, 922);

        t.DeleteRecordFromIndex("B-tree Index.bin",10);
        t.DeleteRecordFromIndex("B-tree Index.bin",10);
        t.DeleteRecordFromIndex("B-tree Index.bin",21);



        t.DisplayIndexFileContent("B-tree Index.bin");
        int Reference= t.SearchARecord("B-tree Index.bin",10);
        if (Reference==-1)
            System.out.println("sorry not found the key you search in index ");
        else
            System.out.println("the Reference of key you search is: "+Reference);

    }
}
