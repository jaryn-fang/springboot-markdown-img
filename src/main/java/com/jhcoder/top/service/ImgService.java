package com.jhcoder.top.service;

import com.jhcoder.top.entity.ImgEntity;
import com.jhcoder.top.mapper.PageBean;

public interface ImgService {
    PageBean<ImgEntity> selectPage(int pageNum, int pageSize);

    int insert(ImgEntity record);

    int deleteByPrimaryKey(Integer id);

    ImgEntity selectByPrimaryKey(Integer id);

}
