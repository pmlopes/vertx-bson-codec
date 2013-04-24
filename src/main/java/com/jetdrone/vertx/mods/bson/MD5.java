package com.jetdrone.vertx.mods.bson;

/**
 * MD5 Hash
 */
public interface MD5 {

    /**
     * Hash of a Digest
     *
     * e.g.:
     * MessageDigest md = MessageDigest.getInstance("MD5");
     * byte[] digest = md.digest(data);
     *
     * @return byte representation of this hash
     */
    byte[] getHash();
}
