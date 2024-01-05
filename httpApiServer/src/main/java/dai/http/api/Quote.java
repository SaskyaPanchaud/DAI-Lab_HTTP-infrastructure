package dai.http.api;

public class Quote {
    public String quote = "";
    public String author = "";

    public Quote() {
    }

    public Quote(String quote, String author) {
        this.quote = quote;
        this.author = author;
    }

    public String getQuote() {
        return quote;
    }

    public String getAuthor() {
        return author;
    }
}