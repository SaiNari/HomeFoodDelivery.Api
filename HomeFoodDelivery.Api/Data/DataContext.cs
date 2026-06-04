using Microsoft.EntityFrameworkCore;
using HomeFoodDelivery.Api.Models;

namespace HomeFoodDelivery.Api.Data;

public class DataContext : DbContext
{
    public DataContext(DbContextOptions<DataContext> options) : base(options)
    {
    }

    public DbSet<User> Users => Set<User>();
    public DbSet<MealShift> MealShifts => Set<MealShift>();
    public DbSet<DailyMenu> DailyMenus => Set<DailyMenu>();
    public DbSet<Order> Orders => Set<Order>();

    protected override void OnModelCreating(ModelBuilder modelBuilder)
    {
        base.OnModelCreating(modelBuilder);

        // Enforce the unique database constraints we set up in DBeaver
        modelBuilder.Entity<User>()
            .HasIndex(u => u.PhoneNumber)
            .IsUnique();

        modelBuilder.Entity<Order>()
            .HasIndex(o => o.IdempotencyKey)
            .IsUnique();
    }
}