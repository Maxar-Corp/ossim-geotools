package org.ossim.kettle.types

import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaBase

public class ValueMetaClonableImage extends OssimValueMetaBase {

  public ValueMetaClonableImage() {
    this( null );
  }

  public ValueMetaClonableImage( String name ) {
    super( name, OssimValueMetaBase.TYPE_CLONABLE_IMAGE );
  }

  @Override
  public Object getNativeDataType( Object object ) throws KettleValueException {
    getImage(object)
  }

}
