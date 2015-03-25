package org.ossim.kettle.steps.tilestore

import geoscript.proj.Projection
import org.pentaho.di.core.CheckResultInterface
import org.pentaho.di.core.Const
import org.pentaho.di.core.Counter
import org.pentaho.di.core.annotations.Step
import org.pentaho.di.core.database.DatabaseMeta
import org.pentaho.di.core.exception.KettleException
import org.pentaho.di.core.exception.KettleValueException
import org.pentaho.di.core.exception.KettleXMLException
import org.pentaho.di.core.row.RowMetaInterface
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
 * Created by gpotts on 3/24/15.
 */

@Step(
        id="TileStoreOperation",
        name="operation.name",
        description="operation.description",
        categoryDescription="categoryDescription",
        image="org/ossim/kettle/steps/tilestore/icon.png",
        i18nPackageName="org.ossim.steps.kettle.tilestore"
)
class TileStoreOperationMeta extends BaseStepMeta implements StepMetaInterface
{
   TileStoreCommon tileStoreCommon
   /**
    * If Layer field name is not empty then the input field will contain the layer to write to
    * else we will ue the layerName field
    */
   String layerFieldName
   String layerName
   TileStoreWriterData.TileStoreOpType operationType = TileStoreWriterData.TileStoreOpType.CREATE_LAYER

   String wktBoundsField
   String minXField
   String minYField
   String maxXField
   String maxYField
   String epsgField
   String minLevelField
   String maxLevelField
   String tileWidthField
   String tileHeightField

   String getXML() throws KettleValueException
   {
      StringBuffer retval = new StringBuffer(400);

      tileStoreCommon.getXML(retval, repository)
      retval.append( "    " ).append( XMLHandler.addTagValue( "layerFieldName", layerFieldName?:"" ) );
      retval.append( "    " ).append( XMLHandler.addTagValue( "layerName", layerName?:"" ) );
      retval.append( "    " ).append(XMLHandler.addTagValue("operationType", operationType.toString())); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
   }
   void getFields(RowMetaInterface r, String origin, RowMetaInterface[] info,
                  StepMeta nextStep, VariableSpace space)
   {
      try
      {

      }
      catch ( MetaStoreException e )
      {
         logDebug( e.getMessage(), e );
      }
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
         tileStoreCommon?.readData(stepnode, databases, repository)
         layerFieldName         = XMLHandler.getTagValue(stepnode, "layerFieldName");
         layerName              = XMLHandler.getTagValue(stepnode, "layerName");
         def operationTypeValue = XMLHandler.getTagValue(stepnode, "operationType");
         if(operationTypeValue)
         {
            operationType = TileStoreWriterData.TileStoreOpType."${operationTypeValue}"
         }
      }
      catch (Exception e)
      {
         throw new KettleXMLException(org.ossim.kettle.steps.datainfoindexer.Messages.getString("DataInfoIndexerMeta.Exception.UnableToReadStepInfo"), e); //$NON-NLS-1$
      }
   }
   void readRep(Repository rep, ObjectId id_step, List<DatabaseMeta> databases, Map<String, Counter> counters) throws KettleException
   {
      this.setDefault();
      try
      {
         tileStoreCommon.readRep(rep, id_step, databases, counters)
         layerFieldName  = rep.getStepAttributeString(id_step, "layerFieldName")?:""
         layerName  = rep.getStepAttributeString(id_step, "layerName")?:""
         String operationTypeString   = rep.getStepAttributeString(id_step, "operationType");
         if(operationTypeString)
         {
            operationType = TileStoreWriterData.TileStoreOpType."${operationTypeString}"
         }
      }
      catch (Exception e)
      {
         // System.out.println (e);
         throw new KettleException(org.ossim.kettle.steps.datainfoindexer.Messages.getString("FileExistsMeta.Exception.UnexpectedErrorReadingStepInfo"), e); //$NON-NLS-1$
      }
   }

   void saveRep(Repository rep, ObjectId id_transformation, ObjectId id_step) throws KettleException
   {
      try
      {
         tileStoreCommon.saveRep(rep, id_transformation, id_step)
         if ( layerName)
         {
            rep.saveStepAttribute( id_transformation, id_step, 0, "layerName", layerName );
         }
         if ( layerFieldName)
         {
            rep.saveStepAttribute( id_transformation, id_step, 0, "layerFieldName", layerFieldName );
         }
         rep.saveStepAttribute(id_transformation,
                 id_step, "operationType",
                 operationType.toString()) //$NON-NLS-1$
      }
      catch(e)
      {

      }
   }
   void check(List<CheckResultInterface> remarks, TransMeta transmeta, StepMeta stepinfo, RowMetaInterface prev, String[] inputList, String[] outputList, RowMetaInterface info)
   {
   }

   StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta transMeta, Trans disp)
   {
      return new TileStoreOperation(stepMeta, stepDataInterface, cnr, transMeta, disp);
   }
   String getDialogClassName()
   {
      return TileStoreOperationDialog.class.name;
   }

   StepDataInterface getStepData()
   {
      return new TileStoreWriterData();
   }
   void setDefault()
   {
      tileStoreCommon  = new TileStoreCommon()
      tileStoreCommon.setDefault()

      wktBoundsField  = ""
      minXField       = "-180.0"
      minYField       = "-90.0"
      maxXField       = "180.0"
      maxYField       = "90.0"
      epsgField       = "EPSG:4326"
      minLevelField   = "0"
      maxLevelField   = "22"
      tileWidthField  = "256"
      tileHeightField = "256"
   }

}

