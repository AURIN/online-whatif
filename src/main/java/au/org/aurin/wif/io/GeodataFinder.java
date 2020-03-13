package au.org.aurin.wif.io;

import static org.geotools.jdbc.JDBCDataStoreFactory.DATABASE;
import static org.geotools.jdbc.JDBCDataStoreFactory.HOST;
import static org.geotools.jdbc.JDBCDataStoreFactory.SCHEMA;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeSet;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.sql.DataSource;

import org.geotools.data.DataStore;
import org.geotools.data.FileDataStore;
import org.geotools.data.FileDataStoreFinder;
import org.geotools.data.Query;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.data.simple.SimpleFeatureStore;
import org.geotools.feature.DefaultFeatureCollections;
import org.geotools.feature.FeatureCollections;
import org.geotools.filter.text.cql2.CQLException;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.filter.Filter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import au.org.aurin.wif.exception.io.DatabaseFailedException;
import au.org.aurin.wif.exception.validate.WifInvalidInputException;
import au.org.aurin.wif.model.Projection;
import au.org.aurin.wif.svc.WifKeys;
import it.geosolutions.geoserver.rest.GeoServerRESTPublisher;

/**
 * The Class GeodataFinder.
 */
@Component
public class GeodataFinder {

  /** The Constant LOGGER. */
  private static final Logger LOGGER = LoggerFactory
      .getLogger(GeodataFinder.class);

  /** The postgis data store config. */
  @Autowired
  @Qualifier(value = "wifDataStoreConfig")
  private PostgisDataStoreConfig postgisDataStoreConfig;

  /** The data source factory. */
  @Autowired
  private DataSourceFactory dataSourceFactory;

  /** The geodata filterer. */
  @Autowired
  private GeodataFilterer geodataFilterer;

  /** The data source. */
  @Autowired
  private DataSource dataSource;

  /** The wif data source. */
  private GeospatialDataSource wifDataSource;

  @Autowired
  private GeoServerRESTPublisher geoserverPublisher;

  /**
   * Inits the.
   */

  @PostConstruct
  public void init() {
    LOGGER.trace("Initializing version: " + WifKeys.WIF_KEY_VERSION);
    LOGGER.info("using the following server: {} for database/schema: {}",
        postgisDataStoreConfig.getDataStoreParams().get(HOST.key),
        postgisDataStoreConfig.getDataStoreParams().get(DATABASE.key) + "/"
            + postgisDataStoreConfig.getDataStoreParams().get(SCHEMA.key));
  }

  /**
   * Cleanup.
   */

  @PreDestroy
  public void cleanup() {
    if (wifDataSource != null) {
      if (wifDataSource.getDataStore() != null) {
        LOGGER.info(" Attempting to dispose of data store ");
        wifDataSource.getDataStore().dispose();
      }
    }
    LOGGER.trace(" Service succesfully cleared! ");
  }

  /**
   * Gets the json file.
   *
   * @param path
   *          the path
   * @return the json file
   */
  public File getJsonFile(final String path) {

    // 2. Convert JSON to Java object
    final URL urlOut = this.getClass().getClassLoader().getResource(path);
    String uri = urlOut.getFile();
    LOGGER.debug("url: " + uri);
    LOGGER.debug("os " + System.getProperty("os.name"));
    // TODO Find another way that is compatible with Windows, spaces in
    // path
    // InputStream
    // in=this.getClass().getClassLoader().getSystemResourceAsStream("JSONs/Suburbanization.json");
    if (System.getProperty("os.name").contains("Win")) {
      uri = uri.replaceAll("%20", " "); // FIXME not very resilient
      LOGGER.debug("url forwindows: " + uri);
    }
    return new File(uri);
  }

  /**
   * Gets the wif ua zfrom shp file.
   *
   * @param uAZShpURL
   *          the u az shp url
   * @return the wif ua zfrom shp file
   */
  public SimpleFeatureCollection getWifUAZfromShpFile(final URL uAZShpURL) {

    LOGGER.info("uAZShpURL FilePath: {} ", uAZShpURL.getFile());
    SimpleFeatureCollection uazFeatureCollection = FeatureCollections
        .newCollection();
    FileDataStore storeUD;
    try {
      storeUD = openFileDataStore(uAZShpURL.getFile());
      final SimpleFeatureSource featureSourceUD = storeUD.getFeatureSource();
      uazFeatureCollection = featureSourceUD.getFeatures();
      LOGGER.debug("featureCollection size : {} ", uazFeatureCollection.size());
    } catch (final IOException e) {
      LOGGER.error("getWifUAZfromShpFile ", e.getMessage());
    }

    return uazFeatureCollection;
  }

  /**
   * Gets the wif ua zfrom db.
   *
   * @param uazTbl
   *          the uaz tbl
   * @return the wif ua zfrom db
   */
  public SimpleFeatureCollection getWifUAZfromDB(final String uazTbl) {

    LOGGER.info("getWifUAZfromDB for table : {}", uazTbl);
    SimpleFeatureCollection uazFeatureCollection = FeatureCollections
        .newCollection();
    DataStore wifDataStore;

    try {
      wifDataStore = openPostgisDataStore();
      final SimpleFeatureSource featureSourceUD = wifDataStore
          .getFeatureSource(uazTbl);
      uazFeatureCollection = featureSourceUD.getFeatures();
      LOGGER.info("UAZ featureCollection size : {} ",
          uazFeatureCollection.size());
    } catch (final IOException e) {
      LOGGER.error("could not access table {} ", uazTbl);
    }

    return uazFeatureCollection;
  }

