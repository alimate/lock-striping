package me.alidg;

import org.openjdk.jmh.annotations.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;
import static org.openjdk.jmh.annotations.Mode.Throughput;

@Fork(value = 2)
@Warmup(iterations = 5)
@State(Scope.Benchmark)
public class MicroBenchmark {

    private static final int NUMBER_OF_ITERATIONS = 100;
    private static final List<String> KEYS = IntStream.rangeClosed(1, 100).mapToObj(i -> "key" + i).collect(toList());

    public static void main(String[] args) throws Exception {
        org.openjdk.jmh.Main.main(args);
    }

    @Benchmark
    @BenchmarkMode(Throughput)
    public void forCoarseGrainedLocking() {
        workload(new CoarseGrainedConcurrentMap<>());
    }

    @Benchmark
    @BenchmarkMode(Throughput)
    public void forStripedLocking() {
        workload(new LockStripedConcurrentMap<>());
    }

    private void workload(ConcurrentMap<String, String> map) {
        var requests = new CompletableFuture<?>[NUMBER_OF_ITERATIONS * 3];
        for (var i = 0; i < NUMBER_OF_ITERATIONS; i++) {
            requests[3 * i] = CompletableFuture.supplyAsync(() -> map.put(randomKey(), "value"));
            requests[3 * i + 1] = CompletableFuture.supplyAsync(() -> map.get(randomKey()));
            requests[3 * i + 2] = CompletableFuture.supplyAsync(() -> map.remove(randomKey()));
        }

        CompletableFuture.allOf(requests).join();
    }

    private String randomKey() {
        return KEYS.get(ThreadLocalRandom.current().nextInt(100));
    }
}
