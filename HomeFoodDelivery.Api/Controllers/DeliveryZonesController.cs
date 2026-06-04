using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using HomeFoodDelivery.Api.Data;
using HomeFoodDelivery.Api.Models;

namespace HomeFoodDelivery.Api.Controllers
{
    [ApiController]
    [Route("api/[controller]")]
    public class DeliveryZonesController : ControllerBase
    {
        private readonly DataContext _context;

        public DeliveryZonesController(DataContext context)
        {
            _context = context;
        }

        // GET: api/DeliveryZones
        [HttpGet]
        public async Task<ActionResult<IEnumerable<DeliveryZone>>> GetZones()
        {
            // Only return active zones to the mobile app
            return await _context.DeliveryZones.Where(z => z.IsActive).ToListAsync();
        }
    }
}