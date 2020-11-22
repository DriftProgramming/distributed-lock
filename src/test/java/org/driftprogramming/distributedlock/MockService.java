package org.driftprogramming.distributedlock;

import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
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
            expireTime = 20)
    public Long execute(long id, String name) throws InterruptedException {
        System.out.println(new Date());
        COUNT++;
        Thread.sleep(1000);
        return id;
    }
}
