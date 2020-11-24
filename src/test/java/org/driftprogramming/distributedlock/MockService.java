package org.driftprogramming.distributedlock;

import org.driftprogramming.distributedlock.annotation.LockKey;
import org.driftprogramming.distributedlock.annotation.DistributedLock;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class MockService {

    public static Integer COUNT = 0;

    @DistributedLock(lockType = LockType.X_SYSTEM_INVENTORY_LOCK,
            key = {"#name", "#id"},
            waitTime = 10 * 2 + 1,
            expireTime = 2 + 1,
            timeUnit = TimeUnit.SECONDS
    )
    public Long execute_lockable_1(String name, Long id) throws InterruptedException {
        COUNT++;
        Thread.sleep(2000);
        System.out.println("Job done " + COUNT);
        return id;
    }

    @DistributedLock(lockType = LockType.X_SYSTEM_INVENTORY_LOCK, key = {"#order.id"})
    public Long execute_lockable_2(Order order) {
        return order.getId();
    }

    @DistributedLock(lockType = LockType.X_SYSTEM_INVENTORY_LOCK, key = {"#order.item.id"})
    public Long execute_lockable_3(Order order) {
        return order.getId();
    }

    @DistributedLock(lockType = LockType.X_SYSTEM_INVENTORY_LOCK, key = {"#order.id+'.'+#order.item.id"})
    public Long execute_lockable_4(Order order) {
        return order.getId();
    }

    @DistributedLock(lockType = LockType.X_SYSTEM_INVENTORY_LOCK, key = {"#order.id", "#item.id"})
    public Long execute_lockable_5(Order order, Item item) {
        return order.getId();
    }

    @DistributedLock(lockType = LockType.X_SYSTEM_INVENTORY_LOCK, key = {"#order.id + '.' + #item.id"})
    public Long execute_lockable_6(Order order, Item item) {
        return order.getId();
    }

    @DistributedLock(lockType = LockType.X_SYSTEM_INVENTORY_LOCK, key = {"#order.id + '.' + #key"})
    public Long execute_lockable_7(Order order, String key) {
        return order.getId();
    }

    @DistributedLock(lockType = LockType.X_SYSTEM_INVENTORY_LOCK, key = {"#orders.![id]"})
    public Long execute_lockable_8(List<Order> orders) {
        return 1l;
    }

    @DistributedLock(lockType = LockType.X_SYSTEM_INVENTORY_LOCK, key = {"#orders.![id]", "#name"})
    public Long execute_lockable_9(List<Order> orders, String name1, String name) {
        return 1l;
    }

    @DistributedLock(lockType = LockType.X_SYSTEM_INVENTORY_LOCK, key = {"#orders.![id]", "#name"})
    public Long execute_lockable_10(List<Order> orders, @LockKey String name1, String name) {
        return 1l;
    }

    @DistributedLock(lockType = LockType.X_SYSTEM_INVENTORY_LOCK, key = {"#orders.![id]", "#name"})
    public Long execute_lockable_11(List<Order> orders, @LockKey("id") Item item, String name) {
        return 1l;
    }
}
