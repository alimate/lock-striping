Lock Striping
--------------
A simple repository to demonstrate the fact that how well a fine-grained synchronized concurrent data structure performs
compared to its coarse-grained counterpart.

## Benchmark Results
For the sake of comparison we chose to implement a simple concurrent `Map` data structure. This is the benchmark result
for the implementation that used one lock for all its operations:
```
  8462.372 ±(99.9%) 64.701 ops/s [Average]
  (min, avg, max) = (8158.904, 8462.372, 8817.462), stdev = 115.005
  CI (99.9%): [8397.672, 8527.073] (assumes normal distribution)
```
And the result for the implementation that used the Lock Striping:
```
  14093.433 ±(99.9%) 167.937 ops/s [Average]
  (min, avg, max) = (13648.912, 14093.433, 14737.457), stdev = 298.507
  CI (99.9%): [13925.497, 14261.370] (assumes normal distribution)
```
As you can spot from the result, the implementation based Lock Striping has twice the throughput!