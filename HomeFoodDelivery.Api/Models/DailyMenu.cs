using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace HomeFoodDelivery.Api.Models;

public class DailyMenu
{
    [Key]
    public int MenuId { get; set; }

    [Required]
    public int CookId { get; set; }
    [ForeignKey(nameof(CookId))]
    public User? Cook { get; set; }

    [Required]
    public int ShiftId { get; set; }
    [ForeignKey(nameof(ShiftId))]
    public MealShift? MealShift { get; set; }

    [Required]
    public DateTime MenuDate { get; set; }

    [Required]
    [MaxLength(150)]
    public string DishName { get; set; } = string.Empty;

    public string? Description { get; set; }

    [Required]
    public int AvailablePortions { get; set; }

    [Required]
    [Column(TypeName = "decimal(10,2)")]
    public decimal PricePerPortion { get; set; }

    public DateTime CreatedAt { get; set; } = DateTime.UtcNow;
}