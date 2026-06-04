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
    [MaxLength(15)]
    public string PhoneNumber { get; set; } = string.Empty;

    [Required]
    [MaxLength(20)]
    public string UserRole { get; set; } = string.Empty;

    public int? ZoneId { get; set; } // Nullable because they set it during registration

    [JsonIgnore]
    [ForeignKey(nameof(ZoneId))]
    public DeliveryZone? DeliveryZone { get; set; }
    // ---------------------------

    public string? AddressText { get; set; }

    public DateTime CreatedAt { get; set; } = DateTime.UtcNow;

}