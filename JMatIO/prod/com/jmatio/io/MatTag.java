package com.jmatio.io;

import java.nio.ByteBuffer;

import com.jmatio.common.MatLevel5DataTypes;

/**
 *
 * @author Wojciech Gradkowski <wgradkowski@gmail.com>
 */
public abstract class MatTag {
    /**
     * The data type, see <code>MatLevel5DataTypes</code>.
     */
    protected int type;
    /**
     * The number of bytes of data in the tag (including any padding).
     */
    protected int size;
    /**
     * Whether the tag uses the small data format.
     */
    protected boolean compressed;
    /**
     * The number of bytes of padding (note that this is unused for <code>type</type> of <code>MatLevel5DataTypes.miCOMPRESSED</code>.
     */
    protected int padding;

    protected ByteBuffer data;

    MatTag(ByteBuffer data) {
        this.data = data;
    }

    /**
     * Calculate padding
     */
    protected void calculatePadding() {
        if (!this.compressed) {
            // Data is not packed in the tag, must be divisible by 8 bytes.
            int b = (this.getNElements() % (8 / this.sizeOf())) * this.sizeOf();
            this.padding = b != 0 ? 8 - b : 0;
        } else {
            // Data is _packed_ in the tag (compressed), must fit into 4 bytes.
            this.padding = 4 - this.size;
        }
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() {
        String s = "[tag: " + MatLevel5DataTypes.matrixTypeToString(type) + " size: " + size + "]";
        return s;
    }

    /**
     * Get size of single data in this tag.
     *
     * @return number of bytes for single data
     */
    public int sizeOf() {
        return MatLevel5DataTypes.sizeOf(type);
    }

    public int getNElements() {
        return this.size / this.sizeOf();
    }
}
