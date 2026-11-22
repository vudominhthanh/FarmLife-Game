package contracts;

public record ItemQuantity(
        String itemCode,
        short quality,
        int quantity
) {
    public ItemQuantity(String itemCode, int quantity) {
        this(itemCode, (short) 100, quantity);
    }
}
