package com.dws.challenge.service;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ReentrantLockWrapper implements LockWrapper{
    private final Lock lock;

    public ReentrantLockWrapper() {
        this.lock = new ReentrantLock();
    }

    @Override
    public void lock() {
        lock.lock();
    }

    @Override
    public void unlock() {
        lock.unlock();
    }

    @Override
    public boolean tryLock() {
        return lock.tryLock();
    }
}
