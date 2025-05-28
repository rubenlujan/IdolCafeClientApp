package com.hrg.idolcafeclientapp.data.models;

public class SystemConfig {
    private int CompanyId;
    private int AllowPaymentWithTerminal;
    private int AllowSaleAlcohol;
    private String PrinterName;
    private int PrintLogo;

    public int getCompanyId() {
        return CompanyId;
    }

    public void setCompanyId(int companyId) {
        CompanyId = companyId;
    }

    public int getAllowPaymentWithTerminal() {
        return AllowPaymentWithTerminal;
    }

    public void setAllowPaymentWithTerminal(int allowPaymentWithTerminal) {
        AllowPaymentWithTerminal = allowPaymentWithTerminal;
    }

    public int getAllowSaleAlcohol() {
        return AllowSaleAlcohol;
    }

    public void setAllowSaleAlcohol(int allowSaleAlcohol) {
        AllowSaleAlcohol = allowSaleAlcohol;
    }

    public String getPrinterName() {
        return PrinterName;
    }

    public void setPrinterName(String printerName) {
        PrinterName = printerName;
    }

    public int getPrintLogo() {
        return PrintLogo;
    }

    public void setPrintLogo(int printLogo) {
        PrintLogo = printLogo;
    }
}
