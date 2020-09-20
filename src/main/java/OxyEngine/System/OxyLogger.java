package OxyEngine.System;

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

    @Override
    public String format(LogRecord record) {
        String date = getDateFromMillis();
        if(record.getLevel() == Level.INFO) {
            return ANSI_YELLOW + date + " PM" + " [" + record.getLevel() + "] Message: " + record.getMessage() + ANSI_RESET + "\n";
        }
        else if(record.getLevel() == Level.SEVERE) {
            return ANSI_RED + date + " PM" + " [" + record.getLevel() + "] Message: " + record.getMessage() + ANSI_RESET + "\n";
        }
        else if(record.getLevel() == Level.WARNING){
            return ANSI_BLUE + date + " PM" + " [" + record.getLevel() + "] Message: " + record.getMessage() + ANSI_RESET + "\n";
        }
        return "";
    }

    private String getDateFromMillis(){
        LocalDate date = LocalDate.now();
        LocalTime time = LocalTime.now();
        return date.toString() + " " + time;
    }
}
