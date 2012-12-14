package bson.vertx;

/**
 * User Defined Binary Data Object
 */
public interface Binary {

    /**
     * Serialize the Object to a byte array
     * @return byte representation of this Object
     */
    byte[] getBytes();
}
