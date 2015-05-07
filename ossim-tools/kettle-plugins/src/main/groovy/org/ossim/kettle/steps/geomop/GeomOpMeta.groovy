package org.ossim.kettle.steps.geomop;

import java.util.List;
import java.util.Map;
import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Counter;
import org.pentaho.di.core.annotations.Step;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueDataUtil;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaAndData;
import org.pentaho.di.core.row.value.ValueMetaFactory;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.w3c.dom.Node;
import org.ossim.kettle.types.OssimValueMetaBase;
import org.ossim.kettle.steps.geomop.GeomOpData.GeomOpType

@Step(
		  id="GeomOperation",
		  name="name",
		  description="description",
		  categoryDescription="categoryDescription",
		  image="org/ossim/kettle/steps/geomop/icon.png",
		  i18nPackageName="org.ossim.steps.kettle.geomop"
)
public class GeomOpMeta extends BaseStepMeta implements StepMetaInterface
{

	def outputFieldName = "geom_op_result"
	def inputGeomField1 = ""
	def inputGeomField2 = ""
	def param1
	def param2
	def operationType   = GeomOpType.UNION

	GeomOpMeta()
	{
		super();
		this.setDefault();
	}

	String getXML() throws KettleValueException
	{
		StringBuffer retval = new StringBuffer(400);
		//retval.append("    <values>"+Const.CR);
		retval.append("        ").append(XMLHandler.addTagValue("outputFieldName", outputFieldName)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		retval.append("        ").append(XMLHandler.addTagValue("inputGeomField1", inputGeomField1)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		retval.append("        ").append(XMLHandler.addTagValue("inputGeomField2", inputGeomField2)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		retval.append("        ").append(XMLHandler.addTagValue("operationType", operationType.toString())); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		retval.append("        ").append(XMLHandler.addTagValue("param1", param1?:"")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		retval.append("        ").append(XMLHandler.addTagValue("param2", param2?:"")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		return retval;
	}

	def isSingleRow()
	{
		def result = false

		switch(operationType)
		{
			case GeomOpType.UNION:
				if(inputGeomField1&&!inputGeomField2)
				{
					result = true
				}
				break
			default:
				break
		}

		result
	}
	def getOutputType()
	{
		def result = OssimValueMetaBase.TYPE_GEOMETRY_2D

		switch(operationType)
		{
			case GeomOpType.DISTANCE..GeomOpType.AREA:
				result = ValueMetaInterface.TYPE_NUMBER
				break
			case GeomOpType.INTERSECTS..GeomOpType.DISJOINT:
				result = ValueMetaInterface.TYPE_BOOLEAN
				break
			default:
				break
				break
		}

		result
	}
	void getFields(RowMetaInterface r, String origin,
						RowMetaInterface[] info,
						StepMeta nextStep, VariableSpace space)
	{
		def oType = this.outputType
		ValueMetaInterface field = ValueMetaFactory.createValueMeta(outputFieldName,
				  oType);

		if(oType == ValueMetaInterface.TYPE_NUMBER)
		{
			field.setLength( -1 );
			field.setPrecision( 18 );
			field.setConversionMask( "##.##################;-##.##################" );
		}
		field.setOrigin(origin);
		r.addValueMeta(field);
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
		def values = stepnode
		try
		{
			def outputFieldNameValue = XMLHandler.getTagValue(values, "outputFieldName");
			def inputGeomField1Value = XMLHandler.getTagValue(values, "inputGeomField1");
			def inputGeomField2Value = XMLHandler.getTagValue(values, "inputGeomField2");
			def operationTypeValue   = XMLHandler.getTagValue(values, "operationType");
			def param1Value   = XMLHandler.getTagValue(values, "param1");
			def param2Value   = XMLHandler.getTagValue(values, "param2");

			if(outputFieldNameValue)
			{
				outputFieldName = outputFieldNameValue
			}
			if(inputGeomField1Value)
			{
				inputGeomField1 = inputGeomField1Value
			}
			if(inputGeomField2Value)
			{
				inputGeomField2 = inputGeomField2Value
			}
			if(operationTypeValue)
			{
				operationType = GeomOpType."${operationTypeValue}"
			}
			if(param1Value)
			{
				param1 = param1Value
			}
			if(param2Value)
			{
				param2 = param2Value
			}
		}
		catch (Exception e)
		{
			throw new KettleXMLException(Messages.getString("FileExistsMeta.Exception.UnableToReadStepInfo"), e); //$NON-NLS-1$
		}
	}

	void setDefault()
	{
		outputFieldName = "geom_op_result"
		inputGeomField1 = ""
		inputGeomField2 = ""
		param1          = ""
		param2          = ""
		operationType   = GeomOpType.UNION
	}

	void readRep(Repository rep, ObjectId id_step, List<DatabaseMeta> databases, Map<String, Counter> counters) throws KettleException
	{
		this.setDefault();
		try
		{
			String outputFieldNameString = rep.getStepAttributeString(id_step, "outputFieldName");
			String inputGeomField1String = rep.getStepAttributeString(id_step, "inputGeomField1");
			String inputGeomField2String = rep.getStepAttributeString(id_step, "inputGeomField2");
			String operationTypeString   = rep.getStepAttributeString(id_step, "operationType");
			String param1String   = rep.getStepAttributeString(id_step, "param1");
			String param2String   = rep.getStepAttributeString(id_step, "param2");

			if(outputFieldNameString != null && !outputFieldNameString.isEmpty())
			{
				outputFieldName = outputFieldNameString;
			}
			if(inputGeomField1String != null && !inputGeomField1String.isEmpty())
			{
				inputGeomField1 = inputGeomField1String;
			}
			if(inputGeomField2String != null && !inputGeomField2String.isEmpty())
			{
				inputGeomField2 = inputGeomField2String;
			}
			if(operationTypeString != null && !operationTypeString.isEmpty())
			{
				operationType = GeomOpType."${operationTypeString}";
			}
			if(param1String != null && !param1String.isEmpty())
			{
				param1 = param1String;
			}
			if(param2String != null && !param2String.isEmpty())
			{
				param2 = param2String;
			}

		}
		catch (Exception e)
		{
			throw new KettleException(Messages.getString("FileExistsMeta.Exception.UnexpectedErrorReadingStepInfo"), e); //$NON-NLS-1$
		}
	}

	void saveRep(Repository rep, ObjectId id_transformation, ObjectId id_step) throws KettleException
	{
		try
		{
			rep.saveStepAttribute(id_transformation,
					  id_step, "outputFieldName",
					  outputFieldName) //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation,
					  id_step, "inputGeomField1",
					  inputGeomField1) //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation,
					  id_step, "inputGeomField2",
					  inputGeomField2) //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation,
					  id_step, "operationType",
					  operationType.toString()) //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation,
					  id_step, "param1",
					  param1.toString()) //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation,
					  id_step, "param2",
					  param2.toString()) //$NON-NLS-1$
		}
		catch (Exception e)
		{
			throw new KettleException(Messages.getString("FileExistsMeta.Exception.UnableToSaveStepInfo") + id_step, e); //$NON-NLS-1$
		}
	}

