import java.io.BufferedReader;
import java.io.FileReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

public class CryptoTaxCalculator {
    public static void main(String[] args) {
        List<Transaction> transactions = new ArrayList<>();
        try {
            BufferedReader reader = new BufferedReader(new FileReader("crypto_tax.txt"));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] fields = line.split(" ");
                String type = fields[0];
                String coin = fields[1];
                BigDecimal price = new BigDecimal(fields[2]);
                BigDecimal quantity = new BigDecimal(fields[3]);
                Transaction transaction = new Transaction(type, coin, price, quantity);
                transactions.add(transaction);
            }
            reader.close();
        } catch (Exception e) {
            System.err.format("Exception occurred trying to read '%s'.", "crypto_tax.txt");
            e.printStackTrace();
            System.exit(1);
        }

        BigDecimal totalProfit = calculateTotalProfit(transactions);
        System.out.println(totalProfit.setScale(2, RoundingMode.HALF_UP));
    }

    private static BigDecimal calculateTotalProfit(List<Transaction> transactions) {
        BigDecimal totalProfit = BigDecimal.ZERO;
        for (Transaction sellTransaction : transactions) {
            if (sellTransaction.type.equals("S")) {
                BigDecimal sellQuantity = sellTransaction.quantity;
                BigDecimal remainingSellQuantity = sellQuantity;
                BigDecimal sellPrice = sellTransaction.price;

                for (Transaction buyTransaction : transactions) {
                    if (buyTransaction.type.equals("B") && buyTransaction.coin.equals(sellTransaction.coin)) {
                        BigDecimal buyQuantity = buyTransaction.quantity;
                        BigDecimal remainingBuyQuantity = buyQuantity;
                        BigDecimal buyPrice = buyTransaction.price;
                        BigDecimal profit = BigDecimal.ZERO;

                        if (remainingSellQuantity.compareTo(remainingBuyQuantity) > 0) {
                            profit = remainingBuyQuantity.multiply(sellPrice.subtract(buyPrice));
                            remainingSellQuantity = remainingSellQuantity.subtract(remainingBuyQuantity);
                            remainingBuyQuantity = BigDecimal.ZERO;
                        } else {
                            profit = remainingSellQuantity.multiply(sellPrice.subtract(buyPrice));
                            remainingBuyQuantity = remainingBuyQuantity.subtract(remainingSellQuantity);
                            remainingSellQuantity = BigDecimal.ZERO;
                        }

                        totalProfit = totalProfit.add(profit);
                        buyTransaction.quantity = remainingBuyQuantity;

                        if (remainingSellQuantity.equals(BigDecimal.ZERO)) {
                            break;
                        }
                    }
                }

                if (remainingSellQuantity.compareTo(BigDecimal.ZERO) > 0) {
                    System.err.format("Error: Not enough %s coins to sell.\n", sellTransaction.coin);
                    System.exit(1);
                }
            }
        }

        return totalProfit;
    }

    private static class Transaction {
        String type;
        String coin;
        BigDecimal price;
        BigDecimal quantity;

        public Transaction(String type, String coin, BigDecimal price, BigDecimal quantity) {
            this.type = type;
            this.coin = coin;
            this.price = price;
            this.quantity = quantity;
        }
    }
}
