package org.driftprogramming.distributedlock;

import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;

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

}
