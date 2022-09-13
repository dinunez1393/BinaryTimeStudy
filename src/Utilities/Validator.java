package Utilities;

import java.time.LocalDateTime;

public abstract class Validator {

    /**
     * Converts the date-time format from Excel to Java LocalDateTime type
     * @param ExcelDateTimeFormat the Excel Date-Time format
     * @return a LocalDateTime type
     */
    public static LocalDateTime fromSQLDateTimeToLocalDateTime(String ExcelDateTimeFormat) {
        //Separate date and time
        String[] dateAndTime = ExcelDateTimeFormat.split(" ");

        //Strip any leading or trailing spaces in date and time
        String date = dateAndTime[0].strip();
        String time = dateAndTime[1].strip();

        //Separate date and time in two different arrays
        String[] fullDate = date.split("/");
        String[] fullTime = time.split(":");

        //Date and time constituents:
        int year = Integer.parseInt(fullDate[2]);
        int month = Integer.parseInt(fullDate[0]);
        int day = Integer.parseInt(fullDate[1]);
        int hour = Integer.parseInt(fullTime[0]);
        int minutes = Integer.parseInt(fullTime[1]);

        //Construct LocalDateTime
        var localDateTime = LocalDateTime.of(year, month, day, hour, minutes);
        return localDateTime;
    }

    /**
     * Converts a LocalDateTime type variable to a String formatted for Excel date-time
     * @param localDateTime a LocalDateTime variable
     * @return a String suitable for Excel date-time format
     */
    public static String fromLocalDateTimeToExcelFormat(LocalDateTime localDateTime) {
        return (localDateTime.toLocalDate().toString() + " " + localDateTime.toLocalTime().toString());
    }
}
