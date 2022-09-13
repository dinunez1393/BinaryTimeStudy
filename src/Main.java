import Utilities.ReaderWriter;
import com.opencsv.exceptions.CsvValidationException;
import model.*;

import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws IOException, CsvValidationException {
        String inFile = "IPQC2_CHIMERA.csv";
        String outFile = "IPQC2_CHIMERA_cleaned.csv";
        final String CSV_EXT = ".csv";
        final String CLEANED_EXT = "_cleaned";
        String directoryName, continueResponse, tempInFile;
        String[] outFileHeader = {"SerialNumber", "IPQC_CheckIn", "IPQC_CheckOut", "TimeDifferenceMinutes", "SKU", "CustomerPN", "Location"};
        var userInput = new Scanner(System.in);
        var rawScans = new ArrayList<RawScan>();
        var cleanedScans = new ArrayList<CleanedScan>();
        var checkpointIDs = new int[2];
        boolean continueScanning = false;

        //Folder where the CSV files are saved
        System.out.println("Enter the name of the directory:");
        directoryName = userInput.nextLine();
        directoryName += "/";

        do {
            //Update incoming and outgoing file names
            System.out.println("Enter the name of the CSV file to read (DO NOT include .csv extension):");
            inFile = userInput.nextLine();
            tempInFile = inFile;
            inFile += CSV_EXT;
            outFile = tempInFile + CLEANED_EXT + CSV_EXT;

            //Update first_transaction and second_transaction column names
            System.out.println("Enter the name of the first transaction column:");
            outFileHeader[1] = userInput.nextLine();
            System.out.println("Enter the name of the second transaction column:");
            outFileHeader[2] = userInput.nextLine();

            //Input for first and second checkpoint IDs
            System.out.println("Enter the ID of the first transaction checkpoint:");
            checkpointIDs[0] = userInput.nextInt();
            userInput.nextLine();
            System.out.println("Enter the ID of the second transaction checkpoint:");
            checkpointIDs[1] = userInput.nextInt();
            userInput.nextLine();

            //Perform reading, cleaning and writing
            rawScans = ReaderWriter.csvReader(directoryName + inFile, 10_000, 7);
            cleanedScans = ReaderWriter.dataCleaner(rawScans, checkpointIDs);
            ReaderWriter.csvWriter(directoryName + outFile, outFileHeader, cleanedScans, 7);
            //Clean ArrayLists for new use
            rawScans.clear();
            cleanedScans.clear();

            //Ask if there are more CSV files to process
            System.out.println("Do you need to scan another file? y/n");
            continueResponse = userInput.nextLine();
            switch (continueResponse) {
                case "y" -> continueScanning = true;
                case "n" -> {
                    continueScanning = false;
                    System.out.println("Goodbye");
                    userInput.close();
                }
                default -> {
                    System.out.println("That's not a valid response. Program terminates now");
                    continueScanning = false;
                    userInput.close();
                }
            }
        } while(continueScanning);
    }
}
