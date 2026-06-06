using HomeFoodDelivery.Api.Data;
using HomeFoodDelivery.Api.DTOs;
using HomeFoodDelivery.Api.Models;
using HomeFoodDelivery.Api.Services;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;

namespace HomeFoodDelivery.Api.Controllers
{
    [ApiController]
    [Route("api/[controller]")]
    public class AuthController : ControllerBase
    {
        private readonly DataContext _context;
        private readonly IWalletService _walletService;

        public AuthController(DataContext context, IWalletService walletService)
        {
            _context = context;
            _walletService = walletService;
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

            if (!string.IsNullOrEmpty(request.ReferralCode) && request.ReferralCode.StartsWith("OOTA-EMP-"))
            {
                var referrerIdString = request.ReferralCode.Replace("OOTA-EMP-", "");
                if (int.TryParse(referrerIdString, out int referrerId))
                {
                    await _walletService.CreditBalanceAsync(referrerId, 50.00m, $"Referral Bonus for inviting {newUser.FullName}");

                    await _walletService.CreditBalanceAsync(newUser.UserId, 50.00m, "Welcome Bonus (Referred by a friend)");
                }
            }

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