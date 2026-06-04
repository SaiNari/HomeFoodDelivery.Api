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

        // GET: api/DailyMenus
        [HttpGet]
        public async Task<ActionResult<IEnumerable<DailyMenu>>> GetDailyMenus()
        {
            return await _context.DailyMenus.ToListAsync();
        }

        // POST: api/DailyMenus
        [HttpPost]
        public async Task<ActionResult<DailyMenu>> PostDailyMenu(DailyMenu dailyMenu)
        {
            _context.DailyMenus.Add(dailyMenu);
            await _context.SaveChangesAsync();

            // Return the newly created menu
            return CreatedAtAction(nameof(GetDailyMenus), new { id = dailyMenu.MenuId }, dailyMenu);
        }
    }
}