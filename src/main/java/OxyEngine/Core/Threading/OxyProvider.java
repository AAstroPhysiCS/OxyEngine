package OxyEngine.Core.Threading;

public interface OxyProvider {

    void invokeCreate();

    void invokeUpdate(float ts);
}
