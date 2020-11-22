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
    void test_method_lock() {
        String name = "denghejun";
        String id = "007";
        String result = service.execute(name, id);
        Assert.assertEquals(name + id, result);
    }

    @Test
    void test_param_index_lock() {
        String name = "denghejun";
        Long id = 111l;
        Long result = service.execute(name, id);
        Assert.assertEquals(id, result);
    }

    @Test
    void test_param_index_with_collection_param_type() {
        Long id = 9898l;
        List<String> names = new ArrayList<>();
        names.add("name1");
        names.add("name2");

        service.execute(id, names);
    }

    @Test
    void test_param_index_lock_two_params() throws InterruptedException {
        String name = "order name";
        Long id = 9080l;
        Long result = service.execute(id, name);
        Assert.assertEquals(id, result);
    }

    @Test
    void test_multiple_threads_lock() throws InterruptedException {
        int THREAD_POOL_SIZE = 100;
        int JOB_COUNT = 40;
        List<Long> ids = new CopyOnWriteArrayList<>();
        ExecutorService executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
        CountDownLatch latch = new CountDownLatch(JOB_COUNT);
        for (int i = 0; i < JOB_COUNT; i++) {
            executorService.execute(() -> {
                try {
                    String name = "order name";
                    Long id = 9080l;
                    Long result = service.execute(id, name);
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

}
