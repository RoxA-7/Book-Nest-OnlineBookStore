package com.bookstore.model;

public class StatItem {
    private String label;
    private String meta;
    private long value;
    private long maxValue;

    public StatItem() {
    }

    public StatItem(String label, String meta, long value, long maxValue) {
        this.label = label;
        this.meta = meta;
        this.value = value;
        this.maxValue = maxValue;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getMeta() {
        return meta;
    }

    public void setMeta(String meta) {
        this.meta = meta;
    }

    public long getValue() {
        return value;
    }

    public void setValue(long value) {
        this.value = value;
    }

    public long getMaxValue() {
        return maxValue;
    }

    public void setMaxValue(long maxValue) {
        this.maxValue = maxValue;
    }

    public int getPercent() {
        if (maxValue <= 0) {
            return 0;
        }
        return Math.max(4, (int) Math.round(value * 100.0 / maxValue));
    }
}
