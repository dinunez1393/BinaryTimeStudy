package Utilities;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvValidationException;
import model.CleanedField;
import model.RawField;

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
     * The list from Excel should be sorted by Transaction date in DESC order first and then by Serial Number in DESC order too
     * @param filePath the file path of the CSV file to be read
     * @param constants various constants to use for initializing other variables in the method
     */
    public static ArrayList<RawField> csvReader(String filePath, int... constants) throws IOException, CsvValidationException {
        final int MAX_LIST_CAPACITY = constants[0];
        final int MAX_ROW_CAPACITY = constants[1];
        var fileReader = new FileReader(filePath);
        var csvReader = new CSVReader(fileReader);
        var nextRecord = new String[MAX_ROW_CAPACITY];
        var rawData = new ArrayList<RawField>(MAX_LIST_CAPACITY);
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

            rawData.add(new RawField(serialNumber, checkPointId, checkPointName, transactionDate, SKU, customerPN, location));
        }
        csvReader.close();
        Logger.getGlobal().info("The data from CSV file \"" + filePath + "\" read successfully\n");
        return rawData;
    }

    /**
     * This method iterates through an ArrayList of cleaned data ArrayLists and writes them each to their own CSV file using the csvWriter private method
     * @param filePath the file path of the outgoing CSV files. Each file will be enumerated, starting from 1
     * @param cleanedData the parent ArrayList containing ArrayLists with cleaned data
     * @param maxRowCapacity the maximum number of rows for each file
     * @param checkPointNames an ArrayLists containing all the checkpoint names for all stations. Each station has two checkpoint names; i.e. Check-in and Check-out
     */
    public static void csvWriterIterator(String filePath, ArrayList<ArrayList<CleanedField>> cleanedData, int maxRowCapacity, ArrayList<String> checkPointNames) throws IOException {
        final String CSV_EXT = ".csv";
        String tempFilePath;
        int i = 0;
        int j = 1;
        String[] outFileHeader = {"SerialNumber", "IPQC_CheckIn", "IPQC_CheckOut", "TimeDifferenceMinutes", "SKU", "CustomerPN", "Location"};

        while (i < checkPointNames.size()) { //Check that i remains below the checkPointNames array size to avoid index out of bounds
            //Iterate through the ArrayLists of cleaned data to write each on its own CSV file
            for (ArrayList<CleanedField> stationCleanedData: cleanedData) { //the order of the stations match the order and the number of the checkpoint names; otherwise the cleaning of various stations will not work
                tempFilePath = filePath + "_" + j + CSV_EXT; //enumerate each output CSV file
                //Change the checkpoint names in the header to correspond to each station
                outFileHeader[1] = checkPointNames.get(i);
                outFileHeader[2] = checkPointNames.get(i + 1);
                csvWriter(tempFilePath, stationCleanedData, maxRowCapacity, outFileHeader);
                i += 2; //increment i by 2, so it is ready for the next station's two checkpoint names
                j++; //increment j by 1 for the next outgoing file name
            }
        }
        cleanedData.clear(); //Clear the cleaned data parent array to be ready for another run of the program
    }

    /**
     * Helper method for csvWriterIterator. This method writes cleaned data to a CSV file
     * @param filePath the path of the new CSV file
     * @param headerNames the names of each of the columns of the header row
     * @param cleanedData the ArrayList containing the cleaned data
     * @param maxRowCapacity the maximum number of rows
     */
    private static void csvWriter(String filePath, ArrayList<CleanedField> cleanedData, int maxRowCapacity, String[] headerNames) throws IOException {
        var file = new File(filePath);
        var fileWriter = new FileWriter(file);
        var csvWriter = new CSVWriter(fileWriter);
        var nextRecord = new String[maxRowCapacity];

        //Write all the cleaned data from the ArrayList into a CSV
        csvWriter.writeNext(headerNames); //header row

        for (CleanedField cleanedField : cleanedData) {
            nextRecord[0] = cleanedField.getSerialNumber();
            nextRecord[1] = Validator.fromLocalDateTimeToExcelFormat(cleanedField.getFirstCheckPointTransaction());
            nextRecord[2] = Validator.fromLocalDateTimeToExcelFormat(cleanedField.getSecondCheckPointTransaction());
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
     * This method iterates through the ArrayLists of raw data; i.e. each ArrayList contains raw data from one station.
     * This method is the interface between the read raw data, the separation method and the data cleaning method
     * @param rawData the big raw data list
     * @param checkPointIDs the checkpoint IDs of various stations
     * @return an ArrayList containing other ArrayLists with clean data, each ArrayList represents a station (a station has 2 checkpoints: Check-in and Check-out)
     */
    public static ArrayList<ArrayList<CleanedField>> dataCleanerIterator(ArrayList<RawField> rawData, ArrayList<Integer> checkPointIDs) {
        var cleanedData = new ArrayList<ArrayList<CleanedField>>();
        var tempCleanedData = new ArrayList<CleanedField>();
        int i = 0;

        ArrayList<ArrayList<RawField>> separatedRawData = stationSeparator(checkPointIDs, rawData); //Convert the big raw data list into separated by station raw data lists

        while (i < checkPointIDs.size()) { //Check that i remains below the checkPointIDs array size to avoid index out of bounds
            //Iterate through the ArrayLists of raw data
            for (ArrayList<RawField> stationRawData : separatedRawData) { //the order of the stations match the order and the number of the checkpoint IDs; otherwise the cleaning of various stations will not work
                tempCleanedData = dataCleaner(stationRawData, checkPointIDs.get(i), checkPointIDs.get(i + 1));
                cleanedData.add((ArrayList<CleanedField>) tempCleanedData.clone());
                tempCleanedData.clear(); //clear temporary ArrayList for new use
                i += 2; //increment i by 2, so it is ready for the next station's two checkpoint IDs
            }
        }
        return cleanedData;
    }

    /**
     * This method is a helper for the dataCleanerIterator method. It cleans raw data from a read CSV file
     * @param rawData the ArrayList containing raw data
     * @param checkPointIDs the checkpoint IDs of the first and second transactions (i.e. Check-in and Check-out)
     * @return a new ArrayList with cleaned data
     */
    private static ArrayList<CleanedField> dataCleaner(ArrayList<RawField> rawData, int... checkPointIDs) {
        final int FIRST_CHECKPOINT_ID = checkPointIDs[0];
        final int SECOND_CHECKPOINT_ID = checkPointIDs[1];
        var cleanedData = new ArrayList<CleanedField>();
        String serialNumber, SKU, customerPN, location;
        long minutesDifference;
        LocalDateTime firstCheckPointTransaction, secondCheckPointTransaction;

        for (int j = 0; j < rawData.size(); j++) {
            if (rawData.get(j).getCheckPointId() == SECOND_CHECKPOINT_ID) { //station's second checkpoint ID
                for (int k = j + 1; k < rawData.size(); k++) {
                    if ((rawData.get(k).getCheckPointId() == FIRST_CHECKPOINT_ID) && //station's first checkpoint ID
                            (rawData.get(k).getSerialNumber().equals(rawData.get(j).getSerialNumber()))) {
                        serialNumber = rawData.get(j).getSerialNumber();
                        firstCheckPointTransaction = rawData.get(k).getTransactionDate();
                        secondCheckPointTransaction = rawData.get(j).getTransactionDate();
                        minutesDifference = ChronoUnit.MINUTES.between(rawData.get(k).getTransactionDate(), rawData.get(j).getTransactionDate());
                        SKU = rawData.get(j).getSKU();
                        customerPN = rawData.get(j).getCustomerPN();
                        location = rawData.get(j).getLocation();

                        cleanedData.add(new CleanedField(serialNumber, firstCheckPointTransaction, secondCheckPointTransaction, minutesDifference, SKU, customerPN, location));
                        j = k; //Jump into the next occurrence of rack CheckPoint 1 & rack CheckPoint 2
                        break;
                    }
                }
            }
        }
        //Sort the cleaned data by the first transaction in ASC order and return the cleaned ArrayList
        cleanedData.sort((CleanedField first, CleanedField second) -> {
            if (first.getFirstCheckPointTransaction().isBefore(second.getFirstCheckPointTransaction()))
                return -1;
            else if (first.getFirstCheckPointTransaction().isAfter(second.getFirstCheckPointTransaction()))
                return 1;
            else return 0;
        });
        return cleanedData;
    }

    /**
     * Helper for the dataCleanerIterator method. This private method takes a big raw data list with fields from various stations (each station has two checkpoints; i.e.: Check-in, Check-out)
     * and separates all the items by station; i.e.: all the items that belong to one station are put into a new ArrayList, then each station's ArrayList is saved into a parent ArrayList
     * @param checkPointIDs all the checkpoint IDs from various stations
     * @param rawData the big raw data list
     * @return an ArrayList of ArrayLists, each containing raw data from one station; i.e.: each item (inner ArrayList) represents a station
     */
    private static ArrayList<ArrayList<RawField>> stationSeparator(ArrayList<Integer> checkPointIDs, ArrayList<RawField> rawData) {
        var separatedRawData = new ArrayList<ArrayList<RawField>>();
        var tempSeparated = new ArrayList<RawField>();

        //Iterate over all checkpoints to separate all items from the big raw data list into smaller raw data lists by stations
        // Note: Each station has 2 checkpoints (i.e.: Check-in and Check-out)
        for (int i = 0; i < checkPointIDs.size(); i++) {
            for (RawField rawField : rawData) {
                if (rawField.getCheckPointId() == checkPointIDs.get(i)) {
                    tempSeparated.add(rawField);
                }
                else if (rawField.getCheckPointId() == checkPointIDs.get(i + 1)) {
                    tempSeparated.add(rawField);
                }
            }
            separatedRawData.add((ArrayList<RawField>) tempSeparated.clone());
            tempSeparated.clear();
            i++; //increase i by 1, so it is ready for the next station's two checkpoint IDs
        }
        return separatedRawData;
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
//    var rawScans = new ArrayList<RawField>(MAX_LIST_CAPACITY);
//    var cleanedScans = new ArrayList<CleanedField>();
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
//        rawScans.add(new RawField(serialNumber, checkPointId, checkPointName, transactionDate, SKU, customerPN, location));
//        }
//        }
//        csvReader.close();
//        Logger.getGlobal().info("The data from CSV file \"" + inFile + "\" read successfully");

//        for (RawField field : rawScans) {
//            System.out.println(field.getSerialNumber() + "\t" + field.getCheckPointId() + "\t" + field.getCheckPointName() + "\t" +
//                    field.getTransactionDate() + "\t" + field.getSKU() + "\t" + field.getCustomerPN() + "\t" + field.getLocation());
//            System.out.println();
//        }
