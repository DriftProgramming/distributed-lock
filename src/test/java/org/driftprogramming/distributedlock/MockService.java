package org.driftprogramming.distributedlock;

import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
public class MockService {

    public static Integer COUNT = 0;

    @DistributedLock
    public String execute(String name, String id) {
        return name + id;
    }

    @DistributedLock(lockIndex = {1})
    public Long execute(String name, Long id) {
        return id;
    }

    @DistributedLock(lockIndex = {0, 1})
    public Long execute(long id, List<String> names) {
        return id;
    }

    @DistributedLock(
            lockType = LockType.X_SYSTEM_INVENTORY_LOCK,
            lockIndex = {0, 1},
            timeUnit = TimeUnit.SECONDS,
            waitTime = 10 * 2 + 1,
            expireTime = 3)
    public Long execute(long id, String name) throws InterruptedException {
        COUNT++;
        int millis = COUNT == 2 ? 25000 : 2000;
        System.out.println("Thread " + Thread.currentThread().getId() + " will work: " + millis + " millis");
        Thread.sleep(millis);
        return id;
    }
}