  /**
   * Delete wif uaz in db.
   *
   * @param uazTbl
   *          the uaz tbl
   */
  public void deleteWifUAZInDB(final String uazTbl) {
    final String query = "DROP TABLE "
        + postgisDataStoreConfig.getDataStoreParams().get(SCHEMA.key) + "."
        + uazTbl;
    LOGGER.info("deleteWifUAZInDB: query is {} ", query);
    final JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
    jdbcTemplate.execute(query);
  }

  /**
   * Expand ua zcolumns.
   *
   * @param uazTbl
   *          the uaz tbl
   * @param columnList
   *          the column list
   * @return the boolean
   */
  public Boolean expandUAZcolumns(final String uazTbl,
      final List<String> columnList) {

    LOGGER.info("expandUAZcolumns: ");
    LOGGER.info("Adding {} columns ", columnList.size());
    for (final String column : columnList) {
      LOGGER.info("Using column ={}", column);
    }

    final JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
    for (final String factor : columnList) {

      String query = "ALTER TABLE "
          + postgisDataStoreConfig.getDataStoreParams().get(SCHEMA.key) + "."
          + uazTbl + " ADD COLUMN \"" + factor + "\" double precision";
      LOGGER.info("expandUAZcolumns query is {} ", query);
      jdbcTemplate.execute(query);

      // create index on new added columns.
      // query = "CREATE INDEX idx_" + factor + "_" + uazTbl + " ON "
      query = "CREATE INDEX idx_" + uazTbl + "_" + factor + " ON "
          + postgisDataStoreConfig.getDataStoreParams().get(SCHEMA.key) + "."
          + uazTbl + " (" + "\"" + factor + "\"" + ")";
      LOGGER.info("expandUAZcolumns index query is {} ", query);
      jdbcTemplate.execute(query);

    }
    return true;
  }

  public Boolean expandUAZcolumnsALU(final String uazTbl,
      final List<String> columnList) {

    LOGGER.info("expandUAZcolumns: ");
    LOGGER.debug("Adding {} columns ", columnList.size());
    for (final String column : columnList) {
      LOGGER.trace("Using column ={}", column);
    }

    final JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
    for (final String factor : columnList) {

      String query = "ALTER TABLE "
          + postgisDataStoreConfig.getDataStoreParams().get(SCHEMA.key) + "."
          + uazTbl + " ADD COLUMN \"" + factor + "\" character varying(40);";
      LOGGER.debug(" query is {} ", query);
      jdbcTemplate.execute(query);

      // create index on new added columns.
      query = "CREATE INDEX idx_" + factor + "_" + uazTbl + " ON "
          + postgisDataStoreConfig.getDataStoreParams().get(SCHEMA.key) + "."
          + uazTbl + " (" + "\"" + factor + "\"" + ")";
      jdbcTemplate.execute(query);

    }
    return true;
  }

  public Boolean updateUAZcolumnsALU(final String existingLUAttributeName,
      final String uazTbl, final String[] columnList) {

    LOGGER.info("updateUAZcolumnsALU: ");
    LOGGER.debug("Adding {} columns ", columnList.length);
    for (final String column : columnList) {
      LOGGER.trace("Using column ={}", column);
    }
    ;

    String query = "";
    String fields = "";
    final JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
    int i = 1;
    for (final String factor : columnList) {
      // updatating column

      query = "SELECT count(*)  FROM information_schema.columns "
          + " WHERE table_name='" + uazTbl + "'" + " and column_name='"
          + factor + "'";

      LOGGER.info("updateUAZcolumnsALU query is: " + query);

      if (jdbcTemplate.queryForObject(query, Integer.class) == 0) {
        query = "ALTER TABLE "
            + postgisDataStoreConfig.getDataStoreParams().get(SCHEMA.key) + "."
            + uazTbl + " ADD COLUMN \"" + factor + "\" character varying(40);";
        LOGGER.debug(" query is {} ", query);
        jdbcTemplate.execute(query);

        // create index on new added columns.
        query = "CREATE INDEX idx_" + factor + "_" + uazTbl + " ON "
            + postgisDataStoreConfig.getDataStoreParams().get(SCHEMA.key) + "."
            + uazTbl + " (" + "\"" + factor + "\"" + ")";
        jdbcTemplate.execute(query);

        geoserverPublisher.reload();

      }
      if (i == 1) {
        // first column updates with existingLUAttributeName
        query = "update "
            + postgisDataStoreConfig.getDataStoreParams().get(SCHEMA.key) + "."
            + uazTbl + " SET \"" + factor + "\"" + "=concat('"
            + WifKeys.FUTURELU_PREFIX + "'," + "\"" + existingLUAttributeName
            + "\"" + ")";
        LOGGER.debug(" query is {} ", query);
        // jdbcTemplate.execute(query);
        LOGGER.info("updateUAZcolumnsALU query is: " + query);
        jdbcTemplate.batchUpdate(new String[] { query });
      } else {

        fields = fields + "\"" + factor + "\" = '',";
        // query = "update "
        // + postgisDataStoreConfig.getDataStoreParams().get(SCHEMA.key) + "."
        // + uazTbl + " SET \"" + factor + "\"" + "=''";
        // LOGGER.debug(" query is {} ", query);
        // // jdbcTemplate.execute(query);
        //
        // jdbcTemplate.batchUpdate(new String[] { query });
      }

      i = i + 1;

    }
    if (i > 1) {
      fields = fields.substring(0, fields.length() - 1);
      query = "update "
          + postgisDataStoreConfig.getDataStoreParams().get(SCHEMA.key) + "."
          + uazTbl + " SET " + fields;
      LOGGER.info(" query is {} ", query);
      // jdbcTemplate.execute(query);

      jdbcTemplate.batchUpdate(new String[] { query });
    }
    return true;
  }

