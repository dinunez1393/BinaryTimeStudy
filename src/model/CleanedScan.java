package model;

import java.time.LocalDateTime;

public class CleanedScan {
    private String serialNumber;
    private LocalDateTime firstScanTransaction;
    private LocalDateTime secondScanTransaction;
    private long timeDifferenceMinutes;
    private String SKU;
    private String customerPN;
    private String location;

    public CleanedScan(String serialNumber, LocalDateTime firstScanTransaction, LocalDateTime secondScanTransaction, long timeDifferenceMinutes,
                       String SKU, String customerPN, String location) {
        this.serialNumber = serialNumber;
        this.firstScanTransaction = firstScanTransaction;
        this.secondScanTransaction = secondScanTransaction;
        this.timeDifferenceMinutes = timeDifferenceMinutes;
        this.SKU = SKU;
        this.customerPN = customerPN;
        this.location = location;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public LocalDateTime getFirstScanTransaction() {
        return firstScanTransaction;
    }

    public void setFirstScanTransaction(LocalDateTime firstScanTransaction) {
        this.firstScanTransaction = firstScanTransaction;
    }

    public LocalDateTime getSecondScanTransaction() {
        return secondScanTransaction;
    }

    public void setSecondScanTransaction(LocalDateTime secondScanTransaction) {
        this.secondScanTransaction = secondScanTransaction;
    }

    public long getTimeDifferenceMinutes() {
        return timeDifferenceMinutes;
    }

    public void setTimeDifferenceMinutes(long timeDifferenceMinutes) {
        this.timeDifferenceMinutes = timeDifferenceMinutes;
    }

    public String getSKU() {
        return SKU;
    }

    public void setSKU(String SKU) {
        this.SKU = SKU;
    }

    public String getCustomerPN() {
        return customerPN;
    }

    public void setCustomerPN(String customerPN) {
        this.customerPN = customerPN;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}
