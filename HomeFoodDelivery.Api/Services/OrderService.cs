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
    private readonly IWalletService _walletService;

    public OrderService(DataContext context, IHubContext<KitchenHub> hubContext, IWalletService walletService)
    {
        _context = context;
        _hubContext = hubContext;
        _walletService = walletService;
    }

    private DateTime GetCurrentIstTime()
    {
        try { return TimeZoneInfo.ConvertTimeFromUtc(DateTime.UtcNow, TimeZoneInfo.FindSystemTimeZoneById("India Standard Time")); }
        catch { return TimeZoneInfo.ConvertTimeFromUtc(DateTime.UtcNow, TimeZoneInfo.FindSystemTimeZoneById("Asia/Kolkata")); }
    }

    public async Task<Order> PlaceOrderAsync(Order order)
    {
        using var transaction = await _context.Database.BeginTransactionAsync();
        try
        {
            var menu = await _context.DailyMenus.Include(m => m.MealShift).FirstOrDefaultAsync(m => m.MenuId == order.MenuId);
            if (menu == null) throw new Exception("Menu item not found.");

            var istNow = GetCurrentIstTime();
            if (menu.MenuDate.Date == istNow.Date && menu.MealShift != null && istNow.TimeOfDay >= menu.MealShift.CutoffTime)
                throw new Exception($"Too late! The cutoff time for {menu.DishName} has already passed.");

            if (menu.AvailablePortions < order.Quantity) throw new Exception($"Sold out! Only {menu.AvailablePortions} portions left.");

            menu.AvailablePortions -= order.Quantity;
            _context.Orders.Add(order);
            await _context.SaveChangesAsync();
            await transaction.CommitAsync();
            await _hubContext.Clients.All.SendAsync("OrderReceived");
            return order;
        }
        catch { await transaction.RollbackAsync(); throw; }
    }

    public async Task<List<Order>> ProcessCheckoutBatchAsync(CheckoutRequest request)
    {
        using var transaction = await _context.Database.BeginTransactionAsync();
        var processedOrders = new List<Order>();
        var istNow = GetCurrentIstTime();

        try
        {
            if (request.PaymentMethod == "Wallet")
            {
                var success = await _walletService.DebitBalanceAsync(request.CustomerId, request.TotalAmount);
                if (!success) throw new Exception("Insufficient wallet balance.");
            }

            foreach (var item in request.Items)
            {
                var menu = await _context.DailyMenus.Include(m => m.MealShift).FirstOrDefaultAsync(m => m.MenuId == item.MenuId);
                if (menu == null) throw new Exception($"Menu item {item.MenuId} no longer exists.");

                if (menu.MenuDate.Date == istNow.Date && menu.MealShift != null && istNow.TimeOfDay >= menu.MealShift.CutoffTime)
                    throw new Exception($"Too late! The cutoff time for {menu.DishName} has already passed.");

                if (menu.AvailablePortions < item.Quantity) throw new Exception($"Sold out! Only {menu.AvailablePortions} portions left.");

                menu.AvailablePortions -= item.Quantity;
                var newOrder = new Order { CustomerId = request.CustomerId, MenuId = item.MenuId, Quantity = item.Quantity, TotalPrice = item.TotalPrice, OrderStatus = "Pending", PaymentStatus = request.PaymentMethod, OrderTime = DateTime.UtcNow, IdempotencyKey = Guid.NewGuid() };
                _context.Orders.Add(newOrder);
                processedOrders.Add(newOrder);
            }
            await _context.SaveChangesAsync();
            await transaction.CommitAsync();
            await _hubContext.Clients.All.SendAsync("OrderReceived");
            return processedOrders;
        }
        catch { await transaction.RollbackAsync(); throw; }
    }
}