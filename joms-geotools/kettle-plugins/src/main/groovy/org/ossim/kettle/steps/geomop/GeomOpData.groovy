package org.ossim.kettle.steps.geomop

import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.core.row.RowMeta

/**
 * 
 * 
  */
class GeomOpData extends BaseStepData implements StepDataInterface
{
	enum GeomOpType
	{
		UNION(0), 
		INTERSECTION(1), 
		DIFFERENCE(2), 
		CONVEX_HULL(3),
		DISTANCE(4),
		AREA(5),
		BOUNDARY(6),
		CENTROID(7),
		ENVELOPE(8),
		INTERSECTS(9),
		EXACTLY_EQUALS(10),
		EQUALS(11),
		CONTAINS(12),
		COVERED_BY(13),
		COVERS(14),
		CROSSES(15),
		IS_SIMPLE(16),
		OVERLAPS(17),
		WITHIN(18),
		DISJOINT(19),
		TOUCHES(20),
		PROJECTION_TRANSFORM(21)
		private int value
		GeomOpType(int value){this.value = value}
      static def valuesAsString(){this.values().collect(){it.toString()}}
      static def supportsSingleInput(def type){
      	def result = false
      	switch(type)
      	{
      		case GeomOpType.UNION:
      		case GeomOpType.CONVEX_HULL:
      		case GeomOpType.BOUNDARY..GeomOpType.ENVELOPE:
      		case GeomOpType.IS_SIMPLE:
      			result = true
      			break
      		default:
      			result = false
      			break
      	}
      	result
      }
      static def supportsTwoInputs(def type){
      	def result = false
      	switch(type)
      	{
      		case GeomOpType.UNION..GeomOpType.DIFFERENCE:
      		case GeomOpType.DISTANCE:
      		case GeomOpType.INTERSECTS..GeomOpType.CROSSES:
      		case GeomOpType.OVERLAPS:
      		case GeomOpType.WITHIN:
      		case GeomOpType.DISJOINT:
      		case GeomOpType.TOUCHES:
      			result = true
      			break
      		default:
      			result = false
      			break
      	}
      	result
      }
	}

	def outputRowMeta = new RowMeta();

   public GeomOpData()
	{
		super();
	}
}
