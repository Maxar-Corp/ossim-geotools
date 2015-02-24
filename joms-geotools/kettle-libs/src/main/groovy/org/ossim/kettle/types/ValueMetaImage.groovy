package org.ossim.kettle.types

import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaBase

public class ValueMetaImage extends OssimValueMetaBase {

  public ValueMetaImage() {
    this( null );
  }

  public ValueMetaImage( String name ) {
    super( name, OssimValueMetaBase.TYPE_IMAGE );
  }

  @Override
  public Object getNativeDataType( Object object ) throws KettleValueException {
    getImage(object)
  }

}
