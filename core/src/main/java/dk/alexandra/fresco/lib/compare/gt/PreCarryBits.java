package dk.alexandra.fresco.lib.compare.gt;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.util.SIntPair;
import dk.alexandra.fresco.framework.value.SInt;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PreCarryBits implements Computation<SInt, ProtocolBuilderNumeric> {

  private final DRes<List<DRes<SIntPair>>> pairsDef;

  public PreCarryBits(DRes<List<DRes<SIntPair>>> pairs) {
    this.pairsDef = pairs;
  }

  @Override
  public DRes<SInt> buildComputation(ProtocolBuilderNumeric builder) {
    List<DRes<SIntPair>> pairs = pairsDef.out();
    Collections.reverse(pairs);
    int k = pairs.size();
    if (k == 1) {
      return pairs.get(0).out().getSecond();
    } else {
      DRes<List<DRes<SIntPair>>> bitsU = builder.par(par -> {
        List<DRes<SIntPair>> bitsUInner = new ArrayList<>(k / 2);
        for (int i = 0; i < k / 2; i++) {
          DRes<SIntPair> left = pairs.get(2 * i + 1);
          DRes<SIntPair> right = pairs.get(2 * i);
          bitsUInner.add(par.seq(new CarryHelper(left, right)));
        }
        List<DRes<SIntPair>> nextRound = bitsUInner.subList(0, k / 2);
        Collections.reverse(nextRound);
        return () -> nextRound;
      });
      return builder.seq(new PreCarryBits(bitsU));
    }
  }


}
