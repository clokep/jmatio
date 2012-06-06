package com.jmatio.types;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;

import com.jmatio.io.OSMatTag;
import com.jmatio.io.MatlabIOException;
import com.jmatio.common.MatDataTypes;
import com.jmatio.types.MLUInt8;

/**
 * Class represents opaque array (matrix).
 *
 * @author Patrick Cloke <pcloke@mitre.org>
 */
public class MLOpaque extends MLArray {
    private ByteBuffer data;
    private String className;
    private String classType;

    public MLOpaque(String name, String classType, String className) {
        super(name, new int[]{1}, MLArray.mxOPAQUE_CLASS, 0);

        this.name = name;
        this.className = className;
        this.classType = classType;
    }

    public void set(ByteBuffer data) {
        this.data = data;
    }

    public Object get() throws MatlabIOException, IOException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        if (!this.classType.equals("java"))
            throw new MatlabIOException("Unsupported opaque class: " + this.classType);

        // Unserialize the object.
        ByteArrayInputStream bais = new ByteArrayInputStream(this.data.array());
        ObjectInputStream ois = new ObjectInputStream(bais);
        Object ret = ois.readObject();
        ois.close();

        // Ensure the class equals the expected class.
        /*Class clazz = Class.forName(this.className);
        if (!ret.getClass().equals(clazz))
            throw new MatlabIOException("An error occurred unserializing: " + this.classType);
        */

        return ret;
    }

    public ByteBuffer getBuffer() {
        return this.data;
    }

    public String contentToString() {
        StringBuffer sb = new StringBuffer();
        sb.append(this.name + " (" + this.className + ") = \n");
        sb.append("\t");
        //sb.append(this.get());
        sb.append("\n");

        return sb.toString();
    }

    public void dispose() {
        if (this.data != null)
            this.data = null;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object o) {
        if (o instanceof  MLOpaque) {
            return this.data.equals(((MLOpaque)o).data);
        }
        return super.equals(o);
    }

    /**
     * Writes MATRIX into <code>OutputStream</code>. MLOpaques are written a bit
     * differently, so we need to override this method.
     *
     * @param os <code>OutputStream</code>
     * @throws IOException
     */
    @Override
    public void writeMatrix(OutputStream os) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);

        // Write the flags first, as normal.
        this.writeFlags(dos);

        // The name is written (we skip the dimensions).
        this.writeName(dos);

        // The class type.
        this.writeString(dos, this.classType);

        // The class name.
        this.writeString(dos, this.className);

        // Write the actual data.
        this.writeData(dos);

        // Write matrix tag.
        OSMatTag tag = new OSMatTag(MatDataTypes.miMATRIX, baos.toByteArray());
        tag.writeTo(os);
    }

    public void writeData(DataOutputStream dos) throws IOException {
        // Recreate a matrix from the raw bytes and write it.
        MLUInt8 matrix = new MLUInt8("", this.data.array(), 1);
        matrix.isChild = true;
        matrix.writeMatrix(dos);
    }
}
