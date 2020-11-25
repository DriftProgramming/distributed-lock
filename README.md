# Distributed Lock based on Redisson


## How we specify the waitTime and expireTime.

E.g. we have a method, we estimate it should take 2s to complete. 
And we have 10 threads/concurrency to call this method. And this method was locked once a thread call it.
So, basically it is:

```
method_estimated_effort: 2s
concurrency_threads: 10
```
We suggested:
```
waitTime =(concurrency_threads*method_estimated_effort + 1)s = (10*2 + 1)s = 21s
expireTime =(method_estimated_effort + 1)s = (2+1)s = 3s
```

## Redis Config
```aidl
spring.redis.host=127.0.0.1
spring.redis.port=6379
#spring.redis.password=
```

## A fully usage example
```aidl
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

```

## How to Handle Exception
```aidl
try {
  service.execute(id,name); // Will try to get distrubuted lock here.
} catch(DistributedLockException distributedLockException) {
  // handle distributedlock exception
} catch(OtherException otherException) {
 // handle other exceptions ...
}
```

## Suggestion
we suggested you use （Optimistic Lock(Version Strategy) + Distributed Lock）to keep the Eventual Consistency.
E.g. what if somehow one distributed lock method running too long and can not unlock as we expected, so
you need to use Optimistic Lock to keep the business data has Eventual Consistency.

## Notes
- `@DistributedLock` annotation based on Spring AOP, it works for spring bean only, it means it will not work for nested method calling. E.g.:

#### Noncompliant Code Example

```aidl
void method1() {
  this.method2(); // This will NOT work!!
}

@DistributedLock(key = {"#name"})
void method2(String name) {

}

``` 
#### Solution 1:
Separate method2 into another object.


#### Solution 2:
Add @EnableAspectJAutoProxy(exposeProxy = true) onto your springboot application.


@SpringBootApplication
@EnableAspectJAutoProxy(exposeProxy = true)
public class DistributedLockApplication {

	public static void main(String[] args) {
		SpringApplication.run(DistributedLockApplication.class, args);
	}
}

And then 
```aidl
void method1() {
   ((YourClassType) AopContext.currentProxy()).method2("name1");
}

@DistributedLock(key = {"#name"})
void method2(String name) {
}
```