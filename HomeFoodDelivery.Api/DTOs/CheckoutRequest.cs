namespace HomeFoodDelivery.Api.DTOs;

public class CheckoutRequest
{
    public int CustomerId { get; set; }
    public string PaymentMethod { get; set; } = "UPI/COD";
    public List<CartItemDto> Items { get; set; } = new List<CartItemDto>();
    public decimal TotalAmount { get; set; }
}

public class CartItemDto
{
    public int MenuId { get; set; }
    public int Quantity { get; set; }
    public decimal TotalPrice { get; set; }
}