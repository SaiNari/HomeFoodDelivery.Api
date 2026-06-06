using Microsoft.EntityFrameworkCore;
using HomeFoodDelivery.Api.Models;

namespace HomeFoodDelivery.Api.Data;

public class DataContext : DbContext
{
    public DataContext(DbContextOptions<DataContext> options) : base(options)
    {
    }
    public DbSet<Wallet> Wallets { get; set; }
    public DbSet<WalletTransaction> WalletTransactions { get; set; }
    public DbSet<User> Users => Set<User>();
    public DbSet<MealShift> MealShifts => Set<MealShift>();
    public DbSet<DailyMenu> DailyMenus => Set<DailyMenu>();
    public DbSet<Order> Orders => Set<Order>();
    public DbSet<DeliveryZone> DeliveryZones { get; set; }
    public DbSet<Review> Reviews { get; set; }

    protected override void OnModelCreating(ModelBuilder modelBuilder)
    {
        base.OnModelCreating(modelBuilder);
        DateTime seedDate = new DateTime(2026, 6, 5, 0, 0, 0, DateTimeKind.Utc);

        string today = DateTime.UtcNow.ToString("yyyy-MM-dd");
        string timestamp = DateTime.UtcNow.ToString("O");

        modelBuilder.Entity<DailyMenu>().HasData(
        new DailyMenu { MenuId = 101, CookId = 1, ShiftId = 1, MenuDate = seedDate, DishName = "Nellore Karam Dosa", Description = "Spicy and crispy dosa with authentic Nellore karam.", AvailablePortions = 30, PricePerPortion = 70.00m, CreatedAt = seedDate },
        new DailyMenu { MenuId = 102, CookId = 3, ShiftId = 1, MenuDate = seedDate, DishName = "Idli & Ginger Tea", Description = "Soft steaming idlis served with a cup of fresh ginger tea.", AvailablePortions = 25, PricePerPortion = 50.00m, CreatedAt = seedDate },

        new DailyMenu { MenuId = 103, CookId = 1, ShiftId = 2, MenuDate = seedDate, DishName = "Nati Style Donne Chicken Biryani", Description = "Classic local nati style donne chicken biryani.", AvailablePortions = 20, PricePerPortion = 180.00m, CreatedAt = seedDate },
        new DailyMenu { MenuId = 104, CookId = 3, ShiftId = 2, MenuDate = seedDate, DishName = "Hyderabadi Chicken Dum Biryani", Description = "Slow-cooked authentic dum biryani with rich spices.", AvailablePortions = 15, PricePerPortion = 240.00m, CreatedAt = seedDate },

        new DailyMenu { MenuId = 105, CookId = 1, ShiftId = 3, MenuDate = seedDate, DishName = "Nellore Mutton Biryani", Description = "Rich, flavorful, and tender mutton biryani.", AvailablePortions = 10, PricePerPortion = 320.00m, CreatedAt = seedDate },

        new DailyMenu { MenuId = 106, CookId = 3, ShiftId = 3, MenuDate = seedDate, DishName = "Shahi Dry Fruit Halwa", Description = "Premium rich halwa loaded with roasted dry fruits.", AvailablePortions = 12, PricePerPortion = 120.00m, CreatedAt = seedDate },
        new DailyMenu { MenuId = 107, CookId = 1, ShiftId = 3, MenuDate = seedDate, DishName = "Fruit N Nut Fantasy Ice Cream", Description = "Creamy dessert overloaded with real fruits and nuts.", AvailablePortions = 15, PricePerPortion = 90.00m, CreatedAt = seedDate }
    );

        modelBuilder.Entity<User>()
            .HasIndex(u => u.PhoneNumber)
            .IsUnique();

        modelBuilder.Entity<Order>()
            .HasIndex(o => o.IdempotencyKey)
            .IsUnique();

        modelBuilder.Entity<MealShift>().HasData(
            new MealShift { ShiftId = 1, ShiftName = "Breakfast", EntryOpenTime = new TimeSpan(6, 0, 0), CutoffTime = new TimeSpan(7, 30, 0), DeliveryTime = new TimeSpan(8, 15, 0) },
            new MealShift { ShiftId = 2, ShiftName = "Lunch", EntryOpenTime = new TimeSpan(9, 0, 0), CutoffTime = new TimeSpan(11, 0, 0), DeliveryTime = new TimeSpan(12, 0, 0) },
            new MealShift { ShiftId = 3, ShiftName = "Dinner", EntryOpenTime = new TimeSpan(15, 0, 0), CutoffTime = new TimeSpan(18, 30, 0), DeliveryTime = new TimeSpan(19, 30, 0) }
        );

        modelBuilder.Entity<DeliveryZone>().HasData(
            new DeliveryZone { ZoneId = 1, TechParkName = "Bagmane Tech Park", ServicingNeighborhoods = "CV Raman Nagar, Mahadevapura, Indiranagar" },
            new DeliveryZone { ZoneId = 2, TechParkName = "Manyata Tech Park", ServicingNeighborhoods = "Hebbal, Nagawara, Thanisandra" },
            new DeliveryZone { ZoneId = 3, TechParkName = "RMZ Ecospace", ServicingNeighborhoods = "Bellandur, Marathahalli, Sarjapur Road" },
            new DeliveryZone { ZoneId = 4, TechParkName = "Electronic City Phase 1", ServicingNeighborhoods = "Electronic City, Bommanahalli" }
        );

        // Configure Review entity relationships
        modelBuilder.Entity<Review>()
            .HasOne(r => r.Cook)
            .WithMany()
            .HasForeignKey(r => r.CookId)
            .OnDelete(DeleteBehavior.Cascade);

        modelBuilder.Entity<Review>()
            .HasOne(r => r.Customer)
            .WithMany()
            .HasForeignKey(r => r.CustomerId)
            .OnDelete(DeleteBehavior.Cascade);
    }
}