  public Boolean updateUAZcolumnsALUDemonstration(final String uazTbl,
      final String[] columnList) {

    LOGGER.info("updateUAZcolumnsALUDemonstration: ");
    LOGGER.debug("Adding {} columns ", columnList.length);
    for (final String column : columnList) {
      LOGGER.trace("Using column ={}", column);
    }

    final JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
    for (final String factor : columnList) {
      // updatating column for Demonstration project (since it is double)
      // fix me later
      if (!factor.equals(WifKeys.DEMO_ALLOCATION_0)) {
        final String query = "update "
            + postgisDataStoreConfig.getDataStoreParams().get(SCHEMA.key) + "."
            + uazTbl + " SET \"" + factor + "\"" + "=0.0";
        LOGGER.debug(" query is {} ", query);
        jdbcTemplate.execute(query);
      }

    }
    return true;
  }

  /**
   * Create Spatial Index
   *
   * @param uazTbl
   *          the uaz tbl
   * @param geometryColumnName
   *          geometry column name
   * @return the boolean
   */

  public Boolean createSpatialIndex(final String uazTbl,
      final String geometryColumnName) {

    LOGGER.info(
        "createSpatialIndex: for {} table and geometry column name is {} ",
        uazTbl, geometryColumnName);

    final JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

    final String query = "CREATE INDEX idx_geom ON "
        + postgisDataStoreConfig.getDataStoreParams().get(SCHEMA.key) + "."
        + uazTbl + " USING GIST(" + geometryColumnName + ")";

    LOGGER.debug(" query is {} ", query);
    jdbcTemplate.execute(query);

    return true;
  }

  /**
   * Gets the distinct entries for uaz attribute.
   *
   * @param uazTbl
   *          the uaz tbl
   * @param attribute
   *          the attribute
   * @return the distinct entries for uaz attribute
   */
  public List<String> getDistinctEntriesForUAZAttribute(final String uazTbl,
      final String attribute) {
    final String query = "SELECT DISTINCT \"" + attribute + "\" FROM "
        + postgisDataStoreConfig.getDataStoreParams().get(SCHEMA.key) + "."
        + uazTbl + " ORDER BY \"" + attribute + "\"";
    final JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
    LOGGER.info("getDistinctEntriesForUAZAttribute: query is {} ", query);
    final List<String> entries = jdbcTemplate.query(query,
        new RowMapper<String>() {

      @Override
      public String mapRow(final ResultSet rs, final int arg1)
          throws SQLException {
        return rs.getObject(1).toString();
      }

    });

    LOGGER.info(" returning {} distinct entries ", entries.size());
    return entries;
  }

  public List<String> getDistinctColorsForUAZAttribute(final String uazTbl,
      final String attribute) {

    final String query = "select c.a || '@' || c.b  as color from "
        + "(select \"" + attribute + "\"" + " as a , \"" + "color" + "\""
        + " as b from "
        + postgisDataStoreConfig.getDataStoreParams().get(SCHEMA.key) + "."
        + uazTbl + " group by " + "\"" + attribute + "\"" + "," + "\""
        + "color" + "\"" + ") as c";

    // final String query = "SELECT DISTINCT \"" + attribute + "\" FROM "
    // + postgisDataStoreConfig.getDataStoreParams().get(SCHEMA.key) + "."
    // + uazTbl + " ORDER BY \"" + attribute + "\"";
    final JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
    LOGGER.info("getDistinctColorsForUAZAttribute: query is {} ", query);
    final List<String> entries = jdbcTemplate.query(query,
        new RowMapper<String>() {

      @Override
      public String mapRow(final ResultSet rs, final int arg1)
          throws SQLException {
        return rs.getObject(1).toString();
      }

    });

    LOGGER.info(" returning {} distinct entries ", entries.size());
    return entries;
  }

  public List<String> getDistinctColorsForALUConfig(final String uazTbl,
      final String attribute) {

    final JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

    String query = "SELECT column_name FROM information_schema.columns "
        + " WHERE table_name='" + uazTbl
        + "' and  (lower(column_name)='color' or lower(column_name)='colour')";
    LOGGER.info("getDistinctColorsForALUConfig: query is {} ", query);

    // final String stColumn = jdbcTemplate.queryForObject(query, String.class);

    final List<String> stColumns = jdbcTemplate.query(query,
        new RowMapper<String>() {

      @Override
      public String mapRow(final ResultSet rs, final int arg1)
          throws SQLException {
        return rs.getObject(1).toString();
      }

    });

    if (stColumns.size() == 0) {
      query = "SELECT DISTINCT \"" + attribute + "\"  || '@#000000' FROM "
          + postgisDataStoreConfig.getDataStoreParams().get(SCHEMA.key) + "."
          + uazTbl + " ORDER BY \"" + attribute + "\"  || '@#000000'";

    } else {

      LOGGER.info("color fieldname is {} ", stColumns.get(0));
      query = "select c.a || '@' || c.b  as color from " + "(select \""
          + attribute + "\"" + " as a , \"" + stColumns.get(0) + "\""
          + " as b from "
          + postgisDataStoreConfig.getDataStoreParams().get(SCHEMA.key) + "."
          + uazTbl + " group by " + "\"" + attribute + "\"" + "," + "\""
          + stColumns.get(0) + "\"" + ") as c";
    }

    // final String query = "SELECT DISTINCT \"" + attribute + "\" FROM "
    // + postgisDataStoreConfig.getDataStoreParams().get(SCHEMA.key) + "."
    // + uazTbl + " ORDER BY \"" + attribute + "\"";

    LOGGER.info("getDistinctColorsForALUConfig: query is {} ", query);
    final List<String> entries = jdbcTemplate.query(query,
        new RowMapper<String>() {

      @Override
      public String mapRow(final ResultSet rs, final int arg1)
          throws SQLException {
        return rs.getObject(1).toString();
      }

    });

    LOGGER.info(" returning {} distinct entries ", entries.size());
    return entries;
  }

