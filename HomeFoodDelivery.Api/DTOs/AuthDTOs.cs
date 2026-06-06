using System.ComponentModel.DataAnnotations;

namespace HomeFoodDelivery.Api.DTOs;

public class RegisterRequest
{
    [Required]
    public string FullName { get; set; } = string.Empty;

    [Required]
    public string PhoneNumber { get; set; } = string.Empty;

    [Required]
    public string UserRole { get; set; } = string.Empty;

    public string AddressText { get; set; } = string.Empty;

    [Required]
    public int ZoneId { get; set; }

    [MaxLength(6)]
    public string? Pincode { get; set; }

    public string? GoogleId { get; set; }

    // Cook-only fields (ignored for customers).
    public string? KitchenName { get; set; }
    public string? KitchenAddress { get; set; }
    public string? FssaiLicense { get; set; }
    public DateTime? FssaiExpiry { get; set; }
    public double? Latitude { get; set; }
    public double? Longitude { get; set; }
}

public class LoginRequest
{
    public string? PhoneNumber { get; set; }
    public string? GoogleId { get; set; }
}

// Used by customers/cooks to edit their own profile.
public class UpdateProfileRequest
{
    public string? FullName { get; set; }
    public string? AddressText { get; set; }
    public string? Pincode { get; set; }
    public int? ZoneId { get; set; }
    public string? KitchenName { get; set; }
    public string? KitchenAddress { get; set; }
    public double? Latitude { get; set; }
    public double? Longitude { get; set; }
}