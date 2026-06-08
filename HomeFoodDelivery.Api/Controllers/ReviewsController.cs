using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using HomeFoodDelivery.Api.Data;
using HomeFoodDelivery.Api.Models;

namespace HomeFoodDelivery.Api.Controllers
{
    [Route("api/[controller]")]
    [ApiController]
    public class ReviewsController : ControllerBase
    {
        private readonly DataContext _context;

        public ReviewsController(DataContext context)
        {
            _context = context;
        }

        [HttpGet("cook/{cookId}")]
        public async Task<IActionResult> GetReviewsForCook(int cookId)
        {
            // We use a flat query that doesn't rely on complex navigation Include() chains
            var data = await _context.Reviews
                .Where(r => r.CookId == cookId)
                .OrderByDescending(r => r.CreatedAt)
                .Select(r => new {
                    reviewId = r.ReviewId,
                    rating = r.Rating,
                    comment = r.Comment,
                    createdAt = r.CreatedAt,
                    // Manually join to the user table to get the name
                    customerName = _context.Users
                        .Where(u => u.UserId == r.CustomerId)
                        .Select(u => u.FullName)
                        .FirstOrDefault() ?? "Anonymous"
                })
                .ToListAsync();

            return Ok(data);
        }

        [HttpPost]
        public async Task<ActionResult<Review>> PostReview(Review review)
        {
            // 1. Save the new review
            _context.Reviews.Add(review);
            await _context.SaveChangesAsync();

            // 2. Automatically recalculate the Cook's average rating
            var cook = await _context.Users.FindAsync(review.CookId);
            if (cook != null)
            {
                var allReviews = await _context.Reviews.Where(r => r.CookId == review.CookId).ToListAsync();
                cook.Rating = Math.Round(allReviews.Average(r => r.Rating), 1);
                await _context.SaveChangesAsync();
            }

            return Ok(review);
        }
    }
}