  /**
   * Gets the sum of distinct entries for uaz attribute.
   *
   * @param uazTbl
   *          the uaz tbl
   * @param attribute
   *          the attribute
   * @return the sum of distinct entries for uaz attribute
   */
  public double getSumOfDistinctEntriesForUAZAttribute(final String uazTbl,
      final String attribute) {
    final JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
    LOGGER.debug(
        "getSumOfDistinctEntriesForUAZAttribute uazTbl: {}, attribute: {}",
        uazTbl, attribute);
    return jdbcTemplate.queryForObject(
        "SELECT SUM(DISTINCT \"" + attribute + "\") FROM "
            + postgisDataStoreConfig.getDataStoreParams().get(SCHEMA.key) + "."
            + uazTbl, Double.class);
  }

  /**
   * Gets the normalised sum of distinct entries uaz attribute.
   *
   * @param uazTbl
   *          the uaz tbl
   * @param columnName1
   *          the column name1
   * @param normaliser1
   *          the normaliser1
   * @param normaliser2
   *          the normaliser2
   * @return the normalised sum of distinct entries uaz attribute
   */
  public double getNormalisedSumOfDistinctEntriesUAZAttribute(
      final String uazTbl, final String columnName1, final String normaliser1,
      final String normaliser2) {
    final JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
    LOGGER
    .debug(
        "getNormalisedSumOfDistinctEntriesUAZAttribute uazTbl: {}, columnName: {}",
        uazTbl, columnName1);
    LOGGER.debug("Normalizer1: {}, 2:{}", normaliser1, normaliser2);
    return jdbcTemplate.queryForObject("SELECT SUM(DISTINCT (\"" + columnName1
        + "\"*(\"" + normaliser1 + "\"/\"" + normaliser2 + "\"))) FROM "
        + postgisDataStoreConfig.getDataStoreParams().get(SCHEMA.key) + "."
        + uazTbl, Double.class);
  }

  /**
   * Gets the area by score ranges.
   *
   * @param uazTbl
   *          the uaz tbl
   * @param scoreLabel
   *          the score label
   * @param areaFeatureFieldName
   *          the area feature field name
   * @param limit1
   *          the limit1
   * @param limit2
   *          the limit2
   * @return the area by score ranges
   */
  public Double getAreaByScoreRanges(final String uazTbl,
      final String scoreLabel, final String areaFeatureFieldName,
      final double limit1, final double limit2) {
    LOGGER.debug("getAreaByScoreRanges limits: {}, {}", limit1, limit2);
    final JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
    // ali rempved DISTINCT-->> "SELECT SUM(DISTINCT (\"" + areaFeatureFieldName
    final String queryTxt = "SELECT SUM((\"" + areaFeatureFieldName
        + "\")) FROM "
        + postgisDataStoreConfig.getDataStoreParams().get(SCHEMA.key) + "."
        + uazTbl + " WHERE \"" + scoreLabel + "\"" + "  BETWEEN " + limit1
        + " AND " + limit2;
    LOGGER.debug("getAreaByScoreRanges: {}", queryTxt);
    Double area = jdbcTemplate.queryForObject(queryTxt, Double.class);
    if (area == null) {
      area = 0.0;
    }
    return area;
  }

  /**
   * Gets the area by lu.
   *
   * @param uazTbl
   *          the uaz tbl
   * @param areaFeatureFieldName
   *          the area feature field name
   * @param landUseFFName
   *          the land use ff name
   * @param landUseValue
   *          the land use value
   * @param allocationFFName
   *          the allocation ff name
   * @param allocationValue
   *          the allocation value
   * @return the area by lu
   */
  public Double getAreaByLU(final String uazTbl,
      final String areaFeatureFieldName, final String landUseFFName,
      final String landUseValue, final String allocationFFName,
      final String allocationValue) {
    LOGGER.debug("getAreaByLU luValues: {}, {}", landUseFFName, landUseValue);
    LOGGER.debug("allocationFFName, allocationValues: {}, {}",
        allocationFFName, allocationValue);
    Double area = 0.0;
    if (allocationValue == null || allocationFFName == null) {
      LOGGER
      .warn(
          "{}, doesn't have allocation value configured {}, not calculating area",
          landUseValue, allocationFFName);
    } else {
      final JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
      final String queryTxt = "SELECT SUM(DISTINCT (\"" + areaFeatureFieldName
          + "\")) FROM "
          + postgisDataStoreConfig.getDataStoreParams().get(SCHEMA.key) + "."
          + uazTbl + " WHERE \"" + landUseFFName + "\"" + "  = '"
          + landUseValue + "' AND " + "\"" + allocationFFName + "\"" + "  = '"
          + allocationValue + "'";
      LOGGER.debug("getAreaByLU: {}", queryTxt);
      area = jdbcTemplate.queryForObject(queryTxt, Double.class);
    }
    return area;
  }

  public Double getAreaByLUNew(final String uazTbl,
      final String areaFeatureFieldName, final String allocationFFName,
      final String allocationValue) {
    LOGGER.debug("allocationFFName, allocationValues: {}, {}",
        allocationFFName, allocationValue);
    Double area = 0.0;
    if (allocationValue == null || allocationFFName == null) {
      LOGGER
      .warn(
          "{}, doesn't have allocation value configured {}, not calculating area",
          allocationFFName);
    } else {
      final JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
      final String queryTxt = "SELECT SUM(\"" + areaFeatureFieldName
          + "\") FROM "
          + postgisDataStoreConfig.getDataStoreParams().get(SCHEMA.key) + "."
          + uazTbl + " WHERE \"" + allocationFFName + "\"" + "  = '"
          + allocationValue + "'";
      LOGGER.debug("getAreaByLU: {}", queryTxt);
      area = jdbcTemplate.queryForObject(queryTxt, Double.class);
    }
    return area;
  }

