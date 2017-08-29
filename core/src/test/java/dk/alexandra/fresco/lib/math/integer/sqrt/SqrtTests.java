/*
 * Copyright (c) 2015, 2016 FRESCO (http://github.com/aicis/fresco).
 *
 * This file is part of the FRESCO project.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 * FRESCO uses SCAPI - http://crypto.biu.ac.il/SCAPI, Crypto++, Miracl, NTL, and Bouncy Castle.
 * Please see these projects for any further licensing issues.
 */

package dk.alexandra.fresco.lib.math.integer.sqrt;

import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThread;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadFactory;
import dk.alexandra.fresco.framework.builder.numeric.NumericBuilder;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.network.ResourcePoolCreator;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.framework.value.SInt;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Assert;

public class SqrtTests {

  public static class TestSquareRoot<ResourcePoolT extends ResourcePool> extends
      TestThreadFactory {

    @Override
    public TestThread next() {

      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {

        private final int maxBitLength = 32;
        private final BigInteger[] x = new BigInteger[]{BigInteger.valueOf(1234),
            BigInteger.valueOf(12345), BigInteger.valueOf(123456), BigInteger.valueOf(1234567),
            BigInteger.valueOf(12345678), BigInteger.valueOf(123456789)};
        private final int n = x.length;


        @Override
        public void test() throws Exception {
          Application<List<BigInteger>, ProtocolBuilderNumeric> app =
              builder -> {
                NumericBuilder numBuilder = builder.numeric();

                List<Computation<BigInteger>> results = new ArrayList<>(n);

                for (BigInteger input : x) {
                  Computation<SInt> actualInput = numBuilder.input(input, 1);
                  Computation<SInt> result =
                      builder.advancedNumeric().sqrt(actualInput, maxBitLength);
                  Computation<BigInteger> openResult = builder.numeric().open(result);
                  results.add(openResult);
                }
                return () -> results.stream().map(Computation::out).collect(Collectors.toList());
              };

          List<BigInteger> results = secureComputationEngine.runApplication(app,
              ResourcePoolCreator.createResourcePool(conf.sceConf));

          Assert.assertEquals(n, results.size());

          for (int i = 0; i < results.size(); i++) {
            BigInteger result = results.get(i);
            BigInteger expected = BigInteger.valueOf((long) Math.sqrt(x[i].intValue()));

            BigInteger difference = expected.subtract(result).abs();

            int precision = expected.bitLength() - difference.bitLength();

            boolean shouldBeCorrect = precision >= expected.bitLength();
            boolean isCorrect = expected.equals(result);

            Assert.assertFalse(shouldBeCorrect && !isCorrect);

            System.out.println("sqrt(" + x[i] + ") = " + result + ", expected " + expected + ".");
            Assert.assertTrue(isCorrect);
          }
        }
      };
    }
  }
}
