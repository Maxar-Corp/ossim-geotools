package joms.geotools.tileapi.hibernate

import org.hibernate.Criteria
import org.hibernate.Session
import org.hibernate.engine.spi.SessionFactoryImplementor
import org.hibernate.hql.internal.ast.ASTQueryTranslatorFactory
import org.hibernate.hql.spi.QueryTranslator
import org.hibernate.hql.spi.QueryTranslatorFactory
import org.hibernate.internal.CriteriaImpl
import org.hibernate.internal.SessionImpl
import org.hibernate.loader.OuterJoinLoader
import org.hibernate.loader.criteria.CriteriaLoader
import org.hibernate.persister.entity.OuterJoinLoadable

import java.lang.reflect.Field

/**
 * Created by gpotts on 1/23/15.
 */
public class HibernateUtility {

  public static String toSql(Session session, Criteria criteria) {
    try {
      CriteriaImpl c = (CriteriaImpl) criteria;
      SessionImpl s = (SessionImpl) c.getSession();
      SessionFactoryImplementor factory = (SessionFactoryImplementor) s.getSessionFactory();
      String[] implementors = factory.getImplementors(c.getEntityOrClassName());
      CriteriaLoader loader = new CriteriaLoader((OuterJoinLoadable) factory.getEntityPersister(implementors[0]),
              factory, c, implementors[0], s.getLoadQueryInfluencers());

      Field f = OuterJoinLoader.class.getDeclaredField("sql");
      f.setAccessible(true);
      return (String) f.get(loader);
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
  }


  public static String toSql(Session session, String hqlQueryText) {
    if (hqlQueryText != null && hqlQueryText.trim().length() > 0) {
      final QueryTranslatorFactory translatorFactory = new ASTQueryTranslatorFactory();
      final SessionFactoryImplementor factory =
              (SessionFactoryImplementor) session.getSessionFactory();
      final QueryTranslator translator = translatorFactory.
              createQueryTranslator(
                      hqlQueryText,
                      hqlQueryText,
                      java.util.Collections.EMPTY_MAP, factory
              );
      translator.compile(Collections.EMPTY_MAP, false);
      return translator.getSQLString();
    }
    return null;
  }
}