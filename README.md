# Distributed Lock based on Redisson

## DistributedLock Annotation
```
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DistributedLock {
    LockType lockType() default LockType.Default;

    int[] lockIndex() default {};

    TimeUnit timeUnit() default TimeUnit.SECONDS;

    int expireTime() default 30;

    int waitTime() default 30;
}
```

#### 1. lockType
You can add any business type of lock in this lock type enum. e.g. XXX_SYSTEM_ORDER_LOCK
```aidl
public enum LockType {
    Default,
    ...,
}
```

#### 2. lockIndex
An array of parameters index which need to be locked.

#### 3. timeUnit
The unit of lock wait time and expire time. Default is TimeUnit.SECONDS.

#### 4. expireTime
How long this lock will be expired/unlocked automatically. Default is 30s.

#### 5.waitTime
How long we should wait if the current thread can not get the lock immediately. Default is 30s.


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

## A fully usage example
```aidl
@DistributedLock(
            lockType = LockType.X_SYSTEM_INVENTORY_LOCK,
            lockIndex = {0, 1},
            timeUnit = TimeUnit.SECONDS,
            waitTime = 10*2 + 1,
            expireTime = 2 + 1)
    public Long execute(long id, String name) throws InterruptedException {
        System.out.println(new Date());
        COUNT++;
        Thread.sleep(2000); // mock 2s to complete this method.
        return id;
    }
```

## Suggestion
we suggested you use （Optimistic Lock + Distributed Lock）to keep the Eventual Consistency.
E.g. what if somehow one distributed lock method running too long and can not unlock as we expected, so
you need to use Optimistic Lock to keep the business data has Eventual Consistency.
