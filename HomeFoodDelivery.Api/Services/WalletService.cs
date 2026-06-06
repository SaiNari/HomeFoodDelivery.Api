using Microsoft.EntityFrameworkCore;
using HomeFoodDelivery.Api.Data;
using HomeFoodDelivery.Api.Models;

namespace HomeFoodDelivery.Api.Services;

public class WalletService : IWalletService
{
    private readonly DataContext _context;

    public WalletService(DataContext context)
    {
        _context = context;
    }

    public async Task<Wallet> GetWalletAsync(int userId)
    {
        var wallet = await _context.Wallets.FirstOrDefaultAsync(w => w.UserId == userId);
        if (wallet == null)
        {
            wallet = new Wallet { UserId = userId, Balance = 0.00m };
            _context.Wallets.Add(wallet);
            await _context.SaveChangesAsync();
        }
        return wallet;
    }

    public async Task<bool> DebitBalanceAsync(int userId, decimal amount)
    {
        var wallet = await GetWalletAsync(userId);

        if (wallet.Balance < amount) return false; 

        wallet.Balance -= amount;
        wallet.Transactions.Add(new WalletTransaction
        {
            Amount = amount,
            Type = "Debit",
            Description = "Order Payment",
            Timestamp = DateTime.UtcNow
        });

        await _context.SaveChangesAsync();
        return true;
    }

    public async Task<bool> CreditBalanceAsync(int userId, decimal amount, string description)
    {
        var wallet = await GetWalletAsync(userId);
        wallet.Balance += amount;
        wallet.Transactions.Add(new WalletTransaction
        {
            Amount = amount,
            Type = "Credit",
            Description = description,
            Timestamp = DateTime.UtcNow
        });

        await _context.SaveChangesAsync();
        return true;
    }

    public async Task<bool> ProcessWalletPaymentAsync(int userId, decimal amount)
    {
        var wallet = await _context.Wallets.FirstOrDefaultAsync(w => w.UserId == userId);
        if (wallet == null || wallet.Balance < amount) return false;

        wallet.Balance -= amount;
        wallet.Transactions.Add(new WalletTransaction
        {
            Amount = amount,
            Type = "Debit",
            Description = "Order Payment"
        });

        await _context.SaveChangesAsync();
        return true;
    }
}