public class Record {
    int key;
    int reference;

    public Record() { }

    public Record(int key, int reference) {
        this.key = key;
        this.reference = reference;
    }

    public void setKey(int key) { this.key = key; }
    public int getKey() { return key; }

    public void setReference(int reference) { this.reference = reference; }
    public int getReference() { return reference; }
}
