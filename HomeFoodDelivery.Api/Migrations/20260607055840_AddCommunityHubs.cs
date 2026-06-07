using Microsoft.EntityFrameworkCore.Migrations;

#nullable disable

namespace HomeFoodDelivery.Api.Migrations
{
    /// <inheritdoc />
    public partial class AddCommunityHubs : Migration
    {
        /// <inheritdoc />
        protected override void Up(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.AddColumn<string>(
                name: "DropOffInstructions",
                table: "DeliveryZones",
                type: "TEXT",
                nullable: true);

            migrationBuilder.AddColumn<string>(
                name: "HubContactNumber",
                table: "DeliveryZones",
                type: "TEXT",
                nullable: true);

            migrationBuilder.AddColumn<string>(
                name: "HubName",
                table: "DeliveryZones",
                type: "TEXT",
                nullable: true);

            migrationBuilder.UpdateData(
                table: "DeliveryZones",
                keyColumn: "ZoneId",
                keyValue: 1,
                columns: new[] { "DropOffInstructions", "HubContactNumber", "HubName" },
                values: new object[] { null, null, null });

            migrationBuilder.UpdateData(
                table: "DeliveryZones",
                keyColumn: "ZoneId",
                keyValue: 2,
                columns: new[] { "DropOffInstructions", "HubContactNumber", "HubName" },
                values: new object[] { null, null, null });

            migrationBuilder.UpdateData(
                table: "DeliveryZones",
                keyColumn: "ZoneId",
                keyValue: 3,
                columns: new[] { "DropOffInstructions", "HubContactNumber", "HubName" },
                values: new object[] { null, null, null });

            migrationBuilder.UpdateData(
                table: "DeliveryZones",
                keyColumn: "ZoneId",
                keyValue: 4,
                columns: new[] { "DropOffInstructions", "HubContactNumber", "HubName" },
                values: new object[] { null, null, null });
        }

        /// <inheritdoc />
        protected override void Down(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.DropColumn(
                name: "DropOffInstructions",
                table: "DeliveryZones");

            migrationBuilder.DropColumn(
                name: "HubContactNumber",
                table: "DeliveryZones");

            migrationBuilder.DropColumn(
                name: "HubName",
                table: "DeliveryZones");
        }
    }
}
