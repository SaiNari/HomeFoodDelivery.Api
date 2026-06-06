namespace HomeFoodDelivery.Api.Models;

using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

public class Wallet
{
    [Key]
    public int WalletId { get; set; }

    [Required]
    public int UserId { get; set; } 

    [Required]
    [Column(TypeName = "decimal(10,2)")]
    public decimal Balance { get; set; } = 0.00m;

    [ForeignKey(nameof(UserId))]
    public User? User { get; set; }

    public List<WalletTransaction> Transactions { get; set; } = new();
}

public class WalletTransaction
{
    [Key]
    public int TransactionId { get; set; }

    [Required]
    public int WalletId { get; set; }

    [Required]
    [Column(TypeName = "decimal(10,2)")]
    public decimal Amount { get; set; }

    [Required]
    [MaxLength(50)]
    public string Type { get; set; } = string.Empty;

    [Required]
    public DateTime Timestamp { get; set; } = DateTime.UtcNow;

    [MaxLength(500)]
    public string? Description { get; set; }

    [ForeignKey(nameof(WalletId))]
    public Wallet? Wallet { get; set; }
}
