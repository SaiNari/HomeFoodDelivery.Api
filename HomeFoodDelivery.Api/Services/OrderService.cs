using HomeFoodDelivery.Api.Data;
using HomeFoodDelivery.Api.Hubs; 
using HomeFoodDelivery.Api.Models;
using Microsoft.AspNetCore.SignalR; 
using Microsoft.EntityFrameworkCore;
using HomeFoodDelivery.Api.DTOs;

namespace HomeFoodDelivery.Api.Services;

public class OrderService : IOrderService
{
    private readonly DataContext _context;
    private readonly IHubContext<KitchenHub> _hubContext; 

    
    public OrderService(DataContext context, IHubContext<KitchenHub> hubContext)
    {
        _context = context;
        _hubContext = hubContext;
    }

    public async Task<Order> PlaceOrderAsync(Order order)
    {
        using var transaction = await _context.Database.BeginTransactionAsync();

        try
        {
            var menu = await _context.DailyMenus.FirstOrDefaultAsync(m => m.MenuId == order.MenuId);
            if (menu == null) throw new Exception("Menu item not found.");
            if (menu.AvailablePortions < order.Quantity) throw new Exception($"Sold out! Only {menu.AvailablePortions} portions left.");

            menu.AvailablePortions -= order.Quantity;
            _context.Orders.Add(order);

            await _context.SaveChangesAsync();
            await transaction.CommitAsync();

            await _hubContext.Clients.All.SendAsync("OrderReceived");

            return order;
        }
        catch
        {
            await transaction.RollbackAsync();
            throw;
        }
    }

    public async Task<List<Order>> ProcessCheckoutBatchAsync(CheckoutRequest request)
    {
        using var transaction = await _context.Database.BeginTransactionAsync();
        var processedOrders = new List<Order>();
        var batchIdempotencyKey = Guid.NewGuid().ToString();

        try
        {
            foreach (var item in request.Items)
            {
                var menu = await _context.DailyMenus.FirstOrDefaultAsync(m => m.MenuId == item.MenuId);

                if (menu == null)
                    throw new Exception($"Menu item {item.MenuId} no longer exists.");

                if (menu.AvailablePortions < item.Quantity)
                    throw new Exception($"Sold out! Only {menu.AvailablePortions} portions of {menu.DishName} left.");

                menu.AvailablePortions -= item.Quantity;

                var newOrder = new Order
                {
                    CustomerId = request.CustomerId,
                    MenuId = item.MenuId,
                    Quantity = item.Quantity,
                    TotalPrice = item.TotalPrice,
                    OrderStatus = "Pending",
                    PaymentStatus = request.PaymentMethod,
                    OrderTime = DateTime.UtcNow,
                    IdempotencyKey = Guid.NewGuid()
                };

                _context.Orders.Add(newOrder);
                processedOrders.Add(newOrder);
            }

            await _context.SaveChangesAsync();
            await transaction.CommitAsync();

            await _hubContext.Clients.All.SendAsync("OrderReceived");

            return processedOrders;
        }
        catch
        {
            await transaction.RollbackAsync();
            throw;
        }
    }
}