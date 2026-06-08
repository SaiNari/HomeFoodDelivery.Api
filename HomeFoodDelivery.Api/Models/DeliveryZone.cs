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

    public double Latitude { get; set; }
    public double Longitude { get; set; }

    public string? HubName { get; set; }
    public string? HubContactNumber { get; set; }
    public string? DropOffInstructions { get; set; }

    public bool IsActive { get; set; } = true;
}