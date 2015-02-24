package joms.geotools.tileapi.hibernate.controller

import joms.geotools.tileapi.hibernate.HibernateUtility
import joms.geotools.tileapi.hibernate.domain.TileCacheLayerInfo
import joms.geotools.tileapi.hibernate.domain.TileCacheTileTableTemplate
import org.hibernate.Criteria
import org.hibernate.Query
import org.hibernate.criterion.Order
import org.hibernate.criterion.Restrictions
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional


/**
 * Created by gpotts on 1/21/15.
 */
@Repository
@Transactional
class TileCacheLayerInfoDAO  extends DAOImpl<TileCacheLayerInfo> implements DAO<TileCacheLayerInfo>
{
  TileCacheLayerInfo findByName(String name) {
    Query query = session.getNamedQuery("findLayerInfoByName")
            .setString("name", name);

    def items = query.list()
    TileCacheLayerInfo result
    if(items)
    {
      result = items.get(0)
    }

    result
  }

  List list()
  {
    Query query = session.getNamedQuery("findAllLayerInfos")

    query.list()
  }

  String sqlFromCriteria()
  {
    Criteria criteria = session.createCriteria(TileCacheTileTableTemplate.class)
            .add( Restrictions.like("hashId", "Iz%") )
            .addOrder( Order.desc("modifiedDate") )

    HibernateUtility.toSql(session, criteria)

  }
}
