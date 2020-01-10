package me.alidg;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import static java.lang.Math.abs;

public final class LockStripedConcurrentMap<K, V> extends AbstractConcurrentMap<K, V> {

    private final ReentrantLock[] locks;

    public LockStripedConcurrentMap() {
        super();
        locks = new ReentrantLock[INITIAL_CAP];
        for (int i = 0; i < INITIAL_CAP; i++) {
            locks[i] = new ReentrantLock();
        }
    }

    @Override
    protected void acquire(Entry<K, V> entry) {
        locks[abs(entry.hashCode()) % locks.length].lock();
    }

    @Override
    protected void release(Entry<K, V> entry) {
        locks[abs(entry.hashCode()) % locks.length].unlock();
    }

    @Override
    protected boolean shouldResize() {
        return size / table.length >= 5;
    }

    @Override
    @SuppressWarnings({"unchecked", "DuplicatedCode"})
    protected void resize() {
        acquireAll();
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
            releaseAll();
        }
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
