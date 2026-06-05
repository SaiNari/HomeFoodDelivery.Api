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
            // 1. Check if user already exists via Phone or Google ID
            if (await _context.Users.AnyAsync(u => u.PhoneNumber == request.PhoneNumber))
                return BadRequest(new { message = "Phone number is already registered." });

            if (!string.IsNullOrEmpty(request.GoogleId) && await _context.Users.AnyAsync(u => u.GoogleId == request.GoogleId))
                return BadRequest(new { message = "This Google account is already linked to a profile." });

            var newUser = new User
            {
                FullName = request.FullName,
                PhoneNumber = request.PhoneNumber,
                UserRole = request.UserRole,
                AddressText = request.AddressText,
                ZoneId = request.ZoneId,
                Pincode = request.Pincode,
                GoogleId = request.GoogleId,
                CreatedAt = DateTime.UtcNow
            };

            _context.Users.Add(newUser);
            await _context.SaveChangesAsync();

            return Ok(new { message = "Registration successful!", userId = newUser.UserId });
        }

        [HttpPost("login")]
        public async Task<IActionResult> Login(LoginRequest request)
        {
            User? user = null;

            // 1. Smart Lookup: Check if logging in via Google or Phone number
            if (!string.IsNullOrEmpty(request.GoogleId))
            {
                user = await _context.Users
                    .Include(u => u.DeliveryZone)
                    .FirstOrDefaultAsync(u => u.GoogleId == request.GoogleId);
            }
            else if (!string.IsNullOrEmpty(request.PhoneNumber))
            {
                user = await _context.Users
                    .Include(u => u.DeliveryZone)
                    .FirstOrDefaultAsync(u => u.PhoneNumber == request.PhoneNumber);
            }

            if (user == null)
                return Unauthorized(new { message = "Account not found. Proceed to registration." });

            return Ok(new
            {
                message = "Login successful",
                userId = user.UserId,
                fullName = user.FullName,
                role = user.UserRole,  
                zoneId = user.ZoneId,
                zoneName = user.DeliveryZone?.TechParkName
            });
        }
    }
}