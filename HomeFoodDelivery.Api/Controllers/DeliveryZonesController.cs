using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using HomeFoodDelivery.Api.Data;
using HomeFoodDelivery.Api.Models;
using HomeFoodDelivery.Api.DTOs;

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

        [HttpGet]
        public async Task<ActionResult<IEnumerable<DeliveryZone>>> GetZones()
        {
            return await _context.DeliveryZones.Where(z => z.IsActive).ToListAsync();
        }

        [HttpPatch("{id}/hub-details")]
        public async Task<IActionResult> UpdateHubDetails(int id, [FromBody] HubUpdateDto hubDto)
        {
            var zone = await _context.DeliveryZones.FindAsync(id);
            if (zone == null) return NotFound();

            zone.HubName = hubDto.HubName;
            zone.HubContactNumber = hubDto.HubContactNumber;
            zone.DropOffInstructions = hubDto.DropOffInstructions;

            await _context.SaveChangesAsync();
            return Ok(zone);
        }
    }
}