package OxyEngine.Scripting;

public sealed interface Provider permits EntityInfoProvider {

    void invokeCreate();

    void invokeUpdate(float ts);
}
