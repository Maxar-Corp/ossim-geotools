package joms.geotools.tileapi.hibernate.controller;

/**
 * Created by gpotts on 1/21/15.
 */
public interface DAO<T> {
  Class<T> type

  T save(T t)
  void delete(T t)
  T update(T t)
  T saveOrUpdate(T t)
}
