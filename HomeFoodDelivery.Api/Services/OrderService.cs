using Microsoft.EntityFrameworkCore;
using Microsoft.AspNetCore.SignalR; // <-- Add this
using HomeFoodDelivery.Api.Data;
using HomeFoodDelivery.Api.Models;
using HomeFoodDelivery.Api.Hubs;    // <-- Add this

namespace HomeFoodDelivery.Api.Services;

public class OrderService : IOrderService
{
    private readonly DataContext _context;
    private readonly IHubContext<KitchenHub> _hubContext; // <-- Add the megaphone

    // Inject the megaphone into the constructor
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

            // --- REAL-TIME MAGIC ---
            // Tell every connected browser to run their "OrderReceived" JavaScript function
            await _hubContext.Clients.All.SendAsync("OrderReceived");

            return order;
        }
        catch
        {
            await transaction.RollbackAsync();
            throw;
        }
    }
}