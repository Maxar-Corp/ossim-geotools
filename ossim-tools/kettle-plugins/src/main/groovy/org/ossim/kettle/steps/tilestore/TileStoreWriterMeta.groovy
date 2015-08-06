package org.ossim.kettle.steps.tilestore

import org.apache.commons.lang.StringUtils
import org.ossim.kettle.steps.datainfoindexer.DataInfoIndexer
import org.ossim.kettle.steps.datainfoindexer.DataInfoIndexerData
import org.ossim.kettle.steps.datainfoindexer.DataInfoIndexerDialog
import org.ossim.kettle.steps.datainfoindexer.Messages
import org.ossim.omar.hibernate.Hibernate
import org.pentaho.di.core.CheckResult
import org.pentaho.di.core.CheckResultInterface
import org.pentaho.di.core.Const
import org.pentaho.di.core.Counter
import org.pentaho.di.core.annotations.Step
import org.pentaho.di.core.database.DatabaseMeta
import org.pentaho.di.core.exception.KettleException
import org.pentaho.di.core.exception.KettleValueException
import org.pentaho.di.core.exception.KettleXMLException
import org.pentaho.di.core.namedcluster.NamedClusterManager
import org.pentaho.di.core.namedcluster.model.NamedCluster
import org.pentaho.di.core.row.RowMetaInterface
import org.pentaho.di.core.row.ValueMeta
import org.pentaho.di.core.row.ValueMetaInterface
import org.pentaho.di.core.variables.VariableSpace
import org.pentaho.di.core.xml.XMLHandler
import org.pentaho.di.repository.ObjectId
import org.pentaho.di.repository.Repository
import org.pentaho.di.trans.Trans
import org.pentaho.di.trans.TransMeta
import org.pentaho.di.trans.step.BaseStepMeta
import org.pentaho.di.trans.step.StepDataInterface
import org.pentaho.di.trans.step.StepInterface
import org.pentaho.di.trans.step.StepMeta
import org.pentaho.di.trans.step.StepMetaInterface
import org.pentaho.metastore.api.exceptions.MetaStoreException
import org.w3c.dom.Node

/**
 * Created by gpotts on 3/19/15.
 */
@Step(
        id="OSSIMTileStoreWriter",
        name="name",
        description="description",
        categoryDescription="categoryDescription",
        image="org/ossim/kettle/steps/tilestore/icon.png",
        i18nPackageName="org.ossim.steps.kettle.tilestore"
)
class TileStoreWriterMeta extends BaseStepMeta implements StepMetaInterface
{
   TileStoreCommon tileStoreCommon

   Boolean passInputFields

   /**
    * If Layer field name is not empty then the input field will contain the layer to write to
    * else we will ue the layerName field
    */
   String layerFieldName
   String layerName

   String tileLevelFieldName
   String tileRowFieldName
   String tileColFieldName
   String tileMinXFieldName
   String tileMinYFieldName
   String tileMaxXFieldName
   String tileMaxYFieldName
   String epsgCodeFieldName
   String imageFieldName
   String imageStatusFieldName

   String getXML() throws KettleValueException
   {
      StringBuffer retval = new StringBuffer(400);

      retval.append( "    " ).append( XMLHandler.addTagValue( "passInputFields", passInputFields) );
      retval.append( "    " ).append( XMLHandler.addTagValue( "layerFieldName", layerFieldName?:"" ) );
      retval.append( "    " ).append( XMLHandler.addTagValue( "layerName", layerName?:"" ) );

      retval.append( "    " ).append( XMLHandler.addTagValue( "tileLevelFieldName", tileLevelFieldName?:"" ) );
      retval.append( "    " ).append( XMLHandler.addTagValue( "tileRowFieldName", tileRowFieldName?:"" ) );
      retval.append( "    " ).append( XMLHandler.addTagValue( "tileColFieldName", tileColFieldName?:"" ) );
      retval.append( "    " ).append( XMLHandler.addTagValue( "tileMinXFieldName", tileMinXFieldName?:"" ) );
      retval.append( "    " ).append( XMLHandler.addTagValue( "tileMinYFieldName", tileMinYFieldName?:"" ) );
      retval.append( "    " ).append( XMLHandler.addTagValue( "tileMaxXFieldName", tileMaxXFieldName?:"" ) );
      retval.append( "    " ).append( XMLHandler.addTagValue( "tileMaxYFieldName", tileMaxYFieldName?:"" ) );
      retval.append( "    " ).append( XMLHandler.addTagValue( "epsgCodeFieldName", epsgCodeFieldName?:"" ) );
      retval.append( "    " ).append( XMLHandler.addTagValue( "imageFieldName", imageFieldName?:"" ) );
      retval.append( "    " ).append( XMLHandler.addTagValue( "imageStatusFieldName", imageStatusFieldName?:"" ) );

      tileStoreCommon.getXML(retval, repository)

      return retval.toString();
   }

