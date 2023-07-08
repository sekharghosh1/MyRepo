package com.dws.challenge.service;

public interface LockWrapper {

    void lock();
    void unlock();
    boolean tryLock();
}
