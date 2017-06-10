package dk.alexandra.fresco.suite.spdz;

import dk.alexandra.fresco.framework.MPCException;
import dk.alexandra.fresco.framework.Reporter;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.sce.resources.ResourcePoolImpl;
import dk.alexandra.fresco.framework.sce.resources.storage.StreamedStorage;
import dk.alexandra.fresco.suite.spdz.configuration.SpdzConfiguration;
import dk.alexandra.fresco.suite.spdz.storage.SpdzStorage;
import dk.alexandra.fresco.suite.spdz.storage.SpdzStorageDummyImpl;
import dk.alexandra.fresco.suite.spdz.storage.SpdzStorageImpl;
import dk.alexandra.fresco.suite.spdz.utils.Util;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Random;

public class SpdzResourcePool extends ResourcePoolImpl {

  private final StreamedStorage streamedStorage;
  private MessageDigest messageDigest;
  private int modulusSize;
  private BigInteger modulus;
  private BigInteger modulusHalf;
  private SpdzStorage store;
  private boolean outputProtocolInBatch;

  public SpdzResourcePool(int myId, int noOfPlayers,
      Network network,
      StreamedStorage streamedStorage,
      Random random, SecureRandom secRand,
      SpdzConfiguration spdzConf) {
    super(myId, noOfPlayers, network, random, secRand);
    this.streamedStorage = streamedStorage;

    switch (spdzConf.getPreprocessingStrategy()) {
      case DUMMY:
        store = new SpdzStorageDummyImpl(myId, noOfPlayers);
        break;
      case STATIC:
        store = new SpdzStorageImpl(0, noOfPlayers, myId, streamedStorage);
        break;
      case FUELSTATION:
        store = new SpdzStorageImpl(0, noOfPlayers, myId, spdzConf.fuelStationBaseUrl());
    }

    try {
      messageDigest = MessageDigest.getInstance("SHA-256");
    } catch (NoSuchAlgorithmException e) {
      Reporter.warn("SHA-256 not supported as digest on this system. Might not influence "
          + "computation if your chosen SCPS does not depend on a hash function.");
    }

    try {
      this.store.getSSK();
    } catch (MPCException e) {
      throw new MPCException("No preprocessed data found for SPDZ - aborting.", e);
    }

    // Initialize various fields global to the computation.
    this.modulus = store.getSupplier().getModulus();
    this.modulusHalf = this.modulus.divide(BigInteger.valueOf(2));
    this.modulusSize = this.modulus.toByteArray().length;

// TODO
    Util.setModulus(this.getModulus());
  }

  public BigInteger getModulus() {
    return modulus;
  }

  public SpdzStorage getStore() {
    return store;
  }

  public MessageDigest getMessageDigest() {
    return messageDigest;
  }

  public int getModulusSize() {
    return modulusSize;
  }

  boolean isOutputProtocolInBatch() {
    return outputProtocolInBatch;
  }

  public void setOutputProtocolInBatch(boolean outputProtocolInBatch) {
    this.outputProtocolInBatch = outputProtocolInBatch;
  }

  public StreamedStorage getStreamedStorage() {
    return streamedStorage;
  }

  public BigInteger convertRepresentation(BigInteger b) {
    BigInteger actual = b.mod(modulus);
    if (actual.compareTo(modulusHalf) > 0) {
      actual = actual.subtract(modulus);
    }
    return actual;
  }
}
