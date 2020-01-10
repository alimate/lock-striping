package me.alidg;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

public final class CoarseGrainedConcurrentMap<K, V> extends AbstractConcurrentMap<K, V> {

    private final ReentrantLock lock;

    public CoarseGrainedConcurrentMap() {
        super();
        lock = new ReentrantLock();
    }

    @Override
    protected void acquire(Entry<K, V> entry) {
        lock.lock();
    }

    @Override
    protected void release(Entry<K, V> entry) {
        lock.unlock();
    }

    protected boolean shouldResize() {
        return size / table.length >= 5;
    }

    @SuppressWarnings({"unchecked", "DuplicatedCode"})
    protected void resize() {
        lock.lock();
        try {
            var oldTable = table;
            var doubledCapacity = oldTable.length * 2;

            table = (List<Entry<K, V>>[]) new List[doubledCapacity];
            for (int i = 0; i < doubledCapacity; i++) {
                table[i] = new ArrayList<>();
            }

            for (var entries : oldTable) {
                for (var entry : entries) {
                    table[findBucket(entry)].add(entry);
                }
            }
        } finally {
            lock.unlock();
        }
    }
}
