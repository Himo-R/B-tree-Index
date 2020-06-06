import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class B_Tree {
    public int  M;
    protected int rowSize;
    protected int nodeSize;
    void culcRowSize(){ rowSize=M*2+1;}
    void culcnodeSize(){ nodeSize=M*2;}

    public void CreateIndexFileFile (String filename, int numberOfRecords, int m) throws IOException
    {
        RandomAccessFile fileStore = new RandomAccessFile(filename, "rw");
        for (int i = 0; i < ( (((m * 2) + 1) * (numberOfRecords+1) ) ); i++) // ( row * columns )
        {
            fileStore.writeInt(-1);
        }
        fileStore.close();

        culcRowSize();
        culcnodeSize();
    }

    public void InsertNewRecordAtIndex (String filename, int RecordID, int Reference) throws IOException {
        //"flag : is the integer before the node"show us state of record
        //0 --> child , 1 --> root , -1 --> no record yet


        // read the first flag (nodeFlag)
        RandomAccessFile fileStore = new RandomAccessFile(filename, "rw");
        fileStore.seek(0);
        fileStore.seek(rowSize*4);
        int nodeFlag=fileStore.readInt();

        if (nodeFlag==0||nodeFlag==-1)
        //1- file is empty or there is no child ,the flag == -1  or the flag == 0 ,
        //1.1 read the first node and push to a list
        //1.2 filter the list from -1
        //1.3 add new record to list
        //1.4 sort list
        //1.5 if list less or equal M ,write node after the flag. update flag =0 , update flag_of_empty_node=2
        //1.6 if list more M , Split to two lists and create the root list , write root list after flag and update flag =1
        {
            //1.1 read the first node and push to a list
            fileStore.seek(0);
            fileStore.seek((rowSize*4)+4);
            List<Record> listRecord = new ArrayList<>();

            for (int i=0;i<M;i++)
            {
                Record bufferObject=new Record();
                bufferObject.key=fileStore.readInt();
                bufferObject.reference=fileStore.readInt();
                listRecord.add(bufferObject);
            }
            //1.2 filter the list from -1, remove -1 from list
            filterList(listRecord);
            //1.3 add new record to list
            Record newRecord=new Record();
            newRecord.setKey(RecordID); newRecord.setReference(Reference);
            listRecord.add(newRecord);
            //1.4 sort list
            sort(listRecord);
            //1.5 if list less or equal M ,
                // 1.5.1 -update flag =0
                // 1.5.2 -write the list after the flag
                // 1.5.3 -update flag_next_empty_node=2
            if (listRecord.size()<=M)
            {
                //1.5.1 -update flag =0  (update on node flag   0 --> not root)
                fileStore.seek(0);
                fileStore.seek(rowSize*4);
                fileStore.writeInt(0);

                //1.5.2 -write the list after the flag
                fileStore.seek((rowSize*4)+4);
                for(int i=0;i<listRecord.size();i++)
                {
                    fileStore.writeInt(listRecord.get(i).getKey());
                    fileStore.writeInt(listRecord.get(i).getReference());
                }
                //1.5.3 -update flag_next_empty_node=2
                fileStore.seek(4);
                fileStore.writeInt(2);
            }
            //1.6 if list more M ,
                //1.6.1 read flag_next_empty_node
                //1.6.2 Split
                //1.6.3 update on node flag
                //1.6.4 clean old data root
                //1.6.5 write root
                //1.6.6 write child 1 and update node flag
                //1.6.7 write child 2 amd update node flag
                //1.6.8 update flag_of_next_empty_node
            else if (listRecord.size()>M)
            {
                //1.6.1 read flag_next_empty_node
                fileStore.seek(4);
                int flag_of_next_empty_node= fileStore.readInt();

                //1.6.2 Split       (vector have 3 list : 1st is root , 2nd is child 1 , 3rd is child 3)
                Vector <List<Record>> root_child1_child2=new Vector<>();
                root_child1_child2.addAll(0,Split(listRecord,flag_of_next_empty_node,flag_of_next_empty_node+1));

                // 1.6.3 update on node flag. 1--> it is parent
                fileStore.seek(rowSize*4);
                fileStore.writeInt(1);

                //1.6.4 clean old data root "write -1 in all root"
                for (int i=0 ;i< nodeSize;i++)
                    fileStore.writeInt(-1);

                //1.6.5 write the root
                fileStore.seek((rowSize*4)+4);
                for (int i=0 ;i<root_child1_child2.get(0).size();i++)
                {
                    fileStore.writeInt(root_child1_child2.get(0).get(i).getKey());
                    fileStore.writeInt(root_child1_child2.get(0).get(i).getReference());
                }

                //1.6.6 write child 1 and update node flag . "child 1 is in second element on vector"
                    //write node flag . 0--> it's child
                fileStore.seek(rowSize*4*flag_of_next_empty_node);
                fileStore.writeInt(0);
                    //write the child 1
                for (int i=0 ;i<root_child1_child2.get(1).size();i++)
                {
                    fileStore.writeInt(root_child1_child2.get(1).get(i).getKey());
                    fileStore.writeInt(root_child1_child2.get(1).get(i).getReference());
                }

                //1.6.7 write child 1 and update node flag . "child 2 is in third element on vector"
                    //write node flag . 0--> it's child
                fileStore.seek(rowSize*4*(flag_of_next_empty_node+1));
                fileStore.writeInt(0);
                    //write child 2
                for (int i=0 ;i<root_child1_child2.get(2).size();i++)
                {
                    fileStore.writeInt(root_child1_child2.get(2).get(i).getKey());
                    fileStore.writeInt(root_child1_child2.get(2).get(i).getReference());
                }

                //1.6.8 update flag_of_next_empty_node
                fileStore.seek(4);
                fileStore.writeInt(flag_of_next_empty_node+2);
            }


        }

        // 2- there are children

        else if(nodeFlag==1)
        //2- there are children , the flag == 1
            //2.1 read Root push to rootlist
            //2.2 filter list from -1
            //2.3 get the correct Reference child from root list
            //2.4 seek to reference and read the child push to list
            //2.5 filter list from -1
            //2.6 add the new record
            //2.7 sort list
            //2.8 size of child list is less or equal M  "no split"
            //2.9 child list more than M                 "need to split child"


        {
            fileStore.seek(0);
            fileStore.seek((rowSize*4)+4);
            List<Record> rootList = new ArrayList<>();
            //2.1 read Root push to rootlist
            for (int i=0;i<M;i++)
            {
                Record bufferObject=new Record();
                bufferObject.key=fileStore.readInt();
                bufferObject.reference=fileStore.readInt();
                rootList.add(bufferObject);
            }
            //2.2filter list from -1
            filterList(rootList);
            //2.3 searchReferanceChild --->  get the correct child Reference from root list
            int referenceOfChildNode = searchReferanceChild(rootList,RecordID);
                //the new key is large than larger key in index
            if (referenceOfChildNode==-1) {
                //replace last key of root with the new key
                rootList.get(rootList.size()-1).setKey(RecordID);
                //set referenceOfChildNode = last element reference in root list
                referenceOfChildNode = rootList.get(rootList.size() - 1).getReference();
            }
            //2.4 seek to reference and read the child , push to list.
            fileStore.seek((referenceOfChildNode*rowSize*4)+4);
            List<Record> childList = new ArrayList<>();
            for (int i=0;i<M;i++)
            {
                Record bufferObject=new Record();
                bufferObject.key=fileStore.readInt();
                bufferObject.reference=fileStore.readInt();
                childList.add(bufferObject);
            }
            //2.5filter list from -1
            filterList(childList);
            //2.6 add the new record to child list
            Record newRecord=new Record();
            newRecord.setKey(RecordID); newRecord.setReference(Reference);
            childList.add(newRecord);
            //2.7 sort child
            sort(childList);

/*- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -*/

            //2.8 if child list less or equal M . there is enough space in child , no need to split
            //2.8.1 Rewrite root
            //2.8.2write to child
            if (childList.size() <= M)
            {
                //2.8.1 Rewrite root
                fileStore.seek((rowSize*4)+4);
                for (int i=0;i<rootList.size();i++)
                {
                    fileStore.writeInt(rootList.get(i).getKey());
                    fileStore.writeInt(rootList.get(i).getReference());
                }
                //2.8.2write to child
                fileStore.seek((referenceOfChildNode*rowSize*4)+4);
                for (int i=0;i<childList.size();i++)
                {
                    fileStore.writeInt(childList.get(i).getKey());
                    fileStore.writeInt(childList.get(i).getReference());
                }
            }
            //2.9 child list more than M need to split child
                //2.9.1 read flag_next_empty_node
                //2.9.2 Split
                //2.9.3 updateRoot
                //2.9.4 rewrite root
                //2.9.5 write first child
                //2.9.6 write second child
                //2.9.7 update flag_of_next_empty_node
            else if (childList.size() > M)
            {
                //2.9.1 read flag_next_empty_node
                fileStore.seek(4);
                int flag_of_next_empty_node= fileStore.readInt();

                //2.9.2 Split         (vector have 3 list : 1st is root , 2nd is child 1 , 3rd is child 3)
                Vector <List<Record>> root_child1_child2=new Vector<>();
                root_child1_child2.addAll(0,Split(childList,referenceOfChildNode,flag_of_next_empty_node));
                //2.9.3 updateRoot   ---> add new root (this I get after split) to main root
                updateRootAfterSplit(rootList,root_child1_child2.get(0));
                //2.9.4 rewrite root
                fileStore.seek((rowSize*4)+4);
                for (int i=0;i<rootList.size();i++)
                {
                    fileStore.writeInt(rootList.get(i).getKey());
                    fileStore.writeInt(rootList.get(i).getReference());
                }
                //2.9.5 write first child
                fileStore.seek((referenceOfChildNode*rowSize*4)+4);
                    //clean node for old data
                for (int i=0 ;i< nodeSize;i++)
                    fileStore.writeInt(-1);

                fileStore.seek((referenceOfChildNode*rowSize*4)+4);
                for (int i=0;i<root_child1_child2.get(1).size();i++)
                {
                    fileStore.writeInt(root_child1_child2.get(1).get(i).getKey());
                    fileStore.writeInt(root_child1_child2.get(1).get(i).getReference());
                }
                //2.9.6 write second child
                fileStore.seek(flag_of_next_empty_node*rowSize*4);
                fileStore.writeInt(0);
                for (int i=0;i<root_child1_child2.get(2).size();i++)
                {
                    fileStore.writeInt(root_child1_child2.get(2).get(i).getKey());
                    fileStore.writeInt(root_child1_child2.get(2).get(i).getReference());
                }
                //2.9.7 update flag_of_next_empty_node
                fileStore.seek(4);
                fileStore.writeInt((flag_of_next_empty_node+1));
            }
        }
    }
    public void DeleteRecordFromIndex (String filename, int RecordID) throws IOException
    {
        //1. read root and push to list
        //2. filter root from -1
        //3. get the correct Reference child from root list
        //4. seek on reference and read child , push to list
        //5. filter child from -1
        //6. check if the Record ID is found in child list
        //7. delete record form child list
        //8. no merge need --> the child after delete no need to merge
        //9. merge need , --> the child after delete  need to merge

        RandomAccessFile fileStore = new RandomAccessFile(filename, "rw");
        //1. read root and push to list
        fileStore.seek(0);
        fileStore.seek((rowSize*4)+4);
        List<Record> rootList =new ArrayList<>();
        for (int i=0;i<M;i++)
        {
            Record bufferObject=new Record();
            bufferObject.key=fileStore.readInt();
            bufferObject.reference=fileStore.readInt();
            rootList.add(bufferObject);
        }
        //2 filter root from -1
        filterList(rootList);
        //3 get the correct Reference child from root list
        int ReferanceChild=searchReferanceChild(rootList,RecordID);
            //not found in root , the key that wanted to delete is not exist
        if (ReferanceChild==-1)
            System.out.println("Sorry not found "+RecordID+" that you try to delete");
        else
        {
            //4 seek on reference and read child , push to list
            fileStore.seek((ReferanceChild*rowSize*4)+4);
            List<Record> childList=new ArrayList<>();
            for (int i=0;i<M;i++)
            {
                Record bufferObject=new Record();
                bufferObject.key=fileStore.readInt();
                bufferObject.reference=fileStore.readInt();
                childList.add(bufferObject);
            }
            //5. filter child from -1
            filterList(childList);
            //6. check if the Record ID is found in child list
            if (!checkKeyIsExist(childList,RecordID))
            System.out.println("Sorry not found "+RecordID+" that you try to delete");
            else
            {
                //7. delete record form child list
                deleteRecord(childList,RecordID);
                //8. no merge need --> the child after delete no need to merge
                if(childList.size()!=1)
                {
                    //8.1 update root after delete
                    int key_of_last_element;
                    key_of_last_element=childList.get(childList.size()-1).getKey();
                    //8.2 updateRootAfterDelete --> replace last element key in child list we deleted with suitable key in root list
                    updateRootAfterDelete(rootList,key_of_last_element);
                    //8.3 rewrite root
                    fileStore.seek((rowSize*4)+4);
                    for(int i=0;i<rootList.size();i++)
                    {
                        fileStore.writeInt(rootList.get(i).getKey());
                        fileStore.writeInt(rootList.get(i).getReference());
                    }
                    //8.4 rewrite child
                    fileStore.seek((ReferanceChild*rowSize*4)+4);
                        //clean node
                    for (int i=0;i<M*2;i++)
                        fileStore.writeInt(-1);

                    fileStore.seek((ReferanceChild*rowSize*4)+4);
                    for(int i=0;i<childList.size();i++)
                    {
                        fileStore.writeInt(childList.get(i).getKey());
                        fileStore.writeInt(childList.get(i).getReference());
                    }
                }
                //9. merge , --> the child after delete  need to merge
                else
                {
                    //9.1 read previous child
                    fileStore.seek(((ReferanceChild-1)*rowSize*4)+4);
                    List<Record> previousChildList =new ArrayList<>();
                    for (int i=0;i<M;i++)
                    {
                        Record bufferObject=new Record();
                        bufferObject.key=fileStore.readInt();
                        bufferObject.reference=fileStore.readInt();
                        previousChildList.add(bufferObject);
                    }
                    //9.2 filter previous child
                    filterList(previousChildList);
                    //9.3 Merging, add child to previous child
                    previousChildList.add(childList.get(0));
                    //9.4 sort previous child
                    sort(previousChildList);
                    //9.5 rewrite previous child
                    fileStore.seek(((ReferanceChild-1)*rowSize*4)+4);
                    for(int i=0;i<previousChildList.size();i++)
                    {
                        fileStore.writeInt(previousChildList.get(i).getKey());
                        fileStore.writeInt(previousChildList.get(i).getReference());
                    }
                    //9.6 clean old child that child was merged
                    fileStore.seek((ReferanceChild*rowSize*4));
                    for(int i=0;i<((M*2)+1);i++)
                        fileStore.writeInt(-1);
                    //9.7 update flag_of_next_empty_node
                    fileStore.seek(4);
                    int flag_of_next_empty_node=fileStore.readInt();
                    fileStore.seek(4);
                    fileStore.writeInt(flag_of_next_empty_node-1);
                    //9.8 update main root after delete
                    int key_of_last_element_in_previousChildList =previousChildList.get(previousChildList.size()-1).getKey();
                    updateRootAfterDelete(rootList,key_of_last_element_in_previousChildList);
                        //search last key of pre child on root
                    int index=0;
                    for (int i=0;i<rootList.size();i++)
                        if(rootList.get(i).getKey()== key_of_last_element_in_previousChildList)
                            index=i;
                    rootList.get(index-1).setKey(key_of_last_element_in_previousChildList);
                    rootList.remove(index);
                    //9.9 rewrite root
                        //cleaning from old data
                    fileStore.seek((rowSize*4)+4);
                    for (int i=0;i<M*2;i++)
                        fileStore.writeInt(-1);
                    fileStore.seek((rowSize*4)+4);
                    for(int i=0;i<rootList.size();i++)
                    {
                        fileStore.writeInt(rootList.get(i).getKey());
                        fileStore.writeInt(rootList.get(i).getReference());
                    }
                }
            }
        }
    }
    public int SearchARecord (String filename, int RecordID) throws IOException {
        //1. read root push to root list
        //2. read flag root "state of root"
        //3. if  flag root 0 --> no child exist
            //3.1 search key in root list
            //3.2 return reference
        //4. if  flag root --> 1
            //4.1 search to get correct reference
            //4.2 seek on reference
            //4.3 read child and push to list
            //4.4 search key in child list
            //4.5 return reference
        RandomAccessFile fileStore = new RandomAccessFile(filename,"rw");
        //1. read root and push to list
        fileStore.seek(0);
        fileStore.seek((rowSize*4)+4);
        List<Record> rootList =new ArrayList<>();
        for (int i=0;i<M;i++)
        {
            Record bufferObject=new Record();
            bufferObject.key=fileStore.readInt();
            bufferObject.reference=fileStore.readInt();
            rootList.add(bufferObject);
        }
            //filter root from -1
        filterList(rootList);
        //2. read flag root "state of root"
        fileStore.seek(0);
        fileStore.seek(rowSize*4);
        int stateRoot =fileStore.readInt();
        //3. if  flag root 0 --> no child exist

        if (stateRoot==0)
        {
            //3.1 search key in root list,         //3.2 return reference
            for(int i=0;i<rootList.size();i++)
                if (rootList.get(i).getKey()==RecordID)
                return rootList.get(i).getReference();
                else return -1;
        }
        //4. if  flag root --> 1
        else if(stateRoot==1)
        {
            int ReferenceChild=searchReferanceChild(rootList,RecordID);
            //4.1 search to get correct reference
            if (ReferenceChild==-1)
                return -1;
            //4.2 seek on reference
            fileStore.seek((ReferenceChild*rowSize*4)+4);
            //4.3 read child and push to list
            List<Record> childList=new ArrayList<>();
            for (int i=0;i<M;i++)
            {
                Record bufferObject=new Record();
                bufferObject.key=fileStore.readInt();
                bufferObject.reference=fileStore.readInt();
                childList.add(bufferObject);
            }
            // filter child from -1
            filterList(childList);
            //4.4 search key in child list       //4.5 return reference
            for(int i=0;i<childList.size();i++)
                if (childList.get(i).getKey()==RecordID)
                    return childList.get(i).getReference();

            return -1;

        }
        return -1;
    }

    public void DisplayIndexFileContent  (String filename) throws IOException {
        RandomAccessFile fileStore = new RandomAccessFile(filename,"rw");
        fileStore.seek(0);
        for (int i=1;i<=fileStore.length()/4;i++)
        {
            System.out.print(fileStore.readInt()+"  ");
            if (i % ((M*2)+1) == 0)
                System.out.println();
        }
    }


    public static boolean checkKeyIsExist(List<Record> listRecord , int key )
    {
        for(int i=0;i<listRecord.size();i++)
        if(listRecord.get(i).getKey()==key)
            return true;

            return false;
    }


    public static List<Record> updateRootAfterDelete (List<Record> rootList , int key )
    {
        for(int i=0;i<rootList.size();i++)
        {
            if(rootList.get(i).getKey()>=key)
            {
                rootList.get(i).setKey(key);
                break;
            }

        }
        return  rootList;
    }
    public static List<Record> deleteRecord(List<Record> listRecord,int key)
    {
        int N=listRecord.size();
        int indexForDelete=0;
        for(int i=0 ; i<N ;i++)
        {
            if (listRecord.get(i).getKey()==key)
                 indexForDelete = i;
        }
        listRecord.remove(indexForDelete);
        return null;
    }

    public static List<Record> filterList(List<Record> listRecord)
    {

        int j =0;
        int N=listRecord.size();
        for (int i=0;i<N;i++)
            if(listRecord.get(j).getKey()==-1)
            listRecord.remove(j);
            else
                j++;

        return listRecord;
    }

    public static List<Record> updateRootAfterSplit (List<Record> mainRoot, List<Record> newRoot)
    {
        for(int i=0;i<mainRoot.size();i++)
        {
            if (mainRoot.get(i).getKey()== newRoot.get(newRoot.size()-1).getKey())
                mainRoot.set(i,newRoot.get(newRoot.size()-1));
        }
        mainRoot.add(newRoot.get(0));
        sort(mainRoot);
        return mainRoot;
    }

    public static int searchReferanceChild (List<Record> listRecord,int newKey)
    {
        for (int i=0;i<listRecord.size();i++)
        {
            if (newKey <= listRecord.get(i).getKey())
                return listRecord.get(i).getReference();
        }

        return -1;
    }

    public static List<Record> sort(List<Record> listRecord)
    {
        for(int i = 0; i < listRecord.size()-1; i++)
            for (int j = 0; j < listRecord.size()-i-1; j++)
                if (listRecord.get(j).getKey()>listRecord.get(j+1).getKey())
                {
                    Record temp =new Record();
                    temp = listRecord.get(j);
                    listRecord.set(j,listRecord.get(j+1));
                    listRecord.set(j+1,temp);
                }
        return listRecord;
    }


    public static Vector<List<Record>> Split (List<Record> listRecord, int firstReference , int secondReference)
    {
        List<Record>rootList=new ArrayList<>();
        List<Record>firstChild=new ArrayList<>();
        int n =(listRecord.size()+1)/2;
        for (int i=0;i<n;i++)
        {
            firstChild.add(listRecord.get(0));
            listRecord.remove(0);
        }
        int b =firstChild.size();
        int c =listRecord.size();
        Record lastElementAtFirstChild=new Record();
        Record lastElementAtListRecord=new Record();
        lastElementAtFirstChild.setKey(firstChild.get(b-1).getKey());
        lastElementAtFirstChild.setReference(firstChild.get(b-1).getReference());

        lastElementAtListRecord.setKey(listRecord.get(c-1).getKey());
        lastElementAtListRecord.setReference(listRecord.get(c-1).getReference());

        rootList.add(lastElementAtFirstChild);
        rootList.add(lastElementAtListRecord);
        rootList.get(0).setReference(firstReference);
        rootList.get(1).setReference(secondReference);
        Vector<List<Record>> threeLists=new Vector<>();
        threeLists.add(rootList);
        threeLists.add(firstChild);
        threeLists.add(listRecord);
        return threeLists;
    }

}

