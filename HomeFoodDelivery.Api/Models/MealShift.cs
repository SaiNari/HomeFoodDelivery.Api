using System.ComponentModel.DataAnnotations;

namespace HomeFoodDelivery.Api.Models;

public class MealShift
{
    [Key]
    public int ShiftId { get; set; }

    [Required]
    [MaxLength(50)]
    public string ShiftName { get; set; } = string.Empty;

    [Required]
    public TimeSpan EntryOpenTime { get; set; }

    [Required]
    public TimeSpan CutoffTime { get; set; }

    [Required]
    public TimeSpan DeliveryTime { get; set; }
}