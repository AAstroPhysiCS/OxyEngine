package OxyEngine.System;

import OxyEngine.Core.Renderer.Texture.OxyColor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;

public class OxyLogger extends Formatter {

    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_YELLOW = "\u001B[33m";

    public static final OxyColor ANSI_BLUE_OXY = new OxyColor(0.1f, 0.1f, 1.0f, 1.0f);
    public static final OxyColor ANSI_RED_OXY = new OxyColor(1f, 0.1f, 0.1f, 1.0f);
    public static final OxyColor ANSI_YELLOW_OXY = new OxyColor(1.0f, 1.0f, 0.1f, 1.0f);

    //apparently, we cant take console output with System.in... System.in is null
    //for the history, we need to define our own buffer and limit its size
    private static final int bufferCapacity = 10000;
    private static final StringBuilder bufferHistory = new StringBuilder(bufferCapacity);

    @Override
    public String format(LogRecord record) {
        String date = getDateFromMillis();
        String s = "";
        if(record.getLevel() == Level.INFO)
            s = ANSI_YELLOW + initialFormat(date, record);
        else if(record.getLevel() == Level.SEVERE)
            s =  ANSI_RED + initialFormat(date, record);
        else if(record.getLevel() == Level.WARNING)
            s =  ANSI_BLUE + initialFormat(date, record);
        if(!s.isEmpty()) {
            if(bufferHistory.length() > bufferCapacity) bufferHistory.delete(0, 100);
            bufferHistory.append(s);
        }
        return s;
    }

    private String initialFormat(String date, LogRecord record){
        return date + " PM" + " [" + record.getLevel() + "] Message: " + record.getMessage() + ANSI_RESET + "\n";
    }

    private String getDateFromMillis(){
        LocalDate date = LocalDate.now();
        LocalTime time = LocalTime.now();
        return date.toString() + " " + time;
    }

    public static StringBuilder getHistory() {
        return bufferHistory;
    }
}
