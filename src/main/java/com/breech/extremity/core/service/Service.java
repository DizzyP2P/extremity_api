package com.breech.extremity.core.service;

import com.breech.extremity.core.exception.ServiceException;
import org.apache.ibatis.exceptions.TooManyResultsException;
import tk.mybatis.mapper.entity.Condition;

import java.util.List;

public interface Service<T> {
    void save(T model);
    void save(List<T> models);
    void deleteById(String id);
    void deleteByIds(String ids);
    void update(T model);
    T findById(String id);
    T findBy(String fieldName, Object value) throws TooManyResultsException, ServiceException;
    List<T> findByIds(String ids);
    List<T> findByCondition(Condition condition);
    List<T> findAll();
}
