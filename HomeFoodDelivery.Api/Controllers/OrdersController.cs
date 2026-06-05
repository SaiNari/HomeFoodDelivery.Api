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
    public class OrdersController : ControllerBase
    {
        private readonly DataContext _context;
        private readonly IOrderService _orderService;

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
                var completedOrder = await _orderService.PlaceOrderAsync(order);

                return CreatedAtAction(nameof(GetOrders), new { id = completedOrder.OrderId }, completedOrder);
            }
            catch (Exception ex)
            {
                return BadRequest(new { message = ex.Message });
            }
        }

        // GET: api/Orders/cook/{cookId}
        [HttpGet("cook/{cookId}")]
        public async Task<ActionResult<IEnumerable<Order>>> GetOrdersForCook(int cookId)
        {
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

        [HttpPatch("{id}/payment")]
        public async Task<IActionResult> UpdatePaymentStatus(int id, [FromBody] string paymentStatus)
        {
            var order = await _context.Orders.FindAsync(id);
            if (order == null) return NotFound();

            order.PaymentStatus = paymentStatus; 
            await _context.SaveChangesAsync();

            return Ok(new { message = $"Payment status updated to {paymentStatus}" });
        }

        [HttpGet("customer/{customerId}")]
        public async Task<ActionResult<IEnumerable<Order>>> GetOrdersForCustomer(int customerId)
        {
            return await _context.Orders
                .Include(o => o.DailyMenu)
                .Where(o => o.CustomerId == customerId)
                .OrderByDescending(o => o.OrderTime)
                .ToListAsync();
        }

        [HttpPost("checkout")]
        public async Task<IActionResult> Checkout([FromBody] CheckoutRequest request)
        {
            try
            {
                if (request.Items == null || !request.Items.Any())
                    return BadRequest(new { message = "Cart is empty." });

                var completedOrders = await _orderService.ProcessCheckoutBatchAsync(request);
                return Ok(new { message = "Order successful!", orders = completedOrders });
            }
            catch (Exception ex)
            {
                return BadRequest(new { message = ex.Message });
            }
        }
    }
}