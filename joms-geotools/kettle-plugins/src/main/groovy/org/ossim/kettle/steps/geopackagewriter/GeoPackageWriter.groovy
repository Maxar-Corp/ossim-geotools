package org.ossim.kettle.steps.geopackagewriter

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import org.ossim.core.SynchOssimInit
import javax.imageio.ImageIO
import javax.media.jai.JAI

import org.geotools.geopkg.GeoPackage
import org.geotools.geopkg.Tile
import org.geotools.geopkg.TileEntry
import org.geotools.geopkg.TileMatrix
import org.geotools.sql.SqlUtil
import java.sql.Connection
import java.sql.PreparedStatement
import java.util.logging.Level
import javax.imageio.ImageReader

import org.geotools.geometry.jts.ReferencedEnvelope
import org.geotools.referencing.CRS
import groovy.json.JsonSlurper
import groovy.json.JsonOutput

import joms.oms.Chipper

// import org.geotools.factory.Hints

// Hints.putSystemDefault(Hints.FORCE_LONGITUDE_FIRST_AXIS_ORDER, Boolean.TRUE)

class GeoPackageWriter extends BaseStep implements StepInterface
{
	private static final def FULL  = "FULL"
	private static final def JPG   = "JPG"
	private static final def PNG   = "PNG"
	private static final def MIXED = "MIXED"
	private static final def JPEGS = ["JPEG", JPG]
	private static final def LOCAL = "local"
	private static final def GLOBAL = "global"

	private GeoPackageWriterMeta meta = null;
	private GeoPackageWriterData data = null;
	private GeoPackage gpkg = null;
	private def connection = null;
	private def tileEntry = null;
	private def outputLocation = "/tmp/test.gpkg"
	private def imageType = PNG
	private def tilingType = LOCAL
	private def layerName = "tiles"
	private def mixed = false

	private def numFullTiles = 0
	private def numPartialTiles = 0

	public GeoPackageWriter(StepMeta stepMeta, StepDataInterface stepDataInterface,
			int copyNr, TransMeta transMeta, Trans trans) 
	{
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
	}

	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException {

		meta = (GeoPackageWriterMeta) smi;
		data = (GeoPackageWriterData) sdi;

		Object[] row = getRow();
		if (row==null) 
		{
			setOutputDone()
			return false
		}

		if (first) 
		{
			first=false
			data.outputRowMeta = getInputRowMeta().clone()
			meta.getFields(data.outputRowMeta, getStepname(), null, null, this)

			// Set general metadata to gpkg_contents and gpkg_tile_matrix
			def inputSummaryEpsgIdx = getInputRowMeta().indexOfValue(meta.inputSummaryEpsgField)
			def inputSummaryMinXIdx = getInputRowMeta().indexOfValue(meta.inputSummaryMinXField)
			def inputSummaryMaxXIdx = getInputRowMeta().indexOfValue(meta.inputSummaryMaxXField)
			def inputSummaryMinYIdx = getInputRowMeta().indexOfValue(meta.inputSummaryMinYField)
			def inputSummaryMaxYIdx = getInputRowMeta().indexOfValue(meta.inputSummaryMaxYField)

			def summaryEpsg = getInputRowMeta().getString(row, inputSummaryEpsgIdx)
			def minX        = getInputRowMeta().getString(row, inputSummaryMinXIdx) as Double
			def maxX        = getInputRowMeta().getString(row, inputSummaryMaxXIdx) as Double
			def minY        = getInputRowMeta().getString(row, inputSummaryMinYIdx) as Double
			def maxY        = getInputRowMeta().getString(row, inputSummaryMaxYIdx) as Double

			if (imageType == MIXED) {
				mixed = true
			}

			tileEntry = new TileEntry()
			tileEntry.setTableName(layerName)
			tileEntry.setBounds(
			    new ReferencedEnvelope(
			        minX,
			        maxX,
			        minY,
			        maxY, 
			        CRS.decode("${summaryEpsg}")))

			// Get indexes for fields
			def tileWidthIdx  = getInputRowMeta().indexOfValue(meta.inputTileWidthField)
			def tileHeightIdx = getInputRowMeta().indexOfValue(meta.inputTileHeightField)
			def inputSummaryLevelInfoIdx = getInputRowMeta().indexOfValue(meta.inputSummaryLevelInfoField)
			logBasic("summary info idx: $inputSummaryLevelInfoIdx")

			// Get the values for the fields
			def tileWidth     = getInputRowMeta().getString(row, tileWidthIdx)
			def tileHeight    = getInputRowMeta().getString(row, tileHeightIdx)
			def levelInfo     = getInputRowMeta().getString(row, inputSummaryLevelInfoIdx) 

			def slurper = new JsonSlurper()
			def levelInfoAsJson = slurper.parseText(levelInfo)

			levelInfoAsJson.each { info ->
				logBasic("    Zoom level : ${info.zoomLevel}")
				tileEntry.getTileMatricies().add( new TileMatrix(
					info.zoomLevel,
					info.ncols,
					info.nrows,
					tileWidth as Integer,
					tileHeight as Integer,
					info.unitsPerPixelX as Double,
					info.unitsPerPixelY as Double))
			}

			logBasic("tile matricies: " + tileEntry.getTileMatricies())
			gpkg.create(tileEntry)
			
		}

		// Process the tile in this row and enter it into tiles
		int tileLocalRowIdx  = getInputRowMeta().indexOfValue(meta.inputTileLocalRowField)
		int tileLocalColIdx  = getInputRowMeta().indexOfValue(meta.inputTileLocalColField)
		int tileGlobalRowIdx = getInputRowMeta().indexOfValue(meta.inputTileGlobalRowField)
		int tileGlobalColIdx = getInputRowMeta().indexOfValue(meta.inputTileGlobalColField)
		int zoomLevelIdx   = getInputRowMeta().indexOfValue(meta.inputTileLevelField)
		int imageIdx       = getInputRowMeta().indexOfValue(meta.inputImageField)
		int imageStatusIdx = getInputRowMeta().indexOfValue(meta.inputImageStatusField)

		def tileLocalRow  = getInputRowMeta().getString(row, tileLocalRowIdx) as Integer
		def tileLocalCol  = getInputRowMeta().getString(row, tileLocalColIdx) as Integer
		def tileGlobalRow = getInputRowMeta().getString(row, tileGlobalRowIdx) as Integer
		def tileGlobalCol = getInputRowMeta().getString(row, tileGlobalColIdx) as Integer
		def zoomLevel   = getInputRowMeta().getString(row, zoomLevelIdx) as Integer
		def image       = row[imageIdx]
		def imageStatus = getInputRowMeta().getString(row, imageStatusIdx)

		(imageStatus == FULL) ? numFullTiles++ : numPartialTiles++

		if (mixed) {
			imageType = (imageStatus == FULL) ? JPG : PNG
		}

		// JPEG images are coming out strange colors so re-render without alpha
		if (imageType in JPEGS) {

			def threeBand = JAI.create("BandSelect", image, [0,1,2] as int[])
			image = threeBand

		}

		if(image)
		{
			def ostream = new ByteArrayOutputStream()
			ImageIO.write(image, imageType, ostream) 

			// Are the rows and columns relative to local or global bounding box
			def relativeRow = (tilingType == LOCAL) ? tileLocalRow : tileGlobalRow
			def relativeCol = (tilingType == LOCAL) ? tileLocalCol : tileGlobalCol
			
			def tile = new Tile(zoomLevel, relativeCol, relativeRow, ostream.toByteArray())
			ostream.close()
			
			// Write tile to gpkg database
			gpkg.add(tileEntry, tile, connection)
		}

		// right now just copy input to output.
		// remove this later and merge your outputs
		//
	   //putRow(data.outputRowMeta, row);

      return true; // finished with this row, process the next row
	}

