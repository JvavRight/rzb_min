package com.softeem.rzb.oss.controller.api;

import com.softeem.rzb.common.exception.BusinessException;
import com.softeem.rzb.common.result.R;
import com.softeem.rzb.common.result.ResponseEnum;
import com.softeem.rzb.oss.service.FileService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;
import java.io.InputStream;

@Api(tags = "阿里云文件管理")
@RestController
@RequestMapping("/api/oss/file")
public class FileController {

    @Resource
    private FileService fileService;

    /**
     * 文件上传
     */
    @ApiOperation("文件上传")
    @PostMapping("/upload")
    public R upload(
            @ApiParam(value = "文件", required = true)
            @RequestParam("file") MultipartFile file,

            @ApiParam(value = "模块", required = true)
            @RequestParam("module") String module) {

        try {
            InputStream inputStream = file.getInputStream();
            String originalFilename = file.getOriginalFilename();// 获取上传的文件名
            // 返回的是文件在oss服务器上的路径地址
            String uploadUrl = fileService.upload(inputStream, module, originalFilename);

            //返回r对象
            return R.ok().message("文件上传成功").data("url", uploadUrl);
        } catch (IOException e) {
            throw new BusinessException(ResponseEnum.UPLOAD_ERROR, e);//"文件上传错误"
        }
    }

    @ApiOperation("删除OSS文件")
    @DeleteMapping("/remove")
    public R remove(
            @ApiParam(value = "要删除的文件路径", required = true)
            @RequestParam("url") String url) {
        fileService.removeFile(url);
        return R.ok().message("删除成功");
    }

}