using System.ComponentModel.DataAnnotations;

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

    public string? AddressText { get; set; }

    public DateTime CreatedAt { get; set; } = DateTime.UtcNow;
}