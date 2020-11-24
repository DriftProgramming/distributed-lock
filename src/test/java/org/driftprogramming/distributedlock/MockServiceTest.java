package org.driftprogramming.distributedlock;

import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SpringBootTest
class MockServiceTest {

    @Autowired
    MockService service;

    @Test
    void test_multiple_threads_lock() throws InterruptedException {
        int THREAD_POOL_SIZE = 100;
        int JOB_COUNT = 10;
        List<Long> ids = new CopyOnWriteArrayList<>();
        ExecutorService executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
        CountDownLatch latch = new CountDownLatch(JOB_COUNT);
        for (int i = 0; i < JOB_COUNT; i++) {
            executorService.execute(() -> {
                try {
                    String name = "order name";
                    Long id = 9080l;
                    Long result = service.execute_lockable_1(name, id);
                } catch (Exception e) {
                    System.out.println(e);
                    Thread.currentThread().interrupt();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(); // wait JOB_COUNT jobs finish.
        executorService.shutdown();
        System.out.println("COUNT: " + MockService.COUNT);
    }

    @Test
    void test_lockable_1() throws InterruptedException {
        service.execute_lockable_1("Rebecca", 1998l);
    }

    @Test
    void test_lockable_2() {
        Order order = new Order(999l, "order1");
        order.setId(999l);
        service.execute_lockable_2(order);
    }

    @Test
    void test_lockable_3() {
        Order order = new Order(111l, "order1");
        Item item = new Item(222l, "item1");
        order.setItem(item);
        service.execute_lockable_3(order);
    }

    @Test
    void test_lockable_4() {
        Order order = new Order(111l, "order1");
        Item item = new Item(222l, "item1");
        order.setItem(item);
        service.execute_lockable_4(order);
    }

    @Test
    void test_lockable_5() {
        Order order = new Order(111l, "order1");
        Item item = new Item(222l, "item1");
        order.setItem(item);
        service.execute_lockable_5(order, item);
    }

    @Test
    void test_lockable_6() {
        Order order = new Order(111l, "order1");
        Item item = new Item(222l, "item1");
        order.setItem(item);
        service.execute_lockable_6(order, item);
    }

    @Test
    void test_lockable_7() {
        Order order = new Order(111l, "order1");
        Item item = new Item(222l, "item1");
        order.setItem(item);
        service.execute_lockable_7(order, "key001");
    }

    @Test
    void test_lockable_8() {
        Order order1 = new Order(111l, "order1");
        Order order2 = new Order(222l, "order2");
        List<Order> orders = new ArrayList<>();
        orders.add(order1);
        orders.add(order2);

        service.execute_lockable_8(orders);
    }

    @Test
    void test_lockable_9() {
        Order order1 = new Order(111l, "order1");
        Order order2 = new Order(222l, "order2");
        List<Order> orders = new ArrayList<>();
        orders.add(order1);
        orders.add(order2);

        service.execute_lockable_9(orders, "name1", "order_name");
    }

    @Test
    void test_lockable_10() {
        Order order1 = new Order(111l, "order1");
        Order order2 = new Order(222l, "order2");
        List<Order> orders = new ArrayList<>();
        orders.add(order1);
        orders.add(order2);

        service.execute_lockable_10(orders, "name1", "order_name");
    }

    @Test
    void test_lockable_11() {
        Order order1 = new Order(111l, "order1");
        Order order2 = new Order(222l, "order2");
        List<Order> orders = new ArrayList<>();
        orders.add(order1);
        orders.add(order2);
        Item item = new Item(333l, "item1");

        service.execute_lockable_11(orders, item, "order_name");
    }
}
