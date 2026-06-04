using System.ComponentModel.DataAnnotations;

namespace HomeFoodDelivery.Api.DTOs;

// What the mobile app sends when a user signs up
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
    public int ZoneId { get; set; } // The Tech Park they selected
}

// What the mobile app sends when a user logs in
public class LoginRequest
{
    [Required]
    public string PhoneNumber { get; set; } = string.Empty;
}