using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using HomeFoodDelivery.Api.Data;
using HomeFoodDelivery.Api.Models;
using HomeFoodDelivery.Api.Services; // <-- Don't forget the using statement

namespace HomeFoodDelivery.Api.Controllers
{
    [ApiController]
    [Route("api/[controller]")]
    public class OrdersController : ControllerBase
    {
        private readonly DataContext _context;
        private readonly IOrderService _orderService;

        // Inject the new Order Service
        public OrdersController(DataContext context, IOrderService orderService)
        {
            _context = context;
            _orderService = orderService;
        }

        // GET: api/Orders
        [HttpGet]
        public async Task<ActionResult<IEnumerable<Order>>> GetOrders()
        {
            return await _context.Orders.ToListAsync();
        }

        // POST: api/Orders
        [HttpPost]
        public async Task<ActionResult<Order>> PostOrder(Order order)
        {
            try
            {
                // Hand the order to the Service layer for strict processing
                var completedOrder = await _orderService.PlaceOrderAsync(order);

                return CreatedAtAction(nameof(GetOrders), new { id = completedOrder.OrderId }, completedOrder);
            }
            catch (Exception ex)
            {
                // If the service rejects the order (e.g., Sold Out), tell the mobile app exactly why
                return BadRequest(new { message = ex.Message });
            }
        }

        // GET: api/Orders/cook/{cookId}
        [HttpGet("cook/{cookId}")]
        public async Task<ActionResult<IEnumerable<Order>>> GetOrdersForCook(int cookId)
        {
            // Fetch orders that match the cook's ID by joining through the DailyMenu
            return await _context.Orders
                .Include(o => o.DailyMenu)
                .Where(o => o.DailyMenu != null && o.DailyMenu.CookId == cookId)
                .OrderByDescending(o => o.OrderTime)
                .ToListAsync();
        }

        [HttpPatch("{id}/status")]
        public async Task<IActionResult> UpdateOrderStatus(int id, [FromBody] string status)
        {
            var order = await _context.Orders.FindAsync(id);
            if (order == null) return NotFound();

            order.OrderStatus = status;
            await _context.SaveChangesAsync();

            return Ok(new { message = "Status updated successfully" });
        }

        // GET: api/Orders/customer/{customerId}
        [HttpGet("customer/{customerId}")]
        public async Task<ActionResult<IEnumerable<Order>>> GetOrdersForCustomer(int customerId)
        {
            return await _context.Orders
                .Include(o => o.DailyMenu)
                .Where(o => o.CustomerId == customerId)
                .OrderByDescending(o => o.OrderTime)
                .ToListAsync();
        }
    }
}