	void check(List<CheckResultInterface> remarks, TransMeta transmeta, StepMeta stepinfo, RowMetaInterface prev, String[] inputList, String[] outputList, RowMetaInterface info)
	{
		CheckResult cr;
		if (prev==null || prev.size()==0)
		{
			cr = new CheckResult(CheckResult.TYPE_RESULT_WARNING, "Not receiving any fields from previous steps!", stepinfo);
			remarks.add(cr);
		}
		else
		{
			cr = new CheckResult(CheckResult.TYPE_RESULT_OK, "Step is connected to previous one, receiving "+prev.size()+" fields", stepinfo);
			remarks.add(cr);
		}

		// See if we have input streams leading to this step!
		if (inputList.length>0)
		{
			cr = new CheckResult(CheckResult.TYPE_RESULT_OK, "Step is receiving info from other steps.", stepinfo);
			remarks.add(cr);
		}
		else
		{
			cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, "No input received from other steps!", stepinfo);
			remarks.add(cr);
		}
	}

	//String getDialogClassName()
	//{
	//	return DataInfoDialog.class.name;

//	}

	StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta transMeta, Trans disp)
	{
		return new GeomOp(stepMeta, stepDataInterface, cnr, transMeta, disp);
	}

	StepDataInterface getStepData()
	{
		return new GeomOpData();
	}
}
