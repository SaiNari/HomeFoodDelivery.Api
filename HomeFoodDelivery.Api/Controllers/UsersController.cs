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

        [HttpPatch("{id}/profile")]
        public async Task<IActionResult> UpdateCookProfile(int id, [FromBody] UserProfileUpdateDto profileDto)
        {
            var user = await _context.Users.FindAsync(id);
            if (user == null) return NotFound("User not found.");

            user.FacebookUrl = profileDto.FacebookUrl;
            user.InstagramUrl = profileDto.InstagramUrl;
            user.YouTubeUrl = profileDto.YouTubeUrl;

            await _context.SaveChangesAsync();
            return Ok(new { message = "Profile updated successfully!" });
        }

        [HttpGet("cooks/{cookId}/profile")]
        public async Task<IActionResult> GetCookProfileForCustomer(int cookId, [FromQuery] int currentCustomerId)
        {
            var cook = await _context.Users.Include(u => u.Reviews).ThenInclude(r => r.Customer)
                .FirstOrDefaultAsync(u => u.UserId == cookId && u.UserRole == "Cook");
            if (cook == null) return NotFound("Cook not found.");

            var followerCount = await _context.CookFollowers.CountAsync(cf => cf.CookId == cookId);
            var isFollowing = await _context.CookFollowers.AnyAsync(cf => cf.CookId == cookId && cf.CustomerId == currentCustomerId);

            // Check if favorited!
            var isFavorite = await _context.CookFavorites.AnyAsync(cf => cf.CookId == cookId && cf.CustomerId == currentCustomerId);

            var today = DateTime.UtcNow.Date;
            var activeMenu = await _context.DailyMenus.Where(m => m.CookId == cookId && m.MenuDate >= today).ToListAsync();

            return Ok(new
            {
                KitchenName = cook.KitchenName ?? (cook.FullName + "'s Kitchen"),
                KitchenAddress = cook.KitchenAddress ?? cook.AddressText,
                Rating = cook.Rating,
                Followers = followerCount,
                IsFollowing = isFollowing,
                IsFavorite = isFavorite, // Send this to the frontend
                Socials = new { Instagram = cook.InstagramUrl, YouTube = cook.YouTubeUrl, Facebook = cook.FacebookUrl },
                Menu = activeMenu,
                Reviews = cook.Reviews.OrderByDescending(r => r.CreatedAt).Select(r => new {
                    CustomerName = r.Customer?.FullName ?? "Anonymous",
                    Rating = r.Rating,
                    Comment = r.Comment,
                    Date = r.CreatedAt.ToString("MMM dd, yyyy")
                })
            });
        }

        [HttpPost("cooks/{cookId}/toggle-follow")]
        public async Task<IActionResult> ToggleFollow(int cookId, [FromBody] int customerId)
        {
            var existing = await _context.CookFollowers.FirstOrDefaultAsync(cf => cf.CookId == cookId && cf.CustomerId == customerId);
            if (existing != null)
            {
                _context.CookFollowers.Remove(existing);
                await _context.SaveChangesAsync();
                return Ok(new { isFollowing = false });
            }
            _context.CookFollowers.Add(new CookFollower { CookId = cookId, CustomerId = customerId });
            await _context.SaveChangesAsync();
            return Ok(new { isFollowing = true });
        }

        [HttpGet("cooks/{cookId}/followers")]
        public async Task<IActionResult> GetCookFollowers(int cookId)
        {
            var followers = await _context.CookFollowers
                .Include(cf => cf.Customer)
                .Where(cf => cf.CookId == cookId)
                .OrderByDescending(cf => cf.CreatedAt)
                .Select(cf => new {
                    CustomerName = cf.Customer.FullName,
                    FollowedOn = cf.CreatedAt.ToString("MMM dd, yyyy")
                })
                .ToListAsync();

            return Ok(followers);
        }

        [HttpPost("cooks/{cookId}/toggle-favorite")]
        public async Task<IActionResult> ToggleFavorite(int cookId, [FromBody] int customerId)
        {
            var existing = await _context.CookFavorites.FirstOrDefaultAsync(cf => cf.CookId == cookId && cf.CustomerId == customerId);
            if (existing != null)
            {
                _context.CookFavorites.Remove(existing);
                await _context.SaveChangesAsync();
                return Ok(new { isFavorite = false });
            }
            _context.CookFavorites.Add(new CookFavorite { CookId = cookId, CustomerId = customerId });
            await _context.SaveChangesAsync();
            return Ok(new { isFavorite = true });
        }

        [HttpGet("cooks/{userId}/loyal-customers")]
        public async Task<IActionResult> GetLoyalCustomers(int userId)
        {
            // Fetches from CookFavorites now!
            var customers = await _context.CookFavorites
                .Include(cf => cf.Customer)
                .Where(cf => cf.CookId == userId)
                .OrderByDescending(cf => cf.CreatedAt)
                .Select(cf => new {
                    CustomerName = cf.Customer.FullName,
                    FollowedDate = cf.CreatedAt.ToString("MMM dd, yyyy")
                }).ToListAsync();
            return Ok(customers);
        }

        [HttpGet("cooks/{userId}/stats")]
        public async Task<IActionResult> GetCookStats(int userId)
        {
            var followers = await _context.CookFollowers.CountAsync(cf => cf.CookId == userId);
            var following = await _context.CookFollowers.CountAsync(cf => cf.CustomerId == userId);
            return Ok(new { followers, following });
        }

        public class UserProfileUpdateDto
        {
            public string? FacebookUrl { get; set; }
            public string? InstagramUrl { get; set; }
            public string? YouTubeUrl { get; set; }
        }
    }
}
