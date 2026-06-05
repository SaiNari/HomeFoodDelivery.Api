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
    public DbSet<DeliveryZone> DeliveryZones { get; set; }

    protected override void OnModelCreating(ModelBuilder modelBuilder)
    {
        base.OnModelCreating(modelBuilder);

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
    }
}