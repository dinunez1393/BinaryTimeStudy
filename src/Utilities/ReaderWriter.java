package Utilities;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvValidationException;
import model.CleanedScan;
import model.RawScan;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.logging.Logger;

/**
 * This abstract class has methods for reading CSV files, cleaning data from the read CSV file, and writing back to a CSV file with cleaned data
 */
public abstract class ReaderWriter {

    /**
     * This methods reads raw data from a CSV file
     * @param filePath the file path of the CSV file to be read
     * @param constants various constants to use for initializing other variables in the method
     */
    public static ArrayList csvReader(String filePath, int... constants) throws IOException, CsvValidationException {
        final int MAX_LIST_CAPACITY = constants[0];
        final int MAX_ROW_CAPACITY = constants[1];
        var fileReader = new FileReader(filePath);
        var csvReader = new CSVReader(fileReader);
        var nextRecord = new String[MAX_ROW_CAPACITY];
        var rawData = new ArrayList<RawScan>(MAX_LIST_CAPACITY);
        String serialNumber, checkPointName, SKU, customerPN, location;
        int checkPointId;
        LocalDateTime transactionDate;

        //Ignore the title header from raw file
        csvReader.readNext();

        //Read all the records and save it on an ArrayList
        while ((nextRecord = csvReader.readNext()) != null) {
            serialNumber = nextRecord[0];
            checkPointId = Integer.parseInt(nextRecord[1]);
            checkPointName = nextRecord[2];
            transactionDate = Validator.fromSQLDateTimeToLocalDateTime(nextRecord[3]);
            SKU = nextRecord[4];
            customerPN = nextRecord[5];
            location = nextRecord[6];

            rawData.add(new RawScan(serialNumber, checkPointId, checkPointName, transactionDate, SKU, customerPN, location));
        }
        csvReader.close();
        Logger.getGlobal().info("The data from CSV file \"" + filePath + "\" read successfully\n");
        return rawData;
    }

    /**
     * This method writes cleaned data to a CSV file
     * @param filePath the path of the new CSV file
     * @param headerNames the names of each of the columns of the header row
     * @param cleanedData the ArrayList containing the cleaned data
     * @param maxRowCapacity the maximum columns in one row
     */
    public static void csvWriter(String filePath, String[] headerNames, ArrayList<CleanedScan> cleanedData, int maxRowCapacity) throws IOException {
        var file = new File(filePath);
        var fileWriter = new FileWriter(file);
        var csvWriter = new CSVWriter(fileWriter);
        var nextRecord = new String[maxRowCapacity];

        //Write all the cleaned data from the ArrayList into a CSV
        csvWriter.writeNext(headerNames); //header row

        for (CleanedScan cleanedField : cleanedData) {
            nextRecord[0] = cleanedField.getSerialNumber();
            nextRecord[1] = Validator.fromLocalDateTimeToExcelFormat(cleanedField.getFirstScanTransaction());
            nextRecord[2] = Validator.fromLocalDateTimeToExcelFormat(cleanedField.getSecondScanTransaction());
            nextRecord[3] = Long.toString(cleanedField.getTimeDifferenceMinutes());
            nextRecord[4] = cleanedField.getSKU();
            nextRecord[5] = cleanedField.getCustomerPN();
            nextRecord[6] = cleanedField.getLocation();

            csvWriter.writeNext(nextRecord);
        }
        csvWriter.close();
        Logger.getGlobal().info("The data is cleaned and saved successfully on CSV file named \"" + filePath + "\" in the main folder\n");
    }

