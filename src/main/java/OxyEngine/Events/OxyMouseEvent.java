package OxyEngine.Events;

//record class holder, that holds the button id
public record OxyMouseEvent() {
    static int buttonId = -1; //not initialized

    public int getButton() {
        return buttonId;
    }
}
