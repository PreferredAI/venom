package ai.preferred.venom;

public abstract class Parser {
    abstract void tokenize();
    abstract void extract();

    public final void parse() {

        // Tokenize the content of the webpage
        tokenize();

        // Extract content of webpage
        extract();

    }
}
