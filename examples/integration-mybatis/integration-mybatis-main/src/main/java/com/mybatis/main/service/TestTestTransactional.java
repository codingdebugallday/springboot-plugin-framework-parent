package com.mybatis.main.service;

import com.mybatis.main.mapper.RoleMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * <p>
 * description
 * </p>
 *
 * @author isaac 2020/7/1 17:05
 * @since 1.0
 */
@Service
public class TestTestTransactional {

    @Autowired
    private RoleMapper roleMapper;

    @Transactional(rollbackFor = Exception.class)
    public void transactional(){
        roleMapper.insert(String.valueOf(System.currentTimeMillis()), "123");
        roleMapper.insert(String.valueOf(System.currentTimeMillis()), "1234");
        int a = 1/0;
        roleMapper.insert(String.valueOf(System.currentTimeMillis()), "13");
    }


}
