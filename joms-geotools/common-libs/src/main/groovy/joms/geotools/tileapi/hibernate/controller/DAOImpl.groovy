package joms.geotools.tileapi.hibernate.controller

import org.hibernate.Session
import org.hibernate.SessionFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

import javax.annotation.Resource

/**
 * Created by gpotts on 1/21/15.
 */

@Repository
public abstract class DAOImpl<T> implements DAO<T> {
  private Class<T> type;

  @Resource(name = "sessionFactory")
  @Autowired
  SessionFactory sessionFactory;

  void setSessionFactory(SessionFactory sessionFactory) {
    this.sessionFactory = sessionFactory;
  }

  SessionFactory getSessionFactory() {
    this.sessionFactory
  }

  Session getSession() {
    return sessionFactory.getCurrentSession();
  }

  @Override
  @Transactional
  T saveOrUpdate(T t)
  {
    def s = getSession()
    s.saveOrUpdate(t);

    return t
  }

  @Override
  @Transactional
  T update(T t){
    def s = getSession()
    s.update(t);

    return t
  }

  @Override
  @Transactional
  T save(T t) {
    def s = getSession()
    s.save(t);

    return t
    //  getSession().persist(t);
  //  return t;
  }

  @Override
  @Transactional
  void delete(T t) {
    getSession().delete(t);
  }


}
