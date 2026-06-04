using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;
using System.Text.Json.Serialization; // <-- Added for JSON control

namespace HomeFoodDelivery.Api.Models;

public class Order
{
    [Key]
    public int OrderId { get; set; }

    [Required]
    public int CustomerId { get; set; }

    [JsonIgnore] // <-- Hide User object from Swagger
    [ForeignKey(nameof(CustomerId))]
    public User? Customer { get; set; }

    [Required]
    public int MenuId { get; set; }

    
    [ForeignKey(nameof(MenuId))]
    public DailyMenu? DailyMenu { get; set; }

    [Required]
    public int Quantity { get; set; }

    [Required]
    [Column(TypeName = "decimal(10,2)")]
    public decimal TotalPrice { get; set; }

    [Required]
    [MaxLength(30)]
    public string OrderStatus { get; set; } = "Pending";

    [Required]
    public Guid IdempotencyKey { get; set; }

    public DateTime OrderTime { get; set; } = DateTime.UtcNow;
}