	public boolean init(StepMetaInterface smi, StepDataInterface sdi)
	{
		SynchOssimInit.initialize()
		data = (GeoPackageWriterData) sdi
		meta = (GeoPackageWriterMeta) smi

		outputLocation = environmentSubstitute(meta.outputFilename)
		imageType = meta.imageType
		tilingType = meta.tilingType
		layerName = meta.layerName
		gpkg = new GeoPackage(outputLocation as File)
		resetDB(gpkg)
		gpkg.metaClass.add << { TileEntry entry, Tile tile, Connection cx -> 
		        PreparedStatement ps = SqlUtil.prepare(cx, String.format("INSERT INTO %s (zoom_level, tile_column,"
		            + " tile_row, tile_data) VALUES (?,?,?,?)", entry.getTableName()))
		            .set(tile.getZoom()).set(tile.getColumn()).set(tile.getRow()).set(tile.getData())
		            .log(Level.FINE).statement();
	        ps.execute();
	        ps.close();
    	}

		connection = gpkg.getDataSource().getConnection()
		connection.setAutoCommit(false)
		
		return super.init(smi, sdi)
	}

	public void dispose(StepMetaInterface smi, StepDataInterface sdi)
	{
		data = null
		meta = null
		connection?.commit()
		gpkg?.close()
		logBasic("FULL = $numFullTiles")
		logBasic("PARTIAL = $numPartialTiles")

 		super.dispose(smi, sdi)
	}

	private def resetDB(def gpkg) {
	    // Delete the existing contents of the table, so for testing purposes
	    // always start fresh
	    Connection cx = gpkg.getDataSource().getConnection();
	    PreparedStatement ps = cx.prepareStatement("DROP TABLE IF EXISTS ${layerName};");
	    ps.execute();

	    ps = cx.prepareStatement("DROP TABLE IF EXISTS ${GeoPackage.GEOPACKAGE_CONTENTS};");
	    ps.execute();
	    
	    ps = cx.prepareStatement("DROP TABLE IF EXISTS ${GeoPackage.GEOMETRY_COLUMNS};");
	    ps.execute();
	    
	    ps = cx.prepareStatement("DROP TABLE IF EXISTS ${GeoPackage.SPATIAL_REF_SYS};");
	    ps.execute();
	    
	    ps = cx.prepareStatement("DROP TABLE IF EXISTS ${GeoPackage.RASTER_COLUMNS};");
	    ps.execute();
	    
	    ps = cx.prepareStatement("DROP TABLE IF EXISTS ${GeoPackage.TILE_MATRIX_METADATA};");
	    ps.execute();
	    
	    ps = cx.prepareStatement("DROP TABLE IF EXISTS ${GeoPackage.METADATA};");
	    ps.execute();
	    
	    ps = cx.prepareStatement("DROP TABLE IF EXISTS ${GeoPackage.METADATA_REFERENCE};");
	    ps.execute();
	    
	    ps = cx.prepareStatement("DROP TABLE IF EXISTS ${GeoPackage.TILE_MATRIX_SET};");
	    ps.execute();
	    
	    ps = cx.prepareStatement("DROP TABLE IF EXISTS ${GeoPackage.DATA_COLUMN_CONSTRAINTS};");
	    ps.execute();
	    
	    ps = cx.prepareStatement("DROP TABLE IF EXISTS ${GeoPackage.EXTENSIONS};");
	    ps.execute();
	    
	    gpkg.init()

	}

	
}