   void getFields(RowMetaInterface r, String origin, RowMetaInterface[] info,
                  StepMeta nextStep, VariableSpace space)
   {
      // if(outputResultFlag)
      // {
      try
      {

      }
      catch ( MetaStoreException e )
      {
         logDebug( e.getMessage(), e );
      }
      //}
   }

   Object clone()
   {
      Object retval = super.clone();
      return retval;
   }

   void loadXML(Node stepnode, List<DatabaseMeta> databases, Map<String,Counter> counters)
           throws KettleXMLException
   {
      this.setDefault();
      readData(stepnode, databases);
   }

   private void readData(Node stepnode, List<DatabaseMeta> databases)
           throws KettleXMLException
   {
      try
      {
         String passInputFieldsString = XMLHandler.getTagValue(stepnode, "passInputFields");
         layerFieldName       = XMLHandler.getTagValue(stepnode, "layerFieldName");
         layerName            = XMLHandler.getTagValue(stepnode, "layerName");
         tileLevelFieldName   = XMLHandler.getTagValue(stepnode, "tileLevelFieldName")?:""
         tileRowFieldName     = XMLHandler.getTagValue(stepnode, "tileRowFieldName")?:""
         tileColFieldName     = XMLHandler.getTagValue(stepnode, "tileColFieldName")?:""
         tileMinXFieldName    = XMLHandler.getTagValue(stepnode, "tileMinXFieldName")?:""
         tileMinYFieldName    = XMLHandler.getTagValue(stepnode, "tileMinYFieldName")?:""
         tileMaxXFieldName    = XMLHandler.getTagValue(stepnode, "tileMaxXFieldName")?:""
         tileMaxYFieldName    = XMLHandler.getTagValue(stepnode, "tileMaxYFieldName")?:""
         epsgCodeFieldName    = XMLHandler.getTagValue(stepnode, "epsgCodeFieldName")?:""
         imageFieldName       = XMLHandler.getTagValue(stepnode, "imageFieldName")?:""
         imageStatusFieldName = XMLHandler.getTagValue(stepnode, "imageStatusFieldName")?:""

         if(passInputFieldsString) passInputFields = passInputFieldsString.toBoolean()
         tileStoreCommon.readData(stepnode, databases, repository)
      }
      catch (Exception e)
      {
         throw new KettleXMLException(Messages.getString("DataInfoIndexerMeta.Exception.UnableToReadStepInfo"), e); //$NON-NLS-1$
      }
   }

   void setDefault()
   {
      tileLevelFieldName   = "tile_level"
      tileRowFieldName     = "tile_row"
      tileColFieldName     = "tile_col"
      tileMinXFieldName    = "tile_minx"
      tileMinYFieldName    = "tile_miny"
      tileMaxXFieldName    = "tile_maxx"
      tileMaxYFieldName    = "tile_maxy"
      epsgCodeFieldName    = "tile_epsg"
      imageFieldName       = "image"
      imageStatusFieldName = "image_status"
      tileStoreCommon      = new TileStoreCommon()
      tileStoreCommon.setDefault()
      passInputFields      = true
   }

