package OxyEngine.Core.Renderer;

import OxyEngine.Core.Renderer.Texture.Color;
import OxyEngine.Core.Window.Window;
import OxyEngine.TargetPlatform;

import java.nio.ByteBuffer;

class RenderCommand {

    private static RendererContext rendererContext;
    private static RendererAPI rendererAPI;

    static TargetPlatform targetPlatform;

    public static void init(TargetPlatform targetPlatform, boolean debug) {
        RenderCommand.targetPlatform = targetPlatform;
        rendererContext = RendererContext.getContext(targetPlatform);
        rendererAPI = RendererAPI.getContext(targetPlatform);
        rendererContext.init();
        rendererAPI.init(debug);
    }

    public static void drawArrays(int modeID, int first, int count){
        Renderer.Stats.drawCalls++;
        rendererAPI.drawArrays(modeID, first, count);
    }

    public static void drawElements(int modeID, int size, int type, int baseIndex) {
        if (RendererAPI.onStackRenderPass == null) throw new IllegalStateException("RenderPass not bound!");
        Renderer.Stats.drawCalls++;
        rendererAPI.drawElements(modeID, size, type, baseIndex);
    }

    public static void drawElementsIndexed(int modeID, int size, int type, int baseIndex, int baseVertex) {
        if (RendererAPI.onStackRenderPass == null) throw new IllegalStateException("RenderPass not bound!");
        Renderer.Stats.drawCalls++;
        rendererAPI.drawElementsIndexed(modeID, size, type, baseIndex, baseVertex);
    }

    public static void clearColor(Color clearColor) {
        rendererAPI.clearColor(clearColor);
    }

    public static void clearBuffer() {
        rendererAPI.clearBuffer();
    }

    public static void enableDepth(){
        rendererContext.enableDepth();
    }

    public static void disableDepth(){
        rendererContext.disableDepth();
    }

    static void enable(int id){
        rendererContext.enable(id);
    }

    static void disable(int id){
        rendererContext.disable(id);
    }

    public static void swapBuffer(Window handle) {
        rendererContext.swapBuffer(handle);
    }

    public static void beginRenderPass(RenderPass renderPass) {
        rendererAPI.beginRenderPass(renderPass);
    }

    public static void endRenderPass() {
        rendererAPI.endRenderPass();
    }

    public static void bindBuffer(int target, int buffer){
        rendererAPI.bindBuffer(target, buffer);
    }

    public static void readBuffer(int attachment) {
        rendererAPI.readBuffer(attachment);
    }

    public static ByteBuffer mapBuffer(int target, int access){
        return rendererAPI.mapBuffer(target, access);
    }

    public static ByteBuffer mapBufferRange(int target, int buffer, int offset, int length, int access){
        return rendererAPI.mapBufferRange(target, buffer, offset, length, access);
    }

    public static boolean unmapBuffer(int target){
        return rendererAPI.unmapBuffer(target);
    }

    public static void bindImageTexture(int unit,
                                        int texture,
                                        int level,
                                        boolean layered,
                                        int layer,
                                        int access,
                                        int format) {
        rendererAPI.bindImageTexture(unit, texture, level, layered, layer, access, format);
    }

    public static void dispatchCompute(int numGroupsX, int numGroupsY, int numGroupsZ){
        rendererAPI.dispatchCompute(numGroupsX, numGroupsY, numGroupsZ);
    }

    public static void bindBufferBase(int target, int index, int buffer){
        rendererAPI.bindBufferBase(target, index, buffer);
    }

    public static void memoryBarrier(int barriers){
        rendererAPI.memoryBarrier(barriers);
    }

    public static void readPixels(int x,
                                  int y,
                                  int width,
                                  int height,
                                  int format,
                                  int type,
                                  int[] pixels) {
        rendererAPI.readPixels(x, y, width, height, format, type, pixels);
    }

    public static void clearTexImage(int texture,
                                     int level,
                                     int format,
                                     int type,
                                     int[] data) {
        rendererAPI.clearTexImage(texture, level, format, type, data);
    }

    public static void bindTextureUnit(int textureSlot, int textureID){
        rendererContext.bindTextureUnit(textureSlot, textureID);
    }
}
