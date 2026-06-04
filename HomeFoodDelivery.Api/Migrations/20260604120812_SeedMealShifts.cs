using System;
using Microsoft.EntityFrameworkCore.Migrations;

#nullable disable

#pragma warning disable CA1814 // Prefer jagged arrays over multidimensional

namespace HomeFoodDelivery.Api.Migrations
{
    /// <inheritdoc />
    public partial class SeedMealShifts : Migration
    {
        /// <inheritdoc />
        protected override void Up(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.InsertData(
                table: "MealShifts",
                columns: new[] { "ShiftId", "CutoffTime", "DeliveryTime", "EntryOpenTime", "ShiftName" },
                values: new object[,]
                {
                    { 1, new TimeSpan(0, 7, 30, 0, 0), new TimeSpan(0, 8, 15, 0, 0), new TimeSpan(0, 6, 0, 0, 0), "Breakfast" },
                    { 2, new TimeSpan(0, 11, 0, 0, 0), new TimeSpan(0, 12, 0, 0, 0), new TimeSpan(0, 9, 0, 0, 0), "Lunch" },
                    { 3, new TimeSpan(0, 18, 30, 0, 0), new TimeSpan(0, 19, 30, 0, 0), new TimeSpan(0, 15, 0, 0, 0), "Dinner" }
                });
        }

        /// <inheritdoc />
        protected override void Down(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.DeleteData(
                table: "MealShifts",
                keyColumn: "ShiftId",
                keyValue: 1);

            migrationBuilder.DeleteData(
                table: "MealShifts",
                keyColumn: "ShiftId",
                keyValue: 2);

            migrationBuilder.DeleteData(
                table: "MealShifts",
                keyColumn: "ShiftId",
                keyValue: 3);
        }
    }
}
