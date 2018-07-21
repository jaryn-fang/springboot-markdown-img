package com.jhcoder.top.mapper;

import com.github.pagehelper.Page;
import com.jhcoder.top.entity.ImgEntity;
import java.util.List;

public interface ImgEntityMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(ImgEntity record);

    ImgEntity selectByPrimaryKey(Integer id);

    List<ImgEntity> selectAll();

    int updateByPrimaryKey(ImgEntity record);

}