    /**
     * This method cleans raw data from a read CSV file
     * @param rawData the ArrayList containing raw data
     * @param constants various constants to use for initializing other variables in the method
     * @return a new ArrayList with cleaned data
     */
    public static ArrayList dataCleaner(ArrayList<RawScan> rawData, int... constants) {
        final int FIRST_CHECKPOINT_ID = constants[0];
        final int SECOND_CHECKPOINT_ID = constants[1];
        var cleanedData = new ArrayList<CleanedScan>();
        String serialNumber, SKU, customerPN, location;
        long minutesDifference;
        LocalDateTime firstScanTransaction, secondScanTransaction;

        //Clean all the raw data into a new list. The list from Excel is sorted by Transaction date first in DESC order and then by Serial Number in ASC order
        for (int i = 0; i < rawData.size(); i++) {
            if (rawData.get(i).getCheckPointId() == SECOND_CHECKPOINT_ID) {
                for (int j = i + 1; j < rawData.size(); j++) {
                    if ((rawData.get(j).getCheckPointId() == FIRST_CHECKPOINT_ID) &&
                            (rawData.get(j).getSerialNumber().equals(rawData.get(i).getSerialNumber()))) {
                        serialNumber = rawData.get(i).getSerialNumber();
                        firstScanTransaction = rawData.get(j).getTransactionDate();
                        secondScanTransaction = rawData.get(i).getTransactionDate();
                        minutesDifference = ChronoUnit.MINUTES.between(rawData.get(j).getTransactionDate(), rawData.get(i).getTransactionDate());
                        SKU = rawData.get(i).getSKU();
                        customerPN = rawData.get(i).getCustomerPN();
                        location = rawData.get(i).getLocation();

                        cleanedData.add(new CleanedScan(serialNumber, firstScanTransaction, secondScanTransaction, minutesDifference, SKU, customerPN, location));
                        i = j; //Jump into the next occurrence of rack scan 1 & rack scan 2
                        break;
                    }
                }
            }
        }
        return cleanedData;
    }
}

//PROTO CODE

//    final int MAX_LIST_CAPACITY = 13008;
//    final int MAX_ROW_CAPACITY = 7;
//    final int FIRST_RACK_SCAN_ID = 202;
//    final int SECOND_RACK_SCAN_ID = 237;
//    String inFile = "MSF_RackScan1-2_avg_time_for_Feb-Aug_2022.csv";
//    String outFile = "MSF_Cleaned_RackScan_Data_for_Feb-Aug_2022.csv";
//    var file = new File(outFile); //for file writer use
//    var fileReader = new FileReader(inFile);
//    var fileWriter = new FileWriter(file);
//    var csvReader = new CSVReader(fileReader);
//    var csvWriter = new CSVWriter(fileWriter);
//    var nextRecord = new String[MAX_ROW_CAPACITY];
//    var rawScans = new ArrayList<RawScan>(MAX_LIST_CAPACITY);
//    var cleanedScans = new ArrayList<CleanedScan>();
//    String serialNumber, checkPointName, SKU, customerPN, location;
//    String outFileHeader[] = {"SerialNumber", "FirstScanTransaction", "SecondScanTransaction", "TimeDifferenceMinutes", "SKU", "CustomerPN", "Location"};
//    int checkPointId;
//    long minutesDifference;
//    LocalDateTime transactionDate, firstScanTransaction, secondScanTransaction;
//
////Read all the records and save it on an ArrayList
//        for (int i = 0; i < MAX_LIST_CAPACITY; i++) {
//        if ((nextRecord = csvReader.readNext()) != null) {
//        serialNumber = nextRecord[0];
//        checkPointId = Integer.parseInt(nextRecord[1]);
//        checkPointName = nextRecord[2];
//        transactionDate = Validator.fromSQLDateTimeToLocalDateTime(nextRecord[3]);
//        SKU = nextRecord[4];
//        customerPN = nextRecord[5];
//        location = nextRecord[6];
//
//        rawScans.add(new RawScan(serialNumber, checkPointId, checkPointName, transactionDate, SKU, customerPN, location));
//        }
//        }
//        csvReader.close();
//        Logger.getGlobal().info("The data from CSV file \"" + inFile + "\" read successfully");

//        for (RawScan field : rawScans) {
//            System.out.println(field.getSerialNumber() + "\t" + field.getCheckPointId() + "\t" + field.getCheckPointName() + "\t" +
//                    field.getTransactionDate() + "\t" + field.getSKU() + "\t" + field.getCustomerPN() + "\t" + field.getLocation());
//            System.out.println();
//        }
