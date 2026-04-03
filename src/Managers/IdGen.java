package Managers;

public class IdGen {
    private long id;
    public IdGen (){
        id = 1;
    }
    public long createId(){
        id ++;
        return id;
    }
    public long getId(){
        return id;
    }
}
