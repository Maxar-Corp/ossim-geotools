package org.ossim.kettle.steps.createovrhst;

import java.util.List;
import java.util.Map;

import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.annotations.Step;
import org.pentaho.di.core.Counter;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.w3c.dom.Node;

// will take this out of the plugin list temporarily
/*
@Step(
		id="OSSIMCreateOvrHst",
		name="name",
		description="description",
		categoryDescription="categoryDescription", 
		image="org/ossim/kettle/steps/createovrhst/icon.png",
		i18nPackageName="org.omar.kettle.steps.createovrhst"
)
*/
public class CreateOvrHstMeta extends BaseStepMeta implements StepMetaInterface {

	public enum Tag {
		field_name,
	};
	
	private String fieldName;
	
	/**
	 * @return the fieldName
	 */
	String getFieldName() {
		return fieldName;
	}

	/**
	 * @param fieldName the fieldName to set
	 */
	void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}

	/**
	 * checks parameters, adds result to List<CheckResultInterface>
	 * used in Action > Verify transformation
	 */
	void check(List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepMeta, 
			RowMetaInterface prev,String [] input, String [] output, RowMetaInterface info)
	{ 
		if (Const.isEmpty(fieldName)) {
			CheckResultInterface error = new CheckResult(
				CheckResult.TYPE_RESULT_ERROR, 
				Messages.getString("CreateOvrHstMeta.CHECK_ERR_NO_FIELD"), 
				stepMeta
			);
			remarks.add(error);
		} else {
			CheckResultInterface ok = new CheckResult(
				CheckResult.TYPE_RESULT_OK, 
				Messages.getString("CreateOvrHstMeta.CHECK_OK_FIELD"), 
				stepMeta
			);
			remarks.add(ok);
		}
	}

	/**
	 *	creates a new instance of the step (factory)
	 */
	StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface,
			int copyNr, TransMeta transMeta, Trans trans) {
		return new CreateOvrHst(stepMeta, stepDataInterface, copyNr, transMeta, trans);
	}

	/**
	 * creates new instance of the step data (factory)
	 */
	StepDataInterface getStepData() {
		return new CreateOvrHstData();
	}
	
	@Override
	String getDialogClassName() {
		return CreateOvrHstDialog.class.name;
	}

	/**
	 * deserialize from xml 
	 * databases = list of available connections
	 * counters = list of sequence steps
	 */
	void loadXML(Node stepDomNode, List<DatabaseMeta> databases,
			Map<String, Counter> sequenceCounters) throws KettleXMLException {
		fieldName = XMLHandler.getTagValue(stepDomNode, Tag.field_name.name());
	}
	
	/**
	 * @Override
	 */
	String getXML() throws KettleException {
		StringBuilder xml = new StringBuilder();
		xml.append(XMLHandler.addTagValue(Tag.field_name.name(), fieldName));
		return xml.toString();
	}
	
	/**
	 * De-serialize from repository (see loadXML)
	 */
	void readRep(Repository repository, ObjectId stepIdInRepository,
			List<DatabaseMeta> databases, Map<String, Counter> sequenceCounters)
			throws KettleException {
		fieldName = repository.getStepAttributeString(stepIdInRepository, Tag.field_name.name());
	}

	/**
	 * serialize to repository
	 */
	void saveRep(Repository repository, ObjectId idOfTransformation, ObjectId idOfStep)
			throws KettleException {
		repository.saveStepAttribute(idOfTransformation, idOfStep, Tag.field_name.name(), fieldName);
	}
	
	
	/**
	 * initiailize parameters to default
	 */
	void setDefault() {
		fieldName = "filename";
	}

	@Override
	void getFields(RowMetaInterface inputRowMeta, String name,
			RowMetaInterface[] info, StepMeta nextStep, VariableSpace space)
			throws KettleStepException {
//		String realFieldName = space.environmentSubstitute(fieldName);
//		ValueMetaInterface field = new ValueMeta(realFieldName, ValueMetaInterface.TYPE_STRING);
//		field.setOrigin(name);		
//		inputRowMeta.addValueMeta(field);
	}
}
