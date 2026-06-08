using HomeFoodDelivery.Api.Data;
using HomeFoodDelivery.Api.Hubs;
using HomeFoodDelivery.Api.Models;
using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.SignalR;
using Microsoft.EntityFrameworkCore;

namespace HomeFoodDelivery.Api.Controllers
{
    [ApiController]
    [Route("api/[controller]")]
    public class DailyMenusController : ControllerBase
    {
        private readonly DataContext _context;
        private readonly IHubContext<KitchenHub> _hubContext;

        public DailyMenusController(DataContext context, IHubContext<KitchenHub> hubContext)
        {
            _context = context;
            _hubContext = hubContext;
        }

        [HttpGet]
        public async Task<ActionResult<IEnumerable<DailyMenu>>> GetDailyMenus()
        {
            return await _context.DailyMenus.ToListAsync();
        }

        [HttpGet("kitchen/{cookId}")]
        public async Task<ActionResult<IEnumerable<DailyMenu>>> GetMenusForKitchen(int cookId)
        {
            var menus = await _context.DailyMenus
                .Where(m => m.CookId == cookId && m.AvailablePortions > 0 && m.MenuDate.Date >= DateTime.UtcNow.Date)
                .OrderBy(m => m.ShiftId)
                .ToListAsync();

            return Ok(menus);
        }

        [HttpPost]
        public async Task<ActionResult<DailyMenu>> PostDailyMenu(DailyMenu menu)
        {
            try
            {
                menu.CreatedAt = DateTime.UtcNow;
                if (menu.MenuDate == default) menu.MenuDate = DateTime.UtcNow;

                if (menu.CookId == 0 || menu.ShiftId == 0)
                {
                    return BadRequest("CookId and ShiftId are required.");
                }

                _context.DailyMenus.Add(menu);
                await _context.SaveChangesAsync();

                return Ok(menu);
            }
            catch (Exception ex)
            {
                return BadRequest(ex.InnerException?.Message ?? ex.Message);
            }
        }

        [HttpPost]
        public async Task<IActionResult> PostMenu([FromBody] DailyMenu menu)
        {
            _context.DailyMenus.Add(menu);
            await _context.SaveChangesAsync();

            // 1. Find all customer IDs following this specific cook
            var followerIds = await _context.CookFollowers
                .Where(cf => cf.CookId == menu.CookId)
                .Select(cf => cf.CustomerId.ToString())
                .ToListAsync();

            // 2. Send the real-time notification to only these followers
            // We send a specific event "NewMenuAlert"
            await _hubContext.Clients.Users(followerIds).SendAsync("NewMenuAlert", new
            {
                Message = $"Your favorite chef just posted a new menu: {menu.DishName}!",
                MenuId = menu.MenuId,
                CookName = "Your Favorite Kitchen" // You could also fetch the cook's kitchen name here
            });

            return Ok(menu);
        }
    }
}