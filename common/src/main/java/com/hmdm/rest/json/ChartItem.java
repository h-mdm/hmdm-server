package com.hmdm.rest.json;

/**
 * <p>A DTO to carry the data for a statistics chart item.</p>
 *
 * @author seva
 */
public class ChartItem {
    private String stringAttr;
    private int intAttr;
    private int number;

    public String getStringAttr() {
        return stringAttr;
    }

    public void setStringAttr(String stringAttr) {
        this.stringAttr = stringAttr;
    }

    public int getIntAttr() {
        return intAttr;
    }

    public void setIntAttr(int intAttr) {
        this.intAttr = intAttr;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public void setNumber(Long number) {
        this.number = number != null ? number.intValue() : 0;
    }
}