  /**
   * Gets the wif ua zfrom db.
   *
   * @param uazTbl
   *          the uaz tbl
   * @param queryTxt
   *          the query txt
   * @param attributes
   *          the attributes
   * @return the wif ua zfrom db
   */
  public SimpleFeatureCollection getWifUAZfromDB(final String uazTbl,
      final String queryTxt, final String[] attributes) {

    LOGGER.info("getWifUAZfromDB for table : {}", uazTbl);
    LOGGER.info("for filter: {}", queryTxt);
    SimpleFeatureCollection uazFeatureCollection = FeatureCollections
        .newCollection();
    DataStore wifDataStore;
    try {
      wifDataStore = openPostgisDataStore();
      final SimpleFeatureSource featureSourceUD = wifDataStore
          .getFeatureSource(uazTbl);
      final Filter filter = geodataFilterer.getFilter(queryTxt);
      final Query query = new Query(uazTbl, filter, attributes);
      uazFeatureCollection = featureSourceUD.getFeatures(query);
      LOGGER.info("UAZ featureCollection size : {} ",
          uazFeatureCollection.size());
    } catch (final IOException e) {
      LOGGER.error("could not access table {} ", uazTbl);
    } catch (final CQLException e) {
      LOGGER.error("filter is invalid: {} ", queryTxt);
    }

    return uazFeatureCollection;
  }

  /**
   * Gets the feature collectionfrom db.
   *
   * @param uazTbl
   *          the uaz tbl
   * @param filter
   *          the filter
   * @param attributesToReturn
   *          the attributes to return
   * @return the feature collectionfrom db
   * @throws DatabaseFailedException
   */
  public SimpleFeatureCollection getFeatureCollectionfromDB(
      final String uazTbl, final Filter filter,
      final List<String> attributesToReturn) throws DatabaseFailedException {

    LOGGER.info("getFeatureCollectionfromDB for table : {}", uazTbl);
    LOGGER.debug("for filter: {}", filter.toString());
    // SimpleFeatureCollection uazFeatureCollection = FeatureCollections
    // .newCollection();
    // ali
    SimpleFeatureCollection uazFeatureCollection = DefaultFeatureCollections
        .newCollection();

    DataStore wifDataStore = null;
    try {
      // commented by ali, since it is not reflecting new added columns
      // wifDataStore = openPostgisDataStore();

      GeospatialDataSource wifDataSource1;
      wifDataSource1 = dataSourceFactory
          .createGeospatialDataSource(postgisDataStoreConfig
              .getDataStoreParams());

      wifDataStore = wifDataSource1.getDataStore();

      final SimpleFeatureSource featureSourceUD = wifDataStore
          .getFeatureSource(uazTbl);

      if (attributesToReturn == null) {
        final Query query = new Query(uazTbl, filter);
        query.setPropertyNames(attributesToReturn);

        uazFeatureCollection = featureSourceUD.getFeatures(query);
      } else {
        uazFeatureCollection = featureSourceUD.getFeatures(filter);
      }
      LOGGER.debug("UAZ featureCollection size : {} ",
          uazFeatureCollection.size());
    } catch (final IOException e) {
      final String msg = "could not access table" + uazTbl;
      LOGGER.error(msg, e.getMessage());
      throw new DatabaseFailedException(msg, e);
    } finally {
      // wifDataStore.dispose();
    }

    return uazFeatureCollection;
  }

  // TODO consider deleting this method as we might not have any use for it
  // anymore. If this gets deleted, we'll update the Tests
  /**
   * Gets the feature collectionfrom db.
   *
   * @param uazTbl
   *          the uaz tbl
   * @param filter
   *          the filter
   * @return the feature collectionfrom db
   * @throws DatabaseFailedException
   */
  public SimpleFeatureCollection getFeatureCollectionfromDB(
      final String uazTbl, final Filter filter) throws DatabaseFailedException {

    return getFeatureCollectionfromDB(uazTbl, filter, null);
  }

  /**
   * Update uaz area.
   *
   * @param uazTbl
   *          the uaz tbl
   */
  public void updateUAZArea(final String uazTbl) {

    LOGGER.info("updateUAZArea for table : {}", uazTbl);

    DataStore wifDataStore;
    try {
      wifDataStore = openPostgisDataStore();
      final SimpleFeatureSource featureSourceUD = wifDataStore
          .getFeatureSource(uazTbl);
      final SimpleFeatureType schema = featureSourceUD.getSchema();
      final String geomColumnName = schema.getGeometryDescriptor()
          .getLocalName();

      final JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
      final String queryTxt = "UPDATE "
          + postgisDataStoreConfig.getDataStoreParams().get(SCHEMA.key) + "."
          + uazTbl + " SET \"" + WifKeys.DEFAULT_AREA_COLUMN_NAME + "\" = "
          + " (ST_Area( " + geomColumnName + " )) ";
      LOGGER.debug("updateUAZArea: {}", queryTxt);
      jdbcTemplate.execute(queryTxt);
    } catch (final IOException e) {
      LOGGER.error("could not access table {} ", uazTbl);
    }
  }

