package org.jscsi.target.bench;

import java.util.Random;

import org.perfidix.Benchmark;
import org.perfidix.ouput.TabularSummaryOutput;
import org.perfidix.result.BenchmarkResult;

public class StorageBench {

    long index = 0;
    static final int[] toWrite = {
        1024, 2048, 4096, 8192
    };

    static final Random ran = new Random(123l);

    static byte[] data;
    static {
        for (int i = 0; i < toWrite.length; i++) {
            data = new byte[toWrite[i]];
            ran.nextBytes(data);
        }
    }

    public void benchWrite() {

    }

    public static void main(String[] args) {
        Benchmark benchmark = new Benchmark();
        benchmark.add(StorageBench.class);
        BenchmarkResult res = benchmark.run();
        TabularSummaryOutput tab = new TabularSummaryOutput();
        tab.visitBenchmark(res);
    }

}
