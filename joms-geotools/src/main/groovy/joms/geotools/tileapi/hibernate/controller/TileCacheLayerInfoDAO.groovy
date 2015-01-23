package joms.geotools.tileapi.hibernate.controller

import joms.geotools.tileapi.hibernate.domain.TileCacheLayerInfo
import org.hibernate.Query
import org.springframework.stereotype.Repository

import javax.transaction.Transactional

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

    query.list
  }
}