  /**
   * Gets the uAZ attributes.
   *
   * @param uazTbl
   *          the uaz tbl
   * @return the uAZ attributes
   */
  public List<String> getUAZAttributes(final String uazTbl) {

    LOGGER.info("getUAZAttributes for table : {}", uazTbl);

    final List<String> attrs = new ArrayList<String>();
    DataStore wifDataStore;
    try {
      wifDataStore = openPostgisDataStore();
      final SimpleFeatureSource featureSourceUD = wifDataStore
          .getFeatureSource(uazTbl);
      final SimpleFeatureType schema = featureSourceUD.getSchema();

      // let's also take note of the geometry column so that we don't
      // add it to the returned list
      final String geomColumnName = schema.getGeometryDescriptor()
          .getLocalName();

      for (final AttributeDescriptor attr : schema.getAttributeDescriptors()) {
        if (!geomColumnName.equals(attr.getLocalName())) {
          attrs.add(attr.getLocalName());
        }
      }
    } catch (final IOException e) {
      LOGGER.error("could not access table {} ", uazTbl);
    }
    return attrs;
  }

  /**
   * Gets the feature sourcefrom db.
   *
   * @param uazTbl
   *          the uaz tbl
   * @return the feature sourcefrom db
   * @throws WifInvalidInputException
   *           the wif invalid input exception
   */
  public SimpleFeatureSource getFeatureSourcefromDB(final String uazTbl)
      throws WifInvalidInputException {

    LOGGER.info("getFeatureSourcefromDB for table : {}", uazTbl);
    DataStore wifDataStore;
    SimpleFeatureSource featureSourceUD;
    try {
      wifDataStore = openPostgisDataStore();
      featureSourceUD = wifDataStore.getFeatureSource(uazTbl);

      LOGGER.info("UAZ featureSource Name: {} ", featureSourceUD.getName());
      return featureSourceUD;
    }

    catch (final IOException e) {
      LOGGER.error("could not access table {} ", uazTbl);
      throw new WifInvalidInputException("could not access table" + uazTbl);
    }
  }

  /**
   * Gets the feature storefrom db.
   *
   * @param uazTbl
   *          the uaz tbl
   * @return the feature storefrom db
   * @throws WifInvalidInputException
   *           the wif invalid input exception
   */
  public SimpleFeatureStore getFeatureStorefromDB(final String uazTbl)
      throws WifInvalidInputException {

    LOGGER.info("getFeatureStorefromDB for table : {}", uazTbl);

    SimpleFeatureSource featureSourceUD;

    featureSourceUD = getFeatureSourcefromDB(uazTbl);

    if (featureSourceUD instanceof SimpleFeatureStore) {

      LOGGER.info("UAZ featureSource Name: {} ", featureSourceUD.getName());
      return (SimpleFeatureStore) featureSourceUD;
    } else {
      throw new WifInvalidInputException(
          "table doesn't have write priviledges " + uazTbl);
    }
  }

  /**
   * Gets the wif ua zfrom db.
   *
   * @param wifDataStore
   *          the wif data store
   * @param uazTbl
   *          the uaz tbl
   * @param queryTxt
   *          the query txt
   * @param attributes
   *          the attributes
   * @return the wif ua zfrom db
   */
  public SimpleFeatureCollection getWifUAZfromDB(final DataStore wifDataStore,
      final String uazTbl, final String queryTxt, final String[] attributes) {

    LOGGER.info("getWifUAZfromDB for table : {}", uazTbl);
    LOGGER.info("for filter: {}", queryTxt);
    SimpleFeatureCollection uazFeatureCollection = FeatureCollections
        .newCollection();
    try {
      final SimpleFeatureSource featureSourceUD = wifDataStore
          .getFeatureSource(uazTbl);
      final Filter filter = geodataFilterer.getFilter(queryTxt);
      final Query query = new Query(uazTbl, filter, attributes);
      uazFeatureCollection = featureSourceUD.getFeatures(query);
      LOGGER.info("UAZ featureCollection size : {} ",
          uazFeatureCollection.size());
    } catch (final IOException e) {
      LOGGER.error("could not access table {} ", uazTbl);
    } catch (final CQLException e) {
      LOGGER.error("filter is invalid: {} ", queryTxt);
    }

    return uazFeatureCollection;
  }

  /**
   * Open file data store.
   *
   * @param namePath
   *          the name path
   * @return the file data store
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   */
  private FileDataStore openFileDataStore(final String namePath)
      throws IOException {
    final File file = new File(namePath);
    return FileDataStoreFinder.getDataStore(file);
  }

  /**
   * Open postgis data store.
   *
   * @return the data store
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   */
  public DataStore openPostgisDataStore() throws IOException {
    if (wifDataSource == null) {

      LOGGER
      .info(
          "openPostgisDataStore,,accessing the following server: {} for database/schema: {}",
          postgisDataStoreConfig.getDataStoreParams().get(HOST.key),
          postgisDataStoreConfig.getDataStoreParams().get(DATABASE.key)
          + "/"
          + postgisDataStoreConfig.getDataStoreParams().get(SCHEMA.key));
      wifDataSource = dataSourceFactory
          .createGeospatialDataSource(postgisDataStoreConfig
              .getDataStoreParams());
    }

    return wifDataSource.getDataStore();
  }

  /**
   * Gets the area by score ranges.
   *
   * @param uazTbl
   *          the uaz tbl
   * @param scoreLabel
   *          the score label
   * @param areaFeatureFieldName
   *          the area feature field name
   * @param limit1
   *          the limit1
   * @param limit2
   *          the limit2
   * @return the area by score ranges
   */
  public Double getSumofField(final String uazTbl, final String fieldName) {
    LOGGER.debug("getSumofField : {}, {}", uazTbl, fieldName);
    final JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
    final String queryTxt = "SELECT SUM((\"" + fieldName + "\")) FROM "
        + postgisDataStoreConfig.getDataStoreParams().get(SCHEMA.key) + "."
        + uazTbl;
    LOGGER.debug("getSumofField: {}", queryTxt);
    Double area = jdbcTemplate.queryForObject(queryTxt, Double.class);
    if (area == null) {
      area = 0.0;
    }
    return area;
  }

