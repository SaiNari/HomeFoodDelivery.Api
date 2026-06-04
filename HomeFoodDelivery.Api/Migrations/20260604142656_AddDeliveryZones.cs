using Microsoft.EntityFrameworkCore.Migrations;

#nullable disable

#pragma warning disable CA1814 // Prefer jagged arrays over multidimensional

namespace HomeFoodDelivery.Api.Migrations
{
    /// <inheritdoc />
    public partial class AddDeliveryZones : Migration
    {
        /// <inheritdoc />
        protected override void Up(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.AddColumn<int>(
                name: "ZoneId",
                table: "Users",
                type: "INTEGER",
                nullable: true);

            migrationBuilder.CreateTable(
                name: "DeliveryZones",
                columns: table => new
                {
                    ZoneId = table.Column<int>(type: "INTEGER", nullable: false)
                        .Annotation("Sqlite:Autoincrement", true),
                    TechParkName = table.Column<string>(type: "TEXT", maxLength: 100, nullable: false),
                    ServicingNeighborhoods = table.Column<string>(type: "TEXT", maxLength: 200, nullable: false),
                    IsActive = table.Column<bool>(type: "INTEGER", nullable: false)
                },
                constraints: table =>
                {
                    table.PrimaryKey("PK_DeliveryZones", x => x.ZoneId);
                });

            migrationBuilder.InsertData(
                table: "DeliveryZones",
                columns: new[] { "ZoneId", "IsActive", "ServicingNeighborhoods", "TechParkName" },
                values: new object[,]
                {
                    { 1, true, "CV Raman Nagar, Mahadevapura, Indiranagar", "Bagmane Tech Park" },
                    { 2, true, "Hebbal, Nagawara, Thanisandra", "Manyata Tech Park" },
                    { 3, true, "Bellandur, Marathahalli, Sarjapur Road", "RMZ Ecospace" },
                    { 4, true, "Electronic City, Bommanahalli", "Electronic City Phase 1" }
                });

            migrationBuilder.CreateIndex(
                name: "IX_Users_ZoneId",
                table: "Users",
                column: "ZoneId");

            migrationBuilder.AddForeignKey(
                name: "FK_Users_DeliveryZones_ZoneId",
                table: "Users",
                column: "ZoneId",
                principalTable: "DeliveryZones",
                principalColumn: "ZoneId");
        }

        /// <inheritdoc />
        protected override void Down(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.DropForeignKey(
                name: "FK_Users_DeliveryZones_ZoneId",
                table: "Users");

            migrationBuilder.DropTable(
                name: "DeliveryZones");

            migrationBuilder.DropIndex(
                name: "IX_Users_ZoneId",
                table: "Users");

            migrationBuilder.DropColumn(
                name: "ZoneId",
                table: "Users");
        }
    }
}
