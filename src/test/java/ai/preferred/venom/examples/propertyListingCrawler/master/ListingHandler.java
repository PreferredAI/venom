package ai.preferred.venom.examples.propertyListingCrawler.master;

import ai.preferred.venom.examples.propertyListingCrawler.EntityCSVStorage;
import ai.preferred.venom.examples.propertyListingCrawler.entity.Property;
import ai.preferred.venom.Handler;
import ai.preferred.venom.job.Priority;
import ai.preferred.venom.job.PriorityJobAttribute;
import ai.preferred.venom.request.VRequest;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIBuilder;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

/*
THE FOLLOWING EXAMPLE MODIFIED FROM https://github
.com/PreferredAI/venom-examples/blob/master/src/main/java/ai/preferred
/crawler/iproperty/master
 */

public class ListingHandler extends Handler {

  private static final Logger LOGGER = LoggerFactory.getLogger(ListingHandler.class);
  Document document;
  EntityCSVStorage<Property> storage;
  List<Property> propertyList;

  @Override
  public void useStorageHook() {
    // Get the CSV printer we created
    storage = getSession().get(ListingCrawler.STORAGE_KEY);
  }

  @Override
  public void tokenize() {
    LOGGER.info("processing: {}", getRequest().getUrl());
    document = getResponse().getJsoup();
  }

  @Override
  public void parse() {
    propertyList = ListingParser.parseListing(document);

    if (propertyList.isEmpty()) {
      LOGGER.info("there is no results on this page, stopping: {}", getRequest().getUrl());
      return;
    }

  }

  @Override
  public void extract() {
    // Use this wrapper for every IO task, this maintains CPU utilisation to speed up crawling
    getWorker().executeBlockingIO(() -> {
      for (final Property p : propertyList) {
        LOGGER.info("storing property: {} [{}]", p.getTitle(), p.getUrl());
        try {
          storage.append(p);
        } catch (IOException e) {
          LOGGER.error("Unable to store listing.", e);
        }
      }
    });
  }

  @Override
  public void continueCrawlHook() {
    // Crawl another page if there's a next page
    final String url = getRequest().getUrl();
    try {
      final URIBuilder builder = new URIBuilder(url);
      int currentPage = 1;
      for (final NameValuePair param : builder.getQueryParams()) {
        if ("page".equals(param.getName())) {
          currentPage = Integer.parseInt(param.getValue());
        }
      }
      builder.setParameter("page", String.valueOf(currentPage + 1));
      final String nextPageUrl = builder.toString();
      // Schedule the next page
      getScheduler().add(new VRequest(nextPageUrl), this,
              new PriorityJobAttribute(Priority.HIGHEST));
    } catch (URISyntaxException | NumberFormatException e) {
      LOGGER.error("unable to parse url: ", e);
    }
  }
}
