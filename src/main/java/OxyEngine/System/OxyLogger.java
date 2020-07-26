package OxyEngine.System;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;

public class OxyLogger extends Formatter {

    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_RED = "\u001B[31m";

    private static final List<String> history = new ArrayList<>();

    @Override
    public String format(LogRecord record) {
        String date = getDateFromMillis();
        if(record.getLevel() == Level.INFO) {
            String s = ANSI_YELLOW + date + " PM" + " [" + record.getLevel() + "] Message: " + record.getMessage() + ANSI_RESET + "\n";
            history.add(s);
            return s;
        }
        else if(record.getLevel() == Level.SEVERE) {
            String s = ANSI_RED + date + " PM" + " [" + record.getLevel() + "] Message: " + record.getMessage() + ANSI_RESET + "\n";
            history.add(s);
            return s;
        }
        return "";
    }

    private String getDateFromMillis(){
        LocalDate date = LocalDate.now();
        LocalTime time = LocalTime.now();
        return date.toString() + " " + time;
    }

    public static List<String> getHistory() {
        return history;
    }
}
