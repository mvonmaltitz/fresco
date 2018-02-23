package dk.alexandra.fresco.suite.marlin.protocols.natives;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.suite.marlin.datatypes.CompUInt;
import dk.alexandra.fresco.suite.marlin.datatypes.UInt;
import dk.alexandra.fresco.suite.marlin.datatypes.MarlinSInt;
import dk.alexandra.fresco.suite.marlin.resource.MarlinResourcePool;
import dk.alexandra.fresco.suite.marlin.resource.storage.MarlinOpenedValueStore;
import java.math.BigInteger;
import java.util.List;

public class MarlinOutputToAllProtocol<
    CompT extends CompUInt<?, ?, CompT>>
    extends MarlinNativeProtocol<BigInteger, CompT>
    implements RequiresMacCheck {

  private final DRes<SInt> share;
  private BigInteger opened;
  private MarlinSInt<CompT> authenticatedElement;

  public MarlinOutputToAllProtocol(DRes<SInt> share) {
    this.share = share;
  }

  @Override
  public EvaluationStatus evaluate(int round, MarlinResourcePool<CompT> resourcePool,
      Network network) {
    MarlinOpenedValueStore<CompT> openedValueStore = resourcePool.getOpenedValueStore();
    if (round == 0) {
      authenticatedElement = (MarlinSInt<CompT>) share.out();
      network.sendToAll(authenticatedElement.getShare().getLeastSignificant().toByteArray());
      return EvaluationStatus.HAS_MORE_ROUNDS;
    } else {
      List<CompT> shares = resourcePool.getRawSerializer()
          .deserializeList(network.receiveFromAll());
      CompT recombined = UInt.sum(shares);
      openedValueStore.pushOpenedValue(authenticatedElement, recombined);
      this.opened = resourcePool.convertRepresentation(recombined);
      return EvaluationStatus.IS_DONE;
    }
  }

  @Override
  public BigInteger out() {
    return opened;
  }

}