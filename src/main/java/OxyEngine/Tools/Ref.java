package OxyEngine.Tools;

//java is always pass by value. This class just simulates it.
public final class Ref<T> {

    public final T obj;

    public Ref(T obj){
        this.obj = obj;
    }
}
