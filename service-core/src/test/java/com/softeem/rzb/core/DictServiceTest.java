package com.softeem.rzb.core;

import com.softeem.rzb.core.pojo.entity.Dict;
import com.softeem.rzb.core.service.DictService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;


import java.util.List;

@SpringBootTest
public class DictServiceTest {

    @Autowired
    private DictService dictService;

    @Test
    public void testListDictData() {
        List<Dict> dicts = dictService.listByParentId(1L);

        dicts.forEach(System.out::println);
    }

}
