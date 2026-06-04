using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using HomeFoodDelivery.Api.Data;
using HomeFoodDelivery.Api.Models;
using HomeFoodDelivery.Api.DTOs;

namespace HomeFoodDelivery.Api.Controllers
{
    [ApiController]
    [Route("api/[controller]")]
    public class AuthController : ControllerBase
    {
        private readonly DataContext _context;

        public AuthController(DataContext context)
        {
            _context = context;
        }

        [HttpPost("register")]
        public async Task<IActionResult> Register(RegisterRequest request)
        {
            // 1. Check if the user already exists
            if (await _context.Users.AnyAsync(u => u.PhoneNumber == request.PhoneNumber))
                return BadRequest(new { message = "Phone number is already registered. Please log in." });

            // 2. Create the new user and attach them to a Tech Park Zone
            var newUser = new User
            {
                FullName = request.FullName,
                PhoneNumber = request.PhoneNumber,
                UserRole = request.UserRole,
                AddressText = request.AddressText,
                ZoneId = request.ZoneId,
                CreatedAt = DateTime.UtcNow
            };

            _context.Users.Add(newUser);
            await _context.SaveChangesAsync();

            return Ok(new { message = "Registration successful!", userId = newUser.UserId });
        }

        [HttpPost("login")]
        public async Task<IActionResult> Login(LoginRequest request)
        {
            // 1. Find the user and include their Tech Park details
            var user = await _context.Users
                .Include(u => u.DeliveryZone)
                .FirstOrDefaultAsync(u => u.PhoneNumber == request.PhoneNumber);

            // 2. Reject if they don't exist
            if (user == null)
                return Unauthorized(new { message = "User not found. Please register an account." });

            // 3. Return the critical routing data to the mobile app!
            return Ok(new
            {
                message = "Login successful",
                userId = user.UserId,
                fullName = user.FullName,
                role = user.UserRole,       // Mobile app uses this to redirect to Cook vs Customer dashboard
                zoneId = user.ZoneId,       // Mobile app uses this to filter the food
                zoneName = user.DeliveryZone?.TechParkName
            });
        }
    }
}