package au.org.aurin.wif.io;

import java.awt.Color;

import javax.xml.transform.TransformerException;

import org.geotools.factory.CommonFactoryFinder;
import org.geotools.styling.FeatureTypeConstraint;
import org.geotools.styling.FeatureTypeStyle;
import org.geotools.styling.Fill;
import org.geotools.styling.PolygonSymbolizer;
import org.geotools.styling.Rule;
import org.geotools.styling.SLDTransformer;
import org.geotools.styling.Stroke;
import org.geotools.styling.Style;
import org.geotools.styling.StyleBuilder;
import org.geotools.styling.StyleFactory;
import org.geotools.styling.StyledLayerDescriptor;
import org.geotools.styling.UserLayer;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 
 * @author <a href="mailto:marcosnr@unimelb.edu.au"> Marcos Nino-Ruiz
 *         marcosnr@unimelb.edu.au</a> - 2012
 */
@Component
public class GeodataStyler {

  /**
   * logger.
   */
  private static final Logger LOGGER = LoggerFactory
      .getLogger(GeodataStyler.class);

  public StyledLayerDescriptor getSuitabilityStyle(String columnName) {

    LOGGER.debug("creating a style for suitability column: for", columnName);

    StyleFactory sf = CommonFactoryFinder.getStyleFactory();
    FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2();

    UserLayer layer = sf.createUserLayer();
    layer.setName("layer");

    StyledLayerDescriptor sld = sf.createStyledLayerDescriptor();
    sld.setName("sld");
    sld.setTitle("Example");
    sld.setAbstract("Example Style Layer Descriptor");

    // define constraint limited what features the sld applies to
    FeatureTypeConstraint constraint = sf.createFeatureTypeConstraint(
        "Feature", Filter.INCLUDE, null);

    layer.layerFeatureConstraints().add(constraint);

    //
    // create a "user defined" style
    Style style = sf.createStyle();
    style.setName("style");
    style.getDescription().setTitle("User Style");
    style.getDescription().setAbstract("Definition of Style");

    // define feature type styles used to actually define how features are
    // rendered
    FeatureTypeStyle featureTypeStyle = sf.createFeatureTypeStyle();

    // RULE 1
    // first rule to draw cities
    Rule rule1 = sf.createRule();
    rule1.setName("rule1");
    rule1.getDescription().setTitle("City");
    rule1.getDescription().setAbstract("Rule for drawing cities");
    rule1.setFilter(ff.less(ff.property("POPULATION"), ff.literal(50000)));

    //
    // create the graphical mark used to represent a city
    Stroke stroke = sf.stroke(ff.literal("#000000"), null, null, null, null,
        null, null);
    Fill fill = sf.fill(null, ff.literal(Color.BLUE), ff.literal(1.0));
    
StyleBuilder styleBuilder = new StyleBuilder();

PolygonSymbolizer polygonSymbolizer = styleBuilder.createPolygonSymbolizer(Color.BLUE);
polygonSymbolizer.getFill().setOpacity(ff.literal(0.5)); // 50% blue

polygonSymbolizer.setStroke(styleBuilder.createStroke(Color.BLACK, 2.0));

// will create a default feature type style and rule etc...
//Style style = styleBuilder.createStyle(polygonSymbolizer);
rule1.symbolizers().add(polygonSymbolizer);

    featureTypeStyle.rules().add(rule1);
    
style.featureTypeStyles().add(featureTypeStyle);
    
    layer.userStyles().add(style);
    
    sld.layers().add(layer);
    
    // // create the parser with the sld configuration
    // org.geotools.xml.Configuration configuration = new
    // org.geotools.sld.SLDConfiguration();
    // Parser parser = new org.geotools.xml.Parser(configuration);

    // // the xml instance document above
    // InputStream xml = new FileInputStream("markTest.sld");

    // // parse
    // StyledLayerDescriptor sld = (StyledLayerDescriptor) parser.parse(xml);

    // DuplicatingStyleVisitor xerox = new DuplicatingStyleVisitor();
    // style.accepts( xerox );
    // Style copy = (Style) xerox.getCopy();
    return sld;
  }

  public String getSuitabilityStyleXML(String columnName)
      throws TransformerException {
    SLDTransformer transformer = new SLDTransformer();
    StyledLayerDescriptor suitabilityStyle = getSuitabilityStyle(columnName);
    return transformer.transform(suitabilityStyle);
  }
}
