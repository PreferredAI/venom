package ai.preferred.venom.examples.propertyListingCrawler.master;

import ai.preferred.venom.Crawler;
import ai.preferred.venom.Session;
import ai.preferred.venom.SleepScheduler;
import ai.preferred.venom.examples.propertyListingCrawler.EntityCSVStorage;
import ai.preferred.venom.examples.propertyListingCrawler.entity.Property;
import ai.preferred.venom.fetcher.AsyncFetcher;
import ai.preferred.venom.fetcher.Fetcher;
import ai.preferred.venom.request.VRequest;
import ai.preferred.venom.validator.EmptyContentValidator;
import ai.preferred.venom.validator.StatusOkValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/*
THE FOLLOWING EXAMPLE MODIFIED FROM https://github
.com/PreferredAI/venom-examples/blob/master/src/main/java/ai/preferred
/crawler/iproperty/master
 */
public class ListingCrawler {

  // Create session keys for CSV printer to print from handler
  static final Session.Key<EntityCSVStorage<Property>> STORAGE_KEY = new Session.Key<>();

  // You can use this to log to console
  private static final Logger LOGGER = LoggerFactory.getLogger(ListingCrawler.class);

  public static void main(String[] args) {

    // Get file to save to
    final String filename = "./src/test/java/ai/preferred/venom/examples/propertyListingCrawler/iproperty.csv";

    // Start CSV printer
    try (EntityCSVStorage<Property> storage = new EntityCSVStorage<>(filename)) {

      // Let's init the session, this allows us to retrieve the array list in the handler
      final Session session = Session.builder()
          .put(STORAGE_KEY, storage)
          .build();

      // Start crawler
      try (Crawler crawler = createCrawler(createFetcher(), session).start()) {
        LOGGER.info("starting crawler...");

        final String startUrl = "https://www.iproperty.com.sg/rent/list/";
        crawler.getScheduler().add(new VRequest(startUrl), new ListingHandler());
      } catch (Exception e) {
        LOGGER.error("Could not run crawler: ", e);
      }

    } catch (IOException e) {
      LOGGER.error("unable to open file: {}, {}", filename, e);
    }
  }

  private static Fetcher createFetcher() {
    return AsyncFetcher.builder()
        .setValidator(
            new EmptyContentValidator(),
            new StatusOkValidator(),
            new ListingValidator())
        .build();
  }

  private static Crawler createCrawler(Fetcher fetcher, Session session) {
    return Crawler.builder()
        .setFetcher(fetcher)
        .setSession(session)
        // Just to be polite
        .setSleepScheduler(new SleepScheduler(1500, 3000))
        .build();
  }

}
