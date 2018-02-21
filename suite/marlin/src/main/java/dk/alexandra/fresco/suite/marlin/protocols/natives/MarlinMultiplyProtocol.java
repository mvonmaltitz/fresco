package dk.alexandra.fresco.suite.marlin.protocols.natives;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.network.serializers.ByteSerializer;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.suite.marlin.datatypes.CompUInt;
import dk.alexandra.fresco.suite.marlin.datatypes.UInt;
import dk.alexandra.fresco.suite.marlin.datatypes.MarlinSInt;
import dk.alexandra.fresco.suite.marlin.datatypes.MarlinTriple;
import dk.alexandra.fresco.suite.marlin.resource.MarlinResourcePool;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MarlinMultiplyProtocol<H extends UInt<H>, L extends UInt<L>, T extends CompUInt<H, L, T>> extends
    MarlinNativeProtocol<SInt, H, L, T> {

  private final DRes<SInt> left;
  private final DRes<SInt> right;
  private MarlinTriple<H, L, T> triple;
  private SInt product;
  private MarlinSInt<H, L, T> epsilon;
  private MarlinSInt<H, L, T> delta;

  public MarlinMultiplyProtocol(DRes<SInt> left, DRes<SInt> right) {
    this.left = left;
    this.right = right;
  }

  @Override
  public EvaluationStatus evaluate(int round, MarlinResourcePool<H, L, T> resourcePool, Network network) {
    final T macKeyShare = resourcePool.getDataSupplier().getSecretSharedKey();
    ByteSerializer<T> serializer = resourcePool.getRawSerializer();
    if (round == 0) {
      triple = resourcePool.getDataSupplier().getNextTripleShares();
      epsilon = ((MarlinSInt<H, L, T>) left.out()).subtract(triple.getLeft());
      delta = ((MarlinSInt<H, L, T>) right.out()).subtract(triple.getRight());
      network.sendToAll(epsilon.getShare().getLeastSignificant().toByteArray());
      network.sendToAll(delta.getShare().getLeastSignificant().toByteArray());
      return EvaluationStatus.HAS_MORE_ROUNDS;
    } else {
      Pair<T, T> epsilonAndDelta = receiveAndReconstruct(network, resourcePool.getNoOfParties(),
          serializer);
      // compute [prod] = [c] + epsilon * [b] + delta * [a] + epsilon * delta
      T e = epsilonAndDelta.getFirst();
      T d = epsilonAndDelta.getSecond();
      T ed = e.multiply(d);
      product = triple.getProduct()
          .add(triple.getRight().multiply(e))
          .add(triple.getLeft().multiply(d))
          .addConstant(ed, resourcePool.getMyId(), macKeyShare, resourcePool.getFactory().zero());
      resourcePool.getOpenedValueStore().pushOpenedValues(
          Arrays.asList(epsilon, delta),
          Arrays.asList(e, d)
      );
      // TODO is this really necessary?
      triple = null;
      epsilon = null;
      delta = null;
      return EvaluationStatus.IS_DONE;
    }
  }

  /**
   * Retrieves shares for epsilon and delta and reconstructs each.
   */
  private Pair<T, T> receiveAndReconstruct(Network network, int noOfParties,
      ByteSerializer<T> serializer) {
    List<T> epsilonShares = new ArrayList<>(noOfParties);
    List<T> deltaShares = new ArrayList<>(noOfParties);
    for (int i = 1; i <= noOfParties; i++) {
      epsilonShares.add(serializer.deserialize(network.receive(i)));
      deltaShares.add(serializer.deserialize(network.receive(i)));
    }
    T e = UInt.sum(epsilonShares);
    T d = UInt.sum(deltaShares);
    return new Pair<>(e, d);
  }

  @Override
  public SInt out() {
    return product;
  }

}