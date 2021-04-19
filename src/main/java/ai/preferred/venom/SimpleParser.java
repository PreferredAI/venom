package ai.preferred.venom;

import ai.preferred.venom.response.VResponse;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.List;
import java.util.stream.Collectors;

public class SimpleParser extends Parser {
    VResponse response;
    Document document;
    List<String> result;

    @Override
    void tokenize() {
        document = response.getJsoup();
    }

    @Override
    void extract() {
        // Select all HTML elements on the web page
        final Elements elements = document.select("*");

        // Return parsed elements
        result = elements.stream().map(Element::text).collect(Collectors.toList());
    }

}
