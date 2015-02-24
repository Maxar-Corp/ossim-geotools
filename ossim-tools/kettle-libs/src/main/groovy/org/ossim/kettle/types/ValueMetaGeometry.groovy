package org.ossim.kettle.types

import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaBase

public class ValueMetaGeometry extends OssimValueMetaBase {

  public ValueMetaGeometry() {
    this( null );
  }

  public ValueMetaGeometry( String name ) {
    super( name, OssimValueMetaBase.TYPE_GEOMETRY_2D );
  }
  
  @Override
  public Object getNativeDataType( Object object ) throws KettleValueException {
    getGeometry(geometry)
  }

}
