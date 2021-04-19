package ai.preferred.venom.examples.propertyListingCrawler.master;

import ai.preferred.venom.examples.propertyListingCrawler.entity.Property;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/*
THE FOLLOWING EXAMPLE MODIFIED FROM https://github
.com/PreferredAI/venom-examples/blob/master/src/main/java/ai/preferred
/crawler/iproperty/master
 */
public class ListingParser {

  private static final Logger LOGGER = LoggerFactory.getLogger(ListingParser.class);

  private ListingParser() {
    throw new UnsupportedOperationException();
  }

  static List<Property> parseListing(Document document) {
    final Elements properties = document.select("ul.listing-list > li[class~=rent]");
    final ArrayList<Property> result = new ArrayList<>(properties.size());
    for (final Element p : properties) {
      result.add(parseProperty(p));
    }
    return result;
  }

  private static String textOrNull(Element element) {
    return null == element ? null : element.text();
  }

  private static Integer intOrNull(Element element) {
    if (element == null) {
      return null;
    }
    try {
      return Integer.parseInt(element.text());
    } catch (NumberFormatException e) {
      LOGGER.error("could not parse integer", e);
      return null;
    }
  }

  private static Property parseProperty(Element e) {
    final Property property = new Property();

    property.setPrice(textOrNull(e.select("li[class~=listing-primary-price-item]").first()));
    property.setArea(textOrNull(e.select("li.attributes-price-per-unit-size-item > a").first()));
    property.setPsf(textOrNull(e.select("p.secondary-price").first()));

    for (final Element facility : e.select("li.attributes-facilities-item-wrapper")) {
      if (facility.hasClass("bedroom-facility")) {
        property.setNumBeds(intOrNull(facility));
      } else if (facility.hasClass("bathroom-facility")) {
        property.setNumBaths(intOrNull(facility));
      } else if (facility.hasClass("carPark-facility")) {
        property.setCarpark(textOrNull(facility));
      } else {
        LOGGER.info("unrecognized facility: {}", facility.text());
      }
    }

    if (e.select("h3 > a[href~=/rent]").isEmpty()) {
      property.setUrl(e.selectFirst("h3 > p > a[href]").attr("abs:href"));
      property.setAddress(e.select("div.sc-iclvYL > div > a").text());
      property.setType(e.select("div.sc-OJyzl > div > a").text());
      property.setTitle(e.select("h3 > p > a[href]").text());
    } else {
      property.setUrl(e.select("p.row-one-left-col-listing-location > a").attr("abs:href"));
      property.setAddress(e.select("p.row-one-left-col-listing-location").text());
      property.setType(e.select("p.property-type-content").text());
      property.setTitle(e.select("h3 > a[href~=/rent]").text());
    }

    return property;
  }

}
