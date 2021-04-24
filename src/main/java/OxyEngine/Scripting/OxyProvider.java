package OxyEngine.Scripting;

public sealed interface OxyProvider permits OxyScript.EntityInfoProvider {

    void invokeCreate();

    void invokeUpdate(float ts);
}