  public Boolean updateALlocationColumnNew(final String query) {

    final JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
    LOGGER.info(" updateALlocationColumnNew query is {} ", query);
    jdbcTemplate.batchUpdate(new String[] { query });

    return true;
  }

  public Double getSumofALU(final String sql) {
    LOGGER.debug("getSumofALU : {}", sql);
    final JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
    Double area = jdbcTemplate.queryForObject(sql, Double.class);
    if (area == null) {
      area = 0.0;
    }
    return area;
  }

  public String getPrimaryKeyName(final String uazTbl) {

    final String sql = "select  kc.column_name "
        + " from "
        + " information_schema.table_constraints tc, "
        + " information_schema.key_column_usage kc "
        + " where "
        + " tc.table_name = '"
        + uazTbl
        + "' and "
        + " tc.constraint_type = 'PRIMARY KEY' "
        + " and kc.table_name = tc.table_name and kc.table_schema = tc.table_schema "
        + " and kc.constraint_name = tc.constraint_name ";

    final JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
    LOGGER.info("getPrimaryKeyName for table: {}", uazTbl);
    return jdbcTemplate.queryForObject(sql, String.class);
  }

  /**
   * Gets thescore ranges.
   *
   * @param uazTbl
   *          the uaz tbl
   * @param scoreLabel
   *          the score label
   * @return the min,max score values
   */
  public Double getScoreRanges(final String cmp, final String uazTbl,
      final String scoreLabel) {
    LOGGER.info("getScoreRanges for :{}", scoreLabel);
    final JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
    final String queryTxt = "SELECT " + cmp + "(" + scoreLabel + ") FROM "
        + postgisDataStoreConfig.getDataStoreParams().get(SCHEMA.key) + "."
        + uazTbl + " WHERE " + scoreLabel + " >0 ";

    LOGGER.info("getScoreRanges query: {}", queryTxt);
    Double area = jdbcTemplate.queryForObject(queryTxt, Double.class);
    if (area == null) {
      area = 0.0;
    }
    return area;
  }

  public Boolean TransformSuitabilityScore(final String uazTbl,
      final String scoreLabel, final Double minv, final Double maxv) {

    LOGGER.info("TransformSuitabilityScore: for scoreLabel:" + scoreLabel + " ,min:"
        + Double.toString(minv) + ",max:" + Double.toString(maxv));
    final JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
    if (maxv > 0 && minv >0){
      if (maxv - minv>0)
      {
        final String query = "update "
            + postgisDataStoreConfig.getDataStoreParams().get(SCHEMA.key) + "."
            + uazTbl + " SET " + scoreLabel + "= 1 + ((99 * (scoreLabel -" + Double.toString(minv) + "))/(" + Double.toString(maxv) + "-" + Double.toString(minv) +"))" ;

        LOGGER.info("TransformSuitabilityScore query is: " + query);
        jdbcTemplate.batchUpdate(new String[] { query });
      }
      else if (maxv - minv==0)
      {
        final String query = "update "
            + postgisDataStoreConfig.getDataStoreParams().get(SCHEMA.key) + "."
            + uazTbl + " SET " + scoreLabel + "=100" ;

        LOGGER.info("TransformSuitabilityScore query is: " + query);
        jdbcTemplate.batchUpdate(new String[] { query });
      }
    }


    return true;
  }


