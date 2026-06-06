using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace HomeFoodDelivery.Api.Models;

public class CookFollower
{
    [Key]
    public int Id { get; set; }

    [Required]
    public int CustomerId { get; set; }
    [ForeignKey(nameof(CustomerId))]
    public User? Customer { get; set; }

    [Required]
    public int CookId { get; set; }
    [ForeignKey(nameof(CookId))]
    public User? Cook { get; set; }

    public DateTime CreatedAt { get; set; } = DateTime.UtcNow;
}