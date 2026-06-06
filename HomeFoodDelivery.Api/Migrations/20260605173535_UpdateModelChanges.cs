using System;
using Microsoft.EntityFrameworkCore.Migrations;

#nullable disable

#pragma warning disable CA1814 // Prefer jagged arrays over multidimensional

namespace HomeFoodDelivery.Api.Migrations
{
    /// <inheritdoc />
    public partial class UpdateModelChanges : Migration
    {
        /// <inheritdoc />
        protected override void Up(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.InsertData(
                table: "DailyMenus",
                columns: new[] { "MenuId", "AvailablePortions", "CookId", "CreatedAt", "Description", "DishName", "ImageUrl", "IsVegetarian", "MenuDate", "PreparationTimeMinutes", "PricePerPortion", "ShiftId" },
                values: new object[,]
                {
                    { 101, 30, 1, new DateTime(2026, 6, 5, 0, 0, 0, 0, DateTimeKind.Utc), "Spicy and crispy dosa with authentic Nellore karam.", "Nellore Karam Dosa", null, true, new DateTime(2026, 6, 5, 0, 0, 0, 0, DateTimeKind.Utc), 30, 70.00m, 1 },
                    { 102, 25, 3, new DateTime(2026, 6, 5, 0, 0, 0, 0, DateTimeKind.Utc), "Soft steaming idlis served with a cup of fresh ginger tea.", "Idli & Ginger Tea", null, true, new DateTime(2026, 6, 5, 0, 0, 0, 0, DateTimeKind.Utc), 30, 50.00m, 1 },
                    { 103, 20, 1, new DateTime(2026, 6, 5, 0, 0, 0, 0, DateTimeKind.Utc), "Classic local nati style donne chicken biryani.", "Nati Style Donne Chicken Biryani", null, true, new DateTime(2026, 6, 5, 0, 0, 0, 0, DateTimeKind.Utc), 30, 180.00m, 2 },
                    { 104, 15, 3, new DateTime(2026, 6, 5, 0, 0, 0, 0, DateTimeKind.Utc), "Slow-cooked authentic dum biryani with rich spices.", "Hyderabadi Chicken Dum Biryani", null, true, new DateTime(2026, 6, 5, 0, 0, 0, 0, DateTimeKind.Utc), 30, 240.00m, 2 },
                    { 105, 10, 1, new DateTime(2026, 6, 5, 0, 0, 0, 0, DateTimeKind.Utc), "Rich, flavorful, and tender mutton biryani.", "Nellore Mutton Biryani", null, true, new DateTime(2026, 6, 5, 0, 0, 0, 0, DateTimeKind.Utc), 30, 320.00m, 3 },
                    { 106, 12, 3, new DateTime(2026, 6, 5, 0, 0, 0, 0, DateTimeKind.Utc), "Premium rich halwa loaded with roasted dry fruits.", "Shahi Dry Fruit Halwa", null, true, new DateTime(2026, 6, 5, 0, 0, 0, 0, DateTimeKind.Utc), 30, 120.00m, 3 },
                    { 107, 15, 1, new DateTime(2026, 6, 5, 0, 0, 0, 0, DateTimeKind.Utc), "Creamy dessert overloaded with real fruits and nuts.", "Fruit N Nut Fantasy Ice Cream", null, true, new DateTime(2026, 6, 5, 0, 0, 0, 0, DateTimeKind.Utc), 30, 90.00m, 3 }
                });
        }

        /// <inheritdoc />
        protected override void Down(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.DeleteData(
                table: "DailyMenus",
                keyColumn: "MenuId",
                keyValue: 101);

            migrationBuilder.DeleteData(
                table: "DailyMenus",
                keyColumn: "MenuId",
                keyValue: 102);

            migrationBuilder.DeleteData(
                table: "DailyMenus",
                keyColumn: "MenuId",
                keyValue: 103);

            migrationBuilder.DeleteData(
                table: "DailyMenus",
                keyColumn: "MenuId",
                keyValue: 104);

            migrationBuilder.DeleteData(
                table: "DailyMenus",
                keyColumn: "MenuId",
                keyValue: 105);

            migrationBuilder.DeleteData(
                table: "DailyMenus",
                keyColumn: "MenuId",
                keyValue: 106);

            migrationBuilder.DeleteData(
                table: "DailyMenus",
                keyColumn: "MenuId",
                keyValue: 107);
        }
    }
}
