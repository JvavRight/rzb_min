package com.softeem.rzb.core.service.impl;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.softeem.rzb.core.listener.ExcelDictDTOListener;
import com.softeem.rzb.core.mapper.DictMapper;
import com.softeem.rzb.core.pojo.dto.ExcelDictDTO;
import com.softeem.rzb.core.pojo.entity.Dict;
import com.softeem.rzb.core.service.DictService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * 数据字典 服务实现类
 * </p>
 *
 * @author min
 * @since 2024-06-27
 */
@Slf4j
@Service
public class DictServiceImpl extends ServiceImpl<DictMapper, Dict> implements DictService {

    /*
        baseMapper是父类ServiceImpl的属性,所以可以直接使用
        baseMapper 相当于 下面注入的DictMapper
    */
    /*@Autowired
    private DictMapper dictMapper;*/

    @Resource
    private RedisTemplate redisTemplate;

    /**
     * 根据dictCode查询数据字典
     *
     * @param dictCode
     * @param value
     * @return
     */
    @Override
    public String getNameByParentDictCodeAndValue(String dictCode, Integer value) {
        // 查询父节点
        Dict parentDict = baseMapper.selectOne(
                new QueryWrapper<Dict>().eq("dict_code", dictCode));

        if (parentDict == null) {
            return "";
        }
        // 根据父节点id查询子节点
        Dict dict = baseMapper.selectOne(
                new QueryWrapper<Dict>()
                        .eq("parent_id", parentDict.getId())
                        .eq("value", value));

        if (dict == null) {
            return "";
        }

        return dict.getName();
    }

    /**
     * 根据dictCode查询数据字典
     *
     * @param dictCode
     * @return
     */
    @Override
    public List<Dict> findByDictCode(String dictCode) {
        QueryWrapper<Dict> dictQueryWrapper = new QueryWrapper<>();
        dictQueryWrapper.eq("dict_code", dictCode);
        Dict dict = baseMapper.selectOne(dictQueryWrapper);
        return this.listByParentId(dict.getId());
    }

    /**
     * 导入数据字典
     *
     * @param inputStream
     */
    @Transactional(rollbackFor = {Exception.class})//添加事务注解,任何异常都回滚
    @Override
    public void importData(InputStream inputStream) {
        // 这里 需要指定读用哪个class去读，然后读取第一个sheet 文件流会自动关闭
        EasyExcel.read(inputStream, ExcelDictDTO.class, new ExcelDictDTOListener(baseMapper)).sheet().doRead();
        log.info("importData finished");
    }

    /**
     * 导出数据字典
     *
     * @return
     */
    @Override
    public List<ExcelDictDTO> listDictData() {

        List<Dict> dictList = baseMapper.selectList(null);
        //创建ExcelDictDTO列表，将Dict列表转换成ExcelDictDTO列表
        ArrayList<ExcelDictDTO> excelDictDTOList = new ArrayList<>(dictList.size());
        dictList.forEach(dict -> {

            ExcelDictDTO excelDictDTO = new ExcelDictDTO();
            BeanUtils.copyProperties(dict, excelDictDTO);
            excelDictDTOList.add(excelDictDTO);
        });
        return excelDictDTOList;
    }

    /**
     * 根据父id查询子节点
     *
     * @param parentId
     * @return
     */
    @Override
    public List<Dict> listByParentId(Long parentId) {

        //先查询redis中是否存在数据列表
        List<Dict> dictList = null;
        try {
            dictList = (List<Dict>) redisTemplate.opsForValue().get("rzb:core:dictList:" + parentId);
            if (dictList != null) {
                log.info("从redis中取值");
                return dictList;
            }
        } catch (Exception e) {
            log.error("redis服务器异常：" + ExceptionUtils.getStackTrace(e));//此处不抛出异常，继续执行后面的代码
        }

        log.info("从数据库中取值");
        dictList = baseMapper.selectList(new QueryWrapper<Dict>().eq("parent_id", parentId));
        dictList.forEach(dict -> {
            //如果有子节点，则是非叶子节点
            boolean hasChildren = this.hasChildren(dict.getId());
            dict.setHasChildren(hasChildren);
        });

        //将数据存入redis
        try {
            redisTemplate.opsForValue().set("rzb:core:dictList:" + parentId, dictList, 5, TimeUnit.MINUTES);
            log.info("数据存入redis");
        } catch (Exception e) {
            log.error("redis服务器异常：" + ExceptionUtils.getStackTrace(e));//此处不抛出异常，继续执行后面的代码
        }
        return dictList;
    }

    /**
     * 判断该节点是否有子节点
     *
     * @param id 字典表的主键id
     * @return true:有子节点，false:没有子节点
     */
    private boolean hasChildren(Long id) {
        QueryWrapper<Dict> queryWrapper = new QueryWrapper<Dict>().eq("parent_id", id);
        Integer count = baseMapper.selectCount(queryWrapper);
        if (count.intValue() > 0) {
            return true;
        }
        return false;
    }
}
