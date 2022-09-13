package model;

import java.time.LocalDateTime;

public class CleanedField implements Comparable<CleanedField> {
    private String serialNumber;
    private LocalDateTime firstCheckPointTransaction;
    private LocalDateTime secondCheckPointTransaction;
    private long timeDifferenceMinutes;
    private String SKU;
    private String customerPN;
    private String location;

    public CleanedField(String serialNumber, LocalDateTime firstCheckPointTransaction, LocalDateTime secondCheckPointTransaction, long timeDifferenceMinutes,
                        String SKU, String customerPN, String location) {
        this.serialNumber = serialNumber;
        this.firstCheckPointTransaction = firstCheckPointTransaction;
        this.secondCheckPointTransaction = secondCheckPointTransaction;
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

    public LocalDateTime getFirstCheckPointTransaction() {
        return firstCheckPointTransaction;
    }

    public void setFirstCheckPointTransaction(LocalDateTime firstCheckPointTransaction) {
        this.firstCheckPointTransaction = firstCheckPointTransaction;
    }

    public LocalDateTime getSecondCheckPointTransaction() {
        return secondCheckPointTransaction;
    }

    public void setSecondCheckPointTransaction(LocalDateTime secondCheckPointTransaction) {
        this.secondCheckPointTransaction = secondCheckPointTransaction;
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

    @Override
    public int compareTo(CleanedField o) {
        if (this.firstCheckPointTransaction.isBefore(o.firstCheckPointTransaction)) {
            return -1;
        }
        else if (this.firstCheckPointTransaction.isAfter(o.firstCheckPointTransaction)) {
            return 1;
        }
        return 0;
    }
}
