using System.ComponentModel.DataAnnotations;

namespace HomeFoodDelivery.Api.DTOs;

public class RegisterRequest
{
    [Required]
    public string FullName { get; set; } = string.Empty;

    [Required]
    public string PhoneNumber { get; set; } = string.Empty;

    [Required]
    public string UserRole { get; set; } = string.Empty; // "Customer" or "Cook"

    public string AddressText { get; set; } = string.Empty;

    [Required]
    public int ZoneId { get; set; } // Auto-mapped via Pincode or manual choice

    [MaxLength(6)]
    public string? Pincode { get; set; }

    public string? GoogleId { get; set; } // Optional social key
}

public class LoginRequest
{
    public string? PhoneNumber { get; set; }
    public string? GoogleId { get; set; } // Can log in directly via Google ID
}