using HomeFoodDelivery.Api.Data;
using HomeFoodDelivery.Api.Hubs;
using Microsoft.AspNetCore.SignalR;
using Microsoft.EntityFrameworkCore;

namespace HomeFoodDelivery.Api.Services;

public class MenuCutoffWorker : BackgroundService
{
    private readonly IServiceProvider _serviceProvider;
    private readonly ILogger<MenuCutoffWorker> _logger;

    public MenuCutoffWorker(IServiceProvider serviceProvider, ILogger<MenuCutoffWorker> logger)
    {
        _serviceProvider = serviceProvider;
        _logger = logger;
    }

    protected override async Task ExecuteAsync(CancellationToken stoppingToken)
    {
        _logger.LogInformation("Menu Cutoff Worker started. Watching the clock...");

        while (!stoppingToken.IsCancellationRequested)
        {
            try { await ProcessCutoffsAsync(); }
            catch (Exception ex) { _logger.LogError(ex, "Error occurred processing menu cutoffs."); }

            await Task.Delay(TimeSpan.FromMinutes(1), stoppingToken);
        }
    }

    private async Task ProcessCutoffsAsync()
    {
        using var scope = _serviceProvider.CreateScope();
        var context = scope.ServiceProvider.GetRequiredService<DataContext>();
        var hubContext = scope.ServiceProvider.GetRequiredService<IHubContext<KitchenHub>>();

        TimeZoneInfo istZone;
        try { istZone = TimeZoneInfo.FindSystemTimeZoneById("India Standard Time"); }
        catch { istZone = TimeZoneInfo.FindSystemTimeZoneById("Asia/Kolkata"); }

        DateTime istNow = TimeZoneInfo.ConvertTimeFromUtc(DateTime.UtcNow, istZone);
        var today = istNow.Date;
        var currentTime = istNow.TimeOfDay;

        var expiredMenus = await context.DailyMenus
            .Include(m => m.MealShift)
            .Where(m => m.MenuDate.Date == today
                     && m.AvailablePortions > 0
                     && m.MealShift != null
                     && currentTime >= m.MealShift.CutoffTime)
            .ToListAsync();

        if (expiredMenus.Any())
        {
            foreach (var menu in expiredMenus)
            {
                menu.AvailablePortions = 0;
                _logger.LogInformation($"Locked Menu {menu.MenuId} ({menu.DishName}) - Cutoff passed at {currentTime}.");
                await hubContext.Clients.All.SendAsync("MenuLocked", menu.MenuId);
            }
            await context.SaveChangesAsync();
        }
    }
}