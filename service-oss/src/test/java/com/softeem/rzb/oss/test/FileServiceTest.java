package com.softeem.rzb.oss.test;

import com.softeem.rzb.oss.service.FileService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class FileServiceTest {

    @Autowired
    private FileService fileService;

    @Test
    public void testUpload() {
        fileService.removeFile("https://rzb-file-min.oss-cn-hangzhou.aliyuncs.com/mincai/2024/07/01/f05909eb-a767-41a9-8765-377aa37a33b0.png");
    }
}
