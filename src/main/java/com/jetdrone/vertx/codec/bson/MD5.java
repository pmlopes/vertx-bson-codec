package com.jetdrone.vertx.codec.bson;

/**
 * MD5 Hash
 */
@FunctionalInterface
public interface MD5 {

  /**
   * Hash of a Digest
   * <p>
   * e.g.:
   * MessageDigest md = MessageDigest.getInstance("MD5");
   * byte[] digest = md.digest(data);
   *
   * @return byte representation of this hash
   */
  byte[] getHash();
}
