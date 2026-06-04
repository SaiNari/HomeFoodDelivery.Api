using HomeFoodDelivery.Api.Models;

namespace HomeFoodDelivery.Api.Services;

public interface IOrderService
{
    // The contract: Any OrderService MUST have this method.
    Task<Order> PlaceOrderAsync(Order order);
}