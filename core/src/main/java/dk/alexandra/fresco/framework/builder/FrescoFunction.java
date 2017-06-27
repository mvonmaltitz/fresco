package dk.alexandra.fresco.framework.builder;

import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.builder.ProtocolBuilder.SequentialProtocolBuilder;
import java.util.function.BiFunction;
import java.util.function.Function;

public interface FrescoFunction<InputT, OutputT> extends
    BiFunction<InputT, SequentialProtocolBuilder, Computation<OutputT>> {

  default Function<SequentialProtocolBuilder, Computation<OutputT>> asFunction(InputT input) {
    return (builder) -> apply(input, builder);
  }
}
