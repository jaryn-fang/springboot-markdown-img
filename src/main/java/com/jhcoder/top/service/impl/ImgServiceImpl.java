package com.jhcoder.top.service.impl;

import com.github.pagehelper.PageHelper;
import com.jhcoder.top.entity.ImgEntity;
import com.jhcoder.top.mapper.ImgEntityMapper;
import com.jhcoder.top.mapper.PageBean;
import com.jhcoder.top.service.ImgService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ImgServiceImpl implements ImgService {

    @Autowired
    private ImgEntityMapper imgEntityMapper;

    @Override
    public PageBean<ImgEntity> selectPage(int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<ImgEntity> list = this.imgEntityMapper.selectAll();
        return new PageBean<>(list);
    }

    @Override
    public int insert(ImgEntity record) {
        return imgEntityMapper.insert(record);
    }

    @Override
    public int deleteByPrimaryKey(Integer id) {
        return imgEntityMapper.deleteByPrimaryKey(id);
    }

    @Override
    public ImgEntity selectByPrimaryKey(Integer id) {
        return imgEntityMapper.selectByPrimaryKey(id);
    }
}
