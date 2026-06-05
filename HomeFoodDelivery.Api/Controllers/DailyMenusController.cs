using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using HomeFoodDelivery.Api.Data;
using HomeFoodDelivery.Api.Models;

namespace HomeFoodDelivery.Api.Controllers
{
    [ApiController]
    [Route("api/[controller]")]
    public class DailyMenusController : ControllerBase
    {
        private readonly DataContext _context;

        public DailyMenusController(DataContext context)
        {
            _context = context;
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
    }
}