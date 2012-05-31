package com.jmatio.types;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.jmatio.common.MatDataTypes;
import com.jmatio.io.OSArrayTag;

/**
 * This class represents Matlab's Structure object (structure array).
 *
 * Note: Array of structures can contain only structures of the same type,
 * that means structures that have the same field names.
 *
 * @author Wojciech Gradkowski <wgradkowski@gmail.com>
 */
public class MLStructure extends MLArray {
    /**
     * A Set that keeps structure field names.
     */
    private Set<String> keys;
    /**
     * Array of structures.
     */
    private List<Map<String, MLArray>> mlStructArray;
    /**
     * Current structure pointer for bulk insert.
     */
    private int currentIndex = 0;

    public MLStructure(String name, int[] dims) {
        this(name, dims, MLArray.mxSTRUCT_CLASS, 0);
    }

    public MLStructure(String name, int[] dims, int type, int attributes) {
        super(name, dims, type, attributes);

        mlStructArray = new ArrayList<Map<String, MLArray>>(this.getSize());
        keys = new LinkedHashSet<String>();
    }

    /**
     * Sets field for current structure.
     *
     * @param name - name of the field
     * @param value - <code>MLArray</code> field value
     */
    public void setField(String name, MLArray value) {
        this.setField(name, value, currentIndex);
    }

    /**
     * Sets field for (m,n)'th structure in struct array.
     *
     * @param name - name of the field
     * @param value - <code>MLArray</code> field value
     * @param m
     * @param n
     */
    public void setField(String name, MLArray value, int m, int n) {
        this.setField(name, value, this.getIndex(m, n));
    }

    /**
     * Sets filed for structure described by index in struct array.
     *
     * @param name - name of the field
     * @param value - <code>MLArray</code> field value
     * @param index
     */
    public void setField(String name, MLArray value, int index) {
        this.keys.add(name);
        this.currentIndex = index;
        value.isChild = true;

        if (this.mlStructArray.isEmpty() || this.mlStructArray.size() <= index)
            this.mlStructArray.add(index, new LinkedHashMap<String, MLArray>());
        this.mlStructArray.get(index).put(name, value);
    }

    /**
     * Gets the maximum length of field descriptor.
     *
     * @return the maximum field length
     */
    public int getMaxFieldLength() {
        int maxLen = 0;
        for (String s : this.keys)
            maxLen = s.length() > maxLen ? s.length() : maxLen;
        return maxLen + 1;
    }

    public byte[] getMaxFieldLengthToByteArray() {
        return ByteBuffer.allocate(4).putInt(this.getMaxFieldLength()).array();
    }

    /**
     * Dumps field names to byte array. Field names are written as Zero End Strings.
     *
     * @return field names as byte arrays
     */
    public byte[] getKeySetToByteArray() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);

        char[] buffer = new char[this.getMaxFieldLength()];

        try {
            for (String s : this.keys) {
                Arrays.fill(buffer, (char)0);
                System.arraycopy(s.toCharArray(), 0, buffer, 0, s.length());
                dos.writeBytes(new String(buffer));
            }
        } catch  (IOException e) {
            System.err.println("Could not write Structure key set to byte array: " + e );
            return new byte[0];
        }
        return baos.toByteArray();
    }

    /**
     * Gets all field from struct array as flat list of fields.
     *
     * @return the fields of the struct
     */
    public Collection<MLArray> getAllFields() {
        ArrayList<MLArray> fields = new ArrayList<MLArray>();

        for (Map<String, MLArray> struct : this.mlStructArray)
            fields.addAll(struct.values());
        return fields;
    }

    /**
     * Returns the {@link Collection} of keys for this structure.
     * @return the {@link Collection} of keys for this structure
     */
    public Collection<String> getFieldNames() {
        Set<String> fieldNames = new LinkedHashSet<String> ();

        fieldNames.addAll(this.keys);

        return fieldNames;

    }

    /**
     * Gets a value of the field described by name from current structure
     * in struct array.
     *
     * @param name
     * @return the value of a field
     */
    public MLArray getField(String name) {
        return this.getField(name, this.currentIndex);
    }

    /**
     * Gets a value of the field described by name from (m,n)'th structure
     * in struct array.
     *
     * @param name
     * @param m
     * @param n
     * @return the value of a field
     */
    public MLArray getField(String name, int m, int n) {
        return this.getField(name, this.getIndex(m, n));
    }
    /**
     * Gets a value of the field described by name from index'th structure
     * in struct array.
     *
     * @param name
     * @param index
     * @return the value of the field
     */
    public MLArray getField(String name, int index) {
        return this.mlStructArray.get(index).get(name);
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof MLStructure) {
            // Ensure the field names are equal.
            if (!this.keys.equals(((MLStructure)o).keys))
                return false;
            // Ensure the values of those fields are equal.
            for (String key : this.keys) {
                if (!this.getField(key).equals(((MLStructure)o).getField(key)))
                    return false;
            }
            return true;
        }
        return super.equals(o);
    }

    /* (non-Javadoc)
     * @see com.paradigmdesigner.matlab.types.MLArray#contentToString()
     */
    public String contentToString() {
        StringBuffer sb = new StringBuffer();
        sb.append(name + " = \n");

        if (this.getSize() == 1) {
            for (String key : this.keys)
                sb.append("\t" + key + " : " + this.getField(key) + "\n");
        } else {
            sb.append("\n");
            sb.append(this.getM() + "x" + this.getN());
            sb.append(" struct array with fields: \n");
            for (String key : this.keys)
                sb.append("\t" + key + "\n");
        }
        return sb.toString();
    }

    public void dispose() {
        if (this.keys != null && this.mlStructArray != null)
            this.mlStructArray.clear();
        this.currentIndex = 0;
    }

    public void writeData(DataOutputStream dos) throws IOException {
        // Field name length.
        OSArrayTag tag = new OSArrayTag(MatDataTypes.miINT32, this.getMaxFieldLengthToByteArray());
        tag.writeTo(dos);

        // Get field names.
        tag = new OSArrayTag(MatDataTypes.miINT8, this.getKeySetToByteArray());
        tag.writeTo(dos);

        // Don't check the name for fields
        for (MLArray a : this.getAllFields())
            a.writeMatrix(dos);
    }
}