  public Double getAreaByLUNew2(final String uazTbl,
      final String areaFeatureFieldName, final String allocationFFName,
      final String allocationValue, final TreeSet<Projection> projections, final Projection projection) {
    LOGGER.debug("allocationFFName, allocationValues: {}, {}",
        allocationFFName, allocationValue);
    Double area = 0.0;
    Double d0 = 0.0;
    Double d1 = 0.0;
    Double d2 = 0.0;
    String queryTxt = "";
    if (allocationValue == null || allocationFFName == null) {
      LOGGER
      .warn(
          "{}, doesn't have allocation value configured {}, not calculating area",
          allocationFFName);
    } else {
      final JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
      //       queryTxt = "SELECT SUM(\"" + areaFeatureFieldName
      //          + "\") FROM "
      //          + postgisDataStoreConfig.getDataStoreParams().get(SCHEMA.key) + "."
      //          + uazTbl + " WHERE \"" + allocationFFName + "\"" + "  = '"
      //          + allocationValue + "'";
      //      LOGGER.info("getAreaByLUNew2: {}", queryTxt);
      //      d1 = jdbcTemplate.queryForObject(queryTxt, Double.class);

      //////////////////////////////////////////////////////////

      /////new for finding the area which converted to the other landuses.

      final ArrayList<String> columnList = new ArrayList<String>();
      for (final Projection proj : projections) {
        if (proj.getYear() <= projection.getYear()) {
          columnList.add(proj.getLabel());
        }
      }

      final String[] columnListAll = new String[columnList.size()];

      for (int ind = 0; ind < columnList.size(); ind++)
      {
        columnListAll[ind] = "ALU_" + columnList.get(ind);

      }

      Arrays.sort(columnListAll);

      final String firstYear = columnListAll[0];

      queryTxt = "SELECT SUM(\"" + areaFeatureFieldName
          + "\") FROM "
          + postgisDataStoreConfig.getDataStoreParams().get(SCHEMA.key) + "."
          + uazTbl + " WHERE \"" + firstYear + "\"" + "  = '"
          + allocationValue + "'";
      LOGGER.info("getAreaByLUNew2: {}", queryTxt);
      d0 = jdbcTemplate.queryForObject(queryTxt, Double.class); //first year

      if (!firstYear.equals(allocationFFName))
      {
        //        String wherest=" Where \"" + firstYear + "\" ='" + allocationValue + "' AND (";
        //        wherest = wherest + "(\"" + allocationFFName + "\" <>'" +  allocationValue + "' AND \"" + allocationFFName + "\" <>'')";
        //        wherest= wherest + ")";
        //
        //        queryTxt = "select Sum(\"" + areaFeatureFieldName + "\") from "
        //            + postgisDataStoreConfig.getDataStoreParams().get(SCHEMA.key) + "."
        //            + uazTbl  + wherest;
        //
        //        LOGGER.info("getAreaByLUNew2 D2 SQL={}", queryTxt);
        //        d2 = jdbcTemplate.queryForObject(queryTxt, Double.class);




        //////////////////////////////////////////////
        queryTxt = "SELECT SUM(\"" + areaFeatureFieldName
            + "\") FROM "
            + postgisDataStoreConfig.getDataStoreParams().get(SCHEMA.key) + "."
            + uazTbl + " WHERE ";

        String wherest="";
        for (int ind = 1; ind <= columnListAll.length -1; ind++)
        {
          final String ff =columnListAll[ind];
          if (ind ==1)
          {
            wherest = wherest + " (\"" + ff + "\" ='" +  allocationValue + "')";
          }
          else
          {
            wherest = wherest + " OR (\"" + ff + "\" ='" +  allocationValue + "')";
          }

        }
        queryTxt = queryTxt + wherest;

        LOGGER.info("getAreaByLUNew2: {}", queryTxt);
        d1 = jdbcTemplate.queryForObject(queryTxt, Double.class);
        ////////////////////////////////////////////////////

        queryTxt = "SELECT SUM(\"" + areaFeatureFieldName
            + "\") FROM "
            + postgisDataStoreConfig.getDataStoreParams().get(SCHEMA.key) + "."
            + uazTbl + " WHERE ";

        wherest="(\"" + firstYear + "\" ='" + allocationValue + "') AND (";

        for (int ind = 1; ind <= columnListAll.length -1; ind++)
        {
          final String ff =columnListAll[ind];
          if (ind ==1)
          {
            wherest = wherest + "(\"" + ff + "\" <>'" +  allocationValue + "' AND \"" + ff + "\" <>'')";
          }
          else
          {
            wherest = wherest + " OR (\"" + ff + "\" <>'" +  allocationValue + "' AND \"" + ff + "\" <>'')";
          }

        }
        queryTxt = queryTxt + wherest + ")";

        LOGGER.info("getAreaByLUNew2 d2: {}", queryTxt);
        d2 = jdbcTemplate.queryForObject(queryTxt, Double.class);

        if (d1 != null)
        {
          if (d2 != null)
          {
            area = d1- d2;
          }
          else
          {
            area = d1;
          }
        }
        else
        {
          if (d2 != null)
          {
            area = 0- d2;
          }
        }

        if (d0 != null)
        {
          area = area  + d0;
        }

      }
      else //if (!firstYear.equals(allocationFFName))
      {
        if (d0 != null)
        {
          area = area  + d0;
        }
      }




    }
    return area;
  }


  /**
   * Update uaz area.
   *
   * @param uazTbl
   *          the uaz tbl
   */
  public void CopyDemoTable(final String newTbl, final String uazTbl) {

    LOGGER.info("CopyDemoTable for table : {}", uazTbl);

    try {
      final JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

      String queryTxt = "SELECT count(*)  FROM information_schema.tables "
          + " WHERE table_name='" + newTbl + "'" + " and table_schema='"
          + postgisDataStoreConfig.getDataStoreParams().get(SCHEMA.key) + "'";

      LOGGER.info("updateUAZcolumnsALU query is: " + queryTxt);

      Boolean lsw =false;
      if (jdbcTemplate.queryForObject( queryTxt, Integer.class) == 1) {
        lsw = true;
        queryTxt = "DROP TABLE "
            + postgisDataStoreConfig.getDataStoreParams().get(SCHEMA.key) + "."
            + newTbl;
        LOGGER.info("CopyDemoTable querydelete: {}", queryTxt);
        jdbcTemplate.execute(queryTxt);
        geoserverPublisher.reload();
      }
      //      final CREATE TABLE wifdemo.perth_peel_scenario3 (LIKE wifdemo.wif_ac8702c64b7c1f603bcd2671f3039639 INCLUDING INDEXES);
      //      final insert into  wifdemo.perth_peel_scenario3 select *  from wifdemo.wif_ac8702c64b7c1f603bcd2671f3039639;

      queryTxt = "CREATE TABLE "
          + postgisDataStoreConfig.getDataStoreParams().get(SCHEMA.key) + "."
          + newTbl + " (LIKE "+ postgisDataStoreConfig.getDataStoreParams().get(SCHEMA.key) + "." + uazTbl
          + " INCLUDING INDEXES) ";
      LOGGER.info("CopyDemoTable query1: {}", queryTxt);
      jdbcTemplate.execute(queryTxt);

      queryTxt = "insert into "
          + postgisDataStoreConfig.getDataStoreParams().get(SCHEMA.key) + "."
          + newTbl + " SELECT * FROM "+ postgisDataStoreConfig.getDataStoreParams().get(SCHEMA.key) + "." + uazTbl;
      LOGGER.info("CopyDemoTable query2: {}", queryTxt);
      jdbcTemplate.execute(queryTxt);


    } catch (final Exception e) {
      LOGGER.error("could not access table {} ", uazTbl);
    }
  }
}




