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

        // Consolidated POST method - Single entry point
        [HttpPost]
        public async Task<ActionResult<DailyMenu>> PostDailyMenu(DailyMenu menu)
        {
            try
            {
                // Set creation dates if not set
                menu.CreatedAt = DateTime.UtcNow;
                if (menu.MenuDate == default) menu.MenuDate = DateTime.UtcNow;

                // Validate that Required fields are present
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
                // Returns the actual error message if something goes wrong
                return BadRequest(ex.InnerException?.Message ?? ex.Message);
            }
        }
    }
}