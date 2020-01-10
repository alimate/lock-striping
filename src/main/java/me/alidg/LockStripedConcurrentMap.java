package me.alidg;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import static java.lang.Math.abs;

/**
 * An implementation of {@link ConcurrentMap} based on lock striping. Instead of using just one lock for all operations,
 * this implementation distributes an array of locks between different hash table buckets. This trick is based on the
 * fact that a hash table includes a set of independent sub-structures. So we can use different locks for different
 * buckets and consequently reduce the contention and improve the throughput.
 *
 * @param <K> The map key type.
 * @param <V> The map value type.
 */
public final class LockStripedConcurrentMap<K, V> extends AbstractConcurrentMap<K, V> {

    /**
     * Collection of locks to load-balance the synchronization process.
     */
    private final ReentrantLock[] locks;

    /**
     * Initializes a new instance of this implementation by creating the locks.
     */
    public LockStripedConcurrentMap() {
        super();
        locks = new ReentrantLock[INITIAL_CAPACITY];
        for (int i = 0; i < INITIAL_CAPACITY; i++) {
            locks[i] = new ReentrantLock();
        }
    }

    /**
     * Tries to acquire a lock for the given {@code entry} or waits until the lock becomes available.
     *
     * @param entry The entry to find the lock for.
     */
    @Override
    protected void acquire(Entry<K, V> entry) {
        lockFor(entry).lock();
    }

    /**
     * Tries to release the lock for the given {@code entry}.
     *
     * @param entry The entry to find the lock for.
     */
    @Override
    protected void release(Entry<K, V> entry) {
        lockFor(entry).unlock();
    }

    /**
     * Determines whether or not we should resize the hash table buckets. If we have more than
     * five elements in each bucket (on average), then we add more buckets.
     *
     * @return true if we need to add more buckets, false otherwise.
     */
    @Override
    protected boolean shouldResize() {
        return size / table.length >= 5;
    }

    /**
     * Deprecate the old buckets and creates a new set of buckets with twice the size. Since we're modifying the
     * buckets, we first should acquire all possible locks. Otherwise, we could possibly put the data structure
     * consistency at risk.
     */
    @Override
    @SuppressWarnings({"unchecked", "DuplicatedCode"})
    protected void resize() {
        acquireAll();
        try {
            var oldTable = table;
            var doubledCapacity = oldTable.length * 2;

            table = (List<Entry<K, V>>[]) new List[doubledCapacity];
            for (var i = 0; i < doubledCapacity; i++) {
                table[i] = new ArrayList<>();
            }

            for (var entries : oldTable) {
                for (var entry : entries) {
                    table[findBucket(entry)].add(entry);
                }
            }
        } finally {
            releaseAll();
        }
    }

    private ReentrantLock lockFor(Entry<K, V> entry) {
        return locks[abs(entry.hashCode()) % locks.length];
    }

    private void acquireAll() {
        for (ReentrantLock lock : locks) {
            lock.lock();
        }
    }

    private void releaseAll() {
        for (ReentrantLock lock : locks) {
            lock.unlock();
        }
    }
}
