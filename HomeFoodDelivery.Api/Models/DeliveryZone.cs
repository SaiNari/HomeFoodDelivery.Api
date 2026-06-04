using System.ComponentModel.DataAnnotations;

namespace HomeFoodDelivery.Api.Models;

public class DeliveryZone
{
    [Key]
    public int ZoneId { get; set; }

    [Required]
    [MaxLength(100)]
    public string TechParkName { get; set; } = string.Empty;

    [Required]
    [MaxLength(200)]
    public string ServicingNeighborhoods { get; set; } = string.Empty;

    public bool IsActive { get; set; } = true;
}