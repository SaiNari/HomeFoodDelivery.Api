using Microsoft.AspNetCore.Mvc;
using HomeFoodDelivery.Api.Services;
using HomeFoodDelivery.Api.Models;

namespace HomeFoodDelivery.Api.Controllers;

[Route("api/[controller]")]
[ApiController]
public class WalletsController : ControllerBase
{
    private readonly IWalletService _walletService;

    public WalletsController(IWalletService walletService)
    {
        _walletService = walletService;
    }

    // GET: api/Wallets/5
    [HttpGet("{userId}")]
    public async Task<ActionResult<Wallet>> GetWallet(int userId)
    {
        var wallet = await _walletService.GetWalletAsync(userId);
        return Ok(wallet);
    }

    // POST: api/Wallets/5/topup
    [HttpPost("{userId}/topup")]
    public async Task<IActionResult> TopUpWallet(int userId, [FromBody] TopUpRequest request)
    {
        if (request.Amount <= 0)
            return BadRequest(new { message = "Amount must be greater than zero." });

        bool success = await _walletService.CreditBalanceAsync(userId, request.Amount, "Added funds to Wallet");

        if (success)
            return Ok(new { message = $"Successfully added ₹{request.Amount} to your wallet." });

        return BadRequest(new { message = "Failed to top up wallet." });
    }
}

public class TopUpRequest
{
    public decimal Amount { get; set; }
}