   void readRep(Repository rep, ObjectId id_step, List<DatabaseMeta> databases, Map<String, Counter> counters) throws KettleException {

      this.setDefault();
      try
      {
         String passInputFieldsString  = rep.getStepAttributeString(id_step, "passInputFieldsString")?:""
         layerFieldName  = rep.getStepAttributeString(id_step, "layerFieldName")?:""
         layerName  = rep.getStepAttributeString(id_step, "layerName")?:""
         tileLevelFieldName  = rep.getStepAttributeString(id_step, "tileLevelFieldName")?:""
         tileRowFieldName  = rep.getStepAttributeString(id_step, "tileRowFieldName")?:""
         tileColFieldName  = rep.getStepAttributeString(id_step, "tileColFieldName")?:""
         tileMinXFieldName  = rep.getStepAttributeString(id_step, "tileMinXFieldName")?:""
         tileMinYFieldName  = rep.getStepAttributeString(id_step, "tileMinYFieldName")?:""
         tileMaxXFieldName  = rep.getStepAttributeString(id_step, "tileMaxXFieldName")?:""
         tileMaxYFieldName  = rep.getStepAttributeString(id_step, "tileMaxYFieldName")?:""
         imageFieldName  = rep.getStepAttributeString(id_step, "imageFieldName")?:""
         imageStatusFieldName  = rep.getStepAttributeString(id_step, "imageStatusFieldName")?:""
         tileStoreCommon.readRep(rep, id_step, databases, counters)

         if(passInputFieldsString) passInputFields = passInputFieldsString.toBoolean()
      }
      catch (Exception e)
      {
         // System.out.println (e);
         throw new KettleException(Messages.getString("FileExistsMeta.Exception.UnexpectedErrorReadingStepInfo"), e); //$NON-NLS-1$
      }
   }

   void saveRep(Repository rep, ObjectId id_transformation, ObjectId id_step) throws KettleException
   {
      try
      {
         rep.saveStepAttribute( id_transformation, id_step, 0, "passInputFields", passInputFields );
         if ( layerName ) {
            rep.saveStepAttribute( id_transformation, id_step, 0, "layerName", layerName );
         }
         if ( layerFieldName ) {
            rep.saveStepAttribute( id_transformation, id_step, 0, "layerFieldName", layerFieldName );
         }

         if ( tileLevelFieldName ) {
            rep.saveStepAttribute( id_transformation, id_step, 0, "tileLevelFieldName", tileLevelFieldName );
         }
         if ( tileRowFieldName ) {
            rep.saveStepAttribute( id_transformation, id_step, 0, "tileRowFieldName", tileRowFieldName );
         }
         if ( tileColFieldName ) {
            rep.saveStepAttribute( id_transformation, id_step, 0, "tileColFieldName", tileColFieldName );
         }
         if ( tileMinXFieldName ) {
            rep.saveStepAttribute( id_transformation, id_step, 0, "tileMinXFieldName", tileMinXFieldName );
         }
         if ( tileMinYFieldName ) {
            rep.saveStepAttribute( id_transformation, id_step, 0, "tileMinYFieldName", tileMinYFieldName );
         }
         if ( tileMaxXFieldName ) {
            rep.saveStepAttribute( id_transformation, id_step, 0, "tileMaxXFieldName", tileMaxXFieldName );
         }
         if ( tileMaxYFieldName ) {
            rep.saveStepAttribute( id_transformation, id_step, 0, "tileMaxYFieldName", tileMaxYFieldName );
         }
         if ( epsgCodeFieldName  ) {
            rep.saveStepAttribute( id_transformation, id_step, 0, "epsgCodeFieldName", epsgCodeFieldName );
         }
         if ( imageFieldName ) {
            rep.saveStepAttribute( id_transformation, id_step, 0, "imageFieldName", imageFieldName );
         }
         if ( imageStatusFieldName ) {
            rep.saveStepAttribute( id_transformation, id_step, 0, "imageStatusFieldName", imageStatusFieldName );
         }
         tileStoreCommon.saveRep(rep,id_transformation, id_step)
      }
      catch(e)
      {
         logError(e.toString())
      }
   }

   void check(List<CheckResultInterface> remarks, TransMeta transmeta, StepMeta stepinfo, RowMetaInterface prev, String[] inputList, String[] outputList, RowMetaInterface info)
   {
   }

   StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta transMeta, Trans disp)
   {
      return new TileStoreWriter(stepMeta, stepDataInterface, cnr, transMeta, disp);
   }
   String getDialogClassName()
   {
      return TileStoreWriterDialog.class.name;
   }

   StepDataInterface getStepData()
   {
      return new TileStoreCommonData();
   }

}
