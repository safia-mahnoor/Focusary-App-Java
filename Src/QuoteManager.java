import java.util.ArrayList;
import java.util.Random;

public class QuoteManager extends ApplicationManager {
    private ArrayList<Quote> quotes;
    private Random rand;

    public QuoteManager() {
        quotes=new ArrayList<>();
        quotes.add(new Quote("The best way to get started is to quit talking and begin doing."));
        quotes.add(new Quote("Push yourself, because no one else is going to do it for you."));
        quotes.add(new Quote("Dream it. Wish it. Do it."));
        quotes.add(new Quote("You don’t have to be great to start, but you have to start to be great."));
        quotes.add(new Quote("The future depends on what you do today."));
        quotes.add(new Quote("The only way to do great work is to love what you do."));
        quotes.add(new Quote("Success is not final, failure is not fatal: it is the courage to continue that counts."));
        quotes.add(new Quote("Believe you can and you're halfway there."));
        quotes.add(new Quote("The mind is everything. What you think you become."));

        rand = new Random();
    }

    @Override
    public void initialize() {
        System.out.println("QuoteManager initialized.");
    }

    @Override
    public void dispose() {
        System.out.println("QuoteManager disposed.");
    }

    public String getRandomQuote() {
        if (quotes.isEmpty()) {
            return "No quotes available.";
        }
        int totalQuotes = quotes.size();
        int randomIndex = rand.nextInt(totalQuotes);
        Quote selectedQuote = quotes.get(randomIndex); //returns object at specified index of list
        String quoteText = selectedQuote.getText();
        return quoteText;
    }
    public void displayAllQuotes() {
        for (Quote quote : quotes) {
            System.out.println(quote.getText());
        }
    }
}