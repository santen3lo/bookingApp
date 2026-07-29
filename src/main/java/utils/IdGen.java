package utils;

public class IdGen {
    private long id;
    public IdGen (){
        id = 0;
    }
    public long createId(){
        id ++;
        return id;
    }
    public long getId(){
        return id;
    }

    public void setId(long newId) {
        if (newId < id){
            System.err.println("Нельзя сделать такой ID");
        } else {
            id = newId;
        }
    }

}
