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

        // Helper to get IST Time safely
        private DateTime GetCurrentIstTime()
        {
            try { return TimeZoneInfo.ConvertTimeFromUtc(DateTime.UtcNow, TimeZoneInfo.FindSystemTimeZoneById("India Standard Time")); }
            catch { return TimeZoneInfo.ConvertTimeFromUtc(DateTime.UtcNow, TimeZoneInfo.FindSystemTimeZoneById("Asia/Kolkata")); }
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

        // RESTORED: The Master POST Method (Validation + Social Feed)
        [HttpPost]
        public async Task<IActionResult> PostDailyMenu([FromBody] DailyMenu menu)
        {
            try
            {
                menu.CreatedAt = DateTime.UtcNow;
                if (menu.MenuDate == default) menu.MenuDate = DateTime.UtcNow;

                if (menu.CookId == 0 || menu.ShiftId == 0)
                    return BadRequest(new { Message = "CookId and ShiftId are required." });

                _context.DailyMenus.Add(menu);
                await _context.SaveChangesAsync();

                var cook = await _context.Users.FindAsync(menu.CookId);
                string kitchenName = cook?.KitchenName ?? "Your favorite chef";

                var followerIds = await _context.CookFollowers
                    .Where(cf => cf.CookId == menu.CookId)
                    .Select(cf => cf.CustomerId.ToString())
                    .ToListAsync();

                if (followerIds.Any())
                {
                    await _hubContext.Clients.Users(followerIds).SendAsync("NewMenuAlert", new
                    {
                        Message = $"{kitchenName} just posted a new menu: {menu.DishName}!",
                        MenuId = menu.MenuId,
                        CookName = kitchenName
                    });
                }

                return Ok(new { Message = "Meal published successfully!", Menu = menu });
            }
            catch (Exception ex)
            {
                return BadRequest(new { Message = ex.InnerException?.Message ?? ex.Message });
            }
        }

        // NEW: DELETE A MENU (Only before cutoff)
        [HttpDelete("{id}")]
        public async Task<IActionResult> DeleteMenu(int id)
        {
            var menu = await _context.DailyMenus.Include(m => m.MealShift).FirstOrDefaultAsync(m => m.MenuId == id);
            if (menu == null) return NotFound(new { Message = "Menu not found." });

            var istNow = GetCurrentIstTime();

            if (menu.MenuDate.Date == istNow.Date && menu.MealShift != null && istNow.TimeOfDay >= menu.MealShift.CutoffTime)
                return BadRequest(new { Message = "Cannot delete: The kitchen cutoff time for this shift has already passed." });

            _context.DailyMenus.Remove(menu);
            await _context.SaveChangesAsync();
            return Ok(new { Message = "Schedule deleted successfully." });
        }

        // NEW: EDIT A MENU (Only before cutoff)
        [HttpPut("{id}")]
        public async Task<IActionResult> UpdateMenu(int id, [FromBody] DailyMenu updatedMenu)
        {
            var menu = await _context.DailyMenus.Include(m => m.MealShift).FirstOrDefaultAsync(m => m.MenuId == id);
            if (menu == null) return NotFound(new { Message = "Menu not found." });

            var istNow = GetCurrentIstTime();

            if (menu.MenuDate.Date == istNow.Date && menu.MealShift != null && istNow.TimeOfDay >= menu.MealShift.CutoffTime)
                return BadRequest(new { Message = "Cannot edit: The kitchen cutoff time for this shift has already passed." });

            menu.DishName = updatedMenu.DishName;
            menu.Description = updatedMenu.Description;
            menu.PricePerPortion = updatedMenu.PricePerPortion;
            menu.AvailablePortions = updatedMenu.AvailablePortions;

            if (!string.IsNullOrEmpty(updatedMenu.ImageUrl))
                menu.ImageUrl = updatedMenu.ImageUrl;

            await _context.SaveChangesAsync();
            return Ok(new { Message = "Schedule updated successfully.", Menu = menu });
        }
    }
}