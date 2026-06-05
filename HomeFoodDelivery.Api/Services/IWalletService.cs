using HomeFoodDelivery.Api.Models;

namespace HomeFoodDelivery.Api.Services;

public interface IWalletService
{
    Task<Wallet> GetWalletAsync(int userId);
    Task<bool> DebitBalanceAsync(int userId, decimal amount);
    Task<bool> CreditBalanceAsync(int userId, decimal amount, string description);
}