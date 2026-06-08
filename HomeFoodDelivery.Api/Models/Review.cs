using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace HomeFoodDelivery.Api.Models;

public class Review
{
    [Key]
    public int ReviewId { get; set; }
    public int CookId { get; set; }
    public int CustomerId { get; set; }
    public int OrderId { get; set; }
    public int Rating { get; set; } // 1 to 5
    public string Comment { get; set; } = "";
    public DateTime CreatedAt { get; set; } = DateTime.UtcNow;

    [ForeignKey("CookId")]
    public User? Cook { get; set; }
    [ForeignKey("CustomerId")]
    public User? Customer { get; set; }
}