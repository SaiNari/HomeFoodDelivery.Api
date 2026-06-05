using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using HomeFoodDelivery.Api.Data;
using HomeFoodDelivery.Api.Models;

namespace HomeFoodDelivery.Api.Controllers
{
    [ApiController]
    [Route("api/[controller]")]
    public class UsersController : ControllerBase
    {
        private readonly DataContext _context;

        public UsersController(DataContext context)
        {
            _context = context;
        }

        // GET: api/users
        [HttpGet]
        public async Task<ActionResult<IEnumerable<User>>> GetUsers()
        {
            return await _context.Users.ToListAsync();
        }

        [HttpGet("{id}")]
        public async Task<ActionResult<User>> GetUser(int id)
        {
            var user = await _context.Users.FindAsync(id);

            if (user == null)
            {
                return NotFound();
            }

            return user;
        }

        // POST: api/users
        [HttpPost]
        public async Task<ActionResult<User>> PostUser(User user)
        {
            _context.Users.Add(user);
            await _context.SaveChangesAsync();

            return CreatedAtAction(nameof(GetUser), new { id = user.UserId }, user);
        }

        [HttpGet("kitchens/zone/{zoneId}")]
        public async Task<ActionResult<IEnumerable<object>>> GetKitchensForTechPark(int zoneId)
        {
            var zone = await _context.DeliveryZones.FindAsync(zoneId);
            if (zone == null) return NotFound("Tech park not found.");

            var kitchens = await _context.Users
                .Where(u => u.UserRole == "Cook" && u.ZoneId == zoneId)
                .ToListAsync();

            var result = kitchens.Select(k => new
            {
                CookId = k.UserId,
                KitchenName = k.KitchenName ?? (k.FullName + "'s Kitchen"),
                KitchenAddress = k.KitchenAddress ?? k.AddressText,
                Rating = k.Rating,
                DistanceInKm = CalculateDistance(zone.Latitude, zone.Longitude, k.Latitude ?? zone.Latitude, k.Longitude ?? zone.Longitude)
            }).OrderBy(k => k.DistanceInKm).ToList();

            return Ok(result);
        }

        private double CalculateDistance(double lat1, double lon1, double lat2, double lon2)
        {
            var R = 6371;
            var dLat = (lat2 - lat1) * Math.PI / 180;
            var dLon = (lon2 - lon1) * Math.PI / 180;
            var a =
                Math.Sin(dLat / 2) * Math.Sin(dLat / 2) +
                Math.Cos(lat1 * Math.PI / 180) * Math.Cos(lat2 * Math.PI / 180) * Math.Sin(dLon / 2) * Math.Sin(dLon / 2);
            var c = 2 * Math.Atan2(Math.Sqrt(a), Math.Sqrt(1 - a));
            return Math.Round(R * c, 2); 
        }
    }
}
