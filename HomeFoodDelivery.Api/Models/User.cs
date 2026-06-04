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
    public string UserRole { get; set; } = string.Empty; // "Customer" or "Cook"

    public int? ZoneId { get; set; }

    [JsonIgnore]
    [ForeignKey(nameof(ZoneId))]
    public DeliveryZone? DeliveryZone { get; set; }

    public string? AddressText { get; set; }

    // --- NEW FIELD: For neighborhood mapping ---
    [MaxLength(6)]
    public string? Pincode { get; set; }

    // --- NEW FIELD: For social authentication ---
    public string? GoogleId { get; set; }

    public DateTime CreatedAt { get; set; } = DateTime.UtcNow;
}