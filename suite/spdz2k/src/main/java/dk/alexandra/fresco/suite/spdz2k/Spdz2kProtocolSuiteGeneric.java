package dk.alexandra.fresco.suite.spdz2k;

import dk.alexandra.fresco.suite.spdz2k.datatypes.GenericCompUInt;
import dk.alexandra.fresco.suite.spdz2k.datatypes.CompUIntConverterGeneric;

class Spdz2kProtocolSuiteGeneric extends
    Spdz2kProtocolSuite<GenericCompUInt, GenericCompUInt, GenericCompUInt> {

  private final int highBitLength;
  private final int lowBitLength;

  Spdz2kProtocolSuiteGeneric(int highBitLength, int lowBitLength) {
    super(new CompUIntConverterGeneric(highBitLength, lowBitLength));
    this.highBitLength = highBitLength;
    this.lowBitLength = lowBitLength;
  }

}