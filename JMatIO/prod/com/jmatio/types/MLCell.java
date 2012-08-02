package com.jmatio.types;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import com.jmatio.common.MatLevel5DataTypes;
import com.jmatio.io.OSMatTag;
import com.jmatio.io.MatlabIOException;

public class MLCell extends MLArray {
    private ArrayList<MLArray> cells;

    public MLCell(String name, int[] dims) {
        this(name, dims, MatLevel5DataTypes.mxCELL_CLASS, 0);
    }

    public MLCell(String name, int[] dims, int type, int attributes) {
        super(name, dims, type, attributes);

        int length = this.getSize();
        cells = new ArrayList<MLArray>(length);

        for (int i = 0; i < length; ++i)
            cells.add(new MLEmptyArray());
    }

    public void set(MLArray value, int m, int n) {
        this.set(value, this.getIndex(m, n));
    }

    public void set(MLArray value, int index) {
        value.isChild = true;
        this.cells.set(index, value);
    }

    public MLArray get(int m, int n) {
        return cells.get(this.getIndex(m, n));
    }

    public MLArray get(int index) {
        return cells.get(index);
    }

    public ArrayList<MLArray> cells() {
        return cells;
    }

    public String contentToString() {
        StringBuffer sb = new StringBuffer();
        sb.append(this.name + " = \n");

        for (int m = 0; m < this.getM(); ++m) {
            sb.append("\t");
            for (int n = 0; n < this.getN(); ++n) {
                sb.append(this.get(m, n));
                sb.append("\t");
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    public void dispose() {
        if (this.cells != null)
            this.cells.clear();
    }

    public void writeData(DataOutputStream dos) throws IOException {
        for (MLArray a : this.cells())
            a.writeMatrix(dos);
    }
}
