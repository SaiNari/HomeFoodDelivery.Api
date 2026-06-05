using HomeFoodDelivery.Api.Models;
using HomeFoodDelivery.Api.DTOs;

namespace HomeFoodDelivery.Api.Services;

public interface IOrderService
{
    Task<Order> PlaceOrderAsync(Order order);
    Task<List<Order>> ProcessCheckoutBatchAsync(CheckoutRequest request);
}