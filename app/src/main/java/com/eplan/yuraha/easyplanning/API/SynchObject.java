package com.eplan.yuraha.easyplanning.API;



public class SynchObject {

    private long id;
    private long rowId;
    private int tableId;

    public SynchObject(long id, long rowId, int tableId) {
        this.id = id;
        this.rowId = rowId;
        this.tableId = tableId;
    }

    public SynchObject() {
    }

    public int getTableId() {
        return tableId;
    }

    public void setTableId(int tableId) {
        this.tableId = tableId;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "SynchObject{" +
                "id=" + id +
                ", rowId=" + rowId +
                ", tableId=" + tableId +
                '}';
    }

    public long getRowId() {
        return rowId;
    }

    public void setRowId(long rowId) {
        this.rowId = rowId;
    }
}
