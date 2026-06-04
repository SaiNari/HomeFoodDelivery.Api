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
    }
}