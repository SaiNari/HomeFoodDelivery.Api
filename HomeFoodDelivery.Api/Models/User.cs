using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;
using System.Text.Json.Serialization;

namespace HomeFoodDelivery.Api.Models;

public class User
{
    [Key]
    public int UserId { get; set; }

    [Required]
    [MaxLength(100)]
    public string FullName { get; set; } = string.Empty;

    [Required]
    [MaxLength(20)]
    public string PhoneNumber { get; set; } = string.Empty;

    [Required]
    [MaxLength(20)]
    public string UserRole { get; set; } = string.Empty; 

    public int? ZoneId { get; set; }

    [JsonIgnore]
    [ForeignKey(nameof(ZoneId))]
    public DeliveryZone? DeliveryZone { get; set; }

    public string? AddressText { get; set; }
    [MaxLength(6)]
    public string? Pincode { get; set; }

    public string? GoogleId { get; set; }

    [MaxLength(100)]
    public string? KitchenName { get; set; }

    [MaxLength(250)]
    public string? KitchenAddress { get; set; }

    public double? Latitude { get; set; }
    public double? Longitude { get; set; }

    public string? YouTubeUrl { get; set; }
    public string? FacebookUrl { get; set; }
    public string? InstagramUrl { get; set; }

    public double Rating { get; set; } = 5.0;

    public List<Review> Reviews { get; set; } = new List<Review>();

    public DateTime CreatedAt { get; set; } = DateTime.UtcNow;
}