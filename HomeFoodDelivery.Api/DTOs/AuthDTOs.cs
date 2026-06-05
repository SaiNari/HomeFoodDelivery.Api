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

    public string? ReferralCode { get; set; }
}

public class LoginRequest
{
    public string? PhoneNumber { get; set; }
    public string? GoogleId { get; set; } 
}