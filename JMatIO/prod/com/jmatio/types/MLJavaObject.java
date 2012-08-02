package com.jmatio.types;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;

import com.jmatio.io.OSMatTag;
import com.jmatio.io.MatlabIOException;
import com.jmatio.common.MatLevel5DataTypes;

/**
 * Class represents Java Object wrapped in an opaque array (matrix).
 *
 * @author Patrick Cloke <pcloke@mitre.org>
 */
public class MLJavaObject extends MLOpaque {
    public MLJavaObject(MLOpaque opaque) throws IllegalArgumentException {
        super(opaque.name, opaque.classType, opaque.className);
        this.data = opaque.data;

        if (!opaque.isJavaObject())
            throw new IllegalArgumentException("Cannot create a MLJavaObject from a type of: " + this.type + ".");
    }

    public MLJavaObject(String name, Object object) throws IOException {
        super(name, MLOpaque.JAVA_OBJECT_TYPE, null);
        this.setObject(object);
    }

    public void setObject(Object object) throws IOException {
        // Serialize the object.
        ByteArrayOutputStream bais = new ByteArrayOutputStream();
        ObjectOutputStream ois = new ObjectOutputStream(bais);
        ois.writeObject(object);
        ois.close();

        // Replace the current data with the new object's serialized form.
        this.data = ByteBuffer.allocate(bais.size());
        this.data.put(bais.toByteArray());

        this.className = object.getClass().getCanonicalName();
    }

    public Object getObject() throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        // Unserialize the object.
        ByteArrayInputStream bais = new ByteArrayInputStream(this.data.array());
        ObjectInputStream ois = new ObjectInputStream(bais);
        Object obj = ois.readObject();
        ois.close();

        // Find the base class of the data buffer by removing all sets of arrays.
        String baseClassName = this.className;
        int level = 0;
        while (baseClassName.endsWith("[]")) {
            baseClassName = baseClassName.substring(0, baseClassName.length() - 2);
            ++level;
        }
        Class clazz = Class.forName(baseClassName);

        // Find the base class of the object.
        Class objClazz = obj.getClass();
        int objLevel = 0;
        Class tempClazz = objClazz;
        while ((tempClazz = tempClazz.getComponentType()) != null) {
            objClazz = tempClazz;
            ++objLevel;
        }

        // Ensure they are the same array level.
        if (level != objLevel)
            throw new MatlabIOException("Different array level, expected " + this.classType + ", got " + objClazz.getCanonicalName());

        // Ensure the class equals the expected class.
        if (!objClazz.equals(clazz))
            throw new MatlabIOException("An error occurred unserializing: " + this.classType);

        return obj;
    }
}
